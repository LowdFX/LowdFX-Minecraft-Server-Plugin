package at.lowdfx.lowdfx.commands.basicCommands;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;

public class HealCommand implements CommandExecutor {

    public static final String adminPermission = "lowdfx.heal";

    private lowdfx plugin;

    public HealCommand(lowdfx plugin) {
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
                        heal(sender, player);
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
                            healtarget(sender, target, args);
                            return;
                        }

                    }

                }

                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /heal help um eine Hilfe zu erhalten!");

            }
        });
        return true;
    }

    private void heal(CommandSender sender, Player player) {
        final double amount = player.getMaxHealth() - player.getHealth();
        final EntityRegainHealthEvent erhe = new EntityRegainHealthEvent(player, amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
        plugin.getServer().getPluginManager().callEvent(erhe);
        double newAmount = player.getHealth() + erhe.getAmount();
        if (newAmount > player.getMaxHealth()) {
            newAmount = player.getMaxHealth();
        }
        player.setHealth(newAmount);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du wurdest geheilt!");

        for (final PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }



    //-----------------------------------------------------------------------------------------

    //TARGET

    private void healtarget(CommandSender sender, Player player, String[] args) {
        Player target = Bukkit.getPlayer(args[0]);
        final double amount = target.getMaxHealth() - target.getHealth();
        final EntityRegainHealthEvent erhe = new EntityRegainHealthEvent(target, amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
        plugin.getServer().getPluginManager().callEvent(erhe);
        double newAmount = target.getHealth() + erhe.getAmount();
        if (newAmount > target.getMaxHealth()) {
            newAmount = target.getMaxHealth();
        }
        target.setHealth(newAmount);
        target.setFoodLevel(20);
        target.setFireTicks(0);

        for (final PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }

            target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du wurdest geheilt!");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.GREEN +  ChatColor.BOLD + args[0] + ChatColor.GREEN +" wurde geheilt!");
    }


    private void sendHelp(CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE.toString() + " → ";

        if (sender.hasPermission(adminPermission)) {
            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Heal -------");
            sender.sendMessage(commandColor + "/heal" + arrow + color + " Mit diesem Befehl kannst du dich heilen");
            sender.sendMessage(commandColor + "/heal <player>" + arrow + color + " Mit diesem Befehl kannst du einen angegebenen Spieler heilen");
        }
    }
}
