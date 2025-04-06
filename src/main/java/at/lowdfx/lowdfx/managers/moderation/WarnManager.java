package at.lowdfx.lowdfx.managers.moderation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.Configuration;
import at.lowdfx.lowdfx.util.Utilities;
import com.google.gson.reflect.TypeToken;
import com.marcpg.libpg.data.time.Time;
import com.marcpg.libpg.storage.JsonUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public final class WarnManager {
    public record Warn(UUID warned, String warner, String reason, long given) {}

    public static final Map<UUID, ArrayList<Warn>> WARNS = new HashMap<>();

    public static void save() {
        WARNS.keySet().forEach(WarnManager::update);
        JsonUtils.saveSafe(WARNS, LowdFX.DATA_DIR.resolve("warns.json").toFile());
    }

    public static void load() {
        WARNS.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("warns.json").toFile(), Map.of(), new TypeToken<>() {}));
        WARNS.keySet().forEach(WarnManager::update);
    }

    public static void warn(UUID player, Audience by, String reason) {
        update(player);

        ArrayList<Warn> warns = WARNS.computeIfAbsent(player, k -> new ArrayList<>());
        warns.add(new Warn(player, by instanceof Player p ? p.getName() : "Console", reason, System.currentTimeMillis()));

        String name = Bukkit.getOfflinePlayer(player).getName();

        if (warns.size() == 2) {
            Utilities.ban(player, name, banMessage(player),
                    Configuration.WARNING_TEMPBAN_DURATION > 0 ? java.time.Duration.ofMillis(Configuration.WARNING_TEMPBAN_DURATION) : null,
                    "Warn System");
            by.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize(
                    "<green>Spieler wurde für <red>" + Time.preciselyFormat(Configuration.WARNING_TEMPBAN_DURATION / 1000) + " <green> temporär gebannt.")));
        } else if (warns.size() >= 3) {
            Utilities.ban(player, name, banMessage(player), null, "Warn System");
            by.sendMessage(LowdFX.serverMessage(MiniMessage.miniMessage().deserialize("<green>Spieler wurde <red>permanent <green> gebannt.")));
        }
    }


    public static int amount(UUID user) {
        ArrayList<Warn> warns = WARNS.get(user);
        return warns == null ? 0 : warns.size();
    }

    public static void update(UUID user) {
        // Entferne keine Warnungen mehr, da Verwarnungen dauerhaft sein sollen.
        // Die folgende Zeile wurde entfernt, um das automatische Verfallen der Verwarnungen zu unterbinden:
        // warns.removeIf(w -> w.given + Configuration.WARNING_EXPIRATION < System.currentTimeMillis());
    }

    public static @NotNull Component banMessage(UUID user) {
        return Component.text().append(MiniMessage.miniMessage().deserialize("""
                    <red>Bei <b>2</b> Verwarnungen hast du einen temporären Ban für <duration>!
                    Bei <b>3</b> Verwarnungen hast du einen permanenten Ban!
                    ------------------------------------------------------------------
                    """,
                Placeholder.unparsed("duration", Time.preciselyFormat(Configuration.WARNING_TEMPBAN_DURATION / 1000)))).append(warnList(user)).build();
    }

    public static @NotNull List<Component> warnList(UUID user) {
        ArrayList<Warn> warns = WARNS.get(user);
        if (warns == null || warns.isEmpty()) return List.of();

        List<Component> text = new ArrayList<>();
        for (int i = 0; i < warns.size(); i++) {
            Warn w = warns.get(i);
            text.add(Component.newline()
                    .append(Component.text("➽ " + (i + 1) + ". Grund: ", NamedTextColor.GRAY)
                            .append(Component.text(w.reason(), NamedTextColor.RED)))
                    .append(Component.text(", von: ", NamedTextColor.GRAY)
                            .append(Component.text(w.warner(), NamedTextColor.GOLD)))
                    .append(Component.text(", am: ", NamedTextColor.GRAY)
                            .append(Component.text(
                                    LowdFX.TIME_FORMAT.format(LocalDateTime.ofEpochSecond(w.given() / 1000, 0, ZoneOffset.UTC)),
                                    NamedTextColor.WHITE))));
        }
        return text;
    }

    public static boolean removeLastWarn(UUID user) {
        update(user);
        ArrayList<Warn> warns = WARNS.get(user);
        if (warns == null || warns.isEmpty()) {
            return false;
        }
        warns.remove(warns.size() - 1);
        if (warns.isEmpty())
            WARNS.remove(user);
        else if (warns.size() < 2) {
            Utilities.unban(user);
        }
        return true;
    }

    public static boolean removeAllWarns(UUID user) {
        if (WARNS.containsKey(user)) {
            WARNS.remove(user);
            Utilities.unban(user);
            return true;
        }
        return false;
    }
}
