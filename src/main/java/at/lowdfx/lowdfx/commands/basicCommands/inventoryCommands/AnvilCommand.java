package at.lowdfx.lowdfx.commands.basicCommands.inventoryCommands;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnvilCommand implements CommandExecutor {

    public static final String adminPermission = "lowdfx.anvil";

    private lowdfx plugin;

    public AnvilCommand(lowdfx plugin) {
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
                        anvil(sender, player);
                        return;
                    }

                    if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("help")) {
                            sendHelp(sender);
                            return;
                        }

                    }

                }

                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /anvil help um eine Hilfe zu erhalten!");

            }
        });
        return true;
    }

    private void anvil(CommandSender sender, Player player) {
        //AnvilInventory anvil = (AnvilInventory) Bukkit.createInventory(null, Material.ANVIL);

        player.openAnvil(player.getLocation(), true);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Der Amboss öffnet sich!");


    }



    private void sendHelp(CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE.toString() + " → ";

        if (sender.hasPermission(adminPermission)) {
            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Anvil -------");
            sender.sendMessage(commandColor + "/anvil" + arrow + color + " Mit diesem Befehl kannst du einen Amboss öffnen");
        }
    }
}
