package at.lowdfx.lowdfx.commands.lowSubcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;


public class LowHelp implements CommandExecutor {
    public static final String adminPermission = "lowdfx.low.help.admin";
    public static final String playerPermission = "lowdfx.low.help";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE.toString() + " → ";

        if (sender.hasPermission(playerPermission) || sender.hasPermission(adminPermission)) {
            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Low Subcommands -------");
            sender.sendMessage(commandColor + "/low starterkit" + arrow + color + " Gibt ein Starterkit");
        }
        if (sender.hasPermission(adminPermission)) {
            sender.sendMessage(commandColor + "/low info" + arrow + color + " Plugin Infos");
            sender.sendMessage(commandColor + "/low opkit" + arrow + color + " Gibt ein OP-Kit");
        }
        return true;
    }

}


