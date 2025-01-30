package at.lowdfx.lowdfx.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Utilities {
    public static @NotNull Set<Location> connectedChests(Block chestBlock) {
        Set<Location> connectedChests = new HashSet<>();
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block relativeBlock = chestBlock.getRelative(face);
            if (relativeBlock.getType() == Material.CHEST) {
                connectedChests.add(relativeBlock.getLocation());
            }
        }
        return connectedChests;
    }

    public static @NotNull List<String> allTabCompletions(String... other) {
        List<String> all = new ArrayList<>();
        all.addAll(getOnlinePlayers());
        all.addAll(List.of(other));
        return all;
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
