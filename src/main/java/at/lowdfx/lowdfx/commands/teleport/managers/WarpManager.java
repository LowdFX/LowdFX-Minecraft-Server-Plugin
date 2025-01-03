package at.lowdfx.lowdfx.commands.teleport.managers;


import at.lowdfx.lowdfx.lowdfx;
import at.lowdfx.lowdfx.commands.teleport.teleportPoints.TeleportPoint;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class WarpManager {

    private static File file; // Nicht-statische Datei
    private static final YamlConfiguration config = new YamlConfiguration(); // Nicht-statisch
    private static lowdfx plugin;

    public WarpManager(lowdfx pl) {
        this.plugin = pl;
        this.file = new File(plugin.getDataFolder(), "warps.yml"); // Dateiname mit Erweiterung

        if (!file.exists()) {
            try {
                file.createNewFile(); // Neue Datei erstellen, wenn nicht vorhanden
                plugin.getLogger().info("Warp-Datei erstellt: " + file.getName());
            } catch (IOException e) {
                plugin.getLogger().severe("Konnte Warp-Datei nicht erstellen!");
                e.printStackTrace();
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
                plugin.getLogger().severe("Fehler beim Speichern der Warp-Datei!");
                e.printStackTrace();
            }
        }
    }

    public static boolean exits(String name) {
        return config.contains(name);
    }

    public static Location getLocation(String name) {
        return exits(name) ? config.getLocation(name) : null;
    }

    public static void set(String name, Location location) {
        config.set(name, location);
        saveConfig();
    }

    public static void teleport(String name, Entity entity) {
        if (exits(name)) {
            Location loc = getLocation(name);
            if (loc != null) {
                entity.teleport(loc);
            } else {
                plugin.getLogger().warning("Kein gültiger Ort für Warp: " + name);
            }
        } else {
            plugin.getLogger().warning("Warp existiert nicht: " + name);
        }
    }

    public static Set<String> getWarpsList() {
        return config.getKeys(false);
    }

    public static void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Speichern der Warp-Konfiguration!");
            e.printStackTrace();
        }
    }
}

