package ru.vanillaplus.enchants.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.vanillaplus.enchants.VanillaPlusEnchants;
import ru.vanillaplus.enchants.config.ConfigManager;
import ru.vanillaplus.enchants.enchant.EnchantmentManager;
import ru.vanillaplus.enchants.model.CustomEnchantment;

import java.util.Map;

/**
 * Handles Anvil combination of items and custom enchanted books,
 * upgrading levels, and checking for incompatibilities.
 */
public class AnvilListener implements Listener {

    private final VanillaPlusEnchants plugin;
    private final EnchantmentManager enchantmentManager;
    private final ConfigManager configManager;

    public AnvilListener(VanillaPlusEnchants plugin) {
        this.plugin = plugin;
        this.enchantmentManager = plugin.getEnchantmentManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack left = inv.getFirstItem();
        ItemStack right = inv.getSecondItem();

        if (left == null || left.getType().isAir() || right == null || right.getType().isAir()) {
            return;
        }

        Map<CustomEnchantment, Integer> leftCustom = enchantmentManager.getEnchantments(left);
        Map<CustomEnchantment, Integer> rightCustom = enchantmentManager.getEnchantments(right);

        // If neither item has custom enchantments, vanilla handles combination
        if (rightCustom.isEmpty() && leftCustom.isEmpty()) {
            return;
        }

        // Check if right item has vanilla enchants incompatible with left custom enchants
        if (!leftCustom.isEmpty()) {
            for (CustomEnchantment leftEnchant : leftCustom.keySet()) {
                if (enchantmentManager.hasIncompatibleVanilla(right, leftEnchant)) {
                    event.setResult(null);
                    inv.setRepairCost(0);
                    return;
                }
            }
        }

        // If right item has no custom enchantments, let vanilla handle combination unless left had custom enchants
        if (rightCustom.isEmpty()) {
            // If left had custom enchants, preserve them in vanilla result
            ItemStack vanillaResult = event.getResult();
            if (vanillaResult != null && !vanillaResult.getType().isAir()) {
                ItemStack preserved = vanillaResult.clone();
                for (Map.Entry<CustomEnchantment, Integer> entry : leftCustom.entrySet()) {
                    preserved = enchantmentManager.applyEnchantment(preserved, entry.getKey(), entry.getValue());
                }
                event.setResult(preserved);
            }
            return;
        }

        // Right item HAS custom enchantments: enforce strict vanilla item type rules
        boolean isLeftBook = left.getType() == Material.ENCHANTED_BOOK;
        boolean isRightBook = right.getType() == Material.ENCHANTED_BOOK;

        // Anvil rules:
        // 1. Regular Material.BOOK cannot receive or transfer enchantments in an anvil
        if (left.getType() == Material.BOOK || right.getType() == Material.BOOK) {
            event.setResult(null);
            inv.setRepairCost(0);
            return;
        }

        // 2. If left is an enchanted book, right MUST also be an enchanted book (cannot combine tool into book)
        if (isLeftBook && !isRightBook) {
            event.setResult(null);
            inv.setRepairCost(0);
            return;
        }

        // 3. If right is NOT an enchanted book, left and right MUST be the exact same material
        if (!isRightBook && left.getType() != right.getType()) {
            event.setResult(null);
            inv.setRepairCost(0);
            return;
        }

        // 4. Target validation and vanilla incompatibility checks for right custom enchantments
        for (CustomEnchantment rightEnchant : rightCustom.keySet()) {
            if (!isLeftBook && !rightEnchant.getTarget().matches(left.getType())) {
                event.setResult(null);
                inv.setRepairCost(0);
                return;
            }

            if (enchantmentManager.hasIncompatibleVanilla(left, rightEnchant)) {
                event.setResult(null);
                inv.setRepairCost(0);
                return;
            }
        }

        // Starting item: use vanilla result if available, or clone left
        ItemStack vanillaResult = event.getResult();
        ItemStack result;
        if (vanillaResult != null && !vanillaResult.getType().isAir()) {
            result = vanillaResult.clone();
        } else {
            result = left.clone();
            result.setAmount(1);
        }

        int totalCost = Math.max(1, inv.getRepairCost());
        boolean changesApplied = false;

        // Apply all custom enchants from left first
        for (Map.Entry<CustomEnchantment, Integer> entry : leftCustom.entrySet()) {
            result = enchantmentManager.applyEnchantment(result, entry.getKey(), entry.getValue());
        }

        // Now merge custom enchants from right
        for (Map.Entry<CustomEnchantment, Integer> entry : rightCustom.entrySet()) {
            CustomEnchantment enchant = entry.getKey();
            int rightLvl = entry.getValue();
            int leftLvl = leftCustom.getOrDefault(enchant, 0);
            int finalLvl;

            if (leftLvl == 0) {
                finalLvl = rightLvl;
            } else if (leftLvl == rightLvl) {
                finalLvl = Math.min(enchant.getMaxLevel(), leftLvl + 1);
            } else {
                finalLvl = Math.max(leftLvl, rightLvl);
            }

            if (finalLvl > leftLvl || leftLvl == 0) {
                changesApplied = true;
                totalCost += configManager.getAnvilBaseCost() + (finalLvl * configManager.getAnvilLevelMultiplier());
            }

            result = enchantmentManager.applyEnchantment(result, enchant, finalLvl);
        }

        // Handle rename if user typed a custom name
        String renameText = inv.getRenameText();
        boolean renamed = false;
        if (renameText != null && !renameText.trim().isEmpty()) {
            ItemMeta meta = result.getItemMeta();
            meta.displayName(Component.text(renameText.trim()));
            result.setItemMeta(meta);
            totalCost += 1;
            renamed = true;
        }

        // Prevent zero-benefit combinations (e.g. merging sword with lower/equal level when no repair or rename occurs)
        boolean hasVanillaBenefit = vanillaResult != null && !vanillaResult.getType().isAir();
        if (!changesApplied && !hasVanillaBenefit && !renamed) {
            event.setResult(null);
            inv.setRepairCost(0);
            return;
        }

        inv.setMaximumRepairCost(Math.max(40, totalCost + 10));
        inv.setRepairCost(totalCost);
        event.setResult(result);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory() instanceof AnvilInventory inv)) {
            return;
        }

        if (event.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }

        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType().isAir()) {
            return;
        }

        if (enchantmentManager.getEnchantments(result).isEmpty()) {
            return;
        }

        int cost = inv.getRepairCost();
        if (player.getGameMode() != GameMode.CREATIVE && player.getLevel() < cost) {
            event.setCancelled(true);
            return;
        }

        // Handle stack preservation in slot 1 (if a stack of custom books was placed)
        ItemStack right = inv.getSecondItem();
        if (right != null && right.getAmount() > 1) {
            ItemStack leftover = right.clone();
            leftover.setAmount(right.getAmount() - 1);
            plugin.getServer().getScheduler().runTask(plugin, () -> inv.setSecondItem(leftover));
        }

        // NOTE: We do NOT call player.setLevel(...) here.
        // Minecraft's native AnvilMenu.onTake() handles level deduction, anvil damage,
        // and item consumption automatically based on inv.getRepairCost().
    }
}
