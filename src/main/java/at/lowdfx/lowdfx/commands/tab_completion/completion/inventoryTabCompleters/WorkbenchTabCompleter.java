package at.lowdfx.lowdfx.commands.tab_completion.completion.inventoryTabCompleters;

import at.lowdfx.lowdfx.commands.basic.inventory.WorkbenchCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WorkbenchTabCompleter implements TabCompleter {
    private static final List<String> COMMANDS = List.of("help");
    // Eine statische Liste der Befehle

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        // Neues Array für die Vervollständigungen
        final List<String> completions = new ArrayList<>();
        // Konvertieren des Arrays COMMANDS in eine Liste, um copyPartialMatches zu verwenden
        StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
        // Die gefilterten Vervollständigungen zurückgeben

        if (sender.hasPermission(WorkbenchCommand.ADMIN_PERMISSION)) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    return List.of();
                }
                return completions;
            }
        }
        return List.of();
    }
}
