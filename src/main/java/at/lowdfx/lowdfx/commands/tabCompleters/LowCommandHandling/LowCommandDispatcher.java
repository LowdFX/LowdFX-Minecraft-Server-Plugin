package at.lowdfx.lowdfx.commands.tabCompleters.LowCommandHandling;

import at.lowdfx.lowdfx.commands.lowSubcommands.Info;
import at.lowdfx.lowdfx.commands.lowSubcommands.LowHelp;
import at.lowdfx.lowdfx.commands.lowSubcommands.kits.OPKit;
import at.lowdfx.lowdfx.commands.lowSubcommands.kits.StarterKit;
import at.lowdfx.lowdfx.lowdfx;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LowCommandDispatcher implements CommandExecutor {

    public static final List<String> adminPermissions = Arrays.asList(
            "OPKit.adminPermission",
            "Info.adminPermission",
            "LowHelp.adminPermission"
    );

    public boolean adminPermission(CommandSender sender) {
        for (String permission : adminPermissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public static final List<String> playerPermissions = Arrays.asList(
            "Starterkit.playerPermission",
            "LowHelp.playerPermission"
    );

    public boolean playerPermission(CommandSender sender) {
        for (String permission : playerPermissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    //public static final String playerPermission = StarterKit.playerPermission;

    private final Map<String, CommandExecutor> subCommands = new HashMap<>();

    // Konstruktor für die Registrierung der Subcommands
    public LowCommandDispatcher() {
        subCommands.put("help", new LowHelp());
        subCommands.put("info", new Info());
        subCommands.put("starterkit", new StarterKit());
        subCommands.put("opkit", new OPKit());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (adminPermission(sender)){
                new LowHelp();
                return false;
            } if (playerPermission(sender)){
                new LowHelp();
                return false;
            } else {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Fehler: Benutze /low help um eine Hilfe zu erhalten!");
            }
            return true;
        }

        CommandExecutor executor = subCommands.get(args[0].toLowerCase());
        if (executor != null) {
            // Leitet den Befehl an die entsprechende Klasse weiter
            return executor.onCommand(sender, cmd, label, args);
        } else {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + lowdfx.config.getString("basic.servername") + ChatColor.GRAY + " >> " + ChatColor.RED + "Der eingegebene Subcommand " + ChatColor.BOLD + args[0] + ChatColor.RED + " existiert nicht!");
            return false;
        }
    }



}