package gmae.model;


public class PlayerProfile {

    private String playerName;
    private Inventory inventory;
    private int gamesPlayed;
    private int gamesWin;

    public PlayerProfile(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name must not be blank");
        }
        this.playerName = playerName;
        this.inventory = new Inventory();
        this.gamesPlayed = 0;
        this.gamesWin = 0;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getGamesWin() {
        return gamesWin;
    }

    public int getGamesLost() {
        return gamesPlayed - gamesWin;
    }

    public void incrementGamesPlayed() {
        gamesPlayed++;
    }

    public void incrementGamesWin() {
        gamesWin++;
    }

    @Override
    public String toString() {
        return String.format("Profile[%s | Played: %d | Wins: %d]",
                playerName, gamesPlayed, gamesWin);
    }
}
