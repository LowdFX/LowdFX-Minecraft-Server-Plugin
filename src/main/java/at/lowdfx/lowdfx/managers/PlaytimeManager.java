package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.PlaytimeInfo;
import com.marcpg.libpg.storage.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlaytimeManager {
    public static final Map<UUID, PlaytimeInfo> PLAYTIMES = new HashMap<>();

    public static void save() {
        JsonUtils.saveSafe(PLAYTIMES, LowdFX.DATA_DIR.resolve("playtime.json").toFile());
    }

    public static void load() {
        PLAYTIMES.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("playtime.json").toFile(), Map.of()));
    }
}
