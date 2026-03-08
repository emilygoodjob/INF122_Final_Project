package gmae.engine;


public class GameState {

    private boolean running;

    public GameState() {
        this.running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
