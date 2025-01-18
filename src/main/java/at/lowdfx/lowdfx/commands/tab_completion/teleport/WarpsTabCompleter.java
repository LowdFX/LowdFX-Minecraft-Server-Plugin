package at.lowdfx.lowdfx.commands.tab_completion.teleport;

import at.lowdfx.lowdfx.commands.teleport.WarpCommand;
import at.lowdfx.lowdfx.commands.teleport.managers.WarpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WarpsTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) return List.of();

        if (args.length == 1) {
            if (sender.hasPermission(WarpCommand.ADMIN_PERMISSION)) {
                List<String> list = getWarps();
                list.add("set");
                list.add("remove");
                list.add("help");
                return list;
            }
            return getWarps();
        }

        if (args.length == 2) {
            if (sender.hasPermission(WarpCommand.ADMIN_PERMISSION)) {
                if (args[0].equalsIgnoreCase("set"))
                    return getWarps();
                if (args[0].equalsIgnoreCase("remove"))
                    return getWarps();
            }

        }
        return List.of();
    }

    private @NotNull List<String> getWarps() {
        return new ArrayList<>(WarpManager.getWarpsList());
    }
}
