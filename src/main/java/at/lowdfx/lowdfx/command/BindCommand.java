package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.managers.BindManager;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class BindCommand {

    static {
        CommandHelp.register("bind",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/bind help"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du einem Item in der Hand einen Befehl zuweisen.<newline></gray>" +
                                "<yellow>· /bind <name> <command><newline></yellow>" +
                                "<yellow>· /bind delete <name></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.BIND.getPermission(),
                null); // Keine separate Admin-Permission
    }
    @SuppressWarnings("UnstableApiUsage")
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("bind")
                .requires(source -> source.getExecutor() instanceof Player && Perms.check(source, Perms.Perm.BIND))
                // Lösch-Zweig: /bind delete <name>
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("delete")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (String name : BindManager.getBindings().keySet()) {
                                        builder.suggest(name);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String name = context.getArgument("name", String.class);
                                    Player player = (Player) context.getSource().getSender();
                                    if (BindManager.getBinding(name) == null) {
                                        player.sendMessage(LowdFX.serverMessage(
                                                Component.text("Es existiert kein Binding mit dem Namen '" + name + "'.", NamedTextColor.RED)));
                                        return 1;
                                    }
                                    // Entferne das Binding global
                                    BindManager.removeBinding(name);
                                    // Entferne das Binding-Tag von allen Items im Inventar des Spielers, die dieses Binding tragen
                                    for (ItemStack item : player.getInventory().getContents()) {
                                        String itemId = BindManager.getBindId(item);
                                        if (itemId != null && itemId.equals(name)) {
                                            ItemMeta meta = item.getItemMeta();
                                            if (meta != null) {
                                                meta.getPersistentDataContainer().remove(BindManager.BIND_KEY);
                                                item.setItemMeta(meta);
                                            }
                                        }
                                    }
                                    player.sendMessage(LowdFX.serverMessage(
                                            Component.text("Das Binding '" + name + "' wurde entfernt.", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                // Erstellungs-Zweig: /bind <name> <command>
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("command", greedyString())
                                .executes(context -> {
                                    String name = context.getArgument("name", String.class);
                                    String cmd = context.getArgument("command", String.class);
                                    Player player = (Player) context.getSource().getSender();
                                    String world = player.getWorld().getName();
                                    ItemStack itemInHand = player.getInventory().getItemInMainHand();
                                    if (itemInHand == null || itemInHand.getType().isAir()) {
                                        player.sendMessage(LowdFX.serverMessage(
                                                Component.text("Du musst ein gültiges Item in der Hand halten!", NamedTextColor.RED)));
                                        return 1;
                                    }
                                    BindManager.markAsBindItem(itemInHand, name, cmd, world);
                                    player.sendMessage(LowdFX.serverMessage(
                                            Component.text("Das Binding '" + name + "' mit dem Command '" + cmd + "' wurde in der Welt '" + world + "' gesetzt.", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .build();
    }
}
