package at.lowdfx.lowdfx.moderation;

import at.lowdfx.lowdfx.util.Utilities;
import com.marcpg.libpg.data.time.Time;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record Mute(UUID player, @Nullable UUID by, Time duration, long given, String reason) {
    public @NotNull Time timeLeft() {
        return new Time((given + duration.get()) - Utilities.currentTimeSecs());
    }

    public boolean isOver() {
        return given + duration.get() <= Utilities.currentTimeSecs();
    }
}
