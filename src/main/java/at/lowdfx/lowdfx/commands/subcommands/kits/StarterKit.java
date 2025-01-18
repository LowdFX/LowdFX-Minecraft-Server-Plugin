package at.lowdfx.lowdfx.commands.subcommands.kits;

import at.lowdfx.lowdfx.Lowdfx;
import at.lowdfx.lowdfx.items.starterkit.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StarterKit implements CommandExecutor {
    public static final String PLAYER_PERMISSION = "lowdfx.low.starterkit";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!cmd.getName().equalsIgnoreCase("low")) return false;

        // Prüfen, ob ein Subcommand angegeben wurde
        if (args.length == 1 && args[0].equalsIgnoreCase("starterkit")) {
            if (sender.hasPermission(PLAYER_PERMISSION) && sender instanceof Player player) {
                player.getInventory().addItem(StarterStoneSword.get());
                player.getInventory().addItem(StarterStonePickaxe.get());
                player.getInventory().addItem(StarterStoneShovel.get());
                player.getInventory().addItem(StarterStoneAxe.get());
                player.getInventory().addItem(StarterLeatherHelmet.get());
                player.getInventory().addItem(StarterLeatherChestplate.get());
                player.getInventory().addItem(StarterLeatherLeggings.get());
                player.getInventory().addItem(StarterLeatherBoots.get());
                player.getInventory().addItem(StarterFood.get());

                sender.sendMessage(Lowdfx.serverMessage(Component.text("Hier ist dein Starterkit!", NamedTextColor.RED)));
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
