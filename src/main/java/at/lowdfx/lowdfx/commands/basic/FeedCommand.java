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
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FeedCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.feed";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTask(Lowdfx.PLUGIN, () -> {
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                if (args.length == 0) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                        return;
                    }
                    feed(player);
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
                        feedTarget(sender, args);
                        return;
                    }
                }
            }
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutze /feed help um eine Hilfe zu erhalten!", NamedTextColor.RED)));
        });
        return true;
    }

    private void feed(@NotNull Player player) {
        player.setFoodLevel(20);
        player.sendMessage(Lowdfx.serverMessage(Component.text("Dein Hunger wurde gestillt!", NamedTextColor.GREEN)));
        for (final PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    private void feedTarget(CommandSender sender, String @NotNull [] args) {
        Player target = Objects.requireNonNull(Bukkit.getPlayer(args[0]));

        target.setFoodLevel(20);
        for (final PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }

        target.sendMessage(Lowdfx.serverMessage(Component.text("Dein Hunger wurde gestillt!", NamedTextColor.GREEN)));
        sender.sendMessage(Lowdfx.serverMessage(Component.text(args[0] + " hat keinen Hunger mehr!", NamedTextColor.GREEN)));
    }


    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                <gold><b>------- Help: Feed -------</b>
                <yellow>/feed <white>→ <gray> Mit diesem Befehl kannst den Hunger stillen.
                <yellow>/feed <player> <white>→ <gray> Mit diesem Befehl kannst du den Hunger von einem angegebenen Spieler stillen.
                """));
    }
}
