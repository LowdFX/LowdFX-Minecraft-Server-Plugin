package at.lowdfx.lowdfx.items.starterkit;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StarterFood {
    public static final ItemStack ITEM = new ItemStack(Material.BAKED_POTATO);

    static {
        ITEM.editMeta(meta -> {
            meta.displayName(Component.text("Starter Essen", NamedTextColor.RED));
            meta.lore(Lowdfx.STARTER_LORE);
        });
    }

    public static @NotNull ItemStack get() {
        ItemStack item = new ItemStack(ITEM);
        item.setAmount(64);
        return item;
    }
}