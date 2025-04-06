package at.lowdfx.lowdfx.listeners;

import at.lowdfx.lowdfx.util.Perms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class CommandSignCreationListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Component lineComponent = event.line(0);
        String line0 = PlainTextComponentSerializer.plainText().serialize(lineComponent);
        if (line0 == null) return;
        line0 = line0.trim();
        // Prüfe, ob die erste Zeile das Format [TYPE] hat
        if (!line0.startsWith("[") || !line0.endsWith("]")) return;

        // Extrahiere den Typ (z. B. "warp")
        String type = line0.substring(1, line0.length() - 1).toLowerCase();

        // Verwende die globale Permission aus dem Perm-Enum
        Player player = event.getPlayer();
        if (!Perms.check(player, Perms.Perm.COMMANDSIGN)) {
            event.setCancelled(true);
            return;
        }

        // Setze die erste Zeile in Blau
        event.line(0, Component.text("[" + type + "]").color(NamedTextColor.BLUE));
    }
}
