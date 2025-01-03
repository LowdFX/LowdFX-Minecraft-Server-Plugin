package at.lowdfx.lowdfx.commands.tabCompleters;

import at.lowdfx.lowdfx.commands.WarnCommand;
import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.util.*;

public class WarnTabCompleter implements TabCompleter {

    private static final String[] adminCOMMANDS = {"help", "info", "remove", "removeall"};
    private static final String[] playerCOMMANDS = {"help", "info"};
    // Eine statische Liste der Befehle

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Neues Array für die Vervollständigungen
        final List<String> adminCompletions = new ArrayList<>();
        final List<String> playerCompletions = new ArrayList<>();
        // Konvertieren des Arrays COMMANDS in eine Liste, um copyPartialMatches zu verwenden
        StringUtil.copyPartialMatches(args[0], Arrays.asList(adminCOMMANDS), adminCompletions);
        StringUtil.copyPartialMatches(args[0], Arrays.asList(playerCOMMANDS), playerCompletions);

        // Die gefilterten Vervollständigungen zurückgeben

        if(sender.hasPermission(WarnCommand.adminPermission)) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    return new ArrayList<>();
                }
                return adminCompletions;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("removeall")) {
                return getCombinedPlayers();
            }
            if (args.length == 3) {
                return new ArrayList<>();
            }
        }
        if(sender.hasPermission(WarnCommand.playerPermission)) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    return new ArrayList<>();
                }
                return playerCompletions;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();


    }

    public ArrayList<String> getOnlinePlayers() {
        ArrayList<String> list = new ArrayList<>();
        for(Player player : Bukkit.getOnlinePlayers()) {
            list.add(player.getName());
        }
        return list;
    }

    public ArrayList<String> getWarnedPlayers() {
        File dataFolder = lowdfx.getDataFolde(); // Holt das Plugin-Datenverzeichnis
        ArrayList<String> warnedList = new ArrayList<>();
        File warnFolder = new File(dataFolder, "WarnSystem");  // Hier den Pfad zum Ordner der Warn-Daten anpassen

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
                        Bukkit.getLogger().warning("Fehler beim Laden der Warn-Datei für " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
        }
        return warnedList;
    }

    public ArrayList<String> getCombinedPlayers() {
        // Hole die Online-Spieler-Liste
        ArrayList<String> onlinePlayers = getOnlinePlayers();
        // Hole die Liste der verwarnten Spieler
        ArrayList<String> warnedPlayers = getWarnedPlayers();

        // Nutze ein Set, um Duplikate zu vermeiden
        Set<String> combinedSet = new HashSet<>();

        // Füge sowohl online Spieler als auch verwarnten Spieler zum Set hinzu
        combinedSet.addAll(onlinePlayers);
        combinedSet.addAll(warnedPlayers);

        // Wandeln das Set zurück in eine ArrayList
        return new ArrayList<>(combinedSet);
    }


}
