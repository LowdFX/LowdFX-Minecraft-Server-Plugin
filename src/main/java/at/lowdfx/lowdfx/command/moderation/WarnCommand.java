package at.lowdfx.lowdfx.command.moderation;

import at.lowdfx.lowdfx.LowdFX;
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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
public final class WarnCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("warn")
                .requires(source -> Perms.check(source, Perms.Perm.WARN))
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
                        .requires(source -> Perms.check(source, Perms.Perm.WARN_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("reason", StringArgumentType.greedyString())
                                .executes(context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    Collection<PlayerProfile> targets = context.getArgument("players", PlayerProfileListResolver.class).resolve(context.getSource());
                                    String reason = context.getArgument("reason", String.class);

                                    for (PlayerProfile t : targets) {
                                        WarnManager.warn(t.getId(), sender, reason);
                                        sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize("<green>Spieler <b>" + t.getName() + "</b> wurde verwarnt.")));
                                        sender.sendMessage(LowdFX.serverMessage(Component.text("Aktuelle Punkte: ", NamedTextColor.GREEN)
                                                .append(Component.text(WarnManager.amount(t.getId()), NamedTextColor.RED, TextDecoration.BOLD))));
                                    }
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
                // Neuer Subcommand: /warn remove
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                        .requires(source -> Perms.check(source, Perms.Perm.WARN_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
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
                // Neuer Subcommand: /warn removeall
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("removeall")
                        .requires(source -> Perms.check(source, Perms.Perm.WARN_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
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
