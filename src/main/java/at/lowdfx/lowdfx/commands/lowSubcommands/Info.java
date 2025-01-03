package at.lowdfx.lowdfx.commands.lowSubcommands;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Info implements CommandExecutor {
    public static final String adminPermission = "lowdfx.low.info";
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!cmd.getName().equalsIgnoreCase("low")) {
            return false;
        }

        // Prüfen, ob ein Subcommand angegeben wurde
        if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
            if (sender.hasPermission(adminPermission)) {

                    sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "MC-Vers.:" + ChatColor.GOLD + " 1.21.+");
                    sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Plugin-Vers.:" + ChatColor.GOLD + " 1.0");
                    sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Author:" + ChatColor.GOLD + " LowdFX");

            } else {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + lowdfx.config.getString("basic.noPermission"));
            }
            return true;
        }

        // Standardnachricht für den /low-Befehl ohne Subcommand
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Ungültiges Argument! Nutze /low help für Informationen.");
        return true;
    }
}