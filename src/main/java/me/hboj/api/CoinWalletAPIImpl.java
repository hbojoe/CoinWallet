package me.hboj.api;

import me.hboj.CoinWallet;
import me.hboj.inventory.InventoryManager;
import me.hboj.inventory.WalletHolder;
import me.hboj.util.CoinUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public final class CoinWalletAPIImpl implements CoinWalletAPI {

    private final CoinWallet plugin;

    public CoinWalletAPIImpl(CoinWallet plugin) {
        this.plugin = plugin;
    }

    @Override
    public int getBalance(OfflinePlayer player) {
        return InventoryManager.loadBalance(player);
    }

    @Override
    public int getBalance(UUID playerId) {
        return getBalance(Bukkit.getOfflinePlayer(playerId));
    }

    @Override
    public boolean has(OfflinePlayer player, int amount) {
        return amount >= 0 && getBalance(player) >= amount;
    }

    @Override
    public int getMaxBalance() {
        return CoinUtil.getMaxBalance();
    }

    @Override
    public ConcurrentHashMap<String, Double> getCurrencyValues() {
        ConcurrentHashMap<String, Double> values = new ConcurrentHashMap<>();
        values.put("B", (double) CoinUnit.BRONZE.getValue());
        values.put("S", (double) CoinUnit.SILVER.getValue());
        values.put("G", (double) CoinUnit.GOLD.getValue());
        return values;
    }

    @Override
    public int toValue(CoinUnit coinUnit, int amount) {
        if (coinUnit == null || amount < 0) {
            return -1;
        }

        long totalValue = (long) coinUnit.getValue() * amount;
        if (totalValue > Integer.MAX_VALUE) {
            return -1;
        }

        return (int) totalValue;
    }

    @Override
    public TransactionResult setBalance(OfflinePlayer player, int amount) {
        if (amount < 0) {
            return failure(player, "Amount cannot be negative.");
        }

        int maxBalance = getMaxBalance();
        if (amount > maxBalance) {
            return failure(player, "Amount exceeds wallet capacity.");
        }

        int current = getBalance(player);
        InventoryManager.saveBalance(player, amount);
        refreshWalletViews(player);
        return new TransactionResult(true, current, amount, Math.abs(amount - current), "Balance updated.");
    }

    @Override
    public TransactionResult add(OfflinePlayer player, int amount) {
        if (amount < 0) {
            return failure(player, "Amount cannot be negative.");
        }

        int current = getBalance(player);
        int maxBalance = getMaxBalance();

        if ((long) current + amount > maxBalance) {
            return failure(player, "That would exceed wallet capacity.");
        }

        int updated = current + amount;
        InventoryManager.saveBalance(player, updated);
        refreshWalletViews(player);
        return new TransactionResult(true, current, updated, amount, "Funds added.");
    }

    @Override
    public TransactionResult remove(OfflinePlayer player, int amount) {
        if (amount < 0) {
            return failure(player, "Amount cannot be negative.");
        }

        int current = getBalance(player);
        if (current < amount) {
            return failure(player, "Insufficient funds.");
        }

        int updated = current - amount;
        InventoryManager.saveBalance(player, updated);
        refreshWalletViews(player);
        return new TransactionResult(true, current, updated, amount, "Funds removed.");
    }

    @Override
    public TransactionResult addCoins(OfflinePlayer player, CoinUnit coinUnit, int amount) {
        int convertedValue = toValue(coinUnit, amount);
        if (convertedValue < 0) {
            return failure(player, "Invalid coin conversion request.");
        }
        return add(player, convertedValue);
    }

    @Override
    public TransactionResult removeCoins(OfflinePlayer player, CoinUnit coinUnit, int amount) {
        int convertedValue = toValue(coinUnit, amount);
        if (convertedValue < 0) {
            return failure(player, "Invalid coin conversion request.");
        }
        return remove(player, convertedValue);
    }

    private TransactionResult failure(OfflinePlayer player, String message) {
        int current = getBalance(player);
        return new TransactionResult(false, current, current, 0, message);
    }

    private void refreshWalletViews(OfflinePlayer target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!(viewer.getOpenInventory().getTopInventory().getHolder() instanceof WalletHolder holder)) {
                continue;
            }

            if (!holder.getPlayer().getUniqueId().equals(target.getUniqueId())) {
                continue;
            }

            viewer.openInventory(InventoryManager.loadWallet(target));
        }
    }
}
