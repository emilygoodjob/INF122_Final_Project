package gmae.demo;

import gmae.adventures.CaravanTradeAdventure;
import gmae.engine.GameSession;
import gmae.model.Player;
import gmae.model.PlayerProfile;

import java.util.Scanner;

public final class CaravanTradeConsoleDemo {

    private CaravanTradeConsoleDemo() {}

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Caravan Trade Run Console Demo");

            System.out.print("Player 1 name: ");
            String name1 = scanner.nextLine().trim();
            if (name1.isEmpty()) name1 = "Player 1";

            System.out.print("Player 2 name: ");
            String name2 = scanner.nextLine().trim();
            if (name2.isEmpty()) name2 = "Player 2";

            Player p1 = new Player(new PlayerProfile(name1));
            Player p2 = new Player(new PlayerProfile(name2));
            GameSession session = new GameSession(scanner);

            session.startSession(p1, p2, new CaravanTradeAdventure());
        }
    }
}
