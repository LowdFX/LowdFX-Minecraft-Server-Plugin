package at.lowdfx.lowdfx.item.starterkit;

import at.lowdfx.lowdfx.LowdFX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StarterStoneAxe {
    public static final ItemStack ITEM = new ItemStack(Material.STONE_AXE);

    static {
        ITEM.editMeta(meta -> {
            meta.displayName(Component.text("Starter Steinaxt", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            meta.lore(LowdFX.STARTER_LORE);
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