package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@SuppressWarnings("UnstableApiUsage")
public final class TimeCommands {
    public static LiteralCommandNode<CommandSourceStack> dayCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("day")
                .requires(source -> Perms.check(source, Perms.Perm.TIME))
                .executes(context -> {
                    context.getSource().getLocation().getWorld().setTime(1000); // 1000 = Midnight
                    context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Set time to day!", NamedTextColor.GREEN)));
                    return 1;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> nightCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("night")
                .requires(source -> Perms.check(source, Perms.Perm.TIME))
                .executes(context -> {
                    context.getSource().getLocation().getWorld().setTime(18000); // 18000 = Midnight
                    context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Set time to night!", NamedTextColor.GREEN)));
                    return 1;
                })
                .build();
    }
}
