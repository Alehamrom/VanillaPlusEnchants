package ru.vanillaplus.enchants.listeners;

import org.bukkit.GameEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.GenericGameEvent;
import org.bukkit.inventory.ItemStack;
import ru.vanillaplus.enchants.VanillaPlusEnchants;
import ru.vanillaplus.enchants.config.ConfigManager;
import ru.vanillaplus.enchants.config.EnchantConfig;
import ru.vanillaplus.enchants.enchant.EnchantmentManager;
import ru.vanillaplus.enchants.model.CustomEnchantment;

import java.util.Set;

/**
 * Handles Silent Step: suppresses acoustic vibrations for Sculk Sensors and Warden,
 * and applies -30% movement speed debuff while leggings are worn.
 */
public class SilentStepListener implements Listener {

    private final VanillaPlusEnchants plugin;
    private final EnchantmentManager enchantmentManager;
    private final ConfigManager configManager;
    private final NamespacedKey modifierKey;

    private static final Set<GameEvent> VIBRATION_EVENTS = Set.of(
            GameEvent.STEP,
            GameEvent.HIT_GROUND,
            GameEvent.SWIM,
            GameEvent.SPLASH,
            GameEvent.ENTITY_ACTION
    );

    public SilentStepListener(VanillaPlusEnchants plugin) {
        this.plugin = plugin;
        this.enchantmentManager = plugin.getEnchantmentManager();
        this.configManager = plugin.getConfigManager();
        this.modifierKey = new NamespacedKey(plugin, "silent_step_speed_debuff");
    }

    /**
     * Suppresses block-received acoustic vibrations (Sculk Sensors and Sculk Shriekers).
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockReceiveGameEvent(BlockReceiveGameEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (isWearingSilentStep(player) && VIBRATION_EVENTS.contains(event.getEvent())) {
            event.setCancelled(true);
        }
    }

    /**
     * Suppresses generic world-received game events (Warden, Sculk Catalysts).
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGenericGameEvent(GenericGameEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (isWearingSilentStep(player) && VIBRATION_EVENTS.contains(event.getEvent())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeSpeedDebuff(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        removeSpeedDebuff(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> updatePlayerDebuff(event.getPlayer()));
    }

    @EventHandler
    public void onArmorChange(com.destroystokyo.paper.event.player.PlayerArmorChangeEvent event) {
        if (event.getSlotType() == com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType.LEGS) {
            updatePlayerDebuff(event.getPlayer());
        }
    }

    /**
     * Checks if the player is currently wearing leggings with Silent Step.
     */
    public boolean isWearingSilentStep(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        ItemStack leggings = player.getInventory().getLeggings();
        if (leggings == null || leggings.getType().isAir()) {
            return false;
        }
        // Incompatible with Swift Sneak
        if (leggings.containsEnchantment(org.bukkit.enchantments.Enchantment.SWIFT_SNEAK)) {
            return false;
        }
        return enchantmentManager.hasEnchantment(leggings, CustomEnchantment.SILENT_STEP);
    }

    /**
     * Updates the -30% movement speed modifier on the player depending on whether Silent Step leggings are worn.
     */
    public void updatePlayerDebuff(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        EnchantConfig.SilentStepConfig config = configManager.getSilentStep();
        if (!config.enabled) {
            removeSpeedDebuff(player);
            return;
        }

        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr == null) {
            return;
        }

        boolean wearing = isWearingSilentStep(player);
        AttributeModifier existing = getExistingModifier(attr);

        if (wearing) {
            double reduction = Math.abs(config.speedReductionPercent) / 100.0;
            double modifierAmount = -reduction; // -0.30

            if (existing == null) {
                AttributeModifier modifier = new AttributeModifier(modifierKey, modifierAmount, AttributeModifier.Operation.ADD_SCALAR);
                attr.addModifier(modifier);
            } else if (Math.abs(existing.getAmount() - modifierAmount) > 0.001) {
                attr.removeModifier(existing);
                AttributeModifier modifier = new AttributeModifier(modifierKey, modifierAmount, AttributeModifier.Operation.ADD_SCALAR);
                attr.addModifier(modifier);
            }
        } else {
            if (existing != null) {
                attr.removeModifier(existing);
            }
        }
    }

    /**
     * Removes the speed debuff from the player.
     */
    public void removeSpeedDebuff(Player player) {
        if (player == null) return;
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr == null) return;

        AttributeModifier existing = getExistingModifier(attr);
        if (existing != null) {
            attr.removeModifier(existing);
        }
    }

    /**
     * Removes the debuff from all online players (e.g. on disable or reload).
     */
    public void cleanupAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            removeSpeedDebuff(player);
        }
    }

    private AttributeModifier getExistingModifier(AttributeInstance attr) {
        for (AttributeModifier mod : attr.getModifiers()) {
            if (modifierKey.equals(mod.getKey())) {
                return mod;
            }
        }
        return null;
    }
}
