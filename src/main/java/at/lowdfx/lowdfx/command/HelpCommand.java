package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;

public final class HelpCommand {
    @SuppressWarnings("UnstableApiUsage")
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("help")
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    sender.sendMessage(LowdFX.serverMessage(CommandHelp.getAllHelp(sender)));
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("command", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            CommandSender sender = context.getSource().getSender();
                            for (CommandHelp.HelpEntry entry : CommandHelp.getHelpEntries()) {
                                if (sender.hasPermission(entry.getPermission())) {
                                    builder.suggest(entry.getCommand());
                                }
                            }
                            return CompletableFuture.completedFuture(builder.build());
                        })
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            String cmd = context.getArgument("command", String.class);
                            CommandHelp.HelpEntry entry = CommandHelp.getHelpEntry(cmd);
                            if (entry != null) {
                                if (sender.hasPermission(entry.getPermission())) {
                                    Component header = MiniMessage.miniMessage().deserialize("<gold><bold>Hilfe zu /" + entry.getCommand() + ":</bold></gold>\n");
                                    Component detailed = entry.getDetailed();
                                    // Falls ein Admin-Text vorhanden ist und der Sender auch die Admin-Permission besitzt, anhängen.
                                    if (entry.getAdminDetailed() != null && entry.getAdminPermission() != null &&
                                            !entry.getAdminPermission().isEmpty() &&
                                            sender.hasPermission(entry.getAdminPermission())) {
                                        detailed = detailed.append(MiniMessage.miniMessage().deserialize("<newline>")).append(entry.getAdminDetailed());
                                    }
                                    sender.sendMessage(LowdFX.serverMessage(header.append(detailed)));
                                } else {
                                    sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize(
                                            "<red>Du besitzt nicht die erforderliche Permission für diesen Command.</red>")));
                                }
                            } else {
                                sender.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize(
                                        "<red>Für diesen Command ist keine Hilfe registriert.</red>")));
                            }
                            return 1;
                        })
                )
                .build();
    }
}
