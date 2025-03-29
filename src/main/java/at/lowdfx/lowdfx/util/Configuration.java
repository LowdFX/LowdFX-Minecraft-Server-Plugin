package at.lowdfx.lowdfx.util;

import at.lowdfx.lowdfx.LowdFX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Configuration {
    public static FileConfiguration CONFIG;

    public static Component CONNECTION_FIRST_JOIN;
    public static Component CONNECTION_JOIN;
    public static Component CONNECTION_QUIT;

    public static String BASIC_SERVER_NAME;
    public static int BASIC_MAX_HOMES;
    public static long BASIC_HOLOGRAM_REFRESH_INTERVAL;

    public static long WARNING_TEMPBAN_DURATION;
    public static long WARNING_EXPIRATION;

    public static boolean SAFE_TELEPORT_ENABLED;
    public static int TELEPORT_DELAY;
    public static int BACK_COOLDOWN;

    public static void init(@NotNull JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        CONFIG = plugin.getConfig();
        loadValues();
    }

    public static void reload() {
        LowdFX.PLUGIN.reloadConfig();
        CONFIG = LowdFX.PLUGIN.getConfig();
        loadValues();
    }


    private static void loadValues() {
        CONNECTION_FIRST_JOIN = MiniMessage.miniMessage().deserialize(CONFIG.getString("connection.first-join", ""));
        CONNECTION_JOIN = MiniMessage.miniMessage().deserialize(CONFIG.getString("connection.join", ""));
        CONNECTION_QUIT = MiniMessage.miniMessage().deserialize(CONFIG.getString("connection.quit", ""));

        BASIC_SERVER_NAME = CONFIG.getString("basic.server-name", "Server");
        BASIC_MAX_HOMES = CONFIG.getInt("basic.max-homes", 5);
        BASIC_HOLOGRAM_REFRESH_INTERVAL = CONFIG.getLong("basic.hologram-refresh-interval", 20);

        WARNING_TEMPBAN_DURATION = CONFIG.getLong("warning.tempban-duration", 1440) * 60000;
        WARNING_EXPIRATION = CONFIG.getLong("warning.expiration", 7200) * 60000;

        SAFE_TELEPORT_ENABLED = CONFIG.getBoolean("teleport.safe-enabled", true);
        TELEPORT_DELAY = CONFIG.getInt("teleport.delay", 5);
        BACK_COOLDOWN = CONFIG.getInt("teleport.backCooldown", 43200);
    }

    public static FileConfiguration get() {
        return CONFIG;
    }
}
