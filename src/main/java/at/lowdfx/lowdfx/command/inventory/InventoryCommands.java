package at.lowdfx.lowdfx.command.inventory;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.kit.Items;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.Utilities;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.ReferencingInventory;
import xyz.xenondevs.invui.window.Window;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class InventoryCommands {

    static {
        CommandHelp.register("anvil",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/anvil"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du einen Amboss öffnen.<newline></gray>" +
                                "<yellow>· /anvil</yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.ANVIL.getPermission(),
                null); // Keine separate Admin-Permission
    }
    static {
        CommandHelp.register("trash",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/trash"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du einen Mülleimer öffnen.<newline></gray>" +
                                "<yellow>· /trash</yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.TRASH.getPermission(),
                null); // Keine separate Admin-Permission
    }

    static {
        CommandHelp.register("workbench",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/workbench"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du eine Werkbank öffnen.<newline></gray>" +
                                "<yellow>· /workbench</yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.WORKBENCH.getPermission(),
                null); // Keine separate Admin-Permission
    }

    static {
        CommandHelp.register("invsee",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/invsee"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du ein Inventar öffnen.<newline></gray>" +
                                "<yellow>· /invsee <player></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.INVSEE.getPermission(),
                null); // Keine separate Admin-Permission
    }

    static {
        CommandHelp.register("endersee",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/endersee"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du ein Inventar öffnen.<newline></gray>" +
                                "<yellow>· /endersee <player></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.ENDERSEE.getPermission(),
                null); // Keine separate Admin-Permission
    }

    public static LiteralCommandNode<CommandSourceStack> anvilCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("anvil")
                .requires(source -> Perms.check(source, Perms.Perm.ANVIL) && source.getExecutor() instanceof Player)
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                    player.openAnvil(player.getLocation(), true);
                    player.sendMessage(LowdFX.serverMessage(Component.text("Der Amboss öffnet sich!", NamedTextColor.GREEN)));
                    Utilities.positiveSound(player);
                    return 1;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> enderseeCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("endersee")
                .requires(source -> Perms.check(source, Perms.Perm.ENDERSEE) && source.getExecutor() instanceof Player)
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                    player.openInventory(player.getEnderChest());
                    player.sendMessage(LowdFX.serverMessage(Component.text("Deine Enderchest wurde geöffnet!", NamedTextColor.GREEN)));
                    Utilities.positiveSound(player);
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();

                            player.openInventory(target.getEnderChest());
                            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast die Enderchest von " + target.getName() + " geöffnet!", NamedTextColor.GREEN)));
                            Utilities.positiveSound(player);
                            return 1;
                        })
                )
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> invseeCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("invsee")
                .requires(source -> Perms.check(source, Perms.Perm.INVSEE) && source.getExecutor() instanceof Player)
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                            PlayerInventory inv = target.getInventory();

                            Gui gui = Gui.normal().setStructure(
                                            "# h c l b # # o #", // h = Helmet
                                            "# # # # # # # # #", // c = Chestplate
                                            "I I I I I I I I I", // l = Leggings
                                            "I I I I I I I I I", // b = Boots
                                            "I I I I I I I I I", // o = Offhand
                                            "I I I I I I I I I") // I = Inventory
                                    .addIngredient('#', Items.BLACK_BACKGROUND)
                                    .addIngredient('h', new Items.LiveItem(target, PlayerInventory::setHelmet, () -> Objects.requireNonNullElse(inv.getHelmet(), ItemStack.empty()), false))
                                    .addIngredient('c', new Items.LiveItem(target, PlayerInventory::setChestplate, () -> Objects.requireNonNullElse(inv.getChestplate(), ItemStack.empty()), false))
                                    .addIngredient('l', new Items.LiveItem(target, PlayerInventory::setLeggings, () -> Objects.requireNonNullElse(inv.getLeggings(), ItemStack.empty()), false))
                                    .addIngredient('b', new Items.LiveItem(target, PlayerInventory::setBoots, () -> Objects.requireNonNullElse(inv.getBoots(), ItemStack.empty()), false))
                                    .addIngredient('o', new Items.LiveItem(target, PlayerInventory::setItemInOffHand, inv::getItemInOffHand, true))
                                    .addIngredient('I', ReferencingInventory.fromContents(inv))
                                    .build();

                            Window window = Window.single().setTitle("Inventar von " + target.getName()).setGui(gui).setViewer(player).build();
                            window.open();

                            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast das Inventar von " + target.getName() + " geöffnet!", NamedTextColor.GREEN)));
                            Utilities.positiveSound(player);
                            return 1;
                        })
                )
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> trashCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("trash")
                .requires(source -> Perms.check(source, Perms.Perm.TRASH) && source.getExecutor() instanceof Player)
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                    player.openInventory(Bukkit.createInventory(player, 36, Component.text("Mülleimer").decorate(TextDecoration.BOLD)));
                    player.sendMessage(LowdFX.serverMessage(Component.text("Der Mülleimer öffnet sich!", NamedTextColor.GREEN)));
                    Utilities.positiveSound(player);
                    return 1;
                })
                .build();
    }

    public static LiteralCommandNode<CommandSourceStack> workbenchCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("workbench")
                .requires(source -> Perms.check(source, Perms.Perm.WORKBENCH) && source.getExecutor() instanceof Player)
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                    player.openWorkbench(player.getLocation(), true);
                    player.sendMessage(LowdFX.serverMessage(Component.text("Die Werkbank öffnet sich!", NamedTextColor.GREEN)));
                    Utilities.positiveSound(player);
                    return 1;
                })
                .build();
    }
}
