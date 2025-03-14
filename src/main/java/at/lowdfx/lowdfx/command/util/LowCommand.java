package at.lowdfx.lowdfx.command.util;

import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;

@SuppressWarnings("UnstableApiUsage")
public final class LowCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("low")
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                        .requires(source -> Perms.check(source, Perms.Perm.INFO))
                        .executes(context -> {
                            context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize("""
                                    <yellow><b>MC-Version:</b> <gold>1.21.3+
                                    <yellow><b>Plugin-Version:</b> <gold>1.0.0
                                    <yellow><b>Author:</b> <gold>LowdFX"""));
                            return 1;
                        })
                )
                .build();
    }
}
