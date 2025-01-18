package at.lowdfx.lowdfx.commands.teleport;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.commands.teleport.managers.HomeManager;
import at.lowdfx.lowdfx.commands.teleport.teleportPoints.HomePoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HomeCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.home.admin";
    public static final String PLAYER_PERMISSION = "lowdfx.home";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(Lowdfx.PLUGIN, () -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("Das kann nur ein Spieler tun!", NamedTextColor.RED));
                return;
            }

            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("help")) {
                        help(sender);
                        return;
                    }
                }
            }
            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("set")) {
                        set(sender, args);
                        return;
                    }
                }
            }
            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 0) {
                    teleport(sender);
                    return;
                }
            }
            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("remove")) {
                        remove(sender, args);
                        return;
                    }
                }
            }
            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("tp")) {
                        teleportCustom(sender, args);
                        return;
                    }
                }
            }
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                if (args.length == 3) {
                    if (args[0].equalsIgnoreCase("tp_other")) {
                        teleportCustomAdmin(sender, args);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("set_other")) {
                        setAdmin(sender, args);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("remove_other")) {
                        removeAdmin(sender, args);
                        return;
                    }
                }
            }
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutzte /home help für eine Hilfestellung!", NamedTextColor.RED)));
        });
        return true;
    }

    public void teleportCustomAdmin(@NotNull CommandSender sender, String @NotNull [] args) {
        if (args.length != 3 || !sender.hasPermission(ADMIN_PERMISSION) || !args[0].equalsIgnoreCase("tp_other")) return;

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Der eingegebene Spieler " + args[1] + " konnten nicht gefunden werden!", NamedTextColor.RED)));
            return;
        }

        HomePoint homePoint = HomeManager.get(target);
        if (homePoint.doesNotExist(args[2])) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Das Home " + args[2] + " von " + args[1] + " existiert nicht!", NamedTextColor.RED)));
            return;
        }

        homePoint.get(args[2]).teleport((Player) sender, Lowdfx.PLUGIN);
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Du wurdest zum Home " + args[2] + " von " + args[1] + " teleportiert!", NamedTextColor.RED)));
    }

    public void teleportCustom(@NotNull CommandSender sender, String @NotNull [] args) {
        if (args.length != 2 || (!sender.hasPermission(ADMIN_PERMISSION) || !sender.hasPermission(PLAYER_PERMISSION)) || !args[0].equalsIgnoreCase("tp")) return;

        Player player = (Player) sender;
        HomePoint homePoint = HomeManager.get(player);
        if (homePoint.doesNotExist(args[1])) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Dein Home " + args[1] + " wurde noch nicht gesetzt!", NamedTextColor.RED)));
            return;
        }

        homePoint.get(args[1]).teleport(player, Lowdfx.PLUGIN);
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Du wurdest nach Hause " + args[1] + " teleportiert!", NamedTextColor.GREEN)));
    }

    public void teleport(CommandSender sender) {
        Player player = (Player) sender;
        HomePoint homePoint = HomeManager.get(player);
        if (homePoint.doesNotExist("home")) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Dein Home wurde noch nicht gesetzt!", NamedTextColor.RED)));
            return;
        }

        homePoint.get("home").teleport(player, Lowdfx.PLUGIN);
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Du wurdest nach Hause teleportiert!", NamedTextColor.GREEN)));
    }

    // Administrator: set another player's home
    public void setAdmin(@NotNull CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION))
            return;
        if (args.length != 3)
            return;
        if (!args[0].equalsIgnoreCase("set_other"))
            return;

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Der Spieler " + args[1] + " existiert nicht!", NamedTextColor.RED)));
            return;
        }

        HomePoint homePoint = HomeManager.get(target);
        homePoint.set(args[2], ((Player) sender).getLocation());
        homePoint.save();
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Das Home " + args[2] + " von " + args[1] + " wurde gesetzt!", NamedTextColor.RED)));
    }

    // Set your own home
    public void set(CommandSender sender, String @NotNull [] args) {

        Player player = (Player) sender;
        String homeName = (args.length == 1) ? "home" : args[1];

        if (HomeManager.get(player).getHomes().size() >= Lowdfx.CONFIG.getInt("basic.maxhomes") && !player.hasPermission(ADMIN_PERMISSION)) {
            player.sendMessage(Lowdfx.serverMessage(Component.text("Die maximale Home-Grenze von " + Lowdfx.CONFIG.getInt("basic.maxhomes") + " wurde erreicht!", NamedTextColor.RED)));
            return;
        }

        HomePoint homePoint = HomeManager.get(player);
        homePoint.set(homeName, player.getLocation());
        homePoint.save();
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Dein Home " + homeName + " wurde gesetzt!", NamedTextColor.GREEN)));
    }

    // Administrator: Remove another player's home
    public void removeAdmin(@NotNull CommandSender sender, String @NotNull [] args) {
        if (args.length != 3 || !sender.hasPermission(ADMIN_PERMISSION) || !args[0].equalsIgnoreCase("remove_other")) return;

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Der Spieler " + args[1] + " existiert nicht!", NamedTextColor.RED)));
            return;
        }

        HomePoint homePoint = HomeManager.get(target);
        if (homePoint.doesNotExist(args[2])) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Das Home " + args[2] + " existiert nicht!", NamedTextColor.RED)));
            return;
        }

        homePoint.remove(args[2]);
        homePoint.save();
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Das Home " + args[2] + " von " + args[1] + " wurde entfernt!", NamedTextColor.RED)));
    }

    // Remove your own home
    public void remove(CommandSender sender, String @NotNull [] args) {
        Player player = (Player) sender;
        HomePoint homePoint = HomeManager.get(player);

        if (homePoint.doesNotExist(args[1])) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Das Home " + args[1] + " existiert nicht!", NamedTextColor.RED)));
            return;
        }

        homePoint.remove(args[1]);
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Das Home " + args[1] + " wurde entfernt!", NamedTextColor.RED)));
    }

    // Help command
    private void help(@NotNull CommandSender sender) {
        if (sender.hasPermission(PLAYER_PERMISSION) || sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                <gold><b>------- Help: Home -------</b>
                <yellow>/home <white>→ <gray> Teleportiere zu deinem Haupt-Home!
                <yellow>/home set <name> <white>→ <gray> Setze dein Home an deinem aktuellen Standort!
                <yellow>/home remove <name> <white>→ <gray> Entferne dein Home!
                <yellow>/home tp <name> <white>→ <gray> Teleportiere dich zu deinem angegebenem Home!
                """));
        }
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                    <yellow>/home tp_other <player> <home_name> <white>→ <gray> Teleportiere dich zum Home eines anderen Spielers!
                    <yellow>/home set_other <player> <home_name> <white>→ <gray> Setze das Home eines anderen Spielers!
                    <yellow>/home home remove_other <player> <home_name> <white>→ <gray> Entferne das Home eines anderen Spielers!
                    """));
        }
    }
}
