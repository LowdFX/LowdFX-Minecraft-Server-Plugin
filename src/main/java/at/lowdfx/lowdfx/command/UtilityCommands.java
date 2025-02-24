package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class UtilityCommands {
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
                            context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text(player.getName() + " kann nun " + (fly ? "" : "nicht mehr") + "fliegen!", NamedTextColor.GREEN)));
                            return 1;
                        })
                )
                .build();
    }

    public static void setFly(@NotNull Player player, boolean fly) {
        player.setAllowFlight(fly);
        player.setFlying(fly);
        player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst nun " + (fly ? "" : "nicht mehr ") + "fliegen!", fly ? NamedTextColor.GREEN : NamedTextColor.RED)));
    }

    public static LiteralCommandNode<CommandSourceStack> gmCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("gm")
                .requires(source -> Perms.check(source, Perms.Perm.GAME_MODE))
                .then(RequiredArgumentBuilder.<CommandSourceStack, GameMode>argument("mode", ArgumentTypes.gameMode())
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) {
                                context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
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
                                    context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler sind jetzt im " + mode.name().toLowerCase() + " Modus!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .then(RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("modeNumber", IntegerArgumentType.integer(0, 3))
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) {
                                context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
                                return 1;
                            }

                            GameMode mode = GameMode.values()[context.getArgument("modeNumber", Integer.class)];
                            setMode(player, mode);
                            return 1;
                        })
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("players", ArgumentTypes.players())
                                .executes(context -> {
                                    List<Player> players = context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                                    GameMode mode = GameMode.values()[context.getArgument("modeNumber", Integer.class)];
                                    players.forEach(p -> setMode(p, mode));
                                    context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler ist/sind jetzt im " + mode.name().toLowerCase() + " Modus!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .build();
    }

    public static void setMode(@NotNull Player player, GameMode mode) {
        player.setGameMode(mode);
        player.sendMessage(LowdFX.serverMessage(Component.text("Du bist nun im " + mode.name().toLowerCase() + " Modus!", NamedTextColor.GREEN)));
    }
}
