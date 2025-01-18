package at.lowdfx.lowdfx.commands.teleport;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.commands.teleport.managers.WarpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
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
                        sender.sendMessage(Lowdfx.serverMessage(Component.text("Der eingegebene Warppunkt " + args[0] + " existiert nicht!", NamedTextColor.RED)));
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
                            sender.sendMessage(Lowdfx.serverMessage(Component.text("Der Warppunkt " + args[1] + " existiert nicht!", NamedTextColor.RED)));
                        }
                        return;
                    }
                }
            }
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutze /warp help um eine Hilfe zu erhalten!", NamedTextColor.RED)));
        });
        return true;
    }

    private void set(@NotNull CommandSender sender, String name) {
        WarpManager.set(name, ((Player) sender).getLocation());
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Du hast den Warppunkt " + name + " erfolgreich gesetzt!", NamedTextColor.GREEN)));
    }

    private void remove(@NotNull CommandSender sender, String name) {
        WarpManager.set(name, null);
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Du hast den Warppunkt " + name + " gelöscht!", NamedTextColor.GREEN)));
    }

    private void warp(@NotNull CommandSender sender, String name) {
        Entity entity = (Entity) sender;
        WarpManager.teleport(name, entity);
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Du hast den Warppunkt " + name + " teleportiert!", NamedTextColor.GREEN)));
    }

    private void sendHelp(@NotNull CommandSender sender) {
        if (sender.hasPermission(PLAYER_PERMISSION) || sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                <gold><b>------- Help: Warp -------</b>
                <yellow>/warp <white>→ <gray> Teleportiert dich zu dem eingegebenen Warppunkt!
                """));
        }
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                    <yellow>/warp set <name> <white>→ <gray> Setzt einen Warppunkt mit dem eingegebenen Name.
                    <yellow>/warp remove <name> <white>→ <gray> Löscht einen Warppunkt mit dem eingegebenen Name.
                    """));
        }
    }
}
