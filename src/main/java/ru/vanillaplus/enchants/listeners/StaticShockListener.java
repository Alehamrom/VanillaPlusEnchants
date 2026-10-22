package ru.vanillaplus.enchants.listeners;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import ru.vanillaplus.enchants.VanillaPlusEnchants;
import ru.vanillaplus.enchants.config.ConfigManager;
import ru.vanillaplus.enchants.config.EnchantConfig;
import ru.vanillaplus.enchants.enchant.CooldownManager;
import ru.vanillaplus.enchants.enchant.EnchantmentManager;
import ru.vanillaplus.enchants.model.CustomEnchantment;
import ru.vanillaplus.enchants.model.ItemTarget;
import ru.vanillaplus.enchants.util.ParticleUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles Static Shock (Chain Lightning) on critical jump attacks with swords.
 */
public class StaticShockListener implements Listener {

    private final VanillaPlusEnchants plugin;
    private final EnchantmentManager enchantmentManager;
    private final CooldownManager cooldownManager;
    private final ConfigManager configManager;

    public StaticShockListener(VanillaPlusEnchants plugin) {
        this.plugin = plugin;
        this.enchantmentManager = plugin.getEnchantmentManager();
        this.cooldownManager = plugin.getCooldownManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim) || victim instanceof ArmorStand) {
            return;
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();
        // Target: Swords only!
        if (!ItemTarget.SWORD.matches(weapon.getType())) {
            return;
        }

        int level = enchantmentManager.getLevel(weapon, CustomEnchantment.STATIC_SHOCK);
        if (level <= 0) {
            return;
        }

        EnchantConfig.StaticShockConfig config = configManager.getStaticShock();
        if (!config.enabled) {
            return;
        }

        // Trigger: Critical jump attack
        if (!event.isCritical() && !isCriticalAttack(player)) {
            return;
        }

        // Internal cooldown check (default 5.0 seconds)
        if (cooldownManager.isOnCooldown(player.getUniqueId(), "static_shock")) {
            return;
        }

        cooldownManager.setCooldown(player.getUniqueId(), "static_shock", config.cooldownSeconds);

        // Chain lightning to nearby enemies
        double radius = config.chainRadius;
        int maxTargets = Math.min(config.maxTargets, Math.max(config.minTargets, level));
        double damage = config.getDamage(level);

        Location victimLoc = victim.getLocation();
        World world = victim.getWorld();

        List<LivingEntity> nearbyTargets = world.getNearbyEntities(victimLoc, radius, radius, radius)
                .stream()
                .filter(e -> e instanceof LivingEntity && !(e instanceof ArmorStand))
                .map(e -> (LivingEntity) e)
                .filter(e -> isValidChainTarget(player, victim, e))
                .sorted(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(victimLoc)))
                .limit(maxTargets)
                .collect(Collectors.toList());

        Location sourceLoc = victimLoc.clone().add(0, victim.getHeight() * 0.5, 0);

        for (LivingEntity target : nearbyTargets) {
            Location targetLoc = target.getLocation().clone().add(0, target.getHeight() * 0.5, 0);

            // Draw electric arc beam (NO FIRE!)
            ParticleUtils.drawParticleLine(world, sourceLoc, targetLoc, Particle.ELECTRIC_SPARK, 0.3);
            world.spawnParticle(Particle.FLASH, targetLoc, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.ELECTRIC_SPARK, targetLoc, 6, 0.2, 0.2, 0.2, 0.1);

            // Apply electric damage
            target.damage(damage, player);

            // Chain originates from subsequent target
            sourceLoc = targetLoc;
        }

        // Sound effect (thunder / electric crackle without fire)
        world.playSound(victimLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
    }

    private boolean isCriticalAttack(Player player) {
        return player.getFallDistance() > 0.0F
                && !player.isOnGround()
                && !player.isClimbing()
                && !player.isInWater()
                && !player.isInsideVehicle()
                && !player.isSprinting()
                && !player.hasPotionEffect(PotionEffectType.BLINDNESS)
                && !player.hasPotionEffect(PotionEffectType.SLOW_FALLING)
                && player.getAttackCooldown() > 0.85F;
    }

    private boolean isValidChainTarget(Player player, LivingEntity primaryVictim, LivingEntity candidate) {
        if (candidate.equals(primaryVictim) || candidate.equals(player) || candidate.isDead()) {
            return false;
        }
        // Don't chain to player's tamed pets
        if (candidate instanceof Tameable tameable && tameable.isTamed()) {
            if (player.getUniqueId().equals(tameable.getOwnerUniqueId())) {
                return false;
            }
        }
        // If candidate is a player, check if player vs player damage is allowed
        if (candidate instanceof Player candidatePlayer) {
            if (candidatePlayer.getGameMode().name().equals("CREATIVE") || candidatePlayer.getGameMode().name().equals("SPECTATOR")) {
                return false;
            }
        }
        return true;
    }
}
