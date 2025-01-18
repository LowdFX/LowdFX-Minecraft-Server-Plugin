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
import org.jetbrains.annotations.NotNull;

public class TrashCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.trash";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTask(Lowdfx.PLUGIN, () -> {
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                if (args.length == 0) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                        return;
                    }
                    trash(sender, player);
                    return;
                }

                if (args.length == 1) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (args[0].equalsIgnoreCase("help")) {
                        sendHelp(sender);
                        return;
                    }
                }

            }
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutze /trash help um eine Hilfe zu erhalten!", NamedTextColor.RED)));
        });
        return true;
    }

    private void trash(CommandSender sender, @NotNull Player player) {
        player.openInventory(Bukkit.getServer().createInventory(player, 36, Component.text("Mülleimer")));
        player.sendMessage(Lowdfx.serverMessage(Component.text("Der Mülleimer öffnet sich!", NamedTextColor.GREEN)));
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                <gold><b>------- Help: Trash -------</b>
                <yellow>/trash <white>→ <gray> Mit diesem Befehl kannst du einen Mülleimer öffnen, Items verschwinden unwiderruflich!
                """));
    }
}
