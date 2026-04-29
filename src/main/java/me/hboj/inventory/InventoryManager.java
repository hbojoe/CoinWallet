package me.hboj.inventory;

import me.hboj.CoinWallet;
import me.hboj.util.CoinUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.UUID;

public class InventoryManager {

    private static final HashMap<UUID, Integer> balanceCache = new HashMap<>();
    private static final HashMap<UUID, EnumMap<CoinUtil.CoinType, ItemStack>> templateCache = new HashMap<>();

    public static Inventory loadWallet(OfflinePlayer player) {
        int balance = loadBalance(player);
        EnumMap<CoinUtil.CoinType, ItemStack> templates = loadTemplates(player);

        Inventory wallet = Bukkit.createInventory(new WalletHolder(player), 54, Component.text("Coin Wallet"));

        populateInventory(wallet, balance, templates);

        return  wallet;
    }

    public static void saveWalletFromInventory(OfflinePlayer player, Inventory inv) {
        long totalValue = 0;
        EnumMap<CoinUtil.CoinType, ItemStack> templates = loadTemplates(player);

        for (ItemStack item : inv.getContents()) {
            if (item == null) continue;

            CoinUtil.CoinType coinType = CoinUtil.getCoinType(item);
            if (coinType == null) continue;

            totalValue += (long) item.getAmount() * coinType.getValue();
            templates.put(coinType, CoinUtil.toTemplate(item));
        }

        if (totalValue > CoinUtil.getMaxBalance()) totalValue = CoinUtil.getMaxBalance();

        saveBalance(player, (int) totalValue, templates);
    }

    public static void normalizeWalletInventory(OfflinePlayer player, Inventory inv) {
        long totalValue = 0;
        EnumMap<CoinUtil.CoinType, ItemStack> templates = loadTemplates(player);

        for (ItemStack item : inv.getContents()) {
            if (item == null) continue;

            CoinUtil.CoinType coinType = CoinUtil.getCoinType(item);
            if (coinType == null) continue;

            totalValue += (long) item.getAmount() * coinType.getValue();
            templates.put(coinType, CoinUtil.toTemplate(item));
        }

        int cappedValue = (int) Math.min(totalValue, CoinUtil.getMaxBalance());
        inv.clear();
        populateInventory(inv, cappedValue, templates);

        if (player.isOnline()) {
            balanceCache.put(player.getUniqueId(), cappedValue);
            templateCache.put(player.getUniqueId(), cloneTemplates(templates));
        }
    }

    public static int loadBalance(OfflinePlayer player) {
        UUID playerId = player.getUniqueId();
        if (balanceCache.containsKey(playerId)) {
            return balanceCache.get(playerId);
        }

        int finalBalance = loadFromYaml(player);

        if (CoinWallet.getInstance().sql_enabled) {
            int sqlBal = CoinWallet.getInstance().getData().getDiamond(player.getUniqueId());
            if (sqlBal != -1) {
                finalBalance = sqlBal;
            }
        }

        balanceCache.put(playerId, finalBalance);

        return finalBalance;
    }

    public static void saveBalance(OfflinePlayer player, int amount) {
        saveBalance(player, amount, null);
    }

    public static void saveBalance(OfflinePlayer player, int amount, EnumMap<CoinUtil.CoinType, ItemStack> templates) {
        balanceCache.put(player.getUniqueId(), amount);
        if (player.isOnline() && templates != null) {
            templateCache.put(player.getUniqueId(), cloneTemplates(templates));
        }

        File file = new File(CoinWallet.getInstance().getDataFolder(), "wallets/" + player.getUniqueId() + ".yml");
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        YamlConfiguration config = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        config.set("balance", amount);
        if (templates != null) {
            for (CoinUtil.CoinType coinType : CoinUtil.CoinType.values()) {
                ItemStack template = templates.get(coinType);
                if (template != null && !template.getType().isAir()) {
                    config.set(coinType.getTemplatePath(), CoinUtil.toTemplate(template));
                }
            }
        }
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }

        if (CoinWallet.getInstance().sql_enabled) {
            Bukkit.getScheduler().runTaskAsynchronously(CoinWallet.getInstance(), () -> {
                CoinWallet.getInstance().getData().setDiamond(player , amount);
            });
        }
    }

    private static int loadFromYaml(OfflinePlayer player) {
        File file = new File(CoinWallet.getInstance().getDataFolder(), "wallets/" + player.getUniqueId() + ".yml");
        if (!file.exists()) return 0;
        return YamlConfiguration.loadConfiguration(file).getInt("balance", 0);
    }

    private static EnumMap<CoinUtil.CoinType, ItemStack> loadTemplates(OfflinePlayer player) {
        if (player.isOnline() && templateCache.containsKey(player.getUniqueId())) {
            return cloneTemplates(templateCache.get(player.getUniqueId()));
        }

        EnumMap<CoinUtil.CoinType, ItemStack> templates = new EnumMap<>(CoinUtil.CoinType.class);

        File file = new File(CoinWallet.getInstance().getDataFolder(), "wallets/" + player.getUniqueId() + ".yml");
        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (CoinUtil.CoinType coinType : CoinUtil.CoinType.values()) {
                ItemStack template = config.getItemStack(coinType.getTemplatePath());
                if (template != null && !template.getType().isAir()) {
                    templates.put(coinType, CoinUtil.toTemplate(template));
                }
            }
        }

        if (player.isOnline()) {
            templateCache.put(player.getUniqueId(), cloneTemplates(templates));
        }

        return templates;
    }

    private static EnumMap<CoinUtil.CoinType, ItemStack> cloneTemplates(EnumMap<CoinUtil.CoinType, ItemStack> source) {
        EnumMap<CoinUtil.CoinType, ItemStack> clone = new EnumMap<>(CoinUtil.CoinType.class);
        for (CoinUtil.CoinType coinType : CoinUtil.CoinType.values()) {
            ItemStack template = source.get(coinType);
            if (template != null && !template.getType().isAir()) {
                clone.put(coinType, CoinUtil.toTemplate(template));
            }
        }
        return clone;
    }

    private static void populateInventory(Inventory wallet, int balance, EnumMap<CoinUtil.CoinType, ItemStack> templates) {
        int[] counts = CoinUtil.toCounts(balance);

        addCoinStacks(wallet, CoinUtil.CoinType.GOLD, counts[0], templates.get(CoinUtil.CoinType.GOLD));
        addCoinStacks(wallet, CoinUtil.CoinType.SILVER, counts[1], templates.get(CoinUtil.CoinType.SILVER));
        addCoinStacks(wallet, CoinUtil.CoinType.BRONZE, counts[2], templates.get(CoinUtil.CoinType.BRONZE));
    }

    private static void addCoinStacks(Inventory wallet, CoinUtil.CoinType coinType, int amount, ItemStack template) {
        if (amount <= 0) {
            return;
        }

        int maxStack = template != null ? template.getMaxStackSize() : CoinUtil.getCoinMaterial(coinType).getMaxStackSize();
        while (amount > 0) {
            int stackAmount = Math.min(maxStack, amount);
            wallet.addItem(CoinUtil.createCoinStack(coinType, stackAmount, template));
            amount -= stackAmount;
        }
    }
}


