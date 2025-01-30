package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.PlaytimeInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlaytimeManager {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Map<UUID, PlaytimeInfo> PLAYTIMES = new HashMap<>();

    public static void save() {
        try (FileWriter writer = new FileWriter(LowdFX.DATA_DIR.resolve("playtime.json").toFile())) {
            GSON.toJson(PLAYTIMES, writer);
        } catch (Exception e) {
            LowdFX.LOG.error("Fehler beim Speichern der Playtime-Daten!", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void load() {
        try (FileReader reader = new FileReader(LowdFX.DATA_DIR.resolve("playtime.json").toFile())) {
            PLAYTIMES.putAll(GSON.fromJson(reader, Map.class));
        } catch (Exception e) {
            LowdFX.LOG.error("Fehler beim Speichern der Playtime-Daten!", e);
        }
    }
}
