package ru.vanillaplus.enchants;

import org.junit.jupiter.api.Test;
import ru.vanillaplus.enchants.config.EnchantConfig;

import static org.junit.jupiter.api.Assertions.*;

public class EnchantConfigTest {

    @Test
    public void testStaticShockConfig() {
        EnchantConfig.StaticShockConfig config = new EnchantConfig.StaticShockConfig();
        assertEquals(5.0, config.cooldownSeconds);
        assertEquals(4.0, config.chainRadius);
        assertEquals(2, config.minTargets);
        assertEquals(3, config.maxTargets);

        // 1.5 to 3 hearts (3 to 6 HP)
        assertEquals(3.0, config.getDamage(1)); // 1.5 hearts
        assertEquals(4.5, config.getDamage(2)); // 2.25 hearts
        assertEquals(6.0, config.getDamage(3)); // 3.0 hearts
    }

    @Test
    public void testSoulSiphonConfig() {
        EnchantConfig.SoulSiphonConfig config = new EnchantConfig.SoulSiphonConfig();
        // Target mob must have MAX health > 8 hearts (16 HP)
        assertEquals(16.0, config.minTargetMaxHealth);

        // Healing: 1: 0.5 heart (1 HP), 2: 1.0 heart (2 HP), 3: 1.5 hearts (3 HP)
        assertEquals(1.0, config.getHeal(1));
        assertEquals(2.0, config.getHeal(2));
        assertEquals(3.0, config.getHeal(3));
    }

    @Test
    public void testRicochetConfig() {
        EnchantConfig.RicochetConfig config = new EnchantConfig.RicochetConfig();
        // Chance: 33%
        assertEquals(33.0, config.chancePercent);
        // Damage multiplier: 50%
        assertEquals(0.5, config.damageMultiplier);

        // Radius: Level 1 = 5, Level 2 = 10, Level 3 = 15, Level 4 = 20 blocks
        assertEquals(5.0, config.getRadius(1));
        assertEquals(10.0, config.getRadius(2));
        assertEquals(15.0, config.getRadius(3));
        assertEquals(20.0, config.getRadius(4));
    }

    @Test
    public void testSilentStepConfig() {
        EnchantConfig.SilentStepConfig config = new EnchantConfig.SilentStepConfig();
        // Debuff: -30% movement speed while worn
        assertEquals(30.0, config.speedReductionPercent);
        assertTrue(config.showDebuffInLore);
        assertTrue(config.debuffLore.contains("-30%"));
    }

    @Test
    public void testSmeltingTouchConfig() {
        EnchantConfig.SmeltingTouchConfig config = new EnchantConfig.SmeltingTouchConfig();
        assertEquals(1, config.durabilityCostUnsmeltable);
        assertEquals(4, config.durabilityCostSmeltable);
        assertTrue(config.playParticles);
        assertTrue(config.playSound);
    }
}
