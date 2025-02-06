package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.util.FileUtils;
import at.lowdfx.lowdfx.util.PlaytimeInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlaytimeManager {
    public static final Map<UUID, PlaytimeInfo> PLAYTIMES = new HashMap<>();

    public static void save() {
        FileUtils.save(PLAYTIMES, "playtime.json");
    }

    public static void load() {
        PLAYTIMES.putAll(FileUtils.load("playtime.json", Map.of()));
    }
}
