package at.lowdfx.lowdfx.command.teleport;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.managers.teleport.HomeManager;
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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class HomeCommand {
    static {
        CommandHelp.register("home",
                // Kurzinfo (Übersicht)
                MiniMessage.miniMessage().deserialize("/help home"),
                // Ausführliche Beschreibung für normale Spieler
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du deine Homes erstellen/verwalten.<newline>" +
                                "<yellow>· /home               - Teleportiere zu deinem Standard-Home<newline></yellow>" +
                                "<yellow>· /home set <name>    - Setze ein Home<newline></yellow>" +
                                "<yellow>· /home remove <name> - Entferne ein Home<newline></yellow>" +
                                "<yellow>· /home tp <name>     - Teleportiere zu einem benannten Home</yellow>"),
                // Admin-Bereich (nur sichtbar bei HOME_ADMIN)
                MiniMessage.miniMessage().deserialize(
                        "<yellow>· /home tp_other <player> <homeName>    - Teleportiere zu einem Home eines anderen Spielers<newline></yellow>" +
                                "<yellow>· /home set_other <player> <homeName>   - Setze das Home eines anderen Spielers an deine Position<newline></yellow>" +
                                "<yellow>· /home remove_other <player> <homeName> - Entferne das Home eines anderen Spielers</yellow>"),
                // Basis-Permission
                Perms.Perm.HOME.getPermission(),
                // Admin-Permission
                Perms.Perm.HOME_ADMIN.getPermission()
        );
    }
    @SuppressWarnings("UnstableApiUsage")
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("home")
                .requires(source -> {
                    // Sicherstellen, dass der Executor existiert und ein Player ist.
                    Object exec = source.getExecutor();
                    return exec instanceof Player && Perms.check(source, Perms.Perm.HOME);
                })
                // /home – Teleportiere den Spieler zu seinem Standard-Home ("home")
                .executes(context -> {
                    Object exec = context.getSource().getExecutor();
                    if (!(exec instanceof Player player)) {
                        context.getSource().getSender().sendMessage(
                                LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
                        return 1;
                    }
                    UUID playerId = player.getUniqueId();
                    Map<String, SimpleLocation> homes = HomeManager.get(playerId);
                    if (homes == null || homes.isEmpty() || !homes.containsKey("home")) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Du hast keine Homes!", NamedTextColor.RED)));
                        Utilities.negativeSound(player);
                        return 1;
                    }
                    SimpleLocation homeLoc = homes.get("home");
                    if (homeLoc == null) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home konnte nicht geladen werden!", NamedTextColor.RED)));
                        Utilities.negativeSound(player);
                        return 1;
                    }
                    Location loc = homeLoc.asLocation();
                    // Wir gehen hier davon aus, dass asLocation() niemals null zurückgibt.
                    if (Configuration.SAFE_TELEPORT_ENABLED) {
                        TeleportManager.teleportDelayed(player, loc);
                    } else {
                        homeLoc.teleportSafe(player);
                    }
                    player.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest nach Hause teleportiert!", NamedTextColor.GREEN)));
                    Utilities.positiveSound(player);
                    return 1;
                })
                // /home tp <name>: Teleportiere den Spieler zu einem benannten Home
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("tp")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    Object exec = context.getSource().getExecutor();
                                    if (exec instanceof Player p) {
                                        Map<String, SimpleLocation> homes = HomeManager.get(p.getUniqueId());
                                        if (homes != null) {
                                            homes.keySet().forEach(builder::suggest);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    Object exec = context.getSource().getExecutor();
                                    if (!(exec instanceof Player player)) {
                                        return 1;
                                    }
                                    String name = context.getArgument("name", String.class);
                                    Map<String, SimpleLocation> homes = HomeManager.get(player.getUniqueId());
                                    if (homes == null || homes.isEmpty() || !homes.containsKey(name)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du hast kein Home mit dem Namen " + name + "!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    SimpleLocation homeLoc = homes.get(name);
                                    if (homeLoc == null) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Das Home " + name + " konnte nicht geladen werden!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    Location loc = homeLoc.asLocation();
                                    if (Configuration.SAFE_TELEPORT_ENABLED) {
                                        TeleportManager.teleportDelayed(player, loc);
                                    } else {
                                        homeLoc.teleportSafe(player);
                                    }
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest nach Hause " + name + " teleportiert!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    return 1;
                                })
                        )
                )
                // /home set <name>: Setzt das Home an der aktuellen Position des Spielers
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("set")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .executes(context -> {
                                    Object exec = context.getSource().getExecutor();
                                    if (!(exec instanceof Player player)) {
                                        return 1;
                                    }
                                    String name = context.getArgument("name", String.class);
                                    Location loc = player.getLocation();
                                    Map<String, SimpleLocation> homes = HomeManager.get(player.getUniqueId());
                                    if (homes == null) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du hast keine Homes!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    if (homes.size() >= Configuration.BASIC_MAX_HOMES) {
                                        player.sendMessage(LowdFX.serverMessage(
                                                Component.text("Die maximale Anzahl von Homes (" + Configuration.BASIC_MAX_HOMES + ") wurde erreicht!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    homes.put(name, SimpleLocation.ofLocation(loc));
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home " + name + " wurde gesetzt!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    return 1;
                                })
                        )
                )
                // /home remove <name>: Löscht ein benanntes Home
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    Object exec = context.getSource().getExecutor();
                                    if (exec instanceof Player player) {
                                        Map<String, SimpleLocation> homes = HomeManager.get(player.getUniqueId());
                                        if (homes != null) {
                                            homes.keySet().forEach(builder::suggest);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    Object exec = context.getSource().getExecutor();
                                    if (!(exec instanceof Player player)) {
                                        return 1;
                                    }
                                    String name = context.getArgument("name", String.class);
                                    Map<String, SimpleLocation> homes = HomeManager.get(player.getUniqueId());
                                    if (homes == null || homes.isEmpty() || !homes.containsKey(name)) {
                                        player.sendMessage(LowdFX.serverMessage(
                                                Component.text("Du hast kein Home mit dem Namen " + name + "!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    homes.remove(name);
                                    player.sendMessage(LowdFX.serverMessage(
                                            Component.text("Dein Home " + name + " wurde entfernt!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    return 1;
                                })
                        )
                )

                // ======================= ADMIN-BEREICH =======================
                // /home tp_other <player> <homeName>: Teleportiere den Admin zu einem Home eines anderen Spielers (auch offline)
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("tp_other")
                        .requires(source -> Perms.check(source, Perms.Perm.HOME_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    // Alle bekannten Spielernamen aus HomeManager
                                    HomeManager.HOMES.keySet().forEach(uuid -> {
                                        String name = Bukkit.getOfflinePlayer(uuid).getName();
                                        if (name != null) {
                                            builder.suggest(name);
                                        }
                                    });
                                    return builder.buildFuture();
                                })
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("homeName", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String targetName = context.getArgument("player", String.class);
                                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                                            Map<String, SimpleLocation> homes = HomeManager.get(target.getUniqueId());
                                            if (homes != null) {
                                                homes.keySet().forEach(builder::suggest);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String targetName = context.getArgument("player", String.class);
                                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                                            String homeName = context.getArgument("homeName", String.class);
                                            Map<String, SimpleLocation> homes = HomeManager.get(target.getUniqueId());
                                            if (homes == null || homes.isEmpty() || !homes.containsKey(homeName)) {
                                                context.getSource().getSender().sendMessage(
                                                        LowdFX.serverMessage(Component.text("Der Spieler " + target.getName() + " hat kein Home mit dem Namen " + homeName + "!", NamedTextColor.RED)));
                                                return 1;
                                            }
                                            SimpleLocation homeLoc = homes.get(homeName);
                                            if (homeLoc == null) {
                                                context.getSource().getSender().sendMessage(
                                                        LowdFX.serverMessage(Component.text("Das Home " + homeName + " ist ungültig!", NamedTextColor.RED)));
                                                return 1;
                                            }
                                            // Hier gehen wir davon aus, dass asLocation() immer eine gültige Location liefert.
                                            Location loc = homeLoc.asLocation();
                                            // Sicherstellen, dass der Executor ein Player ist.
                                            Player executor = context.getSource().getSender() instanceof Player ? (Player) context.getSource().getSender() : null;
                                            if (executor == null) {
                                                return 1;
                                            }
                                            if (Configuration.SAFE_TELEPORT_ENABLED) {
                                                TeleportManager.teleportDelayed(executor, loc);
                                            } else {
                                                homeLoc.teleportSafe(executor);
                                            }
                                            executor.sendMessage(LowdFX.serverMessage(
                                                    Component.text("Du wurdest zu " + target.getName() + "'s Home " + homeName + " teleportiert!", NamedTextColor.GREEN)));
                                            Utilities.positiveSound(executor);
                                            return 1;
                                        })
                                )
                        )
                )
                // /home set_other <player> <homeName>: Setzt das Home eines anderen Spielers an der aktuellen Position des Admins
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("set_other")
                        .requires(source -> Perms.check(source, Perms.Perm.HOME_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    HomeManager.HOMES.keySet().forEach(uuid -> {
                                        String name = Bukkit.getOfflinePlayer(uuid).getName();
                                        if (name != null) {
                                            builder.suggest(name);
                                        }
                                    });
                                    return builder.buildFuture();
                                })
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("homeName", StringArgumentType.word())
                                        .executes(context -> {
                                            String targetName = context.getArgument("player", String.class);
                                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                                            String homeName = context.getArgument("homeName", String.class);

                                            Player executor = (Player) context.getSource().getExecutor();
                                            if (executor == null) {
                                                context.getSource().getSender().sendMessage(
                                                        LowdFX.serverMessage(Component.text("Fehler: Kein Executor gefunden!", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            Location loc = executor.getLocation();
                                            if (loc == null) {
                                                executor.sendMessage(LowdFX.serverMessage(Component.text("Fehler: Deine Location ist ungültig!", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            Map<String, SimpleLocation> homes = HomeManager.get(target.getUniqueId());
                                            if (homes == null) {
                                                HomeManager.add(target.getUniqueId());
                                                homes = HomeManager.get(target.getUniqueId());
                                            }

                                            homes.put(homeName, SimpleLocation.ofLocation(loc));

                                            if (target.isOnline()) {
                                                ((Player) target).sendMessage(LowdFX.serverMessage(
                                                        Component.text("Dein Home " + homeName + " wurde von " + executor.getName() + " gesetzt!", NamedTextColor.GREEN)));
                                            }
                                            executor.sendMessage(LowdFX.serverMessage(
                                                    Component.text("Du hast " + target.getName() + "'s Home " + homeName + " gesetzt!", NamedTextColor.GREEN)));
                                            Utilities.positiveSound(executor);
                                            return 1;
                                        })
                                )

                        )
                )
                // /home remove_other <player> <homeName>: Entfernt das Home eines anderen Spielers
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove_other")
                        .requires(source -> Perms.check(source, Perms.Perm.HOME_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    HomeManager.HOMES.keySet().forEach(uuid -> {
                                        String name = Bukkit.getOfflinePlayer(uuid).getName();
                                        if (name != null) {
                                            builder.suggest(name);
                                        }
                                    });
                                    return builder.buildFuture();
                                })
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("homeName", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            String targetName = context.getArgument("player", String.class);
                                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
                                            Map<String, SimpleLocation> homes = HomeManager.get(target.getUniqueId());
                                            if (homes != null) {
                                                homes.keySet().forEach(builder::suggest);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            // Kontext abrufen
                                            CommandSourceStack source = context.getSource();

                                            // Spielername und Home-Name abrufen
                                            String targetName = context.getArgument("player", String.class);
                                            String homeName = context.getArgument("homeName", String.class);

                                            // OfflinePlayer ermitteln (Bukkit.getOfflinePlayer() liefert immer ein Objekt)
                                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                                            // Homes des Zielspielers abrufen
                                            Map<String, SimpleLocation> homes = HomeManager.get(target.getUniqueId());
                                            if (homes == null || homes.isEmpty() || !homes.containsKey(homeName)) {
                                                source.getSender();
                                                source.getSender().sendMessage(
                                                        LowdFX.serverMessage(Component.text(
                                                                "Der Spieler " + target.getName() + " hat kein Home mit dem Namen " + homeName + "!",
                                                                NamedTextColor.RED)));
                                                return 1;
                                            }

                                            // Home entfernen
                                            homes.remove(homeName);

                                            // Sende Nachricht an den Zielspieler, falls er online ist
                                            if (target.isOnline() && target.getPlayer() != null) {
                                                ((Player) target).sendMessage(
                                                        LowdFX.serverMessage(Component.text(
                                                                "Dein Home " + homeName + " wurde von einem Admin entfernt!",
                                                                NamedTextColor.RED)));
                                            }

                                            // Executor abrufen; da durch .requires() sichergestellt ist, dass ein Spieler vorhanden ist, kann hier direkt genutzt werden
                                            Player executor = (Player) source.getSender();
                                            executor.sendMessage(
                                                    LowdFX.serverMessage(Component.text(
                                                            "Du hast " + target.getName() + "'s Home " + homeName + " entfernt!",
                                                            NamedTextColor.GREEN)));
                                            Utilities.positiveSound(executor);
                                            return 1;
                                        })

                                )
                        )
                )
                .build();
    }
}
