package at.lowdfx.lowdfx.managers.teleport;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.Configuration;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.SimpleLocation;
import com.google.gson.reflect.TypeToken;
import com.marcpg.libpg.storage.JsonUtils;
import com.marcpg.libpg.storing.Pair;
import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.bukkit.scheduler.BukkitTask;
import at.lowdfx.lowdfx.util.Utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class TeleportManager {

    public static final Map<UUID, Pair<SimpleLocation, AtomicLong>> BACK_POINTS = new HashMap<>();
    // Map für bereits gestartete, verzögerte Teleport-Aufträge
    private static final Map<UUID, BukkitTask> pendingTeleports = new HashMap<>();
    private static final Map<UUID, SimpleLocation> lastEventPoints = new HashMap<>();

    // Diese innere Klasse speichert Informationen zum pending Teleport
    public static class PendingTeleport {
        public final Player player;
        public final Location targetLocation;
        public int remainingTicks; // verbleibende Zeit in Ticks
        public final int initialX, initialY, initialZ;
        public PendingTeleport(Player player, Location targetLocation, int delaySeconds) {
            this.player = player;
            this.targetLocation = targetLocation;
            this.remainingTicks = delaySeconds * 20;
            Location loc = player.getLocation();
            this.initialX = loc.getBlockX();
            this.initialY = loc.getBlockY();
            this.initialZ = loc.getBlockZ();
        }
    }

    public static void save() {
        JsonUtils.saveSafe(BACK_POINTS.entrySet(), LowdFX.DATA_DIR.resolve("back-points.json").toFile());
    }

    public static void load() {
        BACK_POINTS.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("back-points.json").toFile(), Map.of(), new TypeToken<>() {}));
    }

    public static void teleportSafe(@NotNull Entity entity, @NotNull Location loc) {
        teleportSafe(entity, loc, b -> {});
    }

    public static void teleportSafe(@NotNull Entity entity, @NotNull Location loc, @NotNull Consumer<Boolean> after) {
        if (entity instanceof Player p) {
            // Speichere den aktuellen Standort bevor teleportiert wird
            setLastEvent(p);
            BACK_POINTS.put(p.getUniqueId(), Pair.of(SimpleLocation.ofLocation(p.getLocation()), new AtomicLong(System.currentTimeMillis())));
        }
        if (entity.getWorld().equals(loc.getWorld())) {
            entity.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN,
                            TeleportFlag.EntityState.RETAIN_VEHICLE, TeleportFlag.EntityState.RETAIN_PASSENGERS)
                    .thenAccept(success -> {
                        if (success && entity instanceof Player) {
                            // Verzögere die Velocity-Anpassung um einen Tick
                            Bukkit.getScheduler().runTaskLater(LowdFX.PLUGIN, () -> {
                                ((Player) entity).setVelocity(new Vector(0, 0, 0));
                            }, 1L);
                        }
                        after.accept(success);
                    });
        } else { // Bei Cross-World-Teleportation
            if (entity.getVehicle() == null) {
                entity.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(after);
            } else {
                Entity vehicle = entity.getVehicle();
                vehicle.eject();
                teleportSafe(vehicle, loc, b -> {
                    entity.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    vehicle.addPassenger(entity);
                });
            }
        }
    }

    /**
     * Führt einen verzögerten Teleport für einen Spieler durch.
     * Wird in Befehlen wie /warp, /home, /spawn oder /tpa accept verwendet,
     * wenn die globale Safe-Teleport-Funktion in der Config aktiviert ist.
     * Falls Safe-Teleport deaktiviert ist, wird sofort teleportiert.
     */
    public static void teleportDelayed(Player player, Location loc) {
        // Admin-Bypass: sofort teleportieren
        if (Perms.check(player, Perms.Perm.TP_BYPASS)) {
            teleportSafe(player, loc);
            return;
        }

        // Falls Safe-Teleport deaktiviert ist, sofort teleportieren
        if (!Configuration.SAFE_TELEPORT_ENABLED) {
            teleportSafe(player, loc);
            return;
        }

        // Falls bereits ein Teleport-Task läuft, diesen abbrechen.
        if (hasPendingTeleport(player)) {
            cancelPendingTeleport(player);
        }

        int delaySeconds = Configuration.TELEPORT_DELAY;
        PendingTeleport pending = new PendingTeleport(player, loc, delaySeconds);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(LowdFX.PLUGIN, () -> {
            // Prüfe, ob sich der Spieler (in Blockkoordinaten) bewegt hat.
            Location current = player.getLocation();
            if (current.getBlockX() != pending.initialX ||
                    current.getBlockY() != pending.initialY ||
                    current.getBlockZ() != pending.initialZ) {
                player.sendMessage(LowdFX.serverMessage(
                        Component.text("Teleportation abgebrochen, weil du dich bewegt hast!", NamedTextColor.RED)));
                Utilities.negativeSound(player);
                cancelPendingTeleport(player);
                return;
            }
            pending.remainingTicks -= 20;
            int remainingSeconds = pending.remainingTicks / 20;
            if (pending.remainingTicks > 0) {
                player.sendActionBar(Component.text("Teleport in " + remainingSeconds + " Sekunden...", NamedTextColor.YELLOW));
            } else {
                cancelPendingTeleport(player);
                teleportSafe(player, loc);
            }
        }, 0L, 20L);
        pendingTeleports.put(player.getUniqueId(), task);
    }



    /**
     * Bricht einen pending Teleport für den angegebenen Spieler ab.
     */
    public static void cancelPendingTeleport(Player player) {
        BukkitTask task = pendingTeleports.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
    public static boolean hasPendingTeleport(Player player) {
        return pendingTeleports.containsKey(player.getUniqueId());
    }

    /**
     * Speichert den aktuellen Standort des Spielers als letztes Ereignis.
     */
    public static void setLastEvent(Player player) {
        lastEventPoints.put(player.getUniqueId(), SimpleLocation.ofLocation(player.getLocation()));
    }

    /**
     * Liefert den zuletzt gespeicherten Punkt (Teleport oder Tod) des Spielers.
     */
    public static SimpleLocation getLastEvent(UUID playerId) {
        return lastEventPoints.get(playerId);
    }

}
