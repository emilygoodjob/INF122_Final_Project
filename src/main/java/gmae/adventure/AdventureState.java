package gmae.adventure;

// Used by GameSession to display progress to players between turns.
public class AdventureState {

    private final int currentRound;
    private final int maxRounds;   // -1 means unlimited
    private final boolean finished;
    private final String description;

    public AdventureState(int currentRound, int maxRounds, boolean finished, String description) {
        this.currentRound = currentRound;
        this.maxRounds = maxRounds;
        this.finished = finished;
        this.description = description;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public boolean isFinished() {
        return finished;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        String roundInfo;
        if (maxRounds > 0) {
            roundInfo = "Round " + currentRound + "/" + maxRounds;
        } else {
            roundInfo = "Round " + currentRound;
        }

        String result = roundInfo + " | " + description;
        if (finished) {
            result = result + " [FINISHED]";
        }
        return result;
    }
}
