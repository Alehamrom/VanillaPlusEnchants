package ru.vanillaplus.enchants.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.vanillaplus.enchants.VanillaPlusEnchants;
import ru.vanillaplus.enchants.config.ConfigManager;
import ru.vanillaplus.enchants.enchant.EnchantmentManager;
import ru.vanillaplus.enchants.model.CustomEnchantment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main command executor and tab completer for /venchants.
 */
public class VEnchantsCommand implements CommandExecutor, TabCompleter {

    private final VanillaPlusEnchants plugin;
    private final EnchantmentManager enchantmentManager;
    private final ConfigManager configManager;

    public VEnchantsCommand(VanillaPlusEnchants plugin) {
        this.plugin = plugin;
        this.enchantmentManager = plugin.getEnchantmentManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vanillaplusenchants.admin")) {
            sender.sendMessage(configManager.getMessage("no_permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> handleReload(sender);
            case "list" -> handleList(sender);
            case "give" -> handleGive(sender, args);
            case "book" -> handleBook(sender, args);
            default -> sendHelp(sender, label);
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        configManager.reload();
        plugin.getSilentStepListener().cleanupAll();
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getSilentStepListener().updatePlayerDebuff(p);
        }
        sender.sendMessage(configManager.getMessage("reload_success"));
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(configManager.getMessage("list_header", false));
        for (CustomEnchantment e : CustomEnchantment.values()) {
            Map<String, String> placeholders = Map.of(
                    "name", configManager.getEnchantDisplayName(e),
                    "id", e.getId(),
                    "max_level", String.valueOf(e.getMaxLevel()),
                    "targets", e.getTarget().getDisplayName(),
                    "description", configManager.getEnchantDescription(e)
            );
            sender.sendMessage(configManager.getMessage("list_item", placeholders, false));
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(configManager.parse("<red>Использование: /venchants give <игрок> <зачарование> [уровень]</red>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(configManager.getMessage("player_not_found", Map.of("player", args[1]), true));
            return;
        }

        CustomEnchantment enchant = CustomEnchantment.fromId(args[2]);
        if (enchant == null) {
            sender.sendMessage(configManager.getMessage("invalid_enchant", Map.of("enchant", args[2]), true));
            return;
        }

        int level = 1;
        if (args.length >= 4) {
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(configManager.getMessage("invalid_level", Map.of("max", String.valueOf(enchant.getMaxLevel())), true));
                return;
            }
        }

        if (level < 1 || level > enchant.getMaxLevel()) {
            sender.sendMessage(configManager.getMessage("invalid_level", Map.of("max", String.valueOf(enchant.getMaxLevel())), true));
            return;
        }

        ItemStack held = target.getInventory().getItemInMainHand();
        if (held.getType().isAir()) {
            sender.sendMessage(configManager.getMessage("give_no_item"));
            return;
        }

        if (!enchant.getTarget().matchesOrBook(held.getType())) {
            sender.sendMessage(configManager.getMessage("invalid_target"));
            return;
        }

        if (enchantmentManager.hasIncompatibleVanilla(held, enchant)) {
            String conflict = enchantmentManager.getIncompatibleVanillaName(held, enchant);
            sender.sendMessage(configManager.getMessage("incompatible", Map.of("incompatible", conflict != null ? conflict : "Unknown"), true));
            return;
        }

        ItemStack updated;
        if (held.getAmount() > 1) {
            held.setAmount(held.getAmount() - 1);
            ItemStack single = held.clone();
            single.setAmount(1);
            updated = enchantmentManager.applyEnchantment(single, enchant, level);
            HashMap<Integer, ItemStack> overflow = target.getInventory().addItem(updated);
            for (ItemStack left : overflow.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), left);
            }
        } else {
            updated = enchantmentManager.applyEnchantment(held, enchant, level);
            target.getInventory().setItemInMainHand(updated);
        }

        Map<String, String> placeholders = Map.of(
                "enchant", configManager.getEnchantDisplayName(enchant),
                "level", String.valueOf(level),
                "player", target.getName()
        );
        sender.sendMessage(configManager.getMessage("give_success", placeholders, true));
    }

    private void handleBook(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(configManager.parse("<red>Использование: /venchants book <игрок> <зачарование> [уровень]</red>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(configManager.getMessage("player_not_found", Map.of("player", args[1]), true));
            return;
        }

        CustomEnchantment enchant = CustomEnchantment.fromId(args[2]);
        if (enchant == null) {
            sender.sendMessage(configManager.getMessage("invalid_enchant", Map.of("enchant", args[2]), true));
            return;
        }

        int level = 1;
        if (args.length >= 4) {
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(configManager.getMessage("invalid_level", Map.of("max", String.valueOf(enchant.getMaxLevel())), true));
                return;
            }
        }

        if (level < 1 || level > enchant.getMaxLevel()) {
            sender.sendMessage(configManager.getMessage("invalid_level", Map.of("max", String.valueOf(enchant.getMaxLevel())), true));
            return;
        }

        ItemStack book = enchantmentManager.createBook(enchant, level);
        HashMap<Integer, ItemStack> overflow = target.getInventory().addItem(book);
        if (!overflow.isEmpty()) {
            for (ItemStack leftover : overflow.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), leftover);
            }
        }

        Map<String, String> placeholders = Map.of(
                "enchant", configManager.getEnchantDisplayName(enchant),
                "level", String.valueOf(level),
                "player", target.getName()
        );
        sender.sendMessage(configManager.getMessage("book_success", placeholders, true));
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(configManager.parse("<dark_purple>=== <light_purple>VanillaPlusEnchants Команды<dark_purple> ===</dark_purple>"));
        sender.sendMessage(configManager.parse("<yellow>/" + label + " give <игрок> <зачарование> [уровень]</yellow> <gray>- наложить зачарование на предмет в руке</gray>"));
        sender.sendMessage(configManager.parse("<yellow>/" + label + " book <игрок> <зачарование> [уровень]</yellow> <gray>- выдать книгу с зачарованием</gray>"));
        sender.sendMessage(configManager.parse("<yellow>/" + label + " list</yellow> <gray>- список всех доступных зачарований</gray>"));
        sender.sendMessage(configManager.parse("<yellow>/" + label + " reload</yellow> <gray>- перезагрузить конфигурацию</gray>"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("vanillaplusenchants.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filterPrefix(List.of("give", "book", "list", "reload"), args[0]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("book"))) {
            return filterPrefix(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[1]);
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("book"))) {
            List<String> enchants = Arrays.stream(CustomEnchantment.values()).map(CustomEnchantment::getId).collect(Collectors.toList());
            return filterPrefix(enchants, args[2]);
        }

        if (args.length == 4 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("book"))) {
            CustomEnchantment enchant = CustomEnchantment.fromId(args[2]);
            if (enchant != null) {
                List<String> levels = new ArrayList<>();
                for (int i = 1; i <= enchant.getMaxLevel(); i++) {
                    levels.add(String.valueOf(i));
                }
                return filterPrefix(levels, args[3]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterPrefix(List<String> list, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return list;
        }
        String p = prefix.toLowerCase(Locale.ROOT);
        return list.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(p)).collect(Collectors.toList());
    }
}
