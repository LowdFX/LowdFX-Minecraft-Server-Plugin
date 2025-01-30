package at.lowdfx.lowdfx.util;

import at.lowdfx.lowdfx.LowdFX;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.Map;

public class Permissions {
    /**
     * Lädt die Berechtigungen aus der permissions.yml und registriert sie.
     */
    public void loadPermissions() {
        File file = LowdFX.DATA_DIR.resolve("permissions.yml").toFile();

        // Erstelle die Datei, falls sie nicht existiert
        if (!file.exists()) {
            LowdFX.PLUGIN.saveResource("permissions.yml", false);
        }

        // Lade die Datei
        Yaml yaml = new Yaml();
        try {
            Map<String, Object> data = yaml.load(LowdFX.PLUGIN.getResource("permissions.yml"));

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
                        LowdFX.LOG.error("Ungültiger Standardwert für Permission: {}", name);
                    }
                });

                LowdFX.LOG.info("Permissions erfolgreich geladen.");
            } else {
                LowdFX.LOG.warn("Keine Permissions gefunden oder Datei ist leer.");
            }
        } catch (Exception e) {
            LowdFX.LOG.error("Fehler beim Verarbeiten der permissions.yml!", e);
        }
    }

}
