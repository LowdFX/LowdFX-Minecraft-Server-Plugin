package at.lowdfx.lowdfx.inventory;

import at.lowdfx.lowdfx.LowdFX;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LockableData {
    private static final YamlConfiguration DATA = new YamlConfiguration();

    public static void load() {
        try {
            if (LowdFX.DATA_DIR.resolve("lock-data.yml").toFile().createNewFile()) {
                LowdFX.LOG.info("Lock data Datei erstellt.");
            }
            DATA.load(LowdFX.DATA_DIR.resolve("lock-data.yml").toFile());
        } catch (Exception e) { // Generell Exception, weil das in dem fall recht egal ist. Sonst immer spezifischer sein!
            LowdFX.LOG.warn("Konnte lock data Datei nicht erstellen und laden.");
        }
    }

    public static void save() {
        try {
            DATA.save(LowdFX.DATA_DIR.resolve("lock-data.yml").toFile());
        } catch (IOException e) {
            LowdFX.LOG.error("Konnte nicht chest data speichern.", e);
        }
    }

    public static boolean isLocked(@NotNull Location location) {
        return DATA.getBoolean("blocks." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".locked", false); // Standardwert false, Kisten sind zu Beginn nicht gesperrt
    }

    public static void addLocked(@NotNull Location location, String player) {
        String path = "blocks." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        DATA.set(path + ".locked", true);
        DATA.set(path + ".owner", player);
        DATA.set(path + ".whitelist", List.of(player));  // Füge den Spieler zur Whitelist hinzu
        save();
    }

    public static @NotNull Set<Location> getConnectedChests(@NotNull Location location) {
        Set<Location> connectedChests = new HashSet<>();
        Block block = location.getBlock();

        // Wenn der Block keine Kiste ist, gebe eine leere Menge zurück
        if (block.getBlockData() instanceof Chest) return connectedChests;

        // Überprüfen der benachbarten Blöcke (Norden, Osten, Süden, Westen)
        for (Block neighbor : new Block[]{
                block.getRelative(1, 0, 0), // Nachbar im Osten
                block.getRelative(-1, 0, 0), // Nachbar im Westen
                block.getRelative(0, 0, 1), // Nachbar im Süden
                block.getRelative(0, 0, -1)}) { // Nachbar im Norden

            // Wenn der benachbarte Block eine Kiste ist, füge ihn zur Liste hinzu
            if (block.getBlockData() instanceof Chest) {
                connectedChests.add(neighbor.getLocation());
            }
        }

        // Füge die aktuelle Kiste ebenfalls hinzu
        connectedChests.add(location);

        return connectedChests;
    }

    public static void lockAdjacentChests(Location chestLocation, String playerName) {
        addLocked(chestLocation, playerName);
        getConnectedChests(chestLocation).forEach(c -> addLocked(c, playerName));
    }

    public static void unlockAdjacentChests(Location chestLocation) {
        removeLocked(chestLocation);
        getConnectedChests(chestLocation).forEach(LockableData::removeLocked);
    }

    public static void removeDestroyedBlock(@NotNull Location location) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();

        if (DATA.contains(path)) {
            DATA.set(path, null);  // Entfernt den gesamten Kistenpfad
            save();
        }
    }

    public static boolean isPlayerInWhitelist(@NotNull Location location, String player) {
        return DATA.getStringList("chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".whitelist").contains(player);
    }

    public static void addWhitelisted(@NotNull Location location, @NotNull Collection<String> player) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".whitelist";
        List<String> whitelist = DATA.getStringList(path);
        for (String p : player) {
            if (!whitelist.contains(p))
                whitelist.add(p);
        }
        DATA.set(path, whitelist);
        save();
    }

    public static void removeWhitelisted(@NotNull Location location, Collection<String> players) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".whitelist";
        List<String> whitelist = DATA.getStringList(path);
        whitelist.removeAll(players);
        DATA.set(path, whitelist);
        save();
    }

    public static @NotNull List<String> whitelist(@NotNull Location location) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".whitelist";
        return DATA.getStringList(path);
    }

    public static void removeLocked(@NotNull Location location) {
        DATA.set("chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".locked", false);
        save();
    }

    public static String getChestOwner(@NotNull Location location) {
        return DATA.getString("chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".owner", "");
    }

    public static boolean notOwner(String playerName, Location chestLocation) {
        // Holen Sie sich den Besitzer der Kiste aus den Daten (beispielsweise mit einer HashMap)
        return !getChestOwner(chestLocation).equals(playerName);
    }
}
