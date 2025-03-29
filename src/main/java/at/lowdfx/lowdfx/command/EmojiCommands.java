package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.managers.EmojiManager;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class EmojiCommands {

    @SuppressWarnings("UnstableApiUsage")
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("emojis")
                .requires(source -> Perms.check(source, Perms.Perm.EMOJIS)) // Beispiel-Permission
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();

                    // Überschrift
                    sender.sendMessage(Component.text("ChatEmojis", NamedTextColor.YELLOW));

                    // Emojis aus dem EmojiManager holen und anzeigen
                    for (String code : EmojiManager.getEmojiCodes()) {
                        Component symbol = EmojiManager.getEmojiSymbol(code);
                        // Zeigt z. B. ":heart:" gefolgt vom Emoji an
                        Component line = Component.text(":" + code + ": ", NamedTextColor.GRAY)
                                .append(symbol);
                        sender.sendMessage(line);
                    }

                    return 1;
                })
                .build();
    }
}
