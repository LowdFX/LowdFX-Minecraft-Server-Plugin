package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.teleportation.HomePoint;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class HomeManager {
    private static final HashMap<Player, HomePoint> HOMES = new HashMap<>();

    public HomeManager() {
        Bukkit.getOnlinePlayers().forEach(HomeManager::load);
    }

    public void onDisable() {
        HOMES.forEach((p, h) -> save(p));
    }

    public static void load(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(LowdFX.PLUGIN, () -> HOMES.put(player, new HomePoint(player)));
    }

    public static void save(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(LowdFX.PLUGIN, () -> {
            HOMES.get(player).save();
            HOMES.remove(player);
        });
    }

    public static HomePoint get(Player player) {
        return HOMES.get(player);
    }

    public static @NotNull HomePoint get(OfflinePlayer player) {
        return new HomePoint(player);
    }
}
