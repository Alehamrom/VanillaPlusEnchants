package ru.vanillaplus.enchants.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.vanillaplus.enchants.VanillaPlusEnchants;
import ru.vanillaplus.enchants.enchant.SmeltRegistry;
import ru.vanillaplus.enchants.model.CustomEnchantment;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages plugin configuration loading, messages, and MiniMessage parsing.
 */
public class ConfigManager {

    private final VanillaPlusEnchants plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private String prefix = "<dark_purple>[<light_purple>Vanilla+<dark_purple>]</light_purple></dark_purple> ";
    private final Map<String, String> messages = new HashMap<>();

    private String loreFormatSingle = "<!italic><gray>{name}</gray>";
    private String loreFormatLeveled = "<!italic><gray>{name} {roman}</gray>";

    private final EnchantConfig.SmeltingTouchConfig smeltingTouch = new EnchantConfig.SmeltingTouchConfig();
    private final EnchantConfig.StaticShockConfig staticShock = new EnchantConfig.StaticShockConfig();
    private final EnchantConfig.SoulSiphonConfig soulSiphon = new EnchantConfig.SoulSiphonConfig();
    private final EnchantConfig.RicochetConfig ricochet = new EnchantConfig.RicochetConfig();
    private final EnchantConfig.SilentStepConfig silentStep = new EnchantConfig.SilentStepConfig();

    private int anvilBaseCost = 2;
    private int anvilLevelMultiplier = 1;
    private int grindstoneExpPerLevel = 10;

