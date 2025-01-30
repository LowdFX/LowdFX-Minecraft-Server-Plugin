package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.HomeManager;
import at.lowdfx.lowdfx.teleportation.HomePoint;
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

@SuppressWarnings("UnstableApiUsage")
public final class HomeCommand {
    public static final String HOME_ADMIN_PERMISSION = "lowdfx.home.admin";
    public static final String HOME_PERMISSION = "lowdfx.home";

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("home")
                .requires(source -> source.getSender().hasPermission(HOME_PERMISSION))
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                        return 1;
                    }

                    HomePoint homePoint = HomeManager.get(player);
                    if (homePoint.doesNotExist("home")) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home wurde noch nicht gesetzt!", NamedTextColor.RED)));
                        return 1;
                    }

                    homePoint.get("home").teleport(player);
                    player.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest nach Hause teleportiert!", NamedTextColor.GREEN)));
                    return 1;
                })
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("tp")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    if (context.getSource().getSender() instanceof Player player && player.hasPlayedBefore())
                                        HomeManager.get(player).getHomes().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    String name = context.getArgument("name", String.class);

                                    HomePoint homePoint = HomeManager.get(player);
                                    if (homePoint.doesNotExist(name)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home " + name + " wurde noch nicht gesetzt!", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    homePoint.get(name).teleport(player);
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
                                        HomeManager.get(player).getHomes().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .then(RequiredArgumentBuilder.<CommandSourceStack, FinePositionResolver>argument("location", ArgumentTypes.finePosition(true))
                                        .executes(context -> {
                                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                            String name = context.getArgument("name", String.class);
                                            Location location = context.getArgument("location", FinePositionResolver.class).resolve(context.getSource()).toLocation(context.getSource().getLocation().getWorld());

                                            HomePoint homePoint = HomeManager.get(player);
                                            if (homePoint.getHomes().size() >= LowdFX.CONFIG.getInt("basic.maxhomes")) {
                                                player.sendMessage(LowdFX.serverMessage(Component.text("Die maximale Home-Grenze von " + LowdFX.CONFIG.getInt("basic.maxhomes") + " wurde erreicht!", NamedTextColor.RED)));
                                                return 1;
                                            }

                                            homePoint.set(name, location);
                                            homePoint.save();
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
                                        HomeManager.get(player).getHomes().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    String name = context.getArgument("name", String.class);

                                    HomePoint homePoint = HomeManager.get(player);
                                    if (homePoint.doesNotExist(name)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Das Home " + name + " existiert nicht!", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    homePoint.remove(name);
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home " + name + " wurde entfernt!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("tp_other")
                        .requires(source -> source.getSender().hasPermission(HOME_ADMIN_PERMISSION))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            Player player = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                            if (player.hasPlayedBefore())
                                                HomeManager.get(player).getHomes().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String name = context.getArgument("name", String.class);
                                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();

                                            HomePoint homePoint = HomeManager.get(target);
                                            if (homePoint.doesNotExist(name)) {
                                                context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Das Home " + name + " von " + target.getName() + " existiert nicht!", NamedTextColor.RED)));
                                                return 1;
                                            }
                                            homePoint.get(name).teleport(target);

                                            target.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest nach Hause " + name + " teleportiert!", NamedTextColor.GREEN)));
                                            context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text(target.getName() + " wurde zum Home " + name + " teleportiert!", NamedTextColor.GREEN)));
                                            return 1;
                                        })
                                )
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("set_other")
                        .requires(source -> source.getSender().hasPermission(HOME_ADMIN_PERMISSION))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            Player player = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                            if (player.hasPlayedBefore())
                                                HomeManager.get(player).getHomes().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .then(RequiredArgumentBuilder.<CommandSourceStack, FinePositionResolver>argument("location", ArgumentTypes.finePosition(true))
                                                .executes(context -> {
                                                    String name = context.getArgument("name", String.class);
                                                    Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                                    Location location = context.getArgument("location", FinePositionResolver.class).resolve(context.getSource()).toLocation(context.getSource().getLocation().getWorld());

                                                    HomePoint homePoint = HomeManager.get(target);
                                                    homePoint.set(name, location);
                                                    homePoint.save();

                                                    target.sendMessage(LowdFX.serverMessage(Component.text("Dein Home " + name + " wurde gesetzt!", NamedTextColor.GREEN)));
                                                    context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Das Home " + name + " von " + target.getName() + " wurde gesetzt!", NamedTextColor.RED)));
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove_other")
                        .requires(source -> source.getSender().hasPermission(HOME_ADMIN_PERMISSION))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            Player player = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                            if (player.hasPlayedBefore())
                                                HomeManager.get(player).getHomes().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String name = context.getArgument("name", String.class);
                                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();

                                            HomePoint homePoint = HomeManager.get(target);
                                            if (homePoint.doesNotExist(name)) {
                                                context.getSource().getSender().sendMessage(LowdFX.serverMessage(Component.text("Das Home " + name + " von " + target.getName() + " existiert nicht!", NamedTextColor.RED)));
                                                return 1;
                                            }
                                            homePoint.remove(name);
                                            homePoint.save();

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
