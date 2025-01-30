package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class VanishCommand {
    public static final String VANISH_PERMISSION = "lowdfx.vanish";

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("vanish")
                .requires(source -> source.getSender().hasPermission(VANISH_PERMISSION))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        sender.sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                        return 1;
                    }

                    vanish(player, !player.hasMetadata("vanished"), null);
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                        .executes(context -> {
                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();

                            vanish(target, !target.hasMetadata("vanished"), context.getSource().getSender());
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("list")
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            Set<UUID> vanishedPlayers = LowdFX.INVISIBLE_HANDLER.getVanishedPlayers();
                            if (vanishedPlayers.isEmpty()) {
                                sender.sendMessage(LowdFX.serverMessage(Component.text("Es gibt keine vanished Spieler.", NamedTextColor.GREEN)));
                            } else {
                                sender.sendMessage(LowdFX.serverMessage(Component.text("Vanished Spieler:", NamedTextColor.GREEN)));

                                for (UUID uuid : vanishedPlayers) {
                                    Player vanishedPlayer = Bukkit.getPlayer(uuid);
                                    if (vanishedPlayer != null) {
                                        sender.sendMessage(Component.text("- ").append(vanishedPlayer.name()).color(NamedTextColor.GREEN));
                                    }
                                }
                            }
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("join")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .executes(context -> {
                            if (context.getSource().getExecutor() instanceof Player player)
                                fakeJoin(player);
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("quit")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .executes(context -> {
                            if (context.getSource().getExecutor() instanceof Player player)
                                fakeLeave(player);
                            return 1;
                        })
                )
                .build();
    }

    public static void vanish(Player player, boolean state, @Nullable CommandSender sender) {
        UtilityCommands.setFly(player, state);


        if (state) {
            player.setMetadata("vanished", new FixedMetadataValue(LowdFX.PLUGIN, true));
            LowdFX.INVISIBLE_HANDLER.makePlayerInvisible(player);
        } else {
            player.removeMetadata("vanished", LowdFX.PLUGIN);
            LowdFX.INVISIBLE_HANDLER.makePlayerInvisible(player);
        }
        player.setSleepingIgnored(state);
        player.setCollidable(!state);
        player.setCanPickupItems(!state);
        player.setSilent(state);

        if (state) {
            player.sendMessage(LowdFX.serverMessage(Component.text("Du bist nun vanished!", NamedTextColor.GREEN)));
            if (sender != null)
                sender.sendMessage(LowdFX.serverMessage(Component.text(player.getName() + " ist nun vanished!", NamedTextColor.RED)));
        } else {
            player.sendMessage(LowdFX.serverMessage(Component.text("Du bist nun nicht mehr vanished!", NamedTextColor.RED)));
            if (sender != null)
                sender.sendMessage(LowdFX.serverMessage(Component.text(player.getName() + " ist nun nicht mehr vanished!", NamedTextColor.RED)));
        }

        Component adminMessage = LowdFX.serverMessage(Component.text(player.getName() + (state ? " ist nun vanished!" : " ist nicht mehr vanished."), state ? NamedTextColor.GREEN : NamedTextColor.RED));
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p.hasPermission(VANISH_PERMISSION) && !p.equals(player))
                p.sendMessage(adminMessage);
        });

        if (state) {
            fakeLeave(player);
        } else {
            fakeJoin(player);
        }
    }

    public static void fakeJoin(@NotNull Player player) {
        Bukkit.getServer().sendMessage(Component.text(Objects.requireNonNull(LowdFX.CONFIG.getString("join.welcome")), NamedTextColor.YELLOW).appendSpace().append(player.name().color(NamedTextColor.GOLD)).append(Component.text("!", NamedTextColor.YELLOW)));
    }

    public static void fakeLeave(@NotNull Player player) {
        Bukkit.getServer().sendMessage(Component.text(Objects.requireNonNull(LowdFX.CONFIG.getString("join.quit")), NamedTextColor.YELLOW).appendSpace().append(player.name().color(NamedTextColor.GOLD)).append(Component.text("!", NamedTextColor.YELLOW)));
    }
}
