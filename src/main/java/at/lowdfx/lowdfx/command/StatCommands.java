package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
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
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class StatCommands {

    static {
        CommandHelp.register("feed",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/feed <player>"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du deine Hungerleiste füllen.<newline></gray>" +
                                "<yellow>· /feed <player></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.FEED.getPermission(),
                null); // Keine separate Admin-Permission
    }

    static {
        CommandHelp.register("heal",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/heal <player>"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du deine Hungerleiste und Lebensleiste füllen.<newline></gray>" +
                                "<yellow>· /heal <player></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.HEAL.getPermission(),
                null); // Keine separate Admin-Permission
    }

    public static LiteralCommandNode<CommandSourceStack> feedCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("feed")
                .requires(source -> Perms.check(source, Perms.Perm.FEED))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
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
        Utilities.positiveSound(player);
    }

    public static LiteralCommandNode<CommandSourceStack> healCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("heal")
                .requires(source -> Perms.check(source, Perms.Perm.HEAL))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
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
        Utilities.positiveSound(player);
    }
}
