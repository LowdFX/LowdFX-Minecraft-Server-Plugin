package at.lowdfx.lowdfx.commands.subcommands;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Info implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.low.info";
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!cmd.getName().equalsIgnoreCase("low")) return false;

        // Prüfen, ob ein Subcommand angegeben wurde.
        if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                        <yellow><b>MC-Vers.:</b> <gold>1.21.+
                        <yellow><b>Plugin-Vers.:</b> <gold>1.0+
                        <yellow><b>Author.:</b> <gold>LowdFX
                        """));
            } else {
                sender.sendMessage(Lowdfx.serverMessage(Component.text(Objects.requireNonNull(Lowdfx.CONFIG.getString("basic.noPermission")), NamedTextColor.RED)));
            }
            return true;
        }

        // Standardnachricht für den /low-Befehl ohne Subcommand.
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Ungültiges Argument! Nutze /low help für Informationen.", NamedTextColor.RED)));
        return true;
    }
}