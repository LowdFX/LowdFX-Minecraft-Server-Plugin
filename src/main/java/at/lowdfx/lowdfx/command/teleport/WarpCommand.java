package at.lowdfx.lowdfx.command.teleport;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.managers.teleport.WarpManager;
import at.lowdfx.lowdfx.managers.teleport.TeleportManager;
import at.lowdfx.lowdfx.util.Configuration;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.SimpleLocation;
import at.lowdfx.lowdfx.util.Utilities;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public final class WarpCommand {

    static {
        CommandHelp.register("warp",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/warp <name>"),
                // Ausführliche Beschreibung für normale Spieler
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du dich an einen angegebenen Warppunkt teleportieren.<newline>" +
                                "<yellow>· /warp <name>></yellow>"),
                // Zusätzlicher Admin-Teil (optional)
                MiniMessage.miniMessage().deserialize(
                        "<yellow>· /warp set <name><newline></yellow>" +
                                "<yellow>· /warp remove <name></yellow>"),
                // Basis-Permission
                Perms.Perm.WARP.getPermission(),
                // Admin-Permission (hier wird der Admin-Text nur angezeigt, wenn der Spieler diese besitzt)
                Perms.Perm.WARP_ADMIN.getPermission());
    }

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("warp")
                .requires(source -> Perms.check(source, Perms.Perm.WARP))
                // /warp <name>: Teleportiere zum angegebenen Warppunkt
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                        .requires(source -> source.getExecutor() instanceof Player)
                        .suggests((context, builder) -> {
                            WarpManager.WARPS.keySet().forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player))
                                return 1;
                            String name = context.getArgument("name", String.class);

                            if (!WarpManager.WARPS.containsKey(name)) {
                                player.sendMessage(LowdFX.serverMessage(
                                        Component.text("Der eingegebene Warppunkt " + name + " existiert nicht!", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            Location warpLocation = WarpManager.WARPS.get(name).asLocation();
                            if (Configuration.SAFE_TELEPORT_ENABLED) {
                                TeleportManager.teleportDelayed(player, warpLocation);
                            } else {
                                WarpManager.WARPS.get(name).teleportSafe(player);
                            }

                            player.sendMessage(LowdFX.serverMessage(
                                    Component.text("Du wirst zum Warppunkt " + name + " teleportiert!", NamedTextColor.GREEN)));
                            Utilities.positiveSound(player);
                            return 1;
                        })
                )
                // /warp set <name>:
                // Setzt den Warppunkt mit dem angegebenen Namen an der aktuellen Position des Spielers.
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("set")
                        .requires(source -> Perms.check(source, Perms.Perm.WARP_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    WarpManager.WARPS.keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                // Hier wird NICHT mehr ein Positionsargument verlangt – die aktuelle Position des Spielers wird verwendet.
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        return 1;
                                    String name = context.getArgument("name", String.class);
                                    Location location = player.getLocation();
                                    WarpManager.WARPS.put(name, SimpleLocation.ofLocation(location));
                                    player.sendMessage(LowdFX.serverMessage(
                                            Component.text("Du hast den Warppunkt " + name + " erfolgreich gesetzt!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    // Falls dies der einzige Warppunkt ist, setze zusätzlich den Default-Spawn.
                                    if (WarpManager.WARPS.size() == 1) {
                                        WarpManager.setSpawn("default", Bukkit.getWorlds().get(0).getSpawnLocation());
                                    }
                                    return 1;
                                })
                        )
                )
                // /warp remove <name>: Löscht den angegebenen Warppunkt
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                        .requires(source -> Perms.check(source, Perms.Perm.WARP_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    WarpManager.WARPS.keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        return 1;
                                    String name = context.getArgument("name", String.class);

                                    if (!WarpManager.WARPS.containsKey(name)) {
                                        player.sendMessage(LowdFX.serverMessage(
                                                Component.text("Der eingegebene Warppunkt " + name + " existiert nicht!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    WarpManager.WARPS.remove(name);
                                    player.sendMessage(LowdFX.serverMessage(
                                            Component.text("Du hast den Warppunkt " + name + " gelöscht!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    return 1;
                                })
                        )
                )
                .build();
    }
}
