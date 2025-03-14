package at.lowdfx.lowdfx.event;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.moderation.MuteManager;
import at.lowdfx.lowdfx.util.Perms;
import at.lowdfx.lowdfx.util.Utilities;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class MuteEvents implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onAsyncChat(@NotNull AsyncChatEvent event) {
        Player player = event.getPlayer();

        MuteManager.Mute mute = MuteManager.MUTES.get(player.getUniqueId());
        if (mute == null) return;

        if (Perms.check(player, Perms.Perm.MUTE)) {
            MuteManager.MUTES.remove(player.getUniqueId());
        } else if (mute.isOver()) {
            MuteManager.MUTES.remove(player.getUniqueId());
            player.sendMessage(LowdFX.serverMessage(Component.text("Deine Stummschaltung ist abgelaufen. Viel Spaß im Chat und sei nett!", NamedTextColor.GREEN)));
            Utilities.positiveSound(player);
        } else {
            event.message(Component.empty());
            event.setCancelled(true);
            player.sendMessage(LowdFX.serverMessage(Component.text("Du kannst nicht schreiben, weil du gerade stumm geschaltet bist!", NamedTextColor.RED)));
            player.sendMessage(LowdFX.serverMessage(Component.text("Verfall: In " + mute.timeLeft().getPreciselyFormatted(), NamedTextColor.GOLD)));
            player.sendMessage(LowdFX.serverMessage(Component.text("Grund: \"" + mute.reason() + "\"", NamedTextColor.GOLD)));
            Utilities.negativeSound(player);
        }
    }
}
