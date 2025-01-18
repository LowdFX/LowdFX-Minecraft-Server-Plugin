package at.lowdfx.lowdfx.commands.subcommands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;


public class LowHelp implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.low.help.admin";
    public static final String PLAYER_PERMISSION = "lowdfx.low.help";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission(PLAYER_PERMISSION) || sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                    <gold><b>------- Help: Low Subcommands -------</b>
                    <yellow>/low starterkit <white>→ <gray> Gibt ein Starterkit.
                    """));
        }
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                    <yellow>/low info <white>→ <gray> Plugin Infos.
                    <yellow>/low opkit <white>→ <gray> Gibt ein OP-Kit.
                    """));
        }
        return true;
    }

}


