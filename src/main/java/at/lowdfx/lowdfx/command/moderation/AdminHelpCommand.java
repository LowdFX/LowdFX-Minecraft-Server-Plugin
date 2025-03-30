package at.lowdfx.lowdfx.command.moderation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.Utilities;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AdminHelpCommand {

    static {
        CommandHelp.register("adminhelp",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/adminhelp <message>"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du eine Nachricht an alle Admins senden. " +
                                "Dein Name und deine Nachricht werden angezeigt, und die Admins erhalten einen Ton. " +
                                "Falls kein Admin online ist, wirst du informiert.<newline></gray>" +
                                "<yellow>· /adminhelp <message></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.ADMINHELP_SEND.getPermission(),
                null); // Keine separate Admin-Permission
    }

    @SuppressWarnings("UnstableApiUsage")
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("adminhelp")
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("message", StringArgumentType.greedyString())
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            String messageText = context.getArgument("message", String.class);

                            // Prüfe, ob der Absender die Permission zum Senden besitzt.
                            if (!Perms.check(sender, Perms.Perm.ADMINHELP_SEND)) {
                                sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize(
                                        "<red>Du hast keine Berechtigung, eine AdminHelp-Nachricht zu senden.</red>")));
                                return 0;
                            }

                            // Formatierte Nachricht: Absendername + Nachricht
                            Component adminMessage = MiniMessage.miniMessage().deserialize(
                                    "<gold>[AdminHelp] <gray>" + sender.getName() + ": <white>" + messageText);

                            boolean adminFound = false;

                            // Sende die Nachricht an alle Online-Spieler, die die Empfangs-Permission besitzen.
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (Perms.check(player, Perms.Perm.ADMINHELP_RECEIVE)) {
                                    player.sendMessage(LowdFX.serverMessage(adminMessage));
                                    // Spiele einen Ton ab, wenn ein Admin die Nachricht erhält.
                                    Utilities.positiveSound(player);
                                    adminFound = true;
                                }
                            }

                            // Falls kein Admin online ist, informiere den Absender.
                            if (!adminFound) {
                                sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize(
                                        "<red>Leider ist aktuell kein Admin online.</red>")));
                            } else {
                                sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize(
                                        "<green>Deine AdminHelp-Nachricht wurde an die Admins gesendet.</green>")));
                            }
                            return 1;
                        })
                )
                .build();
    }
}
