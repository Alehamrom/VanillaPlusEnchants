package ru.vanillaplus.enchants.model;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;

/**
 * Categorizes item targets for custom enchantments.
 */
public enum ItemTarget {

    PICKAXE_AND_SHOVEL("Кирки и Лопаты", EnumSet.of(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL
    )),

    SWORD("Мечи", EnumSet.of(
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD
    )),

    SWORD_AND_AXE("Мечи и Топоры", EnumSet.of(
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
            Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE
    )),

    BOW_AND_CROSSBOW("Луки и Арбалеты", EnumSet.of(
            Material.BOW, Material.CROSSBOW
    )),

    LEGGINGS("Поножи", EnumSet.of(
            Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS,
            Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS
    ));

    private final String displayName;
    private final Set<Material> applicableMaterials;

    ItemTarget(String displayName, Set<Material> applicableMaterials) {
        this.displayName = displayName;
        this.applicableMaterials = applicableMaterials;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<Material> getApplicableMaterials() {
        return applicableMaterials;
    }

    /**
     * Checks if the given material is a valid equipment target for this enchantment.
     */
    public boolean matches(Material material) {
        return material != null && applicableMaterials.contains(material);
    }

    /**
     * Checks if the material matches or is a book / enchanted book.
     */
    public boolean matchesOrBook(Material material) {
        if (material == null) {
            return false;
        }
        return matches(material) || material == Material.BOOK || material == Material.ENCHANTED_BOOK;
    }
}
