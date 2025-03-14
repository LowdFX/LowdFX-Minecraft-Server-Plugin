package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.util.Utilities;
import com.google.gson.reflect.TypeToken;
import com.marcpg.libpg.data.time.Time;
import com.marcpg.libpg.storage.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlaytimeManager {
    public static class PlaytimeInfo {
        public final UUID uuid;
        public final Time totalTime; // 1000ms Accuracy
        protected long lastLogin; // ~5ms Accuracy
        protected long lastLogout; // ~5ms Accuracy

        public PlaytimeInfo(UUID uuid, Time totalTime, long lastLogin, long lastLogout) {
            this.uuid = uuid;
            this.totalTime = totalTime;
            this.lastLogin = lastLogin;
            this.lastLogout = lastLogout;
        }

        public PlaytimeInfo(UUID uuid) {
            this(uuid, new Time(0), Utilities.currentTimeSecs(), 0);
        }

        public void login() {
            lastLogin = Utilities.currentTimeSecs();
        }

        public void logout() {
            lastLogout = Utilities.currentTimeSecs();
            totalTime.increment(lastLogout - lastLogin);
        }

        public Time totalTime() {
            if (lastLogin > lastLogout) {
                return new Time(totalTime.get() + (Utilities.currentTimeSecs() - lastLogin));
            } else {
                return totalTime;
            }
        }
    }

    public static final Map<UUID, PlaytimeInfo> PLAYTIMES = new HashMap<>();

    public static void save() {
        JsonUtils.saveSafe(PLAYTIMES, LowdFX.DATA_DIR.resolve("playtime.json").toFile());
    }

    public static void load() {
        PLAYTIMES.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("playtime.json").toFile(), Map.of(), new TypeToken<>() {}));
    }
}
