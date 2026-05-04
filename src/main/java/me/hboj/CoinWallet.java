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
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        CoinWalletCommand coinWalletCommand = new CoinWalletCommand(this);
        WalletbalCommand walletbalCommand = new WalletbalCommand(this);
        WalletTopCommand walletTopCommand = new WalletTopCommand(this);

        WalletCommand walletCommand = new WalletCommand(this, coinWalletCommand, walletbalCommand, walletTopCommand);
        registerCommand("wallet", walletCommand);
        getServer().getCommandMap().register(getDescription().getName().toLowerCase(), new me.hboj.commands.NamespacedEcoCommand(coinWalletCommand));
        registerWalletAliases(walletCommand);
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
        if (executor instanceof TabCompleter tabCompleter) {
            pluginCommand.setTabCompleter(tabCompleter);
        }
    }

    private void registerWalletAliases(WalletCommand walletCommand) {
        List<String> aliases = getConfig().getStringList("wallet-aliases");
        if (aliases.isEmpty()) {
            String singleAlias = getConfig().getString("wallet-alias");
            if (singleAlias != null && !singleAlias.isBlank()) {
                aliases = List.of(singleAlias);
            }
        }

        if (aliases.isEmpty()) {
            return;
        }

        CommandMap commandMap = getServer().getCommandMap();
        Set<String> registeredAliases = new HashSet<>();
        Set<String> reservedCommands = Set.of("wallet", "walletbal", "wallettop", "coinwallet");

        for (String configuredAlias : aliases) {
            String alias = configuredAlias == null ? "" : configuredAlias.trim().toLowerCase();
            if (alias.isBlank() || !registeredAliases.add(alias)) {
                continue;
            }

            if (!alias.matches("[a-z0-9_-]+")) {
                getLogger().warning("Skipping invalid wallet alias '" + configuredAlias + "'. Use only letters, numbers, underscores, or hyphens.");
                continue;
            }

            if (reservedCommands.contains(alias) || commandMap.getCommand(alias) != null) {
                getLogger().warning("Skipping wallet alias '" + alias + "' because that command is already registered.");
                continue;
            }

            commandMap.register(getDescription().getName().toLowerCase(), new me.hboj.commands.WalletAliasCommand(alias, walletCommand));
            getLogger().info("Registered wallet alias /" + alias);
        }
    }
}
