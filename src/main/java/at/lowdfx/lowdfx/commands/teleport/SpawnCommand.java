package at.lowdfx.lowdfx.commands.teleport;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.commands.teleport.managers.SpawnManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SpawnCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.spawn.setremove";
    public static final String PLAYER_PERMISSION = "lowdfx.spawn";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(Lowdfx.PLUGIN, () -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("Das darf nur eine Spieler tun!", NamedTextColor.RED));
                return;
            }
            if (sender.hasPermission(PLAYER_PERMISSION)) {
                if (args.length == 0) {
                    spawn(sender);
                    return;
                }

                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("help")) {
                        sendHelp(sender);
                        return;
                    }
                }
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("tp")) {
                        spawnAdmin(sender, args[1]);
                        return;
                    }
                }
            }

            if (sender.hasPermission(ADMIN_PERMISSION)) {
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("set")) {
                        setAdmin(sender, args[1]);
                        return;
                    }

                    if (args[0].equalsIgnoreCase("remove")) {
                        removeAdmin(sender, args[1]);
                        return;
                    }
                }
            }
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler! Benutze /spawn help für eine hilfe!", NamedTextColor.RED)));
        });
        return true;
    }

    private void removeAdmin(CommandSender sender, String name) {
        // Überprüfen, ob der Spawn existiert und sicherstellen, dass mehr als ein Spawn vorhanden ist
        if (SpawnManager.exists(name)) {
            // Prüfen, ob nur der aktuelle Spawn der einzige Spawn ist
            if (SpawnManager.getNames().size() == 1 && SpawnManager.getNames().contains(name)) {
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Es ist nicht möglich, den einzigen verfügbaren Spawn zu löschen.", NamedTextColor.RED)));
                return;
            }

            // Entferne den Spawn, falls vorhanden
            SpawnManager.setSpawn(name, null);
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Der Spawn " + name + " wurde gelöscht!", NamedTextColor.GREEN)));
        } else {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Der Spawn " + name + " existiert nicht!", NamedTextColor.RED)));
        }
    }

    private void setAdmin(CommandSender sender, String name) {
        // Setzen des Spawns
        SpawnManager.setSpawn(name, ((Player) sender).getLocation());
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Der Spawn " + name + " wurde gesetzt!", NamedTextColor.GREEN)));

        // Wenn nur ein Spawn existiert, den Standard-spawn wiederherstellen
        if (SpawnManager.getNames().size() == 1) {
            // Neue Version ohne ServerProperties
            SpawnManager.setSpawn("default", Bukkit.getWorlds().getFirst().getSpawnLocation());  // Hier wird die erste Welt im Server genutzt

        }
    }

    private void spawnAdmin(CommandSender sender, String name) {
        if (SpawnManager.exists(name)) {
            Objects.requireNonNull(SpawnManager.getSpawn(name)).teleport(((Player) sender), Lowdfx.PLUGIN);
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Du wurdest zum Spawn teleportiert!", NamedTextColor.GREEN)));
        } else {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Der eingegebene Spawn " + name + " existiert nicht!", NamedTextColor.RED)));
        }
    }

    private void spawn(@NotNull CommandSender sender) {
        SpawnManager.getSpawn(((Player) sender)).teleport(((Player) sender), Lowdfx.PLUGIN);
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Du wurdest zum Spawn teleportiert!", NamedTextColor.GREEN)));
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                <gold><b>------- Help: Spawn -------</b>
                <yellow>/spawn <white>→ <gray> Teleportiert dich zum Spawn.
                <yellow>/spawn help <white>→ <gray> Sendet dir eine Hilfestellung.
                """));

        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                    <gold><b>------- Help: Spawn -------</b>
                    <yellow>/spawn set <name> <white>→ <gray> Setzt einen Spawn mit dem Namen.
                    <yellow>/spawn remove <name> <white>→ <gray> Löscht einen Spawn mit den Namen.
                    <yellow>/spawn tp <name> <white>→ <gray> Teleportiert dich zum Spawn mit den eingegebenen Namen.
                    """));
        }
    }
}
