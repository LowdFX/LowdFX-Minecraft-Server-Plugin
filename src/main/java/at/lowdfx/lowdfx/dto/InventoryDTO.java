package at.lowdfx.lowdfx.dto;

import java.util.List;

public class InventoryDTO {
    private final List<SimpleItemDTO> mainItems;
    private final List<SimpleItemDTO> armorItems;
    private final SimpleItemDTO offhandItem;

    public InventoryDTO(List<SimpleItemDTO> mainItems, List<SimpleItemDTO> armorItems, SimpleItemDTO offhandItem) {
        this.mainItems = mainItems;
        this.armorItems = armorItems;
        this.offhandItem = offhandItem;
    }

    public List<SimpleItemDTO> getMainItems() {
        return mainItems;
    }

    public List<SimpleItemDTO> getArmorItems() {
        return armorItems;
    }

    public SimpleItemDTO getOffhandItem() {
        return offhandItem;
    }
}
