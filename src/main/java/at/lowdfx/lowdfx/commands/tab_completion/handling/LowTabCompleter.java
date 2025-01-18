package at.lowdfx.lowdfx.commands.tab_completion.handling;

import at.lowdfx.lowdfx.commands.subcommands.Info;
import at.lowdfx.lowdfx.commands.subcommands.LowHelp;
import at.lowdfx.lowdfx.commands.subcommands.kits.OPKit;
import at.lowdfx.lowdfx.commands.subcommands.kits.StarterKit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LowTabCompleter implements TabCompleter {
    private static final List<String> ADMIN_COMMANDS = List.of("info", "opkit");
    private static final List<String> PLAYER_COMMANDS = List.of("help", "starterkit");
    // Eine statische Liste der Befehle.

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        List<String> filteredCompletions = new ArrayList<>();

        // Wenn keine Argumente eingegeben wurden, Vervollständigung für den ersten Befehl anbieten.
        if (args.length == 1) {
            if (sender.hasPermission(LowHelp.ADMIN_PERMISSION) || sender.hasPermission(Info.ADMIN_PERMISSION) || sender.hasPermission(OPKit.ADMIN_PERMISSION)) {
                completions.addAll(ADMIN_COMMANDS);
            }
            if (sender.hasPermission(LowHelp.PLAYER_PERMISSION) || sender.hasPermission(StarterKit.PLAYER_PERMISSION)) {
                completions.addAll(PLAYER_COMMANDS);
            }
        }

        // Wenn das erste Argument eingegeben wurde, Filter anwenden.
        if (args.length > 0) {
            StringUtil.copyPartialMatches(args[0], completions, filteredCompletions);
        }

        // Vervollständigungen zurückgeben.
        return filteredCompletions;
    }
}
