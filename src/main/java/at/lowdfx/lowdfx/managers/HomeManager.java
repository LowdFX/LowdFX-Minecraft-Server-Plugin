package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.SimpleLocation;
import com.marcpg.libpg.storage.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HomeManager {
    public static final Map<UUID, Map<String, SimpleLocation>> HOMES = new HashMap<>();

    public static void save() {
        JsonUtils.saveSafe(HOMES, LowdFX.DATA_DIR.resolve("homes.json").toFile());
    }

    public static void load() {
        HOMES.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("homes.json").toFile(), Map.of(), Map.class));
    }

    public static void add(UUID player) {
        if (HOMES.containsKey(player)) return;
        HOMES.put(player, new HashMap<>());
    }

    public static Map<String, SimpleLocation> get(UUID player) {
        return HOMES.get(player);
    }
}
