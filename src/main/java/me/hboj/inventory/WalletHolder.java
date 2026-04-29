package me.hboj.inventory;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class WalletHolder implements InventoryHolder {

    OfflinePlayer player;

    public WalletHolder(OfflinePlayer player){
        this.player = player;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }
}


