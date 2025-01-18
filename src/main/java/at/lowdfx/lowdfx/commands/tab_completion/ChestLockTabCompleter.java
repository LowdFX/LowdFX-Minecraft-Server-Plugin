package at.lowdfx.lowdfx.commands.tab_completion;

import at.lowdfx.lowdfx.commands.chest.lock.ChestLockCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChestLockTabCompleter implements TabCompleter {
    private static final String[] COMMANDS = { "help", "unlock", "add", "remove" };
    // Eine statische Liste der Befehle

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        // Neues Array für die Vervollständigungen
        final List<String> completions = new ArrayList<>();
        // Konvertieren des Arrays COMMANDS in eine Liste, um copyPartialMatches zu verwenden
        StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
        // Die gefilterten Vervollständigungen zurückgeben

        if (sender.hasPermission(ChestLockCommand.PLAYER_PERMISSION)) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    return List.of();
                }
                return completions;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("unlock") || args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
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

}
