package at.lowdfx.lowdfx.commands.tab_completion.teleport;

import at.lowdfx.lowdfx.commands.teleport.managers.HomeManager;
import at.lowdfx.lowdfx.commands.teleport.teleportPoints.HomePoint;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) return List.of();

        if (args.length == 1) {
            if (sender.hasPermission("lowdfx.home.admin"))
                return getFirstListAdmin();
            else
                return getFirstList();
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) { // Sieht nur so klein aus, weil das mehrere cases in einem sind.
                case "set", "tp", "remove": return getHomes((Player) sender);
                case "set_other", "tp_other", "remove_other": return getOfflinePlayers();
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("remove")) {
                return getHomes(Bukkit.getOfflinePlayer(args[1]));
            }
        }

        return List.of();
    }

    public List<String> getOfflinePlayers() {
        ArrayList<String> list = new ArrayList<>();
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            list.add(player.getName());
        }
        return list;
    }

    public List<String> getHomes(Player player) {
        return new ArrayList<>(HomeManager.get(player).getHomes());
    }

    public List<String> getHomes(@NotNull OfflinePlayer target) {
        if (target.hasPlayedBefore()) {
            HomePoint homePoint = HomeManager.get(target);
            return new ArrayList<>(homePoint.getHomes());
        }
        return List.of();
    }

    public List<String> getFirstListAdmin() {
        List<String> list = getFirstList();
        list.add("set_other");
        list.add("remove_other");
        list.add("tp_other");
        return list;
    }

    public List<String> getFirstList() {
        List<String> list = new ArrayList<>();
        list.add("help");
        list.add("set");
        list.add("remove");
        list.add("tp");
        return list;
    }
}
