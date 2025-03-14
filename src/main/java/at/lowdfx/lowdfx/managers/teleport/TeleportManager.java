package at.lowdfx.lowdfx.managers.teleport;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.SimpleLocation;
import com.google.gson.reflect.TypeToken;
import com.marcpg.libpg.storage.JsonUtils;
import com.marcpg.libpg.storing.Pair;
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

public final class TeleportManager {
    public static final long EXPIRATION = 300000; // 5 Minutes

    public static final Map<UUID, Pair<SimpleLocation, AtomicLong>> BACK_POINTS = new HashMap<>();

    public static void save() {
        JsonUtils.saveSafe(BACK_POINTS.entrySet(), LowdFX.DATA_DIR.resolve("back-points.json").toFile());
    }

    public static void load() {
        BACK_POINTS.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("back-points.json").toFile(), Map.of(), new TypeToken<>() {}));
    }

    public static @Nullable SimpleLocation backPoint(UUID player) {
        Pair<SimpleLocation, AtomicLong> entry = BACK_POINTS.get(player);
        if (entry == null) return null;

        if (System.currentTimeMillis() - entry.right().get() <= EXPIRATION)
            return entry.left();

        BACK_POINTS.remove(player);
        return null;
    }

    public static void teleportSafe(@NotNull Entity entity, @NotNull Location loc) {
        teleportSafe(entity, loc, b -> {});
    }

    public static void teleportSafe(@NotNull Entity entity, @NotNull Location loc, @NotNull Consumer<Boolean> after) {
        if (entity instanceof Player p)
            BACK_POINTS.put(p.getUniqueId(), Pair.of(SimpleLocation.ofLocation(p.getLocation()), new AtomicLong(System.currentTimeMillis())));

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
