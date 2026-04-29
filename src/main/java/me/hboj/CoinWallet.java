package me.hboj;

import me.hboj.api.CoinWalletAPI;
import me.hboj.api.CoinWalletAPIImpl;
import me.hboj.commands.CoinWalletCommand;
import me.hboj.commands.WalletCommand;
import me.hboj.commands.WalletTopCommand;
import me.hboj.commands.WalletbalCommand;
import me.hboj.data.MySQL;
import me.hboj.data.SQLgetter;
import me.hboj.listener.InventoryListener;
import me.hboj.util.ChatUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public final class CoinWallet extends JavaPlugin {

    private static CoinWallet instance;
    public static Component prefix;


    public boolean sql_enabled = false;
    public MySQL sql;
    private SQLgetter data;
    private CoinWalletAPI api;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        prefix = ChatUtil.format(getConfig().getString("prefix"));
        api = new CoinWalletAPIImpl(this);

        getServer().getPluginManager().registerEvents(new InventoryListener(this) , this);
        registerCommand("wallet", new WalletCommand(this));
        registerCommand("walletbal", new WalletbalCommand(this));
        registerCommand("wallettop", new WalletTopCommand(this));
        registerCommand("coinwallet", new CoinWalletCommand(this));
        getServer().getServicesManager().register(CoinWalletAPI.class, api, this, org.bukkit.plugin.ServicePriority.Highest);

        sql_enabled = this.getConfig().getBoolean("SQL.use-mysql");
        if(sql_enabled) {
            this.sql = new MySQL(this.getConfig().getString("SQL.host"), this.getConfig().getString("SQL.port"),
                    this.getConfig().getString("SQL.username"), this.getConfig().getString("SQL.password"),
                    this.getConfig().getString("SQL.database"));

            try {
                sql.connect();
            } catch (SQLException e) {
                sql_enabled = false;
                getLogger().log(Level.SEVERE, "Failed to connect to SQL", e);
            }
        }


        if(sql_enabled) {
            getLogger().info("Database connected ");
            getLogger().info("Leaderboard is enabled");

            this.data = new SQLgetter(this);
        }else{

            getLogger().warning("Database is not connected");
            getLogger().warning("Leaderboard is disabled");
        }

        boolean vaultHookEnabled = getConfig().getBoolean("vault-hook.enabled", false);
        if (vaultHookEnabled) {
            if (getServer().getPluginManager().getPlugin("Vault") != null) {
                getServer().getServicesManager().register(
                        net.milkbowl.vault.economy.Economy.class,
                        new VaultHook(this),
                        this,
                        org.bukkit.plugin.ServicePriority.Highest
                );
                getLogger().info("Hooked into Vault successfully!");
            } else {
                getLogger().warning("Vault hook is enabled in config but Vault was not found.");
            }
        } else {
            getLogger().info("Vault hook is disabled. Wallet balances are not exposed as server money.");
        }


    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregister(CoinWalletAPI.class, api);
        api = null;
        instance = null;
        if (sql != null) {
            sql.disconnect();
        }
    }

    public SQLgetter getData() {
        return data;
    }

    public static CoinWallet getInstance(){
        return instance;
    }

    public CoinWalletAPI getApi() {
        return api;
    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        PluginCommand pluginCommand = getCommand(commandName);
        if (pluginCommand == null) {
            throw new IllegalStateException("Command '" + commandName + "' is missing from plugin.yml");
        }
        pluginCommand.setExecutor(executor);
    }
}
