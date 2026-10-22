package ru.vanillaplus.enchants.enchant;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ru.vanillaplus.enchants.VanillaPlusEnchants;
import ru.vanillaplus.enchants.config.ConfigManager;
import ru.vanillaplus.enchants.model.CustomEnchantment;
import ru.vanillaplus.enchants.util.RomanNumeral;

import java.util.*;

/**
 * Manages PDC storage, item validation, and lore rendering for custom enchantments.
 */
public class EnchantmentManager {

    private final VanillaPlusEnchants plugin;
    private final ConfigManager configManager;
    private final Map<CustomEnchantment, NamespacedKey> enchantKeys = new EnumMap<>(CustomEnchantment.class);
    private final NamespacedKey bookTagKey;
    private final NamespacedKey debuffTagKey;

    public EnchantmentManager(VanillaPlusEnchants plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.bookTagKey = new NamespacedKey(plugin, "is_custom_book");
        this.debuffTagKey = new NamespacedKey(plugin, "has_silent_step_debuff");

        for (CustomEnchantment enchant : CustomEnchantment.values()) {
            enchantKeys.put(enchant, new NamespacedKey(plugin, "enchant_" + enchant.getId()));
        }
    }

    public NamespacedKey getKey(CustomEnchantment enchant) {
        return enchantKeys.get(enchant);
    }

    public NamespacedKey getBookTagKey() {
        return bookTagKey;
    }

    /**
     * Gets the level of a custom enchantment on an item, or 0 if absent.
     */
    public int getLevel(ItemStack item, CustomEnchantment enchant) {
        if (item == null || !item.hasItemMeta() || enchant == null) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey key = enchantKeys.get(enchant);
        if (key != null && pdc.has(key, PersistentDataType.INTEGER)) {
            Integer val = pdc.get(key, PersistentDataType.INTEGER);
            return val != null ? Math.max(0, val) : 0;
        }
        return 0;
    }

    /**
     * Checks if the item has the custom enchantment.
     */
    public boolean hasEnchantment(ItemStack item, CustomEnchantment enchant) {
        return getLevel(item, enchant) > 0;
    }

