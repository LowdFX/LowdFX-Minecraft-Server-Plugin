package at.lowdfx.lowdfx.commands.basicCommands.vanishCommand;

import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class InvisiblePlayerHandler implements Listener {

    private final JavaPlugin plugin;
    private final Map<Player, BossBar> playerBossBars = new HashMap<>();
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private File vanishedPlayersFile;
    private FileConfiguration vanishedPlayersConfig;

    public InvisiblePlayerHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        // Erstellen oder Öffnen der Datei "VanishedPlayers.yml"
        this.vanishedPlayersFile = new File(plugin.getDataFolder(), "VanishedPlayers.yml");
        if (!vanishedPlayersFile.exists()) {
            plugin.saveResource("VanishedPlayers.yml", false); // Erstelle die Datei beim ersten Start
        }

        // Laden der Konfiguration der neuen Datei
        this.vanishedPlayersConfig = YamlConfiguration.loadConfiguration(vanishedPlayersFile);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Spieler unsichtbar machen und BossBar hinzufügen
    public void makePlayerInvisible(Player player) {

        // Überprüfe, ob der Spieler bereits eine BossBar hat und entferne sie
        if (playerBossBars.containsKey(player)) {
            BossBar existingBossBar = playerBossBars.get(player);
            existingBossBar.removePlayer(player);  // Entfernen der bestehenden BossBar
        }

        // Neue BossBar erstellen
        BossBar bossBar = Bukkit.createBossBar(
                "Vanish",
                BarColor.RED,
                BarStyle.SOLID,
                new BarFlag[0]
        );

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                onlinePlayer.hidePlayer(plugin, player);
            }
        }

        player.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);

        playerBossBars.put(player, bossBar);
        vanishedPlayers.add(player.getUniqueId());
    }

    // Spieler sichtbar machen und BossBar entfernen
    public void makePlayerVisible(Player player) {
        if (!playerBossBars.containsKey(player)) {
            return;
        }

        BossBar bossBar = playerBossBars.remove(player);
        if (bossBar != null) {
            bossBar.removePlayer(player);
            bossBar.setVisible(false); // Optional, falls Spieler schon entfernt wurde
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                onlinePlayer.showPlayer(plugin, player);
            }
        }

        player.removeMetadata("vanished", plugin);
        vanishedPlayers.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joinedPlayer = event.getPlayer();

        // Verhindere jegliche Standard-Join-Nachrichten (auch von Plugins).
        event.setJoinMessage(null);

        // Nur eine Nachricht wird hier gesendet, wenn der Spieler vanishing hat.
        if (vanishedPlayers.contains(joinedPlayer.getUniqueId())) {
            makePlayerInvisible(joinedPlayer);

            // Sende deine eigene Nachricht (nachdem das Setzen von setJoinMessage null verhindert wurde)
            if (!joinedPlayer.hasMetadata("vanishedSent")) {
                // Sende Nachricht nur einmal!
                joinedPlayer.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD
                        + lowdfx.config.getString("basic.servername")
                        + ChatColor.GRAY + " >> " + ChatColor.GREEN + "Du bist vanished!");

                // Markiere, dass die Nachricht für diesen Spieler gesendet wurde, um Duplikate zu vermeiden
                joinedPlayer.setMetadata("vanishedSent", new FixedMetadataValue(plugin, true));
            }
        }

        // Verstecke vanished-Spieler vor dem beigetretenen Spieler
        for (Map.Entry<Player, BossBar> entry : playerBossBars.entrySet()) {
            Player vanishedPlayer = entry.getKey();
            if (vanishedPlayer.hasMetadata("vanished")) {
                joinedPlayer.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }



    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player leavingPlayer = event.getPlayer();
        if (leavingPlayer.hasMetadata("vanished")) {
            vanishedPlayers.add(leavingPlayer.getUniqueId());
            event.setQuitMessage(null);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player target = (Player) event.getTarget();
            if (target.hasMetadata("vanished")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player target = (Player) event.getEntity();
            if (target.hasMetadata("vanished")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player target = (Player) event.getEntity();
            if (target.hasMetadata("vanished")) {
                event.setCancelled(true);
            }
        }
    }



    public void saveVanishedPlayers() {
        File file = new File(lowdfx.getDataFolde(), "VanishedPlayers.yml");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<String> vanishedPlayerIds = new ArrayList<>();
        for (UUID uuid : vanishedPlayers) {
            vanishedPlayerIds.add(uuid.toString()); // UUIDs in Strings umwandeln
        }

        config.set("vanished", vanishedPlayerIds);

        try {
            config.save(file); // Speichern der Datei
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void loadVanishedPlayers() {
        File file = new File(lowdfx.getDataFolde(), "VanishedPlayers.yml");

        if (!file.exists()) {
            return; // Wenn die Datei nicht existiert, gibt es keine "vanished"-Spieler
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> vanishedPlayerIds = config.getStringList("vanished");

        vanishedPlayers.clear(); // Liste zurücksetzen
        for (String id : vanishedPlayerIds) {
            try {
                vanishedPlayers.add(UUID.fromString(id));  // UUID hinzufügen
            } catch (IllegalArgumentException e) {
                e.printStackTrace(); // Fehlerbehandlung falls ungültige UUIDs vorhanden sind
            }
        }

    }


    public Set<UUID> getVanishedPlayers() {
        return vanishedPlayers;  // Gibt das Set von Spielern, die vanish sind, zurück
    }


}
