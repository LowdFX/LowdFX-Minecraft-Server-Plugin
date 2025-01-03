package at.lowdfx.lowdfx.commands.chestLock;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ChestData {
    private final lowdfx plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    public ChestData(lowdfx plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "chestdata.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public boolean isChestLocked(Location location) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        boolean locked = dataConfig.getBoolean(path + ".locked", false);  // Standardwert false, Kisten sind zu Beginn nicht gesperrt
        return locked;
    }

    public void addLockedChest(Location location, String player) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        dataConfig.set(path + ".locked", true);
        dataConfig.set(path + ".owner", player);
        dataConfig.set(path + ".whitelist", List.of(player));  // Füge den Spieler zur Whitelist hinzu
        save();
    }

    public Set<Location> getConnectedChests(Location location) {
        Set<Location> connectedChests = new HashSet<>();
        Block block = location.getBlock();

        // Wenn der Block keine Kiste ist, gebe eine leere Menge zurück
        if (block.getType() != Material.CHEST && !block.getType().name().endsWith("SHULKER_BOX")) return connectedChests;

        // Überprüfen der benachbarten Blöcke (Norden, Osten, Süden, Westen)
        for (Block neighbor : new Block[]{
                block.getRelative(1, 0, 0), // Nachbar im Osten
                block.getRelative(-1, 0, 0), // Nachbar im Westen
                block.getRelative(0, 0, 1), // Nachbar im Süden
                block.getRelative(0, 0, -1)}) { // Nachbar im Norden

            // Wenn der benachbarte Block eine Kiste ist, füge ihn zur Liste hinzu
            if (neighbor.getType() == Material.CHEST && block.getType().name().endsWith("SHULKER_BOX")) {
                connectedChests.add(neighbor.getLocation());
            }
        }

        // Füge die aktuelle Kiste ebenfalls hinzu
        connectedChests.add(location);

        return connectedChests;
    }
    public void lockAdjacentChests(Location chestLocation, String playerName) {
        // Hole alle benachbarten Kisten
        Set<Location> connectedChests = getConnectedChests(chestLocation);

        // Sperre die Zielkiste und alle angrenzenden Kisten
        addLockedChest(chestLocation, playerName);

        for (Location adjacentLocation : connectedChests) {
            addLockedChest(adjacentLocation, playerName);
        }
    }

    public void removeDestroyedChest(Location location) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();

        if (dataConfig.contains(path)) {
            dataConfig.set(path, null);  // Entfernt den gesamten Kistenpfad
            save();
        }
        //plugin.getLogger().info("Kiste " + location + " wurde gelöscht von " + player);
    }



    public boolean isPlayerInWhitelist(Location location, String player) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".whitelist";
        List<String> whitelist = dataConfig.getStringList(path);
        return whitelist.contains(player);
    }

    public void addPlayerToWhitelist(Location location, String player) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".whitelist";
        List<String> whitelist = dataConfig.getStringList(path);
        if (!whitelist.contains(player)) {
            whitelist.add(player);
            dataConfig.set(path, whitelist);
            save();
        }
    }

    public void removePlayerFromWhitelist(Location location, String player) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".whitelist";
        List<String> whitelist = dataConfig.getStringList(path);
        whitelist.remove(player);
        dataConfig.set(path, whitelist);
        save();
    }

    public void removeLockedChest(Location location) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        dataConfig.set(path + ".locked", false);
        save();
    }

    public String getChestOwner(Location location) {
        String path = "chests." + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ() + ".owner";
        return dataConfig.getString(path, "");
    }

    public boolean isOwner(String playerName, Location chestLocation) {
        // Holen Sie sich den Besitzer der Kiste aus den Daten (beispielsweise mit einer HashMap)
        String owner = getChestOwner(chestLocation);  // Methode, die den Besitzer aus den Daten zurückgibt
        return owner != null && owner.equals(playerName);
    }

    public void save() {
        if (dataConfig == null || dataFile == null) {
            return;
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
