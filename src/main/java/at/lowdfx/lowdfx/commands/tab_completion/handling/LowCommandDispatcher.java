package at.lowdfx.lowdfx.commands.tab_completion.handling;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.commands.subcommands.Info;
import at.lowdfx.lowdfx.commands.subcommands.LowHelp;
import at.lowdfx.lowdfx.commands.subcommands.kits.OPKit;
import at.lowdfx.lowdfx.commands.subcommands.kits.StarterKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

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

    // Konstruktor für die Registrierung der Subcommands.
    public LowCommandDispatcher() {
        subCommands.put("help", new LowHelp());
        subCommands.put("info", new Info());
        subCommands.put("starterkit", new StarterKit());
        subCommands.put("opkit", new OPKit());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            if (adminPermission(sender)) {
                new LowHelp();
                return false;
            } if (playerPermission(sender)) {
                new LowHelp();
                return false;
            } else {
                sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutze /low help um eine Hilfe zu erhalten!", NamedTextColor.RED)));
            }
            return true;
        }

        CommandExecutor executor = subCommands.get(args[0].toLowerCase());
        if (executor != null) {
            // Leitet den Befehl an die entsprechende Klasse weiter.
            return executor.onCommand(sender, cmd, label, args);
        } else {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Der eingegebene Subcommand " + args[0] + " existiert nicht!", NamedTextColor.RED)));
            return false;
        }
    }
}