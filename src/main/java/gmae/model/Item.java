package gmae.model;

public class Item {
    private String itemId;
    private String name;
    private String itemType;
    private String description;
    private String rarity;

    public Item(String itemId, String name, String itemType, String description, String rarity) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
    }

    // ==================================
    // Getters and setters for the fields
    // ==================================

    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRarity() {
        return rarity;
    }

    public String getItemType() {
        return itemType;
    }
}
