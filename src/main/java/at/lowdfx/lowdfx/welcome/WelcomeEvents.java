package at.lowdfx.lowdfx.welcome;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WelcomeEvents implements Listener {
    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        // Globale Join Nachricht
        event.joinMessage(Component.text(Objects.requireNonNullElse(Lowdfx.CONFIG.getString("basic.servername"), "???"), NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(" >> ", NamedTextColor.GRAY))
                .append(Component.text(Objects.requireNonNullElse(Lowdfx.CONFIG.getString("join.welcome"), "???"), NamedTextColor.YELLOW))
                .append(event.getPlayer().name().color(NamedTextColor.GOLD))
                .append(Component.text("!", NamedTextColor.YELLOW)));
        // Privat Nachricht
        // event.getPlayer().sendMessage(ChatColor.YELLOW + "Guten Tag!");
    }
}
