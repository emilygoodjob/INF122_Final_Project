package gmae.adventures;

import gmae.model.RealmView;

import java.util.Objects;
import java.util.UUID;

public class TradeOrder {

    private final UUID id;
    private final String itemName;
    private final int quantity;
    private final RealmView destination;
    private final int rewardGold;
    private int turnsRemaining;
    private boolean completed;

    public TradeOrder(String itemName, int quantity, RealmView destination, int rewardGold, int turnsRemaining) {
        this(UUID.randomUUID(), itemName, quantity, destination, rewardGold, turnsRemaining, false);
    }

    public TradeOrder(UUID id,
                      String itemName,
                      int quantity,
                      RealmView destination,
                      int rewardGold,
                      int turnsRemaining,
                      boolean completed) {
        if (id == null) {
            throw new IllegalArgumentException("Order id must not be null");
        }
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("Item name must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination realm must not be null");
        }
        if (rewardGold <= 0) {
            throw new IllegalArgumentException("Reward gold must be positive");
        }
        if (turnsRemaining <= 0) {
            throw new IllegalArgumentException("Turns remaining must be positive");
        }

        this.id = id;
        this.itemName = itemName;
        this.quantity = quantity;
        this.destination = destination;
        this.rewardGold = rewardGold;
        this.turnsRemaining = turnsRemaining;
        this.completed = completed;
    }

    public UUID getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public RealmView getDestination() {
        return destination;
    }

    public int getRewardGold() {
        return rewardGold;
    }

    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void tick() {
        if (!completed && turnsRemaining > 0) {
            turnsRemaining--;
        }
    }

    public void markCompleted() {
        completed = true;
    }

    public boolean isExpired() {
        return !completed && turnsRemaining <= 0;
    }

    public String summary() {
        return itemName + " x" + quantity
                + " -> " + destination.getName()
                + " | reward=" + rewardGold
                + "g | turns left=" + turnsRemaining;
    }

    @Override
    public String toString() {
        return summary();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TradeOrder that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
