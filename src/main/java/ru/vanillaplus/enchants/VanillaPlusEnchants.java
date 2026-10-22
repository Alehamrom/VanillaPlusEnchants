package ru.vanillaplus.enchants;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.vanillaplus.enchants.commands.VEnchantsCommand;
import ru.vanillaplus.enchants.config.ConfigManager;
import ru.vanillaplus.enchants.enchant.CooldownManager;
import ru.vanillaplus.enchants.enchant.EnchantmentManager;
import ru.vanillaplus.enchants.enchant.SmeltRegistry;
import ru.vanillaplus.enchants.listeners.*;

/**
 * VanillaPlusEnchants - Vanilla+ Custom Enchantments for Purpur 1.21.
 */
public class VanillaPlusEnchants extends JavaPlugin {

    private static VanillaPlusEnchants instance;

    private SmeltRegistry smeltRegistry;
    private ConfigManager configManager;
    private CooldownManager cooldownManager;
    private EnchantmentManager enchantmentManager;

    private SilentStepListener silentStepListener;
    private BukkitTask syncTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize core components
        this.smeltRegistry = new SmeltRegistry();
        this.configManager = new ConfigManager(this);
        this.configManager.load(getConfig(), smeltRegistry);

        this.cooldownManager = new CooldownManager();
        this.enchantmentManager = new EnchantmentManager(this, configManager);

        // Register listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new SmeltingTouchListener(this), this);
        pm.registerEvents(new StaticShockListener(this), this);
        pm.registerEvents(new SoulSiphonListener(this), this);
        pm.registerEvents(new RicochetListener(this), this);

        this.silentStepListener = new SilentStepListener(this);
        pm.registerEvents(silentStepListener, this);

        pm.registerEvents(new AnvilListener(this), this);
        pm.registerEvents(new GrindstoneListener(this), this);

        // Register command
        VEnchantsCommand commandHandler = new VEnchantsCommand(this);
        var cmd = getCommand("venchants");
        if (cmd != null) {
            cmd.setExecutor(commandHandler);
            cmd.setTabCompleter(commandHandler);
        }

        // Start repeating task: update Silent Step debuff and cooldown cleanups
        this.syncTask = getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                silentStepListener.updatePlayerDebuff(player);
            }
            cooldownManager.cleanup();
        }, 20L, 10L);

        getLogger().info("VanillaPlusEnchants v" + getDescription().getVersion() + " has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (syncTask != null && !syncTask.isCancelled()) {
            syncTask.cancel();
        }

        if (silentStepListener != null) {
            silentStepListener.cleanupAll();
        }

        getLogger().info("VanillaPlusEnchants has been disabled cleanly.");
    }

    public static VanillaPlusEnchants getInstance() {
        return instance;
    }

    public SmeltRegistry getSmeltRegistry() {
        return smeltRegistry;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public EnchantmentManager getEnchantmentManager() {
        return enchantmentManager;
    }

    public SilentStepListener getSilentStepListener() {
        return silentStepListener;
    }
}
