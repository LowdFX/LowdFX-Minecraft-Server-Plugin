package at.lowdfx.lowdfx.command.util;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

@SuppressWarnings("UnstableApiUsage")
public final class TimeCommands {

    static {
        CommandHelp.register("time",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/help time"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesen Befehlen kannst du die Zeit steuern.<newline></gray>" +
                                "<yellow>· /day</yellow>" +
                                "<yellow>· /night</yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.TIME.getPermission(),
                null); // Keine separate Admin-Permission
    }

    public static LiteralCommandNode<CommandSourceStack> dayCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("day")
                .requires(source -> Perms.check(source, Perms.Perm.TIME))
                .executes(context -> {
                    context.getSource().getLocation().getWorld().setTime(1000); // 1000 = Midnight
                    context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Die Zeit ist jetzt Tag!", NamedTextColor.GREEN)));
                    return 1;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> nightCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("night")
                .requires(source -> Perms.check(source, Perms.Perm.TIME))
                .executes(context -> {
                    context.getSource().getLocation().getWorld().setTime(18000); // 18000 = Midnight
                    context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Die Zeit ist jetzt Nacht!", NamedTextColor.GREEN)));
                    return 1;
                })
                .build();
    }
}
