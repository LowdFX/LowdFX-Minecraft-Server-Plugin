package at.lowdfx.lowdfx.kit.starter;

import at.lowdfx.lowdfx.managers.KitManager;
import com.marcpg.libpg.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StarterStoneShovel {
    public static final ItemStack ITEM = new ItemBuilder(Material.STONE_SHOVEL)
            .name(Component.text("Starter Steinschaufel", NamedTextColor.RED))
            .lore(KitManager.STARTER_LORE)
            .enchant(Enchantment.EFFICIENCY, 1)
            .enchant(Enchantment.UNBREAKING, 1)
            .enchant(Enchantment.FORTUNE, 1)
            .editMeta(m -> m.setUnbreakable(true))
            .build();

    public static @NotNull ItemStack get() {
        return new ItemStack(ITEM);
    }
}