package at.lowdfx.lowdfx.commands.basicCommands;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrashCommand implements CommandExecutor {

    public static final String adminPermission = "lowdfx.trash";

    private lowdfx plugin;

    public TrashCommand(lowdfx plugin) {
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
                        trash(sender, player);
                        return;
                    }

                    if (args.length == 1) {
                        Player target = Bukkit.getPlayer(args[0]);

                        if (args[0].equalsIgnoreCase("help")) {
                            sendHelp(sender);
                            return;
                        }

                    }

                }

                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /trash help um eine Hilfe zu erhalten!");

            }
        });
        return true;
    }

    private void trash(CommandSender sender, Player player) {

        player.openInventory(plugin.getServer().createInventory(player, 36, "Mülleimer"));
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Der Mülleimer öffnet sich!");


    }



    private void sendHelp(CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE.toString() + " → ";

        if (sender.hasPermission(adminPermission)) {
            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Trash -------");
            sender.sendMessage(commandColor + "/trash" + arrow + color + " Mit diesem Befehl kannst du einen Mülleimer öffnen, Items verschwinden unwiderruflich!");
        }
    }
}
