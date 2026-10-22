package ru.vanillaplus.enchants;

import org.junit.jupiter.api.Test;
import ru.vanillaplus.enchants.model.CustomEnchantment;
import ru.vanillaplus.enchants.model.ItemTarget;

import static org.junit.jupiter.api.Assertions.*;

public class CustomEnchantmentTest {

    @Test
    public void testFromId() {
        assertEquals(CustomEnchantment.SMELTING_TOUCH, CustomEnchantment.fromId("smelting_touch"));
        assertEquals(CustomEnchantment.SMELTING_TOUCH, CustomEnchantment.fromId("smelting-touch"));
        assertEquals(CustomEnchantment.SMELTING_TOUCH, CustomEnchantment.fromId("smelting touch"));
        assertEquals(CustomEnchantment.SMELTING_TOUCH, CustomEnchantment.fromId("обжиг"));

        assertEquals(CustomEnchantment.STATIC_SHOCK, CustomEnchantment.fromId("static_shock"));
        assertEquals(CustomEnchantment.STATIC_SHOCK, CustomEnchantment.fromId("цепной разряд"));

        assertEquals(CustomEnchantment.SOUL_SIPHON, CustomEnchantment.fromId("soul_siphon"));
        assertEquals(CustomEnchantment.SOUL_SIPHON, CustomEnchantment.fromId("жатва душ"));

        assertEquals(CustomEnchantment.RICOCHET, CustomEnchantment.fromId("ricochet"));
        assertEquals(CustomEnchantment.RICOCHET, CustomEnchantment.fromId("рикошет"));

        assertEquals(CustomEnchantment.SILENT_STEP, CustomEnchantment.fromId("silent_step"));
        assertEquals(CustomEnchantment.SILENT_STEP, CustomEnchantment.fromId("мягкая поступь"));

        assertNull(CustomEnchantment.fromId("unknown_ench"));
        assertNull(CustomEnchantment.fromId(null));
        assertNull(CustomEnchantment.fromId(""));
    }

    @Test
    public void testMaxLevels() {
        assertEquals(1, CustomEnchantment.SMELTING_TOUCH.getMaxLevel());
        assertEquals(3, CustomEnchantment.STATIC_SHOCK.getMaxLevel());
        assertEquals(3, CustomEnchantment.SOUL_SIPHON.getMaxLevel());
        assertEquals(4, CustomEnchantment.RICOCHET.getMaxLevel());
        assertEquals(1, CustomEnchantment.SILENT_STEP.getMaxLevel());
    }

    @Test
    public void testTargets() {
        assertEquals(ItemTarget.PICKAXE_AND_SHOVEL, CustomEnchantment.SMELTING_TOUCH.getTarget());
        assertEquals(ItemTarget.SWORD, CustomEnchantment.STATIC_SHOCK.getTarget());
        assertEquals(ItemTarget.SWORD_AND_AXE, CustomEnchantment.SOUL_SIPHON.getTarget());
        assertEquals(ItemTarget.BOW_AND_CROSSBOW, CustomEnchantment.RICOCHET.getTarget());
        assertEquals(ItemTarget.LEGGINGS, CustomEnchantment.SILENT_STEP.getTarget());
    }

    @Test
    public void testIncompatibilities() {
        assertTrue(CustomEnchantment.SMELTING_TOUCH.isIncompatibleWithVanilla("silk_touch"));
        assertTrue(CustomEnchantment.SMELTING_TOUCH.isIncompatibleWithVanilla("minecraft:silk_touch"));
        assertTrue(CustomEnchantment.SMELTING_TOUCH.isIncompatibleWithVanilla("fortune"));
        assertFalse(CustomEnchantment.SMELTING_TOUCH.isIncompatibleWithVanilla("unbreaking"));

        assertFalse(CustomEnchantment.STATIC_SHOCK.isIncompatibleWithVanilla("sharpness"));
        assertFalse(CustomEnchantment.SOUL_SIPHON.isIncompatibleWithVanilla("looting"));

        assertTrue(CustomEnchantment.RICOCHET.isIncompatibleWithVanilla("infinity"));
        assertTrue(CustomEnchantment.RICOCHET.isIncompatibleWithVanilla("piercing"));
        assertFalse(CustomEnchantment.RICOCHET.isIncompatibleWithVanilla("power"));

        assertTrue(CustomEnchantment.SILENT_STEP.isIncompatibleWithVanilla("swift_sneak"));
        assertFalse(CustomEnchantment.SILENT_STEP.isIncompatibleWithVanilla("protection"));
    }
}
