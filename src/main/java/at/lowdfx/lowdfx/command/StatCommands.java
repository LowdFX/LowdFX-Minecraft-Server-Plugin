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
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class StatCommands {
    public static final String FEED_PERMISSION = "lowdfx.feed";
    public static final String HEAL_PERMISSION = "lowdfx.heal";

    public static LiteralCommandNode<CommandSourceStack> feedCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("feed")
                .requires(source -> source.getSender().hasPermission(FEED_PERMISSION))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                        return 1;
                    }

                    feed(player);
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("players", ArgumentTypes.players())
                        .executes(context -> {
                            List<Player> players = context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                            players.forEach(StatCommands::feed);
                            context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler hat/haben keinen Hunger mehr!", NamedTextColor.GREEN)));
                            return 1;
                        })
                )
                .build();
    }

    public static void feed(@NotNull Player player) {
        player.setFoodLevel(20);
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.sendMessage(LowdFX.serverMessage(Component.text("Dein Hunger wurde gestillt!", NamedTextColor.GREEN)));
    }

    public static LiteralCommandNode<CommandSourceStack> healCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("heal")
                .requires(source -> source.getSender().hasPermission(HEAL_PERMISSION))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                        return 1;
                    }

                    heal(player);
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("players", ArgumentTypes.players())
                        .executes(context -> {
                            List<Player> players = context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                            players.forEach(StatCommands::heal);
                            context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler wurde(n) geheilt!", NamedTextColor.GREEN)));
                            return 1;
                        })
                )
                .build();
    }

    public static void heal(@NotNull Player player) {
        player.heal(Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getBaseValue());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().getEffectCategory() != PotionEffectType.Category.BENEFICIAL) {
                player.removePotionEffect(effect.getType());
            }
        }
        player.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest geheilt!", NamedTextColor.GREEN)));
    }
}
