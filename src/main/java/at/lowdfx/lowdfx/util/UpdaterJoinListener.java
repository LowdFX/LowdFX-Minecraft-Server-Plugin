package at.lowdfx.lowdfx.util;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdaterJoinListener implements Listener {

    private final JavaPlugin plugin;
    private final String updateUrl;
    private final String downloadLink;

    /**
     * Erzeugt einen neuen UpdaterJoinListener.
     *
     * @param plugin       Das Hauptplugin.
     * @param updateUrl    Die URL, unter der die neueste Version als Text verfügbar ist.
     * @param downloadLink Der Downloadlink zur neuen Version.
     */
    public UpdaterJoinListener(JavaPlugin plugin, String updateUrl, String downloadLink) {
        this.plugin = plugin;
        this.updateUrl = updateUrl;
        this.downloadLink = downloadLink;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) return; // Nur OPs sollen informiert werden

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(updateUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String latestVersion = reader.readLine().trim();
                reader.close();

                String currentVersion = plugin.getDescription().getVersion();
                if (!currentVersion.equals(latestVersion)) {
                    // Neue Version verfügbar, Nachricht im Hauptthread senden
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        player.sendMessage("§e[LowdFX Updater] Es gibt ein Update: Version " + latestVersion +
                                " ist verfügbar! Download: " + downloadLink);
                    });
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Update-Check für " + player.getName() + " fehlgeschlagen: " + e.getMessage());
            }
        });
    }
}
