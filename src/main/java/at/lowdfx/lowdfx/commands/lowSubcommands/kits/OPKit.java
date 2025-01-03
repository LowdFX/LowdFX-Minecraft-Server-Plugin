package at.lowdfx.lowdfx.commands.lowSubcommands.kits;

import at.lowdfx.lowdfx.items.opkit.*;
import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OPKit implements CommandExecutor {
    public static final String adminPermission = "lowdfx.low.opkit";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!cmd.getName().equalsIgnoreCase("low")) {
            return false;
        }

        // Prüfen, ob ein Subcommand angegeben wurde
        if (args.length == 1 && args[0].equalsIgnoreCase("opkit")) {
            // Wenn Player führ dies aus...
            if (sender.hasPermission(adminPermission) && sender instanceof Player) {
                Player player = (Player) sender;
                player.getInventory().addItem(OPNetheriteSword.get());
                player.getInventory().addItem(OPNetheritePickaxe.get());
                player.getInventory().addItem(OPNetheriteShovel.get());
                player.getInventory().addItem(OPNetheriteAxe.get());
                player.getInventory().addItem(OPStick.get());
                player.getInventory().addItem(OPNetheriteHelmet.get());
                player.getInventory().addItem(OPNetheriteChestplate.get());
                player.getInventory().addItem(OPNetheriteLeggings.get());
                player.getInventory().addItem(OPNetheriteBoots.get());
                player.getInventory().addItem(OPFood.get());
                player.getInventory().addItem(OPApple.get());


                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Hier ist dein OP Kit!");
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
