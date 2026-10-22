package ru.vanillaplus.enchants.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ru.vanillaplus.enchants.VanillaPlusEnchants;
import ru.vanillaplus.enchants.config.ConfigManager;
import ru.vanillaplus.enchants.config.EnchantConfig;
import ru.vanillaplus.enchants.enchant.EnchantmentManager;
import ru.vanillaplus.enchants.enchant.SmeltRegistry;
import ru.vanillaplus.enchants.model.CustomEnchantment;
import ru.vanillaplus.enchants.model.SmeltResult;
import ru.vanillaplus.enchants.util.ItemUtils;

/**
 * Handles Smelting Touch behavior on block break.
 */
public class SmeltingTouchListener implements Listener {

    private final VanillaPlusEnchants plugin;
    private final EnchantmentManager enchantmentManager;
    private final SmeltRegistry smeltRegistry;
    private final ConfigManager configManager;

    public SmeltingTouchListener(VanillaPlusEnchants plugin) {
        this.plugin = plugin;
        this.enchantmentManager = plugin.getEnchantmentManager();
        this.smeltRegistry = plugin.getSmeltRegistry();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!enchantmentManager.hasEnchantment(tool, CustomEnchantment.SMELTING_TOUCH)) {
            return;
        }

        EnchantConfig.SmeltingTouchConfig config = configManager.getSmeltingTouch();
        if (!config.enabled) {
            return;
        }

        // Incompatible with Silk Touch and Fortune
        if (tool.containsEnchantment(Enchantment.SILK_TOUCH) || tool.containsEnchantment(Enchantment.FORTUNE)) {
            return;
        }

        Block block = event.getBlock();
        Material blockType = block.getType();

        // Check if block is smeltable
        if (!smeltRegistry.isSmeltable(blockType)) {
            // Unsmeltable blocks cost 1 durability (vanilla default applies)
            return;
        }

        // Validate harvest capability: tool must be able to harvest the block
        // E.g. a wooden pickaxe cannot harvest iron ore or gold ore
        java.util.Collection<ItemStack> naturalDrops = block.getDrops(tool, player);
        if (naturalDrops.isEmpty()) {
            return;
        }

        SmeltResult blockResult = smeltRegistry.getResult(blockType);
        if (blockResult == null) {
            return;
        }

        // Smeltable block: cancel vanilla drop, drop smelted items
        event.setDropItems(false);

        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        World world = block.getWorld();
        int totalExp = 0;

        for (ItemStack drop : naturalDrops) {
            SmeltResult dropResult = smeltRegistry.getResult(drop.getType());
            if (dropResult != null) {
                int dropAmount = Math.max(1, drop.getAmount() * dropResult.getAmount());
                world.dropItemNaturally(loc, new ItemStack(dropResult.getResultMaterial(), dropAmount));
                totalExp += dropResult.getExp() * drop.getAmount();
            } else {
                // If the natural drop is not registered individually, use the block result
                world.dropItemNaturally(loc, new ItemStack(blockResult.getResultMaterial(), blockResult.getAmount()));
                totalExp += blockResult.getExp();
            }
        }

        if (totalExp > 0) {
            event.setExpToDrop(event.getExpToDrop() + totalExp);
        }

        // Visual and auditory effects
        if (config.playParticles) {
            world.spawnParticle(Particle.FLAME, loc, 8, 0.25, 0.25, 0.25, 0.05);
            world.spawnParticle(Particle.SMOKE, loc, 4, 0.2, 0.2, 0.2, 0.02);
        }

        if (config.playSound) {
            world.playSound(loc, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.8f, 1.2f);
        }

        // Durability: 4 total (1 vanilla + 3 extra)
        int extraDamage = Math.max(0, config.durabilityCostSmeltable - 1);
        if (extraDamage > 0) {
            ItemUtils.damageTool(player, tool, extraDamage);
        }
    }
}
