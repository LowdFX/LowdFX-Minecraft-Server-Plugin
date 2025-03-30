package at.lowdfx.lowdfx.command.teleport;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.util.CommandHelp;
import at.lowdfx.lowdfx.managers.teleport.TeleportManager;
import at.lowdfx.lowdfx.util.Configuration;
import at.lowdfx.lowdfx.util.Perms;
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
import net.kyori.adventure.text.minimessage.MiniMessage;
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

    static {
        CommandHelp.register("tpa",
                // Kurzinfo (wird in der Übersicht angezeigt)
                MiniMessage.miniMessage().deserialize("/help tpa"),
                // Ausführliche Beschreibung (wird bei /help adminhelp angezeigt)
                MiniMessage.miniMessage().deserialize(
                        "<gray>Mit diesem Befehl kannst du eine TP Anfrage an angegebene Spieler senden.<newline></gray>" +
                                "<yellow>· /tpa <player><newline></yellow>" +
                                "<yellow>· /tpa here <player><newline></yellow>" +
                                "<yellow>· /tpa cancel<newline></yellow>" +
                                "<yellow>· /tpa deny<newline></yellow>" +
                                "<yellow>· /tpa accept<newline></yellow>"),
                null, // Kein zusätzlicher Admin-spezifischer Text
                Perms.Perm.TPA.getPermission(),
                null); // Keine separate Admin-Permission
    }


    public static final long EXPIRATION_TIME = 120000; // 2 Minuten
    // Für normale /tpa-Anfragen: key = Absender, value = Ziel
    public static final Map<UUID, UUID> REQUESTS = new HashMap<>();
    // Speichert, wann die Anfrage erstellt wurde
    public static final Map<UUID, Long> CREATIONS = new HashMap<>();
    // Speichert für den Absender, ob es sich um eine tpahere-Anfrage handelt (true = tpahere, false = normal)
    public static final Map<UUID, Boolean> HERE_FLAGS = new HashMap<>();

    public static LiteralCommandNode<CommandSourceStack> command() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("tpa")
                .requires(source -> Perms.check(source, Perms.Perm.TPA) && source.getExecutor() instanceof Player)
                // Normaler TPA: /tpa <player>
                .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player))
                                return 1;
                            Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                            updateExpiry(player.getUniqueId());

                            if (player.equals(target)) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst dich nicht zu dir teleportieren!", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }
                            if (REQUESTS.containsKey(player.getUniqueId()) && !isExpired(player.getUniqueId())) {
                                OfflinePlayer p = Bukkit.getOfflinePlayer(REQUESTS.get(player.getUniqueId()));
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du hast bereits eine TPA an " + p.getName() + "! Benutze /tpa cancel, um sie abzubrechen.", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }
                            if (REQUESTS.containsValue(target.getUniqueId())) {
                                player.sendMessage(LowdFX.serverMessage(Component.text(target.getName() + " hat bereits eine TPA von einem Spieler!", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }
                            // Normale TPA-Anfrage: Here-Flag ist false
                            REQUESTS.put(player.getUniqueId(), target.getUniqueId());
                            HERE_FLAGS.put(player.getUniqueId(), false);
                            CREATIONS.put(player.getUniqueId(), System.currentTimeMillis());
                            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast " + target.getName() + " eine TPA gesendet!", NamedTextColor.GREEN)));
                            Utilities.positiveSound(player);

                            target.sendMessage(LowdFX.serverMessage(Component.text("Du hast eine TPA von " + player.getName() + " erhalten!", NamedTextColor.GREEN)));
                            target.sendMessage(LowdFX.serverMessage(Component.text()
                                    .append(Component.text("[ANNEHMEN]", NamedTextColor.GREEN)
                                            .hoverEvent(HoverEvent.showText(Component.text("Klicke, um die TPA zu akzeptieren!", NamedTextColor.GREEN)))
                                            .clickEvent(ClickEvent.runCommand("/tpa accept")))
                                    .appendSpace()
                                    .append(Component.text("[ABLEHNEN]", NamedTextColor.RED)
                                            .hoverEvent(HoverEvent.showText(Component.text("Klicke, um die TPA abzulehnen!", NamedTextColor.RED)))
                                            .clickEvent(ClickEvent.runCommand("/tpa deny")))
                                    .build()));
                            Utilities.positiveSound(target);
                            return 1;
                        })
                )
                // Neuer Subcommand: /tpa here <player>
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("here")
                        .requires(source -> Perms.check(source, Perms.Perm.TPA) && source.getExecutor() instanceof Player)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, PlayerSelectorArgumentResolver>argument("player", ArgumentTypes.player())
                                .executes(context -> {
                                    if (!(context.getSource().getExecutor() instanceof Player player))
                                        return 1;
                                    Player target = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                    updateExpiry(player.getUniqueId());
                                    if (player.equals(target)) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst dich nicht zu dir teleportieren!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    if (REQUESTS.containsKey(player.getUniqueId()) && !isExpired(player.getUniqueId())) {
                                        OfflinePlayer p = Bukkit.getOfflinePlayer(REQUESTS.get(player.getUniqueId()));
                                        player.sendMessage(LowdFX.serverMessage(Component.text("Du hast bereits eine TPA gesendet! Benutze /tpa cancel, um sie abzubrechen.", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    if (REQUESTS.containsValue(target.getUniqueId())) {
                                        player.sendMessage(LowdFX.serverMessage(Component.text(target.getName() + " hat bereits eine TPA von einem Spieler!", NamedTextColor.RED)));
                                        Utilities.negativeSound(player);
                                        return 1;
                                    }
                                    // Für tpahere: Here-Flag = true
                                    REQUESTS.put(player.getUniqueId(), target.getUniqueId());
                                    HERE_FLAGS.put(player.getUniqueId(), true);
                                    CREATIONS.put(player.getUniqueId(), System.currentTimeMillis());
                                    player.sendMessage(LowdFX.serverMessage(Component.text("Du hast " + target.getName() + " eine TPahere-Anfrage gesendet!", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(player);

                                    target.sendMessage(LowdFX.serverMessage(Component.text("Du hast eine TPahere-Anfrage von " + player.getName() + " erhalten! Tippe /tpa accept, um dich zu ihm teleportieren zu lassen.", NamedTextColor.GREEN)));
                                    Utilities.positiveSound(target);
                                    return 1;
                                })
                        )
                )
                // /tpa cancel: Bricht die aktuell gesendete TPA ab.
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("cancel")
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player))
                                return 1;

                            updateExpiry(player.getUniqueId());
                            if (!REQUESTS.containsKey(player.getUniqueId())) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du hast keine TPA versendet!", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }
                            OfflinePlayer target = Bukkit.getOfflinePlayer(REQUESTS.get(player.getUniqueId()));

                            remove(player.getUniqueId());

                            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast die TPA an " + target.getName() + " abgebrochen!", NamedTextColor.YELLOW)));
                            Utilities.positiveSound(player);
                            if (target.isOnline()) {
                                Objects.requireNonNull(target.getPlayer()).sendMessage(LowdFX.serverMessage(Component.text(player.getName() + " hat seine TPA abgebrochen!", NamedTextColor.RED)));
                                Utilities.negativeSound(target.getPlayer());
                            }
                            return 1;
                        })
                )
                // /tpa accept: Nimmt die TPA-Anfrage an.
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("accept")
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player acceptor))
                                return 1;
                            // Suche nach einer Anfrage, bei der der Wert (Ziel) gleich der UUID des Akzeptierenden ist.
                            Map.Entry<UUID, UUID> entry = Utilities.getEntryByValue(REQUESTS, acceptor.getUniqueId());
                            if (entry == null || isExpired(entry.getKey())) {
                                acceptor.sendMessage(LowdFX.serverMessage(Component.text("Du hast keine TPA!", NamedTextColor.RED)));
                                Utilities.negativeSound(acceptor);
                                return 1;
                            }
                            UUID senderUUID = entry.getKey();
                            Player sender = Bukkit.getPlayer(senderUUID);
                            if (sender == null) {
                                acceptor.sendMessage(LowdFX.serverMessage(Component.text("Der anfragende Spieler ist nicht mehr online!", NamedTextColor.RED)));
                                Utilities.negativeSound(acceptor);
                                remove(senderUUID);
                                return 1;
                            }
                            boolean here = HERE_FLAGS.getOrDefault(senderUUID, false);
                            remove(senderUUID);
                            if (here) {
                                // Bei tpahere: Teleportiere den Akzeptierenden zu dem Absender.
                                acceptor.sendMessage(LowdFX.serverMessage(Component.text("Du hast die TPahere von " + sender.getName() + " angenommen! Du wirst zu ihm teleportiert.", NamedTextColor.GREEN)));
                                sender.sendMessage(LowdFX.serverMessage(Component.text(acceptor.getName() + " hat deine TPahere angenommen!", NamedTextColor.GREEN)));
                                Utilities.positiveSound(acceptor);
                                Utilities.positiveSound(sender);
                                Location senderLocation = sender.getLocation().clone();
                                if (Configuration.SAFE_TELEPORT_ENABLED) {
                                    TeleportManager.teleportDelayed(acceptor, senderLocation);
                                } else {
                                    Bukkit.getScheduler().runTaskLater(LowdFX.PLUGIN, () -> TeleportManager.teleportSafe(acceptor, senderLocation), 60);
                                }
                            } else {
                                // Normale TPA: Teleportiere den Absender zu dem Akzeptierenden.
                                acceptor.sendMessage(LowdFX.serverMessage(Component.text("Du hast die TPA von " + sender.getName() + " angenommen! Er wird zu dir teleportiert.", NamedTextColor.GREEN)));
                                sender.sendMessage(LowdFX.serverMessage(Component.text(acceptor.getName() + " hat deine TPA angenommen!", NamedTextColor.GREEN)));
                                Utilities.positiveSound(acceptor);
                                Utilities.positiveSound(sender);
                                Location acceptorLocation = acceptor.getLocation().clone();
                                if (Configuration.SAFE_TELEPORT_ENABLED) {
                                    TeleportManager.teleportDelayed(sender, acceptorLocation);
                                } else {
                                    Bukkit.getScheduler().runTaskLater(LowdFX.PLUGIN, () -> TeleportManager.teleportSafe(sender, acceptorLocation), 60);
                                }
                            }
                            return 1;
                        })
                )
                // /tpa deny: Lehnt die TPA-Anfrage ab.
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("deny")
                        .executes(context -> {
                            if (!(context.getSource().getExecutor() instanceof Player player))
                                return 1;
                            Map.Entry<UUID, UUID> entry = Utilities.getEntryByValue(REQUESTS, player.getUniqueId());
                            if (entry == null || isExpired(entry.getKey())) {
                                player.sendMessage(LowdFX.serverMessage(Component.text("Du hast keine TPA!", NamedTextColor.RED)));
                                Utilities.negativeSound(player);
                                return 1;
                            }
                            OfflinePlayer senderOff = Bukkit.getOfflinePlayer(entry.getKey());
                            updateExpiry(player.getUniqueId());
                            remove(entry.getKey());
                            player.sendMessage(LowdFX.serverMessage(Component.text("Du hast die TPA von " + senderOff.getName() + " abgelehnt!", NamedTextColor.YELLOW)));
                            Utilities.positiveSound(player);
                            if (senderOff.isOnline()) {
                                Objects.requireNonNull(senderOff.getPlayer()).sendMessage(LowdFX.serverMessage(Component.text(player.getName() + " hat deine TPA abgelehnt!", NamedTextColor.GREEN)));
                                Utilities.negativeSound(senderOff.getPlayer());
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
        return CREATIONS.containsKey(uuid) && CREATIONS.get(uuid) + EXPIRATION_TIME < System.currentTimeMillis();
    }

    public static void remove(UUID uuid) {
        REQUESTS.remove(uuid);
        CREATIONS.remove(uuid);
        HERE_FLAGS.remove(uuid);
    }
}
