package at.lowdfx.lowdfx.kit.starter;

import at.lowdfx.lowdfx.kit.KitManager;
import com.marcpg.libpg.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StarterLeatherHelmet {
    public static final ItemStack ITEM = new ItemBuilder(Material.LEATHER_HELMET)
            .name(Component.text("Starter Lederkappe", NamedTextColor.RED))
            .lore(KitManager.STARTER_LORE)
            .enchant(Enchantment.UNBREAKING, 1)
            .enchant(Enchantment.PROTECTION, 1)
            .editMeta(m -> m.setUnbreakable(true))
            .build();

    public static @NotNull ItemStack get() {
        return new ItemStack(ITEM);
    }
}