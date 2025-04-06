package at.lowdfx.lowdfx.command.moderation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.managers.moderation.WarnManager;
import at.lowdfx.lowdfx.util.Perms;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class WarnCommand {

    static {
        CommandHelp.register("warn",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/help warn"),
                // Ausführliche Beschreibung für normale Spieler
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du deine Verwarnungen einsehen.<newline>" +
                                "<yellow>· /warn info</yellow>"),
                // Zusätzlicher Admin-Teil (optional)
                MiniMessage.miniMessage().deserialize(
                        "<yellow>· /warn <player> <reason><newline></yellow>" +
                                "<yellow>· /warn remove <player><newline></yellow>" +
                                "<yellow>· /warn removeall <player></yellow>"),
                // Basis-Permission
                Perms.Perm.WARN.getPermission(),
                // Admin-Permission (hier wird der Admin-Text nur angezeigt, wenn der Spieler diese besitzt)
                Perms.Perm.WARN_ADMIN.getPermission());
    }

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("warn")
                .requires(source -> Perms.check(source, Perms.Perm.WARN))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("players", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                                if (offline.getName() != null)
                                    builder.suggest(offline.getName());
                            }
                            return builder.buildFuture();
                        })
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("reason", StringArgumentType.greedyString())
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    String inputName = context.getArgument("players", String.class);
                                    UUID targetUUID = Bukkit.getOfflinePlayer(inputName).getUniqueId();
                                    String reason = context.getArgument("reason", String.class);

                                    WarnManager.warn(targetUUID, sender, reason);
                                    sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize("<green>Spieler <b>" + inputName + "</b> wurde verwarnt.")));
                                    sender.sendMessage(LowdFX.serverMessage(Component.text("Aktuelle Punkte: ", NamedTextColor.GREEN)
                                            .append(Component.text(WarnManager.amount(targetUUID), NamedTextColor.RED, TextDecoration.BOLD))));
                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) {
                                context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
                                return 1;
                            }

                            player.sendMessage(LowdFX.serverMessage(Component.text("Deine Verwarnungen: ", NamedTextColor.GREEN)
                                    .append(Component.text(WarnManager.amount(player.getUniqueId()), NamedTextColor.RED, TextDecoration.BOLD))));
                            WarnManager.warnList(player.getUniqueId()).forEach(player::sendMessage);
                            return 1;
                        })
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
                                .requires(source -> Perms.check(source, Perms.Perm.WARN_ADMIN))
                                .suggests((ctx, builder) -> {
                                    for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                                        if (offline.getName() != null && WarnManager.amount(offline.getUniqueId()) > 0) {
                                            builder.suggest(offline.getName());
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    Collection<PlayerProfile> targets = context.getArgument("players", PlayerProfileListResolver.class).resolve(context.getSource());
                                    for (PlayerProfile t : targets) {
                                        sender.sendMessage(LowdFX.serverMessage(Component.text("Verwarnungen von " + t.getName() + ": ", NamedTextColor.GREEN)
                                                .append(Component.text(WarnManager.amount(t.getId()), NamedTextColor.RED, TextDecoration.BOLD))));
                                        WarnManager.warnList(t.getId()).forEach(sender::sendMessage);
                                    }
                                    return 1;
                                })
                        )
                )
                // Subcommand: /warn remove
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                        .requires(source -> Perms.check(source, Perms.Perm.WARN_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
                                .suggests((ctx, builder) -> {
                                    for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                                        if (offline.getName() != null && WarnManager.amount(offline.getUniqueId()) > 0) {
                                            builder.suggest(offline.getName());
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    Collection<PlayerProfile> targets = context.getArgument("players", PlayerProfileListResolver.class).resolve(context.getSource());
                                    for (PlayerProfile t : targets) {
                                        if (WarnManager.removeLastWarn(t.getId())) {
                                            sender.sendMessage(LowdFX.serverMessage(Component.text("Die letzte Verwarnung von " + t.getName() + " wurde entfernt.", NamedTextColor.GREEN)));
                                        } else {
                                            sender.sendMessage(LowdFX.serverMessage(Component.text(t.getName() + " hat keine Verwarnungen.", NamedTextColor.RED)));
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
// Subcommand: /warn removeall
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("removeall")
                        .requires(source -> Perms.check(source, Perms.Perm.WARN_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
                                .suggests((ctx, builder) -> {
                                    for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                                        if (offline.getName() != null && WarnManager.amount(offline.getUniqueId()) > 0) {
                                            builder.suggest(offline.getName());
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    Collection<PlayerProfile> targets = context.getArgument("players", PlayerProfileListResolver.class).resolve(context.getSource());
                                    for (PlayerProfile t : targets) {
                                        if (WarnManager.removeAllWarns(t.getId())) {
                                            sender.sendMessage(LowdFX.serverMessage(Component.text("Alle Verwarnungen von " + t.getName() + " wurden entfernt.", NamedTextColor.GREEN)));
                                        } else {
                                            sender.sendMessage(LowdFX.serverMessage(Component.text(t.getName() + " hat keine Verwarnungen.", NamedTextColor.RED)));
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
                .build();
    }
}
