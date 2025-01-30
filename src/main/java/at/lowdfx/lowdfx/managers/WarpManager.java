package at.lowdfx.lowdfx.managers;


import at.lowdfx.lowdfx.LowdFX;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class WarpManager {
    private static File file; // Nicht-statische Datei
    private static final YamlConfiguration config = new YamlConfiguration(); // Nicht-statisch

    public WarpManager() {
        file = LowdFX.DATA_DIR.resolve("warps.yml").toFile(); // Dateiname mit Erweiterung

        if (!file.exists()) {
            try {
                if (file.createNewFile()) { // Neue Datei erstellen, wenn nicht vorhanden
                    LowdFX.LOG.info("Warp-Datei erstellt: {}", file.getName());
                }
            } catch (IOException e) {
                LowdFX.LOG.error("Konnte Warp-Datei nicht erstellen!", e);
            }
        }

        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("Fehler beim Laden der Warp-Konfiguration!", e);
        }
    }

    public void onDisable() {
        if (file != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                LowdFX.LOG.error("Fehler beim Speichern der Warp-Datei!", e);
            }
        }
    }

    public static boolean exits(String name) {
        return config.contains(name);
    }

    public static @Nullable Location getLocation(String name) {
        return exits(name) ? config.getLocation(name) : null;
    }

    public static void set(String name, Location location) {
        config.set(name, location);
        saveConfig();
    }

    public static void teleport(String name, Entity entity) {
        if (exits(name)) {
            TeleportManager.update(entity);

            Location loc = getLocation(name);
            if (loc != null) {
                entity.teleport(loc);
            } else {
                LowdFX.LOG.warn("Kein gültiger Ort für Warp: {}", name);
            }
        } else {
            LowdFX.LOG.warn("Warp existiert nicht: {}", name);
        }
    }

    public static @NotNull Set<String> getWarpsList() {
        return config.getKeys(false);
    }

    public static void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            LowdFX.LOG.error("Fehler beim Speichern der Warp-Konfiguration!", e);
        }
    }
}

