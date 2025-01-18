package at.lowdfx.lowdfx.commands.teleport;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.commands.teleport.managers.HomeManager;
import at.lowdfx.lowdfx.commands.teleport.teleportPoints.HomePoint;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HomeCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.home.admin";
    public static final String PLAYER_PERMISSION = "lowdfx.home";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(Lowdfx.PLUGIN, () -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Das kann nur ein Spieler tun!");
                return;
            }

            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("help")) {
                        help(sender);
                        return;
                    }
                }
            }
            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("set")) {
                        set(sender, args);
                        return;
                    }
                }
            }
            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 0) {
                        teleport(sender, args);
                        return;
                }
            }
            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("remove")) {
                        remove(sender, args);
                        return;
                    }
                }
            }
            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("tp")) {
                        teleportCustom(sender, args);
                        return;
                    }
                }
            }
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                if (args.length == 3) {
                    if (args[0].equalsIgnoreCase("tp_other")) {
                        teleportCustomAdmin(sender, args);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("set_other")) {
                        setAdmin(sender, args);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("remove_other")) {
                        removeAdmin(sender, args);
                        return;
                    }
                }
            }

            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutzte /home help für eine Hilfestellung!");
        });
        return true;
    }

    public boolean teleportCustomAdmin(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION))
            return false;
        if (args.length != 3)
            return false;
        if (!args[0].equalsIgnoreCase("tp_other"))
            return false;

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Der eingegebene Spieler " + ChatColor.BOLD + args[1] + ChatColor.RED + " konnten nicht gefunden werden!");
            return true;
        }

        HomePoint homePoint = HomeManager.get(target);
        if (homePoint.doesNotExist(args[2])) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Das Home " + ChatColor.BOLD + args[2] + ChatColor.RED + " von " + ChatColor.BOLD + args[1] + ChatColor.RED + " existiert nicht!");
            return true;
        }

        homePoint.get(args[2]).teleport((Player) sender, Lowdfx.PLUGIN);
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du wurdest zum Home " + ChatColor.BOLD + args[2] + ChatColor.GREEN + " von " + ChatColor.BOLD + args[1] + ChatColor.GREEN + " teleportiert!");
        return true;
    }

    public boolean teleportCustom(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION) || !sender.hasPermission(PLAYER_PERMISSION))
            return false;
        if (args.length != 2)
            return false;
        if (!args[0].equalsIgnoreCase("tp"))
            return false;

        Player player = (Player) sender;
        HomePoint homePoint = HomeManager.get(player);
        if (homePoint.doesNotExist(args[1])) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Dein Home " + ChatColor.BOLD + args[1] + ChatColor.RED + " wurde noch nicht gesetzt!");
            return true;
        }

        homePoint.get(args[1]).teleport(player, Lowdfx.PLUGIN);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du wurdest nach Hause " + ChatColor.BOLD + args[1] + ChatColor.GREEN + " teleportiert!");
        return true;
    }

    public boolean teleport(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        HomePoint homePoint = HomeManager.get(player);
        if (homePoint.doesNotExist("home")) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Dein Home wurde noch nicht gesetzt!");
            return true;
        }

        homePoint.get("home").teleport(player, Lowdfx.PLUGIN);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du wurdest nach Hause teleportiert!");
        return true;
    }

    // Administrator: set another player's home
    public boolean setAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION))
            return false;
        if (args.length != 3)
            return false;
        if (!args[0].equalsIgnoreCase("set_other"))
            return false;

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Der Spieler " + ChatColor.BOLD + args[1] + ChatColor.RED + " existiert nicht!");
            return true;
        }

        HomePoint homePoint = HomeManager.get(target);
        homePoint.set(args[2], ((Player) sender).getLocation());
        homePoint.save();
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Das Home " + ChatColor.BOLD + args[2] + ChatColor.GREEN + " von " + ChatColor.BOLD + args[1] + ChatColor.GREEN + " wurde gesetzt!");
        return true;
    }

    // Set your own home
    public boolean set(CommandSender sender, String @NotNull [] args) {

        Player player = (Player) sender;
        String homeName = (args.length == 1) ? "home" : args[1];

        if (HomeManager.get(player).getHomes().size() >= Lowdfx.CONFIG.getInt("basic.maxhomes") && !player.hasPermission(ADMIN_PERMISSION)) {
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Die maximale Homegrenze von " + ChatColor.BOLD + Lowdfx.CONFIG.getInt("basic.maxhomes") + ChatColor.RED + " wurde erreicht!");
        return true;
        }

        HomePoint homePoint = HomeManager.get(player);
        homePoint.set(homeName, player.getLocation());
        homePoint.save();
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Dein Home " + ChatColor.BOLD + homeName + ChatColor.GREEN + " wurde gesetzt!");
        return true;

        }

    // Administrator: Remove another player's home
    public boolean removeAdmin(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION))
            return false;
        if (args.length != 3)
            return false;
        if (!args[0].equalsIgnoreCase("remove_other"))
            return false;

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Der Spieler " + ChatColor.BOLD + args[1] + ChatColor.RED + " existiert nicht!");
            return true;
        }

        HomePoint homePoint = HomeManager.get(target);
        if (homePoint.doesNotExist(args[2])) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Das Home " + ChatColor.BOLD + args[2] + ChatColor.RED + " existiert nicht!");
            return true;
        }

        homePoint.remove(args[2]);
        homePoint.save();
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Das Home " + ChatColor.BOLD + args[2] + ChatColor.GREEN + " von " + ChatColor.BOLD + args[1] + ChatColor.GREEN + " wurde entfernt!");
        return true;
    }

    // Remove your own home
    public boolean remove(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        HomePoint homePoint = HomeManager.get(player);

        if (homePoint.doesNotExist(args[1])) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Das Home " + args[1] + ChatColor.RED + " existiert nicht!");
            return true;
        }

        homePoint.remove(args[1]);
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Das Home " + ChatColor.BOLD + args[1] + ChatColor.GREEN + " wurde entfernt!");
        return true;
    }

    // Help command
    private void help(@NotNull CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE + " → ";


        if (sender.hasPermission(PLAYER_PERMISSION) || sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Home -------");
            sender.sendMessage(color + arrow + "/home" + commandColor + " Teleportiere zu deinem Haupt-Home!");
            sender.sendMessage(color + arrow + "/home set <name>" + commandColor + " Setze dein Home an deinem aktuellen Standort!");
            sender.sendMessage(color + arrow + "/home remove <name>" + commandColor + " Entferne dein Home!");
            sender.sendMessage(color + arrow + "/home tp <name>" + commandColor + " Teleportiere dich zu deinem angegebenem Home!");
        }
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(color + arrow + "/home tp_other <player> <home_name>" + commandColor + " Teleportiere dich zum Home eines anderen Spielers!");
            sender.sendMessage(color + arrow + "/home set_other <player> <home_name>" + commandColor + " Setze das Home eines anderen Spielers!");
            sender.sendMessage(color + arrow + "/home remove_other <player> <home_name>" + commandColor + " Entferne das Home eines anderen Spielers!");
        }

    }
}
