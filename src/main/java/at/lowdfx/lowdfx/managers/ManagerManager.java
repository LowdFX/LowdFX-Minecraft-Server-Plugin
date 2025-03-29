package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.block.ChestShopManager;
import at.lowdfx.lowdfx.managers.block.LockableManager;
import at.lowdfx.lowdfx.managers.moderation.MuteManager;
import at.lowdfx.lowdfx.managers.moderation.VanishManager;
import at.lowdfx.lowdfx.managers.moderation.WarnManager;
import at.lowdfx.lowdfx.managers.teleport.*;

import java.util.List;

public final class ManagerManager {
    public static final List<Class<?>> CLASSES = List.of(
            ChestShopManager.class, HomeManager.class, LockableManager.class, PlaytimeManager.class,
            SpawnManager.class, TeleportManager.class, VanishManager.class, WarnManager.class, WarpManager.class,
            MuteManager.class, KitManager.class);

    public static void load() {
        for (Class<?> manager : CLASSES) {
            try {
                manager.getMethod("load").invoke(null);
            } catch (ReflectiveOperationException e) {
                LowdFX.LOG.error("Could not load manager: {}", manager.getSimpleName(), e);
            }
        }
    }

    public static void save() {
        for (Class<?> manager : CLASSES) {
            try {
                manager.getMethod("save").invoke(null);
            } catch (ReflectiveOperationException e) {
                LowdFX.LOG.error("Could not load manager: {}", manager.getSimpleName(), e);
            }
        }
    }
}
