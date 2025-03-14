package at.lowdfx.lowdfx.managers.teleport;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.SimpleLocation;
import at.lowdfx.lowdfx.util.Utilities;
import com.google.gson.reflect.TypeToken;
import com.marcpg.libpg.storage.JsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SpawnManager {
    public static final String DEFAULT_SPAWN_NAME = "spawn";
    public static final Map<String, SimpleLocation> SPAWNS = new HashMap<>();

    public static void save() {
        JsonUtils.saveSafe(SPAWNS, LowdFX.DATA_DIR.resolve("spawns.json").toFile());
    }

    public static void load() {
        SPAWNS.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("spawns.json").toFile(), Map.of(), new TypeToken<>() {}));
        if (!SPAWNS.containsKey(DEFAULT_SPAWN_NAME))
            SPAWNS.put(DEFAULT_SPAWN_NAME, SimpleLocation.ofLocation(Objects.requireNonNull(Bukkit.getWorld(Utilities.getServerProperty("level-name"))).getSpawnLocation()));
    }

    // Methode, die den passenden Spawn für den Spieler holt (z.B. mit Permissions).
    public static @NotNull SimpleLocation getSpawn(Player player) {
        for (Map.Entry<String, SimpleLocation> e : SPAWNS.entrySet()) {
            if (player.hasPermission("spawn." + e.getKey())) {
                return e.getValue();
            }
        }
        return SPAWNS.get(DEFAULT_SPAWN_NAME);
    }

    // Einen Spawn festlegen.
    public static void setSpawn(String name, Location location) {
        SPAWNS.put(name, location == null ? null : SimpleLocation.ofLocation(location));
    }
}
