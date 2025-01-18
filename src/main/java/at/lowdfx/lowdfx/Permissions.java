package at.lowdfx.lowdfx;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.Map;

public class Permissions {

    private final Lowdfx plugin;

    public Permissions(Lowdfx plugin) {
        this.plugin = plugin;
    }

    /**
     * Lädt die Berechtigungen aus der permissions.yml und registriert sie.
     */
    public void loadPermissions() {
        File file = new File(plugin.getDataFolder(), "permissions.yml");

        // Erstelle die Datei, falls sie nicht existiert
        if (!file.exists()) {
            plugin.saveResource("permissions.yml", false);
        }

        // Lade die Datei
        Yaml yaml = new Yaml();
        try {
            Map<String, Object> data = yaml.load(plugin.getResource("permissions.yml"));

            if (data != null && data.containsKey("permissions")) {
                @SuppressWarnings("unchecked")
                Map<String, Map<String, String>> permissions = (Map<String, Map<String, String>>) data.get("permissions");

                PluginManager pluginManager = Bukkit.getPluginManager();

                // Registriere jede Permission
                permissions.forEach((name, attributes) -> {
                    String description = attributes.getOrDefault("description", "Keine Beschreibung verfügbar.");
                    String defaultValue = attributes.getOrDefault("default", "OP");

                    try {
                        Permission permission = new Permission(
                                name,
                                description,
                                PermissionDefault.valueOf(defaultValue.toUpperCase())
                        );
                        pluginManager.addPermission(permission);
                    } catch (IllegalArgumentException e) {
                        Lowdfx.LOG.error("Ungültiger Standardwert für Permission: {}", name);
                    }
                });

                Lowdfx.LOG.info("Permissions erfolgreich geladen.");
            } else {
                Lowdfx.LOG.warn("Keine Permissions gefunden oder Datei ist leer.");
            }
        } catch (Exception e) {
            Lowdfx.LOG.error("Fehler beim Verarbeiten der permissions.yml!", e);
        }
    }

}
