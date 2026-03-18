package gmae.adventure;

import gmae.model.Player;
import gmae.model.RealmMap;

import java.util.List;
import java.util.Scanner;

// Abstract base class that every mini-adventure must extend.
public abstract class MiniAdventure {
    protected RealmMap realmMap;
    protected MiniAdventure(RealmMap realmMap) {
        this.realmMap = realmMap;
    }
    public abstract String getName();
    public abstract String getDescription();
    public abstract void init(Player p1, Player p2);
    public abstract List<ActionType> getValidActions(Player player);
    public abstract Action promptActionDetails(Player player, ActionType chosenType, Scanner scanner);
    public abstract void applyAction(Player player, Action action);
    public abstract void endRound();
    public abstract boolean isFinished();
    public abstract Result getResult();
    public abstract void reset();
    public abstract AdventureState getState();

    public Action promptActionGUI(Player player, ActionType type) {
        return Action.of(type);
    }

    public RealmMap getRealmMap() {
        return realmMap;
    }
}
