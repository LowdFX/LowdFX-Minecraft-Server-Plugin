package at.lowdfx.lowdfx.commands.basicCommands;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamemodeCommand implements CommandExecutor {

    public static final String adminPermission = "lowdfx.gm";

    private lowdfx plugin;

    public GamemodeCommand(lowdfx plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
        /*        if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Fehler! Das kann nur ein Spieler tun!");
                    return;
                } */
                if (sender.hasPermission(adminPermission)) {
                    if (args.length == 1) {
                        if (!(sender instanceof Player player)) {
                            sender.sendMessage(ChatColor.RED + "Fehler! Das kann nur ein Spieler tun!");
                            return;
                        }
                        if (args[0].equalsIgnoreCase("help")) {
                            sendHelp(sender);
                            return;
                        }
                        if (args[0].equalsIgnoreCase("0") || args[0].equalsIgnoreCase("survival")) {
                            survival(sender, ((Player) sender).getPlayer());
                            return;
                        }
                        if (args[0].equalsIgnoreCase("1") || args[0].equalsIgnoreCase("creative")) {
                            creative(sender, ((Player) sender).getPlayer());
                            return;
                        }
                        if (args[0].equalsIgnoreCase("2") || args[0].equalsIgnoreCase("adventure")) {
                            adventure(sender, ((Player) sender).getPlayer());
                            return;
                        }
                        if (args[0].equalsIgnoreCase("3") || args[0].equalsIgnoreCase("spectator")) {
                            spectator(sender, ((Player) sender).getPlayer());
                            return;
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Der eingegebene Gamemode " + ChatColor.BOLD + args[0] + ChatColor.RED + " existiert nicht!");
                            return;
                        }

                    }

                    if (args.length == 2) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target == null)
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Spieler nicht gefunden!");

                        if (args[0].equalsIgnoreCase("0") || args[0].equalsIgnoreCase("survival")) {
                            survivaltarget(sender, target, args);
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + ChatColor.BOLD + args[1] + ChatColor.GREEN + " ist nun im" + ChatColor.BOLD + " SURVIVAL " + ChatColor.GREEN + "Modus!");
                            return;
                        }
                        if (args[0].equalsIgnoreCase("1") || args[0].equalsIgnoreCase("creative")) {
                            creativetarget(sender, target, args);
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + ChatColor.BOLD + args[1] + ChatColor.GREEN + " ist nun im" + ChatColor.BOLD + " CREATIVE " + ChatColor.GREEN + "Modus!");
                            return;
                        }
                        if (args[0].equalsIgnoreCase("2") || args[0].equalsIgnoreCase("adventure")) {
                            adventuretarget(sender, target, args);
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + ChatColor.BOLD + args[1] + ChatColor.GREEN + " ist nun im" + ChatColor.BOLD + " ADVENTURE " + ChatColor.GREEN + "Modus!");
                            return;
                        }
                        if (args[0].equalsIgnoreCase("3") || args[0].equalsIgnoreCase("spectator")) {
                            spectatortarget(sender, target, args);
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + ChatColor.BOLD + args[1] + ChatColor.GREEN + " ist nun im" + ChatColor.BOLD + " SPECTATOR " + ChatColor.GREEN + "Modus!");
                            return;
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Der eingegebene Gamemode " + ChatColor.BOLD + args[0] + ChatColor.RED + " existiert nicht!");
                            return;

                        }
                    }
                }

                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /gm help um eine Hilfe zu erhalten!");

            }
        });
        return true;
    }

    private void survival(CommandSender sender, Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du bist nun im" + ChatColor.BOLD + " SURVIVAL " + ChatColor.GREEN + "Modus!");
    }

    private void creative(CommandSender sender, Player player) {
        player.setGameMode(GameMode.CREATIVE);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du bist nun im" + ChatColor.BOLD + " CREATIVE " + ChatColor.GREEN + "Modus!");
    }

    private void adventure(CommandSender sender, Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du bist nun im" + ChatColor.BOLD + " ADVENTURE " + ChatColor.GREEN + "Modus!");
    }

    private void spectator(CommandSender sender, Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du bist nun im" + ChatColor.BOLD + " SPECTATOR " + ChatColor.GREEN + "Modus!");
    }
    //TARGET

    private void survivaltarget(CommandSender sender, Player player, String[] args) {
        Player target = Bukkit.getPlayer(args[1]);
        target.setGameMode(GameMode.SURVIVAL);
        target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du bist nun im" + ChatColor.BOLD + " SURVIVAL " + ChatColor.GREEN + "Modus!");
        }

    private void creativetarget(CommandSender sender, Player player, String[] args) {
        Player target = Bukkit.getPlayer(args[1]);
        target.setGameMode(GameMode.CREATIVE);
        target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du bist nun im" + ChatColor.BOLD + " CREATIVE " + ChatColor.GREEN + "Modus!");
        }

    private void adventuretarget(CommandSender sender, Player player, String[] args) {
        Player target = Bukkit.getPlayer(args[1]);
        target.setGameMode(GameMode.ADVENTURE);
        target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du bist nun im" + ChatColor.BOLD + " ADVENTURE " + ChatColor.GREEN + "Modus!");
        }

    private void spectatortarget(CommandSender sender, Player player, String[] args) {
        Player target = Bukkit.getPlayer(args[1]);
        target.setGameMode(GameMode.SPECTATOR);
        target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du bist nun im" + ChatColor.BOLD + " SPECTATOR " + ChatColor.GREEN + "Modus!");
        }

    private void sendHelp(CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE.toString() + " → ";

        if (sender.hasPermission(adminPermission)) {
            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Gamemodes -------");
            sender.sendMessage(commandColor + "/gm 0 | survival" + arrow + color + " Setzt deinen Gamemode auf SURVIVAL");
            sender.sendMessage(commandColor + "/gm 1 | creative" + arrow + color + " Setzt deinen Gamemode auf CREATIVE");
            sender.sendMessage(commandColor + "/gm 2 | adventure" + arrow + color + " Setzt deinen Gamemode auf ADVENTURE");
            sender.sendMessage(commandColor + "/gm 3 | spectator" + arrow + color + " Setzt deinen Gamemode auf SPECTATOR");
            sender.sendMessage(commandColor + "/gm <mode> <player>" + arrow + color + " Setzt den angegebenen Gamemode für einen angegebenen Spieler");
        }
    }
}
