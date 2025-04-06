package at.lowdfx.lowdfx.command.teleport;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.managers.teleport.TeleportManager;
import at.lowdfx.lowdfx.util.Configuration;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.SimpleLocation;
import at.lowdfx.lowdfx.util.Utilities;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Random;

@SuppressWarnings("UnstableApiUsage")
public final class TpCommands {

    static {
        CommandHelp.register("tphere",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/tphere <player>"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du einen Spieler zu dir teleportieren.<newline></gray>" +
                                "<yellow>· /tphere <player></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.TPHERE.getPermission(),
                null); // Keine separate Admin-Permission
    }

    static {
        CommandHelp.register("tpall",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/tpall"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du alle Spieler zu dir teleportieren.<newline></gray>" +
                                "<yellow>· /tpall</yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.TPALL.getPermission(),
                null); // Keine separate Admin-Permission
    }

    static {
        CommandHelp.register("back",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/back"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du dich zum letzten Teleportpunkt oder Tod teleportieren.<newline></gray>" +
                                "<yellow>· /back</yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.BACK_PREMIUM.getPermission(),
                null); // Keine separate Admin-Permission
    }

    static {
        CommandHelp.register("rtp",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/rtp"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du dich an eine zufällige Stelle teleportieren.<newline></gray>" +
                                "<yellow>· /rtp</yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.RTP.getPermission(),
                null); // Keine separate Admin-Permission
    }

    public static LiteralCommandNode<CommandSourceStack> tphereCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("tphere")
                .requires(source -> Perms.check(source, Perms.Perm.TPHERE))
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("players", ArgumentTypes.players())
                        .executes(context -> {
                            Location target = context.getSource().getLocation();
                            Component message = LowdFX.serverMessage(Component.text("Du wurdest zu " + context.getSource().getSender().getName() + " teleportiert!", NamedTextColor.GREEN));
                            context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).forEach(p -> {
                                TeleportManager.teleportSafe(p, target);
                                p.sendMessage(message);
                                Utilities.positiveSound(p);
                            });
                            return 1;
                        })
                )
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> tpallCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("tpall")
                .requires(source -> Perms.check(source, Perms.Perm.TPALL))
                .executes(context -> {
                    Location target = context.getSource().getLocation();
                    Component message = LowdFX.serverMessage(Component.text("Du wurdest zu " + context.getSource().getSender().getName() + " teleportiert!", NamedTextColor.GREEN));
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        TeleportManager.teleportSafe(p, target);
                        p.sendMessage(message);
                        Utilities.positiveSound(p);
                    });
                    return 1;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> backCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("back")
                .requires(source -> Perms.check(source, Perms.Perm.BACK))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(
                                LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED))
                        );
                        return 1;
                    }

                    // Prüfe, ob der Spieler NICHT die TP_BYPASS-Permission hat und BACK_PREMIUM ist.
                    // Haben sie TP_BYPASS, wird kein Cooldown geprüft.
                    if (!Perms.check(player, Perms.Perm.TP_BYPASS) && Perms.check(player, Perms.Perm.BACK_PREMIUM)) {
                        long currentTime = System.currentTimeMillis();
                        long lastBack = at.lowdfx.lowdfx.managers.teleport.CooldownManager.getBackCooldown(player.getUniqueId());
                        long cooldownMillis = Configuration.BACK_COOLDOWN * 1000L; // BACK_COOLDOWN aus der config.yml in Sekunden

                        if (currentTime < lastBack + cooldownMillis) {
                            long remainingSeconds = (lastBack + cooldownMillis - currentTime) / 1000;
                            String timeLeft = formatTime(remainingSeconds);
                            player.sendMessage(
                                    LowdFX.serverMessage(Component.text("Du kannst /back erst in " + timeLeft + " erneut verwenden!", NamedTextColor.RED))
                            );
                            return 1;
                        }
                        // Aktualisiere den Zeitstempel, da der /back-Befehl ausgeführt wird
                        at.lowdfx.lowdfx.managers.teleport.CooldownManager.setBackCooldown(player.getUniqueId(), currentTime);
                    }

                    // Letztes Ereignis (Teleport oder Tod) abrufen
                    SimpleLocation lastEvent = TeleportManager.getLastEvent(player.getUniqueId());
                    if (lastEvent == null) {
                        player.sendMessage(
                                LowdFX.serverMessage(Component.text("Kein Ereignis zum Zurückteleportieren gefunden!", NamedTextColor.RED))
                        );
                        Utilities.negativeSound(player);
                        return 1;
                    }

                    lastEvent.teleportSafe(player);
                    player.sendMessage(
                            LowdFX.serverMessage(Component.text("Du wurdest zurück teleportiert!", NamedTextColor.GREEN))
                    );
                    Utilities.positiveSound(player);
                    return 1;
                })
                .build();
    }

    /**
     * Formatiert die übergebenen Sekunden in ein hh:mm:ss-Format.
     */
    private static String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }





    public static LiteralCommandNode<CommandSourceStack> rtpCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("rtp")
                .requires(source -> Perms.check(source, Perms.Perm.RTP))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(
                                LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED))
                        );
                        return 1;
                    }

                    // Erzeuge eine zufällige Position im Bereich -10000 bis 10000 (X und Z)
                    World world = player.getWorld();
                    Random random = new Random();
                    int range = 10000;
                    int x = random.nextInt(range * 2) - range;
                    int z = random.nextInt(range * 2) - range;
                    int y = world.getHighestBlockYAt(x, z);
                    Location randomLocation = new Location(world, x + 0.5, y + 1, z + 0.5);

                    // Starte die verzögerte Teleportation
                    TeleportManager.teleportDelayed(player, randomLocation);

                    // Zeige nach Ablauf des Delays die Koordinaten als Title für 3 Sekunden an
                    int delaySeconds = Configuration.TELEPORT_DELAY;
                    Bukkit.getScheduler().runTaskLater(LowdFX.PLUGIN, () -> {
                        String coords = "X: " + x + "  Y: " + (y + 1) + "  Z: " + z;
                        player.showTitle(net.kyori.adventure.title.Title.title(
                                Component.text(coords),
                                Component.empty(),
                                net.kyori.adventure.title.Title.Times.times(
                                        java.time.Duration.ofMillis(500),   // Fade In (10 ticks)
                                        java.time.Duration.ofMillis(3000),  // Stay (60 ticks)
                                        java.time.Duration.ofMillis(500)    // Fade Out (10 ticks)
                                )
                        ));
                    }, delaySeconds * 20L + 1L);

                    return 1;
                })
                .build();
    }


}
