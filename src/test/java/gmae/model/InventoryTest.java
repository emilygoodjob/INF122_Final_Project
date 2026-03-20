package gmae.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
    }

    @Test
    void startsEmpty() {
        assertTrue(inventory.isEmpty());
        assertEquals(0, inventory.size());
    }

    @Test
    void addItemIncreasesSize() {
        inventory.addItem(new ItemAdapter("Sword", 3, "Weapon", "A sharp blade"));
        assertEquals(1, inventory.size());
        assertFalse(inventory.isEmpty());
    }

    @Test
    void addNullItemThrows() {
        assertThrows(IllegalArgumentException.class, () -> inventory.addItem(null));
    }

    @Test
    void findByIdReturnsCorrectItem() {
        ItemAdapter item = new ItemAdapter("Shield", 2, "Armor", "Wooden shield");
        inventory.addItem(item);
        ItemAdapter found = inventory.findById(item.getId());
        assertSame(item, found);
    }

    @Test
    void findByIdReturnsNullForMissingId() {
        assertNull(inventory.findById(java.util.UUID.randomUUID()));
    }

    @Test
    void removeByIdDecreasesSize() {
        ItemAdapter item = new ItemAdapter("Potion", 1, "Consumable", "Heals 10 HP");
        inventory.addItem(item);
        inventory.removeItem(item.getId());
        assertEquals(0, inventory.size());
    }

    @Test
    void removeByIdReturnsRemovedItem() {
        ItemAdapter item = new ItemAdapter("Potion", 1, "Consumable", "desc");
        inventory.addItem(item);
        ItemAdapter removed = inventory.removeItem(item.getId());
        assertSame(item, removed);
    }

    @Test
    void removeByIdReturnsNullWhenNotFound() {
        assertNull(inventory.removeItem(java.util.UUID.randomUUID()));
    }

    @Test
    void clearEmptiesInventory() {
        inventory.addItem(new ItemAdapter("ItemA", 1, "TypeA", "desc"));
        inventory.addItem(new ItemAdapter("ItemB", 2, "TypeB", "desc"));
        inventory.clear();
        assertTrue(inventory.isEmpty());
    }

    @Test
    void getItemsReturnsUnmodifiableView() {
        inventory.addItem(new ItemAdapter("Ring", 1, "Accessory", "desc"));
        List<ItemAdapter> items = inventory.getItems();
        assertThrows(UnsupportedOperationException.class, () -> items.add(new ItemAdapter("Extra", 1, "Type", "desc")));
    }

    @Test
    void getItemsOfTypeFiltersCorrectly() {
        ItemAdapter base = new ItemAdapter("Base", 1, "Base", "desc");
        inventory.addItem(base);

        // Subclass
        ItemAdapter sub = new ItemAdapter("Sub", 2, "Sub", "desc") {};
        inventory.addItem(sub);

        List<ItemAdapter> all = inventory.getItemsOfType(ItemAdapter.class);
        assertEquals(2, all.size());
    }

    @Test
    void updateItemChangesFields() {
        ItemAdapter item = new ItemAdapter("OldName", 1, "OldType", "OldDesc");
        inventory.addItem(item);
        inventory.updateItem(item.getId(), "NewName", 5, "NewType", "NewDesc");
        assertEquals("NewName", item.getName());
        assertEquals(5, item.getRarity());
        assertEquals("NewType", item.getType());
        assertEquals("NewDesc", item.getDescription());
    }
}
