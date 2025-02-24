package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.LockableManager;
import at.lowdfx.lowdfx.util.Perms;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collection;

@SuppressWarnings({ "UnstableApiUsage", "DuplicatedCode" })
public final class LockCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("lock")
                .requires(source -> Perms.check(source, Perms.Perm.LOCK) && source.getExecutor() instanceof Player)
                .executes(context -> {
                    if (!(context.getSource().getSender() instanceof Player player)) return 1;

                    Block block = player.getTargetBlockExact(5);
                    if (LockableManager.notLockable(block)) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann.", NamedTextColor.RED)));
                        return 1;
                    }

                    if (LockableManager.isLocked(block.getLocation())) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Dieser Block ist bereits gesperrt.", NamedTextColor.RED)));
                        return 1;
                    }

                    LockableManager.lock(player.getUniqueId(), block);
                    player.sendMessage(LowdFX.serverMessage(Component.text("Block wurde gesperrt!", NamedTextColor.GREEN)));
                    return 1;
                })
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("unlock")
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) return 1;

                            Block block = player.getTargetBlockExact(5);
                            if (LockableManager.notLockable(block)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann.", NamedTextColor.RED)));
                                return 1;
                            }

                            LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
                            if (locked == null) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Der Block ist nicht gesperrt.", NamedTextColor.RED)));
                                return 1;
                            }

                            if (!locked.isOwner(player)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst der Besitzer sein, um den Block zu entsperren.", NamedTextColor.RED)));
                                return 1;
                            }

                            LockableManager.unlock(block.getLocation());
                            player.sendMessage(LowdFX.serverMessage(Component.text("Block wurde entsperrt!", NamedTextColor.GREEN)));
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("whitelist")
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("add")
                                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
                                        .executes(context -> {
                                            if (!(context.getSource().getSender() instanceof Player player)) return 1;
                                            Collection<PlayerProfile> players = context.getArgument("players", PlayerProfileListResolver.class).resolve(context.getSource());

                                            Block block = player.getTargetBlockExact(5);
                                            if (LockableManager.notLockable(block)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann.", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
                                            if (locked == null) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Der Block ist nicht gesperrt.", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            if (!locked.isOwner(player)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst der Besitzer sein, um den Block zu modifizieren.", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            players.forEach(p -> locked.addWhitelist(p.getId()));
                                            player.sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler wurde(n) zur whitelist vom Block hinzugefügt!", NamedTextColor.GREEN)));
                                            return 1;
                                        })
                                )
                        )
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
                                        .executes(context -> {
                                            if (!(context.getSource().getSender() instanceof Player player)) return 1;
                                            Collection<PlayerProfile> players = context.getArgument("players", PlayerProfileListResolver.class).resolve(context.getSource());

                                            Block block = player.getTargetBlockExact(5);
                                            if (LockableManager.notLockable(block)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann.", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
                                            if (locked == null) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Der Block ist nicht gesperrt.", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            if (!locked.isOwner(player)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst der Besitzer sein, um den Block zu modifizieren.", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            players.forEach(p -> locked.removeWhitelist(p.getId()));
                                            player.sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler wurde(n) von der whitelist vom Block entfernt!", NamedTextColor.GREEN)));
                                            return 1;
                                        })
                                )
                        )
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("list")
                                .executes(context -> {
                                    if (!(context.getSource().getSender() instanceof Player player)) return 1;

                                    Block block = player.getTargetBlockExact(5);
                                    if (LockableManager.notLockable(block)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
                                    if (locked == null) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Der Block ist nicht gesperrt.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    if (locked.notAllowed(player)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst berechtigt sein, um den Block zu modifizieren.", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    if (locked.whitelist().isEmpty()) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Es sind keine Spieler in der Whitelist von dem Block.", NamedTextColor.YELLOW)));
                                    } else {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Spieler in der Whitelist von dem Block:", NamedTextColor.GREEN)));
                                        locked.whitelist().forEach(p -> player.sendMessage(Component.text("- " + Bukkit.getOfflinePlayer(p).getName(), NamedTextColor.GREEN)));
                                    }
                                    return 1;
                                })
                        )
                )
                .build();
    }
}
