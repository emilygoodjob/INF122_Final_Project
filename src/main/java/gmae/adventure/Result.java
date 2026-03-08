package gmae.adventure;

import gmae.model.Player;


public class Result {

    private final boolean isTie;
    private final Player winner;   // null when isTie == true
    private final String summary;

    private Result(boolean isTie, Player winner, String summary) {
        this.isTie = isTie;
        this.winner = winner;
        this.summary = summary;
    }

    public static Result win(Player winner, String summary) {
        if (winner == null) throw new IllegalArgumentException("Winner must not be null");
        return new Result(false, winner, summary);
    }

    public static Result tie(String summary) {
        return new Result(true, null, summary);
    }

    public boolean isTie() {
        return isTie;
    }

    public Player getWinner() {
        return winner;
    }

    public String getSummary() {
        return summary;
    }

    @Override
    public String toString() {
        if (isTie) {
            return "Tie — " + summary;
        }
        return "Winner: " + winner.getProfile().getPlayerName() + " — " + summary;
    }
}
