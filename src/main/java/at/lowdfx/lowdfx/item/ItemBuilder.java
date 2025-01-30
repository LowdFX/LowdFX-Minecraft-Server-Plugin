package at.lowdfx.lowdfx.item;

import at.lowdfx.lowdfx.LowdFX;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(@NotNull ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();

        if (this.meta == null)
            LowdFX.LOG.error("Could not initialize item builder of item: {}", item.getType(), new RuntimeException());
    }

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder editMeta(@NotNull Consumer<ItemMeta> metaEdit) {
        metaEdit.accept(meta);
        return this;
    }

    public ItemBuilder name(Component name) {
        meta.displayName(name);
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        meta.lore(lore);
        return this;
    }

    public ItemBuilder addLore(Component lore) {
        List<Component> newLore = meta.hasLore() ? new ArrayList<>(Objects.requireNonNull(meta.lore())) : new ArrayList<>();
        newLore.add(lore);
        meta.lore(newLore);

        return this;
    }

    public ItemBuilder amount(@Range(from = 0, to = 64) int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
