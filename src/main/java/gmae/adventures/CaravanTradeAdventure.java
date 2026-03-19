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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

public class CaravanTradeAdventure extends MiniAdventure {

    // Caravan Trade Run:
    // Two players travel between realms, buy and sell goods,
    // and complete trade orders to earn gold.
    // The winner is the first player to reach the gold target,
    // or the player with the most gold after the max round limit.

    private static final int DEFAULT_STARTING_GOLD = 12;
    private static final int DEFAULT_GOLD_TARGET = 30;
    private static final int DEFAULT_MAX_ROUNDS = 10;
    private static final int DEFAULT_ORDER_COUNT = 3;

    private static final List<String> GOODS = List.of(
            "Spice", "Silk", "Ore", "Tea"
    );

    private final Random rng;
    private final int startingGold;
    private final int goldTarget;
    private final int maxRounds;

    private final Map<Player, Integer> gold = new LinkedHashMap<>();
    private final Map<RealmView, TradeList> marketByRealm = new LinkedHashMap<>();
    private final List<TradeOrder> openOrders = new ArrayList<>();

    private Player player1;
    private Player player2;
    private int currentRound;
    private Result result;

    public CaravanTradeAdventure() {
        this(RealmMap.createRandomSizedGrid(6, 8));
    }

    public CaravanTradeAdventure(RealmMap realmMap) {
        this(realmMap, DEFAULT_STARTING_GOLD, DEFAULT_GOLD_TARGET, DEFAULT_MAX_ROUNDS, new Random());
    }

    CaravanTradeAdventure(RealmMap realmMap, int startingGold, int goldTarget, int maxRounds, Random rng) {
        super(realmMap);
        if (realmMap == null || realmMap.getRealms().isEmpty()) {
            throw new IllegalArgumentException("Caravan Trade Run requires a non-empty realm map");
        }
        if (startingGold < 0) {
            throw new IllegalArgumentException("Starting gold must not be negative");
        }
        if (goldTarget <= 0) {
            throw new IllegalArgumentException("Gold target must be positive");
        }
        if (maxRounds <= 0) {
            throw new IllegalArgumentException("Max rounds must be positive");
        }
        this.startingGold = startingGold;
        this.goldTarget = goldTarget;
        this.maxRounds = maxRounds;
        this.rng = rng == null ? new Random() : rng;
    }

    @Override
    public String getName() {
        return "Caravan Trade Run";
    }

    @Override
    public String getDescription() {
        return "Travel between realms, buy and sell goods, and deliver trade orders to build the richest caravan.";
    }

    @Override
    public void init(Player p1, Player p2) {
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("Two players are required for Caravan Trade Run");
        }

        reset();
        this.player1 = p1;
        this.player2 = p2;
        this.currentRound = 1;

        p1.getProfile().getInventory().clear();
        p2.getProfile().getInventory().clear();

        gold.put(p1, startingGold);
        gold.put(p2, startingGold);
        p1.setScore(startingGold);
        p2.setScore(startingGold);

