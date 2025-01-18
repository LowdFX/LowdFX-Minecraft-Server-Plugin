package at.lowdfx.lowdfx.items.opkit;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class OPFood extends JavaPlugin {
    public static final ItemStack ITEM = new ItemStack(Material.GOLDEN_CARROT);

    static {
        ITEM.editMeta(meta -> {
            meta.displayName(Component.text("OP Essen", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            meta.lore(Lowdfx.OP_LORE);
        });
    }

    public static @NotNull ItemStack get() {
        ItemStack item = new ItemStack(ITEM);
        item.setAmount(64);
        return item;
    }
}
