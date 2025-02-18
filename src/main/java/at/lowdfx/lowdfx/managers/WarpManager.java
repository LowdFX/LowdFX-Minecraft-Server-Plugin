package at.lowdfx.lowdfx.managers;


import at.lowdfx.lowdfx.LowdFX;
import com.marcpg.libpg.storage.JsonUtils;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class WarpManager {
    public static final Map<String, Location> WARPS = new HashMap<>();

    public static void save() {
        JsonUtils.saveSafe(WARPS, LowdFX.DATA_DIR.resolve("warps.json").toFile());
    }

    public static void load() {
        WARPS.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("warps.json").toFile(), Map.of()));
    }

    public static void set(String name, Location location) {
        WARPS.put(name, location);
        save();
    }

    public static void remove(String name) {
        WARPS.remove(name);
        save();
    }
}
