package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.moderation.Mute;
import at.lowdfx.lowdfx.util.Time;
import at.lowdfx.lowdfx.util.Utilities;
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

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public final class MuteCommands {
    public static final List<String> TIME_UNITS = List.of("s", "min", "h", "d", "wk", "mo");
    public static final Map<UUID, Mute> MUTES = new HashMap<>();
    public static final Time MAX_TIME = new Time(1, Time.Unit.YEARS);

    public static LiteralCommandNode<CommandSourceStack> muteCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("mute")
                .requires(source -> source.getSender().hasPermission("poo.mute"))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            List<UUID> excluded = new ArrayList<>(MUTES.keySet());
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
                                            OfflinePlayer target = Bukkit.getOfflinePlayer(context.getArgument("player", String.class));

                                            Time time = Time.parse(context.getArgument("time", String.class));
                                            String reason = context.getArgument("reason", String.class);

                                            if (time.get() <= 0) {
                                                source.sendMessage(Component.text("Die Zeit " + time.getPreciselyFormatted() + " ist nicht gültig!", NamedTextColor.RED));
                                                return 1;
                                            }
                                            if (time.get() > MAX_TIME.get()) {
                                                source.sendMessage(Component.text("Die Zeit " + time.getPreciselyFormatted() + " ist zu hoch! Limit ist " + MAX_TIME.getPreciselyFormatted() + ".", NamedTextColor.RED));
                                                return 1;
                                            }
                                            if (time.get() <= 0) {
                                                source.sendMessage(Component.text("Der Spieler " + target.getName() + " ist schon stumm geschaltet!", NamedTextColor.RED));
                                                return 1;
                                            }

                                            MUTES.put(target.getUniqueId(), new Mute(target.getUniqueId(), source instanceof Player player ? player.getUniqueId() : null, time, Utilities.currentTimeSecs(), reason));

                                            if (target.isOnline()) {
                                                Objects.requireNonNull(target.getPlayer()).sendMessage(Component.text("Du bist ab jetzt auf diesem Server stumm geschaltet!", NamedTextColor.RED)
                                                        .appendNewline()
                                                        .append(Component.text("Verfall: ", NamedTextColor.GRAY).append(Component.text("In " + time.getPreciselyFormatted(), NamedTextColor.BLUE)))
                                                        .appendNewline()
                                                        .append(Component.text("Grund: ", NamedTextColor.GRAY).append(Component.text(reason, NamedTextColor.BLUE))));
                                            }
                                            source.sendMessage(LowdFX.serverMessage(Component.text(target.getName() + " erfolgreich für " + time.getPreciselyFormatted() + " stumm geschaltet mit dem Grund: \"" + reason + "\"", NamedTextColor.YELLOW)));
                                            LowdFX.LOG.info("{} muted {} for {} with the reason: \"{}\"!", source.getName(), target.getName(), time.getPreciselyFormatted(), reason);
                                            return 1;
                                        })
                                )
                        )
                )
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> unmuteCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("unmute")
                .requires(source -> source.getSender().hasPermission("poo.mute"))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            String sourceName = context.getSource().getSender() instanceof Player player ? player.getName() : "";
                            MUTES.keySet().stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                                    .filter(s -> !sourceName.equals(s))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            CommandSender source = context.getSource().getSender();
                            OfflinePlayer target = Bukkit.getOfflinePlayer(context.getArgument("player", String.class));

                            if (!MUTES.containsKey(target.getUniqueId())) {
                                source.sendMessage(Component.text("Der Spieler " + target.getName() + " ist nicht stumm geschaltet!", NamedTextColor.RED));
                                return 1;
                            }

                            MUTES.remove(target.getUniqueId());

                            source.sendMessage(LowdFX.serverMessage(Component.text("Erfolgreich " + target.getName() + "s Stummschaltung entfernt!", NamedTextColor.YELLOW)));
                            LowdFX.LOG.info("{} unmuted {}!", source.getName(), target.getName());
                            return 1;
                        })
                )
                .build();
    }
}
