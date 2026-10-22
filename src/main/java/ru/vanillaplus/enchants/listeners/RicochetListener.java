package ru.vanillaplus.enchants.listeners;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import ru.vanillaplus.enchants.VanillaPlusEnchants;
import ru.vanillaplus.enchants.config.ConfigManager;
import ru.vanillaplus.enchants.config.EnchantConfig;
import ru.vanillaplus.enchants.enchant.EnchantmentManager;
import ru.vanillaplus.enchants.model.CustomEnchantment;
import ru.vanillaplus.enchants.model.ItemTarget;

import java.util.Comparator;
import java.util.Optional;
import java.util.Random;

/**
 * Handles Ricochet enchantment: arrows have a 33% chance to bounce to a nearby enemy with 50% damage.
 */
public class RicochetListener implements Listener {

    private final VanillaPlusEnchants plugin;
    private final EnchantmentManager enchantmentManager;
    private final ConfigManager configManager;
    private final NamespacedKey arrowLevelKey;
    private final NamespacedKey arrowBouncedKey;
    private final Random random = new Random();

    public RicochetListener(VanillaPlusEnchants plugin) {
        this.plugin = plugin;
        this.enchantmentManager = plugin.getEnchantmentManager();
        this.configManager = plugin.getConfigManager();
        this.arrowLevelKey = new NamespacedKey(plugin, "arrow_ricochet_level");
        this.arrowBouncedKey = new NamespacedKey(plugin, "arrow_ricochet_bounced");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShootBow(EntityShootBowEvent event) {
        ItemStack bow = event.getBow();
        if (bow == null || !ItemTarget.BOW_AND_CROSSBOW.matches(bow.getType())) {
            return;
        }

        // Incompatible with Infinity and Piercing
        if (bow.containsEnchantment(Enchantment.INFINITY) || bow.containsEnchantment(Enchantment.PIERCING)) {
            return;
        }

        int level = enchantmentManager.getLevel(bow, CustomEnchantment.RICOCHET);
        if (level <= 0) {
            return;
        }

        EnchantConfig.RicochetConfig config = configManager.getRicochet();
        if (!config.enabled) {
            return;
        }

        if (event.getProjectile() instanceof AbstractArrow arrow) {
            PersistentDataContainer pdc = arrow.getPersistentDataContainer();
            pdc.set(arrowLevelKey, PersistentDataType.INTEGER, level);
            pdc.set(arrowBouncedKey, PersistentDataType.BYTE, (byte) 0);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof AbstractArrow arrow)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim) || victim instanceof ArmorStand) {
            return;
        }

        PersistentDataContainer pdc = arrow.getPersistentDataContainer();
        if (!pdc.has(arrowLevelKey, PersistentDataType.INTEGER)) {
            return;
        }

        int level = pdc.getOrDefault(arrowLevelKey, PersistentDataType.INTEGER, 0);
        if (level <= 0) {
            return;
        }

        byte bounced = pdc.getOrDefault(arrowBouncedKey, PersistentDataType.BYTE, (byte) 0);
        if (bounced != 0) {
            // Prevent recursive infinite bouncing
            return;
        }

        EnchantConfig.RicochetConfig config = configManager.getRicochet();
        if (!config.enabled) {
            return;
        }

        // 33% chance trigger
        if (random.nextDouble() * 100.0 >= config.chancePercent) {
            return;
        }

        // Mark this arrow as bounced
        pdc.set(arrowBouncedKey, PersistentDataType.BYTE, (byte) 1);

        double radius = config.getRadius(level);
        World world = victim.getWorld();
        Location victimCenter = victim.getLocation().add(0, victim.getHeight() * 0.5, 0);

        // Find nearest second enemy within radius
        Optional<LivingEntity> secondaryTarget = world.getNearbyEntities(victimCenter, radius, radius, radius)
                .stream()
                .filter(e -> e instanceof LivingEntity && !(e instanceof ArmorStand))
                .map(e -> (LivingEntity) e)
                .filter(e -> isValidSecondaryTarget(arrow.getShooter(), victim, e))
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(victimCenter)));

        if (secondaryTarget.isEmpty()) {
            return;
        }

        LivingEntity target = secondaryTarget.get();
        Location targetCenter = target.getLocation().add(0, target.getHeight() * 0.5, 0);

        // Calculate bounce direction
        Vector direction = targetCenter.toVector().subtract(victimCenter.toVector());
        double targetDistance = direction.length();
        if (targetDistance <= 0.001) {
            return;
        }
        direction.normalize();

        // 50% of the original arrow damage
        double originalDamage = event.getFinalDamage();
        double secondaryDamage = Math.max(1.0, originalDamage * config.damageMultiplier);
        double spawnOffset = Math.min(0.25, Math.max(0.05, targetDistance * 0.25));

        // Spawn bounced arrow flying towards secondary target
        world.spawn(victimCenter.clone().add(direction.clone().multiply(spawnOffset)), Arrow.class, bouncedArrow -> {
            bouncedArrow.setVelocity(direction.multiply(1.5));
            bouncedArrow.setDamage(secondaryDamage / 1.5); // 1.5 speed * (secondaryDamage / 1.5) = exact secondaryDamage
            if (arrow.getShooter() instanceof org.bukkit.projectiles.ProjectileSource source) {
                bouncedArrow.setShooter(source);
            }
            bouncedArrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            bouncedArrow.setCritical(false);
            bouncedArrow.setFireTicks(arrow.getFireTicks());

            PersistentDataContainer bouncePdc = bouncedArrow.getPersistentDataContainer();
            bouncePdc.set(arrowLevelKey, PersistentDataType.INTEGER, level);
            bouncePdc.set(arrowBouncedKey, PersistentDataType.BYTE, (byte) 1); // Already bounced!
        });

        // Visual and auditory ricochet effects
        world.spawnParticle(Particle.CRIT, victimCenter, 12, 0.2, 0.2, 0.2, 0.15);
        world.spawnParticle(Particle.SWEEP_ATTACK, victimCenter, 1, 0, 0, 0, 0);
        world.playSound(victimCenter, Sound.ITEM_SHIELD_BLOCK, 0.8f, 1.8f);
        world.playSound(victimCenter, Sound.ENTITY_ARROW_HIT, 1.0f, 1.2f);
    }

    private boolean isValidSecondaryTarget(Object shooter, LivingEntity primaryVictim, LivingEntity candidate) {
        if (candidate.equals(primaryVictim) || candidate.isDead()) {
            return false;
        }
        if (shooter != null && candidate.equals(shooter)) {
            return false;
        }
        // Ensure line of sight between primary victim and candidate so arrow doesn't hit a wall
        if (!primaryVictim.hasLineOfSight(candidate)) {
            return false;
        }
        if (shooter instanceof Player player && candidate instanceof Tameable tameable && tameable.isTamed()) {
            if (player.getUniqueId().equals(tameable.getOwnerUniqueId())) {
                return false;
            }
        }
        if (candidate instanceof Player candidatePlayer) {
            if (candidatePlayer.getGameMode().name().equals("CREATIVE") || candidatePlayer.getGameMode().name().equals("SPECTATOR")) {
                return false;
            }
        }
        return true;
    }
}
