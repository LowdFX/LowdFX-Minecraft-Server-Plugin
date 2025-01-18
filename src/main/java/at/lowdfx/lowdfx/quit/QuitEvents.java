package at.lowdfx.lowdfx.quit;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class QuitEvents implements Listener {
    @EventHandler
    public void onPlayerLeave(@NotNull PlayerQuitEvent event) {
        // Globale Leave Nachricht
        event.quitMessage(Component.text(Objects.requireNonNullElse(Lowdfx.CONFIG.getString("basic.servername"), "???"), NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(" >> ", NamedTextColor.GRAY))
                .append(Component.text(Objects.requireNonNullElse(Lowdfx.CONFIG.getString("join.quit"), "???"), NamedTextColor.YELLOW))
                .append(event.getPlayer().name().color(NamedTextColor.GOLD))
                .append(Component.text("!", NamedTextColor.YELLOW)));
        // Privat Leave Nachricht
        // event.getPlayer().sendMessage(ChatColor.YELLOW + lowdfx.config.getString("welcome.quit"));
    }
}
