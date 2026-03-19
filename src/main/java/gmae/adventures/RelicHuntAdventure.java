package gmae.adventures;

import gmae.adventure.Action;
import gmae.adventure.ActionType;
import gmae.adventure.AdventureState;
import gmae.adventure.MiniAdventure;
import gmae.adventure.Result;
import gmae.model.Inventory;
import gmae.model.ItemAdapter;
import gmae.model.Player;
import gmae.model.RealmView;
import gmae.model.RealmMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RelicHuntAdventure extends MiniAdventure {

    private static final int DEFAULT_TARGET_RELICS = 5;
    private static final int DEFAULT_MAX_ROUNDS = 10;
    private static final double ITEM_DROP_CHANCE = 0.35;

    private final Random rng;
    private final int targetRelics;
    private final int maxRounds;
    private final Map<Player, Integer> relicCount = new LinkedHashMap<>();
    private final Set<Player> defendingPlayers = new LinkedHashSet<>();
    private final Map<Player, ActiveEffects> activeEffects = new LinkedHashMap<>();
    private final Map<RealmView, Integer> relicsInRealm = new LinkedHashMap<>();

    private Player player1;
    private Player player2;
    private int currentRound;
    private Result result;

    public RelicHuntAdventure() {
        this(RealmMap.createRandomSizedGrid(6, 8));
    }

    public RelicHuntAdventure(RealmMap realmMap) {
        this(realmMap, DEFAULT_TARGET_RELICS, DEFAULT_MAX_ROUNDS, new Random());
    }

    RelicHuntAdventure(RealmMap realmMap, int targetRelics, int maxRounds, Random rng) {
        super(realmMap);
        if (realmMap == null || realmMap.getRealms().isEmpty()) {
            throw new IllegalArgumentException("Relic Hunt requires a non-empty realm map");
        }
        if (targetRelics <= 0) {
            throw new IllegalArgumentException("Target relics must be positive");
        }
        if (maxRounds <= 0) {
            throw new IllegalArgumentException("Max rounds must be positive");
        }
        this.targetRelics = targetRelics;
        this.maxRounds = maxRounds;
        this.rng = rng == null ? new Random() : rng;
    }

    @Override
    public String getName() {
        return "Relic Hunt";
    }

    @Override
    public String getDescription() {
        return "Race across the realm map, collect scattered relics, and outplay your rival with defense and power-ups.";
    }

    @Override
    public void init(Player p1, Player p2) {
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("Two players are required for Relic Hunt");
        }

        reset();
        this.player1 = p1;
        this.player2 = p2;
        this.currentRound = 1;

        p1.getProfile().getInventory().clear();
        p2.getProfile().getInventory().clear();

        relicCount.put(p1, 0);
        relicCount.put(p2, 0);
        activeEffects.put(p1, new ActiveEffects());
        activeEffects.put(p2, new ActiveEffects());

        seedRelicsAcrossMap();
        assignStartingRealms();
    }

    @Override
    public List<ActionType> getValidActions(Player player) {
        List<ActionType> actions = new ArrayList<>();
        if (!realmMap.neighborsOf(player.getPosition()).isEmpty()) {
            actions.add(ActionType.MOVE);
        }
        actions.add(ActionType.DEFEND);
        if (!getPowerUps(player).isEmpty()) {
            actions.add(ActionType.USE_ITEM);
        }
        actions.add(ActionType.PASS);
        return List.copyOf(actions);
    }

    @Override
    public Action promptActionDetails(Player player, ActionType chosenType, Scanner scanner) {
        return switch (chosenType) {
            case MOVE -> promptMove(player, scanner);
            case USE_ITEM -> promptUseItem(player, scanner);
            default -> Action.of(chosenType);
        };
    }

    @Override
    public void applyAction(Player player, Action action) {
        if (result != null) {
            return;
        }

        switch (action.getType()) {
            case MOVE -> move(player, (RealmView) action.getParam("target"));
            case DEFEND -> defend(player);
            case USE_ITEM -> useItem(player, (UUID) action.getParam("itemId"));
            case PASS -> System.out.println(player.getProfile().getPlayerName() + " waits this turn.");
            default -> System.out.println("That action is not supported in Relic Hunt.");
        }

        updateResultIfNeeded();
    }

    @Override
    public void endRound() {
        if (result != null) {
            return;
        }
        defendingPlayers.clear();
        currentRound++;
        if (currentRound > maxRounds) {
            result = determineWinnerByRelicCount();
        }
    }

    @Override
    public boolean isFinished() {
        return result != null;
    }

    @Override
    public Result getResult() {
        if (result == null && currentRound > maxRounds) {
            result = determineWinnerByRelicCount();
        }
        return result;
    }

    @Override
    public void reset() {
        player1 = null;
        player2 = null;
        currentRound = 0;
        result = null;
        relicCount.clear();
        defendingPlayers.clear();
        activeEffects.clear();
        relicsInRealm.clear();
    }

    @Override
    public AdventureState getState() {
        int shownRound = currentRound <= 0 ? 1 : Math.min(currentRound, maxRounds);
        return new AdventureState(shownRound, maxRounds, isFinished(), buildStateDescription());
    }

    private Action promptMove(Player player, Scanner scanner) {
        List<RealmView> neighbors = realmMap.neighborsOf(player.getPosition());
        System.out.println("Choose a destination:");
        for (int i = 0; i < neighbors.size(); i++) {
            RealmView neighbor = neighbors.get(i);
            System.out.printf("  %d. %s (relics: %d)%n", i + 1, neighbor.getName(), relicsInRealm.getOrDefault(neighbor, 0));
        }
        int choice = readChoice(scanner, 1, neighbors.size(), "Destination");
        return Action.of(ActionType.MOVE, "target", neighbors.get(choice - 1));
    }

    private Action promptUseItem(Player player, Scanner scanner) {
        List<PowerUp> powerUps = getPowerUps(player);
        System.out.println("Choose an item to use:");
        for (int i = 0; i < powerUps.size(); i++) {
            PowerUp powerUp = powerUps.get(i);
            System.out.printf("  %d. %s - %s%n", i + 1, powerUp.getName(), powerUp.getDescription());
        }
        int choice = readChoice(scanner, 1, powerUps.size(), "Item");
        return Action.of(ActionType.USE_ITEM, "itemId", powerUps.get(choice - 1).getId());
    }

    private boolean move(Player player, RealmView destination) {
        RealmView current = player.getPosition();
        if (destination == null || current == null || !realmMap.isAdjacent(current, destination)) {
            System.out.println("Invalid move. Choose an adjacent realm.");
            return false;
        }

        Player opponent = opponentOf(player);
        if (opponent != null && destination.equals(opponent.getPosition()) && defendingPlayers.contains(opponent)) {
            System.out.println(opponent.getProfile().getPlayerName() + " is defending that realm. Move cancelled.");
            return false;
        }

        ActiveEffects effects = activeEffects.get(player);
        if (effects != null && effects.autoBlockRealm != null && !effects.autoBlockRealm.equals(destination)) {
            effects.autoBlockRealm = null;
        }

        player.setRealm(destination);
        System.out.println(player.getProfile().getPlayerName() + " moves to " + destination.getName() + ".");

        if (opponent != null && destination.equals(opponent.getPosition())) {
            return attemptSteal(player, opponent);
        }
        return collectRelics(player, destination);
    }

    private void defend(Player player) {
        defendingPlayers.add(player);
        System.out.println(player.getProfile().getPlayerName() + " braces for a relic steal.");
    }

    private boolean useItem(Player player, UUID itemId) {
        Inventory inventory = player.getProfile().getInventory();
        ItemAdapter item = inventory.findById(itemId);
        if (!(item instanceof PowerUp powerUp)) {
            System.out.println("That item is no longer available.");
            return false;
        }
        inventory.removeItem(itemId);
        powerUp.apply(this, player);
        System.out.println(player.getProfile().getPlayerName() + " used " + powerUp.getName() + ".");
        return true;
    }

    private boolean collectRelics(Player player, RealmView realm) {
        int available = relicsInRealm.getOrDefault(realm, 0);
        if (available <= 0) {
            System.out.println("No relics remain in this realm.");
            return false;
        }

        int amount = 1;
        ActiveEffects effects = activeEffects.get(player);
        if (effects != null && effects.doubleNextCollect) {
            amount = 2;
            effects.doubleNextCollect = false;
        }
        amount = Math.min(amount, available);

        relicsInRealm.put(realm, available - amount);
        addRelics(player, amount);
        System.out.println(player.getProfile().getPlayerName() + " collected " + amount + " relic(s).");
        maybeAwardItem(player);
        return true;
    }

    private boolean attemptSteal(Player player, Player opponent) {
        if (shouldBlockSteal(opponent)) {
            System.out.println(opponent.getProfile().getPlayerName() + " blocked the steal attempt.");
            return false;
        }
        if (relicCount.getOrDefault(opponent, 0) <= 0) {
            System.out.println(opponent.getProfile().getPlayerName() + " has no relics to steal.");
            return false;
        }

        addRelics(player, 1);
        removeRelics(opponent, 1);
        System.out.println(player.getProfile().getPlayerName() + " stole 1 relic from " + opponent.getProfile().getPlayerName() + ".");
        maybeAwardItem(player);
        return true;
    }

    private boolean shouldBlockSteal(Player opponent) {
        ActiveEffects opponentEffects = activeEffects.get(opponent);
        if (opponentEffects == null) {
            return false;
        }
        RealmView protectedRealm = opponentEffects.autoBlockRealm;
        if (protectedRealm != null && protectedRealm.equals(opponent.getPosition())) {
            opponentEffects.autoBlockRealm = null;
            return true;
        }
        return false;
    }

    private void maybeAwardItem(Player player) {
        if (rng.nextDouble() >= ITEM_DROP_CHANCE) {
            return;
        }
        PowerUp powerUp = createRandomPowerUp();
        player.getProfile().getInventory().addItem(powerUp);
        System.out.println(player.getProfile().getPlayerName() + " found a power-up: " + powerUp.getName() + ".");
    }

    private PowerUp createRandomPowerUp() {
        PowerUpType type = rng.nextBoolean() ? PowerUpType.DOUBLE_NEXT_COLLECT : PowerUpType.AUTO_BLOCK_STEAL_ONCE;
        return switch (type) {
            case DOUBLE_NEXT_COLLECT -> new PowerUp(
                    "Twin Relic Charm",
                    2,
                    type,
                    "Your next successful relic collection gains double relics.",
                    0
            );
            case AUTO_BLOCK_STEAL_ONCE -> new PowerUp(
                    "Ward Sigil",
                    1,
                    type,
                    "Blocks one steal attempt in your current realm.",
                    1
            );
        };
    }

    private void activateDoubleCollect(Player player) {
        activeEffects.get(player).doubleNextCollect = true;
    }

    private void activateAutoBlock(Player player) {
        activeEffects.get(player).autoBlockRealm = player.getPosition();
    }

    private void addRelics(Player player, int amount) {
        int updated = relicCount.getOrDefault(player, 0) + amount;
        relicCount.put(player, updated);
        player.setScore(updated);
    }

    private void removeRelics(Player player, int amount) {
        int updated = Math.max(0, relicCount.getOrDefault(player, 0) - amount);
        relicCount.put(player, updated);
        player.setScore(updated);
    }

    private void updateResultIfNeeded() {
        if (relicCount.getOrDefault(player1, 0) >= targetRelics) {
            result = Result.win(player1, player1.getProfile().getPlayerName() + " reached the relic target of " + targetRelics + ".");
        } else if (relicCount.getOrDefault(player2, 0) >= targetRelics) {
            result = Result.win(player2, player2.getProfile().getPlayerName() + " reached the relic target of " + targetRelics + ".");
        }
    }

    private Result determineWinnerByRelicCount() {
        int p1Relics = relicCount.getOrDefault(player1, 0);
        int p2Relics = relicCount.getOrDefault(player2, 0);
        if (p1Relics == p2Relics) {
            return Result.tie("Both hunters finished with " + p1Relics + " relics after " + maxRounds + " rounds.");
        }
        Player winner = p1Relics > p2Relics ? player1 : player2;
        int winnerRelics = Math.max(p1Relics, p2Relics);
        return Result.win(winner, winner.getProfile().getPlayerName() + " collected the most relics (" + winnerRelics + ") by round " + maxRounds + ".");
    }

    private void seedRelicsAcrossMap() {
        for (RealmView realm : realmMap.getRealms()) {
            relicsInRealm.put(realm, 1 + rng.nextInt(2));
        }
    }

    private void assignStartingRealms() {
        List<RealmView> realms = new ArrayList<>(realmMap.getRealms());
        realms.sort(Comparator.comparing(RealmView::getId));
        Collections.shuffle(realms, rng);
        player1.setRealm(realms.get(0));
        player2.setRealm(realms.size() > 1 ? realms.get(1) : realms.get(0));
    }

    private List<PowerUp> getPowerUps(Player player) {
        return player.getProfile().getInventory().getItemsOfType(PowerUp.class);
    }

    private Player opponentOf(Player player) {
        if (player == null) {
            return null;
        }
        return player.equals(player1) ? player2 : player1;
    }

    private String buildStateDescription() {
        if (player1 == null || player2 == null) {
            return "Adventure not initialized.";
        }

        String players = formatPlayer(player1) + System.lineSeparator() + formatPlayer(player2);
        String realms = realmMap.getRealms().stream()
                .sorted(Comparator.comparing(RealmView::getId))
                .map(this::formatRealmState)
                .collect(Collectors.joining(" | "));
        return "Target relics: " + targetRelics + System.lineSeparator()
                + players + System.lineSeparator()
                + "Map: " + realms;
    }

    private String formatPlayer(Player player) {
        String effects = formatEffects(player);
        return player.getProfile().getPlayerName()
                + " @ " + player.getPosition().getName()
                + " | relics=" + relicCount.getOrDefault(player, 0)
                + " | inventory=" + player.getProfile().getInventory().size()
                + " | status=" + effects;
    }

    private String formatEffects(Player player) {
        List<String> statuses = new ArrayList<>();
        if (defendingPlayers.contains(player)) {
            statuses.add("defending");
        }
        ActiveEffects effects = activeEffects.get(player);
        if (effects != null) {
            if (effects.doubleNextCollect) {
                statuses.add("double-next-collect");
            }
            if (effects.autoBlockRealm != null && effects.autoBlockRealm.equals(player.getPosition())) {
                statuses.add("auto-block-ready");
            }
        }
        if (statuses.isEmpty()) {
            return "none";
        }
        return String.join(", ", statuses);
    }

    private String formatRealmState(RealmView realm) {
        List<String> occupants = new ArrayList<>();
        if (realm.equals(player1.getPosition())) {
            occupants.add(player1.getProfile().getPlayerName());
        }
        if (realm.equals(player2.getPosition())) {
            occupants.add(player2.getProfile().getPlayerName());
        }

        String suffix = occupants.isEmpty() ? "" : " [" + String.join(", ", occupants) + "]";
        return realm.getId() + "=" + relicsInRealm.getOrDefault(realm, 0) + suffix;
    }

    public int getRelicsInRealm(RealmView realm) {
        return relicsInRealm.getOrDefault(realm, 0);
    }

    public int getTargetRelics() {
        return targetRelics;
    }

    public int getRelicCount(Player player) {
        return relicCount.getOrDefault(player, 0);
    }

    private int readChoice(Scanner scanner, int min, int max, String prompt) {
        while (true) {
            System.out.printf("%s (%d-%d): ", prompt, min, max);
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= min && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Invalid input. Please enter a number between " + min + " and " + max + ".");
        }
    }

    private enum PowerUpType {
        DOUBLE_NEXT_COLLECT,
        AUTO_BLOCK_STEAL_ONCE
    }

    private static final class ActiveEffects {
        private boolean doubleNextCollect;
        private RealmView autoBlockRealm;
    }

    private static final class PowerUp extends ItemAdapter {

        private final PowerUpType powerUpType;
        private final int durationTurns;

        private PowerUp(String name, int rarity, PowerUpType powerUpType, String description, int durationTurns) {
            super(name, rarity, "PowerUp", description);
            this.powerUpType = powerUpType;
            this.durationTurns = durationTurns;
        }

        public PowerUpType getPowerUpType() {
            return powerUpType;
        }

        public int getDurationTurns() {
            return durationTurns;
        }

        public void apply(RelicHuntAdventure adventure, Player player) {
            switch (powerUpType) {
                case DOUBLE_NEXT_COLLECT -> adventure.activateDoubleCollect(player);
                case AUTO_BLOCK_STEAL_ONCE -> adventure.activateAutoBlock(player);
            }
        }

        public boolean expiresAfterRound() {
            return durationTurns > 0;
        }
    }
}
