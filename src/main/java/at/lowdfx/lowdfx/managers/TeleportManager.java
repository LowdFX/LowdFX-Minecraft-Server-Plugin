package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import com.marcpg.libpg.storage.JsonUtils;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class TeleportManager {
    public static final long EXPIRATION = 300000; // 5 Minutes

    public static final Map<UUID, Map.Entry<Location, AtomicLong>> BACK_POINTS = new HashMap<>();

    public static void save() {
        JsonUtils.saveSafe(BACK_POINTS, LowdFX.DATA_DIR.resolve("back-points.json").toFile());
    }

    public static void load() {
        BACK_POINTS.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("back-points.json").toFile(), Map.of()));
    }

    public static @Nullable Location backPoint(UUID player) {
        Map.Entry<Location, AtomicLong> entry = BACK_POINTS.get(player);
        if (entry == null) return null;

        if (System.currentTimeMillis() - entry.getValue().get() <= EXPIRATION)
            return entry.getKey();

        BACK_POINTS.remove(player);
        return null;
    }

    public static void teleportSafe(@NotNull Entity entity, @NotNull Location loc) {
        teleportSafe(entity, loc, b -> {});
    }

    public static void teleportSafe(@NotNull Entity entity, @NotNull Location loc, @NotNull Consumer<Boolean> after) {
        if (entity instanceof Player p)
            BACK_POINTS.put(p.getUniqueId(), Map.entry(p.getLocation().clone(), new AtomicLong(System.currentTimeMillis())));

        if (entity.getWorld().equals(loc.getWorld())) {
            entity.teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN, TeleportFlag.EntityState.RETAIN_VEHICLE, TeleportFlag.EntityState.RETAIN_PASSENGERS).thenAccept(after);
        } else { // Retain flags do not work in cross-world teleportation.
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
}
