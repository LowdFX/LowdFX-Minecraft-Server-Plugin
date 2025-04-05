package at.lowdfx.lowdfx.command.moderation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public final class SudoCommand {

    static {
        CommandHelp.register("sudo",
                Component.text("Führt einen Befehl als ein Spieler aus."),
                Component.text("Syntax: /sudo <player> <command>\nMit diesem Befehl wird der angegebene Spieler den angegebenen Befehl ausführen, als hätte er ihn selbst eingegeben."),
                Component.empty(),
                Perms.Perm.SUDO.getPermission(),
                null);
    }

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("sudo")
                // Der Executor muss die SUDO-Permission besitzen – egal ob er Console oder ein Spieler ist.
                .requires(source -> Perms.check(source, Perms.Perm.SUDO))
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player",
                                ArgumentTypes.player())
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("command", greedyString())
                                .executes(context -> {
                                    // Ermittele den Zielspieler (als erstes Element der Auswahl)
                                    Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                    String cmd = context.getArgument("command", String.class);
                                    // Lasse den Zielspieler den Befehl ausführen
                                    target.performCommand(cmd);
                                    context.getSource().getSender().sendMessage(LowdFX.serverMessage(
                                            Component.text("Der Befehl '" + cmd + "' wurde von " + target.getName() + " ausgeführt.", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .build();
    }
}
