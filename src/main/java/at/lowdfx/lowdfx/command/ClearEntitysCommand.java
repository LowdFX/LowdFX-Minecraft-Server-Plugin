package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public final class ClearEntitysCommand {
    static {
        CommandHelp.register("clear",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/clear items/monsters"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du alle herumliegenden Items löschen.<newline></gray>" +
                                "<yellow>· /clear items</yellow>"),
                // Zusätzlicher Admin-Teil (optional)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du alle feindlichen Kreaturen löschen.<newline></gray>" +
                        "<yellow>· /clear monsters</yellow>"),
                Perms.Perm.CLEARITEMS.getPermission(),
                Perms.Perm.CLEARMONSTERS.getPermission()); // Keine separate Admin-Permission
    }


    public static LiteralCommandNode<CommandSourceStack> command() {
        LiteralArgumentBuilder<CommandSourceStack> clearBuilder = LiteralArgumentBuilder.literal("clear");
        clearBuilder.requires(source -> Perms.check(source, Perms.Perm.CLEARITEMS) || Perms.check(source, Perms.Perm.CLEARMONSTERS) && source.getExecutor() instanceof Player);
        // Sub-Befehl: /clear monsters - löscht alle feindlichen Kreaturen (Monster)
        clearBuilder.then(LiteralArgumentBuilder.<CommandSourceStack>literal("monsters")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    CommandSender sender = source.getSender();

                    // Berechtigungsprüfung
                    if (!Perms.check(sender, Perms.Perm.CLEARMONSTERS)) {
                        sender.sendMessage(LowdFX.serverMessage(Component.text("Du hast keine Berechtigung, diesen Befehl zu nutzen.")));
                        return 0;
                    }

                    int removedCount = 0;
                    for (World world : Bukkit.getWorlds()) {
                        for (Entity entity : world.getEntities()) {
                            if (entity instanceof Monster) {
                                entity.remove();
                                removedCount++;
                            }
                        }
                    }

                    sender.sendMessage(LowdFX.serverMessage(Component.text("Es wurden " + removedCount + " feindliche Kreaturen entfernt.")));
                    return 1;
                }));

        // Sub-Befehl: /clear items - löscht alle am Boden liegenden Items
        clearBuilder.then(LiteralArgumentBuilder.<CommandSourceStack>literal("items")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    CommandSender sender = source.getSender();

                    // Berechtigungsprüfung
                    if (!Perms.check(sender, Perms.Perm.CLEARITEMS)) {
                        sender.sendMessage(LowdFX.serverMessage(Component.text("Du hast keine Berechtigung, diesen Befehl zu nutzen.")));
                        return 0;
                    }

                    int removedCount = 0;
                    for (World world : Bukkit.getWorlds()) {
                        for (Entity entity : world.getEntities()) {
                            if (entity instanceof Item) {
                                entity.remove();
                                removedCount++;
                            }
                        }
                    }

                    sender.sendMessage(LowdFX.serverMessage(Component.text("Es wurden " + removedCount + " Items vom Boden entfernt.")));
                    return 1;
                }));

        return clearBuilder.build();
    }
}
