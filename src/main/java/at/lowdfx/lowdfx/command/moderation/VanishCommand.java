package at.lowdfx.lowdfx.command.moderation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.command.util.UtilityCommands;
import at.lowdfx.lowdfx.managers.moderation.VanishManager;
import at.lowdfx.lowdfx.util.Perms;
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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class VanishCommand {

    static {
        CommandHelp.register("vanish",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/help vanish"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du dich oder einen Spieler in vanish versetzen.<newline></gray>" +
                                "<yellow>· /vanish</yellow>" +
                                "<yellow>· /vanish <player></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.VANISH.getPermission(),
                null); // Keine separate Admin-Permission
    }

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("vanish")
                .requires(source -> Perms.check(source, Perms.Perm.VANISH))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        sender.sendMessage(LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
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
                            if (VanishManager.getVanishedPlayers().isEmpty()) {
                                sender.sendMessage(LowdFX.serverMessage(Component.text("Es gibt keine vanished Spieler.", NamedTextColor.GREEN)));
                            } else {
                                sender.sendMessage(LowdFX.serverMessage(Component.text("Vanished Spieler:", NamedTextColor.GREEN)));

                                for (UUID uuid : VanishManager.getVanishedPlayers()) {
                                    Player vanishedPlayer = Bukkit.getPlayer(uuid);
                                    if (vanishedPlayer != null) {
                                        sender.sendMessage(LowdFX.serverMessage(Component.text("- ").append(vanishedPlayer.name()).color(NamedTextColor.GREEN)));
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
                                Bukkit.getServer().sendMessage(Utilities.joinMessage(player));
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("quit")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .executes(context -> {
                            if (context.getSource().getExecutor() instanceof Player player)
                                Bukkit.getServer().sendMessage(Utilities.quitMessage(player));
                            return 1;
                        })
                )
                .build();
    }

    public static void vanish(Player player, boolean state, @Nullable CommandSender sender) {
        UtilityCommands.setFly(player, state);


        if (state) {
            player.setMetadata("vanished", new FixedMetadataValue(LowdFX.PLUGIN, true));
            VanishManager.makePlayerInvisible(player);
            VanishManager.applyNametag(player);
        } else {
            player.removeMetadata("vanished", LowdFX.PLUGIN);
            VanishManager.makePlayerVisible(player);
            VanishManager.resetNametag(player);
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
            if (Perms.check(p, Perms.Perm.VANISH) && !p.equals(player))
                p.sendMessage(adminMessage);
        });

        if (state) {
            Bukkit.getServer().sendMessage(Utilities.quitMessage(player));
        } else {
            Bukkit.getServer().sendMessage(Utilities.joinMessage(player));
        }
    }
}
