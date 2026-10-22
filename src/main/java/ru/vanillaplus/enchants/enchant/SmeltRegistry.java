package ru.vanillaplus.enchants.enchant;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import ru.vanillaplus.enchants.model.SmeltResult;

import java.util.*;

/**
 * Registry mapping block materials to their smelted outputs for Smelting Touch.
 */
public class SmeltRegistry {

    private final Map<Material, SmeltResult> smeltMap = new EnumMap<>(Material.class);
    private final Set<Material> blacklist = EnumSet.noneOf(Material.class);

    private int durabilityCostUnsmeltable = 1;
    private int durabilityCostSmeltable = 4;

    public SmeltRegistry() {
        registerDefaults();
    }

    /**
     * Registers default smelting recipes per specifications.
     */
    public void registerDefaults() {
        smeltMap.clear();
        blacklist.clear();

        // Blacklist Ancient Debris explicitly
        blacklist.add(Material.ANCIENT_DEBRIS);

        // Iron
        register(Material.RAW_IRON, Material.IRON_INGOT, 1, 1);
        register(Material.IRON_ORE, Material.IRON_INGOT, 1, 1);
        register(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT, 1, 1);
        register(Material.RAW_IRON_BLOCK, Material.IRON_BLOCK, 1, 9);

        // Copper
        register(Material.RAW_COPPER, Material.COPPER_INGOT, 1, 1);
        register(Material.COPPER_ORE, Material.COPPER_INGOT, 1, 1);
        register(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT, 1, 1);
        register(Material.RAW_COPPER_BLOCK, Material.COPPER_BLOCK, 1, 9);

        // Gold
        register(Material.RAW_GOLD, Material.GOLD_INGOT, 1, 1);
        register(Material.GOLD_ORE, Material.GOLD_INGOT, 1, 1);
        register(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT, 1, 1);
        register(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT, 1, 1);
        register(Material.RAW_GOLD_BLOCK, Material.GOLD_BLOCK, 1, 9);

        // Sand & Clay
        register(Material.SAND, Material.GLASS, 1, 0);
        register(Material.RED_SAND, Material.GLASS, 1, 0);
        register(Material.CLAY, Material.BRICK, 4, 1); // 1 clay block drops 4 clay balls -> 4 bricks
        register(Material.CLAY_BALL, Material.BRICK, 1, 1);

        // Stone & Deepslate & Netherrack
        register(Material.COBBLESTONE, Material.STONE, 1, 0);
        register(Material.STONE, Material.SMOOTH_STONE, 1, 0);
        register(Material.COBBLED_DEEPSLATE, Material.DEEPSLATE, 1, 0);
        register(Material.NETHERRACK, Material.NETHER_BRICK, 1, 0);
        register(Material.WET_SPONGE, Material.SPONGE, 1, 0);
        register(Material.CACTUS, Material.GREEN_DYE, 1, 1);
    }

    public void register(Material input, Material output, int amount, int exp) {
        if (input == null || output == null) return;
        smeltMap.put(input, new SmeltResult(output, amount, exp));
    }

    public void loadFromConfig(ConfigurationSection section) {
        if (section == null) return;

        this.durabilityCostUnsmeltable = section.getInt("durability_cost_unsmeltable", 1);
        this.durabilityCostSmeltable = section.getInt("durability_cost_smeltable", 4);

        // Load blacklist
        List<String> blList = section.getStringList("blacklist");
        for (String blName : blList) {
            try {
                Material m = Material.valueOf(blName.toUpperCase(Locale.ROOT));
                blacklist.add(m);
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Load custom or overridden recipes
        ConfigurationSection recipesSec = section.getConfigurationSection("smeltable_blocks");
        if (recipesSec != null) {
            for (String key : recipesSec.getKeys(false)) {
                try {
                    Material in = Material.valueOf(key.toUpperCase(Locale.ROOT));
                    String outName = recipesSec.getString(key);
                    if (outName != null) {
                        Material out = Material.valueOf(outName.toUpperCase(Locale.ROOT));
                        int amount = (in == Material.CLAY) ? 4 : 1;
                        int exp = 1;
                        register(in, out, amount, exp);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    /**
     * Checks if a block of given material can be smelted.
     */
    public boolean isSmeltable(Material material) {
        if (material == null || blacklist.contains(material)) {
            return false;
        }
        return smeltMap.containsKey(material);
    }

    /**
     * Gets the smelted result for the given material, or null if unsmeltable.
     */
    public SmeltResult getResult(Material material) {
        if (material == null || blacklist.contains(material)) {
            return null;
        }
        return smeltMap.get(material);
    }

    public int getDurabilityCostUnsmeltable() {
        return durabilityCostUnsmeltable;
    }

    public void setDurabilityCostUnsmeltable(int cost) {
        this.durabilityCostUnsmeltable = cost;
    }

    public int getDurabilityCostSmeltable() {
        return durabilityCostSmeltable;
    }

    public void setDurabilityCostSmeltable(int cost) {
        this.durabilityCostSmeltable = cost;
    }

    public Set<Material> getBlacklist() {
        return Collections.unmodifiableSet(blacklist);
    }

    public Map<Material, SmeltResult> getSmeltMap() {
        return Collections.unmodifiableMap(smeltMap);
    }
}
