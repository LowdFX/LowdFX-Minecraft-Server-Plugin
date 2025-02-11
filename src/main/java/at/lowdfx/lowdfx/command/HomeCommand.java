package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.HomeManager;
import at.lowdfx.lowdfx.util.Perms;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public final class HomeCommand {
    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("home")
                .requires(source -> Perms.check(source, Perms.Perm.HOME))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                        return 1;
                    }

                    Map<String, Location> homes = HomeManager.get(player.getUniqueId());
                    if (!homes.containsKey("home")) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home wurde noch nicht gesetzt!", NamedTextColor.RED)));
                        return 1;
                    }

                    player.teleport(homes.get("home"));
                    player.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest nach Hause teleportiert!", NamedTextColor.GREEN)));
                    return 1;
                })
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("tp")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    if (context.getSource().getSender() instanceof Player player && player.hasPlayedBefore())
                                        HomeManager.get(player.getUniqueId()).keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    String name = context.getArgument("name", String.class);

                                    Map<String, Location> homes = HomeManager.get(player.getUniqueId());
                                    if (!homes.containsKey(name)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home " + name + " wurde noch nicht gesetzt!", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    player.teleport(homes.get(name));
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest nach Hause " + name + " teleportiert!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("set")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    if (context.getSource().getSender() instanceof Player player && player.hasPlayedBefore())
                                        HomeManager.get(player.getUniqueId()).keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .then(RequiredArgumentBuilder.<CommandSourceStack, FinePositionResolver>argument("location", ArgumentTypes.finePosition(true))
                                        .executes(context -> {
                                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                            String name = context.getArgument("name", String.class);
                                            Location location = context.getArgument("location", FinePositionResolver.class).resolve(context.getSource()).toLocation(context.getSource().getLocation().getWorld());

                                            Map<String, Location> homes = HomeManager.get(player.getUniqueId());
                                            if (homes.size() >= LowdFX.CONFIG.getInt("basic.maxhomes")) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Die maximale Home-Grenze von " + LowdFX.CONFIG.getInt("basic.maxhomes") + " wurde erreicht!", NamedTextColor.RED)));
                                                return 1;
                                            }
                                            homes.put(name, location);
                                            player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home " + name + " wurde gesetzt!", NamedTextColor.GREEN)));
                                            return 1;
                                        })
                                )
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    if (context.getSource().getSender() instanceof Player player && player.hasPlayedBefore())
                                        HomeManager.get(player.getUniqueId()).keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    String name = context.getArgument("name", String.class);

                                    Map<String, Location> homes = HomeManager.get(player.getUniqueId());
                                    if (!homes.containsKey(name)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Das Home " + name + " existiert nicht!", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    homes.remove(name);
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home " + name + " wurde entfernt!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("tp_other")
                        .requires(source -> Perms.check(source, Perms.Perm.HOME_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            Player player = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                            if (player.hasPlayedBefore())
                                                HomeManager.get(player.getUniqueId()).keySet().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String name = context.getArgument("name", String.class);
                                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();

                                            Map<String, Location> homes = HomeManager.get(target.getUniqueId());
                                            if (!homes.containsKey(name)) {
                                                context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Das Home " + name + " von " + target.getName() + " existiert nicht!", NamedTextColor.RED)));
                                                return 1;
                                            }
                                            target.teleport(homes.get(name));

                                            target.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest nach Hause " + name + " teleportiert!", NamedTextColor.GREEN)));
                                            context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text(target.getName() + " wurde zum Home " + name + " teleportiert!", NamedTextColor.GREEN)));
                                            return 1;
                                        })
                                )
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("set_other")
                        .requires(source -> Perms.check(source, Perms.Perm.HOME_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            Player player = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                            if (player.hasPlayedBefore())
                                                HomeManager.get(player.getUniqueId()).keySet().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .then(RequiredArgumentBuilder.<CommandSourceStack, FinePositionResolver>argument("location", ArgumentTypes.finePosition(true))
                                                .executes(context -> {
                                                    String name = context.getArgument("name", String.class);
                                                    Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                                    Location location = context.getArgument("location", FinePositionResolver.class).resolve(context.getSource()).toLocation(context.getSource().getLocation().getWorld());

                                                    Map<String, Location> homes = HomeManager.get(target.getUniqueId());
                                                    homes.put(name, location);

                                                    target.sendMessage(LowdFX.serverMessage(Component.text("Dein Home " + name + " wurde gesetzt!", NamedTextColor.GREEN)));
                                                    context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Das Home " + name + " von " + target.getName() + " wurde gesetzt!", NamedTextColor.RED)));
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove_other")
                        .requires(source -> Perms.check(source, Perms.Perm.HOME_ADMIN))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            Player player = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                            if (player.hasPlayedBefore())
                                                HomeManager.get(player.getUniqueId()).keySet().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String name = context.getArgument("name", String.class);
                                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();

                                            Map<String, Location> homes = HomeManager.get(target.getUniqueId());
                                            if (!homes.containsKey(name)) {
                                                context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Das Home " + name + " von " + target.getName() + " existiert nicht!", NamedTextColor.RED)));
                                                return 1;
                                            }
                                            homes.remove(name);

                                            target.sendMessage(LowdFX.serverMessage(Component.text("Dein Home " + name + " wurde entfernt!", NamedTextColor.GREEN)));
                                            context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Das Home " + name + " von " + target.getName() + " wurde entfernt!", NamedTextColor.RED)));
                                            return 1;
                                        })
                                )
                        )
                )
                .build();
    }
}
