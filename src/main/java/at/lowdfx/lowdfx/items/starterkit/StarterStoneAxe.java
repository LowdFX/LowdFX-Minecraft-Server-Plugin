package at.lowdfx.lowdfx.items.starterkit;

import at.lowdfx.lowdfx.Lowdfx;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StarterStoneAxe {
    public static final ItemStack ITEM = new ItemStack(Material.STONE_AXE);

    static {
        ITEM.editMeta(meta -> {
            meta.displayName(Component.text("Starter Steinaxt", NamedTextColor.RED));
            meta.lore(Lowdfx.STARTER_LORE);
            meta.setUnbreakable(true);

            meta.addEnchant(Enchantment.EFFICIENCY, 1, false);
            meta.addEnchant(Enchantment.UNBREAKING, 1, false);
            meta.addEnchant(Enchantment.FORTUNE, 1, false);
        });
    }

    public static @NotNull ItemStack get() {
        return new ItemStack(ITEM);
    }
}