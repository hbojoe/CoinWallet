package me.hboj.api;

import org.bukkit.OfflinePlayer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public interface CoinWalletAPI {

    enum CoinUnit {
        BRONZE(1),
        SILVER(64),
        GOLD(4096);

        private final int value;

        CoinUnit(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    final class TransactionResult {
        private final boolean success;
        private final int previousBalance;
        private final int newBalance;
        private final int amountChanged;
        private final String message;

        public TransactionResult(boolean success, int previousBalance, int newBalance, int amountChanged, String message) {
            this.success = success;
            this.previousBalance = previousBalance;
            this.newBalance = newBalance;
            this.amountChanged = amountChanged;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getPreviousBalance() {
            return previousBalance;
        }

        public int getNewBalance() {
            return newBalance;
        }

        public int getAmountChanged() {
            return amountChanged;
        }

        public String getMessage() {
            return message;
        }
    }

    int getBalance(OfflinePlayer player);

    int getBalance(UUID playerId);

    boolean has(OfflinePlayer player, int amount);

    int getMaxBalance();

    ConcurrentHashMap<String, Double> getCurrencyValues();

    int toValue(CoinUnit coinUnit, int amount);

    TransactionResult setBalance(OfflinePlayer player, int amount);

    TransactionResult add(OfflinePlayer player, int amount);

    TransactionResult remove(OfflinePlayer player, int amount);

    TransactionResult addCoins(OfflinePlayer player, CoinUnit coinUnit, int amount);

    TransactionResult removeCoins(OfflinePlayer player, CoinUnit coinUnit, int amount);
}
