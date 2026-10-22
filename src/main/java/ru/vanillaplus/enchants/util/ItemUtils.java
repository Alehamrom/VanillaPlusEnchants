package ru.vanillaplus.enchants.util;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Random;

/**
 * Utilities for manipulating items and tool durability.
 */
public final class ItemUtils {

    private static final Random RANDOM = new Random();

    private ItemUtils() {
    }

    /**
     * Damages a tool by the specified amount, accounting for Unbreaking and creative mode.
     *
     * @param player the player using the tool
     * @param item the tool item stack
     * @param amount the raw durability damage to apply
     */
    public static void damageTool(Player player, ItemStack item, int amount) {
        if (player != null && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (item == null || !(item.getItemMeta() instanceof Damageable damageable) || amount <= 0) {
            return;
        }

        int unbreakingLevel = item.getEnchantmentLevel(Enchantment.UNBREAKING);
        int damageToApply = 0;

        for (int i = 0; i < amount; i++) {
            if (unbreakingLevel > 0) {
                // Vanilla unbreaking formula for tools: 1 / (level + 1)
                if (RANDOM.nextInt(unbreakingLevel + 1) == 0) {
                    damageToApply++;
                }
            } else {
                damageToApply++;
            }
        }

        if (damageToApply <= 0) {
            return;
        }

        int newDamage = damageable.getDamage() + damageToApply;
        int maxDurability = item.getType().getMaxDurability();

        if (maxDurability > 0 && newDamage >= maxDurability) {
            // Tool breaks
            item.setAmount(0);
            if (player != null) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                ItemStack inHand = player.getInventory().getItemInMainHand();
                if (inHand == item || inHand.equals(item)) {
                    player.getInventory().setItemInMainHand(null);
                }
            }
        } else {
            damageable.setDamage(newDamage);
            item.setItemMeta(damageable);
        }
    }
}
