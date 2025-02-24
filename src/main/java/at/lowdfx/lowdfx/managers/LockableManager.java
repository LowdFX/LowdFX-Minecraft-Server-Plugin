package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.SimpleLocation;
import at.lowdfx.lowdfx.util.Utilities;
import com.marcpg.libpg.storage.JsonUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LockableManager {
    public record Locked(UUID owner, SimpleLocation location, ArrayList<UUID> whitelist) {
        public void removeWhitelist(UUID player) {
            whitelist.remove(player);
        }

        public void addWhitelist(UUID player) {
            if (player == owner) return;
            whitelist.add(player);
        }

        public boolean isOwner(@NotNull Player player) {
            return owner.equals(player.getUniqueId()) || Perms.check(player, Perms.Perm.CHEST_SHOP_ADMIN);
        }

        public boolean notAllowed(@NotNull Player player) {
            return !whitelist.contains(player.getUniqueId()) && !isOwner(player);
        }
    }

    public static final Map<UUID, Map<SimpleLocation, Locked>> LOCKED = new HashMap<>();

    public static void save() {
        JsonUtils.saveSafe(LOCKED, LowdFX.DATA_DIR.resolve("locked.json").toFile());
    }

    public static void load() {
        LOCKED.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("locked.json").toFile(), Map.of(), Map.class));
    }

    public static void lock(UUID owner, @NotNull Block block) {
        lock(block, new Locked(owner, SimpleLocation.ofLocation(block.getLocation()), new ArrayList<>()));
    }

    public static void lock(@NotNull Block block, @NotNull Locked data) {
        Map<SimpleLocation, Locked> ownerLocked = LOCKED.computeIfAbsent(data.owner(), k -> new HashMap<>());
        ownerLocked.put(data.location, data);

        if (!(block.getState() instanceof Chest)) return;
        Block connectedChest = Utilities.connectedChest(data.location().asLocation().getBlock());
        if (connectedChest != null && !ownerLocked.containsKey(SimpleLocation.ofLocation(connectedChest.getLocation())))
            ownerLocked.put(SimpleLocation.ofLocation(connectedChest.getLocation()), data);
    }

    public static void unlock(@NotNull Location location) {
        Block connected = location.getBlock() instanceof Chest ? Utilities.connectedChest(location.getBlock()) : null;
        for (Map.Entry<UUID, Map<SimpleLocation, Locked>> e : LOCKED.entrySet()) {
            if (e.getValue().remove(SimpleLocation.ofLocation(location)) == null) continue;
            if (connected != null)
                e.getValue().remove(SimpleLocation.ofLocation(connected.getLocation()));
            break;
        }
    }

    public static boolean isLocked(Location location) {
        if (location == null) return false;
        for (Map.Entry<UUID, Map<SimpleLocation, Locked>> e : LOCKED.entrySet()) {
            if (e.getValue().containsKey(SimpleLocation.ofLocation(location))) return true;
        }
        return false;
    }

    public static @Nullable Locked getLocked(Location location) {
        if (location == null) return null;
        for (Map.Entry<UUID, Map<SimpleLocation, Locked>> e : LOCKED.entrySet()) {
            Locked d = e.getValue().get(SimpleLocation.ofLocation(location));
            if (d != null)
                return d;
        }
        return null;
    }

    public static boolean notLockable(Block block) {
        return block == null || !(block.getState() instanceof Container || block.getBlockData() instanceof Openable);
    }
}
