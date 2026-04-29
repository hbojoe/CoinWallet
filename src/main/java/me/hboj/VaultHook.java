package me.hboj;

import me.hboj.api.CoinWalletAPI;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class VaultHook extends AbstractEconomy {

    private final CoinWallet plugin;

    public VaultHook(CoinWallet plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "hCoinWallet";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double amount) {
        return (int) amount + " Coins";
    }

    @Override
    public String currencyNamePlural() {
        return "Coins";
    }

    @Override
    public String currencyNameSingular() {
        return "Coin";
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return plugin.getApi().getBalance(player);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if(player.getName() != null) {
            org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(player.getName());
            if(p != null) player = p;
        }

        if (amount < 0) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Negative");

        int amountToAdd = (int) amount;
        CoinWalletAPI.TransactionResult result = plugin.getApi().add(player, amountToAdd);
        if (!result.isSuccess()) {
            return new EconomyResponse(0, result.getNewBalance(), EconomyResponse.ResponseType.FAILURE, result.getMessage());
        }
        return new EconomyResponse(result.getAmountChanged(), result.getNewBalance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Negative");

        int toTake = (int) amount;
        CoinWalletAPI.TransactionResult result = plugin.getApi().remove(player, toTake);
        if (!result.isSuccess()) {
            return new EconomyResponse(0, result.getNewBalance(), EconomyResponse.ResponseType.FAILURE, result.getMessage());
        }
        return new EconomyResponse(result.getAmountChanged(), result.getNewBalance(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override public boolean createPlayerAccount(String playerName, String worldName) { return createPlayerAccount(playerName); }
    @Override public double getBalance(String playerName, String world) { return getBalance(playerName); }

    @Override
    public boolean has(String s, double v) {
        return getBalance(s) >= v;
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return getBalance(s, s1) >= v;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        return withdrawPlayer(player, amount);
    }

    @Override public boolean hasAccount(String playerName) { return true; }
    @Override public boolean hasAccount(String playerName, String worldName) { return true; }

    @Override
    public double getBalance(String playerName) {
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) { return withdrawPlayer(playerName, amount); }


    @Override public EconomyResponse depositPlayer(String playerName, String worldName, double amount) { return depositPlayer(playerName, amount); }
    @Override public EconomyResponse createBank(String name, String player) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No Banks"); }
    @Override public EconomyResponse deleteBank(String name) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No Banks"); }
    @Override public EconomyResponse bankBalance(String name) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No Banks"); }
    @Override public EconomyResponse bankHas(String name, double amount) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No Banks"); }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No Banks"); }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No Banks"); }
    @Override public EconomyResponse isBankOwner(String name, String playerName) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No Banks"); }
    @Override public EconomyResponse isBankMember(String name, String playerName) { return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No Banks"); }
    @Override public List<String> getBanks() { return null; }
}
