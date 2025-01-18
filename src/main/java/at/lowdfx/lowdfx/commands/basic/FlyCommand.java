package at.lowdfx.lowdfx.commands.basic;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FlyCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.fly";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTask(Lowdfx.PLUGIN, () -> {
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                if (args.length == 0) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                        return;
                    }
                    fly(player);
                    return;
                }

                if (args.length == 1) {
                    Player target = Bukkit.getPlayer(args[0]);

                    if (args[0].equalsIgnoreCase("help")) {
                        sendHelp(sender);
                        return;
                    }
                    if (target == null) {
                        sender.sendMessage(Lowdfx.serverMessage(Component.text("Spieler nicht gefunden!", NamedTextColor.RED)));
                        return;
                    }

                    if (args[0].equalsIgnoreCase(target.getName())) {
                        flyTarget(sender, args);
                        return;
                    }

                }

            }
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutze /fly help um eine Hilfe zu erhalten!", NamedTextColor.RED)));
        });
        return true;
    }

    private void fly(@NotNull Player player) {
        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(Lowdfx.serverMessage(Component.text("Du kannst nun fliegen!", NamedTextColor.GREEN)));
        } else {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage(Lowdfx.serverMessage(Component.text("Du kannst nun nicht mehr fliegen!", NamedTextColor.GREEN)));
        }
    }


    private void flyTarget(CommandSender sender, String @NotNull [] args) {
        Player target = Bukkit.getPlayer(args[0]);
        if (!Objects.requireNonNull(target).getAllowFlight()) {
            target.setAllowFlight(true);
            target.setFlying(true);
            target.sendMessage(Lowdfx.serverMessage(Component.text("Du kannst nun fliegen!", NamedTextColor.GREEN)));
            sender.sendMessage(Lowdfx.serverMessage(Component.text(args[0] + "kann nun fliegen!", NamedTextColor.GREEN)));
        } else {
            target.setAllowFlight(false);
            target.setFlying(false);
            target.sendMessage(Lowdfx.serverMessage(Component.text("Du kannst nun nicht mehr fliegen!", NamedTextColor.GREEN)));
            sender.sendMessage(Lowdfx.serverMessage(Component.text(args[0] + " kann nun nicht mehr fliegen!", NamedTextColor.GREEN)));
        }
    }


    private void sendHelp(@NotNull CommandSender sender) {
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                    <gold><b>------- Help: Fly -------</b>
                    <yellow>/fly <white>→ <gray> Mit diesem Befehl kannst du fliegen.
                    <yellow>/fly <player> <white>→ <gray> Mit diesem Befehl kannst du einem angegebenem Spieler in Fly Mode setzen.
                    """));
        }
    }
}
