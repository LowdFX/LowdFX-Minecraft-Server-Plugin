package at.lowdfx.lowdfx;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerProperties {
    public enum ServerPropertyType {
        WORLD_NAME("level-name");

        public final String key;

        ServerPropertyType(String key) {
            this.key = key;
        }
    }

    public static String get(@NotNull ServerPropertyType type) {
        try (InputStream input = new FileInputStream(new File(Bukkit.getWorldContainer(), "server.properties"))) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty(type.key);
        } catch (IOException e) {
            Lowdfx.LOG.error("Konnte nicht server.properties lesen.", e);
            throw new RuntimeException(e);
        }
    }
}
