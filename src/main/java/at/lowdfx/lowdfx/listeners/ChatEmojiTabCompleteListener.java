package at.lowdfx.lowdfx.listeners;

import at.lowdfx.lowdfx.managers.EmojiManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

public class ChatEmojiTabCompleteListener implements Listener {

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
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        // Verwende den Serializer, der § als Farbcode ausgibt
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        // Ersetze :code: durch das passende Symbol
        for (String code : EmojiManager.getEmojiCodes()) {
            String find = ":" + code + ":";
            if (message.contains(find)) {
                Component symbolComponent = EmojiManager.getEmojiSymbol(code);
                if (symbolComponent != null) {
                    // Dies liefert einen String mit §-Farbmarkierungen, z. B. "§6❤"
                    String symbol = serializer.serialize(symbolComponent);
                    message = message.replace(find, symbol);
                }
            }
        }
        event.setMessage(message);
    }
}
