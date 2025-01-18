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

public class AnvilCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.anvil";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTask(Lowdfx.PLUGIN, () -> {

            if (sender.hasPermission(ADMIN_PERMISSION)) {
                if (args.length == 0) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                        return;
                    }
                    anvil(sender, player);
                    return;
                }

                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("help")) {
                        sendHelp(sender);
                        return;
                    }
                }
            }
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutze /anvil help um eine Hilfe zu erhalten!", NamedTextColor.RED)));
        });
        return true;
    }

    private void anvil(@NotNull CommandSender sender, @NotNull Player player) {
        //AnvilInventory anvil = (AnvilInventory) Bukkit.createInventory(null, Material.ANVIL);

        player.openAnvil(player.getLocation(), true);
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Der Amboss öffnet sich!", NamedTextColor.GREEN)));
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                <gold><b>------- Help: Anvil -------</b>
                <yellow>/anvil <white>→ <gray> Mit diesem Befehl kannst du einen Amboss öffnen.
                """));
    }
}
