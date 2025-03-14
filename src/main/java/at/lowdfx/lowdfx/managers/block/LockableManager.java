package at.lowdfx.lowdfx.managers.block;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.SimpleLocation;
import at.lowdfx.lowdfx.util.Utilities;
import com.google.gson.reflect.TypeToken;
import com.marcpg.libpg.storage.JsonUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class LockableManager {
    public static final class Locked {
        private final UUID owner;
        private final SimpleLocation location;
        public SimpleLocation connected;
        private final ArrayList<UUID> whitelist;

        public Locked(UUID owner, SimpleLocation location, SimpleLocation connected, ArrayList<UUID> whitelist) {
            this.owner = owner;
            this.location = location;
            this.connected = connected;
            this.whitelist = whitelist;
        }

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

        public boolean isBlock(SimpleLocation loc) {
            return Objects.equals(loc, location) || Objects.equals(loc, connected);
        }

        public UUID owner() {
            return owner;
        }

        public SimpleLocation location() {
            return location;
        }

        public ArrayList<UUID> whitelist() {
            return whitelist;
        }
    }

    public static final Map<UUID, ArrayList<Locked>> LOCKED = new HashMap<>();

    public static void save() {
        JsonUtils.saveSafe(LOCKED, LowdFX.DATA_DIR.resolve("locked.json").toFile());
    }

    public static void load() {
        LOCKED.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("locked.json").toFile(), Map.of(), new TypeToken<>() {}));
    }

    public static void lock(UUID owner, @NotNull Block block) {
        lock(block, new Locked(owner, SimpleLocation.ofLocation(block.getLocation()), null, new ArrayList<>()));
    }

    public static void lock(@NotNull Block block, @NotNull Locked data) {
        ArrayList<Locked> ownerLocked = LOCKED.computeIfAbsent(data.owner(), k -> new ArrayList<>());
        ownerLocked.add(data);

        if (!(block.getState() instanceof Chest)) return;
        Block connectedChest = Utilities.connectedChest(data.location().asLocation().getBlock());
        if (connectedChest != null && ownerLocked.stream().noneMatch(s -> s.isBlock(SimpleLocation.ofLocation(connectedChest.getLocation())))) {
            data.connected = SimpleLocation.ofLocation(connectedChest.getLocation());
        }
    }

    public static void unlock(@NotNull Location location) {
        Block connected = location.getBlock().getState() instanceof Chest ? Utilities.connectedChest(location.getBlock()) : null;
        for (Map.Entry<UUID, ArrayList<Locked>> e : LOCKED.entrySet()) {
            if (!e.getValue().removeIf(s -> s.isBlock(SimpleLocation.ofLocation(location)))) continue;
            if (connected != null)
                e.getValue().removeIf(s -> s.isBlock(SimpleLocation.ofLocation(connected.getLocation())));
            break;
        }
    }

    public static boolean isLocked(Location location) {
        if (location == null) return false;
        for (Map.Entry<UUID, ArrayList<Locked>> e : LOCKED.entrySet()) {
            if (e.getValue().stream().anyMatch(s -> s.isBlock(SimpleLocation.ofLocation(location)))) return true;
        }
        return false;
    }

    public static @Nullable Locked getLocked(Location location) {
        if (location == null) return null;
        for (Map.Entry<UUID, ArrayList<Locked>> e : LOCKED.entrySet()) {
            Optional<Locked> d = e.getValue().stream().filter(s -> s.isBlock(SimpleLocation.ofLocation(location))).findFirst();
            if (d.isPresent())
                return d.get();
        }
        return null;
    }

    public static boolean notLockable(Block block) {
        return block == null || !(block.getState() instanceof Container || block.getBlockData() instanceof Openable);
    }
}
