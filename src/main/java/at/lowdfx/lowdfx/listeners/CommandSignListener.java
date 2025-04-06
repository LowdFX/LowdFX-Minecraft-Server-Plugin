package at.lowdfx.lowdfx.listeners;

import at.lowdfx.lowdfx.util.Perms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CommandSignListener implements Listener {

    private final FileConfiguration config;

    public CommandSignListener(JavaPlugin plugin) {
        // Speichere die commandsigns.yml im Hauptordner des Plugins (also im übergeordneten Ordner von getDataFolder())
        File file = new File(plugin.getDataFolder(), "commandsigns.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create commandsigns.yml: " + e.getMessage());
            }
            config = new YamlConfiguration();
            // Setze alle gewünschten Standardtypen
            config.set("warp", true);
            config.set("spawn", true);
            config.set("trash", true);
            config.set("playtime", true);
            config.set("anvil", true);
            config.set("workbench", true);
            config.set("kit", true);
            config.set("feed", true);
            config.set("heal", true);
            config.set("day", true);
            config.set("night", true);
            config.set("rtp", true);
            config.set("emojis", true);
            config.set("help", true);
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().warning("Could not save commandsigns.yml: " + e.getMessage());
            }
        } else {
            config = YamlConfiguration.loadConfiguration(file);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        BlockState state = block.getState();
        if (!(state instanceof Sign)) return;
        Sign sign = (Sign) state;

        // Entferne Farbcodes aus der ersten Zeile
        Component line = sign.getSide(Side.FRONT).line(0);
        String rawLine = PlainTextComponentSerializer.plainText().serialize(line).trim();
        if (!rawLine.startsWith("[") || !rawLine.endsWith("]")) return;
        String type = rawLine.substring(1, rawLine.length() - 1).toLowerCase();

        if (!config.getBoolean(type, false)) return;

        String command = "";
        switch (type) {
            case "warp":
                String warpName = PlainTextComponentSerializer.plainText()
                        .serialize(sign.getSide(Side.FRONT).line(1))
                        .trim();
                if (warpName.isEmpty()) return;
                command = "warp " + warpName;
                break;
            case "spawn":
                String spawnName = PlainTextComponentSerializer.plainText()
                        .serialize(sign.getSide(Side.FRONT).line(1))
                        .trim();
                command = spawnName.isEmpty() ? "spawn" : "spawn tp " + spawnName;
                break;
            case "trash":
                command = "trash";
                break;
            case "playtime":
                String target = PlainTextComponentSerializer.plainText()
                        .serialize(sign.getSide(Side.FRONT).line(1))
                        .trim();
                command = target.isEmpty() ? "playtime" : "playtime " + target;
                break;
            case "anvil":
                command = "anvil";
                break;
            case "workbench":
                command = "workbench";
                break;
            case "kit":
                String kitName = PlainTextComponentSerializer.plainText()
                        .serialize(sign.getSide(Side.FRONT).line(1))
                        .trim();
                if (kitName.isEmpty()) return;
                command = "kit " + kitName;
                break;
            case "feed":
                command = "feed";
                break;
            case "heal":
                command = "heal";
                break;
            case "day":
                command = "day";
                break;
            case "night":
                command = "night";
                break;
            case "rtp":
                command = "rtp";
                break;
            case "emojis":
                command = "emojis";
                break;
            case "help":
                command = "help";
                break;
            default:
                return;
        }

        Player player = event.getPlayer();
        player.performCommand(command);
        event.setCancelled(true);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Component line = event.line(0);
        String line0 = PlainTextComponentSerializer.plainText().serialize(line).trim();
        if (line0 == null) return;
        line0 = line0.trim();
        if (!line0.startsWith("[") || !line0.endsWith("]")) return;
        String type = line0.substring(1, line0.length() - 1).toLowerCase();

        Player player = event.getPlayer();
        // Hier wird die globale Permission über das Perm-Enum verwendet
        if (!Perms.check(player, at.lowdfx.lowdfx.util.Perms.Perm.COMMANDSIGN)) {
            event.setCancelled(true);
            return;
        }

        event.line(0, Component.text("[" + type + "]").color(NamedTextColor.BLUE));
    }
}
