package at.lowdfx.lowdfx.commands.basic.vanishCommand;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.vanish";

    private final InvisiblePlayerHandler invisibleHandler;

    public VanishCommand(InvisiblePlayerHandler invisibleHandler) {
        this.invisibleHandler = invisibleHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
            Bukkit.getScheduler().runTask(Lowdfx.PLUGIN, () -> {
                if (sender.hasPermission(ADMIN_PERMISSION)) {
                    if (args.length == 0) {
                        if (!(sender instanceof Player player)) {
                            sender.sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                            return;
                        }
                        vanish(sender, player);
                        return;
                    }

                    if (args.length == 1) {
                        Player player = (Player) sender;

                        if (args[0].equalsIgnoreCase("help")) {
                            sendHelp(sender);
                            return;
                        }
                        if (args[0].equalsIgnoreCase("list")) {
                            vanishList(sender);
                            return;
                        }
                        if (args[0].equalsIgnoreCase("join")) {
                            vanishJoin(sender, player);
                            return;
                        }
                        if (args[0].equalsIgnoreCase("quit")) {
                            vanishQuit(sender, player);
                            return;
                        }
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            sender.sendMessage(Lowdfx.serverMessage(Component.text("Spieler nicht gefunden!", NamedTextColor.RED)));
                            return;
                        }

                        if (args[0].equalsIgnoreCase(target.getName())) {
                            vanishTarget(sender, target, args);
                            return;
                        }
                    }
                }
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutze /vanish help um eine Hilfe zu erhalten!", NamedTextColor.RED)));
            });
            return true;
    }

    private void vanish(CommandSender sender, Player player) {
        if (!player.hasMetadata("vanished")) {
            // Flugmodus aktivieren, falls nicht erlaubt
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }

            // Spieler unsichtbar machen
            invisibleHandler.makePlayerInvisible(player);
            player.setMetadata("vanished", new FixedMetadataValue(Lowdfx.PLUGIN, true));
            player.setSleepingIgnored(true);
            player.setCollidable(false);
            player.setCanPickupItems(false);
            player.setSilent(true);


            // Benachrichtigungen
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Du wurdest vanished!", NamedTextColor.GREEN)));

            // Benachrichtigung an alle Admins
            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission(ADMIN_PERMISSION) && !admin.equals(player)) {
                    admin.sendMessage(Lowdfx.serverMessage(Component.text(player.getName() + " ist nun vanished!", NamedTextColor.GREEN)));
                } else {
                    admin.sendMessage(Component.text(Objects.requireNonNull(Lowdfx.CONFIG.getString("join.quit")), NamedTextColor.YELLOW).append(player.name().color(NamedTextColor.GOLD)).append(Component.text("!", NamedTextColor.YELLOW)));
                }
            }

        } else {
            // Flugmodus deaktivieren
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }

            // Spieler sichtbar machen
            invisibleHandler.makePlayerVisible(player);
            player.removeMetadata("vanished", Lowdfx.PLUGIN);
            player.setSleepingIgnored(false);
            player.setCollidable(true);
            player.setCanPickupItems(true);
            player.setSilent(false);

            // Benachrichtigungen
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Du bist nun nicht mehr vanished!", NamedTextColor.RED)));

            // Benachrichtigung an alle Admins
            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission(ADMIN_PERMISSION) && !admin.equals(player)) {
                    sender.sendMessage(Lowdfx.serverMessage(Component.text(player.getName() + " ist nicht mehr vanished!", NamedTextColor.RED)));
                } else {
                    admin.sendMessage(Component.text(Objects.requireNonNull(Lowdfx.CONFIG.getString("join.welcome")), NamedTextColor.YELLOW).append(player.name().color(NamedTextColor.GOLD)).append(Component.text("!", NamedTextColor.YELLOW)));
                }
            }
        }
    }

    private void vanishTarget(CommandSender sender, Player target, String[] args) {
        if (target == null) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Spieler nicht gefunden!", NamedTextColor.RED)));
            return;
        }

        if (!target.hasMetadata("vanished")) {
            // Aktiviere Flugmodus, falls noch nicht erlaubt
            if (!target.getAllowFlight()) {
                target.setAllowFlight(true);
                target.setFlying(true);
            }

            // Spieler unsichtbar machen
            invisibleHandler.makePlayerInvisible(target);
            target.setMetadata("vanished", new FixedMetadataValue(Lowdfx.PLUGIN, true));
            target.setSleepingIgnored(true);
            target.setCollidable(false);
            target.setCanPickupItems(false);
            target.setSilent(true);

            // Benachrichtigungen
            target.sendMessage(Lowdfx.serverMessage(Component.text("Du wurdest vanished!", NamedTextColor.GREEN)));
            target.sendMessage(Lowdfx.serverMessage(Component.text(target.getName() + " wurdest vanished!", NamedTextColor.GREEN)));

            // Benachrichtigung an alle mit Admin-Berechtigung
            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission(ADMIN_PERMISSION) && !admin.equals(target)) {
                    admin.sendMessage(Lowdfx.serverMessage(Component.text(target.getName() + " ist nun vanished!", NamedTextColor.GREEN)));
                } else {
                    admin.sendMessage(Component.text(Objects.requireNonNull(Lowdfx.CONFIG.getString("join.quit")), NamedTextColor.YELLOW).append(target.name().color(NamedTextColor.GOLD)).append(Component.text("!", NamedTextColor.YELLOW)));
                }
            }

        } else {
            // Flugmodus deaktivieren
            if (target.getAllowFlight()) {
                target.setAllowFlight(false);
                target.setFlying(false);
            }

            // Spieler sichtbar machen
            invisibleHandler.makePlayerVisible(target);
            target.removeMetadata("vanished", Lowdfx.PLUGIN);
            target.setSleepingIgnored(false);
            target.setCollidable(true);
            target.setCanPickupItems(true);
            target.setSilent(false);

            // Benachrichtigungen
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Du bist nun nicht mehr vanished!", NamedTextColor.RED)));
            sender.sendMessage(Lowdfx.serverMessage(Component.text(target.getName() + " ist nun nicht mehr vanished!", NamedTextColor.RED)));

            // Benachrichtigung an alle mit Admin-Berechtigung
            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission(ADMIN_PERMISSION) && !admin.equals(target)) {
                    sender.sendMessage(Lowdfx.serverMessage(Component.text(target.getName() + " ist nicht mehr vanished!", NamedTextColor.RED)));
                } else {
                    admin.sendMessage(Component.text(Objects.requireNonNull(Lowdfx.CONFIG.getString("join.welcome")), NamedTextColor.YELLOW).append(target.name().color(NamedTextColor.GOLD)).append(Component.text("!", NamedTextColor.YELLOW)));
                }
            }
        }
    }

    private void vanishList(CommandSender sender) {
        Set<UUID> vanishedPlayers = invisibleHandler.getVanishedPlayers();
        if (vanishedPlayers.isEmpty()) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Es gibt keine vanished Spieler.", NamedTextColor.GREEN)));
        } else {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Vanished Spieler:", NamedTextColor.GREEN)));

            for (UUID uuid : vanishedPlayers) {
                Player vanishedPlayer = Bukkit.getPlayer(uuid);
                if (vanishedPlayer != null) {
                    sender.sendMessage(Component.text("- ").append(vanishedPlayer.name()).color(NamedTextColor.GREEN));
                }
            }
        }

    }

    private void vanishJoin(CommandSender sender, Player target) {
        // Sende eine Nachricht an alle Online-Spieler (außer target)
        for (Player admin : Bukkit.getOnlinePlayers()) {
            admin.sendMessage(Component.text(Objects.requireNonNull(Lowdfx.CONFIG.getString("join.welcome")), NamedTextColor.YELLOW).append(target.name().color(NamedTextColor.GOLD)).append(Component.text("!", NamedTextColor.YELLOW)));
        }
    }

    private void vanishQuit(CommandSender sender, Player target) {
        // Sende eine Nachricht an alle Online-Spieler (außer target)
        for (Player admin : Bukkit.getOnlinePlayers()) {
            admin.sendMessage(Lowdfx.serverMessage(Component.text(Objects.requireNonNull(Lowdfx.CONFIG.getString("join.quit")), NamedTextColor.YELLOW).append(target.name()).append(Component.text("!", NamedTextColor.YELLOW))));
        }
    }

    private void sendHelp(@NotNull CommandSender sender) {
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                <gold><b>------- Help: Vanish -------</b>
                <yellow>/vanish <white>→ <gray> Mit diesem Befehl machst du dich unsichtbar.
                <yellow>/vanish <player> <white>→ <gray> Mit diesem Befehl kannst du einen angegebenen Spieler unsichtbar machen.
                <yellow>/vanish list <white>→ <gray> Mit diesem Befehl kannst du alle vanished Spieler ansehen.
                <yellow>/vanish join <white>→ <gray> Mit diesem Befehl kannst du eine Server Join Nachricht senden.
                <yellow>/vanish quit <white>→ <gray> Mit diesem Befehl kannst du eine Server Quit Nachricht senden.
                """));
        }
    }
}
