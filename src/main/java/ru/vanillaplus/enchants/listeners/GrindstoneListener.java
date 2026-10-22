package ru.vanillaplus.enchants.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import ru.vanillaplus.enchants.VanillaPlusEnchants;
import ru.vanillaplus.enchants.config.ConfigManager;
import ru.vanillaplus.enchants.enchant.EnchantmentManager;
import ru.vanillaplus.enchants.model.CustomEnchantment;

import java.util.Map;

/**
 * Handles Grindstone disenchantment: removes custom enchantments and restores exp.
 */
public class GrindstoneListener implements Listener {

    private final VanillaPlusEnchants plugin;
    private final EnchantmentManager enchantmentManager;
    private final ConfigManager configManager;

    public GrindstoneListener(VanillaPlusEnchants plugin) {
        this.plugin = plugin;
        this.enchantmentManager = plugin.getEnchantmentManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        GrindstoneInventory inv = event.getInventory();
        ItemStack upper = inv.getUpperItem();
        ItemStack lower = inv.getLowerItem();

        Map<CustomEnchantment, Integer> upperEnchants = enchantmentManager.getEnchantments(upper);
        Map<CustomEnchantment, Integer> lowerEnchants = enchantmentManager.getEnchantments(lower);

        if (upperEnchants.isEmpty() && lowerEnchants.isEmpty()) {
            return;
        }

        ItemStack baseItem = (upper != null && !upper.getType().isAir()) ? upper : lower;
        if (baseItem == null || baseItem.getType().isAir()) {
            return;
        }

        // If vanilla already provided a result, clean custom enchants from it; otherwise use baseItem cleaned
        ItemStack vanillaResult = event.getResult();
        ItemStack result = (vanillaResult != null && !vanillaResult.getType().isAir())
                ? vanillaResult.clone()
                : baseItem.clone();

        result = enchantmentManager.removeAllEnchantments(result);
        event.setResult(result);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory() instanceof GrindstoneInventory inv)) {
            return;
        }

        if (event.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }

        ItemStack upper = inv.getUpperItem();
        ItemStack lower = inv.getLowerItem();

        int totalLevels = 0;
        for (int lvl : enchantmentManager.getEnchantments(upper).values()) {
            totalLevels += lvl;
        }
        for (int lvl : enchantmentManager.getEnchantments(lower).values()) {
            totalLevels += lvl;
        }

        if (totalLevels <= 0) {
            return;
        }

        int expToAward = totalLevels * configManager.getGrindstoneExpPerLevel();
        if (expToAward > 0) {
            player.giveExp(expToAward);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.0f, 1.0f);
    }
}
