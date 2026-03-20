package gmae.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private PlayerProfile profile;
    private Player player;

    @BeforeEach
    void setUp() {
        profile = new PlayerProfile("Alice");
        player = new Player(profile);
    }

    @Test
    void constructorThrowsOnNullProfile() {
        assertThrows(IllegalArgumentException.class, () -> new Player(null));
    }

    @Test
    void initialScoreIsZero() {
        assertEquals(0, player.getScore());
    }

    @Test
    void initialPositionIsNull() {
        assertNull(player.getPosition());
    }

    @Test
    void getProfileReturnsProfile() {
        assertSame(profile, player.getProfile());
    }

    @Test
    void addScoreAccumulates() {
        player.addScore(5);
        player.addScore(3);
        assertEquals(8, player.getScore());
    }

    @Test
    void setScoreOverwritesScore() {
        player.addScore(10);
        player.setScore(2);
        assertEquals(2, player.getScore());
    }

    @Test
    void setRealmUpdatesPosition() {
        Realm realm = new Realm("Test Realm", "desc", "0,0", null);
        RealmView view = new RealmAdapter(realm);
        player.setRealm(view);
        assertEquals(view, player.getPosition());
    }

    @Test
    void resetForAdventureClearsScoreAndRealm() {
        Realm realm = new Realm("Test Realm", "desc", "0,0", null);
        RealmView view = new RealmAdapter(realm);
        player.setRealm(view);
        player.addScore(99);

        player.resetForAdventure();

        assertEquals(0, player.getScore());
        assertNull(player.getPosition());
    }

    @Test
    void idIsNotNull() {
        assertNotNull(player.getId());
    }

    @Test
    void twoPlayersHaveDifferentIds() {
        Player other = new Player(new PlayerProfile("Bob"));
        assertNotEquals(player.getId(), other.getId());
    }
}
