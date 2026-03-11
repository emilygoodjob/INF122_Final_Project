package gmae.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Inventory {

    private final List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item must not be null");
        }
        items.add(item);
    }

    public Item removeItem(UUID itemId) {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.getId().equals(itemId)) {
                items.remove(i);
                return item;
            }
        }
        return null;
    }

    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    public void updateItem(UUID itemId, String name, int rarity, String type, String description) {
        Item item = findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("No item found for id: " + itemId);
        }
        item.update(name, rarity, type, description);
    }

    public Item findById(UUID itemId) {
        for (Item item : items) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public <T extends Item> List<T> getItemsOfType(Class<T> itemType) {
        List<T> matchingItems = new ArrayList<>();
        for (Item item : items) {
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
