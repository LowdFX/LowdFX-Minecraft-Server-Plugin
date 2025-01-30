package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.command.MuteCommands;
import at.lowdfx.lowdfx.moderation.Mute;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MuteEvents implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onAsyncChat(@NotNull AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!MuteCommands.MUTES.containsKey(event.getPlayer().getUniqueId())) return;

        Mute mute = MuteCommands.MUTES.get(uuid);

        if (player.hasPermission("poo.mute") || player.hasPermission("poo.admin")) {
            MuteCommands.MUTES.remove(uuid);
        } else if (mute.isOver()) {
            MuteCommands.MUTES.remove(uuid);
            player.sendMessage(LowdFX.serverMessage(Component.text("Deine Stummschaltung ist abgelaufen. Viel Spaß im Chat und sei nett!", NamedTextColor.GREEN)));
        } else {
            event.message(Component.empty());
            event.setCancelled(true);
            player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst nicht schreiben, weil du gerade stumm geschaltet bist!", NamedTextColor.RED)));
            player.sendMessage(LowdFX.serverMessage(Component.text("Verfall: In " + mute.timeLeft().getPreciselyFormatted(), NamedTextColor.GOLD)));
            player.sendMessage(LowdFX.serverMessage(Component.text("Grund: \"" + mute.reason() + "\"", NamedTextColor.GOLD)));
        }
    }
}
