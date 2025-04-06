package at.lowdfx.lowdfx.managers;

import at.lowdfx.lowdfx.LowdFX;
import com.google.gson.reflect.TypeToken;
import com.marcpg.libpg.storage.JsonUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class BindManager {
    // Datenklasse für ein Binding
    public static class BindingData {
        public String command;
        public String world;
        public String material;      // z. B. "COMPASS"
        public String displayName;   // optional

        public BindingData() {}

        public BindingData(String command, String world, String material, String displayName) {
            this.command = command;
            this.world = world;
            this.material = material;
            this.displayName = displayName;
        }
    }

    // Map von Binding-Namen zu BindingData
    private static final Map<String, BindingData> bindings = new HashMap<>();
    // Datei, in der die Bindings gespeichert werden
    private static final File bindingsFile = new File(LowdFX.DATA_DIR.toFile(), "bindings.json");

    // Dieser Key wird in der Item-NBT genutzt, um den Binding-Identifier zu speichern.
    public static final NamespacedKey BIND_KEY = new NamespacedKey(LowdFX.PLUGIN, "bind_id");

    public static void addBinding(String name, BindingData data) {
        bindings.put(name, data);
        save();
    }

    public static void removeBinding(String name) {
        bindings.remove(name);
        save();
    }

    public static BindingData getBinding(String name) {
        return bindings.get(name);
    }

    public static Map<String, BindingData> getBindings() {
        return bindings;
    }

    /**
     * Markiert das übergebene Item als Bind‑Item mit der angegebenen Bind‑ID (name) und speichert dessen Eigenschaften.
     */
    public static void markAsBindItem(ItemStack item, String name, String command, String world) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(BIND_KEY, PersistentDataType.STRING, name);
        item.setItemMeta(meta);
        String material = item.getType().name();
        Component display = meta.displayName();
        String displayName = display != null ? net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(display) : null;
        BindingData data = new BindingData(command, world, material, displayName);
        addBinding(name, data);
    }

    /**
     * Gibt die Bind‑ID (Name) zurück, wenn das Item als Bind‑Item markiert ist, sonst null.
     */
    public static String getBindId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(BIND_KEY, PersistentDataType.STRING);
    }

    /**
     * Erzeugt ein neues Bind‑Item basierend auf den gespeicherten Eigenschaften des Bindings mit der angegebenen ID.
     * Falls keine Daten vorhanden sind, wird als Fallback ein COMPASS verwendet.
     */
    public static ItemStack createBindItem(String name) {
        BindingData data = getBinding(name);
        Material type;
        try {
            type = (data != null && data.material != null) ? Material.valueOf(data.material) : Material.COMPASS;
        } catch (IllegalArgumentException e) {
            type = Material.COMPASS;
        }
        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();
        if (data != null && data.displayName != null) {
            meta.displayName(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(data.displayName));
        }
        meta.getPersistentDataContainer().set(BIND_KEY, PersistentDataType.STRING, name);
        item.setItemMeta(meta);
        return item;
    }

    public static void save() {
        try {
            JsonUtils.saveSafe(bindings, bindingsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        try {
            Map<String, BindingData> loaded = JsonUtils.loadSafe(bindingsFile, new HashMap<String, BindingData>(), new TypeToken<Map<String, BindingData>>() {} );
            bindings.clear();
            bindings.putAll(loaded);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
