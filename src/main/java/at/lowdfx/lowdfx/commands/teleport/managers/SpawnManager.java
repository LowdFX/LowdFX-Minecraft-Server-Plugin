package at.lowdfx.lowdfx.commands.teleport.managers;

import at.lowdfx.lowdfx.commands.teleport.teleportPoints.TeleportPoint;
import at.lowdfx.lowdfx.lowdfx;
import at.lowdfx.lowdfx.ServerProperties;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class SpawnManager implements Listener {

    private static File file;
    private static final YamlConfiguration config = new YamlConfiguration();
    private static lowdfx plugin;
    private static final String defaultSpawnName = "spawn";

    // Registrieren von Events
    @EventHandler
    public void onPlayerSpawn(PlayerRespawnEvent event) {
        getSpawn(event.getPlayer()).teleport(event.getPlayer(), plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setBedSpawnLocation(getSpawn(event.getPlayer()).getLocation(), true);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!event.getPlayer().hasPlayedBefore()) {
                getSpawn(event.getPlayer()).teleport(event.getPlayer(), plugin);
            }
        });
    }

    // Konstruktor zum Laden der Spawn-Datei und initialisieren von Spawns
    public SpawnManager(lowdfx pl) {
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            plugin = pl;
            file = new File(plugin.getDataFolder(), "spawns.yml");

            // Überprüfen ob die Datei existiert, andernfalls wird sie erstellt
            if (!file.exists()) {
                try {
                    // Ordner erstellen falls nicht vorhanden
                    if (!plugin.getDataFolder().exists()) {
                        plugin.getDataFolder().mkdirs();
                    }
                    file.createNewFile();
                    plugin.getLogger().info("Spawns-Datei erstellt!");
                } catch (IOException e) {
                    plugin.getLogger().severe("Konnte die Spawns-Datei nicht erstellen!");
                    e.printStackTrace();
                }
            }

            // Laden der Datei, falls sie existiert
            try {
                config.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException("Fehler beim Laden der Spawn-Konfiguration!", e);
            }

            // Falls noch kein default Spawn existiert, erstelle ihn
            if (!config.contains(defaultSpawnName)) {
                config.set(defaultSpawnName, Bukkit.getWorld(ServerProperties.get(ServerProperties.ServerPropertieType.worldname)).getSpawnLocation());
            }
        });
    }

    // Disable-Methode zum Speichern der Datei
    public void onDisable() {
        try {
            if (file != null) {
                config.save(file);
                plugin.getLogger().info("Spawns-Datei gespeichert.");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Speichern der Spawn-Datei!");
            e.printStackTrace();
        }
    }

    // Methode, die den passenden Spawn für den Spieler holt (z.B. mit Permissions)
    public static TeleportPoint getSpawn(Player player) {
        for (String key : config.getKeys(false)) {
            if (player.hasPermission("spawn." + key)) {
                return new TeleportPoint(config.getLocation(key));
            }
        }
        return new TeleportPoint(config.getLocation(defaultSpawnName));
    }

    // Überprüfen, ob ein Spawn existiert
    public static boolean exists(String name) {
        return config.contains(name);
    }

    // Einen Spawn mit Namen zurückgeben
    public static TeleportPoint getSpawn(String name) {
        if (exists(name)) {
            return new TeleportPoint(config.getLocation(name));
        }
        return null;
    }

    // Alle verfügbaren Spawn-Namen zurückgeben
    public static Set<String> getNames() {
        return config.getKeys(false);
    }

    // Einen Spawn festlegen
    public static void setSpawn(String name, Location location) {
        config.set(name, location);
    }
}
