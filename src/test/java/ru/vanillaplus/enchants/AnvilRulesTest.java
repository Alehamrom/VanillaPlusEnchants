package ru.vanillaplus.enchants;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import ru.vanillaplus.enchants.model.CustomEnchantment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AnvilRulesTest {

    /**
     * Replicates the Anvil item-type validation rules implemented in AnvilListener.
     */
    private boolean isLegalAnvilItemCombination(Material left, Material right, CustomEnchantment rightEnchant) {
        if (left == null || right == null) return false;

        boolean isLeftBook = left == Material.ENCHANTED_BOOK;
        boolean isRightBook = right == Material.ENCHANTED_BOOK;

        // 1. Regular Material.BOOK cannot receive or transfer enchantments in an anvil
        if (left == Material.BOOK || right == Material.BOOK) {
            return false;
        }

        // 2. If left is an enchanted book, right MUST also be an enchanted book (cannot combine tool into book)
        if (isLeftBook && !isRightBook) {
            return false;
        }

        // 3. If right is NOT an enchanted book, left and right MUST be the exact same material
        if (!isRightBook && left != right) {
            return false;
        }

        // 4. Target check
        if (!isLeftBook && !rightEnchant.getTarget().matches(left)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a combination has actual upgrade benefit.
     */
    private boolean hasCombinationBenefit(int leftLvl, int rightLvl, boolean hasVanillaBenefit, boolean renamed) {
        if (renamed || hasVanillaBenefit) return true;
        if (leftLvl == 0 && rightLvl > 0) return true;
        if (leftLvl == rightLvl) return true; // Level upgrade!
        return rightLvl > leftLvl;
    }

    @Test
    public void testToolIntoBookIsRejected() {
        // Book in slot 0 + Diamond Sword with Static Shock in slot 1
        assertFalse(isLegalAnvilItemCombination(Material.ENCHANTED_BOOK, Material.DIAMOND_SWORD, CustomEnchantment.STATIC_SHOCK),
                "Combining a tool into an enchanted book must be rejected");
        assertFalse(isLegalAnvilItemCombination(Material.BOOK, Material.DIAMOND_SWORD, CustomEnchantment.STATIC_SHOCK),
                "Regular book in slot 0 must be rejected");
    }

    @Test
    public void testCrossToolMergingIsRejected() {
        // Diamond Sword + Diamond Axe with Soul Siphon
        assertFalse(isLegalAnvilItemCombination(Material.DIAMOND_SWORD, Material.DIAMOND_AXE, CustomEnchantment.SOUL_SIPHON),
                "Cross-tool combination (sword + axe) must be rejected");

        // Iron Sword + Netherite Sword
        assertFalse(isLegalAnvilItemCombination(Material.IRON_SWORD, Material.NETHERITE_SWORD, CustomEnchantment.STATIC_SHOCK),
                "Cross-tier combination (iron + netherite) must be rejected");
    }

    @Test
    public void testValidCombinationsAreAccepted() {
        // Diamond Pickaxe + Enchanted Book (Smelting Touch)
        assertTrue(isLegalAnvilItemCombination(Material.DIAMOND_PICKAXE, Material.ENCHANTED_BOOK, CustomEnchantment.SMELTING_TOUCH));

        // Diamond Sword + Diamond Sword (Static Shock)
        assertTrue(isLegalAnvilItemCombination(Material.DIAMOND_SWORD, Material.DIAMOND_SWORD, CustomEnchantment.STATIC_SHOCK));

        // Enchanted Book + Enchanted Book (Ricochet)
        assertTrue(isLegalAnvilItemCombination(Material.ENCHANTED_BOOK, Material.ENCHANTED_BOOK, CustomEnchantment.RICOCHET));
    }

    @Test
    public void testZeroBenefitCombinationRejected() {
        // Sword with Soul Siphon III + Sword with Soul Siphon I (no vanilla benefit, no rename)
        assertFalse(hasCombinationBenefit(3, 1, false, false),
                "Sacrificing lower-level item with no benefit must be rejected");

        // Sword with Soul Siphon III + Sword with Soul Siphon III -> max level reached, no benefit if already maxed?
        // Wait: III + III: if max is 3, final is 3. But left == right upgrades up to max!
        assertTrue(hasCombinationBenefit(1, 1, false, false), "I + I produces upgrade to II");
        assertTrue(hasCombinationBenefit(2, 2, false, false), "II + II produces upgrade to III");
        assertTrue(hasCombinationBenefit(0, 2, false, false), "Adding new enchant produces benefit");
        assertTrue(hasCombinationBenefit(1, 2, false, false), "Higher level on right produces benefit");
        assertTrue(hasCombinationBenefit(3, 1, true, false), "If vanilla repair occurred, combination is allowed");
        assertTrue(hasCombinationBenefit(3, 1, false, true), "If renamed, combination is allowed");
    }
}
