package at.lowdfx.lowdfx.listeners;

import at.lowdfx.lowdfx.managers.EmojiManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

public class ChatEmojiTabCompleteListener implements Listener {
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onChatTabComplete(PlayerChatTabCompleteEvent event) {
        // Das zuletzt eingegebene Token (z. B. ":sm")
        String lastToken = event.getLastToken();
        // Nur fortfahren, wenn das Token mit ":" beginnt
        if (!lastToken.startsWith(":")) {
            return;
        }

        // Entferne den Doppelpunkt für den Vergleich
        String partial = lastToken.substring(1).toLowerCase();

        // Füge passende Emoji-Vorschläge hinzu, z. B. ":smile:"
        for (String code : EmojiManager.getEmojiCodes()) {
            if (code.toLowerCase().startsWith(partial)) {
                event.getTabCompletions().add(":" + code + ":");
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        String rawMessage = MiniMessage.miniMessage().serialize(event.message());

        for (String code : EmojiManager.getEmojiCodes()) {
            String placeholder = ":" + code + ":";
            String miniEmoji = EmojiManager.getEmojiMiniMessage(code);
            if (miniEmoji != null) {
                rawMessage = rawMessage.replace(placeholder, miniEmoji);
            }
        }

        Component updated = MiniMessage.miniMessage().deserialize(rawMessage);
        event.message(updated);
    }

}
