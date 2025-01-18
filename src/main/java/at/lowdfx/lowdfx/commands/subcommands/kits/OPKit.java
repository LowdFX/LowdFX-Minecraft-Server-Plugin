package at.lowdfx.lowdfx.commands.subcommands.kits;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.items.opkit.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OPKit implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.low.opkit";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!cmd.getName().equalsIgnoreCase("low")) return false;

        // Prüfen, ob ein Subcommand angegeben wurde
        if (args.length == 1 && args[0].equalsIgnoreCase("opkit")) {
            if (sender.hasPermission(ADMIN_PERMISSION) && sender instanceof Player player) {
                player.getInventory().addItem(OPNetheriteSword.get());
                player.getInventory().addItem(OPNetheritePickaxe.get());
                player.getInventory().addItem(OPNetheriteShovel.get());
                player.getInventory().addItem(OPNetheriteAxe.get());
                player.getInventory().addItem(OPStick.get());
                player.getInventory().addItem(OPNetheriteHelmet.get());
                player.getInventory().addItem(OPNetheriteChestplate.get());
                player.getInventory().addItem(OPNetheriteLeggings.get());
                player.getInventory().addItem(OPNetheriteBoots.get());
                player.getInventory().addItem(OPFood.get());
                player.getInventory().addItem(OPApple.get());

                sender.sendMessage(Lowdfx.serverMessage(Component.text("Hier ist dein OP Kit!", NamedTextColor.RED)));
            } else {
                sender.sendMessage(Lowdfx.serverMessage(Component.text(Objects.requireNonNull(Lowdfx.CONFIG.getString("basic.noPermission")), NamedTextColor.RED)));
            }
            return true;
        }

        // Standardnachricht für den /low-Befehl ohne Subcommand
        sender.sendMessage(Lowdfx.serverMessage(Component.text("Ungültiges Argument! Nutze /low help für Informationen.", NamedTextColor.RED)));
        return true;
    }
}
