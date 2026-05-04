package me.hboj.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class NamespacedEcoCommand extends Command {

    private final CoinWalletCommand coinWalletCommand;

    public NamespacedEcoCommand(CoinWalletCommand coinWalletCommand) {
        super("coinwallet:eco", "Admin wallet economy command", "/coinwallet:eco <give|take> <player> <bronze|silver|gold> <amount>", List.of());
        this.coinWalletCommand = coinWalletCommand;
        setPermission("coinwallet.eco");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return coinWalletCommand.executeEcoCommand(sender, commandLabel, withEcoSubcommand(args));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return coinWalletCommand.tabCompleteEco(sender, withEcoSubcommand(args));
    }

    private String[] withEcoSubcommand(String[] args) {
        String[] routedArgs = Arrays.copyOf(args, args.length + 1);
        System.arraycopy(routedArgs, 0, routedArgs, 1, args.length);
        routedArgs[0] = "eco";
        return routedArgs;
    }
}
