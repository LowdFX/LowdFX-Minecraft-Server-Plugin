package at.lowdfx.lowdfx.commands.tab_completion.completion.inventoryTabCompleters;

import at.lowdfx.lowdfx.Utilities;
import at.lowdfx.lowdfx.commands.basic.inventory.InvseeCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InvseeTabCompleter implements TabCompleter {
    private static final List<String> COMMANDS = List.of("help");
    // Eine statische Liste der Befehle

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        // Neues Array für die Vervollständigungen
        final List<String> completions = new ArrayList<>();
        // Konvertieren des Arrays COMMANDS in eine Liste, um copyPartialMatches zu verwenden
        StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
        // Die gefilterten Vervollständigungen zurückgeben

        if (sender.hasPermission(InvseeCommand.ADMIN_PERMISSION)) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    return List.of();
                }
                return Utilities.allTabCompletions("help");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("invsee")) {
                return Utilities.getOnlinePlayers();
            }
        }
        return List.of();
    }
}
