package ru.vanillaplus.enchants.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration holder for custom enchantments.
 */
public final class EnchantConfig {

    public static class SmeltingTouchConfig {
        public boolean enabled = true;
        public String name = "Обжиг";
        public String description = "Автоматически переплавляет добытые руды, песок и глину.";
        public int durabilityCostUnsmeltable = 1;
        public int durabilityCostSmeltable = 4;
        public boolean playParticles = true;
        public boolean playSound = true;
    }

    public static class StaticShockConfig {
        public boolean enabled = true;
        public String name = "Цепной разряд";
        public String description = "При критической атаке в прыжке выпускает разряд по 2-3 врагам.";
        public double cooldownSeconds = 5.0;
        public double chainRadius = 4.0;
        public int minTargets = 2;
        public int maxTargets = 3;
        public Map<Integer, Double> damageMap = new HashMap<>(Map.of(1, 3.0, 2, 4.5, 3, 6.0));

        public double getDamage(int level) {
            return damageMap.getOrDefault(level, 3.0 + (level - 1) * 1.5);
        }
    }

    public static class SoulSiphonConfig {
        public boolean enabled = true;
        public String name = "Жатва душ";
        public String description = "Восстанавливает здоровье при убийстве мобов с запасом здоровья более 8 сердец (16 HP).";
        public double minTargetMaxHealth = 16.0;
        public Map<Integer, Double> healMap = new HashMap<>(Map.of(1, 1.0, 2, 2.0, 3, 3.0));

        public double getHeal(int level) {
            return healMap.getOrDefault(level, 1.0 * level);
        }
    }

    public static class RicochetConfig {
        public boolean enabled = true;
        public String name = "Рикошет";
        public String description = "Стрелы с шансом 33% рикошетят в ближайшего второго врага с 50% урона.";
        public double chancePercent = 33.0;
        public double damageMultiplier = 0.5;
        public Map<Integer, Double> radiusMap = new HashMap<>(Map.of(1, 5.0, 2, 10.0, 3, 15.0, 4, 20.0));

        public double getRadius(int level) {
            return radiusMap.getOrDefault(level, 5.0 * level);
        }
    }

    public static class SilentStepConfig {
        public boolean enabled = true;
        public String name = "Мягкая поступь";
        public String description = "Подавляет вибрации шагов для скалк-сенсоров и Вардена, снижая скорость бега на 30%.";
        public double speedReductionPercent = 30.0;
        public boolean showDebuffInLore = true;
        public String debuffLore = "<!italic><red>-30% Скорость бега</red>";
    }

    private EnchantConfig() {
    }
}
