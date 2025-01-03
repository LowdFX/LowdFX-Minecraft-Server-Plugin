package at.lowdfx.lowdfx.commands.basicCommands.vanishCommand;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor {

    public static final String adminPermission = "lowdfx.vanish";
    private lowdfx plugin;
    private final InvisiblePlayerHandler invisibleHandler;


    public VanishCommand(lowdfx plugin, InvisiblePlayerHandler invisibleHandler) {
        this.plugin = plugin;
        this.invisibleHandler = invisibleHandler;
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
                            vanish(sender, player);
                            return;
                        }

                        if (args.length == 1) {
                            Player player = (Player) sender;

                            if (args[0].equalsIgnoreCase("help")) {
                                sendHelp(sender);
                                return;
                            }
                            if (args[0].equalsIgnoreCase("list")) {
                                vanishlist(sender);
                                return;
                            }
                            if (args[0].equalsIgnoreCase("join")) {
                                vanishjoin(sender, player);
                                return;
                            }
                            if (args[0].equalsIgnoreCase("quit")) {
                                vanishquit(sender, player);
                                return;
                            }
                            Player target = Bukkit.getPlayer(args[0]);
                            if (target == null) {
                                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Spieler nicht gefunden!");
                                return;
                            }

                            if (args[0].equalsIgnoreCase(target.getName())) {
                                vanishtarget(sender, target, args);
                                return;
                            }


                        }

                    }

                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /vanish help um eine Hilfe zu erhalten!");

                }
            });
            return true;
    }

    private void vanish(CommandSender sender, Player player) {
        if (!player.hasMetadata("vanished")) {
            // Flugmodus aktivieren, falls nicht erlaubt
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }

            // Spieler unsichtbar machen
            invisibleHandler.makePlayerInvisible(player);
            player.setMetadata("vanished", new FixedMetadataValue(plugin, true));
            player.setSleepingIgnored(true);
            player.setCollidable(false);
            player.setCanPickupItems(false);
            player.setSilent(true);


            // Benachrichtigungen
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                    + lowdfx.config.getString("basic.servername")
                    + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du wurdest vanished!");

            // Benachrichtigung an alle Admins
            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission(adminPermission) && !admin.equals(player)) {
                    admin.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                            + lowdfx.config.getString("basic.servername")
                            + ChatColor.GRAY + " >> " + ChatColor.GREEN + ChatColor.BOLD + player.getName()
                            + ChatColor.GREEN + " ist nun unsichtbar.");
                } else{
                    admin.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> "
                            + ChatColor.YELLOW + "" + lowdfx.config.getString("join.quit") + " " + ChatColor.GOLD + ChatColor.BOLD + player.getPlayer().getName()
                            + ChatColor.YELLOW + " !");
                }
            }

        } else {
            // Flugmodus deaktivieren
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }

            // Spieler sichtbar machen
            invisibleHandler.makePlayerVisible(player);
            player.removeMetadata("vanished", plugin);
            player.setSleepingIgnored(false);
            player.setCollidable(true);
            player.setCanPickupItems(true);
            player.setSilent(false);

            // Benachrichtigungen
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                    + lowdfx.config.getString("basic.servername")
                    + ChatColor.GRAY + " >> " + ChatColor.RED + "Du bist nun nicht mehr vanished!");

            // Benachrichtigung an alle Admins
            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission(adminPermission) && !admin.equals(player)) {
                    admin.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                            + lowdfx.config.getString("basic.servername")
                            + ChatColor.GRAY + " >> " + ChatColor.GREEN + ChatColor.BOLD + player.getName()
                            + ChatColor.RED + " ist nun sichtbar.");
                }else {
                    admin.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> "
                            + ChatColor.YELLOW + "" + lowdfx.config.getString("join.welcome") + " " + ChatColor.GOLD + ChatColor.BOLD + player.getPlayer().getName()
                            + ChatColor.YELLOW + " !");
                }
            }
        }
    }




    //-----------------------------------------------------------------------------------------

    //TARGET

    private void vanishtarget(CommandSender sender, Player target, String[] args) {
        if (target == null) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                    + lowdfx.config.getString("basic.servername")
                    + ChatColor.GRAY + " >> " + ChatColor.RED + "Spieler nicht gefunden!");
            return;
        }

        if (!target.hasMetadata("vanished")) {
            // Aktiviere Flugmodus, falls noch nicht erlaubt
            if (!target.getAllowFlight()) {
                target.setAllowFlight(true);
                target.setFlying(true);
            }

            // Spieler unsichtbar machen
            invisibleHandler.makePlayerInvisible(target);
            target.setMetadata("vanished", new FixedMetadataValue(plugin, true));
            target.setSleepingIgnored(true);
            target.setCollidable(false);
            target.setCanPickupItems(false);
            target.setSilent(true);

            // Benachrichtigungen
            target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                    + lowdfx.config.getString("basic.servername")
                    + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du wurdest vanished!");

            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                    + lowdfx.config.getString("basic.servername")
                    + ChatColor.GRAY + " >> " + ChatColor.GREEN + ChatColor.BOLD
                    + target.getName() + ChatColor.GREEN + " wurde vanished!");

            // Benachrichtigung an alle mit Admin-Berechtigung
            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission(adminPermission) && !admin.equals(target)) {
                    admin.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                            + lowdfx.config.getString("basic.servername")
                            + ChatColor.GRAY + " >> " + ChatColor.GREEN + ChatColor.BOLD + target.getName()
                            + ChatColor.GREEN + " ist nun unsichtbar.");
                }else {
                    admin.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> "
                            + ChatColor.YELLOW + "" + lowdfx.config.getString("join.quit") + " " + ChatColor.GOLD + ChatColor.BOLD + target.getPlayer().getName()
                            + ChatColor.YELLOW + " !");
                }
            }

        } else {
            // Flugmodus deaktivieren
            if (target.getAllowFlight()) {
                target.setAllowFlight(false);
                target.setFlying(false);
            }

            // Spieler sichtbar machen
            invisibleHandler.makePlayerVisible(target);
            target.removeMetadata("vanished", plugin);
            target.setSleepingIgnored(false);
            target.setCollidable(true);
            target.setCanPickupItems(true);
            target.setSilent(false);

            // Benachrichtigungen
            target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                    + lowdfx.config.getString("basic.servername")
                    + ChatColor.GRAY + " >> " + ChatColor.RED + "Du bist nun nicht mehr vanished!");

            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                    + lowdfx.config.getString("basic.servername")
                    + ChatColor.GRAY + " >> " + ChatColor.GREEN + ChatColor.BOLD
                    + target.getName() + ChatColor.RED + " wurde vanish entzogen!");

            // Benachrichtigung an alle mit Admin-Berechtigung
            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission(adminPermission) && !admin.equals(target)) {
                    admin.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                            + lowdfx.config.getString("basic.servername")
                            + ChatColor.GRAY + " >> " + ChatColor.GREEN + ChatColor.BOLD + target.getName()
                            + ChatColor.RED + " ist nun sichtbar.");
                }else {
                    admin.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> "
                            + ChatColor.YELLOW + "" + lowdfx.config.getString("join.welcome") + " " + ChatColor.GOLD + ChatColor.BOLD + target.getPlayer().getName()
                            + ChatColor.YELLOW + " !");
                }
            }
        }
    }

    private void vanishlist(CommandSender sender) {
        Set<UUID> vanishedPlayers = invisibleHandler.getVanishedPlayers();
        if (vanishedPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername")
                    + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Es gibt keine vanished Spieler.");
        } else {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername")
                    + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Vanished Spieler:");
            StringBuilder listMessage = new StringBuilder(ChatColor.GREEN + "");

            for (UUID uuid : vanishedPlayers) {
                Player vanishedPlayer = Bukkit.getPlayer(uuid);
                if (vanishedPlayer != null) {
                    listMessage.append(ChatColor.GREEN).append(vanishedPlayer.getName()).append("\n");
                }
            }

            sender.sendMessage(listMessage.toString());
        }

    }


    private void vanishjoin(CommandSender sender, Player target) {
        // Sende eine Nachricht an alle Online-Spieler (außer target)
        for (Player admin : Bukkit.getOnlinePlayers()) {
                // Nachricht senden
                admin.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername")
                        + ChatColor.GRAY + " >> " + ChatColor.YELLOW + lowdfx.config.getString("join.welcome")
                        + " " + ChatColor.GOLD + ChatColor.BOLD + target.getName()
                        + ChatColor.YELLOW + "!");

        }
    }

    private void vanishquit(CommandSender sender, Player target) {
        // Sende eine Nachricht an alle Online-Spieler (außer target)
        for (Player admin : Bukkit.getOnlinePlayers()) {
                // Nachricht senden
                admin.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername")
                        + ChatColor.GRAY + " >> " + ChatColor.YELLOW + lowdfx.config.getString("join.quit")
                        + " " + ChatColor.GOLD + ChatColor.BOLD + target.getName()
                        + ChatColor.YELLOW + "!");

        }
    }


    private void sendHelp(CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE.toString() + " → ";

        if (sender.hasPermission(adminPermission)) {
            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Vanish -------");
            sender.sendMessage(commandColor + "/vanish" + arrow + color + " Mit diesem Befehl machst du dich unsichtbar");
            sender.sendMessage(commandColor + "/vanish <player>" + arrow + color + " Mit diesem Befehl kannst du einen angegebenen Spieler unsichtbar machen");
            sender.sendMessage(commandColor + "/vanish list" + arrow + color + " Mit diesem Befehl kannst du alle vanished Spieler ansehen");
            sender.sendMessage(commandColor + "/vanish join" + arrow + color + " Mit diesem Befehl kannst du eine Server Join Nachricht senden");
            sender.sendMessage(commandColor + "/vanish quit" + arrow + color + " Mit diesem Befehl kannst du eine Server Quit Nachricht senden");
        }
    }


}
