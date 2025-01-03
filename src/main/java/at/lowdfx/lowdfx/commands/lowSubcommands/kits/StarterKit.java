package at.lowdfx.lowdfx.commands.lowSubcommands.kits;

import at.lowdfx.lowdfx.items.starterkit.*;
import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StarterKit implements CommandExecutor {

    public static final String playerPermission = "lowdfx.low.starterkit";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!cmd.getName().equalsIgnoreCase("low")) {
            return false;
        }

        // Prüfen, ob ein Subcommand angegeben wurde
        if (args.length == 1 && args[0].equalsIgnoreCase("starterkit")) {
            // Wenn Player führ dies aus...

                if (sender.hasPermission(playerPermission) && sender instanceof Player) {
                    Player player = (Player) sender;
                    player.getInventory().addItem(StarterStoneSword.get());
                    player.getInventory().addItem(StarterStonePickaxe.get());
                    player.getInventory().addItem(StarterStoneShovel.get());
                    player.getInventory().addItem(StarterStoneAxe.get());
                    player.getInventory().addItem(StarterLeatherHelmet.get());
                    player.getInventory().addItem(StarterLeatherChestplate.get());
                    player.getInventory().addItem(StarterLeatherLeggings.get());
                    player.getInventory().addItem(StarterLeatherBoots.get());
                    player.getInventory().addItem(StarterFood.get());

                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Hier ist dein Starterkit!");
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
