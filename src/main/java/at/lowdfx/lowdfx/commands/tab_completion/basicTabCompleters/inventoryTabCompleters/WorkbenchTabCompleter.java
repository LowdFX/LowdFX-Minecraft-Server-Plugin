package at.lowdfx.lowdfx.commands.tab_completion.basicTabCompleters.inventoryTabCompleters;

import at.lowdfx.lowdfx.commands.basic.inventoryCommands.WorkbenchCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkbenchTabCompleter implements TabCompleter {
    private static final String[] COMMANDS = { "help" };
    // Eine statische Liste der Befehle

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, String alias, String @NotNull [] args) {
        // Neues Array für die Vervollständigungen
        final List<String> completions = new ArrayList<>();
        // Konvertieren des Arrays COMMANDS in eine Liste, um copyPartialMatches zu verwenden
        StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
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
