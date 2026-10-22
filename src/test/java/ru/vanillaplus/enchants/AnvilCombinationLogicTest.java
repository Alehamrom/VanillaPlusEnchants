package ru.vanillaplus.enchants;

import org.junit.jupiter.api.Test;
import ru.vanillaplus.enchants.model.CustomEnchantment;

import static org.junit.jupiter.api.Assertions.*;

public class AnvilCombinationLogicTest {

    private int calculateCombinedLevel(int leftLvl, int rightLvl, int maxLevel) {
        if (leftLvl <= 0) {
            return Math.min(rightLvl, maxLevel);
        }
        if (rightLvl <= 0) {
            return Math.min(leftLvl, maxLevel);
        }
        if (leftLvl == rightLvl) {
            return Math.min(maxLevel, leftLvl + 1);
        }
        return Math.min(maxLevel, Math.max(leftLvl, rightLvl));
    }

    @Test
    public void testSmeltingTouchCombination() {
        int max = CustomEnchantment.SMELTING_TOUCH.getMaxLevel(); // 1
        assertEquals(1, calculateCombinedLevel(0, 1, max));
        assertEquals(1, calculateCombinedLevel(1, 1, max));
    }

    @Test
    public void testStaticShockCombination() {
        int max = CustomEnchantment.STATIC_SHOCK.getMaxLevel(); // 3
        assertEquals(1, calculateCombinedLevel(0, 1, max));
        assertEquals(2, calculateCombinedLevel(1, 1, max));
        assertEquals(2, calculateCombinedLevel(1, 2, max));
        assertEquals(2, calculateCombinedLevel(2, 1, max));
        assertEquals(3, calculateCombinedLevel(2, 2, max));
        assertEquals(3, calculateCombinedLevel(3, 3, max));
    }

    @Test
    public void testSoulSiphonCombination() {
        int max = CustomEnchantment.SOUL_SIPHON.getMaxLevel(); // 3
        assertEquals(2, calculateCombinedLevel(1, 1, max));
        assertEquals(3, calculateCombinedLevel(2, 2, max));
        assertEquals(3, calculateCombinedLevel(3, 3, max));
    }

    @Test
    public void testRicochetCombination() {
        int max = CustomEnchantment.RICOCHET.getMaxLevel(); // 4
        assertEquals(1, calculateCombinedLevel(0, 1, max));
        assertEquals(2, calculateCombinedLevel(1, 1, max));
        assertEquals(3, calculateCombinedLevel(2, 2, max));
        assertEquals(4, calculateCombinedLevel(3, 3, max));
        assertEquals(4, calculateCombinedLevel(4, 4, max));
        assertEquals(3, calculateCombinedLevel(1, 3, max));
        assertEquals(4, calculateCombinedLevel(4, 2, max));
    }

    @Test
    public void testSilentStepCombination() {
        int max = CustomEnchantment.SILENT_STEP.getMaxLevel(); // 1
        assertEquals(1, calculateCombinedLevel(0, 1, max));
        assertEquals(1, calculateCombinedLevel(1, 1, max));
    }
}
