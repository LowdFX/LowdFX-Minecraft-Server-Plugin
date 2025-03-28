package at.lowdfx.lowdfx.managers.moderation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.Perms;
import com.google.gson.reflect.TypeToken;
import com.marcpg.libpg.storage.JsonUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class VanishManager {
    private static final BossBar BOSS_BAR = BossBar.bossBar(Component.text("Vanish"), 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
    private static final Set<UUID> VANISHED = new HashSet<>();

    public static void save() {
        JsonUtils.saveSafe(VANISHED, LowdFX.DATA_DIR.resolve("vanished.json").toFile());
    }

    public static void load() {
        VANISHED.addAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("vanished.json").toFile(), List.of(), new TypeToken<>() {}));
    }

    public static void applyNametag(Player vanishedPlayer) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = viewer.getScoreboard();
            String teamName = "vanish_" + vanishedPlayer.getName().substring(0, Math.min(14, vanishedPlayer.getName().length()));

            // Hol oder erstell Team
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
            }

            // Sichtbarkeit nur für Spieler mit Permission
            if (Perms.check(viewer, Perms.Perm.VANISH)) {
                team.prefix(Component.text("[VANISHED] ", NamedTextColor.RED));
            } else {
                team.prefix(Component.empty());
            }

            team.addEntry(vanishedPlayer.getName());
        }
    }

    public static void resetNametag(Player player) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = viewer.getScoreboard();
            for (Team team : scoreboard.getTeams()) {
                if (team.hasEntry(player.getName())) {
                    team.removeEntry(player.getName());
                }
            }
        }
    }


    // Spieler unsichtbar machen und BossBar anzeigen.
    public static void makePlayerInvisible(@NotNull Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(player) && !Perms.check(player, Perms.Perm.VANISH)) {
                onlinePlayer.hidePlayer(LowdFX.PLUGIN, player);
            }

        }

        player.setMetadata("vanished", new FixedMetadataValue(LowdFX.PLUGIN, true));
        BOSS_BAR.addViewer(player);
        VANISHED.add(player.getUniqueId());
    }

    // Spieler sichtbar machen und BossBar wegmachen.
    public static void makePlayerVisible(@NotNull Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                onlinePlayer.showPlayer(LowdFX.PLUGIN, player);
            }
        }

        player.removeMetadata("vanished", LowdFX.PLUGIN);
        BOSS_BAR.removeViewer(player);
        VANISHED.remove(player.getUniqueId());
    }

    public static Set<UUID> getVanishedPlayers() {
        return VANISHED;
    }
}
