package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import at.lowdfx.lowdfx.managers.block.ChestShopManager;
import at.lowdfx.lowdfx.util.Configuration;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HologramManager {
    public static final NamespacedKey KEY = new NamespacedKey("lowdfx", "hologram");

    private static final Map<Location, List<ArmorStand>> HOLOGRAMS = new HashMap<>();

    public static void load() {
        Bukkit.getScheduler().runTaskTimer(LowdFX.PLUGIN, () -> {
            if (HOLOGRAMS.isEmpty()) return;

            HOLOGRAMS.forEach((location, stands) -> {
                if (stands == null)
                    return;

                ChestShopManager.Shop shop = ChestShopManager.getShop(location);
                if (shop == null)
                    return;

                List<Component> text = ChestShopManager.hologramText(shop);
                for (int i = 0; i < text.size(); i++) {
                    try {
                        stands.get(i).customName(text.get(i));
                    } catch (IndexOutOfBoundsException e) {
                        Location loc = shop.location().asLocation().add(0.5, 1.05 + (0.25 * i), 0.5);
                        stands.add(i, loc.getWorld().spawn(loc, ArmorStand.class, armorStandInit(text.get(i))));
                    }
                }
            });
        }, Configuration.BASIC_HOLOGRAM_REFRESH_INTERVAL, Configuration.BASIC_HOLOGRAM_REFRESH_INTERVAL);
    }

    public static void save() {
        removeAll();
    }

    public static void spawnSafe(Location location, @NotNull List<Component> text) {
        remove(location);
        add(location, true, text);
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

    private static @NotNull Consumer<ArmorStand> armorStandInit(Component name) {
        return s -> {
            s.getPersistentDataContainer().set(KEY, PersistentDataType.BOOLEAN, true);
            s.setGravity(false);
            s.setVisible(false);
            s.setMarker(true);
            s.customName(name);
            s.setCustomNameVisible(true);
        };
    }

    public static void fixAll() {
        HOLOGRAMS.forEach((l, s) -> {
            l.clone().getNearbyEntitiesByType(ArmorStand.class, 2, 20, 2, e -> e.getPersistentDataContainer().has(KEY)).forEach(Entity::remove);
            s.clear();
        });
    }
}
