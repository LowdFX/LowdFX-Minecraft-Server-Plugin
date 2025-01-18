package at.lowdfx.lowdfx.commands.teleport.teleportPoints;

import at.lowdfx.lowdfx.Lowdfx;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class HomePoint {
    private final File file;
    private final YamlConfiguration config = new YamlConfiguration();

    public HomePoint(@NotNull Player player) {
        File folder = Lowdfx.DATA_DIR.resolve("HomePoints").toFile();
        folder.mkdirs();

        this.file = new File(folder.getAbsolutePath(), player.getName() + ".yml");
        try {
            if (this.file.exists()) {
                this.config.load(this.file);
            }
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public HomePoint(@NotNull OfflinePlayer player) {
        File folder = Lowdfx.DATA_DIR.resolve("HomePoints").toFile();
        folder.mkdirs();

        this.file = new File(folder.getAbsolutePath(), player.getName() + ".yml");
        try {
            if (this.file.exists()) {
                this.config.load(this.file);
            }
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            this.config.save(this.file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(String name, Location location) {
        this.config.set(name, location);
    }

    public void remove(String name) {
        this.config.set(name, null);
    }

    public Set<String> getHomes() {
        return this.config.getKeys(false);
    }

    public boolean doesNotExist(String name) {
        return this.config.getLocation(name) == null;
    }

    public TeleportPoint get(String name) {
        return new TeleportPoint(this.config.getLocation(name));
    }
}
