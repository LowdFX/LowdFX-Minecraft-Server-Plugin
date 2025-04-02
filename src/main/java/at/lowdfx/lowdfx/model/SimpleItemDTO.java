package at.lowdfx.lowdfx.model;

import java.util.Map;

public class SimpleItemDTO {
    private final Map<String, Object> itemData;

    public SimpleItemDTO(Map<String, Object> itemData) {
        this.itemData = itemData;
    }

    public Map<String, Object> getItemData() {
        return itemData;
    }
}
