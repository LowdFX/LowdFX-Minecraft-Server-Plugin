package at.lowdfx.lowdfx.command.util;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.Utilities;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class UtilityCommands {

    static {
        CommandHelp.register("chat",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/chat clear"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du den Chat löschen.<newline></gray>" +
                                "<yellow>· /chat clear</yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.CHAT_CLEAR.getPermission(),
                null); // Keine separate Admin-Permission
    }

    static {
        CommandHelp.register("god",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/god <player>"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du dich oder einen angegebenen Spieler in Godmode versetzen.<newline></gray>" +
                                "<yellow>· /god<newline></yellow>" +
                                "<yellow>· /god <player></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.GOD.getPermission(),
                null); // Keine separate Admin-Permission
    }

    static {
        CommandHelp.register("fly",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/fly <player>"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du dich oder einen angegebenen Spieler in Fly versetzen.<newline></gray>" +
                                "<yellow>· /fly<newline></yellow>" +
                                "<yellow>· /fly <player></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.FLY.getPermission(),
                null); // Keine separate Admin-Permission
    }

    static {
        CommandHelp.register("gm",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/gm <mode> <player>"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du von dir oder einem angegebenem Spieler den Spielmodus ändern.<newline></gray>" +
                                "<yellow>· /gm <mode><newline></yellow>" +
                                "<yellow>· /gm <mode> <player></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.GAME_MODE.getPermission(),
                null); // Keine separate Admin-Permission
    }

    public static LiteralCommandNode<CommandSourceStack> chatClearCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("chat")
                .requires(source -> Perms.check(source, Perms.Perm.CHAT_CLEAR))
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("clear")
                        .executes(context -> {
                            // Sende an jeden Spieler 100 leere Nachrichten, um den Chat "zu leeren"
                            Bukkit.getOnlinePlayers().forEach(player -> {
                                for (int i = 0; i < 200; i++) {
                                    player.sendMessage("");
                                }
                            });
                            context.getSource().getSender().sendMessage(
                                    LowdFX.serverMessage(Component.text("Chat wurde geleert.", NamedTextColor.GREEN))
                            );
                            return 1;
                        })
                )
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> godCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("god")
                .requires(source -> Perms.check(source, Perms.Perm.GOD)) // Stelle sicher, dass es auch eine entsprechende Berechtigung gibt.
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(
                                LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED))
                        );
                        return 1;
                    }
                    // Toggle den Godmode des ausführenden Spielers
                    boolean god = !player.isInvulnerable();
                    setGod(player, god);
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                        .executes(context -> {
                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                            // Toggle den Godmode des Zielspielers
                            boolean god = !target.isInvulnerable();
                            setGod(target, god);
                            context.getSource().getSender().sendMessage(
                                    LowdFX.serverMessage(Component.text(target.getName() + " ist nun " + (god ? "im Godmode" : "nicht mehr im Godmode") + "!", NamedTextColor.GREEN))
                            );
                            return 1;
                        })
                )
                .build();
    }

    public static void setGod(@NotNull Player player, boolean god) {
        // Setzt die Invulnerabilität des Spielers, wodurch er keinen Schaden mehr nimmt, wenn god==true
        player.setInvulnerable(god);
        if (god) {
            player.sendMessage(
                    LowdFX.serverMessage(Component.text("Du bist nun im Godmode!", NamedTextColor.GREEN))
            );
            Utilities.positiveSound(player);
        } else {
            player.sendMessage(
                    LowdFX.serverMessage(Component.text("Du bist nun nicht mehr im Godmode!", NamedTextColor.RED))
            );
            Utilities.negativeSound(player);
        }
    }


    public static LiteralCommandNode<CommandSourceStack> flyCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("fly")
                .requires(source -> Perms.check(source, Perms.Perm.FLY))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
                        return 1;
                    }

                    setFly(player, !player.getAllowFlight());
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                        .executes(context -> {
                            Player player = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                            boolean fly = !player.getAllowFlight();
                            setFly(player, fly);
                            context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text(player.getName() + " kann nun " + (fly ? "" : "nicht mehr") + " fliegen!", NamedTextColor.GREEN)));
                            return 1;
                        })
                )
                .build();
    }

    public static void setFly(@NotNull Player player, boolean fly) {
        player.setAllowFlight(fly);
        player.setFlying(fly);
        if (fly) {
            player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst nun fliegen!", NamedTextColor.GREEN)));
            Utilities.positiveSound(player);
        } else {
            player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst nun nicht mehr fliegen!", NamedTextColor.RED)));
            Utilities.negativeSound(player);
        }
    }

    public static LiteralCommandNode<CommandSourceStack> gmCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("gm")
                .requires(source -> Perms.check(source, Perms.Perm.GAME_MODE))
                .then(RequiredArgumentBuilder.<CommandSourceStack, GameMode>argument("mode", ArgumentTypes.gameMode())
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) {
                                context.getSource().getSender().sendMessage(LowdFX.serverMessage(
                                        Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
                                return 1;
                            }

                            GameMode mode = context.getArgument("mode", GameMode.class);
                            setMode(player, mode);
                            return 1;
                        })
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("players", ArgumentTypes.players())
                                .executes(context -> {
                                    List<Player> players = context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                                    GameMode mode = context.getArgument("mode", GameMode.class);
                                    players.forEach(p -> setMode(p, mode));
                                    context.getSource().getSender().sendMessage(LowdFX.serverMessage(
                                            Component.text(players.size() + " Spieler sind jetzt im " + mode.name().toLowerCase() + " Modus!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .then(RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("modeNumber", IntegerArgumentType.integer(0, 3))
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) {
                                context.getSource().getSender().sendMessage(LowdFX.serverMessage(
                                        Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
                                return 1;
                            }

                            int modeNumber = context.getArgument("modeNumber", Integer.class);
                            GameMode[] modeMapping = { GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE, GameMode.SPECTATOR };
                            GameMode mode = modeMapping[modeNumber];
                            setMode(player, mode);
                            return 1;
                        })
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("players", ArgumentTypes.players())
                                .executes(context -> {
                                    List<Player> players = context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                                    int modeNumber = context.getArgument("modeNumber", Integer.class);
                                    GameMode[] modeMapping = { GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE, GameMode.SPECTATOR };
                                    GameMode mode = modeMapping[modeNumber];
                                    players.forEach(p -> setMode(p, mode));
                                    context.getSource().getSender().sendMessage(LowdFX.serverMessage(
                                            Component.text(players.size() + " Spieler sind jetzt im " + mode.name().toLowerCase() + " Modus!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .build();
    }

    public static void setMode(@NotNull Player player, GameMode mode) {
        player.setGameMode(mode);
        player.sendMessage(LowdFX.serverMessage(Component.text("Du bist nun im " + mode.name().toLowerCase() + " Modus!", NamedTextColor.GREEN)));
        Utilities.positiveSound(player);
    }
}
