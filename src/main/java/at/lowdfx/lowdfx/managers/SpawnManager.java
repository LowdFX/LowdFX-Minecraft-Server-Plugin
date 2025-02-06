package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.teleportation.TeleportPoint;
import at.lowdfx.lowdfx.util.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;

public class SpawnManager {
    private static final YamlConfiguration DATA = new YamlConfiguration();
    private static final String DEFAULT_SPAWN_NAME = "spawn";

    public static void loadData() {
        // Überprüfen, ob die Datei existiert, andernfalls wird sie erstellt
        if (Files.notExists(LowdFX.DATA_DIR.resolve("spawns.yml"))) {
            try {
                // Ordner erstellen falls nicht vorhanden
                if (LowdFX.DATA_DIR.resolve("spawns.yml").toFile().createNewFile()) {
                    LowdFX.LOG.info("Spawns-Datei erstellt!");
                }
            } catch (IOException e) {
                LowdFX.LOG.error("Konnte die Spawns-Datei nicht erstellen!", e);
            }
        }

        // Laden der Datei, falls sie existiert
        try {
            DATA.load(LowdFX.DATA_DIR.resolve("spawns.yml").toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("Fehler beim Laden der Spawn-Konfiguration!", e);
        }

                config.set(defaultSpawnName, Objects.requireNonNull(Bukkit.getWorld(ServerProperties.get(ServerProperties.ServerPropertyType.WORLD_NAME))).getSpawnLocation());
        // Falls noch kein default Spawn existiert, erstelle ihn
        if (!DATA.contains(DEFAULT_SPAWN_NAME)) {
        }
    }

    public static void saveData() {
        try {
            DATA.save(LowdFX.DATA_DIR.resolve("spawns.yml").toFile());
            LowdFX.LOG.info("Spawns-Datei gespeichert.");
        } catch (IOException e) {
            LowdFX.LOG.error("Fehler beim Speichern der Spawn-Datei!", e);
        }
    }

    // Methode, die den passenden Spawn für den Spieler holt (z.B. mit Permissions)
    public static @NotNull TeleportPoint getSpawn(Player player) {
        for (String key : DATA.getKeys(false)) {
            if (player.hasPermission("spawn." + key)) {
                return new TeleportPoint(DATA.getLocation(key));
            }
        }
        return new TeleportPoint(DATA.getLocation(DEFAULT_SPAWN_NAME));
    }

    // Überprüfen, ob ein Spawn existiert
    public static boolean exists(String name) {
        return DATA.contains(name);
    }

    // Einen Spawn mit Namen zurückgeben
    public static @Nullable TeleportPoint getSpawn(String name) {
        if (exists(name)) {
            return new TeleportPoint(DATA.getLocation(name));
        }
        return null;
    }

    // Alle verfügbaren Spawn-Namen zurückgeben
    public static @NotNull Set<String> getNames() {
        return DATA.getKeys(false);
    }

    // Einen Spawn festlegen
    public static void setSpawn(String name, Location location) {
        DATA.set(name, location);
    }
}
