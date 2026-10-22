package ru.vanillaplus.enchants.listeners;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import ru.vanillaplus.enchants.VanillaPlusEnchants;
import ru.vanillaplus.enchants.config.ConfigManager;
import ru.vanillaplus.enchants.config.EnchantConfig;
import ru.vanillaplus.enchants.enchant.EnchantmentManager;
import ru.vanillaplus.enchants.model.CustomEnchantment;
import ru.vanillaplus.enchants.model.ItemTarget;

/**
 * Handles Soul Siphon (lifesteal on kill from mobs with max health > 8 hearts).
 */
public class SoulSiphonListener implements Listener {

    private final VanillaPlusEnchants plugin;
    private final EnchantmentManager enchantmentManager;
    private final ConfigManager configManager;

    public SoulSiphonListener(VanillaPlusEnchants plugin) {
        this.plugin = plugin;
        this.enchantmentManager = plugin.getEnchantmentManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof ArmorStand) {
            return;
        }

        Player killer = victim.getKiller();
        if (killer == null || !killer.isOnline() || killer.isDead()) {
            return;
        }

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        // Target: Swords & Axes only
        if (!ItemTarget.SWORD_AND_AXE.matches(weapon.getType())) {
            return;
        }

        int level = enchantmentManager.getLevel(weapon, CustomEnchantment.SOUL_SIPHON);
        if (level <= 0) {
            return;
        }

        EnchantConfig.SoulSiphonConfig config = configManager.getSoulSiphon();
        if (!config.enabled) {
            return;
        }

        // Condition: Target mob must have MAX health > 8 hearts (16 HP)
        // Prevents farming on cows, pigs, chickens, bats, sheep.
        double victimMaxHealth = getEntityMaxHealth(victim);
        if (victimMaxHealth <= config.minTargetMaxHealth) {
            return;
        }

        // Healing: 1: 0.5 heart (1 HP), 2: 1.0 heart (2 HP), 3: 1.5 hearts (3 HP)
        double healAmount = config.getHeal(level);
        double currentHealth = killer.getHealth();
        double playerMaxHealth = getEntityMaxHealth(killer);

        if (currentHealth < playerMaxHealth) {
            killer.setHealth(Math.min(playerMaxHealth, currentHealth + healAmount));
        }

        // Visual soul escape particles and audio
        Location loc = victim.getLocation().add(0, victim.getHeight() * 0.5, 0);
        victim.getWorld().spawnParticle(Particle.SOUL, loc, 12, 0.3, 0.5, 0.3, 0.05);
        victim.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 6, 0.2, 0.4, 0.2, 0.02);
        victim.getWorld().playSound(loc, Sound.PARTICLE_SOUL_ESCAPE, 0.8f, 1.2f);
    }

    private double getEntityMaxHealth(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            return attr.getValue();
        }
        return entity.getMaxHealth();
    }
}
