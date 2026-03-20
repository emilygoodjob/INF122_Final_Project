package gmae.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerProfileTest {

    @Test
    void constructorSetsName() {
        PlayerProfile p = new PlayerProfile("Alice");
        assertEquals("Alice", p.getPlayerName());
    }

    @Test
    void constructorThrowsOnNullName() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerProfile(null));
    }

    @Test
    void constructorThrowsOnBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerProfile("   "));
    }

    @Test
    void initialStatsAreZero() {
        PlayerProfile p = new PlayerProfile("Bob");
        assertEquals(0, p.getGamesPlayed());
        assertEquals(0, p.getGamesWin());
        assertEquals(0, p.getGamesLost());
    }

    @Test
    void incrementGamesPlayedIncreasesCount() {
        PlayerProfile p = new PlayerProfile("Carol");
        p.incrementGamesPlayed();
        p.incrementGamesPlayed();
        assertEquals(2, p.getGamesPlayed());
    }

    @Test
    void incrementGamesWinIncreasesWins() {
        PlayerProfile p = new PlayerProfile("Dave");
        p.incrementGamesWin();
        assertEquals(1, p.getGamesWin());
    }

    @Test
    void gamesLostIsPlayedMinusWins() {
        PlayerProfile p = new PlayerProfile("Eve");
        p.incrementGamesPlayed();
        p.incrementGamesPlayed();
        p.incrementGamesPlayed();
        p.incrementGamesWin();
        assertEquals(2, p.getGamesLost());
    }

    @Test
    void inventoryStartsEmpty() {
        PlayerProfile p = new PlayerProfile("Frank");
        assertTrue(p.getInventory().isEmpty());
    }

    @Test
    void setPlayerNameUpdatesName() {
        PlayerProfile p = new PlayerProfile("Grace");
        p.setPlayerName("Heidi");
        assertEquals("Heidi", p.getPlayerName());
    }
}
