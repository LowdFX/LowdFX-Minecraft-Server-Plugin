package at.lowdfx.lowdfx.inventory;

import at.lowdfx.lowdfx.LowdFX;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class LockableData {
    private final File dataFile;
    private final FileConfiguration dataConfig;

    public LockableData() {
        this.dataFile = LowdFX.DATA_DIR.resolve("lock-data.yml").toFile();
        try {
            if (!dataFile.createNewFile()) {
                LowdFX.LOG.info("Lock data Datei erstellt.");
            }
        } catch (IOException e) {
            LowdFX.LOG.warn("Konnte lock data Datei nicht erstellen.");
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public boolean isLocked(@NotNull Location location) {
        return dataConfig.getBoolean("blocks." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".locked", false); // Standardwert false, Kisten sind zu Beginn nicht gesperrt
    }

    public void addLocked(@NotNull Location location, String player) {
        String path = "blocks." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        dataConfig.set(path + ".locked", true);
        dataConfig.set(path + ".owner", player);
        dataConfig.set(path + ".whitelist", List.of(player));  // Füge den Spieler zur Whitelist hinzu
        save();
    }

    public Set<Location> getConnectedChests(@NotNull Location location) {
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

    public void lockAdjacentChests(Location chestLocation, String playerName) {
        addLocked(chestLocation, playerName);
        getConnectedChests(chestLocation).forEach(c -> addLocked(c, playerName));
    }

    public void unlockAdjacentChests(Location chestLocation) {
        removeLocked(chestLocation);
        getConnectedChests(chestLocation).forEach(this::removeLocked);
    }

    public void removeDestroyedBlock(@NotNull Location location) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();

        if (dataConfig.contains(path)) {
            dataConfig.set(path, null);  // Entfernt den gesamten Kistenpfad
            save();
        }
    }

    public boolean isPlayerInWhitelist(@NotNull Location location, String player) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".whitelist";
        List<String> whitelist = dataConfig.getStringList(path);
        return whitelist.contains(player);
    }

    public void addWhitelisted(@NotNull Location location, @NotNull Collection<String> player) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".whitelist";
        List<String> whitelist = dataConfig.getStringList(path);
        for (String p : player) {
            if (!whitelist.contains(p))
                whitelist.add(p);
        }
        dataConfig.set(path, whitelist);
        save();
    }

    public void removeWhitelisted(@NotNull Location location, Collection<String> players) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".whitelist";
        List<String> whitelist = dataConfig.getStringList(path);
        whitelist.removeAll(players);
        dataConfig.set(path, whitelist);
        save();
    }

    public void removeLocked(@NotNull Location location) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        dataConfig.set(path + ".locked", false);
        save();
    }

    public String getChestOwner(@NotNull Location location) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".owner";
        return dataConfig.getString(path, "");
    }

    public boolean notOwner(String playerName, Location chestLocation) {
        // Holen Sie sich den Besitzer der Kiste aus den Daten (beispielsweise mit einer HashMap)
        String owner = getChestOwner(chestLocation); // Methode, die den Besitzer aus den Daten zurückgibt
        return owner == null || !owner.equals(playerName);
    }

    public void save() {
        if (dataConfig == null || dataFile == null) return;
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            LowdFX.LOG.error("Konnte nicht chest data speichern.", e);
        }
    }
}
