package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.teleportation.HomePoint;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.UUID;

public class HomeManager {
    public static final HashMap<UUID, HomePoint> HOMES = new HashMap<>();

    public static void loadAll() {
        Bukkit.getOnlinePlayers().forEach(p -> HomeManager.load(p.getUniqueId()));
    }

    public static void load(UUID player) {
        Bukkit.getScheduler().runTaskAsynchronously(LowdFX.PLUGIN, () -> HOMES.put(player, new HomePoint(player)));
    }

    public static void saveAll() {
        HOMES.forEach((p, h) -> save(p));
    }

    public static void save(UUID player) {
        Bukkit.getScheduler().runTaskAsynchronously(LowdFX.PLUGIN, () -> {
            HOMES.get(player).save();
            HOMES.remove(player);
        });
    }

    public static HomePoint get(UUID player) {
        return HOMES.get(player);
    }
}
