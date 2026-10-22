package ru.vanillaplus.enchants.model;

import org.bukkit.enchantments.Enchantment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Defines the custom enchantments in VanillaPlusEnchants.
 */
public enum CustomEnchantment {

    SMELTING_TOUCH(
            "smelting_touch",
            "Обжиг",
            "Автоматически переплавляет добытые руды, песок и глину.",
            1,
            ItemTarget.PICKAXE_AND_SHOVEL,
            Set.of("silk_touch", "fortune")
    ),

    STATIC_SHOCK(
            "static_shock",
            "Цепной разряд",
            "При критической атаке в прыжке выпускает разряд по 2-3 врагам.",
            3,
            ItemTarget.SWORD,
            Collections.emptySet()
    ),

    SOUL_SIPHON(
            "soul_siphon",
            "Жатва душ",
            "Восстанавливает здоровье при убийстве мобов с запасом здоровья > 8 сердец.",
            3,
            ItemTarget.SWORD_AND_AXE,
            Collections.emptySet()
    ),

    RICOCHET(
            "ricochet",
            "Рикошет",
            "Стрелы с шансом 33% рикошетят в ближайшего второго врага с 50% урона.",
            4,
            ItemTarget.BOW_AND_CROSSBOW,
            Set.of("infinity", "piercing")
    ),

    SILENT_STEP(
            "silent_step",
            "Мягкая поступь",
            "Подавляет вибрации шагов для скалк-сенсоров и Вардена, снижая скорость бега на 30%.",
            1,
            ItemTarget.LEGGINGS,
            Set.of("swift_sneak")
    );

    private final String id;
    private final String defaultDisplayName;
    private final String defaultDescription;
    private final int maxLevel;
    private final ItemTarget target;
    private final Set<String> incompatibleVanillaKeys;

    CustomEnchantment(String id, String defaultDisplayName, String defaultDescription, int maxLevel,
                      ItemTarget target, Set<String> incompatibleVanillaKeys) {
        this.id = id;
        this.defaultDisplayName = defaultDisplayName;
        this.defaultDescription = defaultDescription;
        this.maxLevel = maxLevel;
        this.target = target;
        this.incompatibleVanillaKeys = incompatibleVanillaKeys;
    }

    public String getId() {
        return id;
    }

    public String getDefaultDisplayName() {
        return defaultDisplayName;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public ItemTarget getTarget() {
        return target;
    }

    public Set<String> getIncompatibleVanillaKeys() {
        return incompatibleVanillaKeys;
    }

    /**
     * Checks if this custom enchantment is incompatible with a vanilla enchantment key.
     */
    public boolean isIncompatibleWithVanilla(String vanillaKey) {
        if (vanillaKey == null) {
            return false;
        }
        String clean = vanillaKey.toLowerCase(Locale.ROOT);
        if (clean.contains(":")) {
            clean = clean.substring(clean.indexOf(':') + 1);
        }
        return incompatibleVanillaKeys.contains(clean);
    }

    /**
     * Checks if this custom enchantment is incompatible with a Bukkit Enchantment.
     */
    public boolean isIncompatibleWithVanilla(Enchantment enchantment) {
        if (enchantment == null) {
            return false;
        }
        return isIncompatibleWithVanilla(enchantment.getKey().getKey());
    }

    /**
     * Finds a CustomEnchantment by id or name (supports aliases, rus, eng, spaces, dashes).
     */
    public static CustomEnchantment fromId(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String clean = text.trim().toLowerCase(Locale.ROOT)
                .replace(" ", "_")
                .replace("-", "_");

        for (CustomEnchantment e : values()) {
            if (e.id.equals(clean) || e.name().toLowerCase(Locale.ROOT).equals(clean)) {
                return e;
            }
        }

        // Russian name aliases
        if (clean.contains("обжиг") || clean.contains("smelt")) {
            return SMELTING_TOUCH;
        }
        if (clean.contains("цепн") || clean.contains("разряд") || clean.contains("shock") || clean.contains("static")) {
            return STATIC_SHOCK;
        }
        if (clean.contains("жатв") || clean.contains("душ") || clean.contains("siphon") || clean.contains("soul")) {
            return SOUL_SIPHON;
        }
        if (clean.contains("рикошет") || clean.contains("ricochet")) {
            return RICOCHET;
        }
        if (clean.contains("поступ") || clean.contains("мягк") || clean.contains("silent") || clean.contains("step")) {
            return SILENT_STEP;
        }

        return null;
    }
}
