package ru.vanillaplus.enchants;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigFileTest {

    @Test
    public void testConfigYamlExistsAndParses() {
        File configFile = new File("src/main/resources/config.yml");
        assertTrue(configFile.exists(), "config.yml must exist in src/main/resources");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        assertNotNull(config.getString("prefix"));

        // Verify messages
        assertNotNull(config.getString("messages.no_permission"));
        assertNotNull(config.getString("messages.player_not_found"));
        assertNotNull(config.getString("messages.give_success"));
        assertNotNull(config.getString("messages.book_success"));
        assertNotNull(config.getString("messages.reload_success"));
        assertNotNull(config.getString("messages.incompatible"));

        // Verify enchants section
        assertTrue(config.getBoolean("enchants.smelting_touch.enabled"));
        assertEquals(1, config.getInt("enchants.smelting_touch.durability_cost_unsmeltable"));
        assertEquals(4, config.getInt("enchants.smelting_touch.durability_cost_smeltable"));
        assertTrue(config.getStringList("enchants.smelting_touch.blacklist").contains("ANCIENT_DEBRIS"));

        assertTrue(config.getBoolean("enchants.static_shock.enabled"));
        assertEquals(5.0, config.getDouble("enchants.static_shock.cooldown_seconds"));
        assertEquals(4.0, config.getDouble("enchants.static_shock.chain_radius"));
        assertEquals(3.0, config.getDouble("enchants.static_shock.damage.1"));
        assertEquals(6.0, config.getDouble("enchants.static_shock.damage.3"));

        assertTrue(config.getBoolean("enchants.soul_siphon.enabled"));
        assertEquals(16.0, config.getDouble("enchants.soul_siphon.min_target_max_health"));
        assertEquals(1.0, config.getDouble("enchants.soul_siphon.heal.1"));
        assertEquals(2.0, config.getDouble("enchants.soul_siphon.heal.2"));
        assertEquals(3.0, config.getDouble("enchants.soul_siphon.heal.3"));

        assertTrue(config.getBoolean("enchants.ricochet.enabled"));
        assertEquals(33.0, config.getDouble("enchants.ricochet.chance_percent"));
        assertEquals(0.5, config.getDouble("enchants.ricochet.damage_multiplier"));
        assertEquals(5.0, config.getDouble("enchants.ricochet.radius.1"));
        assertEquals(10.0, config.getDouble("enchants.ricochet.radius.2"));
        assertEquals(15.0, config.getDouble("enchants.ricochet.radius.3"));
        assertEquals(20.0, config.getDouble("enchants.ricochet.radius.4"));

        assertTrue(config.getBoolean("enchants.silent_step.enabled"));
        assertEquals(30.0, config.getDouble("enchants.silent_step.speed_reduction_percent"));

        // Verify anvil & grindstone
        assertEquals(2, config.getInt("anvil.base_cost"));
        assertEquals(10, config.getInt("grindstone.exp_per_level"));
    }
}
