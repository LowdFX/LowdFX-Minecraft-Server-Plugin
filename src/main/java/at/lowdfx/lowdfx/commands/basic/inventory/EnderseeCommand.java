package at.lowdfx.lowdfx.commands.basic.inventory;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class EnderseeCommand implements CommandExecutor, Listener {
    public static final String ADMIN_PERMISSION = "lowdfx.endersee";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTask(Lowdfx.PLUGIN, () -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                return;
            }
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                if (args.length == 0) {
                    endersee(sender, player);
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
                        enderseeTarget(sender, target, args);
                        return;
                    }
                }
            }
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutze /endersee help um eine Hilfe zu erhalten!", NamedTextColor.RED)));
        });
        return true;
    }


    private void endersee(@NotNull CommandSender sender, @NotNull Player player) {
        player.openInventory(((Player) sender).getEnderChest());
        sender.sendMessage(Component.text("Deine Enderchest wurde geöffnet!", NamedTextColor.GREEN));
    }

    private void enderseeTarget(CommandSender sender, Player player, String @NotNull [] args) {
        // Überprüfen, ob ein Zielspieler angegeben wurde
        if (args.length == 0) {
            sender.sendMessage(Component.text("Du musst den Namen eines Spielers angeben!", NamedTextColor.RED));
            return;
        }

        // Hole den Zielspieler aus den Argumenten
        Player target = Bukkit.getPlayer(args[0]);

        // Überprüfen, ob der Zielspieler online ist
        if (target == null) {
            sender.sendMessage(Component.text("Der angegebene Spieler ist nicht online!", NamedTextColor.RED));
            return;
        }

        // Überprüfen, ob der Sender versucht, dass eigene Inventar zu sehen
        if (sender == target) {
            Player open_inv = Bukkit.getPlayerExact(args[0]);
            player.openInventory(Objects.requireNonNull(open_inv).getEnderChest());
            return;
        }

        // Der Sender wird das Inventar sehen, nicht der Spieler (Player), der den Befehl ausführt
        Player p = (Player) sender;  // Der Sender wird hier als der Öffner des Inventars gesetzt
        Player open_inv = Bukkit.getPlayerExact(args[0]);

        // Öffne das Inventar für den Sender
        p.openInventory(Objects.requireNonNull(open_inv).getEnderChest());

        // Sende eine Nachricht an den Sender, dass das Inventar geöffnet wurde
        sender.sendMessage(Component.text("Du hast die Enderchest von " + target.getName() + " geöffnet!", NamedTextColor.GREEN));
    }

    private void sendHelp(@NotNull CommandSender sender) {
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                <gold><b>------- Help: Endersee -------</b>
                <yellow>/endersee <white>→ <gray> Mit diesem Befehl kannst du deine Enderchest anzeigen.
                <yellow>/endersee <player> <white>→ <gray> Mit diesem Befehl kannst du die Enderchest eines angegeben Spielers anzeigen.
                """));
        }
    }
}
