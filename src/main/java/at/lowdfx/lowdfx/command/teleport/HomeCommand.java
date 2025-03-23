package at.lowdfx.lowdfx.command.teleport;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.teleport.HomeManager;
import at.lowdfx.lowdfx.managers.teleport.TeleportManager;
import at.lowdfx.lowdfx.util.Configuration;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.SimpleLocation;
import at.lowdfx.lowdfx.util.Utilities;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
                // /home – Teleportiere den Spieler zu seinem Default-Home ("home")
                .executes(context -> {
                    if (!(context.getSource().getExecutor() instanceof Player player)) {
                        context.getSource().getSender().sendMessage(
                                LowdFX.serverMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED)));
                        return 1;
                    }
                    Map<String, SimpleLocation> homes = HomeManager.get(player.getUniqueId());
                    if (!homes.containsKey("home")) {
                        player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home wurde noch nicht gesetzt!", NamedTextColor.RED)));
                        Utilities.negativeSound(player);
                        return 1;
                    }
                    SimpleLocation homeLoc = homes.get("home");
                    if (Configuration.SAFE_TELEPORT_ENABLED) {
                        TeleportManager.teleportDelayed(player, homeLoc.asLocation());
                    } else {
                        homeLoc.teleportSafe(player);
                    }
                    player.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest nach Hause teleportiert!", NamedTextColor.GREEN)));
                    Utilities.positiveSound(player);
                    return 1;
                })
                // /home tp <name>: Teleportiere den Spieler zum benannten Home
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("tp")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    Player player = (Player) context.getSource().getExecutor();
                                    HomeManager.get(player.getUniqueId()).keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        return 1;
                                    String name = context.getArgument("name", String.class);
                                    Map<String, SimpleLocation> homes = HomeManager.get(player.getUniqueId());
                                    if (!homes.containsKey(name)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home " + name + " wurde noch nicht gesetzt!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    SimpleLocation homeLoc = homes.get(name);
                                    if (Configuration.SAFE_TELEPORT_ENABLED) {
                                        TeleportManager.teleportDelayed(player, homeLoc.asLocation());
                                    } else {
                                        homeLoc.teleportSafe(player);
                                    }
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Du wurdest nach Hause " + name + " teleportiert!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    return 1;
                                })
                        )
                )
                // /home set <name>: Setzt das Home an der aktuellen Position des Spielers
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("set")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    Player player = (Player) context.getSource().getExecutor();
                                    HomeManager.get(player.getUniqueId()).keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        return 1;
                                    String name = context.getArgument("name", String.class);
                                    Location location = player.getLocation();
                                    Map<String, SimpleLocation> homes = HomeManager.get(player.getUniqueId());
                                    if (homes.size() >= Configuration.BASIC_MAX_HOMES) {
                                        player.sendMessage(LowdFX.serverMessage(
                                                Component.text("Die maximale Home-Grenze von " + Configuration.BASIC_MAX_HOMES + " wurde erreicht!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    homes.put(name, SimpleLocation.ofLocation(location));
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home " + name + " wurde gesetzt!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    return 1;
                                })
                        )
                )
                // /home remove <name>: Löscht das benannte Home
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove")
                        .requires(source -> source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    Player player = (Player) context.getSource().getExecutor();
                                    HomeManager.get(player.getUniqueId()).keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        return 1;
                                    String name = context.getArgument("name", String.class);
                                    Map<String, SimpleLocation> homes = HomeManager.get(player.getUniqueId());
                                    if (!homes.containsKey(name)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Das Home " + name + " existiert nicht!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    homes.remove(name);
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Dein Home " + name + " wurde entfernt!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);
                                    return 1;
                                })
                        )
                )
                .build();
    }
}
