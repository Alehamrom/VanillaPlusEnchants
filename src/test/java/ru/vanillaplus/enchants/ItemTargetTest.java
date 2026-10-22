package ru.vanillaplus.enchants;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import ru.vanillaplus.enchants.model.ItemTarget;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTargetTest {

    @Test
    public void testPickaxeAndShovel() {
        ItemTarget target = ItemTarget.PICKAXE_AND_SHOVEL;
        assertTrue(target.matches(Material.DIAMOND_PICKAXE));
        assertTrue(target.matches(Material.NETHERITE_PICKAXE));
        assertTrue(target.matches(Material.IRON_SHOVEL));
        assertTrue(target.matches(Material.WOODEN_SHOVEL));

        assertFalse(target.matches(Material.DIAMOND_SWORD));
        assertFalse(target.matches(Material.BOW));
        assertFalse(target.matches(Material.DIAMOND_AXE));
    }

    @Test
    public void testSword() {
        ItemTarget target = ItemTarget.SWORD;
        assertTrue(target.matches(Material.NETHERITE_SWORD));
        assertTrue(target.matches(Material.DIAMOND_SWORD));
        assertTrue(target.matches(Material.IRON_SWORD));

        assertFalse(target.matches(Material.DIAMOND_AXE));
        assertFalse(target.matches(Material.BOW));
        assertFalse(target.matches(Material.DIAMOND_PICKAXE));
    }

    @Test
    public void testSwordAndAxe() {
        ItemTarget target = ItemTarget.SWORD_AND_AXE;
        assertTrue(target.matches(Material.NETHERITE_SWORD));
        assertTrue(target.matches(Material.DIAMOND_AXE));
        assertTrue(target.matches(Material.IRON_AXE));

        assertFalse(target.matches(Material.BOW));
        assertFalse(target.matches(Material.CROSSBOW));
        assertFalse(target.matches(Material.DIAMOND_PICKAXE));
    }

    @Test
    public void testBowAndCrossbow() {
        ItemTarget target = ItemTarget.BOW_AND_CROSSBOW;
        assertTrue(target.matches(Material.BOW));
        assertTrue(target.matches(Material.CROSSBOW));

        assertFalse(target.matches(Material.ARROW));
        assertFalse(target.matches(Material.DIAMOND_SWORD));
    }

    @Test
    public void testLeggings() {
        ItemTarget target = ItemTarget.LEGGINGS;
        assertTrue(target.matches(Material.NETHERITE_LEGGINGS));
        assertTrue(target.matches(Material.DIAMOND_LEGGINGS));
        assertTrue(target.matches(Material.IRON_LEGGINGS));
        assertTrue(target.matches(Material.LEATHER_LEGGINGS));

        assertFalse(target.matches(Material.DIAMOND_CHESTPLATE));
        assertFalse(target.matches(Material.DIAMOND_BOOTS));
        assertFalse(target.matches(Material.DIAMOND_HELMET));
    }

    @Test
    public void testMatchesOrBook() {
        for (ItemTarget target : ItemTarget.values()) {
            assertTrue(target.matchesOrBook(Material.BOOK));
            assertTrue(target.matchesOrBook(Material.ENCHANTED_BOOK));
            assertFalse(target.matchesOrBook(null));
        }
    }
}
