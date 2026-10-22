package ru.vanillaplus.enchants.enchant;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player cooldowns for enchantment abilities.
 */
public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    /**
     * Checks if a player's ability is currently on cooldown.
     */
    public boolean isOnCooldown(UUID uuid, String abilityKey) {
        if (uuid == null || abilityKey == null) return false;
        Map<String, Long> userMap = cooldowns.get(uuid);
        if (userMap == null) return false;

        Long expiry = userMap.get(abilityKey);
        if (expiry == null) return false;

        if (System.currentTimeMillis() >= expiry) {
            userMap.remove(abilityKey);
            return false;
        }
        return true;
    }

    /**
     * Gets the remaining cooldown time in milliseconds.
     */
    public long getRemainingMillis(UUID uuid, String abilityKey) {
        if (uuid == null || abilityKey == null) return 0;
        Map<String, Long> userMap = cooldowns.get(uuid);
        if (userMap == null) return 0;

        Long expiry = userMap.get(abilityKey);
        if (expiry == null) return 0;

        long diff = expiry - System.currentTimeMillis();
        return Math.max(0, diff);
    }

    /**
     * Gets remaining cooldown in fractional seconds.
     */
    public double getRemainingSeconds(UUID uuid, String abilityKey) {
        return getRemainingMillis(uuid, abilityKey) / 1000.0;
    }

    /**
     * Sets a cooldown for a player ability.
     *
     * @param uuid player UUID
     * @param abilityKey ability key
     * @param seconds cooldown duration in seconds
     */
    public void setCooldown(UUID uuid, String abilityKey, double seconds) {
        if (uuid == null || abilityKey == null || seconds <= 0) return;
        long expiry = System.currentTimeMillis() + (long) (seconds * 1000L);
        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(abilityKey, expiry);
    }

    /**
     * Resets a cooldown for a specific ability.
     */
    public void reset(UUID uuid, String abilityKey) {
        if (uuid == null || abilityKey == null) return;
        Map<String, Long> userMap = cooldowns.get(uuid);
        if (userMap != null) {
            userMap.remove(abilityKey);
        }
    }

    /**
     * Clears all cooldowns for a player.
     */
    public void resetAll(UUID uuid) {
        if (uuid != null) {
            cooldowns.remove(uuid);
        }
    }

    /**
     * Cleans up expired cooldown entries to prevent memory leaks.
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> {
            entry.getValue().entrySet().removeIf(e -> now >= e.getValue());
            return entry.getValue().isEmpty();
        });
    }
}
