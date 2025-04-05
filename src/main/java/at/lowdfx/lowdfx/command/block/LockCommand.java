package at.lowdfx.lowdfx.command.block;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.managers.block.LockableManager;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.SimpleLocation;
import at.lowdfx.lowdfx.util.Utilities;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({ "UnstableApiUsage", "DuplicatedCode" })
public final class LockCommand {

    static {
        CommandHelp.register("lock",
                MiniMessage.miniMessage().deserialize("/lock help"),
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du öffenbare Blöcke sperren.<newline></gray>" +
                                "<yellow>· /lock<newline></yellow>" +
                                "<yellow>· /lock global<newline></yellow>" +
                                "<yellow>· /lock unlock<newline></yellow>" +
                                "<yellow>· /lock info<newline></yellow>" +
                                "<yellow>· /lock whitelist add<newline></yellow>" +
                                "<yellow>· /lock whitelist remove<newline></yellow>" +
                                "<yellow>· /lock whitelist list</yellow>"),
                null,
                Perms.Perm.LOCK.getPermission(),
                null);
    }

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("lock")
                .requires(source -> Perms.check(source, Perms.Perm.LOCK) && source.getExecutor() instanceof Player)
                // Standard /lock: Erstellt einen Lock mit global = false.
                .executes(context -> {
                    if (!(context.getSource().getSender() instanceof Player player)) return 1;

                    Block block = player.getTargetBlockExact(5);
                    if (LockableManager.notLockable(block)) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann.", NamedTextColor.RED)));
                        Utilities.negativeSound(player);
                        return 1;
                    }

                    if (LockableManager.isLocked(block.getLocation())) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Dieser Block ist bereits gesperrt.", NamedTextColor.RED)));
                        Utilities.negativeSound(player);
                        return 1;
                    }

                    LockableManager.lock(player.getUniqueId(), block, false);
                    player.sendMessage(LowdFX.serverMessage(Component.text("Block wurde gesperrt! Nur der Besitzer und Whitelist-Spieler haben Zugriff.", NamedTextColor.GREEN)));
                    Utilities.positiveSound(player);
                    return 1;
                })
                // /lock global: Wenn der Block bereits gesperrt ist, kann der Besitzer den Lock von nicht-global zu global ändern.
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("global")
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) return 1;

                            Block block = player.getTargetBlockExact(5);
                            if (LockableManager.notLockable(block)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            if (LockableManager.isLocked(block.getLocation())) {
                                LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
                                if (!locked.isOwner(player)) {
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Du musst der Besitzer sein, um den Lock-Status zu ändern.", NamedTextColor.RED)));
                                    Utilities.negativeSound(player);
                                    return 1;
                                }
                                if (locked.isGlobal()) {
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Dieser Block ist bereits global gesperrt.", NamedTextColor.YELLOW)));
                                    return 1;
                                }
                                locked.setGlobal(true);
                                player.sendMessage(LowdFX.serverMessage(Component.text("Lock wurde auf global umgestellt – nun kann jeder interagieren (nur Abbau verboten).", NamedTextColor.GREEN)));
                                Utilities.positiveSound(player);
                                return 1;
                            } else {
                                LockableManager.Locked locked = new LockableManager.Locked(
                                        player.getUniqueId(), SimpleLocation.ofLocation(block.getLocation()), null, new ArrayList<>(), true
                                );
                                LockableManager.lock(block, locked);
                                player.sendMessage(LowdFX.serverMessage(Component.text("Block wurde global gesperrt – jeder kann interagieren, nur Abbau & Neusperrung verboten!", NamedTextColor.GREEN)));
                                Utilities.positiveSound(player);
                                return 1;
                            }
                        })
                )
                // /lock modify hopperout und hopperin
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("modify")
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("hopperin")
                                .executes(context -> {
                                    if (!(context.getSource().getSender() instanceof Player player)) return 1;
                                    Block block = player.getTargetBlockExact(5);
                                    if (LockableManager.notLockable(block)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen gültigen Block anvisieren.", NamedTextColor.RED)));
                                        return 1;
                                    }
                                    LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
                                    if (locked == null) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Dieser Block ist nicht gesperrt.", NamedTextColor.RED)));
                                        return 1;
                                    }
                                    if (!locked.isOwner(player)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du bist nicht der Besitzer dieses Locks.", NamedTextColor.RED)));
                                        return 1;
                                    }
                                    // Toggle den hopperIn-Status:
                                    boolean newState = !locked.isHopperInAllowed();
                                    locked.setHopperInAllowed(newState);
                                    if (newState) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Hopper (und HopperMinecarts) dürfen nun Items in diese Kiste einlegen.", NamedTextColor.GREEN)));
                                    } else {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Hopper (und HopperMinecarts) dürfen nun keine Items mehr in diese Kiste einlegen.", NamedTextColor.YELLOW)));
                                    }
                                    return 1;
                                })
                        )
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("hopperout")
                                .executes(context -> {
                                    if (!(context.getSource().getSender() instanceof Player player)) return 1;
                                    Block block = player.getTargetBlockExact(5);
                                    if (LockableManager.notLockable(block)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen gültigen Block anvisieren.", NamedTextColor.RED)));
                                        return 1;
                                    }
                                    LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
                                    if (locked == null) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Dieser Block ist nicht gesperrt.", NamedTextColor.RED)));
                                        return 1;
                                    }
                                    if (!locked.isOwner(player)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du bist nicht der Besitzer dieses Locks.", NamedTextColor.RED)));
                                        return 1;
                                    }
                                    // Toggle den hopperOut-Status:
                                    boolean newState = !locked.isHopperOutAllowed();
                                    locked.setHopperOutAllowed(newState);
                                    if (newState) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Hopper (und HopperMinecarts) dürfen nun Items aus dieser Kiste entnehmen.", NamedTextColor.GREEN)));
                                    } else {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Hopper (und HopperMinecarts) dürfen nun keine Items mehr aus dieser Kiste entnehmen.", NamedTextColor.YELLOW)));
                                    }
                                    return 1;
                                })
                        )
                )

                // /lock unlock
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("unlock")
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) return 1;

                            Block block = player.getTargetBlockExact(5);
                            if (LockableManager.notLockable(block)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
                            if (locked == null) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Der Block ist nicht gesperrt.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            if (!locked.isOwner(player)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst der Besitzer sein, um den Block zu entsperren.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            LockableManager.unlock(block.getLocation());
                            player.sendMessage(LowdFX.serverMessage(Component.text("Block wurde entsperrt!", NamedTextColor.GREEN)));
                            Utilities.positiveSound(player);
                            return 1;
                        })
                )
                // /lock info: Zeigt Besitzer und Whitelist der anvisierten Kiste an.
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) return 1;

                            Block block = player.getTargetBlockExact(5);
                            if (LockableManager.notLockable(block)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
                            if (locked == null) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Der Block ist nicht gesperrt.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }

                            String ownerName = Bukkit.getOfflinePlayer(locked.owner()).getName();
                            StringBuilder whitelistNames = new StringBuilder();
                            if (locked.whitelist().isEmpty()) {
                                whitelistNames.append("Keine Spieler in der Whitelist.");
                            } else {
                                for (var uuid : locked.whitelist()) {
                                    String name = Bukkit.getOfflinePlayer(uuid).getName();
                                    whitelistNames.append("- ").append(name).append("\n");
                                }
                            }

                            player.sendMessage(LowdFX.serverMessage(Component.text("Besitzer: " + ownerName + "\nWhitelist:\n" + whitelistNames, NamedTextColor.GREEN)));
                            return 1;
                        })
                )
                // whitelist-Befehle bleiben unverändert.
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("whitelist")
                        .then(LiteralArgumentBuilder.<CommandSourceStack>literal("add")
                                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerProfileListResolver>argument("players", ArgumentTypes.playerProfiles())
                                        .executes(context -> {
                                            if (!(context.getSource().getSender() instanceof Player player)) return 1;
                                            Collection<PlayerProfile> players = context.getArgument("players", PlayerProfileListResolver.class).resolve(context.getSource());

                                            Block block = player.getTargetBlockExact(5);
                                            if (LockableManager.notLockable(block)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst einen Block anvisieren, den man öffnen kann.", NamedTextColor.RED)));
                                                Utilities.negativeSound(player);
                                                return 1;
                                            }

                                            LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
                                            if (locked == null) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Der Block ist nicht gesperrt.", NamedTextColor.RED)));
                                                Utilities.negativeSound(player);
                                                return 1;
                                            }

                                            if (!locked.isOwner(player)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst der Besitzer sein, um den Block zu modifizieren.", NamedTextColor.RED)));
                                                Utilities.negativeSound(player);
                                                return 1;
                                            }

                                            players.forEach(p -> locked.addWhitelist(p.getId()));
                                            player.sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler wurde(n) zur whitelist vom Block hinzugefügt!", NamedTextColor.GREEN)));
                                            Utilities.positiveSound(player);
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
                                                Utilities.negativeSound(player);
                                                return 1;
                                            }

                                            LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
                                            if (locked == null) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Der Block ist nicht gesperrt.", NamedTextColor.RED)));
                                                Utilities.negativeSound(player);
                                                return 1;
                                            }

                                            if (!locked.isOwner(player)) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Du musst der Besitzer sein, um den Block zu modifizieren.", NamedTextColor.RED)));
                                                Utilities.negativeSound(player);
                                                return 1;
                                            }

                                            players.forEach(p -> locked.removeWhitelist(p.getId()));
                                            player.sendMessage(LowdFX.serverMessage(Component.text(players.size() + " Spieler wurde(n) von der whitelist vom Block entfernt!", NamedTextColor.GREEN)));
                                            Utilities.positiveSound(player);
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
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    LockableManager.Locked locked = LockableManager.getLocked(block.getLocation());
                                    if (locked == null) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Der Block ist nicht gesperrt.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }

                                    if (locked.notAllowed(player)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du musst berechtigt sein, um den Block zu modifizieren.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
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
