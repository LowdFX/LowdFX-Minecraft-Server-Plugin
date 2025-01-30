package at.lowdfx.lowdfx.item.opkit;

import at.lowdfx.lowdfx.LowdFX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OPApple {
    public static final ItemStack ITEM = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);

    static {
        ITEM.editMeta(meta -> {
            meta.displayName(Component.text("OP Apfel", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            meta.lore(LowdFX.OP_LORE);
        });
    }

    public static @NotNull ItemStack get() {
        ItemStack item = new ItemStack(ITEM);
        item.setAmount(64);
        return item;
    }
}

