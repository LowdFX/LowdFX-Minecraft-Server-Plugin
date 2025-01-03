package at.lowdfx.lowdfx.commands.tabCompleters.LowCommandHandling;

import at.lowdfx.lowdfx.commands.lowSubcommands.Info;
import at.lowdfx.lowdfx.commands.lowSubcommands.LowHelp;
import at.lowdfx.lowdfx.commands.lowSubcommands.kits.OPKit;
import at.lowdfx.lowdfx.commands.lowSubcommands.kits.StarterKit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LowTabCompleter implements TabCompleter {

    private static final String[] adminCommands = {"info", "opkit"};
    private static final String[] playerCommands = {"help", "starterkit"};
    // Eine statische Liste der Befehle

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> filteredCompletions = new ArrayList<>();

        // Wenn keine Argumente eingegeben wurden, Vervollständigung für den ersten Befehl anbieten
        if (args.length == 1) {
            if (sender.hasPermission(LowHelp.adminPermission) || sender.hasPermission(Info.adminPermission) || sender.hasPermission(OPKit.adminPermission)) {
                completions.addAll(Arrays.asList(adminCommands));
            }
            if (sender.hasPermission(LowHelp.playerPermission) || sender.hasPermission(StarterKit.playerPermission)) {
                completions.addAll(Arrays.asList(playerCommands));
            }
        }

        // Wenn das erste Argument eingegeben wurde, Filter anwenden
        if (args.length > 0) {
            StringUtil.copyPartialMatches(args[0], completions, filteredCompletions);
        }

        // Vervollständigungen zurückgeben
        return filteredCompletions;
    }
}
