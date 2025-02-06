package at.lowdfx.lowdfx.managers;


import at.lowdfx.lowdfx.LowdFX;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

public class WarpManager {
    private static final YamlConfiguration DATA = new YamlConfiguration();

    public static boolean exits(String name) {
        return DATA.contains(name);
    }

    public static @Nullable Location getLocation(String name) {
        return exits(name) ? DATA.getLocation(name) : null;
    }

    public static void set(String name, Location location) {
        DATA.set(name, location);
        saveData();
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
        return DATA.getKeys(false);
    }

    public static void loadData() {
        if (Files.notExists(LowdFX.DATA_DIR.resolve("warps.yml"))) {
            try {
                if (LowdFX.DATA_DIR.resolve("warps.yml").toFile().createNewFile()) // Neue Datei erstellen, wenn nicht vorhanden
                    LowdFX.LOG.info("Warp-Datei erstellt.");
            } catch (IOException e) {
                LowdFX.LOG.error("Konnte Warp-Datei nicht erstellen!", e);
            }
        }

        try {
            DATA.load(LowdFX.DATA_DIR.resolve("warps.yml").toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("Fehler beim Laden der Warp-Konfiguration!", e);
        }
    }

    public static void saveData() {
        try {
            DATA.save(LowdFX.DATA_DIR.resolve("warps.yml").toFile());
        } catch (IOException e) {
            LowdFX.LOG.error("Fehler beim Speichern der Warp-Konfiguration!", e);
        }
    }
}

