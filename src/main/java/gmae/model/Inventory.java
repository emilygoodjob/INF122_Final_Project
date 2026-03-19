package gmae.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Inventory {

    private final List<ItemAdapter> items = new ArrayList<>();

    public void addItem(ItemAdapter item) {
        if (item == null) {
            throw new IllegalArgumentException("Item must not be null");
        }
        items.add(item);
    }

    public ItemAdapter removeItem(UUID itemId) {
        for (int i = 0; i < items.size(); i++) {
            ItemAdapter item = items.get(i);
            if (item.getId().equals(itemId)) {
                items.remove(i);
                return item;
            }
        }
        return null;
    }

    public boolean removeItem(ItemAdapter item) {
        return items.remove(item);
    }

    public void updateItem(UUID itemId, String name, int rarity, String type, String description) {
        ItemAdapter item = findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("No item found for id: " + itemId);
        }
        item.update(name, rarity, type, description);
    }

    public ItemAdapter findById(UUID itemId) {
        for (ItemAdapter item : items) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    public List<ItemAdapter> getItems() {
        return Collections.unmodifiableList(items);
    }

    public <T extends ItemAdapter> List<T> getItemsOfType(Class<T> itemType) {
        List<T> matchingItems = new ArrayList<>();
        for (ItemAdapter item : items) {
            if (itemType.isInstance(item)) {
                matchingItems.add(itemType.cast(item));
            }
        }
        return List.copyOf(matchingItems);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }
}
