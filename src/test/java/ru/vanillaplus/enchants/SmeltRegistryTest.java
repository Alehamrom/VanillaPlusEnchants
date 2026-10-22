package ru.vanillaplus.enchants;

import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vanillaplus.enchants.enchant.SmeltRegistry;
import ru.vanillaplus.enchants.model.SmeltResult;

import static org.junit.jupiter.api.Assertions.*;

public class SmeltRegistryTest {

    private SmeltRegistry registry;

    @BeforeEach
    public void setUp() {
        registry = new SmeltRegistry();
    }

    @Test
    public void testSmeltableOresAndMaterials() {
        // Raw ores & ores
        assertTrue(registry.isSmeltable(Material.RAW_IRON));
        assertTrue(registry.isSmeltable(Material.IRON_ORE));
        assertTrue(registry.isSmeltable(Material.DEEPSLATE_IRON_ORE));
        assertTrue(registry.isSmeltable(Material.RAW_COPPER));
        assertTrue(registry.isSmeltable(Material.COPPER_ORE));
        assertTrue(registry.isSmeltable(Material.RAW_GOLD));
        assertTrue(registry.isSmeltable(Material.GOLD_ORE));

        // Sand & Clay
        assertTrue(registry.isSmeltable(Material.SAND));
        assertTrue(registry.isSmeltable(Material.RED_SAND));
        assertTrue(registry.isSmeltable(Material.CLAY));

        // Stones
        assertTrue(registry.isSmeltable(Material.COBBLESTONE));
        assertTrue(registry.isSmeltable(Material.STONE));
        assertTrue(registry.isSmeltable(Material.COBBLED_DEEPSLATE));
        assertTrue(registry.isSmeltable(Material.NETHERRACK));
    }

    @Test
    public void testAncientDebrisExplicitlyBlacklisted() {
        assertFalse(registry.isSmeltable(Material.ANCIENT_DEBRIS));
        assertNull(registry.getResult(Material.ANCIENT_DEBRIS));
        assertTrue(registry.getBlacklist().contains(Material.ANCIENT_DEBRIS));
    }

    @Test
    public void testSmeltOutputs() {
        SmeltResult iron = registry.getResult(Material.RAW_IRON);
        assertNotNull(iron);
        assertEquals(Material.IRON_INGOT, iron.getResultMaterial());
        assertEquals(1, iron.getAmount());

        SmeltResult copper = registry.getResult(Material.COPPER_ORE);
        assertNotNull(copper);
        assertEquals(Material.COPPER_INGOT, copper.getResultMaterial());
        assertEquals(1, copper.getAmount());

        SmeltResult gold = registry.getResult(Material.RAW_GOLD);
        assertNotNull(gold);
        assertEquals(Material.GOLD_INGOT, gold.getResultMaterial());
        assertEquals(1, gold.getAmount());

        SmeltResult sand = registry.getResult(Material.SAND);
        assertNotNull(sand);
        assertEquals(Material.GLASS, sand.getResultMaterial());
        assertEquals(1, sand.getAmount());

        SmeltResult clay = registry.getResult(Material.CLAY);
        assertNotNull(clay);
        assertEquals(Material.BRICK, clay.getResultMaterial());
        assertEquals(4, clay.getAmount()); // 1 clay block drops 4 clay balls -> 4 bricks!
    }

    @Test
    public void testDurabilityCosts() {
        // Durability cost: 1 on normal unsmeltable blocks, 4 (1 base + 3 extra) on smeltable blocks
        assertEquals(1, registry.getDurabilityCostUnsmeltable());
        assertEquals(4, registry.getDurabilityCostSmeltable());
    }

    @Test
    public void testUnsmeltableMaterials() {
        assertFalse(registry.isSmeltable(Material.DIRT));
        assertFalse(registry.isSmeltable(Material.AIR));
        assertFalse(registry.isSmeltable(Material.DIAMOND_ORE));
        assertFalse(registry.isSmeltable(Material.EMERALD_ORE));
        assertFalse(registry.isSmeltable(null));
    }
}
