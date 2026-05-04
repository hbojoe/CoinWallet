package me.hboj.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public final class CommandSuggestions {

    private CommandSuggestions() {
    }

    public static List<String> matching(Collection<String> options, String input) {
        String prefix = input == null ? "" : input.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(prefix))
                .toList();
    }

    public static List<String> onlinePlayers(String input) {
        return matching(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList(), input);
    }
}
