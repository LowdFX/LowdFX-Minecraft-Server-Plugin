package at.lowdfx.lowdfx.util;

import com.marcpg.libpg.data.time.Time;

import java.util.UUID;

public class PlaytimeInfo {
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
