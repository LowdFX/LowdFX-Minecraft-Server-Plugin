package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.managers.PlaytimeManager;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public final class PlaytimeCommand {

    static {
        CommandHelp.register("playtime",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/playtime"),
                // Ausführliche Beschreibung für normale Spieler
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du deine gesamte Spielzeit anzeigen lassen.<newline>" +
                                "<yellow>· /playtime</yellow>"),
                // Zusätzlicher Admin-Teil (optional)
                MiniMessage.miniMessage().deserialize(
                        "<yellow>· /playtime <player></yellow>"),
                // Basis-Permission
                Perms.Perm.PLAYTIME.getPermission(),
                // Admin-Permission (hier wird der Admin-Text nur angezeigt, wenn der Spieler diese besitzt)
                Perms.Perm.PLAYTIME_ADMIN.getPermission());
    }

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("playtime")
                .requires(source -> Perms.check(source, Perms.Perm.PLAYTIME))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
                        return 1;
                    }

                    player.sendMessage(LowdFX.serverMessage(Component.text("Deine Spielzeit: ", NamedTextColor.YELLOW).append(Component.text(PlaytimeManager.PLAYTIMES.get(player.getUniqueId()).totalTime().getPreciselyFormatted(), NamedTextColor.WHITE))));
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                        .requires(source -> Perms.check(source, Perms.Perm.PLAYTIME_ADMIN))
                        .executes(context -> {
                            Player player = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                            context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Spielzeit von " + player.getName() + ": ", NamedTextColor.YELLOW).append(Component.text(PlaytimeManager.PLAYTIMES.get(player.getUniqueId()).totalTime().getPreciselyFormatted(), NamedTextColor.WHITE))));
                            return 1;
                        })
                )
                .build();
    }
}
