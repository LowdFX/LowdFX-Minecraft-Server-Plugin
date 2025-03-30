package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public final class ScaleCommand {

    static {
        CommandHelp.register("scale",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/scale"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du dich klein machen.<newline></gray>" +
                                "<yellow>· /scale</yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.SCALE.getPermission(),
                null); // Keine separate Admin-Permission
    }

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("scale")
                .requires(source -> Perms.check(source, Perms.Perm.SCALE))
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();
                    if (!(sender instanceof Player player)) {
                        context.getSource().getSender().sendMessage(
                                LowdFX.serverMessage(Component.text("Fehler! Dieser Befehl kann nur von Spielern ausgeführt werden.", NamedTextColor.RED))
                        );
                        return 1;
                    }

                    // Versuche den aktuellen Scale-Wert zu lesen und togglen zwischen 1.0 (normal) und 0.5 (klein)
                    double currentScale;
                    try {
                        currentScale = player.getAttribute(Attribute.SCALE).getBaseValue();
                    } catch (Exception e) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Fehler beim Auslesen deiner Größe.", NamedTextColor.RED)));
                        return 1;
                    }

                    double newScale = (currentScale == 1.0 ? 0.5 : 1.0);

                    try {
                        player.getAttribute(Attribute.SCALE).setBaseValue(newScale);
                    } catch (Exception e) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Fehler beim Setzen der neuen Größe.", NamedTextColor.RED)));
                        return 1;
                    }

                    if (newScale == 0.5) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Du bist jetzt klein.", NamedTextColor.GREEN)));
                    } else {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Du bist jetzt normal groß.", NamedTextColor.GREEN)));
                    }
                    return 1;
                })
                .build();
    }
}
