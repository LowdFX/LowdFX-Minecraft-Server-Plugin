package at.lowdfx.lowdfx.command.util;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.EmojiManager;
import at.lowdfx.lowdfx.util.Configuration;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

@SuppressWarnings("UnstableApiUsage")
public final class LowCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("low")
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                        .requires(source -> Perms.check(source, Perms.Perm.INFO))
                        .executes(context -> {
                            context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize("""
                            <yellow><b>MC-Version:</b> <gold>1.21.4+
                            <yellow><b>Plugin-Version:</b> <gold>1.2.0
                            <yellow><b>Author:</b> <gold>LowdFX"""));
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("reload")
                        .requires(source -> Perms.check(source, Perms.Perm.RELOAD))
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            sender.sendMessage(LowdFX.serverMessage(Component.text("Lade config.yml & emojis.yml neu...", NamedTextColor.YELLOW)));
                            LowdFX.PLUGIN.reloadConfig();
                            Configuration.reload();
                            // Emojis neu laden:
                            EmojiManager.init(LowdFX.PLUGIN);
                            sender.sendMessage(LowdFX.serverMessage(Component.text("Config & Emojis erfolgreich neu geladen.", NamedTextColor.GREEN)));
                            return 1;
                        })
                )
                .build();
    }
}
