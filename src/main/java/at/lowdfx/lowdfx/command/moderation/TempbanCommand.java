package at.lowdfx.lowdfx.command.moderation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.util.Utilities;
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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public final class TempbanCommand {

    static {
        CommandHelp.register("tempban",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/tempban <player> <time> <reason>"),
                // Ausführliche Beschreibung (wird bei /help tempban angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du einen temporären ban vergeben.</gray>" +
                                "<gray>Falls der Spieler nicht online ist, wird nur der Name gebannt und nicht die IP.<newline></gray>" +
                                "<yellow>· /tempban <player> <time> <reason></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.BAN.getPermission(),
                null  // Keine separate Admin-Permission
        );
    }


    @SuppressWarnings("UnstableApiUsage")
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("tempban")
                .requires(source -> Perms.check(source, Perms.Perm.BAN))
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("time", StringArgumentType.word())
                                // Tab-Completion für die Zeiteinheit (d, w, m, y)
                                .suggests((context, builder) -> {
                                    builder.suggest("d");
                                    builder.suggest("w");
                                    builder.suggest("m");
                                    builder.suggest("y");
                                    return CompletableFuture.completedFuture(builder.build());
                                })
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            CommandSender sender = context.getSource().getSender();
                                            Collection<PlayerProfile> targets = context.getArgument("players", PlayerProfileListResolver.class).resolve(context.getSource());
                                            String timeArg = context.getArgument("time", String.class);
                                            String reasonText = context.getArgument("reason", String.class);

                                            Duration duration = parseDuration(timeArg);
                                            if (duration == null) {
                                                sender.sendMessage(LowdFX.serverMessage(
                                                        MiniMessage.miniMessage().deserialize("<red>Ungültiges Zeitformat! Verwende z.B. 6d, 6w, 6m, 6y.")));
                                                return 0;
                                            }

                                            for (PlayerProfile t : targets) {
                                                Component banReason = MiniMessage.miniMessage().deserialize("<red>Temporärer Bann für " + timeArg + " | Grund: " + reasonText);
                                                Utilities.ban(t, banReason, duration, "Tempban Command");
                                                sender.sendMessage(LowdFX.serverMessage(
                                                        MiniMessage.miniMessage().deserialize("<green>Spieler <b>" + t.getName() + "</b> wurde temporär gebannt für " + timeArg + " | Grund: " + reasonText)));
                                            }
                                            return 1;
                                        })
                                )
                        )
                )
                .build();
    }

    /**
     * Parst einen Zeit-String in ein Duration-Objekt.
     * Unterstützte Formate:
     *  - d: Tage
     *  - w: Wochen
     *  - m: Monate (ca. 30 Tage)
     *  - y: Jahre (ca. 365 Tage)
     */
    private static Duration parseDuration(String input) {
        if (input == null || input.length() < 2) return null;
        char timeUnit = input.charAt(input.length() - 1);
        String numberPart = input.substring(0, input.length() - 1);
        int timeValue;
        try {
            timeValue = Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
            return null;
        }
        return switch (timeUnit) {
            case 'd' -> Duration.ofDays(timeValue);
            case 'w' -> Duration.ofDays(timeValue * 7L);
            case 'm' -> Duration.ofDays(timeValue * 30L); // Näherungsweise als 30 Tage
            case 'y' -> Duration.ofDays(timeValue * 365L); // Näherungsweise als 365 Tage
            default -> null;
        };
    }
}
