package at.lowdfx.lowdfx.managers.teleport;

import at.lowdfx.lowdfx.LowdFX;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class CooldownManager {
    private static File cooldownFile;
    private static FileConfiguration cooldownConfig;

    // Diese Methode sollte im Plugin beim onEnable() aufgerufen werden
    public static void init() {
        cooldownFile = new File(LowdFX.DATA_DIR.toFile(), "backCooldowns.yml");
        if (!cooldownFile.exists()) {
            try {
                cooldownFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cooldownConfig = YamlConfiguration.loadConfiguration(cooldownFile);
    }

    /**
     * Gibt den gespeicherten Zeitstempel des letzten /back-Befehls für den Spieler zurück,
     * oder 0L, falls noch keiner gesetzt wurde.
     */
    public static long getBackCooldown(UUID playerId) {
        return cooldownConfig.getLong(playerId.toString(), 0L);
    }

    /**
     * Speichert den aktuellen Zeitstempel für den /back-Befehl eines Spielers.
     */
    public static void setBackCooldown(UUID playerId, long timestamp) {
        cooldownConfig.set(playerId.toString(), timestamp);
        try {
            cooldownConfig.save(cooldownFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
