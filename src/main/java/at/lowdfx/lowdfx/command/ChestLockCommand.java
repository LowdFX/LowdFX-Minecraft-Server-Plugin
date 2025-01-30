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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings({ "UnstableApiUsage", "DuplicatedCode" })
public final class ChestLockCommand {
    public static final String LOCK_PERMISSION = "lowdfx.lock";
    public static final String LOCK_ADMIN_PERMISSION = "lowdfx.lock.admin";

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("lock")
                .requires(source -> source.getSender().hasPermission(LOCK_PERMISSION) && source.getExecutor() instanceof Player)
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                    Block targetBlock = player.getTargetBlockExact(10);

                    if (targetBlock == null || (targetBlock.getType() != Material.CHEST && !targetBlock.getType().name().endsWith("SHULKER_BOX"))) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst eine normale Kiste oder eine Shulker-Kiste anvisieren, um diesen Befehl auszuführen.", NamedTextColor.RED)));
                        return 1;
                    }

                    boolean canLock = !LowdFX.CHESTS_DATA.isChestLocked(targetBlock.getLocation()) ||
                            LowdFX.CHESTS_DATA.isPlayerInWhitelist(targetBlock.getLocation(), player.getName()) ||
                            context.getSource().getSender().hasPermission(LOCK_ADMIN_PERMISSION);

                    if (!canLock) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diese Kiste nicht sperren, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                        return 1;
                    }

                    if (LowdFX.CHESTS_DATA.isChestLocked(targetBlock.getLocation())) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Diese Kiste ist bereits gesperrt.", NamedTextColor.RED)));
                        return 1;
                    }

                    LowdFX.CHESTS_DATA.addLockedChest(targetBlock.getLocation(), player.getName());
                    Utilities.connectedChests(targetBlock).forEach(l -> LowdFX.CHESTS_DATA.lockAdjacentChests(l, player.getName()));

                    player.sendMessage(LowdFX.serverMessage(Component.text("Kiste gesperrt und du bist automatisch auf die Whitelist gesetzt!", NamedTextColor.GREEN)));
                    return 1;
                })
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("unlock")
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                            Block targetBlock = player.getTargetBlockExact(10);

                            if (targetBlock == null || (targetBlock.getType() != Material.CHEST && !targetBlock.getType().name().endsWith("SHULKER_BOX"))) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst eine normale Kiste oder eine Shulker-Kiste anvisieren, um diesen Befehl auszuführen.", NamedTextColor.RED)));
                                return 1;
                            }

                            if (!context.getSource().getSender().hasPermission(LOCK_ADMIN_PERMISSION) && LowdFX.CHESTS_DATA.notOwner(player.getName(), targetBlock.getLocation())) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diese Kiste nicht entsperren, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                                return 1;
                            }

                            LowdFX.CHESTS_DATA.removeLockedChest(targetBlock.getLocation());
                            player.sendMessage(LowdFX.serverMessage(Component.text("Kiste entsperrt!", NamedTextColor.GREEN)));
                            return 1;
                        })
                )
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("players", ArgumentTypes.players())
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("add")
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    Collection<Player> players = context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                                    Block targetBlock = player.getTargetBlockExact(10);

                                    if (targetBlock == null || (targetBlock.getType() != Material.CHEST && !targetBlock.getType().name().endsWith("SHULKER_BOX"))) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst eine normale Kiste oder eine Shulker-Kiste anvisieren, um diesen Befehl auszuführen.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    if (!context.getSource().getSender().hasPermission(LOCK_ADMIN_PERMISSION) && !LowdFX.CHESTS_DATA.isPlayerInWhitelist(targetBlock.getLocation(), player.getName())) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diese Kiste nicht bearbeiten, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    LowdFX.CHESTS_DATA.addWhitelisted(targetBlock.getLocation(), players.stream().map(Player::getName).toList());
                                    player.sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler zur Whitelist hinzugefügt!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    Collection<Player> players = context.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
                                    Block targetBlock = player.getTargetBlockExact(10);

                                    if (targetBlock == null || (targetBlock.getType() != Material.CHEST && !targetBlock.getType().name().endsWith("SHULKER_BOX"))) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst eine normale Kiste oder eine Shulker-Kiste anvisieren, um diesen Befehl auszuführen.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    if (!context.getSource().getSender().hasPermission(LOCK_ADMIN_PERMISSION) && !LowdFX.CHESTS_DATA.isPlayerInWhitelist(targetBlock.getLocation(), player.getName())) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst diese Kiste nicht bearbeiten, da du nicht der Besitzer bist.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    LowdFX.CHESTS_DATA.removeWhitelisted(targetBlock.getLocation(), players.stream().map(Player::getName).toList());
                                    player.sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler von der Whitelist entfernt!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .build();
    }
}
