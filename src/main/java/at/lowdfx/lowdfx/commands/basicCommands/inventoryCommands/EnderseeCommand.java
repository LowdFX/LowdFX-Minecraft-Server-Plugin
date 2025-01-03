package at.lowdfx.lowdfx.commands.basicCommands.inventoryCommands;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;


public class EnderseeCommand implements CommandExecutor, Listener {

    public static final String adminPermission = "lowdfx.endersee";

    private lowdfx plugin;

    public EnderseeCommand(lowdfx plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Fehler! Das kann nur ein Spieler tun!");
                    return;
                }
                if (sender.hasPermission(adminPermission)) {
                    if (args.length == 0) {
                        endersee(sender, player);
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
                            enderseetarget(sender, target, args);
                            return;
                        }

                    }

                }

                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /endersee help um eine Hilfe zu erhalten!");

            }
        });
        return true;
    }


    private void endersee(CommandSender sender, Player player) {
        player.openInventory(((Player) sender).getEnderChest());
        sender.sendMessage(ChatColor.GREEN + "Deine Enderchest wurde geöffnet!");
    }

    private void enderseetarget(CommandSender sender, Player player, String[] args) {
        // Überprüfen, ob ein Zielspieler angegeben wurde
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Du musst den Namen eines Spielers angeben!");
            return;
        }

        // Hole den Zielspieler aus den Argumenten
        Player target = Bukkit.getPlayer(args[0]);

        // Überprüfen, ob der Zielspieler online ist
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Der angegebene Spieler ist nicht online!");
            return;
        }

        // Überprüfen, ob der Sender versucht, das eigene Inventar zu sehen
        if (sender == target) {
            Player open_inv = Bukkit.getPlayerExact(args[0]);
            player.openInventory(open_inv.getEnderChest());
            return;
        }

        // Der Sender wird das Inventar sehen, nicht der Spieler (Player), der den Befehl ausführt
        Player p = (Player) sender;  // Der Sender wird hier als der Öffner des Inventars gesetzt
        Player open_inv = Bukkit.getPlayerExact(args[0]);

        // Öffne das Inventar für den Sender
        p.openInventory(open_inv.getEnderChest());

        // Sende eine Nachricht an den Sender, dass das Inventar geöffnet wurde
        sender.sendMessage(ChatColor.GREEN + "Du hast die Enderchest von " + target.getName() + " geöffnet!");
    }






    private void sendHelp(CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE.toString() + " → ";

        if (sender.hasPermission(adminPermission)) {
            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Endersee -------");
            sender.sendMessage(commandColor + "/endersee" + arrow + color + " Mit diesem Befehl kannst du deine Enderchest anzeigen");
            sender.sendMessage(commandColor + "/endersee <Spieler>" + arrow + color + " Mit diesem Befehl kannst du die Enderchest eines angegeben Spielers anzeigen");
        }
    }
}
