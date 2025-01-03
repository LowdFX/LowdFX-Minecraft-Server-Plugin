package at.lowdfx.lowdfx.commands.tabCompleters.teleport;

import at.lowdfx.lowdfx.commands.teleport.SpawnCommand;
import at.lowdfx.lowdfx.commands.teleport.managers.SpawnManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class SpawnTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        // Wenn der Befehl nur ein Argument (die Aktion) hat
        if (args.length == 1) {
            ArrayList<String> suggestions = new ArrayList<>();
            // Vorschläge für normale Spieler
            if (sender.hasPermission(SpawnCommand.playerPermission)) {
                suggestions.add("help");
                suggestions.add("tp");
            }
            // Vorschläge für Admins (inklusive set und remove)
            if (sender.hasPermission(SpawnCommand.adminPermission)) {
                suggestions.add("set");
                suggestions.add("remove");
            }
            return suggestions;
        }

        // Wenn das erste Argument 'tp' oder 'set' oder 'remove' ist und das zweite Argument den Namen des Spawn erfordert
        if (args.length == 2) {
            ArrayList<String> spawnNames = getSpawnNames();
            if (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("remove")) {
                return spawnNames;
            }
        }

        // Standardmäßig keine Vorschläge zurückgeben
        return new ArrayList<>();
    }

    // Diese Methode gibt die Namen der bestehenden Spawns zurück
    private ArrayList<String> getSpawnNames() {
        ArrayList<String> list = new ArrayList<>();
        for (String key : SpawnManager.getNames()) {
            list.add(key);
        }
        return list;
    }
}
