package at.lowdfx.lowdfx.util;

import at.lowdfx.lowdfx.LowdFX;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class Utilities {
    public static @NotNull Set<Location> connectedChests(Block chestBlock) {
        Set<Location> connectedChests = new HashSet<>();
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block relativeBlock = chestBlock.getRelative(face);
            if (relativeBlock.getState() instanceof Chest)
                connectedChests.add(relativeBlock.getLocation());
        }
        return connectedChests;
    }

    public static List<String> getOnlinePlayers() {
        // Use legacy Collectors#toList instead of Stream#toList, as it returns a modifiable ArrayList.
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    public static long currentTimeSecs() {
        return (long) (System.currentTimeMillis() * 0.001);
    }

    public static <K, V> Map.@Nullable Entry<K, V> getEntryByValue(@NotNull Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value))
                return entry;
        }
        return null;
    }
}
