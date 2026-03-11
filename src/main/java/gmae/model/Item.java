package gmae.model;

import java.util.UUID;

public class Item {

    private final UUID id;
    private String name;
    private int rarity;
    private String type;
    private String description;

    public Item(String name, int rarity, String type, String description) {
        this(UUID.randomUUID(), name, rarity, type, description);
    }

    public Item(UUID id, String name, int rarity, String type, String description) {
        if (id == null) {
            throw new IllegalArgumentException("Item id must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name must not be blank");
        }
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.type = type == null ? "" : type;
        this.description = description == null ? "" : description;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getRarity() {
        return rarity;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public void update(String name, int rarity, String type, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name must not be blank");
        }
        this.name = name;
        this.rarity = rarity;
        this.type = type == null ? "" : type;
        this.description = description == null ? "" : description;
    }

    @Override
    public String toString() {
        return name + (description.isBlank() ? "" : " - " + description);
    }
}
