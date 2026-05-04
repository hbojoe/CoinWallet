package me.hboj.commands;

import me.hboj.CoinWallet;
import me.hboj.inventory.InventoryManager;
import me.hboj.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WalletCommand implements TabExecutor {

    private final CoinWallet plugin;
    private final CoinWalletCommand coinWalletCommand;
    private final WalletbalCommand walletbalCommand;
    private final WalletTopCommand walletTopCommand;

    public WalletCommand(CoinWallet plugin, CoinWalletCommand coinWalletCommand, WalletbalCommand walletbalCommand, WalletTopCommand walletTopCommand) {
        this.plugin = plugin;
        this.coinWalletCommand = coinWalletCommand;
        this.walletbalCommand = walletbalCommand;
        this.walletTopCommand = walletTopCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String arg, String[] args) {
        if (args.length > 0) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("eco")) {
                return coinWalletCommand.executeEcoCommand(sender, arg, args);
            }

            if (subCommand.equals("balance") || subCommand.equals("bal")) {
                return walletbalCommand.onCommand(sender, command, arg, withoutFirst(args));
            }

            if (subCommand.equals("top")) {
                return walletTopCommand.onCommand(sender, command, arg, withoutFirst(args));
            }

            if (subCommand.equals("open")) {
                return openWallet(sender, withoutFirst(args));
            }
        }

        return openWallet(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            if (sender.hasPermission("coinwallet.wallet")) {
                suggestions.add("open");
            }
            if (sender.hasPermission("coinwallet.walletbal")) {
                suggestions.add("balance");
                suggestions.add("bal");
            }
            if (sender.hasPermission("coinwallet.wallettop")) {
                suggestions.add("top");
            }
            if (sender.hasPermission("coinwallet.eco")) {
                suggestions.add("eco");
            }
            if (sender.hasPermission("coinwallet.wallet-others")) {
                suggestions.addAll(CommandSuggestions.onlinePlayers(args[0]));
            }
            return CommandSuggestions.matching(suggestions, args[0]);
        }

        if (args[0].equalsIgnoreCase("eco")) {
            return coinWalletCommand.tabCompleteEco(sender, args);
        }

        if ((args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("bal")) && args.length == 2 && sender.hasPermission("coinwallet.walletbal-others")) {
            return CommandSuggestions.onlinePlayers(args[1]);
        }

        if (args[0].equalsIgnoreCase("top") && args.length == 2 && sender.hasPermission("coinwallet.wallettop")) {
            return CommandSuggestions.matching(List.of("1", "2", "3"), args[1]);
        }

        if (args[0].equalsIgnoreCase("open") && args.length == 2 && sender.hasPermission("coinwallet.wallet-others")) {
            return CommandSuggestions.onlinePlayers(args[1]);
        }

        return List.of();
    }

    private boolean openWallet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("coinwallet.wallet")) {
            return true;
        }

        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatUtil.formatWithPrefix(plugin.getConfig().getString("messages.not-a-player")));
            return true;
        }

        if (args.length == 0) {
            Inventory inventory = InventoryManager.loadWallet(p);
            p.openInventory(inventory);
            return true;
        }

        if (args.length == 1 && p.hasPermission("coinwallet.wallet-others")) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            if (player.hasPlayedBefore()) {
                Inventory inventory = InventoryManager.loadWallet(player);
                p.openInventory(inventory);
            } else {
                sender.sendMessage(ChatUtil.formatWithPrefix(plugin.getConfig().getString("messages.player-dont-exist")));
            }
        }

        return true;
    }

    private String[] withoutFirst(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}

