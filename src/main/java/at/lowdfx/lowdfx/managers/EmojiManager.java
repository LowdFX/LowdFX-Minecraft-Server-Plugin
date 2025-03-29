package at.lowdfx.lowdfx.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class EmojiManager {

    // Speichert die Emojis als Components, sodass Farben und Formatierungen erhalten bleiben.
    private static final Map<String, Component> EMOJIS = new LinkedHashMap<>();

    /**
     * Lädt die emojis.yml aus dem Ressourcen-Ordner in das Plugin-Data-Verzeichnis und
     * wandelt Farbcodes (z. B. &c) mithilfe des LegacyComponentSerializer in ein Component um.
     *
     * @param plugin Das Plugin
     */
    public static void init(Plugin plugin) {
        // Sicherstellen, dass das Plugin-Datenverzeichnis existiert
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Could not create plugin data folder!");
            }
        }
        File targetFile = new File(plugin.getDataFolder(), "emojis.yml");
        if (!targetFile.exists()) {
            // Kopiert die Datei aus den Ressourcen in das Datenverzeichnis
            plugin.saveResource("emojis.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(targetFile);
        EMOJIS.clear();
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        for (String key : config.getKeys(false)) {
            String rawSymbol = config.getString(key);
            if (rawSymbol != null) {
                // Deserialisiert den Legacy-String (mit &-Codes) in ein Component
                Component component = serializer.deserialize(rawSymbol);
                EMOJIS.put(key, component);
            }
        }
    }

    /**
     * Liefert alle in der Konfiguration vorhandenen Emoji-Codes.
     *
     * @return Eine Set-Instanz mit allen Emoji-Codes.
     */
    public static Set<String> getEmojiCodes() {
        return EMOJIS.keySet();
    }

    /**
     * Gibt das Emoji-Symbol als Component zu einem gegebenen Code zurück.
     * Das zurückgegebene Component enthält die farbigen Formatierungen, die in der YAML definiert sind.
     *
     * @param code Der Emoji-Code (z. B. "oof").
     * @return Das farbige Emoji-Symbol als Component oder null, falls der Code nicht existiert.
     */
    public static Component getEmojiSymbol(String code) {
        return EMOJIS.get(code);
    }
}
