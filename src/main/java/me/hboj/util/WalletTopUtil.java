package me.hboj.util;

import me.hboj.CoinWallet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.List;

public class WalletTopUtil {
    public static List<Component> formattopboard(HashMap<String, Integer> diamondmap , int offset){
        List<Component> result = new ArrayList<>();
        int totalCoins = CoinWallet.getInstance().getData().getTotalDiamonds();

        String header = CoinWallet.getInstance().getConfig().getString("messages.wallet-top.header")
                .replace("%TOTAL_COINS%" , String.valueOf(totalCoins))
                .replace("%TOTAL_DIAMONDS%" , String.valueOf(totalCoins));

        result.add(ChatUtil.format(header));

        int i = (10 * (offset - 1)) + 1;

        for(Map.Entry<String , Integer> entry: sortByValue(diamondmap).entrySet()){
            String playername = entry.getKey();
            OfflinePlayer player = Bukkit.getOfflinePlayer(playername);

            if(player.isOnline()){
                playername = LegacyComponentSerializer.legacySection().serialize(player.getPlayer().displayName());
            }

            result.add(ChatUtil.walletTopListFormat(
                    playername,
                    i,
                    entry.getValue(),
                    CoinWallet.getInstance().getConfig().getString("messages.wallet-top.list")
            ));
            i++;
        }

        return result;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));


        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}


