package me.hboj.listener;

import me.hboj.CoinWallet;
import me.hboj.inventory.InventoryManager;
import me.hboj.inventory.WalletHolder;
import me.hboj.util.CoinUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final CoinWallet plugin;

    public InventoryListener(CoinWallet plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();

        if (inventory.getHolder() instanceof WalletHolder holder) {
            if (!holder.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                if (!player.hasPermission("coinwallet.wallet-others")) {
                    return;
                }
            }

            InventoryManager.normalizeWalletInventory(holder.getPlayer(), inventory);
            InventoryManager.saveWalletFromInventory(holder.getPlayer(), inventory);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof WalletHolder)) {
            return;
        }

        Inventory wallet = event.getView().getTopInventory();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        boolean clickedWallet = event.getRawSlot() < wallet.getSize();
        boolean shouldNormalize = false;

        if (clickedWallet) {
            shouldNormalize = true;
            if (clickedItem != null && !clickedItem.getType().isAir() && !CoinUtil.isCoin(clickedItem)) {
                event.setCancelled(true);
                return;
            }

            if (cursorItem != null && !cursorItem.getType().isAir() && !CoinUtil.isCoin(cursorItem)) {
                event.setCancelled(true);
                return;
            }

            if (event.getClick() == ClickType.NUMBER_KEY) {
                ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                if (hotbarItem != null && !hotbarItem.getType().isAir() && !CoinUtil.isCoin(hotbarItem)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (event.isShiftClick()) {
            shouldNormalize = true;
            if (clickedItem != null && !clickedItem.getType().isAir() && !CoinUtil.isCoin(clickedItem)) {
                event.setCancelled(true);
                return;
            }
        }

        if (shouldNormalize && !event.isCancelled()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (wallet.getHolder() instanceof WalletHolder holder) {
                    InventoryManager.normalizeWalletInventory(holder.getPlayer(), wallet);
                }
            });
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof WalletHolder holder)) {
            return;
        }

        Inventory wallet = event.getView().getTopInventory();
        boolean touchingWallet = event.getRawSlots().stream().anyMatch(slot -> slot < wallet.getSize());
        if (!touchingWallet) {
            return;
        }

        ItemStack oldCursor = event.getOldCursor();
        if (oldCursor != null && !oldCursor.getType().isAir() && !CoinUtil.isCoin(oldCursor)) {
            event.setCancelled(true);
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> InventoryManager.normalizeWalletInventory(holder.getPlayer(), wallet));
    }
}
