package at.lowdfx.lowdfx.command.moderation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.moderation.MuteManager;
import at.lowdfx.lowdfx.util.Perms;
import com.marcpg.libpg.data.time.Time;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class MuteCommands {
    public static final List<String> TIME_UNITS = List.of("s", "min", "h", "d", "wk", "mo");

    public static LiteralCommandNode<CommandSourceStack> muteCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("mute")
                .requires(source -> Perms.check(source, Perms.Perm.MUTE))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            List<UUID> excluded = new ArrayList<>(MuteManager.MUTES.keySet());
                            if (context.getSource().getSender() instanceof Player player)
                                excluded.add(player.getUniqueId());

                            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                                if (!excluded.contains(player.getUniqueId()))
                                    builder.suggest(player.getName());
                            }
                            return builder.buildFuture();
                        })
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("time", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    String input = Objects.requireNonNullElse(builder.getInput().split(" ")[0], "");
                                    TIME_UNITS.forEach(unit -> builder.suggest(input.replaceAll("[^-\\d.]+", "") + unit));
                                    return builder.buildFuture();
                                })
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            CommandSender source = context.getSource().getSender();
                                            Time time = Time.parse(context.getArgument("time", String.class));

                                            if (time.get() <= 0) {
                                                source.sendMessage(LowdFX.serverMessage(Component.text("Die Zeit " + time.getPreciselyFormatted() + " ist nicht gültig!", NamedTextColor.RED)));
                                                return 1;
                                            }
                                            if (time.get() > MuteManager.MAX_TIME.get()) {
                                                source.sendMessage(LowdFX.serverMessage(Component.text("Die Zeit " + time.getPreciselyFormatted() + " ist zu hoch! Limit ist " + MuteManager.MAX_TIME.getPreciselyFormatted() + ".", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            MuteManager.mute(Bukkit.getOfflinePlayer(context.getArgument("player", String.class)).getUniqueId(), source, context.getArgument("reason", String.class), time);
                                            return 1;
                                        })
                                )
                        )
                )
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> unmuteCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("unmute")
                .requires(source -> Perms.check(source, Perms.Perm.MUTE))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            String sourceName = context.getSource().getSender() instanceof Player player ? player.getName() : "";
                            MuteManager.MUTES.keySet().stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                                    .filter(s -> !sourceName.equals(s))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            MuteManager.unmute(Bukkit.getOfflinePlayer(context.getArgument("player", String.class)).getUniqueId(), context.getSource().getSender());
                            return 1;
                        })
                )
                .build();
    }
}
