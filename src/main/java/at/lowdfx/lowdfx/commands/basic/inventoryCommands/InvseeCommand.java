package at.lowdfx.lowdfx.commands.basic.inventoryCommands;

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

public class InvseeCommand implements CommandExecutor, Listener {
    public static final String ADMIN_PERMISSION = "lowdfx.invsee";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTask(Lowdfx.PLUGIN, () -> {

            if (sender.hasPermission(ADMIN_PERMISSION)) {
                if (args.length == 0) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                        return;
                    }
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
                        invsee(sender, target, args);
                        return;
                    }
                }
            }
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutze /invsee help um eine Hilfe zu erhalten!", NamedTextColor.RED)));
        });
        return true;
    }

    private void invsee(CommandSender sender, Player player, String @NotNull [] args) {
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

        // Überprüfen, ob der Sender versucht, das eigene Inventar zu sehen
        if (sender == target) {
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Du kannst nicht dein eigenes Inventar auslesen!", NamedTextColor.RED)));
            return;
        }

        // Der Sender wird das Inventar sehen, nicht der Spieler (Player), der den Befehl ausführt
        Player p = (Player) sender;  // Der Sender wird hier als der Öffner des Inventars gesetzt
        Player open_inv = Bukkit.getPlayerExact(args[0]);

        // Öffne das Inventar für den Sender
        p.openInventory(Objects.requireNonNull(open_inv).getInventory());

        // Sende eine Nachricht an den Sender, dass das Inventar geöffnet wurde
        sender.sendMessage(Component.text("Du hast das Inventar von " + target.getName() + " geöffnet!", NamedTextColor.GREEN));
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                <gold><b>------- Help: Invsee -------</b>
                <yellow>/invsee <player> <white>→ <gray> Mit diesem Befehl kannst du das Inventar eines angegebenen Spielers anzeigen.
                """));
    }
}
