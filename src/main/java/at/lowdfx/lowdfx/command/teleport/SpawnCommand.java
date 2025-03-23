package at.lowdfx.lowdfx.command.teleport;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.teleport.SpawnManager;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public final class SpawnCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("spawn")
                .requires(source -> source.getExecutor() instanceof Player && Perms.check(source, Perms.Perm.SPAWN))
                // Standard: /spawn teleportiert den Spieler zum globalen Spawn.
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player))
                        return 1;

                    // Hole den globalen Spawn als SimpleLocation
                    SimpleLocation spawnLoc = SpawnManager.getSpawn(player);
                    if (Configuration.SAFE_TELEPORT_ENABLED) {
                        TeleportManager.teleportDelayed(player, spawnLoc.asLocation());
                    } else {
                        spawnLoc.teleportSafe(player);
                    }
                    player.sendMessage(LowdFX.serverMessage(
                            Component.text("Du wurdest zum Spawn teleportiert!", NamedTextColor.GREEN)));
                    Utilities.positiveSound(player);
                    return 1;
                })
                // /spawn tp <name>: Teleportiere den Spieler zum benannten Spawn.
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("tp")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    SpawnManager.SPAWNS.keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        return 1;
                                    String name = context.getArgument("name", String.class);
                                    if (!SpawnManager.SPAWNS.containsKey(name)) {
                                        player.sendMessage(LowdFX.serverMessage(
                                                Component.text("Der eingegebene Spawn " + name + " existiert nicht!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    SimpleLocation spawnLoc = SpawnManager.SPAWNS.get(name);
                                    if (Configuration.SAFE_TELEPORT_ENABLED) {
                                        TeleportManager.teleportDelayed(player, spawnLoc.asLocation());
                                    } else {
                                        spawnLoc.teleportSafe(player);
                                    }
                                    player.sendMessage(LowdFX.serverMessage(
                                            Component.text("Du wurdest zum Spawn " + name + " teleportiert!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    return 1;
                                })
                        )
                )
                // /spawn set <name>: Setzt den Spawn mit dem angegebenen Namen an der aktuellen Position des Spielers.
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("set")
                        .requires(source -> Perms.check(source, Perms.Perm.SPAWN_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    SpawnManager.SPAWNS.keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                // Keine Positionsangabe mehr – verwende die aktuelle Position des Spielers.
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        return 1;
                                    String name = context.getArgument("name", String.class);
                                    Location location = player.getLocation();
                                    SpawnManager.SPAWNS.put(name, SimpleLocation.ofLocation(location));
                                    player.sendMessage(LowdFX.serverMessage(
                                            Component.text("Du hast den Spawn " + name + " erfolgreich gesetzt!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    // Falls dies der einzige Spawn ist, setze zusätzlich den globalen Default-Spawn.
                                    if (SpawnManager.SPAWNS.size() == 1) {
                                        SpawnManager.setSpawn("default", Bukkit.getWorlds().get(0).getSpawnLocation());
                                    }
                                    return 1;
                                })
                        )
                )
                // /spawn remove <name>: Löscht den benannten Spawn.
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                        .requires(source -> Perms.check(source, Perms.Perm.SPAWN_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    SpawnManager.SPAWNS.keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        return 1;
                                    String name = context.getArgument("name", String.class);
                                    if (!SpawnManager.SPAWNS.containsKey(name)) {
                                        player.sendMessage(LowdFX.serverMessage(
                                                Component.text("Der Spawn " + name + " existiert nicht!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    // Verhindere das Löschen, wenn es der einzige Spawn ist.
                                    if (SpawnManager.SPAWNS.size() == 1 && SpawnManager.SPAWNS.containsKey(name)) {
                                        player.sendMessage(LowdFX.serverMessage(
                                                Component.text("Es ist nicht möglich, den einzigen verfügbaren Spawn zu löschen.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    SpawnManager.setSpawn(name, null);
                                    player.sendMessage(LowdFX.serverMessage(
                                            Component.text("Der Spawn " + name + " wurde gelöscht!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    return 1;
                                })
                        )
                )
                .build();
    }
}
