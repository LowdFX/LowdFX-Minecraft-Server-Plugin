package at.lowdfx.lowdfx.dto;

import org.bukkit.inventory.ItemStack;
import java.util.List;

public class InventoryData {
    private final List<ItemStack> mainItems;
    private final List<ItemStack> armorItems;
    private final List<ItemStack> offhandItems;

    public InventoryData(List<ItemStack> mainItems, List<ItemStack> armorItems, List<ItemStack> offhandItems) {
        this.mainItems = mainItems;
        this.armorItems = armorItems;
        this.offhandItems = offhandItems;
    }

    public List<ItemStack> getMainItems() {
        return mainItems;
    }

    public List<ItemStack> getArmorItems() {
        return armorItems;
    }

    public List<ItemStack> getOffhandItems() {
        return offhandItems;
    }
}
