package me.hboj.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class WalletAliasCommand extends Command {

    private final WalletCommand walletCommand;

    public WalletAliasCommand(String name, WalletCommand walletCommand) {
        super(name, "Alias for /wallet", "/wallet [open|balance|bal|top|eco]", List.of());
        this.walletCommand = walletCommand;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return walletCommand.onCommand(sender, this, commandLabel, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return walletCommand.onTabComplete(sender, this, alias, args);
    }
}
