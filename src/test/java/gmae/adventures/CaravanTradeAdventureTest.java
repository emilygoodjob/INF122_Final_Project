package gmae.adventures;

import gmae.adventure.Action;
import gmae.adventure.ActionType;
import gmae.adventure.AdventureState;
import gmae.adventure.Result;
import gmae.model.Player;
import gmae.model.PlayerProfile;
import gmae.model.RealmMap;
import gmae.model.RealmView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CaravanTradeAdventureTest {

    private static final int STARTING_GOLD = 12;
    private static final int GOLD_TARGET = 30;
    private static final int MAX_ROUNDS = 3;

    private RealmMap map;
    private CaravanTradeAdventure adventure;
    private Player p1;
    private Player p2;

    @BeforeEach
    void setUp() {
        map = RealmMap.createRandomBoard(3, 3, new Random(42));
        adventure = new CaravanTradeAdventure(map, STARTING_GOLD, GOLD_TARGET, MAX_ROUNDS, new Random(0));
        p1 = new Player(new PlayerProfile("Alice"));
        p2 = new Player(new PlayerProfile("Bob"));
        adventure.init(p1, p2);
    }

    @Test
    void nameAndDescription() {
        assertEquals("Caravan Trade Run", adventure.getName());
        assertNotNull(adventure.getDescription());
        assertFalse(adventure.getDescription().isBlank());
    }

    @Test
    void playersStartWithConfiguredGold() {
        assertEquals(STARTING_GOLD, adventure.getGoldOf(p1));
        assertEquals(STARTING_GOLD, adventure.getGoldOf(p2));
    }

    @Test
    void notFinishedAfterInit() {
        assertFalse(adventure.isFinished());
    }

    @Test
    void resultIsNullBeforeGameEnds() {
        assertNull(adventure.getResult());
    }

    @Test
    void initialStateIsRoundOne() {
        AdventureState state = adventure.getState();
        assertEquals(1, state.getCurrentRound());
        assertEquals(MAX_ROUNDS, state.getMaxRounds());
        assertFalse(state.isFinished());
    }

    @Test
    void validActionsAlwaysContainPass() {
        assertTrue(adventure.getValidActions(p1).contains(ActionType.PASS));
        assertTrue(adventure.getValidActions(p2).contains(ActionType.PASS));
    }

    @Test
    void validActionsContainMoveWhenAdjacentRealmsExist() {
        List<RealmView> neighbors = map.neighborsOf(p1.getPosition());
        if (!neighbors.isEmpty()) {
            assertTrue(adventure.getValidActions(p1).contains(ActionType.MOVE));
        }
    }

    @Test
    void passActionDoesNotChangeGold() {
        int before = adventure.getGoldOf(p1);
        adventure.applyAction(p1, Action.of(ActionType.PASS));
        assertEquals(before, adventure.getGoldOf(p1));
    }

    @Test
    void moveActionMovesToAdjacentRealm() {
        List<RealmView> neighbors = map.neighborsOf(p1.getPosition());
        if (neighbors.isEmpty()) return;

        RealmView destination = neighbors.get(0);
        adventure.applyAction(p1, Action.of(ActionType.MOVE, "target", destination));
        assertEquals(destination, p1.getPosition());
    }

    @Test
    void moveToNonAdjacentRealmIsIgnored() {
        RealmView originalPosition = p1.getPosition();
        RealmView nonAdjacent = null;
        for (RealmView realm : map.getRealms()) {
            if (!realm.equals(originalPosition) && !map.isAdjacent(originalPosition, realm)) {
                nonAdjacent = realm;
                break;
            }
        }
        if (nonAdjacent == null) return;

        adventure.applyAction(p1, Action.of(ActionType.MOVE, "target", nonAdjacent));
        assertEquals(originalPosition, p1.getPosition());
    }

    @Test
    void buyGoodsDecreasesGold() {
        TradeList market = adventure.getMarketAt(p1.getPosition());
        if (market == null) return;

        String affordable = null;
        int price = 0;
        for (String item : market.getItemNames()) {
            int p = market.getBuyPrice(item);
            if (p > 0 && p <= adventure.getGoldOf(p1)) {
                affordable = item;
                price = p;
                break;
            }
        }
        if (affordable == null) return; // can't afford anything

        int before = adventure.getGoldOf(p1);
        adventure.applyAction(p1, Action.of(ActionType.BUY,
                java.util.Map.of("itemName", affordable, "quantity", 1)));
        assertEquals(before - price, adventure.getGoldOf(p1));
    }

    @Test
    void sellGoodsIncreasesGold() {
        // First buy a good, then sell it
        TradeList market = adventure.getMarketAt(p1.getPosition());
        if (market == null) return;

        String good = null;
        int buyPrice = 0;
        for (String item : market.getItemNames()) {
            int p = market.getBuyPrice(item);
            if (p > 0 && p <= adventure.getGoldOf(p1)) {
                good = item;
                buyPrice = p;
                break;
            }
        }
        if (good == null) return;

        adventure.applyAction(p1, Action.of(ActionType.BUY,
                java.util.Map.of("itemName", good, "quantity", 1)));

        int afterBuy = adventure.getGoldOf(p1);
        int sellPrice = market.getSellPrice(good);

        adventure.applyAction(p1, Action.of(ActionType.SELL,
                java.util.Map.of("itemName", good, "quantity", 1)));

        assertEquals(afterBuy + sellPrice, adventure.getGoldOf(p1));
    }

    @Test
    void gameFinishesAfterMaxRounds() {
        for (int i = 0; i < MAX_ROUNDS; i++) {
            adventure.applyAction(p1, Action.of(ActionType.PASS));
            adventure.applyAction(p2, Action.of(ActionType.PASS));
            adventure.endRound();
        }
        assertTrue(adventure.isFinished());
        assertNotNull(adventure.getResult());
    }

    @Test
    void tieWhenBothHaveEqualGoldAtRoundEnd() {
        for (int i = 0; i < MAX_ROUNDS; i++) {
            adventure.applyAction(p1, Action.of(ActionType.PASS));
            adventure.applyAction(p2, Action.of(ActionType.PASS));
            adventure.endRound();
        }
        Result result = adventure.getResult();
        assertNotNull(result);
        assertTrue(result.isTie());
    }

    @Test
    void goldTargetWinsImmediately() {
        CaravanTradeAdventure quickWin = new CaravanTradeAdventure(
                map, GOLD_TARGET, GOLD_TARGET, MAX_ROUNDS, new Random(1));
        Player a = new Player(new PlayerProfile("Winner"));
        Player b = new Player(new PlayerProfile("Loser"));
        quickWin.init(a, b);

        // Both start at goldTarget
        quickWin.applyAction(a, Action.of(ActionType.PASS));
        assertTrue(quickWin.isFinished());
        assertNotNull(quickWin.getResult());
    }

    @Test
    void initThrowsOnNullPlayer1() {
        CaravanTradeAdventure fresh = new CaravanTradeAdventure(map, STARTING_GOLD, GOLD_TARGET, MAX_ROUNDS, new Random(0));
        assertThrows(IllegalArgumentException.class, () -> fresh.init(null, p2));
    }

    @Test
    void initThrowsOnNullPlayer2() {
        CaravanTradeAdventure fresh = new CaravanTradeAdventure(map, STARTING_GOLD, GOLD_TARGET, MAX_ROUNDS, new Random(0));
        assertThrows(IllegalArgumentException.class, () -> fresh.init(p1, null));
    }

    @Test
    void constructorThrowsOnNegativeStartingGold() {
        assertThrows(IllegalArgumentException.class,
                () -> new CaravanTradeAdventure(map, -1, GOLD_TARGET, MAX_ROUNDS, new Random()));
    }

    @Test
    void constructorThrowsOnZeroGoldTarget() {
        assertThrows(IllegalArgumentException.class,
                () -> new CaravanTradeAdventure(map, STARTING_GOLD, 0, MAX_ROUNDS, new Random()));
    }

    @Test
    void constructorThrowsOnZeroMaxRounds() {
        assertThrows(IllegalArgumentException.class,
                () -> new CaravanTradeAdventure(map, STARTING_GOLD, GOLD_TARGET, 0, new Random()));
    }

    @Test
    void openOrdersAreGeneratedAfterInit() {
        assertFalse(adventure.getOpenOrdersList().isEmpty());
    }

    @Test
    void marketExistsForEachRealm() {
        for (RealmView realm : map.getRealms()) {
            TradeList market = adventure.getMarketAt(realm);
            assertNotNull(market, "Market should exist for realm: " + realm.getName());
        }
    }

    @Test
    void resetClearsState() {
        adventure.applyAction(p1, Action.of(ActionType.PASS));
        adventure.reset();
        assertNull(adventure.getResult());
        assertFalse(adventure.isFinished());
    }

    @Test
    void goldTargetGetter() {
        assertEquals(GOLD_TARGET, adventure.getGoldTarget());
    }
}
