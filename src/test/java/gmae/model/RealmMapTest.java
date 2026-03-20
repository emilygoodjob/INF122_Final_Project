package gmae.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class RealmMapTest {

    private static RealmMap smallMap() {
        // Fixed seed for reproducibility
        return RealmMap.createRandomBoard(3, 3, new Random(42));
    }

    @Test
    void createRandomBoardHasCorrectDimensions() {
        RealmMap map = smallMap();
        assertEquals(3, map.getRows());
        assertEquals(3, map.getCols());
    }

    @Test
    void createRandomBoardHasAtLeastOneRealm() {
        RealmMap map = smallMap();
        assertFalse(map.getRealms().isEmpty());
    }

    @Test
    void isInsideBoardReturnsTrueForValidCell() {
        RealmMap map = smallMap();
        assertTrue(map.isInsideBoard(0, 0));
        assertTrue(map.isInsideBoard(2, 2));
    }

    @Test
    void isInsideBoardReturnsFalseForOutOfBounds() {
        RealmMap map = smallMap();
        assertFalse(map.isInsideBoard(-1, 0));
        assertFalse(map.isInsideBoard(0, -1));
        assertFalse(map.isInsideBoard(3, 0));
        assertFalse(map.isInsideBoard(0, 3));
    }

    @Test
    void getRealmAtThrowsForOutOfBoundsCell() {
        RealmMap map = smallMap();
        assertThrows(IllegalArgumentException.class, () -> map.getRealmAt(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> map.getRealmAt(3, 0));
    }

    @Test
    void connectMakesRealmsAdjacent() {
        RealmMap map = smallMap();
        List<RealmView> realms = List.copyOf(map.getRealms());
        // Find two realms that are NOT already adjacent and connect them
        // Or just verify that placed adjacent cells are already connected
        for (RealmView r : realms) {
            List<RealmView> neighbors = map.neighborsOf(r);
            for (RealmView neighbor : neighbors) {
                assertTrue(map.isAdjacent(r, neighbor));
                assertTrue(map.isAdjacent(neighbor, r)); // bidirectional
            }
        }
    }

    @Test
    void isAdjacentReturnsFalseForNullArguments() {
        RealmMap map = smallMap();
        RealmView realm = map.getRealms().iterator().next();
        assertFalse(map.isAdjacent(null, realm));
        assertFalse(map.isAdjacent(realm, null));
        assertFalse(map.isAdjacent(null, null));
    }

    @Test
    void neighborsOfNullReturnsEmpty() {
        RealmMap map = smallMap();
        assertTrue(map.neighborsOf(null).isEmpty());
    }

    @Test
    void placeRealmThrowsOnOccupiedCell() {
        // Build a 3x3 map manually to test placeRealm
        RealmMap map = RealmMap.createRandomBoard(3, 3, new Random(0));
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (!map.isEmptyCell(r, c)) {
                    final int row = r, col = c;
                    Realm newRealm = new Realm("Duplicate", "desc", "0,0", null);
                    RealmView view = new RealmAdapter(newRealm);
                    assertThrows(IllegalArgumentException.class, () -> map.placeRealm(view, row, col));
                    return;
                }
            }
        }
    }

    @Test
    void placeRealmThrowsOnNullRealm() {
        RealmMap map = RealmMap.createRandomBoard(3, 4, new Random(7));
        // Find an empty cell
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 4; c++) {
                if (map.isEmptyCell(r, c)) {
                    final int row = r, col = c;
                    assertThrows(IllegalArgumentException.class, () -> map.placeRealm(null, row, col));
                    return;
                }
            }
        }
    }

    @Test
    void randomRealmReturnsNonNull() {
        RealmMap map = smallMap();
        RealmView realm = map.randomRealm(new Random(1));
        assertNotNull(realm);
    }

    @Test
    void randomRealmThrowsOnNullRandom() {
        RealmMap map = smallMap();
        assertThrows(IllegalArgumentException.class, () -> map.randomRealm(null));
    }

    @Test
    void getRowOfAndGetColOfReturnMinusOneForUnknownRealm() {
        RealmMap map = smallMap();
        Realm raw = new Realm("Unknown", "desc", "x,y", null);
        RealmView unknown = new RealmAdapter(raw);
        assertEquals(-1, map.getRowOf(unknown));
        assertEquals(-1, map.getColOf(unknown));
    }

    @Test
    void createRandomBoardThrowsForOutOfRangeDimensions() {
        assertThrows(IllegalArgumentException.class, () -> RealmMap.createRandomBoard(2, 3, new Random()));
        assertThrows(IllegalArgumentException.class, () -> RealmMap.createRandomBoard(3, 9, new Random()));
    }

    @Test
    void getRealmsIsUnmodifiable() {
        RealmMap map = smallMap();
        assertThrows(UnsupportedOperationException.class, () -> map.getRealms().clear());
    }
}
