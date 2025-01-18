package at.lowdfx.lowdfx.commands.basic.vanish;

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
import org.jetbrains.annotations.Nullable;

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
                        vanish(player);
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
                            vanishJoin(player);
                            return;
                        }
                        if (args[0].equalsIgnoreCase("quit")) {
                            vanishQuit(player);
                            return;
                        }
                        Player target = Bukkit.getPlayer(args[0]);
                        if (target == null) {
                            sender.sendMessage(Lowdfx.serverMessage(Component.text("Spieler nicht gefunden!", NamedTextColor.RED)));
                            return;
                        }

                        if (args[0].equalsIgnoreCase(target.getName())) {
                            vanishTarget(sender, target);
                            return;
                        }
                    }
                }
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutze /vanish help um eine Hilfe zu erhalten!", NamedTextColor.RED)));
            });
            return true;
    }

    private void vanish(@NotNull Player player) {
        if (!player.hasMetadata("vanished")) {
            vanish(player, true, null);
            announceVanish(player, true);
        } else {
            vanish(player, false, null);
            announceVanish(player, false);
        }
    }

    private void vanishTarget(CommandSender sender, Player target) {
        if (target == null) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Spieler nicht gefunden!", NamedTextColor.RED)));
            return;
        }

        if (!target.hasMetadata("vanished")) {
            vanish(target, true, sender);
            announceVanish(target, true);
        } else {
            vanish(target, false, sender);
            announceVanish(target, false);
        }
    }

    public void vanish(@NotNull Player player, boolean state, @Nullable CommandSender sender) {
        // Aktiviere Flugmodus, falls noch nicht erlaubt
        if (!player.getAllowFlight()) {
            player.setAllowFlight(state);
            player.setFlying(state);
        }

        // Spieler unsichtbar machen
        invisibleHandler.makePlayerInvisible(player);
        if (state) {
            player.setMetadata("vanished", new FixedMetadataValue(Lowdfx.PLUGIN, true));
        } else {
            player.removeMetadata("vanished", Lowdfx.PLUGIN);
        }
        player.setSleepingIgnored(state);
        player.setCollidable(!state);
        player.setCanPickupItems(!state);
        player.setSilent(state);

        // Benachrichtigungen
        if (state) {
            player.sendMessage(Lowdfx.serverMessage(Component.text("Du bist nun vanished!", NamedTextColor.GREEN)));
            if (sender != null)
                sender.sendMessage(Lowdfx.serverMessage(Component.text(player.getName() + " ist nun vanished!", NamedTextColor.RED)));
        } else {
            player.sendMessage(Lowdfx.serverMessage(Component.text("Du bist nun nicht mehr vanished!", NamedTextColor.RED)));
            if (sender != null)
                sender.sendMessage(Lowdfx.serverMessage(Component.text(player.getName() + " ist nun nicht mehr vanished!", NamedTextColor.RED)));
        }
    }

    private void announceVanish(@NotNull Player player, boolean state) {
        Component adminMessage = Lowdfx.serverMessage(Component.text(player.getName() + (state ? " ist nun vanished!" : " ist nicht mehr vanished."), state ? NamedTextColor.GREEN : NamedTextColor.RED));
        Component playerMessage = Component.text(Objects.requireNonNull(Lowdfx.CONFIG.getString("join." + (state ? "quit" : "welcome"))), NamedTextColor.YELLOW).append(player.name().color(NamedTextColor.GOLD)).append(Component.text("!", NamedTextColor.YELLOW));

        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p.hasPermission(ADMIN_PERMISSION) && !p.equals(player))
                p.sendMessage(adminMessage);
        });
        Bukkit.getServer().sendMessage(playerMessage);
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

    private void vanishJoin(Player target) {
        // Sende eine Nachricht an alle Online-Spieler (außer target)
        for (Player admin : Bukkit.getOnlinePlayers()) {
            admin.sendMessage(Component.text(Objects.requireNonNull(Lowdfx.CONFIG.getString("join.welcome")), NamedTextColor.YELLOW).append(target.name().color(NamedTextColor.GOLD)).append(Component.text("!", NamedTextColor.YELLOW)));
        }
    }

    private void vanishQuit(Player target) {
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
