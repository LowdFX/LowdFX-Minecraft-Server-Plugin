package at.lowdfx.lowdfx.commands.teleport;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.commands.teleport.managers.WarpManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WarpCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.warp.setremove";
    public static final String PLAYER_PERMISSION = "lowdfx.warp";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTask(Lowdfx.PLUGIN, () -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!"));
                return;
            }
            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("help")) {
                        sendHelp(sender);
                        return;
                    }
                    if (WarpManager.exits(args[0])) {
                        warp(sender, args[0]);
                    } else {
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Der eingegebene Warppunkt " + args[0] + ChatColor.RED + " existiert nicht!");
                    }
                    return;
                }
            }

            if (args.length == 2) {
                if (sender.hasPermission(ADMIN_PERMISSION)) {
                    if (args[0].equalsIgnoreCase("set")) {
                        set(sender, args[1]);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("remove")) {
                        if (WarpManager.exits(args[1])) {
                            remove(sender, args[1]);
                        } else {
                            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Der Warppunkt " + ChatColor.BOLD + args[1] + ChatColor.RED + " existiert nicht!");
                        }
                        return;
                    }
                }
            }

            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /warp help um eine Hilfe zu erhalten!");

        });
        return true;
    }

    private void set(CommandSender sender, String name) {
        WarpManager.set(name, ((Player) sender).getLocation());
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " +ChatColor.GREEN + "Du hast den Warppunkt " + ChatColor.BOLD + name + ChatColor.GREEN + " erfolgreich gesetzt!");
    }

    private void remove(CommandSender sender, String name) {
        WarpManager.set(name, null);
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " +ChatColor.GREEN + "Du hast den Warppunkt " + ChatColor.BOLD + name + ChatColor.GREEN + " gelöscht!");
    }

    private void warp(CommandSender sender, String name) {
        Entity entity = (Entity) sender;
        WarpManager.teleport(name, entity);
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + Lowdfx.CONFIG.getString("basic.servername") + ChatColor.GRAY + " >> " +ChatColor.GREEN + "Du wurdest zu " + ChatColor.BOLD + name + ChatColor.GREEN + " teleportiert!");
    }

    private void sendHelp(CommandSender sender) {
        String title = ChatColor.GOLD.toString();
        String color = ChatColor.GRAY.toString();
        String commandColor = ChatColor.YELLOW.toString();
        String arrow = ChatColor.WHITE.toString() + " → ";

        if (sender.hasPermission(PLAYER_PERMISSION) || sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(title + ChatColor.BOLD + "------- Help: Warp -------");
            sender.sendMessage(commandColor + "/warp <name>" + arrow + color + " Teleportiert dich zu dem eingegebenen Warppunkt.");
        }

        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(commandColor + "/warp set <name>" +  arrow + color +" Setzt einen Warppunkt mit dem eingegebenen Name.");
            sender.sendMessage(commandColor + "/warp remove <name>" +  arrow + color +" Löscht einen Warppunkt mit dem eingegebenen Name.");
        }
    }
}
