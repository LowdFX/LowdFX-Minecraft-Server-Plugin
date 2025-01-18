package at.lowdfx.lowdfx.commands.chest.shop;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ShopData(UUID owner, Location location, ItemStack item, int price) {
    @Override
    public @NotNull ItemStack item() {
        return item.clone();
    }
}