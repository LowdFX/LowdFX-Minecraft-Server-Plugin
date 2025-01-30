package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.teleportation.TeleportPoint;
import at.lowdfx.lowdfx.util.ServerProperties;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;

public class SpawnManager {
    private static File file;
    private static final YamlConfiguration config = new YamlConfiguration();
    private static final String defaultSpawnName = "spawn";

    // Konstruktor zum Laden der Spawn-Datei und initialisieren von Spawns
    public SpawnManager() {
        Bukkit.getScheduler().runTaskAsynchronously(LowdFX.PLUGIN, () -> {
            file = LowdFX.DATA_DIR.resolve("spawns.yml").toFile();

            // Überprüfen, ob die Datei existiert, andernfalls wird sie erstellt
            if (!file.exists()) {
                try {
                    // Ordner erstellen falls nicht vorhanden
                    if (Files.notExists(LowdFX.DATA_DIR)) {
                        Files.createDirectories(LowdFX.DATA_DIR);
                    }
                    if (file.createNewFile()) {
                        LowdFX.LOG.info("Spawns-Datei erstellt!");
                    }
                } catch (IOException e) {
                    LowdFX.LOG.error("Konnte die Spawns-Datei nicht erstellen!", e);
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
                LowdFX.LOG.info("Spawns-Datei gespeichert.");
            }
        } catch (IOException e) {
            LowdFX.LOG.error("Fehler beim Speichern der Spawn-Datei!", e);
        }
    }

    // Methode, die den passenden Spawn für den Spieler holt (z.B. mit Permissions)
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
