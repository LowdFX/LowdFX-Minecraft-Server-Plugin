package at.lowdfx.lowdfx.commands.teleport.managers;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.commands.teleport.teleportPoints.HomePoint;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class HomeManager implements Listener {
    private static final HashMap<Player, HomePoint> HOMES = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        load(event.getPlayer());
    }
    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        save(event.getPlayer());
    }

    public HomeManager() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            load(player);
        }
    }

    public void onDisable() {
        for (Player player : HOMES.keySet()) {
            save(player);
        }
    }

    private static void load(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Lowdfx.PLUGIN, () -> HOMES.put(player, new HomePoint(player)));
    }

    private static void save(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Lowdfx.PLUGIN, () -> {
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
