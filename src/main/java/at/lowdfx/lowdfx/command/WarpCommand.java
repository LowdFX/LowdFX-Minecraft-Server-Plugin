package at.lowdfx.lowdfx.command;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.WarpManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public final class WarpCommand {
    public static final String WARP_ADMIN_PERMISSION = "lowdfx.warp.setremove";
    public static final String WARP_PERMISSION = "lowdfx.warp";

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("warp")
                .requires(source -> source.getSender().hasPermission(WARP_PERMISSION))
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                        .requires(source -> source.getExecutor() instanceof Player)
                        .suggests((context, builder) -> {
                            WarpManager.getWarpsList().forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                            String name = context.getArgument("name", String.class);

                            if (!WarpManager.exits(name)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Der eingegebene Warppunkt " + name + " existiert nicht!", NamedTextColor.RED)));
                                return 1;
                            }

                            WarpManager.teleport(name, player);
                            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast dich zu dem Warppunkt " + name + " teleportiert!", NamedTextColor.GREEN)));
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("set")
                        .requires(source -> source.getSender().hasPermission(WARP_ADMIN_PERMISSION))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    WarpManager.getWarpsList().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .then(RequiredArgumentBuilder.<CommandSourceStack, FinePositionResolver>argument("location", ArgumentTypes.finePosition(true))
                                        .executes(context -> {
                                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                            String name = context.getArgument("name", String.class);
                                            Location location = context.getArgument("location", FinePositionResolver.class).resolve(context.getSource()).toLocation(context.getSource().getLocation().getWorld());

                                            WarpManager.set(name, location);
                                            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast den Warppunkt " + name + " erfolgreich gesetzt!", NamedTextColor.GREEN)));
                                            return 1;
                                        })
                                )
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                        .requires(source -> source.getSender().hasPermission(WARP_ADMIN_PERMISSION))
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    WarpManager.getWarpsList().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                                    String name = context.getArgument("name", String.class);

                                    if (!WarpManager.exits(name)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Der eingegebene Warppunkt " + name + " existiert nicht!", NamedTextColor.RED)));
                                        return 1;
                                    }

                                    WarpManager.set(name, null);
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Du hast den Warppunkt " + name + " gelöscht!", NamedTextColor.GREEN)));
                                    return 1;
                                })
                        )
                )
                .build();
    }
}