    public ConfigManager(VanillaPlusEnchants plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads or reloads configuration from config.yml.
     */
    public void reload() {
        if (plugin != null) {
            plugin.reloadConfig();
            load(plugin.getConfig(), plugin.getSmeltRegistry());
        }
    }

    /**
     * Loads settings from a FileConfiguration or ConfigurationSection.
     */
    public void load(FileConfiguration config, SmeltRegistry smeltRegistry) {
        if (config == null) return;

        this.prefix = config.getString("prefix", "<dark_purple>[<light_purple>Vanilla+<dark_purple>]</light_purple></dark_purple> ");

        // Messages
        ConfigurationSection msgSec = config.getConfigurationSection("messages");
        messages.clear();
        if (msgSec != null) {
            for (String key : msgSec.getKeys(false)) {
                messages.put(key, msgSec.getString(key, ""));
            }
        }

        // Lore
        ConfigurationSection loreSec = config.getConfigurationSection("lore");
        if (loreSec != null) {
            this.loreFormatSingle = loreSec.getString("format_single", "<!italic><gray>{name}</gray>");
            this.loreFormatLeveled = loreSec.getString("format_leveled", "<!italic><gray>{name} {roman}</gray>");
        }

        // Smelting Touch
        ConfigurationSection stSec = config.getConfigurationSection("enchants.smelting_touch");
        if (stSec != null) {
            smeltingTouch.enabled = stSec.getBoolean("enabled", true);
            smeltingTouch.name = stSec.getString("name", "Обжиг");
            smeltingTouch.description = stSec.getString("description", "Автоматически переплавляет добытые руды, песок и глину.");
            smeltingTouch.durabilityCostUnsmeltable = stSec.getInt("durability_cost_unsmeltable", 1);
            smeltingTouch.durabilityCostSmeltable = stSec.getInt("durability_cost_smeltable", 4);
            smeltingTouch.playParticles = stSec.getBoolean("play_particles", true);
            smeltingTouch.playSound = stSec.getBoolean("play_sound", true);
            if (smeltRegistry != null) {
                smeltRegistry.loadFromConfig(stSec);
            }
        }

        // Static Shock
        ConfigurationSection ssSec = config.getConfigurationSection("enchants.static_shock");
        if (ssSec != null) {
            staticShock.enabled = ssSec.getBoolean("enabled", true);
            staticShock.name = ssSec.getString("name", "Цепной разряд");
            staticShock.description = ssSec.getString("description", "При критической атаке в прыжке выпускает разряд по 2-3 врагам.");
            staticShock.cooldownSeconds = ssSec.getDouble("cooldown_seconds", 5.0);
            staticShock.chainRadius = ssSec.getDouble("chain_radius", 4.0);
            staticShock.minTargets = ssSec.getInt("min_targets", 2);
            staticShock.maxTargets = ssSec.getInt("max_targets", 3);
            ConfigurationSection dmgSec = ssSec.getConfigurationSection("damage");
            if (dmgSec != null) {
                staticShock.damageMap.clear();
                for (String lvlKey : dmgSec.getKeys(false)) {
                    try {
                        int lvl = Integer.parseInt(lvlKey);
                        staticShock.damageMap.put(lvl, dmgSec.getDouble(lvlKey));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Soul Siphon
        ConfigurationSection sipSec = config.getConfigurationSection("enchants.soul_siphon");
        if (sipSec != null) {
            soulSiphon.enabled = sipSec.getBoolean("enabled", true);
            soulSiphon.name = sipSec.getString("name", "Жатва душ");
            soulSiphon.description = sipSec.getString("description", "Восстанавливает здоровье при убийстве мобов с запасом здоровья более 8 сердец (16 HP).");
            soulSiphon.minTargetMaxHealth = sipSec.getDouble("min_target_max_health", 16.0);
            ConfigurationSection healSec = sipSec.getConfigurationSection("heal");
            if (healSec != null) {
                soulSiphon.healMap.clear();
                for (String lvlKey : healSec.getKeys(false)) {
                    try {
                        int lvl = Integer.parseInt(lvlKey);
                        soulSiphon.healMap.put(lvl, healSec.getDouble(lvlKey));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Ricochet
        ConfigurationSection ricSec = config.getConfigurationSection("enchants.ricochet");
        if (ricSec != null) {
            ricochet.enabled = ricSec.getBoolean("enabled", true);
            ricochet.name = ricSec.getString("name", "Рикошет");
            ricochet.description = ricSec.getString("description", "Стрелы с шансом 33% рикошетят в ближайшего второго врага с 50% урона.");
            ricochet.chancePercent = ricSec.getDouble("chance_percent", 33.0);
            ricochet.damageMultiplier = ricSec.getDouble("damage_multiplier", 0.5);
            ConfigurationSection radSec = ricSec.getConfigurationSection("radius");
            if (radSec != null) {
                ricochet.radiusMap.clear();
                for (String lvlKey : radSec.getKeys(false)) {
                    try {
                        int lvl = Integer.parseInt(lvlKey);
                        ricochet.radiusMap.put(lvl, radSec.getDouble(lvlKey));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Silent Step
        ConfigurationSection stepSec = config.getConfigurationSection("enchants.silent_step");
        if (stepSec != null) {
            silentStep.enabled = stepSec.getBoolean("enabled", true);
            silentStep.name = stepSec.getString("name", "Мягкая поступь");
            silentStep.description = stepSec.getString("description", "Подавляет вибрации шагов для скалк-сенсоров и Вардена, снижая скорость бега на 30%.");
            silentStep.speedReductionPercent = stepSec.getDouble("speed_reduction_percent", 30.0);
            silentStep.showDebuffInLore = stepSec.getBoolean("show_debuff_in_lore", true);
            silentStep.debuffLore = stepSec.getString("debuff_lore", "<!italic><red>-30% Скорость бега</red>");
        }

        // Anvil
        this.anvilBaseCost = config.getInt("anvil.base_cost", 2);
        this.anvilLevelMultiplier = config.getInt("anvil.level_cost_multiplier", 1);

        // Grindstone
        this.grindstoneExpPerLevel = config.getInt("grindstone.exp_per_level", 10);
    }

    /**
     * Formats a raw message template with placeholders and Adventure MiniMessage.
     */
    public Component getMessage(String key, Map<String, String> placeholders, boolean withPrefix) {
        String template = messages.getOrDefault(key, key);
        if (withPrefix) {
            template = prefix + template;
        }
        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                template = template.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return miniMessage.deserialize(template);
    }

    public Component getMessage(String key, boolean withPrefix) {
        return getMessage(key, Map.of(), withPrefix);
    }

    public Component getMessage(String key) {
        return getMessage(key, true);
    }

    public Component parse(String miniMessageString) {
        return miniMessage.deserialize(miniMessageString != null ? miniMessageString : "");
    }

    public String getEnchantDisplayName(CustomEnchantment enchantment) {
        return switch (enchantment) {
            case SMELTING_TOUCH -> smeltingTouch.name;
            case STATIC_SHOCK -> staticShock.name;
            case SOUL_SIPHON -> soulSiphon.name;
            case RICOCHET -> ricochet.name;
            case SILENT_STEP -> silentStep.name;
        };
    }

    public String getEnchantDescription(CustomEnchantment enchantment) {
        return switch (enchantment) {
            case SMELTING_TOUCH -> smeltingTouch.description;
            case STATIC_SHOCK -> staticShock.description;
            case SOUL_SIPHON -> soulSiphon.description;
            case RICOCHET -> ricochet.description;
            case SILENT_STEP -> silentStep.description;
        };
    }

    public boolean isEnchantEnabled(CustomEnchantment enchantment) {
        return switch (enchantment) {
            case SMELTING_TOUCH -> smeltingTouch.enabled;
            case STATIC_SHOCK -> staticShock.enabled;
            case SOUL_SIPHON -> soulSiphon.enabled;
            case RICOCHET -> ricochet.enabled;
            case SILENT_STEP -> silentStep.enabled;
        };
    }

    public String getPrefix() {
        return prefix;
    }

    public String getLoreFormatSingle() {
        return loreFormatSingle;
    }

    public String getLoreFormatLeveled() {
        return loreFormatLeveled;
    }

    public EnchantConfig.SmeltingTouchConfig getSmeltingTouch() {
        return smeltingTouch;
    }

    public EnchantConfig.StaticShockConfig getStaticShock() {
        return staticShock;
    }

    public EnchantConfig.SoulSiphonConfig getSoulSiphon() {
        return soulSiphon;
    }

    public EnchantConfig.RicochetConfig getRicochet() {
        return ricochet;
    }

    public EnchantConfig.SilentStepConfig getSilentStep() {
        return silentStep;
    }

    public int getAnvilBaseCost() {
        return anvilBaseCost;
    }

    public int getAnvilLevelMultiplier() {
        return anvilLevelMultiplier;
    }

    public int getGrindstoneExpPerLevel() {
        return grindstoneExpPerLevel;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
