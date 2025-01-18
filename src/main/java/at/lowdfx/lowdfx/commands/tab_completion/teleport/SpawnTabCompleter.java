package at.lowdfx.lowdfx.commands.tab_completion.teleport;

import at.lowdfx.lowdfx.commands.teleport.SpawnCommand;
import at.lowdfx.lowdfx.commands.teleport.managers.SpawnManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SpawnTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        // Wenn der Befehl nur ein Argument (die Aktion) hat.
        if (args.length == 1) {
            ArrayList<String> suggestions = new ArrayList<>();
            // Vorschläge für normale Spieler.
            if (sender.hasPermission(SpawnCommand.PLAYER_PERMISSION)) {
                suggestions.add("help");
                suggestions.add("tp");
            }
            // Vorschläge für Admins (inklusive set und remove).
            if (sender.hasPermission(SpawnCommand.ADMIN_PERMISSION)) {
                suggestions.add("set");
                suggestions.add("remove");
            }
            return suggestions;
        }

        // Wenn das erste Argument 'tp' oder 'set' oder 'remove' ist und das zweite Argument den Namen des Spawn erfordert.
        if (args.length == 2) {
            List<String> spawnNames = getSpawnNames();
            if (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("remove")) {
                return spawnNames;
            }
        }

        // Standardmäßig keine Vorschläge zurückgeben.
        return List.of();
    }

    // Diese Methode gibt die Namen der bestehenden Spawns zurück.
    private @NotNull List<String> getSpawnNames() {
        return new ArrayList<>(SpawnManager.getNames());
    }
}
