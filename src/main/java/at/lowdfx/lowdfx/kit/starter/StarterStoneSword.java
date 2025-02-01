package at.lowdfx.lowdfx.kit.starter;

import at.lowdfx.lowdfx.LowdFX;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StarterStoneSword {
    public static final ItemStack ITEM = new ItemStack(Material.STONE_SWORD);

    static {
        ITEM.editMeta(meta -> {
            meta.displayName(Component.text("Starter Steinschwert", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
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
