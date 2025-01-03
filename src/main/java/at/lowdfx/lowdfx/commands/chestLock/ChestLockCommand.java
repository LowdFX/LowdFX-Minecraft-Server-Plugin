package at.lowdfx.lowdfx.commands.chestLock;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;


public class ChestLockCommand implements CommandExecutor {
    private final lowdfx plugin;
    public static final String playerPermission = "lowdfx.lock";
    public static final String adminPermission = "lowdfx.lock.admin";

    public ChestLockCommand(lowdfx plugin) {
        this.plugin = plugin;
    }


    private Set<Location> getConnectedChests(Block chestBlock) {
        Set<Location> connectedChests = new HashSet<>();
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block relativeBlock = chestBlock.getRelative(face);
            if (relativeBlock.getType() == Material.CHEST) {
                connectedChests.add(relativeBlock.getLocation());
            }
        }
        return connectedChests;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Nur Spieler können diesen Befehl ausführen.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            String title = ChatColor.GOLD.toString();
            String color = ChatColor.GRAY.toString();
            String commandColor = ChatColor.YELLOW.toString();
            String arrow = ChatColor.WHITE.toString() + " → ";

            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Chest Lock -------");
            sender.sendMessage(commandColor + "/lock" + arrow + color + " Sperrt die anvisierte Truhe");
            sender.sendMessage(commandColor + "/lock add <Spieler>" + arrow + color + " Fügt zur anvisierter Kiste einene Spieler zur Whitelist");
            sender.sendMessage(commandColor + "/lock remove" + arrow + color + " Entfernt von anvisierter Kiste einene Spieler von der Whitelist");
            sender.sendMessage(commandColor + "/lock unlock" + arrow + color + " Entsperrt die anvisierte Truhe");
            return true;
        }

        Block targetBlock = player.getTargetBlockExact(5);

        if (targetBlock == null || (targetBlock.getType() != Material.CHEST && !targetBlock.getType().name().endsWith("SHULKER_BOX"))) {
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du musst eine normale Kiste oder eine Shulker-Kiste anvisieren, um diesen Befehl auszuführen.");
            return true;
        }
        Location targetLocation = targetBlock.getLocation();
        // Lade Chest-Daten für den Spieler
        ChestData data = plugin.getChestData();



        // Überprüfen, ob der Spieler ein Ziel hat (z.B. eine Kiste)
        if (args.length == 0 || args[0].equalsIgnoreCase("unlock") || args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
            targetBlock = player.getTargetBlockExact(5);
            if (targetBlock == null || (targetBlock.getType() != Material.CHEST && !targetBlock.getType().name().endsWith("SHULKER_BOX"))) {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du musst eine normale Kiste oder eine Shulker-Kiste anvisieren, um diesen Befehl auszuführen.");
                return true;
            }


            targetLocation = targetBlock.getLocation();
        }

        if (args.length == 0) {
            // Hole den Block, auf den der Spieler schaut (maximale Entfernung: 5 Blöcke)

            // Überprüfen, ob der Spieler auf eine Kiste schaut
            if (targetBlock == null || (targetBlock.getType() != Material.CHEST && !targetBlock.getType().name().endsWith("SHULKER_BOX"))) {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du musst eine normale Kiste oder eine Shulker-Kiste anvisieren, um diesen Befehl auszuführen.");
                return true;
            }

            // Überprüfe, ob die Kiste gesperrt werden kann
            boolean canLock = !data.isChestLocked(targetLocation) ||
                    data.isPlayerInWhitelist(targetLocation, player.getName()) ||
                    player.hasPermission(adminPermission);

            if (canLock) {
                if (data.isChestLocked(targetLocation)) {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Diese Kiste ist bereits gesperrt!");
                    return true;
                }

                // Hole die benachbarten Kisten
                Set<Location> connectedChests = getConnectedChests(targetBlock); // Wir verwenden den bereits definierten targetBlock.

                // Sperre die Kiste und alle benachbarten Kisten
                data.addLockedChest(targetLocation, player.getName());
                for (Location adjacentLocation : connectedChests) {
                    data.lockAdjacentChests(adjacentLocation, player.getName());
                }

                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Kiste gesperrt und du bist automatisch auf die Whitelist gesetzt!");
            } else {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du kannst diese Kiste nicht sperren, da du nicht der Besitzer bist.");
            }

            return true;
        }








        //Location targetLocation = targetBlock.getLocation();
        // Standard-Kommandos wie /lock add, remove, unlock behalten
        if (args.length == 1) {

            // Befehl "unlock"
            if (args[0].equalsIgnoreCase("unlock")) {
                // Überprüfe, ob der Spieler die Permission hat oder Besitzer ist
                if (player.hasPermission(adminPermission) || data.isOwner(player.getName(), targetLocation)) {
                    data.removeLockedChest(targetLocation);
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Kiste entsperrt!");
                } else {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du kannst diese Kiste nicht entsperren, da du nicht der Besitzer bist.");
                }
                return true;
            }else {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /lock help um eine Hilfe zu erhalten!");
                return true;
            }
        }
        //Block targetBlock = player.getTargetBlockExact(5);
        if (args.length == 2) {
            Player target = Bukkit.getPlayer(args[1]);



            if (target == null)
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Spieler " + ChatColor.BOLD + args[1] + ChatColor.RED +  " ist nicht online!");

            if (args[0].equalsIgnoreCase("add")) {
                // Überprüfe, ob der Spieler die Permission hat oder Besitzer ist

                if (player.hasPermission(adminPermission) || data.isPlayerInWhitelist(targetLocation, player.getName())) {
                    data.addPlayerToWhitelist(targetLocation, args[1]);
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN  + "Spieler " + ChatColor.BOLD + args[1] + ChatColor.GREEN + " zur Whitelist hinzugefügt!");
                } else {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du kannst diese Kiste nicht bearbeiten, da du nicht der Besitzer bist.");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                // Überprüfe, ob der Spieler die Permission hat oder Besitzer ist
                if (player.hasPermission(adminPermission) || data.isPlayerInWhitelist(targetLocation, player.getName())) {
                    data.removePlayerFromWhitelist(targetLocation, args[1]);
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Spieler " + ChatColor.BOLD + args[1] + ChatColor.RED + " von der Whitelist entfernt!");
                } else {
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du kannst diese Kiste nicht bearbeiten, da du nicht der Besitzer bist.");
                }
                return true;
            }
             else {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /lock help um eine Hilfe zu erhalten!");
                return true;

            }
        } else{
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /lock help um eine Hilfe zu erhalten!");
            return true;
        }
    }
}
