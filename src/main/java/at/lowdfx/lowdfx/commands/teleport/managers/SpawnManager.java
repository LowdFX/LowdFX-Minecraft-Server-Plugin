package at.lowdfx.lowdfx.commands.teleport.managers;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.ServerProperties;
import at.lowdfx.lowdfx.commands.teleport.teleportPoints.TeleportPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;

public class SpawnManager implements Listener {
    private static File file;
    private static final YamlConfiguration config = new YamlConfiguration();
    private static final String defaultSpawnName = "spawn";

    // Registrieren von Events
    @EventHandler
    public void onPlayerSpawn(@NotNull PlayerRespawnEvent event) {
        getSpawn(event.getPlayer()).teleport(event.getPlayer(), Lowdfx.PLUGIN);
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        event.getPlayer().setRespawnLocation(getSpawn(event.getPlayer()).location(), true);
        Bukkit.getScheduler().runTaskAsynchronously(Lowdfx.PLUGIN, () -> {
            if (!event.getPlayer().hasPlayedBefore()) {
                getSpawn(event.getPlayer()).teleport(event.getPlayer(), Lowdfx.PLUGIN);
            }
        });
    }

    // Konstruktor zum Laden der Spawn-Datei und initialisieren von Spawns
    public SpawnManager(Lowdfx pl) {
        Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
            file = Lowdfx.DATA_DIR.resolve("spawns.yml").toFile();

            // Überprüfen, ob die Datei existiert, andernfalls wird sie erstellt
            if (!file.exists()) {
                try {
                    // Ordner erstellen falls nicht vorhanden
                    if (Files.notExists(Lowdfx.DATA_DIR)) {
                        Files.createDirectories(Lowdfx.DATA_DIR);
                    }
                    if (file.createNewFile()) {
                        Lowdfx.LOG.info("Spawns-Datei erstellt!");
                    }
                } catch (IOException e) {
                    Lowdfx.LOG.error("Konnte die Spawns-Datei nicht erstellen!", e);
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
                config.set(defaultSpawnName, Objects.requireNonNull(Bukkit.getWorld(ServerProperties.get(ServerProperties.ServerPropertyType.WORLD_NAME))).getSpawnLocation());
            }
        });
    }

    // Disable-Methode zum Speichern der Datei
    public void onDisable() {
        try {
            if (file != null) {
                config.save(file);
                Lowdfx.LOG.info("Spawns-Datei gespeichert.");
            }
        } catch (IOException e) {
            Lowdfx.LOG.error("Fehler beim Speichern der Spawn-Datei!", e);
        }
    }

    // Methode, die den passenden Spawn für den Spieler holt (z.B. mit Permissions)
    @Contract("_ -> new")
    public static @NotNull TeleportPoint getSpawn(Player player) {
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
    public static @Nullable TeleportPoint getSpawn(String name) {
        if (exists(name)) {
            return new TeleportPoint(config.getLocation(name));
        }
        return null;
    }

    // Alle verfügbaren Spawn-Namen zurückgeben
    public static @NotNull Set<String> getNames() {
        return config.getKeys(false);
    }

    // Einen Spawn festlegen
    public static void setSpawn(String name, Location location) {
        config.set(name, location);
    }
}
