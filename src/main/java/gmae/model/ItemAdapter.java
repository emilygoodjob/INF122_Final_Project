package gmae.model;

import java.util.UUID;

public class ItemAdapter {

    private final Item adaptee;

    public ItemAdapter(String name, int rarity, String type, String description) {
        this(new Item(UUID.randomUUID().toString(), validateName(name), type == null ? "" : type, description == null ? "" : description, toLegacyRarity(rarity)));
    }

    public ItemAdapter(UUID id, String name, int rarity, String type, String description) {
        this(new Item(validateId(id).toString(), validateName(name), type == null ? "" : type, description == null ? "" : description, toLegacyRarity(rarity)));
    }

    public ItemAdapter(Item adaptee) {
        if (adaptee == null) {
            throw new IllegalArgumentException("Underlying item must not be null");
        }
        this.adaptee = adaptee;
    }

    public UUID getId() {
        try {
            return UUID.fromString(adaptee.getItemId());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Stored itemId is not a valid UUID: " + adaptee.getItemId(), e);
        }
    }

    public String getName() {
        return adaptee.getName();
    }

    public int getRarity() {
        return fromLegacyRarity(adaptee.getRarity());
    }

    public String getType() {
        return adaptee.getItemType();
    }

    public String getDescription() {
        return adaptee.getDescription();
    }

    public Item getAdaptee() {
        return adaptee;
    }

    public void update(String name, int rarity, String type, String description) {
        validateName(name);
        setField(adaptee, "name", name);
        setField(adaptee, "itemType", type == null ? "" : type);
        setField(adaptee, "description", description == null ? "" : description);
        setField(adaptee, "rarity", toLegacyRarity(rarity));
    }

    @Override
    public String toString() {
        String description = getDescription();
        return getName() + (description.isBlank() ? "" : " - " + description);
    }

    private static UUID validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Item id must not be null");
        }
        return id;
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name must not be blank");
        }
        return name;
    }

    private static String toLegacyRarity(int rarity) {
        return Integer.toString(rarity);
    }

    private static int fromLegacyRarity(String rarity) {
        if (rarity == null || rarity.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(rarity.trim());
        } catch (NumberFormatException e) {
            return switch (rarity.trim().toLowerCase()) {
                case "common" -> 1;
                case "uncommon" -> 2;
                case "rare" -> 3;
                case "epic" -> 4;
                case "legendary" -> 5;
                default -> 0;
            };
        }
    }

    private static void setField(Item item, String fieldName, Object value) {
        try {
            var field = Item.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(item, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to update field: " + fieldName, e);
        }
    }
}