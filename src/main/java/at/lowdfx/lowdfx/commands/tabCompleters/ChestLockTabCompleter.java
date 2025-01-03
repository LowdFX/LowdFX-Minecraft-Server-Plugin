package at.lowdfx.lowdfx.commands.tabCompleters;

import at.lowdfx.lowdfx.commands.chestLock.ChestLockCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChestLockTabCompleter implements TabCompleter {

    private static final String[] COMMANDS = {"help", "unlock", "add", "remove"};
    // Eine statische Liste der Befehle

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Neues Array für die Vervollständigungen
        final List<String> completions = new ArrayList<>();
        // Konvertieren des Arrays COMMANDS in eine Liste, um copyPartialMatches zu verwenden
        StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
        // Die gefilterten Vervollständigungen zurückgeben

        if(sender.hasPermission(ChestLockCommand.playerPermission)) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    return new ArrayList<>();
                }
                return completions;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("unlock") || args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                return getOnlinePlayers();
            }
        }
        return new ArrayList<>();
    }

    public ArrayList<String> getOnlinePlayers() {
        ArrayList<String> list = new ArrayList<>();
        for(Player player : Bukkit.getOnlinePlayers()) {
            list.add(player.getName());
        }
        return list;
    }

}
