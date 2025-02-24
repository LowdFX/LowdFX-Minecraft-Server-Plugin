package at.lowdfx.lowdfx.util;

import at.lowdfx.lowdfx.LowdFX;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HologramManager {
    private static final Map<Location, List<ArmorStand>> HOLOGRAMS = new HashMap<>();
    private static final Map<Location, BukkitTask> UPDATERS = new HashMap<>();

    public static void runUpdater(Location location, int period, Supplier<List<Component>> textSupplier) {
        if (UPDATERS.containsKey(location)) return;

        UPDATERS.put(location, Bukkit.getScheduler().runTaskTimer(LowdFX.PLUGIN, () -> {
            List<ArmorStand> stands = HologramManager.HOLOGRAMS.get(location);
            if (stands == null || stands.isEmpty()) {
                UPDATERS.get(location).cancel();
                UPDATERS.remove(location);
                return;
            }

            List<Component> text = textSupplier.get();
            for (int i = 0; i < stands.size(); i++) {
                stands.get(i).customName(text.size() > i ? text.get(i) : Component.empty());
            }
        }, period, period));
    }

    public static void spawnSafe(Location location, Component... text) {
        spawnSafe(location, List.of(text));
    }

    public static void spawnSafe(Location location, @NotNull List<Component> text) {
        remove(location);
        add(location, true, text);
    }

    public static void add(@NotNull Location location, boolean offset, Component... text) {
        add(location, offset, List.of(text));
    }

    public static void add(@NotNull Location location, boolean offset, @NotNull List<Component> text) {
        Location finalLocation = offset ? location.clone().add(0.5, 1.05, 0.5) : location.clone();
        HOLOGRAMS.put(location, new ArrayList<>(text.stream()
                .map(t -> finalLocation.getWorld().spawn(finalLocation.add(0, 0.25, 0), ArmorStand.class, armorStandInit(t)))
                .toList()));
    }

    public static void remove(Location location) {
        List<ArmorStand> stands = HOLOGRAMS.remove(location);
        if (stands == null || stands.isEmpty()) return;

        for (ArmorStand stand : stands) {
            if (stand != null && !stand.isDead() && stand.isValid())
                stand.remove();
        }
    }

    public static void removeAll() {
        HOLOGRAMS.values().forEach(l -> l.forEach(Entity::remove));
        HOLOGRAMS.clear();
    }

    @Contract(pure = true)
    private static @NotNull Consumer<ArmorStand> armorStandInit(Component name) {
        return s -> {
            s.setGravity(false);
            s.setVisible(false);
            s.setMarker(true);
            s.customName(name);
            s.setCustomNameVisible(true);
        };
    }
}