        buildMarkets();
        assignStartingRealms();
        replenishOrders();
    }

    @Override
    public List<ActionType> getValidActions(Player player) {
        List<ActionType> actions = new ArrayList<>();

        if (!realmMap.neighborsOf(player.getPosition()).isEmpty()) {
            actions.add(ActionType.MOVE);
        }
        if (hasAffordableGoods(player)) {
            actions.add(ActionType.BUY);
        }
        if (hasSellableGoods(player)) {
            actions.add(ActionType.SELL);
        }
        if (!fulfillableOrdersFor(player).isEmpty()) {
            actions.add(ActionType.TRADE);
        }
        actions.add(ActionType.PASS);

        return List.copyOf(actions);
    }

    @Override
    public Action promptActionDetails(Player player, ActionType chosenType, Scanner scanner) {
        return switch (chosenType) {
            case MOVE -> promptMove(player, scanner);
            case BUY -> promptBuy(player, scanner);
            case SELL -> promptSell(player, scanner);
            case TRADE -> promptDeliver(player, scanner);
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
            case BUY -> buyGoods(
                    player,
                    (String) action.getParam("itemName"),
                    (Integer) action.getParam("quantity")
            );
            case SELL -> sellGoods(
                    player,
                    (String) action.getParam("itemName"),
                    (Integer) action.getParam("quantity")
            );
            case TRADE -> deliverOrder(player, (UUID) action.getParam("orderId"));
            case PASS -> System.out.println(player.getProfile().getPlayerName() + " passes this turn.");
            default -> System.out.println("Unsupported action for Caravan Trade Run.");
        }

        updateResultIfNeeded();
    }

    @Override
    public void endRound() {
        if (result != null) {
            return;
        }

        expireOrdersAndRefill();

        if (currentRound >= maxRounds) {
            result = determineWinnerByGold();
        } else {
            currentRound++;
        }
    }

    @Override
    public boolean isFinished() {
        return result != null;
    }

    @Override
    public Result getResult() {
        if (result == null && currentRound >= maxRounds) {
            result = determineWinnerByGold();
        }
        return result;
    }

    @Override
    public void reset() {
        player1 = null;
        player2 = null;
        currentRound = 0;
        result = null;
        gold.clear();
        marketByRealm.clear();
        openOrders.clear();
    }

    @Override
    public AdventureState getState() {
        int shownRound = currentRound <= 0 ? 1 : Math.min(currentRound, maxRounds);
        return new AdventureState(shownRound, maxRounds, isFinished(), buildStateDescription());
    }

    // Initialize players, markets, orders, and starting gold.
    private void buildMarkets() {
        List<RealmView> realms = new ArrayList<>(realmMap.getRealms());
        realms.sort(Comparator.comparing(RealmView::getId));

        for (int realmIndex = 0; realmIndex < realms.size(); realmIndex++) {
            RealmView realm = realms.get(realmIndex);
            Map<String, Integer> buyPrices = new LinkedHashMap<>();
            Map<String, Integer> sellPrices = new LinkedHashMap<>();

            for (int goodIndex = 0; goodIndex < GOODS.size(); goodIndex++) {
                String good = GOODS.get(goodIndex);
                int buyPrice = 3 + ((realmIndex + goodIndex * 2) % 6);
                int sellPrice = Math.max(1, buyPrice - 1);
                buyPrices.put(good, buyPrice);
                sellPrices.put(good, sellPrice);
            }

            marketByRealm.put(realm, new TradeList(realm.getId(), buyPrices, sellPrices));
        }
    }

    private void assignStartingRealms() {
        List<RealmView> realms = new ArrayList<>(realmMap.getRealms());
        realms.sort(Comparator.comparing(RealmView::getId));
        Collections.shuffle(realms, rng);

        player1.setRealm(realms.get(0));
        player2.setRealm(realms.size() > 1 ? realms.get(1) : realms.get(0));
    }

    private void replenishOrders() {
        List<RealmView> realms = new ArrayList<>(realmMap.getRealms());
        realms.sort(Comparator.comparing(RealmView::getId));

        while (openOrders.size() < DEFAULT_ORDER_COUNT) {
            String itemName = GOODS.get(rng.nextInt(GOODS.size()));
            int quantity = 1 + rng.nextInt(2);
            RealmView destination = realms.get(rng.nextInt(realms.size()));
            int rewardGold = 6 + quantity + rng.nextInt(5);
            int turnsRemaining = 3 + rng.nextInt(3);

            openOrders.add(new TradeOrder(itemName, quantity, destination, rewardGold, turnsRemaining));
        }
    }

    // Return the list of actions currently available to the player.
    private Action promptMove(Player player, Scanner scanner) {
        List<RealmView> neighbors = realmMap.neighborsOf(player.getPosition());
        System.out.println("Choose a destination:");
        for (int i = 0; i < neighbors.size(); i++) {
            RealmView realm = neighbors.get(i);
            System.out.printf("  %d. %s%n", i + 1, realm.getName());
        }
        int choice = readChoice(scanner, 1, neighbors.size(), "Destination");
        return Action.of(ActionType.MOVE, "target", neighbors.get(choice - 1));
    }

    private Action promptBuy(Player player, Scanner scanner) {
        TradeList market = marketByRealm.get(player.getPosition());
        List<String> goods = new ArrayList<>(market.getItemNames());
        goods.sort(String::compareTo);

        System.out.println("Choose a good to buy:");
        for (int i = 0; i < goods.size(); i++) {
            String itemName = goods.get(i);
            System.out.printf("  %d. %s (%dg)%n", i + 1, itemName, market.getBuyPrice(itemName));
        }

        int itemChoice = readChoice(scanner, 1, goods.size(), "Good");
        String itemName = goods.get(itemChoice - 1);
        int maxAffordable = Math.max(1, getGold(player) / market.getBuyPrice(itemName));
        int cappedMax = Math.min(3, maxAffordable);

        int quantity = readChoice(scanner, 1, cappedMax, "Quantity");
        return Action.of(ActionType.BUY, Map.of(
                "itemName", itemName,
                "quantity", quantity
        ));
    }

    private Action promptSell(Player player, Scanner scanner) {
        Map<String, Long> ownedGoods = countGoodsByName(player);
        List<String> goods = ownedGoods.keySet().stream().sorted().toList();
        TradeList market = marketByRealm.get(player.getPosition());

        System.out.println("Choose a good to sell:");
        for (int i = 0; i < goods.size(); i++) {
            String itemName = goods.get(i);
            System.out.printf("  %d. %s (you have %d, sell price %dg)%n",
                    i + 1,
                    itemName,
                    ownedGoods.get(itemName),
                    market.getSellPrice(itemName));
        }

        int itemChoice = readChoice(scanner, 1, goods.size(), "Good");
        String itemName = goods.get(itemChoice - 1);
        int maxOwned = ownedGoods.get(itemName).intValue();
        int quantity = readChoice(scanner, 1, maxOwned, "Quantity");

        return Action.of(ActionType.SELL, Map.of(
                "itemName", itemName,
                "quantity", quantity
        ));
    }

    private Action promptDeliver(Player player, Scanner scanner) {
        List<TradeOrder> deliverable = fulfillableOrdersFor(player);

        System.out.println("Choose an order to deliver:");
        for (int i = 0; i < deliverable.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, deliverable.get(i).summary());
        }

        int choice = readChoice(scanner, 1, deliverable.size(), "Order");
        return Action.of(ActionType.TRADE, "orderId", deliverable.get(choice - 1).getId());
    }

    // Apply the selected action (move, buy, sell, deliver, or pass).
    private boolean move(Player player, RealmView destination) {
        RealmView current = player.getPosition();
        if (destination == null || current == null || !realmMap.isAdjacent(current, destination)) {
            System.out.println("Invalid move. Choose an adjacent realm.");
            return false;
        }

        player.setRealm(destination);
        System.out.println(player.getProfile().getPlayerName() + " travels to " + destination.getName() + ".");
        return true;
    }

    private boolean buyGoods(Player player, String itemName, int quantity) {
        if (itemName == null || quantity <= 0) {
            System.out.println("Invalid purchase.");
            return false;
        }

        TradeList market = marketByRealm.get(player.getPosition());
        int unitPrice = market.getBuyPrice(itemName);
        if (unitPrice <= 0) {
            System.out.println("That good is not sold here.");
            return false;
        }

        int totalCost = unitPrice * quantity;
        if (getGold(player) < totalCost) {
            System.out.println("Not enough gold.");
            return false;
        }

        Inventory inventory = player.getProfile().getInventory();
        for (int i = 0; i < quantity; i++) {
            inventory.addItem(new Goods(itemName, player.getPosition().getName()));
        }

        adjustGold(player, -totalCost);
        System.out.println(player.getProfile().getPlayerName()
                + " bought " + quantity + " " + itemName
                + " for " + totalCost + " gold.");
        return true;
    }

    private boolean sellGoods(Player player, String itemName, int quantity) {
        if (itemName == null || quantity <= 0) {
            System.out.println("Invalid sale.");
            return false;
        }

        List<Goods> goods = getGoodsByName(player, itemName);
        if (goods.size() < quantity) {
            System.out.println("You do not have enough " + itemName + " to sell.");
            return false;
        }

        TradeList market = marketByRealm.get(player.getPosition());
        int unitPrice = market.getSellPrice(itemName);
        if (unitPrice <= 0) {
            System.out.println("This market is not buying that good.");
            return false;
        }

        Inventory inventory = player.getProfile().getInventory();
        for (int i = 0; i < quantity; i++) {
            inventory.removeItem(goods.get(i).getId());
        }

        int revenue = unitPrice * quantity;
        adjustGold(player, revenue);
        System.out.println(player.getProfile().getPlayerName()
                + " sold " + quantity + " " + itemName
                + " for " + revenue + " gold.");
        return true;
    }

    private boolean deliverOrder(Player player, UUID orderId) {
        TradeOrder order = findOrder(orderId);
        if (order == null || order.isCompleted() || order.isExpired()) {
            System.out.println("That order is no longer available.");
            return false;
        }
        if (!player.getPosition().equals(order.getDestination())) {
            System.out.println("You must be in " + order.getDestination().getName() + " to deliver this order.");
            return false;
        }

        List<Goods> goods = getGoodsByName(player, order.getItemName());
        if (goods.size() < order.getQuantity()) {
            System.out.println("You do not have enough " + order.getItemName() + " to complete this order.");
            return false;
        }

        Inventory inventory = player.getProfile().getInventory();
        for (int i = 0; i < order.getQuantity(); i++) {
            inventory.removeItem(goods.get(i).getId());
        }

        order.markCompleted();
        openOrders.remove(order);
        adjustGold(player, order.getRewardGold());

        System.out.println(player.getProfile().getPlayerName()
                + " delivered " + order.getItemName()
                + " x" + order.getQuantity()
                + " and earned " + order.getRewardGold() + " gold.");
        return true;
    }

    // Advance round state, expire orders, and check win condition.
    private void expireOrdersAndRefill() {
        for (TradeOrder order : openOrders) {
            order.tick();
        }

        openOrders.removeIf(order -> order.isCompleted() || order.isExpired());
        replenishOrders();
    }

    private void updateResultIfNeeded() {
        if (getGold(player1) >= goldTarget && getGold(player2) >= goldTarget) {
            if (getGold(player1) == getGold(player2)) {
                result = Result.tie("Both caravans reached the gold target with " + getGold(player1) + " gold.");
            } else {
                result = determineWinnerByGold();
            }
            return;
        }

        if (getGold(player1) >= goldTarget) {
            result = Result.win(player1,
                    player1.getProfile().getPlayerName() + " reached the gold target of " + goldTarget + ".");
        } else if (getGold(player2) >= goldTarget) {
            result = Result.win(player2,
                    player2.getProfile().getPlayerName() + " reached the gold target of " + goldTarget + ".");
        }
    }

    private Result determineWinnerByGold() {
        int p1Gold = getGold(player1);
        int p2Gold = getGold(player2);

        if (p1Gold == p2Gold) {
            return Result.tie("Both caravans finished with " + p1Gold + " gold after " + maxRounds + " rounds.");
        }

        Player winner = p1Gold > p2Gold ? player1 : player2;
        int winningGold = Math.max(p1Gold, p2Gold);
        return Result.win(winner,
                winner.getProfile().getPlayerName() + " finished with the most gold (" + winningGold + ").");
    }

    private boolean hasAffordableGoods(Player player) {
        TradeList market = marketByRealm.get(player.getPosition());
        if (market == null) {
            return false;
        }
        for (String itemName : market.getItemNames()) {
            int price = market.getBuyPrice(itemName);
            if (price > 0 && getGold(player) >= price) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSellableGoods(Player player) {
        return !countGoodsByName(player).isEmpty();
    }

    private List<TradeOrder> fulfillableOrdersFor(Player player) {
        RealmView currentRealm = player.getPosition();
        Map<String, Long> goods = countGoodsByName(player);

        return openOrders.stream()
                .filter(order -> !order.isCompleted())
                .filter(order -> !order.isExpired())
                .filter(order -> order.getDestination().equals(currentRealm))
                .filter(order -> goods.getOrDefault(order.getItemName(), 0L) >= order.getQuantity())
                .toList();
    }

    private TradeOrder findOrder(UUID orderId) {
        if (orderId == null) {
            return null;
        }
        for (TradeOrder order : openOrders) {
            if (order.getId().equals(orderId)) {
                return order;
            }
        }
        return null;
    }

    private int getGold(Player player) {
        return gold.getOrDefault(player, 0);
    }

    private void adjustGold(Player player, int delta) {
        int updated = getGold(player) + delta;
        gold.put(player, updated);
        player.setScore(updated);
    }

    private Map<String, Long> countGoodsByName(Player player) {
        return player.getProfile().getInventory().getItems().stream()
                .filter(item -> item instanceof Goods)
                .map(ItemAdapter::getName)
                .collect(Collectors.groupingBy(
                        name -> name,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    private List<Goods> getGoodsByName(Player player, String itemName) {
        return player.getProfile().getInventory().getItems().stream()
                .filter(item -> item instanceof Goods)
                .map(item -> (Goods) item)
                .filter(item -> item.getName().equals(itemName))
                .toList();
    }

    // Build a readable summary of player gold, positions, and active orders.
    private String buildStateDescription() {
        if (player1 == null || player2 == null) {
            return "Adventure not initialized.";
        }

        return "Gold target: " + goldTarget + System.lineSeparator()
                + formatPlayer(player1) + System.lineSeparator()
                + formatPlayer(player2) + System.lineSeparator()
                + "Open orders: " + formatOrders();
    }

    private String formatPlayer(Player player) {
        RealmView realm = player.getPosition();
        TradeList market = marketByRealm.get(realm);

        return player.getProfile().getPlayerName()
                + " @ " + realm.getName()
                + " | gold=" + getGold(player)
                + " | goods=" + formatInventory(player)
                + " | market=" + (market == null ? "none" : market.describeMarket());
    }

    private String formatInventory(Player player) {
        Map<String, Long> counts = countGoodsByName(player);
        if (counts.isEmpty()) {
            return "empty";
        }
        return counts.entrySet().stream()
                .map(entry -> entry.getKey() + " x" + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    private String formatOrders() {
        if (openOrders.isEmpty()) {
            return "none";
        }
        return openOrders.stream()
                .map(TradeOrder::summary)
                .collect(Collectors.joining(" || "));
    }

    public TradeList getMarketAt(RealmView realm) {
        return marketByRealm.get(realm);
    }

    public int getGoldOf(Player player) {
        return gold.getOrDefault(player, 0);
    }

    public int getGoldTarget() {
        return goldTarget;
    }

    public List<TradeOrder> getOpenOrdersList() {
        return Collections.unmodifiableList(openOrders);
    }

    public List<TradeOrder> getFulfillableOrdersForPlayer(Player player) {
        return fulfillableOrdersFor(player);
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

    private static final class Goods extends ItemAdapter {
        private Goods(String name, String originRealmName) {
            super(
                    name,
                    1,
                    "TradeGood",
                    "Bought in " + originRealmName + " for caravan trade."
            );
        }
    }
}
