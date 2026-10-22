package ru.vanillaplus.enchants;

import org.junit.jupiter.api.Test;
import ru.vanillaplus.enchants.config.EnchantConfig;

import static org.junit.jupiter.api.Assertions.*;

public class SoulSiphonConditionTest {

    private boolean canTriggerSoulSiphon(double maxHealth, double threshold) {
        return maxHealth > threshold;
    }

    @Test
    public void testPassiveMobsAreBlocked() {
        EnchantConfig.SoulSiphonConfig config = new EnchantConfig.SoulSiphonConfig();
        double threshold = config.minTargetMaxHealth; // 16.0 HP = 8 hearts

        // Cow: 10 HP
        assertFalse(canTriggerSoulSiphon(10.0, threshold), "Cows must not trigger Soul Siphon");
        // Pig: 10 HP
        assertFalse(canTriggerSoulSiphon(10.0, threshold), "Pigs must not trigger Soul Siphon");
        // Chicken: 4 HP
        assertFalse(canTriggerSoulSiphon(4.0, threshold), "Chickens must not trigger Soul Siphon");
        // Bat: 6 HP
        assertFalse(canTriggerSoulSiphon(6.0, threshold), "Bats must not trigger Soul Siphon");
        // Sheep: 8 HP
        assertFalse(canTriggerSoulSiphon(8.0, threshold), "Sheep must not trigger Soul Siphon");
        // Spider: 16 HP (exactly 8 hearts -> not > 8 hearts)
        assertFalse(canTriggerSoulSiphon(16.0, threshold), "Mobs with exactly 8 hearts must not trigger Soul Siphon");
    }

    @Test
    public void testHostileMobsAreAllowed() {
        EnchantConfig.SoulSiphonConfig config = new EnchantConfig.SoulSiphonConfig();
        double threshold = config.minTargetMaxHealth;

        // Zombie: 20 HP
        assertTrue(canTriggerSoulSiphon(20.0, threshold), "Zombies (20 HP) must trigger Soul Siphon");
        // Skeleton: 20 HP
        assertTrue(canTriggerSoulSiphon(20.0, threshold), "Skeletons (20 HP) must trigger Soul Siphon");
        // Creeper: 20 HP
        assertTrue(canTriggerSoulSiphon(20.0, threshold), "Creepers (20 HP) must trigger Soul Siphon");
        // Enderman: 40 HP
        assertTrue(canTriggerSoulSiphon(40.0, threshold), "Endermen (40 HP) must trigger Soul Siphon");
        // Player: 20 HP
        assertTrue(canTriggerSoulSiphon(20.0, threshold), "Players (20 HP) must trigger Soul Siphon");
        // Boss: 300 HP
        assertTrue(canTriggerSoulSiphon(300.0, threshold), "Bosses must trigger Soul Siphon");
    }

    @Test
    public void testHealValues() {
        EnchantConfig.SoulSiphonConfig config = new EnchantConfig.SoulSiphonConfig();
        // Level 1: 0.5 heart (1 HP)
        assertEquals(1.0, config.getHeal(1));
        // Level 2: 1.0 heart (2 HP)
        assertEquals(2.0, config.getHeal(2));
        // Level 3: 1.5 hearts (3 HP)
        assertEquals(3.0, config.getHeal(3));
    }
}
