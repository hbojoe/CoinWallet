package me.hboj.util;

import me.hboj.CoinWallet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {

    public static Component format(String message) {
        if (message == null) return Component.empty();

        String hexParsed = hexColor(message);
        String legacyCode = hexParsed.replace('&', '\u00A7');
        return LegacyComponentSerializer.legacySection().deserialize(legacyCode);
    }

    public static Component formatWithPrefix(String message) {
        return CoinWallet.prefix.append(format(message));
    }

    private static String hexColor(String message) {
        final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, "\u00A7x"
                    + "\u00A7" + group.charAt(0) + "\u00A7" + group.charAt(1)
                    + "\u00A7" + group.charAt(2) + "\u00A7" + group.charAt(3)
                    + "\u00A7" + group.charAt(4) + "\u00A7" + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }

    public static Component walletbalformat(String playername, int amount, String message) {
        String result = message;
        int[] counts = CoinUtil.toCounts(amount);

        result = result.replace("%PLAYER%", playername);
        result = result.replace("%AMOUNT%", String.valueOf(amount));
        result = result.replace("%GOLD%", String.valueOf(counts[0]));
        result = result.replace("%SILVER%", String.valueOf(counts[1]));
        result = result.replace("%BRONZE%", String.valueOf(counts[2]));
        result = result.replace("%GOLD_VALUE%", String.valueOf(CoinUtil.CoinType.GOLD.getValue()));
        result = result.replace("%SILVER_VALUE%", String.valueOf(CoinUtil.CoinType.SILVER.getValue()));
        result = result.replace("%BRONZE_VALUE%", String.valueOf(CoinUtil.CoinType.BRONZE.getValue()));

        return formatWithPrefix(result);
    }

    public static Component walletTopListFormat(String playername, int rank, int amount, String message) {
        String result = message;

        result = result.replace("%PLAYER%", playername);
        result = result.replace("%RANK%", String.valueOf(rank));
        result = result.replace("%AMOUNT%", String.valueOf(amount));

        return format(result);
    }
}
