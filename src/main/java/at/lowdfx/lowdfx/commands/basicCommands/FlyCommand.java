package at.lowdfx.lowdfx.commands.basicCommands;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {

    public static final String adminPermission = "lowdfx.fly";

    private lowdfx plugin;

    public FlyCommand(lowdfx plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {

                if (sender.hasPermission(adminPermission)) {
                    if (args.length == 0) {
                        if (!(sender instanceof Player player)) {
                            sender.sendMessage(ChatColor.RED + "Fehler! Das kann nur ein Spieler tun!");
                            return;
                        }
                        fly(sender, player);
                        return;
                    }

                    if (args.length == 1) {
                        Player target = Bukkit.getPlayer(args[0]);

                        if (args[0].equalsIgnoreCase("help")) {
                            sendHelp(sender);
                            return;
                        }
                        if (target == null) {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Spieler nicht gefunden!");
                            return;
                        }

                        if (args[0].equalsIgnoreCase(target.getName())) {
                            flytarget(sender, target, args);
                            return;
                        }

                    }

                }

                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /fly help um eine Hilfe zu erhalten!");

            }
        });
        return true;
    }

    private void fly(CommandSender sender, Player player) {
        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du kannst nun fliegen!");

        }else {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du kannst nun nicht mehr fliegen!");
        }
       }

    //TARGET

    private void flytarget(CommandSender sender, Player player, String[] args) {
        Player target = Bukkit.getPlayer(args[0]);
        if (!target.getAllowFlight()) {
            target.setAllowFlight(true);
            target.setFlying(true);
            target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du kannst nun fliegen!");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN +  ChatColor.BOLD + args[0] + ChatColor.GREEN +" kann nun fliegen!");
        }else {
            target.setAllowFlight(false);
            target.setFlying(false);
            target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du kannst nun nicht mehr fliegen!");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN +  ChatColor.BOLD + args[0] + ChatColor.GREEN +" kann nun nicht mehr fliegen!");
        }
    }


    private void sendHelp(CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE.toString() + " → ";

        if (sender.hasPermission(adminPermission)) {
            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Fly -------");
            sender.sendMessage(commandColor + "/fly" + arrow + color + " Mit diesem Befehl kannst du fliegen");
            sender.sendMessage(commandColor + "/fly <player>" + arrow + color + " Mit diesem Befehl kannst du einem angegebenem Spieler in Fly Mode setzen");
        }
    }
}
