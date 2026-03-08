package gmae.model;

import java.util.UUID;


public class Player {

    private final UUID id;
    private final PlayerProfile profile;
    private Realm realm;
    private int score;

    public Player(PlayerProfile profile) {
        if (profile == null) throw new IllegalArgumentException("Profile must not be null");
        this.id = UUID.randomUUID();
        this.profile = profile;
        this.score = 0;
    }

    public UUID getId() {
        return id;
    }

    public PlayerProfile getProfile() {
        return profile;
    }

    public int getScore() {
        return score;
    }

    public void move(String direction) {
        // move in realmMap
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    public Realm getPosition() {
        return realm;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void resetForAdventure() {
        this.score = 0;
        this.realm = null;
    }

    @Override
    public String toString() {
        return "Player[" + profile.getPlayerName() + " | score=" + score + "]";
    }
}
