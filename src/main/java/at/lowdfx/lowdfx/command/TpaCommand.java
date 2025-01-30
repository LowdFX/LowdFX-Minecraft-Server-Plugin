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
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class TpaCommand {
    public static final long EXPIRATION_TIME = 120000; // 2 Minutes / 120 Seconds
    public static final Map<UUID, UUID> REQUESTS = new HashMap<>();
    public static final Map<UUID, Long> CREATIONS = new HashMap<>();

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("tpa")
                .requires(source -> source.getExecutor() instanceof Player)
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;
                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();

                            updateExpiry(player.getUniqueId());

                            if (player == target) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst dich nicht zu dir teleportieren.!", NamedTextColor.RED)));
                                return 1;
                            }
                            if (REQUESTS.containsKey(player.getUniqueId()) && !isExpired(player.getUniqueId())) {
                                OfflinePlayer p = Bukkit.getOfflinePlayer(REQUESTS.get(player.getUniqueId()));
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du hast bereits einen TPA an " + p.getName() + "! ", NamedTextColor.RED)
                                        .append(Component.text("Benutze ", NamedTextColor.GRAY)).append(Component.text("/tpa cancel", NamedTextColor.YELLOW)
                                                .hoverEvent(HoverEvent.showText(Component.text("Klicke, um den Befehl auszuführen!", NamedTextColor.YELLOW)))
                                                .clickEvent(ClickEvent.runCommand("/tpa cancel"))).append(Component.text(" um sie abzubrechen.", NamedTextColor.GRAY))));
                                return 1;
                            }
                            if (REQUESTS.containsValue(target.getUniqueId())) {
                                player.sendMessage(LowdFX.serverMessage(Component.text(target.getName() + " hat bereits eine TPA von einem Spieler!", NamedTextColor.RED)));
                                return 1;
                            }

                            REQUESTS.put(player.getUniqueId(), target.getUniqueId());
                            CREATIONS.put(player.getUniqueId(), System.currentTimeMillis());
                            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast " + target.getName() + " eine TPA gesendet!", NamedTextColor.GREEN)));

                            target.sendMessage(LowdFX.serverMessage(Component.text("Du hast eine TPA von " + player.getName() + " bekommen!", NamedTextColor.GREEN)));
                            target.sendMessage(LowdFX.serverMessage(Component.text()
                                    .append(Component.text("[ANNEHMEN]", NamedTextColor.GREEN)
                                            .hoverEvent(HoverEvent.showText(Component.text("Klicke, um die TPA zu akzeptieren!", NamedTextColor.GREEN)))
                                            .clickEvent(ClickEvent.runCommand("/tpa accept")))
                                    .appendSpace()
                                    .append(Component.text("[ABLEHNEN]", NamedTextColor.RED)
                                            .hoverEvent(HoverEvent.showText(Component.text("Klicke, um die TPA abzulehnen!", NamedTextColor.RED)))
                                            .clickEvent(ClickEvent.runCommand("/tpa deny")))
                                    .build()));
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("cancel")
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                            updateExpiry(player.getUniqueId());
                            if (!REQUESTS.containsKey(player.getUniqueId())) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du hast keine TPA versendet!", NamedTextColor.RED)));
                                return 1;
                            }
                            OfflinePlayer target = Bukkit.getOfflinePlayer(REQUESTS.get(player.getUniqueId()));

                            remove(player.getUniqueId());

                            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast die TPA an " + target.getName() + " abgebrochen!", NamedTextColor.YELLOW)));
                            if (target.isOnline()) {
                                Objects.requireNonNull(target.getPlayer()).sendMessage(LowdFX.serverMessage(Component.text(player.getName() + " hat seine TPA abgebrochen!", NamedTextColor.GREEN)));
                            }

                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("accept")
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                            Map.Entry<UUID, UUID> entry = Utilities.getEntryByValue(REQUESTS, player.getUniqueId());
                            if (entry == null || isExpired(entry.getKey())) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du hast keine TPA!", NamedTextColor.RED)));
                                return 1;
                            }

                            Player target = Bukkit.getPlayer(entry.getKey());
                            if (target == null) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Der Spieler ist nicht mehr online!", NamedTextColor.RED)));
                                remove(entry.getKey());
                                return 1;
                            }

                            updateExpiry(player.getUniqueId());
                            remove(entry.getKey());

                            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast die TPA von " + target.getName() + " angenommen!", NamedTextColor.GREEN)));
                            target.sendMessage(LowdFX.serverMessage(Component.text(player.getName() + " hat deine TPA angenommen!", NamedTextColor.GREEN)));
                            target.sendActionBar(Component.text("Du wirst teleportiert, beweg dich nicht!", NamedTextColor.GREEN));

                            Location targetLocation = player.getLocation().clone(); // In case the target logs out.
                            Bukkit.getScheduler().runTaskLater(LowdFX.PLUGIN, () -> target.teleport(targetLocation), 60);
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("deny")
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player)) return 1;

                            Map.Entry<UUID, UUID> entry = Utilities.getEntryByValue(REQUESTS, player.getUniqueId());
                            if (entry == null || isExpired(entry.getKey())) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du hast keine TPA!", NamedTextColor.RED)));
                                return 1;
                            }
                            OfflinePlayer target = Bukkit.getOfflinePlayer(entry.getKey());

                            updateExpiry(player.getUniqueId());
                            remove(entry.getKey());

                            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast die TPA von " + target.getName() + " abgelehnt!", NamedTextColor.YELLOW)));
                            if (target.isOnline()) {
                                Objects.requireNonNull(target.getPlayer()).sendMessage(LowdFX.serverMessage(Component.text(player.getName() + " hat deine TPA abgelehnt!", NamedTextColor.GREEN)));
                            }
                            return 1;
                        })
                )
                .build();
    }

    public static void updateExpiry(UUID uuid) {
        if (isExpired(uuid)) remove(uuid);
    }

    public static boolean isExpired(UUID uuid) {
        return CREATIONS.get(uuid) + EXPIRATION_TIME < System.currentTimeMillis();
    }

    public static void remove(UUID uuid) {
        REQUESTS.remove(uuid);
        CREATIONS.remove(uuid);
    }
}
