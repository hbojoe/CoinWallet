package me.hboj.commands;

import me.hboj.CoinWallet;
import me.hboj.api.CoinWalletAPI;
import me.hboj.util.ChatUtil;
import me.hboj.util.CoinUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public class CoinWalletCommand implements TabExecutor {

    private final CoinWallet plugin;

    public CoinWalletCommand(CoinWallet plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return executeEcoCommand(sender, label, args);
    }

    public boolean executeEcoCommand(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("coinwallet.eco")) {
            return true;
        }

        if (args.length != 5 || !args[0].equalsIgnoreCase("eco")) {
            sender.sendMessage(ChatUtil.formatWithPrefix(getEcoUsage(label)));
            return true;
        }

        String action = args[1].toLowerCase();
        if (!action.equals("give") && !action.equals("take")) {
            sender.sendMessage(ChatUtil.formatWithPrefix(getMessage("messages.eco-invalid-action", "&cInvalid action. Use give or take.")));
            return true;
        }

        OfflinePlayer target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            target = Bukkit.getOfflinePlayer(args[2]);
        }

        if (!target.isOnline() && !target.hasPlayedBefore()) {
            sender.sendMessage(ChatUtil.formatWithPrefix(getMessage("messages.player-dont-exist", "&cPlayer Doesnt exist")));
            return true;
        }

        CoinUtil.CoinType coinType = CoinUtil.parseCoinType(args[3]);
        if (coinType == null) {
            sender.sendMessage(ChatUtil.formatWithPrefix(getMessage("messages.eco-invalid-coin", "&cInvalid coin type. Use bronze, silver, or gold.")));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[4]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(ChatUtil.formatWithPrefix(getMessage("messages.eco-invalid-amount", "&cAmount must be a positive whole number.")));
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage(ChatUtil.formatWithPrefix(getMessage("messages.eco-invalid-amount", "&cAmount must be a positive whole number.")));
            return true;
        }

        int unitValue = coinType.getValue();
        long valueDelta = (long) amount * unitValue;

        if (valueDelta > Integer.MAX_VALUE) {
            sender.sendMessage(ChatUtil.formatWithPrefix(getMessage("messages.eco-invalid-amount", "&cAmount is too large.")));
            return true;
        }

        CoinWalletAPI.TransactionResult result;
        if (action.equals("give")) {
            result = plugin.getApi().add(target, (int) valueDelta);
            if (!result.isSuccess()) {
                sender.sendMessage(ChatUtil.formatWithPrefix(getMessage("messages.eco-wallet-full", "&cThat would exceed this player's wallet capacity.")));
                return true;
            }
        } else {
            result = plugin.getApi().remove(target, (int) valueDelta);
            if (!result.isSuccess()) {
                sender.sendMessage(ChatUtil.formatWithPrefix(getMessage("messages.eco-insufficient", "&cThat player does not have enough funds.")));
                return true;
            }
        }

        int newBalance = result.getNewBalance();

        int[] resultingCounts = CoinUtil.toCounts(newBalance);
        String coinName = coinType.name().toLowerCase() + "coin";
        if (action.equals("give")) {
            sender.sendMessage(ChatUtil.formatWithPrefix(getMessage("messages.eco-give-success", "&aGave %COIN_AMOUNT% %COIN_TYPE% to %PLAYER%. &7Now: %GOLD%G %SILVER%S %BRONZE%B")
                    .replace("%PLAYER%", target.getName() == null ? target.getUniqueId().toString() : target.getName())
                    .replace("%COIN_AMOUNT%", String.valueOf(amount))
                    .replace("%COIN_TYPE%", coinName)
                    .replace("%VALUE%", String.valueOf(valueDelta))
                    .replace("%GOLD%", String.valueOf(resultingCounts[0]))
                    .replace("%SILVER%", String.valueOf(resultingCounts[1]))
                    .replace("%BRONZE%", String.valueOf(resultingCounts[2]))));
        } else {
            sender.sendMessage(ChatUtil.formatWithPrefix(getMessage("messages.eco-take-success", "&aTook %COIN_AMOUNT% %COIN_TYPE% from %PLAYER%. &7Now: %GOLD%G %SILVER%S %BRONZE%B")
                    .replace("%PLAYER%", target.getName() == null ? target.getUniqueId().toString() : target.getName())
                    .replace("%COIN_AMOUNT%", String.valueOf(amount))
                    .replace("%COIN_TYPE%", coinName)
                    .replace("%VALUE%", String.valueOf(valueDelta))
                    .replace("%GOLD%", String.valueOf(resultingCounts[0]))
                    .replace("%SILVER%", String.valueOf(resultingCounts[1]))
                    .replace("%BRONZE%", String.valueOf(resultingCounts[2]))));
        }

        if (target.isOnline() && !(sender instanceof Player player && player.getUniqueId().equals(target.getUniqueId()))) {
            Player onlineTarget = target.getPlayer();
            if (onlineTarget != null) {
                onlineTarget.sendMessage(ChatUtil.formatWithPrefix(getMessage("messages.eco-notify-target", "&eYour wallet was updated: %ACTION% %COIN_AMOUNT% %COIN_TYPE%. &7Now: %GOLD%G %SILVER%S %BRONZE%B")
                        .replace("%ACTION%", action.equals("give") ? "added" : "removed")
                        .replace("%COIN_AMOUNT%", String.valueOf(amount))
                        .replace("%COIN_TYPE%", coinName)
                        .replace("%VALUE%", String.valueOf(valueDelta))
                        .replace("%GOLD%", String.valueOf(resultingCounts[0]))
                        .replace("%SILVER%", String.valueOf(resultingCounts[1]))
                        .replace("%BRONZE%", String.valueOf(resultingCounts[2]))));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return tabCompleteEco(sender, args);
    }

    public List<String> tabCompleteEco(CommandSender sender, String[] args) {
        if (!sender.hasPermission("coinwallet.eco")) {
            return List.of();
        }

        if (args.length == 1) {
            return CommandSuggestions.matching(List.of("eco"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("eco")) {
            return CommandSuggestions.matching(List.of("give", "take"), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("eco")) {
            return CommandSuggestions.onlinePlayers(args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("eco")) {
            return CommandSuggestions.matching(List.of("bronze", "silver", "gold"), args[3]);
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("eco")) {
            return CommandSuggestions.matching(List.of("1", "10", "64"), args[4]);
        }

        return List.of();
    }

    private String getEcoUsage(String label) {
        String usage = getMessage("messages.eco-usage", "&cUsage: /wallet eco <give|take> <player> <bronze|silver|gold> <amount>");
        if (label.equalsIgnoreCase("coinwallet:eco")) {
            return usage.replace("/coinwallet eco", "/" + label).replace("/wallet eco", "/" + label);
        }
        return usage.replace("/coinwallet", "/" + label).replace("/wallet", "/" + label);
    }

    private String getMessage(String path, String fallback) {
        return plugin.getConfig().getString(path, fallback);
    }
}
