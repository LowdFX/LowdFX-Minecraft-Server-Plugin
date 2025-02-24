package at.lowdfx.lowdfx.kit;

import at.lowdfx.lowdfx.LowdFX;
import com.google.common.collect.Lists;
import com.marcpg.libpg.storage.JsonUtils;
import com.marcpg.libpg.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.window.Window;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class KitManager {
    public static final NamespacedKey KEY = new NamespacedKey("lowdfx", "kit-preview");

    public static final Map<UUID, Kit> KITS = new HashMap<>();
    public static final NamespacedKey STARTERKIT_KEY = new NamespacedKey("lowdfx", "got-starterkit");

    public static void load(UUID player) {
        if (!KITS.containsKey(player))
            KITS.put(player, new Kit());
    }

    public static void load() {
        KITS.putAll(JsonUtils.loadSafe(LowdFX.DATA_DIR.resolve("kits.json").toFile(), Map.of()));
    }

    public static void save() {
        JsonUtils.saveSafe(KITS, LowdFX.DATA_DIR.resolve("kits.json").toFile());
    }

    public static void showConfig(@NotNull Player player) {
        Kit kit = KITS.get(player.getUniqueId());
        if (kit == null) return;

        VirtualInventory inv = kit.inv();

        Gui gui = Gui.normal().setStructure(
                        "# h c l b # # * #", // h = Helmet
                        "# # # # # # # # #", // c = Chestplate
                        "* * * * * * * * *", // l = Leggings
                        "* * * * * * * * *", // b = Boots
                        "* * * * * * * * *", // o = Offhand
                        "* * * * * * * * *") // I = Inventory
                .addIngredient('#', Items.BLACK_BACKGROUND)
                .addIngredient('h', KitSlot.HELMET.asItem())
                .addIngredient('c', KitSlot.CHESTPLATE.asItem())
                .addIngredient('l', KitSlot.LEGGINGS.asItem())
                .addIngredient('b', KitSlot.BOOTS.asItem())
                .addIngredient('*', inv)
                .build();

        Window.single().setGui(gui)
                .setTitle("Dein Kit Layout")
                .setCloseable(true)
                .addCloseHandler(() -> {
                    kit.items.clear();
                    ItemStack[] items = inv.getItems();
                    for (int i = 0; i < items.length; i++) {
                        if (items[i] == null) continue;
                        try {
                            Integer ordinal = items[i].getItemMeta().getPersistentDataContainer().get(KEY, PersistentDataType.INTEGER);
                            if (ordinal != null)
                                kit.items.put(KitSlot.values()[ordinal], i);
                        } catch (IndexOutOfBoundsException ignored) {}
                    }
                })
                .open(player);
    }

    public static final class Kit {
        private final Map<KitSlot, Integer> items = new HashMap<>();

        public Kit(Map<KitSlot, Integer> items) {
            this.items.putAll(items);
        }

        public Kit() {
            this(Map.of(                        //   Default slots for kits:
                    KitSlot.SWORD, 0,           // [X][ ][ ][ ][ ][ ][ ][ ][ ]
                    KitSlot.AXE, 1,             // [ ][X][ ][ ][ ][ ][ ][ ][ ]
                    KitSlot.PICKAXE, 2,         // [ ][ ][X][ ][ ][ ][ ][ ][ ]
                    KitSlot.SHOVEL, 3,          // [ ][ ][ ][X][ ][ ][ ][ ][ ]
                    KitSlot.EXTRA_FOOD, 7,      // [ ][ ][ ][ ][ ][ ][ ][X][ ]
                    KitSlot.FOOD, 8             // [ ][ ][ ][ ][ ][ ][ ][ ][X]
            ));                                 // [S][A][p][s][ ][ ][ ][F][f]
        }

        public Map<KitSlot, Integer> items() {
            return items;
        }

        public @NotNull VirtualInventory inv() {
            VirtualInventory inv = new VirtualInventory(37);
            Lists.reverse(List.copyOf(items.entrySet())).forEach(e -> inv.setItemSilently(e.getValue(), e.getKey().asItem()));
            return inv;
        }
    }

    public enum KitSlot {
        SWORD(Material.IRON_SWORD, "Sword", "Your main weapon."),
        AXE(Material.IRON_AXE, "Axe", "Your secondary weapon, also used to break wood."),
        PICKAXE(Material.IRON_PICKAXE, "Pickaxe", "Your tool for breaking stone."),
        SHOVEL(Material.IRON_SHOVEL, "Shovel", "Your tool for breaking dirt."),
        HELMET(Material.IRON_HELMET, "Helmet", "Armor for your head."),
        CHESTPLATE(Material.IRON_CHESTPLATE, "Chestplate", "Armor for your upper body."),
        LEGGINGS(Material.IRON_LEGGINGS, "Leggings", "Armor for your legs."),
        BOOTS(Material.IRON_BOOTS, "Boots", "Armor for your feet."),
        FOOD(Material.APPLE, "Food", "Food to still your hunger."),
        EXTRA_FOOD(Material.GOLDEN_APPLE, "Extra Food", "Special food with special effects.");

        private final Material example;
        private final String name;
        private final String description;

        KitSlot(Material example, String name, String description) {
            this.example = example;
            this.name = name;
            this.description = description;
        }

        public ItemStack asItem() {
            return new ItemBuilder(example)
                    .name(Component.text(name, NamedTextColor.YELLOW))
                    .lore(List.of(Component.text(description, NamedTextColor.GRAY)))
                    .editMeta(m -> m.getPersistentDataContainer().set(KEY, PersistentDataType.INTEGER, ordinal()))
                    .build();
        }
    }
}
