package at.lowdfx.lowdfx.commands.basic;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class HealCommand implements CommandExecutor {
    public static final String ADMIN_PERMISSION = "lowdfx.heal";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Bukkit.getScheduler().runTask(Lowdfx.PLUGIN, () -> {

            if (sender.hasPermission(ADMIN_PERMISSION)) {
                if (args.length == 0) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(Component.text("Fehler! Das kann nur ein Spieler tun!", NamedTextColor.RED));
                        return;
                    }
                    heal(sender, player);
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
                        healTarget(sender, target, args);
                        return;
                    }

                }

            }
            sender.sendMessage(Lowdfx.serverMessage(Component.text("Fehler: Benutze /heal help um eine Hilfe zu erhalten!", NamedTextColor.RED)));
        });
        return true;
    }

    private void heal(CommandSender sender, @NotNull Player player) {
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getBaseValue();
        double amount = maxHealth - player.getHealth();

        EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
        Lowdfx.PLUGIN.getServer().getPluginManager().callEvent(event);

        double newAmount = Math.min(player.getHealth() + event.getAmount(), maxHealth);
        player.setHealth(newAmount);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.sendMessage(Lowdfx.serverMessage(Component.text("Du wurdest geheilt!", NamedTextColor.GREEN)));

        for (final PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    private void healTarget(CommandSender sender, @NotNull Player player, String @NotNull [] args) {
        Player target = Bukkit.getPlayer(args[0]);

        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getBaseValue();
        double amount = maxHealth - player.getHealth();

        final EntityRegainHealthEvent event = new EntityRegainHealthEvent(Objects.requireNonNull(target), amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
        Lowdfx.PLUGIN.getServer().getPluginManager().callEvent(event);

        double newAmount = Math.min(player.getHealth() + event.getAmount(), maxHealth);
        target.setHealth(newAmount);
        target.setFoodLevel(20);
        target.setFireTicks(0);

        for (final PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }

        target.sendMessage(Lowdfx.serverMessage(Component.text("Du wurdest geheilt!", NamedTextColor.GREEN)));
        target.sendMessage(Lowdfx.serverMessage(Component.text(args[0] + " wurde geheilt!", NamedTextColor.GREEN)));
    }


    private void sendHelp(@NotNull CommandSender sender) {
        if (sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("""
                <gold><b>------- Help: Heal -------</b>
                <yellow>/heal <white>→ <gray> Mit diesem Befehl kannst du dich heilen.
                <yellow>/heal <player> <white>→ <gray> Mit diesem Befehl kannst du einen angegebenen Spieler heilen.
                """));
        }
    }
}
