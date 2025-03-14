package at.lowdfx.lowdfx.managers.moderation;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.Utilities;
import com.google.gson.reflect.TypeToken;
import com.marcpg.libpg.data.time.Time;
import com.marcpg.libpg.storage.JsonUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MuteManager {
    public record Mute(UUID player, String by, String reason, Time duration, long given) {
        public @NotNull Time timeLeft() {
            return new Time((given + duration.get()) - Utilities.currentTimeSecs());
        }

        public boolean isOver() {
            return given + duration.get() <= Utilities.currentTimeSecs();
        }
    }

    public static final Time MAX_TIME = new Time(1, Time.Unit.YEARS);
    public static final Map<UUID, Mute> MUTES = new HashMap<>();

    public static void save() {
        JsonUtils.saveSafe(MUTES, LowdFX.DATA_DIR.resolve("mutes.json").toFile());
    }

    public static void load() {
        MUTES.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("mutes.json").toFile(), Map.of(), new TypeToken<>() {}));
    }

    public static void mute(UUID target, Audience by, String reason, Time duration) {
        if (MUTES.containsKey(target)) {
            by.sendMessage(LowdFX.serverMessage(Component.text("Der Spieler " + Bukkit.getOfflinePlayer(target).getName() + " ist schon stumm geschaltet!", NamedTextColor.RED)));
            return;
        }

        String byText = by instanceof Player p ? p.getName() : "Console";
        MUTES.put(target, new Mute(target, byText, reason, duration, Utilities.currentTimeSecs()));

        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            Objects.requireNonNull(targetPlayer).sendMessage(LowdFX.serverMessage(Component.text("Du bist ab jetzt auf diesem Server stumm geschaltet!", NamedTextColor.RED)
                    .appendNewline()
                    .append(Component.text("Verfall: ", NamedTextColor.GRAY).append(Component.text("In " + duration.getPreciselyFormatted(), NamedTextColor.BLUE)))
                    .appendNewline()
                    .append(Component.text("Grund: ", NamedTextColor.GRAY).append(Component.text(reason, NamedTextColor.BLUE)))));
            Utilities.negativeSound(targetPlayer);
        }
        by.sendMessage(LowdFX.serverMessage(Component.text(Bukkit.getOfflinePlayer(target).getName() + " erfolgreich für " + duration.getPreciselyFormatted() + " stumm geschaltet mit dem Grund: \"" + reason + "\"", NamedTextColor.YELLOW)));
        LowdFX.LOG.info("{} muted {} for {} with the reason: \"{}\"!", byText, Bukkit.getOfflinePlayer(target).getName(), duration.getPreciselyFormatted(), reason);
    }

    public static void unmute(UUID target, Audience by) {
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);

        if (!MUTES.containsKey(target)) {
            by.sendMessage(LowdFX.serverMessage(Component.text("Der Spieler " + targetPlayer.getName() + " ist nicht stumm geschaltet!", NamedTextColor.RED)));
            return;
        }

        MUTES.remove(target);

        by.sendMessage(LowdFX.serverMessage(Component.text("Erfolgreich " + targetPlayer.getName() + "s Stummschaltung entfernt!", NamedTextColor.YELLOW)));
        LowdFX.LOG.info("{} unmuted {}!", by instanceof Player p ? p.getName() : "Console", targetPlayer);
    }
}
