package me.hboj.commands;

import me.hboj.CoinWallet;
import me.hboj.inventory.InventoryManager;
import me.hboj.util.ChatUtil;
import me.hboj.util.CoinUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class WalletbalCommand implements CommandExecutor {

    private final CoinWallet plugin;

    public WalletbalCommand(CoinWallet plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String arg, String[] args) {
        if (!sender.hasPermission("coinwallet.walletbal")) {
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(ChatUtil.formatWithPrefix(plugin.getConfig().getString("messages.not-a-player")));
                return true;
            }

            String playerName = LegacyComponentSerializer.legacySection().serialize(p.displayName());
            int balance = InventoryManager.loadBalance(p);
            sendBalanceBreakdown(sender, playerName, balance, true);
            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("coinwallet.walletbal-others")) {
                return true;
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            if (!player.hasPlayedBefore()) {
                sender.sendMessage(ChatUtil.formatWithPrefix(plugin.getConfig().getString("messages.player-dont-exist")));
                return true;
            }

            String playerName = player.getName();
            if (player.isOnline() && player.getPlayer() != null) {
                playerName = LegacyComponentSerializer.legacySection().serialize(player.getPlayer().displayName());
            }

            int balance = InventoryManager.loadBalance(player);
            sendBalanceBreakdown(sender, playerName, balance, false);
            return true;
        }

        return true;
    }

    private void sendBalanceBreakdown(CommandSender sender, String playerName, int amount, boolean selfView) {
        String headerPath = selfView ? "messages.walletbal-layout.header-self" : "messages.walletbal-layout.header-other";
        String defaultHeader = selfView
                ? "&eWallet &7>> &fYour balance breakdown"
                : "&eWallet &7>> &f%PLAYER%'s balance breakdown";

        String header = plugin.getConfig().getString(headerPath, defaultHeader);
        sender.sendMessage(ChatUtil.formatWithPrefix(applyPlaceholders(header, playerName, amount)));

        List<String> lines = plugin.getConfig().getStringList("messages.walletbal-layout.lines");
        if (lines.isEmpty()) {
            lines = List.of(
                    "&8&m------------------------------",
                    "&6GoldCoin &7>> &fBalance: &e%GOLD%G",
                    "&7SilverCoin &7>> &fBalance: &e%SILVER%S",
                    "&cCopperCoin &7>> &fBalance: &e%BRONZE%C",
                    "&8&m------------------------------",
                    "&eTotal Value &7>> &f%AMOUNT%C"
            );
        }

        for (String line : lines) {
            sender.sendMessage(ChatUtil.format(applyPlaceholders(line, playerName, amount)));
        }
    }

    private String applyPlaceholders(String message, String playerName, int amount) {
        int[] counts = CoinUtil.toCounts(amount);
        String result = message;
        result = result.replace("%PLAYER%", playerName);
        result = result.replace("%AMOUNT%", String.valueOf(amount));
        result = result.replace("%GOLD%", String.valueOf(counts[0]));
        result = result.replace("%SILVER%", String.valueOf(counts[1]));
        result = result.replace("%BRONZE%", String.valueOf(counts[2]));
        result = result.replace("%GOLD_VALUE%", String.valueOf(CoinUtil.CoinType.GOLD.getValue()));
        result = result.replace("%SILVER_VALUE%", String.valueOf(CoinUtil.CoinType.SILVER.getValue()));
        result = result.replace("%BRONZE_VALUE%", String.valueOf(CoinUtil.CoinType.BRONZE.getValue()));
        return result;
    }
}
