package me.hboj.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class CoinWalletAPIProvider {

    private CoinWalletAPIProvider() {
    }

    public static CoinWalletAPI get() {
        RegisteredServiceProvider<CoinWalletAPI> registration = Bukkit.getServicesManager().getRegistration(CoinWalletAPI.class);
        return registration != null ? registration.getProvider() : null;
    }
}
