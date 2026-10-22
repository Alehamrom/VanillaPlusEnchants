package ru.vanillaplus.enchants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vanillaplus.enchants.enchant.CooldownManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CooldownManagerTest {

    private CooldownManager cooldownManager;
    private UUID testPlayerId;

    @BeforeEach
    public void setUp() {
        cooldownManager = new CooldownManager();
        testPlayerId = UUID.randomUUID();
    }

    @Test
    public void testCooldownFlow() {
        assertFalse(cooldownManager.isOnCooldown(testPlayerId, "static_shock"));

        // Set 5 second cooldown
        cooldownManager.setCooldown(testPlayerId, "static_shock", 5.0);
        assertTrue(cooldownManager.isOnCooldown(testPlayerId, "static_shock"));
        assertTrue(cooldownManager.getRemainingSeconds(testPlayerId, "static_shock") > 0);

        // Reset
        cooldownManager.reset(testPlayerId, "static_shock");
        assertFalse(cooldownManager.isOnCooldown(testPlayerId, "static_shock"));
        assertEquals(0, cooldownManager.getRemainingSeconds(testPlayerId, "static_shock"));
    }

    @Test
    public void testResetAll() {
        cooldownManager.setCooldown(testPlayerId, "ability_1", 10.0);
        cooldownManager.setCooldown(testPlayerId, "ability_2", 10.0);
        assertTrue(cooldownManager.isOnCooldown(testPlayerId, "ability_1"));
        assertTrue(cooldownManager.isOnCooldown(testPlayerId, "ability_2"));

        cooldownManager.resetAll(testPlayerId);
        assertFalse(cooldownManager.isOnCooldown(testPlayerId, "ability_1"));
        assertFalse(cooldownManager.isOnCooldown(testPlayerId, "ability_2"));
    }

    @Test
    public void testExpiredCooldown() throws InterruptedException {
        // Set short 50ms cooldown
        cooldownManager.setCooldown(testPlayerId, "fast_ability", 0.05);
        assertTrue(cooldownManager.isOnCooldown(testPlayerId, "fast_ability"));

        Thread.sleep(60);
        assertFalse(cooldownManager.isOnCooldown(testPlayerId, "fast_ability"));
    }
}
