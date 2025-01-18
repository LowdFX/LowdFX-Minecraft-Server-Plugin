package at.lowdfx.lowdfx.commands.tab_completion.completion;

import at.lowdfx.lowdfx.Utilities;
import at.lowdfx.lowdfx.commands.basic.GamemodeCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GamemodeTabCompleter implements TabCompleter {
    private static final List<String> COMMANDS = List.of("help", "0", "1", "2", "3", "survival", "creative", "adventure", "spectator");
    // Eine statische Liste der Befehle

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        // Neues Array für die Vervollständigungen
        final List<String> completions = new ArrayList<>();
        // Konvertieren des Arrays COMMANDS in eine Liste, um copyPartialMatches zu verwenden
        StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
        // Die gefilterten Vervollständigungen zurückgeben

        if (sender.hasPermission(GamemodeCommand.ADMIN_PERMISSION)) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    return List.of();
                }
                return completions;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("0") || args[0].equalsIgnoreCase("1") || args[0].equalsIgnoreCase("2")
            || args[0].equalsIgnoreCase("3") || args[0].equalsIgnoreCase("survival") || args[0].equalsIgnoreCase("creative") ||
                    args[0].equalsIgnoreCase("adventure") || args[0].equalsIgnoreCase("spectator")) {
                return Utilities.getOnlinePlayers();
            }
        }
        return List.of();
    }
}
