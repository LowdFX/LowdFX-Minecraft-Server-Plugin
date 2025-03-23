package at.lowdfx.lowdfx.util;

import at.lowdfx.lowdfx.LowdFX;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.ban.BanListType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

public final class Utilities {
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

    public static String getServerProperty(@NotNull String key) {
        try (InputStream input = new FileInputStream(new File(Bukkit.getWorldContainer(), "server.properties"))) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty(key);
        } catch (IOException e) {
            LowdFX.LOG.error("Konnte nicht server.properties lesen.", e);
            throw new RuntimeException(e);
        }
    }

    public static Block connectedChest(Block block) {
        if (block != null && block.getBlockData() instanceof Chest chestBlockData && chestBlockData.getType() != Chest.Type.SINGLE) {
            BlockFace direction = switch (chestBlockData.getFacing()) {
                case NORTH -> chestBlockData.getType() == Chest.Type.LEFT ? BlockFace.EAST : BlockFace.WEST;
                case EAST -> chestBlockData.getType() == Chest.Type.LEFT ? BlockFace.SOUTH : BlockFace.NORTH;
                case SOUTH -> chestBlockData.getType() == Chest.Type.LEFT ? BlockFace.WEST : BlockFace.EAST;
                case WEST -> chestBlockData.getType() == Chest.Type.LEFT ? BlockFace.NORTH : BlockFace.SOUTH;
                default -> null;
            };
            return direction == null ? null : block.getRelative(direction);
        }
        return null;
    }

    public static @NotNull Component joinMessage(@NotNull Player player) {
        return LowdFX.serverMessage((player.hasPlayedBefore() ? Configuration.CONNECTION_JOIN : Configuration.CONNECTION_FIRST_JOIN)
                .replaceText(TextReplacementConfig.builder().match("\\{0\\}").replacement(player.name()).build()));
    }

    public static @NotNull Component quitMessage(@NotNull Player player) {
        return LowdFX.serverMessage(Configuration.CONNECTION_QUIT.replaceText(TextReplacementConfig.builder().match("\\{0\\}").replacement(player.name()).build()));
    }

    /**
     * Bannt das Profil und – falls der Spieler online ist – auch dessen IP.
     * Für den IP-Ban wird der Grund um einen Marker "[UUID:<SpielerUUID>]" erweitert,
     * sodass man später auch bei Offline-Spielern den IP-Ban anhand dieses Markers aufheben kann.
     */
    public static void ban(PlayerProfile target, Component reason, @Nullable Duration duration, String source) {
        String stringReason = LegacyComponentSerializer.legacySection().serialize(reason);
        // Umrechnung von Duration zu Date
        Date expiration = null;
        if (duration != null) {
            expiration = new Date(System.currentTimeMillis() + duration.toMillis());
        }

        String targetName = target.getName();
        if (targetName == null) {
            targetName = target.getId().toString();
        }
        // Profil-Ban hinzufügen (verwende targetName als Schlüssel)
        Bukkit.getBanList(BanListType.PROFILE).addBan(targetName, stringReason, expiration, source);

        Player t = Bukkit.getPlayer(target.getId());
        if (t != null && t.getAddress() != null) {
            String ip = t.getAddress().getAddress().getHostAddress();
            // Den IP-Ban-Grund um einen Marker erweitern, damit er später gefunden werden kann
            String ipBanReason = stringReason + " [UUID:" + target.getId() + "]";
            Bukkit.getBanList(BanListType.IP).addBan(ip, ipBanReason, expiration, source);
            t.kick(reason);
        }
    }


    /**
     * Hebt den Profil- und IP-Ban auf.
     * Bei den IP-Bans wird die Liste aller IP-Ban-Einträge durchlaufen und
     * es werden alle entfernt, deren Grund den Marker "[UUID:<SpielerUUID>]" enthält.
     */
    public static void unban(PlayerProfile target) {
        if (target == null) return;
        String targetName = target.getName();
        String targetUUID = target.getId().toString();
        // Direkt versuchen, den Ban anhand des Spielernamens aufzuheben:
        if (targetName != null) {
            Bukkit.getBanList(BanListType.PROFILE).pardon(targetName);
        }
        // Zusätzlich versuchen, falls die Ban-Liste den Spieler anhand der UUID speichert:
        Bukkit.getBanList(BanListType.PROFILE).pardon(targetUUID);

        // IP-Ban aufheben: Durchsuche die IP-Ban-Einträge nach dem Marker "[UUID:<targetUUID>]"
        String uuidMarker = "[UUID:" + targetUUID + "]";
        Bukkit.getBanList(BanListType.IP).getBanEntries().forEach(entry -> {
            String banReason = entry.getReason();
            if (banReason != null && banReason.contains(uuidMarker)) {
                Bukkit.getBanList(BanListType.IP).pardon(entry.getTarget());
            }
        });
    }


    public static void positiveSound(@NotNull Player player) {
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    public static void negativeSound(@NotNull Player player) {
        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    public static <T> int removeIf(@NotNull java.util.List<T> list, Predicate<? super T> filter) {
        int removed = 0;
        Iterator<T> each = list.iterator();
        while (each.hasNext()) {
            if (filter.test(each.next())) {
                each.remove();
                removed++;
            }
        }
        return removed;
    }
}
