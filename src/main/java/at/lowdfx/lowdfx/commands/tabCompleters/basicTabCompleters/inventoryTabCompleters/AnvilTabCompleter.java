package at.lowdfx.lowdfx.commands.tabCompleters.basicTabCompleters.inventoryTabCompleters;

import at.lowdfx.lowdfx.commands.basicCommands.inventoryCommands.AnvilCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class AnvilTabCompleter implements TabCompleter {

    private static final String[] COMMANDS = {"help"};
    // Eine statische Liste der Befehle

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Neues Array für die Vervollständigungen
        final List<String> completions = new ArrayList<>();
        // Konvertieren des Arrays COMMANDS in eine Liste, um copyPartialMatches zu verwenden
        StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
        // Die gefilterten Vervollständigungen zurückgeben

        if(sender.hasPermission(AnvilCommand.adminPermission)) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    return new ArrayList<>();
                }
                return completions;
            }
        }
        return new ArrayList<>();
    }

}
