package at.lowdfx.lowdfx.command.inventory;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.kit.KitManager;
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
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("UnstableApiUsage")
public final class KitCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("kit")
                .requires(source -> source.getExecutor() instanceof Player)
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("starter")
                        .requires(source -> Perms.check(source, Perms.Perm.STARTER_KIT))
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                            if (player.getPersistentDataContainer().has(KitManager.STARTERKIT_KEY)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du hast bereits das starter kit bekommen!", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            KitManager.KITS.get(player.getUniqueId()).give(false, player);
                            player.getPersistentDataContainer().set(KitManager.STARTERKIT_KEY, PersistentDataType.BOOLEAN, true);

                            player.sendMessage(LowdFX.serverMessage(Component.text("Hier ist dein Starterkit!", NamedTextColor.GREEN)));
                            Utilities.positiveSound(player);
                            return 1;
                        })
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                                .requires(source -> Perms.check(source, Perms.Perm.KIT_ADMIN))
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();

                                    KitManager.KITS.get(target.getUniqueId()).give(false, target);
                                    target.getPersistentDataContainer().set(KitManager.STARTERKIT_KEY, PersistentDataType.BOOLEAN, true);

                                    target.sendMessage(LowdFX.serverMessage(Component.text("Hier ist dein Starterkit!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(target);
                                    player.sendMessage(LowdFX.serverMessage(Component.text(target.getName() + " hat das Starterkit bekommen!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("op")
                        .requires(source -> Perms.check(source, Perms.Perm.OP_KIT))
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                            KitManager.KITS.get(player.getUniqueId()).give(true, player);

                            player.sendMessage(LowdFX.serverMessage(Component.text("Hier ist dein OP Kit!", NamedTextColor.GREEN)));
                            Utilities.positiveSound(player);
                            return 1;
                        })
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                                .requires(source -> Perms.check(source, Perms.Perm.KIT_ADMIN))
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();

                                    KitManager.KITS.get(target.getUniqueId()).give(true, target);

                                    target.sendMessage(LowdFX.serverMessage(Component.text("Hier ist dein OP Kit!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(target);
                                    player.sendMessage(LowdFX.serverMessage(Component.text(target.getName() + " hat das OP Kit bekommen!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("edit")
                        .executes(context -> {
                            if (context.getSource().getExecutor() instanceof Player player)
                                KitManager.showConfig(player);
                            return 1;
                        })
                )
                .build();
    }
}
