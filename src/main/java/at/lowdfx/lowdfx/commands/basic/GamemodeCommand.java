package at.lowdfx.lowdfx.commands.basic;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GamemodeCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.gm";


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTask(Lowdfx.PLUGIN, () -> {
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                if (args.length == 1) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                    } else {
                        resolve(args[0], sender, player);
                    }
                    return;
                }

                if (args.length == 2) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(Lowdfx.serverMessage(Component.text("Spieler nicht gefunden!", NamedTextColor.RED)));
                    } else {
                        resolve(args[0], sender, target);
                    }
                    return;
                }
            }
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutze /gm help um eine Hilfe zu erhalten!", NamedTextColor.RED)));
        });
        return true;
    }

    private void resolve(@NotNull String arg, CommandSender sender, Player target) {
        switch (arg.toLowerCase()) {
            case "help" -> sendHelp(sender);
            case "0", "survival" -> setMode(GameMode.SURVIVAL, target);
            case "1", "creative" -> setMode(GameMode.CREATIVE, target);
            case "2", "adventure" -> setMode(GameMode.ADVENTURE, target);
            case "3", "spectator" -> setMode(GameMode.SPECTATOR, target);
            default -> sender.sendMessage(Lowdfx.serverMessage(Component.text("Der eingegebene Gamemode " + arg + " existiert nicht!", NamedTextColor.RED)));
        }
    }

    private void setMode(GameMode mode, @NotNull Player player) {
        player.setGameMode(mode);
        player.sendMessage(Lowdfx.serverMessage(Component.text("Du bist nun im " + mode.name().toLowerCase() + " Modus!", NamedTextColor.GREEN)));
    }

    private void sendHelp(@NotNull CommandSender sender) {
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                    <gold><b>------- Help: Gamemodes -------</b>
                    <yellow>/gm 0 | survival <white>→ <gray> Setzt deinen Gamemode zu survival.
                    <yellow>/gm 1 | creative <white>→ <gray> Setzt deinen Gamemode zu creative.
                    <yellow>/gm 2 | adventure <white>→ <gray> Setzt deinen Gamemode zu adventure.
                    <yellow>/gm 3 | spectator <white>→ <gray> Setzt deinen Gamemode zu spectator.
                    """));
        }
    }
}
