package at.lowdfx.lowdfx.commands.tab_completion;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.Utilities;
import at.lowdfx.lowdfx.commands.WarnCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WarnTabCompleter implements TabCompleter {
    private static final List<String> ADMIN_COMMANDS = List.of("help", "info", "remove", "removeall");
    private static final List<String> PLAYER_COMMANDS = List.of("help", "info");
    // Eine statische Liste der Befehle

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        // Neues Array für die Vervollständigungen
        final List<String> adminCompletions = new ArrayList<>();
        final List<String> playerCompletions = new ArrayList<>();
        // Konvertieren des Arrays COMMANDS in eine Liste, um copyPartialMatches zu verwenden
        StringUtil.copyPartialMatches(args[0], ADMIN_COMMANDS, adminCompletions);
        StringUtil.copyPartialMatches(args[0], PLAYER_COMMANDS, playerCompletions);

        // Die gefilterten Vervollständigungen zurückgeben

        if (sender.hasPermission(WarnCommand.ADMIN_PERMISSION)) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    return List.of();
                }
                return adminCompletions;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("removeall")) {
                return getCombinedPlayers();
            }
            if (args.length == 3) {
                return List.of();
            }
        }
        if (sender.hasPermission(WarnCommand.PLAYER_PERMISSION)) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    return List.of();
                }
                return playerCompletions;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
                return List.of();
            }
        }

        return List.of();
    }

    public ArrayList<String> getWarnedPlayers() {
        ArrayList<String> warnedList = new ArrayList<>();
        File warnFolder = Lowdfx.DATA_DIR.resolve("WarnSystem").toFile();  // Hier den Pfad zum Ordner der Warn-Daten anpassen

        // Durchlaufe alle Dateien im Warn-Ordner
        File[] files = warnFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".yml")) {  // Überprüfe, ob die Datei eine YML-Datei ist
                    try {
                        // Lade die Konfiguration der Datei (Spieler-Verwarnung)
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                        int warns = config.getInt("warns", 0);  // Anzahl der Verwarnungen für den Spieler

                        if (warns > 0) {
                            // Spieler mit Verwarnungen zur Liste hinzufügen
                            String playerName = file.getName().replace(".yml", "");  // Entferne die ".yml"-Erweiterung
                            warnedList.add(playerName);
                        }
                    } catch (Exception e) {
                        Lowdfx.LOG.warn("Fehler beim Laden der Warn-Datei für {}: {}", file.getName(), e.getMessage());
                    }
                }
            }
        }
        return warnedList;
    }

    public ArrayList<String> getCombinedPlayers() {
        List<String> all = new ArrayList<>();
        all.addAll(Utilities.getOnlinePlayers());
        all.addAll(getWarnedPlayers());
        return new ArrayList<>(all);
    }
}
