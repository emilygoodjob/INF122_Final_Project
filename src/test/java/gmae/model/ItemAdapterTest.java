package gmae.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ItemAdapterTest {

    @Test
    void constructorSetsFields() {
        ItemAdapter item = new ItemAdapter("Axe", 3, "Weapon", "A two-handed axe");
        assertEquals("Axe", item.getName());
        assertEquals(3, item.getRarity());
        assertEquals("Weapon", item.getType());
        assertEquals("A two-handed axe", item.getDescription());
    }

    @Test
    void constructorThrowsOnBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new ItemAdapter("", 1, "Type", "desc"));
    }

    @Test
    void constructorThrowsOnNullName() {
        assertThrows(IllegalArgumentException.class, () -> new ItemAdapter(null, 1, "Type", "desc"));
    }

    @Test
    void idIsNotNull() {
        ItemAdapter item = new ItemAdapter("Arrow", 1, "Ammo", "desc");
        assertNotNull(item.getId());
    }

    @Test
    void twoItemsHaveDifferentIds() {
        ItemAdapter a = new ItemAdapter("A", 1, "T", "d");
        ItemAdapter b = new ItemAdapter("B", 1, "T", "d");
        assertNotEquals(a.getId(), b.getId());
    }

    @Test
    void constructorWithExplicitUUID() {
        UUID id = UUID.randomUUID();
        ItemAdapter item = new ItemAdapter(id, "Rune", 4, "Magic", "Ancient rune");
        assertEquals(id, item.getId());
        assertEquals("Rune", item.getName());
    }

    @Test
    void nullTypeDefaultsToEmptyString() {
        ItemAdapter item = new ItemAdapter("Gem", 2, null, "desc");
        assertEquals("", item.getType());
    }

    @Test
    void nullDescriptionDefaultsToEmptyString() {
        ItemAdapter item = new ItemAdapter("Stone", 1, "Rock", null);
        assertEquals("", item.getDescription());
    }

    @Test
    void updateChangesAllFields() {
        ItemAdapter item = new ItemAdapter("Old", 1, "OldType", "OldDesc");
        item.update("New", 5, "NewType", "NewDesc");
        assertEquals("New", item.getName());
        assertEquals(5, item.getRarity());
        assertEquals("NewType", item.getType());
        assertEquals("NewDesc", item.getDescription());
    }

    @Test
    void rarityZeroForEmptyLegacyValue() {
        Item raw = new Item(UUID.randomUUID().toString(), "Test", "T", "d", "");
        ItemAdapter item = new ItemAdapter(raw);
        assertEquals(0, item.getRarity());
    }

    @Test
    void rarityParsedFromLegacyStringCommon() {
        Item raw = new Item(UUID.randomUUID().toString(), "Test", "T", "d", "common");
        ItemAdapter item = new ItemAdapter(raw);
        assertEquals(1, item.getRarity());
    }

    @Test
    void toStringContainsName() {
        ItemAdapter item = new ItemAdapter("Dagger", 2, "Weapon", "Sharp");
        assertTrue(item.toString().contains("Dagger"));
    }
}
