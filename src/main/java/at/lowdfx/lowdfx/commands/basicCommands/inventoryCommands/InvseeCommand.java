package at.lowdfx.lowdfx.commands.basicCommands.inventoryCommands;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class InvseeCommand implements CommandExecutor, Listener {

    public static final String adminPermission = "lowdfx.invsee";

    private lowdfx plugin;

    public InvseeCommand(lowdfx plugin) {
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
                            invsee(sender, target, args);
                            return;
                        }

                    }

                }

                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /invsee help um eine Hilfe zu erhalten!");

            }
        });
        return true;
    }

    private void invsee(CommandSender sender, Player player, String[] args) {
        // Überprüfen, ob ein Zielspieler angegeben wurde
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du musst den Namen eines Spielers angeben!");
            return;
        }

        // Hole den Zielspieler aus den Argumenten
        Player target = Bukkit.getPlayer(args[0]);

        // Überprüfen, ob der Zielspieler online ist
        if (target == null) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Der angegebene Spieler ist nicht online!");
            return;
        }

        // Überprüfen, ob der Sender versucht, das eigene Inventar zu sehen
        if (sender == target) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Du kannst nicht dein eigenes Inventar auslesen!");
            return;
        }

        // Der Sender wird das Inventar sehen, nicht der Spieler (Player), der den Befehl ausführt
        Player p = (Player) sender;  // Der Sender wird hier als der Öffner des Inventars gesetzt
        Player open_inv = Bukkit.getPlayerExact(args[0]);

        // Öffne das Inventar für den Sender
        p.openInventory(open_inv.getInventory());

        // Sende eine Nachricht an den Sender, dass das Inventar geöffnet wurde
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du hast das Inventar von " + ChatColor.BOLD + target.getName() + ChatColor.GREEN + " geöffnet!");
    }






    private void sendHelp(CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE.toString() + " → ";

        if (sender.hasPermission(adminPermission)) {
            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Invsee -------");
            sender.sendMessage(commandColor + "/invsee <Spieler>" + arrow + color + " Mit diesem Befehl kannst du das Inventar eines angegebenen Spielers anzeigen");
        }
    }
}
