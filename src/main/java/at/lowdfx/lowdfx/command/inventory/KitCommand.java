package at.lowdfx.lowdfx.command.inventory;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.managers.KitManager;
import at.lowdfx.lowdfx.util.Configuration;
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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("UnstableApiUsage")
public final class KitCommand {

    static {
        // Dynamischer Hilfetext
        String detailedHelp = "<gray>Mit diesem Befehl kannst du Kits verwalten.<newline>";
        if (Configuration.BASIC_STARTERKIT) {
            detailedHelp += "<yellow>· /kit starter</yellow><newline>";
        }
        detailedHelp += "<yellow>· /kit opkit<newline></yellow>" +
                "<yellow>· /kit edit</yellow></gray>";

        CommandHelp.register("kit",
                MiniMessage.miniMessage().deserialize("/kit"),
                MiniMessage.miniMessage().deserialize(detailedHelp),
                MiniMessage.miniMessage().deserialize("<yellow>Admin-Bereich: /kit opkit, /kit edit</yellow>"),
                Perms.Perm.STARTER_KIT.getPermission(),
                Perms.Perm.KIT_ADMIN.getPermission());
    }

    public static LiteralCommandNode<CommandSourceStack> command() {
        LiteralArgumentBuilder<CommandSourceStack> builder = LiteralArgumentBuilder.<CommandSourceStack>literal("kit")
                .requires(source -> source.getExecutor() instanceof Player);

        // /kit starter wird nur registriert, wenn Configuration.BASIC_STARTERKIT aktiviert ist.
        if (Configuration.BASIC_STARTERKIT) {
            builder.then(LiteralArgumentBuilder.<CommandSourceStack>literal("starter")
                    .requires(source -> Perms.check(source, Perms.Perm.STARTER_KIT))
                    .executes(context -> {
                        Player player = (Player) context.getSource().getSender();
                        // Toggle: Falls das Starterkit bereits erhalten wurde, deaktiviere es.
                        if (player.getPersistentDataContainer().has(KitManager.STARTERKIT_KEY)) {
                            player.getPersistentDataContainer().remove(KitManager.STARTERKIT_KEY);
                            // Optional: Hier könnte man auch vorhandene Kit-Items aus dem Inventar entfernen.
                            player.sendMessage(LowdFX.serverMessage(Component.text("Das Starterkit wurde deaktiviert.", NamedTextColor.GREEN)));
                            Utilities.positiveSound(player);
                            return 1;
                        }
                        // Andernfalls aktiviere das Starterkit.
                        KitManager.KITS.get(player.getUniqueId()).give(false, player);
                        player.getPersistentDataContainer().set(KitManager.STARTERKIT_KEY, PersistentDataType.BOOLEAN, true);
                        player.sendMessage(LowdFX.serverMessage(Component.text("Hier ist dein Starterkit!", NamedTextColor.GREEN)));
                        Utilities.positiveSound(player);
                        return 1;
                    })
                    .then(LiteralArgumentBuilder.<CommandSourceStack>literal("player")
                            .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                                    .requires(source -> Perms.check(source, Perms.Perm.KIT_ADMIN))
                                    .executes(context -> {
                                        Player player = (Player) context.getSource().getSender();
                                        Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                                                .resolve(context.getSource()).getFirst();
                                        // Toggle für Admin: Falls der Zielspieler das Starterkit hat, deaktiviere es.
                                        if (target.getPersistentDataContainer().has(KitManager.STARTERKIT_KEY)) {
                                            target.getPersistentDataContainer().remove(KitManager.STARTERKIT_KEY);
                                            target.sendMessage(LowdFX.serverMessage(Component.text("Das Starterkit wurde deaktiviert.", NamedTextColor.GREEN)));
                                            Utilities.positiveSound(target);
                                            player.sendMessage(LowdFX.serverMessage(Component.text(target.getName() + " hat das Starterkit deaktiviert.", NamedTextColor.GREEN)));
                                            return 1;
                                        }
                                        KitManager.KITS.get(target.getUniqueId()).give(false, target);
                                        target.getPersistentDataContainer().set(KitManager.STARTERKIT_KEY, PersistentDataType.BOOLEAN, true);
                                        target.sendMessage(LowdFX.serverMessage(Component.text("Hier ist dein Starterkit!", NamedTextColor.GREEN)));
                                        Utilities.positiveSound(target);
                                        player.sendMessage(LowdFX.serverMessage(Component.text(target.getName() + " hat das Starterkit bekommen!", NamedTextColor.GREEN)));
                                        return 1;
                                    })
                            )
                    )
            );
        }

        // /kit opkit Zweig
        builder.then(LiteralArgumentBuilder.<CommandSourceStack>literal("op")
                .requires(source -> Perms.check(source, Perms.Perm.OP_KIT))
                .executes(context -> {
                    Player player = (Player) context.getSource().getSender();
                    KitManager.KITS.get(player.getUniqueId()).give(true, player);
                    player.sendMessage(LowdFX.serverMessage(Component.text("Hier ist dein OP Kit!", NamedTextColor.GREEN)));
                    Utilities.positiveSound(player);
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                        .requires(source -> Perms.check(source, Perms.Perm.KIT_ADMIN))
                        .executes(context -> {
                            Player player = (Player) context.getSource().getSender();
                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class)
                                    .resolve(context.getSource()).getFirst();
                            KitManager.KITS.get(target.getUniqueId()).give(true, target);
                            target.sendMessage(LowdFX.serverMessage(Component.text("Hier ist dein OP Kit!", NamedTextColor.GREEN)));
                            Utilities.positiveSound(target);
                            player.sendMessage(LowdFX.serverMessage(Component.text(target.getName() + " hat das OP Kit bekommen!", NamedTextColor.GREEN)));
                            return 1;
                        })
                )
        );

        // /kit edit Zweig
        builder.then(LiteralArgumentBuilder.<CommandSourceStack>literal("edit")
                .requires(source -> Perms.check(source, Perms.Perm.KIT_ADMIN))
                .executes(context -> {
                    if (context.getSource().getExecutor() instanceof Player player)
                        KitManager.showConfig(player);
                    return 1;
                })
        );

        return builder.build();
    }
}
