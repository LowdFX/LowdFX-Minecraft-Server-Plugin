package at.lowdfx.lowdfx.commands.basicCommands;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class FeedCommand implements CommandExecutor {

    public static final String adminPermission = "lowdfx.feed";

    private lowdfx plugin;

    public FeedCommand(lowdfx plugin) {
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
                        feed(sender, player);
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
                            feedtarget(sender, target, args);
                            return;
                        }

                    }

                }

                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /feed help um eine Hilfe zu erhalten!");

            }
        });
        return true;
    }

    private void feed(CommandSender sender, Player player) {
        player.setFoodLevel(20);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Dein Hunger wurde gestillt");
        for (final PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }



    //-----------------------------------------------------------------------------------------

    //TARGET

    private void feedtarget(CommandSender sender, Player player, String[] args) {
        Player target = Bukkit.getPlayer(args[0]);

        target.setFoodLevel(20);
        for (final PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }

        target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Dein Hunger wurde gestillt!");
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN +  ChatColor.BOLD + args[0] + ChatColor.GREEN +"´s Hunger wurde gestillt!");
    }


    private void sendHelp(CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE.toString() + " → ";

        if (sender.hasPermission(adminPermission)) {
            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Feed -------");
            sender.sendMessage(commandColor + "/feed" + arrow + color + " Mit diesem Befehl kannst den Hunger stillen");
            sender.sendMessage(commandColor + "/feed <player>" + arrow + color + " Mit diesem Befehl kannst du den Hunger von einem angegebenen Spieler stillen");
        }
    }
}
