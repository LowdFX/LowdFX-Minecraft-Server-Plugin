package at.lowdfx.lowdfx.commands.tab_completion.basicTabCompleters.inventoryTabCompleters;

import at.lowdfx.lowdfx.commands.basic.inventoryCommands.EnderseeCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EnderseeTabCompleter implements TabCompleter {
    private static final String[] COMMANDS = { "help" };
    // Eine statische Liste der Befehle

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        // Neues Array für die Vervollständigungen
        final List<String> completions = new ArrayList<>();
        // Konvertieren des Arrays COMMANDS in eine Liste, um copyPartialMatches zu verwenden
        StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
        // Die gefilterten Vervollständigungen zurückgeben

        if (sender.hasPermission(EnderseeCommand.ADMIN_PERMISSION)) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    return List.of();
                }
                return allCompletions();
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("endersee")) {
                return getOnlinePlayers();
            }
        }
        return List.of();
    }

    public ArrayList<String> getOnlinePlayers() {
        ArrayList<String> list = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            list.add(player.getName());
        }
        return list;
    }

    public ArrayList<String> allCompletions() {
        // Hole die Online-Spieler-Liste
        ArrayList<String> onlinePlayers = getOnlinePlayers();

        // Beispielhafte Liste von commands (dies könnte in einer echten Implementierung dynamisch sein)
        ArrayList<String> commands = new ArrayList<>();
        commands.add("help");  // Beispiel eines commands

        // Nutze ein Set, um Duplikate zu vermeiden
        Set<String> combinedSet = new HashSet<>();

        // Füge sowohl online Spieler als auch commands zum Set hinzu
        combinedSet.addAll(onlinePlayers);
        combinedSet.addAll(commands); // Hier fügen wir die commands hinzu

        // Wandeln das Set zurück in eine ArrayList und gib es zurück
        return new ArrayList<>(combinedSet);
    }

}