    /**
     * Retrieves all custom enchantments and their levels present on the item.
     */
    public Map<CustomEnchantment, Integer> getEnchantments(ItemStack item) {
        Map<CustomEnchantment, Integer> result = new EnumMap<>(CustomEnchantment.class);
        if (item == null || !item.hasItemMeta()) {
            return result;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        for (Map.Entry<CustomEnchantment, NamespacedKey> entry : enchantKeys.entrySet()) {
            if (pdc.has(entry.getValue(), PersistentDataType.INTEGER)) {
                Integer level = pdc.get(entry.getValue(), PersistentDataType.INTEGER);
                if (level != null && level > 0) {
                    result.put(entry.getKey(), level);
                }
            }
        }
        return result;
    }

    /**
     * Checks if the item can receive this enchantment.
     */
    public boolean canApplyTo(ItemStack item, CustomEnchantment enchant) {
        if (item == null || enchant == null) {
            return false;
        }
        Material mat = item.getType();
        if (mat.isAir()) {
            return false;
        }

        // Target check
        if (!enchant.getTarget().matchesOrBook(mat)) {
            return false;
        }

        // Vanilla incompatibility check
        if (hasIncompatibleVanilla(item, enchant)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the item has any vanilla enchantments incompatible with the custom enchantment.
     */
    public boolean hasIncompatibleVanilla(ItemStack item, CustomEnchantment enchant) {
        if (item == null || !item.hasItemMeta() || enchant == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();

        // Check regular enchantments
        Map<Enchantment, Integer> enchants = meta.getEnchants();
        for (Enchantment e : enchants.keySet()) {
            if (enchant.isIncompatibleWithVanilla(e)) {
                return true;
            }
        }

        // Check stored enchantments if book
        if (meta instanceof EnchantmentStorageMeta storage) {
            for (Enchantment e : storage.getStoredEnchants().keySet()) {
                if (enchant.isIncompatibleWithVanilla(e)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the name of the conflicting vanilla enchantment, or null.
     */
    public String getIncompatibleVanillaName(ItemStack item, CustomEnchantment enchant) {
        if (item == null || !item.hasItemMeta() || enchant == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();

        for (Enchantment e : meta.getEnchants().keySet()) {
            if (enchant.isIncompatibleWithVanilla(e)) {
                return e.getKey().getKey();
            }
        }

        if (meta instanceof EnchantmentStorageMeta storage) {
            for (Enchantment e : storage.getStoredEnchants().keySet()) {
                if (enchant.isIncompatibleWithVanilla(e)) {
                    return e.getKey().getKey();
                }
            }
        }

        return null;
    }

    /**
     * Applies a custom enchantment to an ItemStack with updated PDC and Lore.
     */
    public ItemStack applyEnchantment(ItemStack item, CustomEnchantment enchant, int level) {
        if (item == null || enchant == null || level <= 0) {
            return item;
        }
        ItemStack clone = item.clone();

        // If regular book, turn into Enchanted Book
        if (clone.getType() == Material.BOOK) {
            clone.setType(Material.ENCHANTED_BOOK);
        }

        ItemMeta meta = clone.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        int cappedLevel = Math.min(level, enchant.getMaxLevel());
        pdc.set(enchantKeys.get(enchant), PersistentDataType.INTEGER, cappedLevel);

        if (clone.getType() == Material.ENCHANTED_BOOK) {
            pdc.set(bookTagKey, PersistentDataType.BYTE, (byte) 1);
        }

        // Apply authentic vanilla enchantment glint
        meta.setEnchantmentGlintOverride(true);

        updateLore(meta);
        clone.setItemMeta(meta);
        return clone;
    }

    /**
     * Removes a single custom enchantment from an ItemStack.
     */
    public ItemStack removeEnchantment(ItemStack item, CustomEnchantment enchant) {
        if (item == null || !item.hasItemMeta() || enchant == null) {
            return item;
        }
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        pdc.remove(enchantKeys.get(enchant));

        boolean hasRemainingCustom = false;
        for (NamespacedKey key : enchantKeys.values()) {
            if (pdc.has(key, PersistentDataType.INTEGER)) {
                hasRemainingCustom = true;
                break;
            }
        }
        if (!hasRemainingCustom && !meta.hasEnchants()) {
            meta.setEnchantmentGlintOverride(null);
        }

        updateLore(meta);
        clone.setItemMeta(meta);
        return clone;
    }

    /**
     * Removes all custom enchantments from an ItemStack.
     */
    public ItemStack removeAllEnchantments(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        for (NamespacedKey key : enchantKeys.values()) {
            pdc.remove(key);
        }
        pdc.remove(bookTagKey);
        pdc.remove(debuffTagKey);

        if (!meta.hasEnchants()) {
            meta.setEnchantmentGlintOverride(null);
        }

        updateLore(meta);
        clone.setItemMeta(meta);

        // If it was an enchanted book without vanilla enchantments, revert to regular book
        if (clone.getType() == Material.ENCHANTED_BOOK) {
            ItemMeta m = clone.getItemMeta();
            boolean hasVanilla = false;
            if (m instanceof EnchantmentStorageMeta storage) {
                hasVanilla = !storage.getStoredEnchants().isEmpty();
            }
            if (!hasVanilla && !m.hasEnchants()) {
                clone.setType(Material.BOOK);
            }
        }

        return clone;
    }

    /**
     * Creates an enchanted book with the specified custom enchantment.
     */
    public ItemStack createBook(CustomEnchantment enchant, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        return applyEnchantment(book, enchant, level);
    }

    /**
     * Checks if the item is an enchanted book with custom enchantments.
     */
    public boolean isCustomBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK || !item.hasItemMeta()) {
            return false;
        }
        return !getEnchantments(item).isEmpty();
    }

    /**
     * Re-renders the item's lore: replaces old custom enchantment lines with current ones.
     */
    public void updateLore(ItemMeta meta) {
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        List<Component> existingLore = meta.lore();
        List<Component> cleanedLore = new ArrayList<>();

        // Identify custom enchant names to strip them from lore
        Set<String> customNames = new HashSet<>();
        for (CustomEnchantment e : CustomEnchantment.values()) {
            customNames.add(configManager.getEnchantDisplayName(e).toLowerCase(Locale.ROOT));
            customNames.add(e.getDefaultDisplayName().toLowerCase(Locale.ROOT));
            customNames.add(e.getId().toLowerCase(Locale.ROOT));
        }

        PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

        if (existingLore != null) {
            for (Component line : existingLore) {
                String plain = plainSerializer.serialize(line).toLowerCase(Locale.ROOT).trim();
                boolean isCustomLine = false;
                for (String name : customNames) {
                    if (plain.equals(name) || plain.startsWith(name + " ")) {
                        isCustomLine = true;
                        break;
                    }
                }
                // Also filter debuff line if present
                if (plain.contains("-30%") || plain.contains("скорость бега") || plain.contains("movement speed")) {
                    isCustomLine = true;
                }

                if (!isCustomLine) {
                    cleanedLore.add(line);
                }
            }
        }

        // Build active custom enchantment lore components
        List<Component> customLines = new ArrayList<>();
        boolean hasSilentStep = false;

        for (CustomEnchantment e : CustomEnchantment.values()) {
            NamespacedKey key = enchantKeys.get(e);
            if (pdc.has(key, PersistentDataType.INTEGER)) {
                Integer lvl = pdc.get(key, PersistentDataType.INTEGER);
                if (lvl != null && lvl > 0) {
                    if (e == CustomEnchantment.SILENT_STEP) {
                        hasSilentStep = true;
                    }
                    customLines.add(renderLoreLine(e, lvl));
                }
            }
        }

        // If Silent Step is present, add debuff lore if enabled
        if (hasSilentStep && configManager.getSilentStep().showDebuffInLore) {
            customLines.add(configManager.parse(configManager.getSilentStep().debuffLore));
        }

        // Assemble final lore: custom enchant lines at the top, followed by existing lore
        List<Component> finalLore = new ArrayList<>(customLines);
        finalLore.addAll(cleanedLore);

        meta.lore(finalLore.isEmpty() ? null : finalLore);
    }

    /**
     * Renders a single custom enchantment lore line using MiniMessage format.
     */
    public Component renderLoreLine(CustomEnchantment enchant, int level) {
        String displayName = configManager.getEnchantDisplayName(enchant);
        String template;

        if (enchant.getMaxLevel() == 1) {
            template = configManager.getLoreFormatSingle().replace("{name}", displayName);
        } else {
            String roman = RomanNumeral.toRoman(level);
            template = configManager.getLoreFormatLeveled()
                    .replace("{name}", displayName)
                    .replace("{roman}", roman)
                    .replace("{level}", String.valueOf(level));
        }

        return configManager.parse(template);
    }
}
