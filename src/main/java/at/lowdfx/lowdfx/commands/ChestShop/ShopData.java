package at.lowdfx.lowdfx.commands.ChestShop;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShopData {

    private final UUID owner;
    private final Location location;
    private final ItemStack item;
    private final int price;

    public ShopData(UUID owner, Location location, ItemStack item, int price) {
        this.owner = owner;
        this.location = location;
        this.item = item;
        this.price = price;
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getLocation() {
        return location;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public int getPrice() {
        return price;
    }
}