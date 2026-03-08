package gmae.engine;

import gmae.adventure.Action;
import gmae.adventure.ActionType;
import gmae.adventure.MiniAdventure;
import gmae.adventure.Result;
import gmae.model.Player;

import java.util.List;
import java.util.Scanner;


public class GameSession {

    private Player player1;
    private Player player2;
    private MiniAdventure currentAdventure;
    private final GameState gameState;
    private final Scanner scanner;

    public GameSession(Scanner scanner) {
        this.gameState = new GameState();
        this.scanner = scanner;
    }

    public void startSession(Player p1, Player p2, MiniAdventure adventure) {
        this.player1 = p1;
        this.player2 = p2;
        this.currentAdventure = adventure;

        p1.resetForAdventure();
        p2.resetForAdventure();
        adventure.init(p1, p2);
        gameState.setRunning(true);

        printHeader("Starting: " + adventure.getName());
        System.out.println("Player 1: " + p1.getProfile().getPlayerName());
        System.out.println("Player 2: " + p2.getProfile().getPlayerName());
        System.out.println(adventure.getDescription());

        runGameLoop();
    }
    public void endSession() {
        gameState.setRunning(false);
    }
    public GameState getGameState() {
        return gameState;
    }


    private void runGameLoop() {
        while (gameState.isRunning() && !currentAdventure.isFinished()) {
            System.out.println("\n" + currentAdventure.getState());
            System.out.println("-".repeat(50));

            takeTurn(player1);
            if (currentAdventure.isFinished()) {
                break;
            }
            takeTurn(player2);
            if (currentAdventure.isFinished()) {
                break;
            }

            currentAdventure.endRound();
        }
        concludeSession();
    }

    private void takeTurn(Player player) {
        System.out.println("\n" + player.getProfile().getPlayerName() + "'s turn");
        List<ActionType> validActions = currentAdventure.getValidActions(player);
        System.out.println("Available actions:");
        for (int i = 0; i < validActions.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, validActions.get(i));
        }

        int choice = readIntInRange(1, validActions.size(), "Enter choice");
        ActionType chosenType = validActions.get(choice - 1);
        Action action = currentAdventure.promptActionDetails(player, chosenType, scanner);
        currentAdventure.applyAction(player, action);
    }

    private void concludeSession() {
        gameState.setRunning(false);
        printHeader("Game Over");

        if (currentAdventure.isFinished()) {
            Result result = currentAdventure.getResult();
            System.out.println(result);
            updateProfiles(result);
        }
    }

    private void updateProfiles(Result result) {
        player1.getProfile().incrementGamesPlayed();
        player2.getProfile().incrementGamesPlayed();
        if (!result.isTie() && result.getWinner() != null) {
            result.getWinner().getProfile().incrementGamesWin();
        }
    }


    
    public int readIntInRange(int min, int max, String prompt) {
        while (true) {
            System.out.printf("%s (%d-%d): ", prompt, min, max);
            try {
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val >= min && val <= max) return val;
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid input. Please enter a number between " + min + " and " + max + ".");
        }
    }

    public String readLine(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("Input cannot be empty.");
        }
    }
    private void printHeader(String title) {
        System.out.println();
        System.out.println("=".repeat(50));
        System.out.println("  " + title);
        System.out.println("=".repeat(50));
    }
}
