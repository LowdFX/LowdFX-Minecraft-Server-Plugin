package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
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

@SuppressWarnings({ "UnstableApiUsage", "DuplicatedCode" })
public final class LockCommand {
    public static final String LOCK_PERMISSION = "lowdfx.lock";
    public static final String LOCK_ADMIN_PERMISSION = "lowdfx.lock.admin";

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("lock")
                .requires(source -> source.getSender().hasPermission(LOCK_PERMISSION) && source.getExecutor() instanceof Player)
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                    Block targetBlock = player.getTargetBlockExact(10);

                    if (targetBlock == null || !(targetBlock.getState() instanceof Container || targetBlock.getBlockData() instanceof Openable)) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann, um diesen Befehl auszuführen.", NamedTextColor.RED)));
                        return 1;
                    }

                    boolean canLock = !LowdFX.LOCKABLE_DATA.isLocked(targetBlock.getLocation()) ||
                            LowdFX.LOCKABLE_DATA.isPlayerInWhitelist(targetBlock.getLocation(), player.getName()) ||
                            context.getSource().getSender().hasPermission(LOCK_ADMIN_PERMISSION);

                    if (!canLock) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diesen Block nicht sperren, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                        return 1;
                    }

                    if (LowdFX.LOCKABLE_DATA.isLocked(targetBlock.getLocation())) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Dieser Block ist bereits gesperrt.", NamedTextColor.RED)));
                        return 1;
                    }

                    if (targetBlock.getBlockData() instanceof Chest) {
                        Utilities.connectedChests(targetBlock).forEach(l -> LowdFX.LOCKABLE_DATA.lockAdjacentChests(l, player.getName()));
                    } else {
                        LowdFX.LOCKABLE_DATA.addLocked(targetBlock.getLocation(), player.getName());
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

                            if (!context.getSource().getSender().hasPermission(LOCK_ADMIN_PERMISSION) && LowdFX.LOCKABLE_DATA.notOwner(player.getName(), targetBlock.getLocation())) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diesen Block nicht entsperren, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                                return 1;
                            }

                            if (targetBlock.getBlockData() instanceof Chest) {
                                Utilities.connectedChests(targetBlock).forEach(l -> LowdFX.LOCKABLE_DATA.unlockAdjacentChests(l));
                            } else {
                                LowdFX.LOCKABLE_DATA.removeLocked(targetBlock.getLocation());
                            }

                            player.sendMessage(LowdFX.serverMessage(Component.text("Block entsperrt!", NamedTextColor.GREEN)));
                            return 1;
                        })
                )
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("players", ArgumentTypes.players())
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("add")
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    Collection<Player> players = context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                                    Block targetBlock = player.getTargetBlockExact(10);

                                    if (targetBlock == null || !(targetBlock.getState() instanceof Container || targetBlock.getBlockData() instanceof Openable)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann, um diesen Befehl auszuführen.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    if (!context.getSource().getSender().hasPermission(LOCK_ADMIN_PERMISSION) && !LowdFX.LOCKABLE_DATA.isPlayerInWhitelist(targetBlock.getLocation(), player.getName())) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diesen Block nicht bearbeiten, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    LowdFX.LOCKABLE_DATA.addWhitelisted(targetBlock.getLocation(), players.stream().map(Player::getName).toList());
                                    player.sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler zur Whitelist hinzugefügt!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    Collection<Player> players = context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                                    Block targetBlock = player.getTargetBlockExact(10);

                                    if (targetBlock == null || !(targetBlock.getState() instanceof Container || targetBlock.getBlockData() instanceof Openable)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann, um diesen Befehl auszuführen.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    if (!context.getSource().getSender().hasPermission(LOCK_ADMIN_PERMISSION) && !LowdFX.LOCKABLE_DATA.isPlayerInWhitelist(targetBlock.getLocation(), player.getName())) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diesen Block nicht bearbeiten, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    LowdFX.LOCKABLE_DATA.removeWhitelisted(targetBlock.getLocation(), players.stream().map(Player::getName).toList());
                                    player.sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler von der Whitelist entfernt!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .build();
    }
}
