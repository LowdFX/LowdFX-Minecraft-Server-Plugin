package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.inventory.LockableData;
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
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

@SuppressWarnings({ "UnstableApiUsage", "DuplicatedCode" })
public final class LockCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("lock")
                .requires(source -> Perms.check(source, Perms.Perm.LOCK) && source.getExecutor() instanceof Player)
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                    Block targetBlock = player.getTargetBlockExact(10);

                    if (targetBlock == null || !(targetBlock.getState() instanceof Container || targetBlock.getBlockData() instanceof Openable)) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann, um diesen Befehl auszuführen.", NamedTextColor.RED)));
                        return 1;
                    }

                    boolean canLock = !LockableData.isLocked(targetBlock.getLocation()) ||
                            LockableData.isPlayerInWhitelist(targetBlock.getLocation(), player.getName()) ||
                            Perms.check(context, Perms.Perm.LOCK_ADMIN);

                    if (!canLock) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diesen Block nicht sperren, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                        return 1;
                    }

                    if (LockableData.isLocked(targetBlock.getLocation())) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Dieser Block ist bereits gesperrt.", NamedTextColor.RED)));
                        return 1;
                    }

                    if (targetBlock.getBlockData() instanceof Chest) {
                        Utilities.connectedChests(targetBlock).forEach(l -> LockableData.lockAdjacentChests(l, player.getName()));
                    } else {
                        LockableData.addLocked(targetBlock.getLocation(), player.getName());
                    }

                    player.sendMessage(LowdFX.serverMessage(Component.text("Block gesperrt und du bist automatisch auf die Whitelist gesetzt!", NamedTextColor.GREEN)));
                    return 1;
                })
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("unlock")
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                            Block targetBlock = player.getTargetBlockExact(10);

                            if (targetBlock == null || !(targetBlock.getState() instanceof Container || targetBlock.getBlockData() instanceof Openable)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann, um diesen Befehl auszuführen.", NamedTextColor.RED)));
                                return 1;
                            }

                            if (!Perms.check(context, Perms.Perm.LOCK_ADMIN) && LockableData.notOwner(player.getName(), targetBlock.getLocation())) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diesen Block nicht entsperren, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                                return 1;
                            }

                            if (LockableData.isLocked(targetBlock.getLocation())) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Dieser Block ist nicht gesperrt.", NamedTextColor.RED)));
                                return 1;
                            }

                            if (targetBlock.getBlockData() instanceof Chest) {
                                Utilities.connectedChests(targetBlock).forEach(LockableData::unlockAdjacentChests);
                            } else {
                                LockableData.removeLocked(targetBlock.getLocation());
                            }

                            player.sendMessage(LowdFX.serverMessage(Component.text("Block entsperrt!", NamedTextColor.GREEN)));
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("whitelist")
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("add")
                                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("players", ArgumentTypes.players())
                                        .executes(context -> {
                                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                            Collection<Player> players = context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                                            Block targetBlock = player.getTargetBlockExact(10);

                                            if (targetBlock == null || !(targetBlock.getState() instanceof Container || targetBlock.getBlockData() instanceof Openable)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann, um diesen Befehl auszuführen.", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            if (!Perms.check(context, Perms.Perm.LOCK_ADMIN) && !LockableData.isPlayerInWhitelist(targetBlock.getLocation(), player.getName())) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diesen Block nicht bearbeiten, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            LockableData.addWhitelisted(targetBlock.getLocation(), players.stream().map(Player::getName).toList());
                                            player.sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler zur Whitelist hinzugefügt!", NamedTextColor.GREEN)));
                                            return 1;
                                        })
                                )
                        )
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("players", ArgumentTypes.players())
                                        .executes(context -> {
                                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                            Collection<Player> players = context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                                            Block targetBlock = player.getTargetBlockExact(10);

                                            if (targetBlock == null || !(targetBlock.getState() instanceof Container || targetBlock.getBlockData() instanceof Openable)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann, um diesen Befehl auszuführen.", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            if (!Perms.check(context, Perms.Perm.LOCK_ADMIN) && !LockableData.isPlayerInWhitelist(targetBlock.getLocation(), player.getName())) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diesen Block nicht bearbeiten, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            LockableData.removeWhitelisted(targetBlock.getLocation(), players.stream().map(Player::getName).toList());
                                            player.sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler von der Whitelist entfernt!", NamedTextColor.GREEN)));
                                            return 1;
                                        })
                                )
                        )
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("list")
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    Block targetBlock = player.getTargetBlockExact(10);

                                    if (targetBlock == null || !(targetBlock.getState() instanceof Container || targetBlock.getBlockData() instanceof Openable)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann, um diesen Befehl auszuführen.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    if (!Perms.check(context, Perms.Perm.LOCK_ADMIN) && !LockableData.isPlayerInWhitelist(targetBlock.getLocation(), player.getName())) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diesen Block nicht bearbeiten, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    List<String> whitelist = LockableData.whitelist(targetBlock.getLocation());
                                    if (whitelist.isEmpty()) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Es sind keine Spieler in der Whitelist von dem Block.", NamedTextColor.RED)));
                                    } else {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Spieler in der Whitelist von dem Block:", NamedTextColor.GREEN)));
                                        whitelist.forEach(p -> player.sendMessage(Component.text("- " + p, NamedTextColor.GREEN)));
                                    }

                                    return 1;
                                })
                        )
                )
                .build();
    }
}
