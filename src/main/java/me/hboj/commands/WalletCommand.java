package me.hboj.commands;

import me.hboj.CoinWallet;
import me.hboj.inventory.InventoryManager;
import me.hboj.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class WalletCommand implements CommandExecutor {

    CoinWallet plugin;

    public WalletCommand(CoinWallet plugin){this.plugin = plugin;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String arg, String[] args) {

        if(sender.hasPermission("coinwallet.wallet") && sender instanceof Player p){
            if(args.length ==0) {
                Inventory inventory = InventoryManager.loadWallet(p);
                p.openInventory(inventory);
            }else if (args.length == 1){
                if(p.hasPermission("coinwallet.wallet-others")){
                    OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                    if(player.hasPlayedBefore()) {
                        Inventory inventory = InventoryManager.loadWallet(player);
                        p.openInventory(inventory);
                    }else {
                        sender.sendMessage(ChatUtil.formatWithPrefix(plugin.getConfig().getString("messages.player-dont-exist") ));
                    }
                }
            }
        }

        return false;
    }
}


