package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.PlaytimeManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public final class PlaytimeCommand {
    public static final String PLAYTIME_ADMIN_PERMISSION = "lowdfx.playtime.admin";

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("playtime")
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                        return 1;
                    }

                    player.sendMessage(LowdFX.serverMessage(Component.text("Deine Spielzeit: ", NamedTextColor.YELLOW).append(Component.text(PlaytimeManager.PLAYTIMES.get(player.getUniqueId()).totalTime().getPreciselyFormatted(), NamedTextColor.WHITE))));
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                        .requires(source -> source.getSender().hasPermission(PLAYTIME_ADMIN_PERMISSION))
                        .executes(context -> {
                            Player player = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                            context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Spielzeit von " + player.getName() + ": ", NamedTextColor.YELLOW).append(Component.text(PlaytimeManager.PLAYTIMES.get(player.getUniqueId()).totalTime().getPreciselyFormatted(), NamedTextColor.WHITE))));
                            return 1;
                        })
                )
                .build();
    }
}
