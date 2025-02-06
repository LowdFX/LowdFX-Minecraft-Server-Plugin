package at.lowdfx.lowdfx.managers;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TeleportManager {
    public static final long EXPIRATION = 300000; // 5 Minutes
    public static final Map<UUID, Map.Entry<Location, AtomicLong>> BACK_POINTS = new HashMap<>();

    public static void update(@NotNull Entity entity) {
        update(entity.getUniqueId(), entity.getLocation().clone());
    }

    public static void update(UUID player, Location loc) {
        BACK_POINTS.put(player, Map.entry(loc, new AtomicLong(System.currentTimeMillis())));
    }

    public static @Nullable Location backPoint(UUID player) {
        Map.Entry<Location, AtomicLong> entry = BACK_POINTS.get(player);
        if (entry == null) return null;

        if (System.currentTimeMillis() - entry.getValue().get() <= EXPIRATION)
            return entry.getKey();

        BACK_POINTS.remove(player);
        return null;
    }
}
