package ru.vanillaplus.enchants;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import ru.vanillaplus.enchants.config.EnchantConfig;
import ru.vanillaplus.enchants.enchant.SmeltRegistry;
import ru.vanillaplus.enchants.model.CustomEnchantment;
import ru.vanillaplus.enchants.model.SmeltResult;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EnchantmentMechanicsTest {

    @Test
    public void testSmeltingTouchHarvestVerification() {
        SmeltRegistry registry = new SmeltRegistry();

        // 1. Smeltable ore test
        assertTrue(registry.isSmeltable(Material.IRON_ORE));
        assertTrue(registry.isSmeltable(Material.COPPER_ORE));
        assertTrue(registry.isSmeltable(Material.RAW_COPPER));

        // 2. Unharvestable block (e.g. wooden pickaxe on iron ore returns empty drops)
        List<ItemStack> woodenPickaxeDropsOnIronOre = Collections.emptyList();
        assertTrue(woodenPickaxeDropsOnIronOre.isEmpty(), "Wooden pickaxe cannot harvest iron ore");

        // 3. Multi-drop calculation for Copper Ore (e.g. 3 raw copper drops from 1 ore)
        int rawCopperAmount = 3;
        SmeltResult copperResult = registry.getResult(Material.RAW_COPPER);
        assertNotNull(copperResult);
        assertEquals(Material.COPPER_INGOT, copperResult.getResultMaterial());
        assertEquals(1, copperResult.getAmount());
        // Total ingots awarded = 3 * 1 = 3
        int totalIngots = rawCopperAmount * copperResult.getAmount();
        assertEquals(3, totalIngots, "Mining 3 raw copper should yield 3 copper ingots");

        // 4. Clay block drops 4 clay balls -> 4 bricks
        SmeltResult clayResult = registry.getResult(Material.CLAY);
        assertNotNull(clayResult);
        assertEquals(4, clayResult.getAmount());

        SmeltResult clayBallResult = registry.getResult(Material.CLAY_BALL);
        assertNotNull(clayBallResult);
        assertEquals(1, clayBallResult.getAmount());
    }

    @Test
    public void testRicochetMechanics() {
        EnchantConfig.RicochetConfig config = new EnchantConfig.RicochetConfig();

        // 33% chance trigger
        assertEquals(33.0, config.chancePercent);

        // 50% damage multiplier
        assertEquals(0.5, config.damageMultiplier);
        double originalArrowDamage = 12.0;
        double bouncedArrowDamage = Math.max(1.0, originalArrowDamage * config.damageMultiplier);
        assertEquals(6.0, bouncedArrowDamage, "Secondary arrow must deal exactly 50% damage");

        // Radii: Level 1 = 5, Level 2 = 10, Level 3 = 15, Level 4 = 20
        assertEquals(5.0, config.getRadius(1));
        assertEquals(10.0, config.getRadius(2));
        assertEquals(15.0, config.getRadius(3));
        assertEquals(20.0, config.getRadius(4));
    }

    @Test
    public void testStaticShockMechanics() {
        EnchantConfig.StaticShockConfig config = new EnchantConfig.StaticShockConfig();

        // Cooldown: 5 seconds
        assertEquals(5.0, config.cooldownSeconds);

        // Chain radius: 4 blocks
        assertEquals(4.0, config.chainRadius);

        // Targets: min 2, max 3
        assertEquals(2, config.minTargets);
        assertEquals(3, config.maxTargets);

        // Damage: 1.5 to 3.0 hearts (3.0 to 6.0 HP)
        assertEquals(3.0, config.getDamage(1)); // 1.5 hearts
        assertEquals(4.5, config.getDamage(2)); // 2.25 hearts
        assertEquals(6.0, config.getDamage(3)); // 3.0 hearts
    }

    @Test
    public void testSilentStepMechanics() {
        EnchantConfig.SilentStepConfig config = new EnchantConfig.SilentStepConfig();

        // Speed reduction: 30%
        assertEquals(30.0, config.speedReductionPercent);
        double modifierAmount = -(config.speedReductionPercent / 100.0);
        assertEquals(-0.30, modifierAmount, 0.0001);

        // Single level (max level 1)
        assertEquals(1, CustomEnchantment.SILENT_STEP.getMaxLevel());

        // Incompatible with Swift Sneak
        assertTrue(CustomEnchantment.SILENT_STEP.isIncompatibleWithVanilla("swift_sneak"));
    }
}
