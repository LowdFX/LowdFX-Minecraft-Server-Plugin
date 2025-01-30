package at.lowdfx.lowdfx.moderation;

import at.lowdfx.lowdfx.LowdFX;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VanishingHandler {

    private final Map<Player, BossBar> playerBossBars = new HashMap<>();
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    public VanishingHandler() {
        // Erstellen oder Öffnen der Datei "VanishedPlayers.yml"
        File vanishedPlayersFile = LowdFX.DATA_DIR.resolve("VanishedPlayers.yml").toFile();
        if (!vanishedPlayersFile.exists()) {
            LowdFX.PLUGIN.saveResource("VanishedPlayers.yml", false); // Erstelle die Datei beim ersten Start
        }
    }

    // Spieler unsichtbar machen und BossBar hinzufügen
    public void makePlayerInvisible(Player player) {
        // Überprüfe, ob der Spieler bereits eine BossBar hat und entferne sie
        if (playerBossBars.containsKey(player)) {
            BossBar existingBossBar = playerBossBars.get(player);
            existingBossBar.removeViewer(player); // Entfernen der bestehenden BossBar
        }

        // Neue BossBar erstellen
        BossBar bossBar = BossBar.bossBar(Component.text("Vanish"), 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                onlinePlayer.hidePlayer(LowdFX.PLUGIN, player);
            }
        }

        player.setMetadata("vanished", new FixedMetadataValue(LowdFX.PLUGIN, true));
        bossBar.addViewer(player);

        playerBossBars.put(player, bossBar);
        vanishedPlayers.add(player.getUniqueId());
    }

    public void saveVanishedPlayers() {
        File file = LowdFX.DATA_DIR.resolve("VanishedPlayers.yml").toFile();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<String> vanishedPlayerIds = new ArrayList<>();
        for (UUID uuid : vanishedPlayers) {
            vanishedPlayerIds.add(uuid.toString()); // UUIDs in Strings umwandeln
        }

        config.set("vanished", vanishedPlayerIds);

        try {
            config.save(file); // Speichern der Datei
        } catch (IOException e) {
            LowdFX.LOG.error("Konnte nicht vanish Spieler speichern.", e);
        }
    }

    public void loadVanishedPlayers() {
        File file = LowdFX.DATA_DIR.resolve("VanishedPlayers.yml").toFile();
        if (!file.exists())
            return; // Wenn die Datei nicht existiert, gibt es keine "vanished"-Spieler

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> vanishedPlayerIds = config.getStringList("vanished");

        vanishedPlayers.clear(); // Liste zurücksetzen
        for (String id : vanishedPlayerIds) {
            try {
                vanishedPlayers.add(UUID.fromString(id));  // UUID hinzufügen
            } catch (IllegalArgumentException e) {
                LowdFX.LOG.error("Konnte nicht vanish Spieler laden.", e);
            }
        }

    }

    public Map<Player, BossBar> playerBossBars() {
        return playerBossBars;
    }

    public Set<UUID> getVanishedPlayers() {
        return vanishedPlayers;  // Gibt das Set von Spielern, die vanish sind, zurück
    }
}
