package me.hboj.util;

import me.hboj.CoinWallet;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class CoinUtil {

    private static final int CONVERSION_RATE = 64;

    private CoinUtil() {
    }

    public enum CoinType {
        GOLD("coins.gold", 10009, CONVERSION_RATE * CONVERSION_RATE, Material.GOLD_NUGGET, "&6Gold Coin"),
        SILVER("coins.silver", 10010, CONVERSION_RATE, Material.IRON_NUGGET, "&7Silver Coin"),
        BRONZE("coins.bronze", 10011, 1, Material.BRICK, "&cBronze Coin");

        private final String configPath;
        private final int defaultId;
        private final int value;
        private final Material defaultMaterial;
        private final String defaultDisplayName;

        CoinType(String configPath, int defaultId, int value, Material defaultMaterial, String defaultDisplayName) {
            this.configPath = configPath;
            this.defaultId = defaultId;
            this.value = value;
            this.defaultMaterial = defaultMaterial;
            this.defaultDisplayName = defaultDisplayName;
        }

        public String getConfigPath() {
            return configPath;
        }

        public int getDefaultId() {
            return defaultId;
        }

        public int getValue() {
            return value;
        }

        public Material getDefaultMaterial() {
            return defaultMaterial;
        }

        public String getDefaultDisplayName() {
            return defaultDisplayName;
        }

        public String getTemplatePath() {
            return "templates." + name().toLowerCase();
        }
    }

    public static int getCoinId(CoinType type) {
        FileConfiguration config = CoinWallet.getInstance().getConfig();
        return config.getInt(type.getConfigPath() + ".id", type.getDefaultId());
    }

    public static Material getCoinMaterial(CoinType type) {
        FileConfiguration config = CoinWallet.getInstance().getConfig();
        String materialName = config.getString(type.getConfigPath() + ".material", type.getDefaultMaterial().name());
        Material material = Material.matchMaterial(materialName);
        if (material == null || material.isAir()) {
            return type.getDefaultMaterial();
        }
        return material;
    }

    public static String getCoinDisplayName(CoinType type) {
        FileConfiguration config = CoinWallet.getInstance().getConfig();
        return config.getString(type.getConfigPath() + ".name", type.getDefaultDisplayName());
    }

    public static CoinType getCoinType(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return null;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasCustomModelData()) {
            return null;
        }

        int customModelData = itemMeta.getCustomModelData();
        for (CoinType coinType : CoinType.values()) {
            if (customModelData == getCoinId(coinType)) {
                return coinType;
            }
        }

        return null;
    }

    public static boolean isCoin(ItemStack itemStack) {
        return getCoinType(itemStack) != null;
    }

    public static ItemStack toTemplate(ItemStack itemStack) {
        ItemStack template = itemStack.clone();
        template.setAmount(1);
        return template;
    }

    public static ItemStack createCoinStack(CoinType type, int amount, ItemStack template) {
        ItemStack stack;
        if (template != null && !template.getType().isAir()) {
            stack = template.clone();
            applyFallbackName(type, stack);
        } else {
            stack = new ItemStack(getCoinMaterial(type));
            ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.setCustomModelData(getCoinId(type));
            itemMeta.displayName(ChatUtil.format(getCoinDisplayName(type)));
            stack.setItemMeta(itemMeta);
        }

        stack.setAmount(amount);
        return stack;
    }

    private static void applyFallbackName(CoinType type, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }

        if (!meta.hasDisplayName()) {
            meta.displayName(ChatUtil.format(getCoinDisplayName(type)));
            stack.setItemMeta(meta);
        }
    }

    public static int getMaxBalance() {
        int maxStack = 64;
        return 54 * maxStack * CoinType.GOLD.getValue();
    }

    public static int[] toCounts(int balance) {
        int gold = balance / CoinType.GOLD.getValue();
        int remainderAfterGold = balance % CoinType.GOLD.getValue();
        int silver = remainderAfterGold / CoinType.SILVER.getValue();
        int bronze = remainderAfterGold % CoinType.SILVER.getValue();
        return new int[]{gold, silver, bronze};
    }

    public static CoinType parseCoinType(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normalized = input.toLowerCase()
                .replace("_", "")
                .replace("-", "")
                .trim();

        if (normalized.endsWith("coin")) {
            normalized = normalized.substring(0, normalized.length() - 4);
        }

        if (normalized.equals("gold")) {
            return CoinType.GOLD;
        }
        if (normalized.equals("silver")) {
            return CoinType.SILVER;
        }
        if (normalized.equals("bronze") || normalized.equals("copper")) {
            return CoinType.BRONZE;
        }

        return null;
    }
}


