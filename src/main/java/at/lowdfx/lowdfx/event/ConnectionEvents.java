package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.kit.KitManager;
import at.lowdfx.lowdfx.managers.HomeManager;
import at.lowdfx.lowdfx.managers.PlaytimeManager;
import at.lowdfx.lowdfx.managers.SpawnManager;
import at.lowdfx.lowdfx.managers.TeleportManager;
import at.lowdfx.lowdfx.util.PlaytimeInfo;
import at.lowdfx.lowdfx.util.Utilities;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class ConnectionEvents implements Listener {
    public static Component FIRST_JOIN_MESSAGE;
    public static Component JOIN_MESSAGE;
    public static Component QUIT_MESSAGE;

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Join Message
        event.joinMessage(Utilities.joinMessage(player));

        // Playtime
        if (PlaytimeManager.PLAYTIMES.containsKey(uuid)) {
            PlaytimeManager.PLAYTIMES.get(uuid).login();
        } else {
            PlaytimeManager.PLAYTIMES.put(uuid, new PlaytimeInfo(uuid));
        }

        // Homes
        HomeManager.add(player.getUniqueId());

        // Kits
        KitManager.load(player.getUniqueId());

        // Spawns
        if (!event.getPlayer().hasPlayedBefore())
            TeleportManager.teleportSafe(player, SpawnManager.getSpawn(player));
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Quit Message
        event.quitMessage(Utilities.quitMessage(player));

        // Playtime
        if (PlaytimeManager.PLAYTIMES.containsKey(uuid)) {
            PlaytimeManager.PLAYTIMES.get(uuid).logout();
        }

        // Spawns
        player.setRespawnLocation(SpawnManager.getSpawn(player), true);
    }
}
