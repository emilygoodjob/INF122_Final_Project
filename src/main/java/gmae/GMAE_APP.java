package gmae;

import gmae.menu.MenuController;
import gmae.profile.ProfileManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class GMAE_APP extends Application {

    private MenuController menuController;
    private ProfileManager profileManager;

    @Override
    public void start(Stage primaryStage) {
        initialize();

        // GUI to be expanded later
        StackPane root = new StackPane(new Label("GMAE ..."));
        primaryStage.setScene(new Scene(root, 900, 650));
        primaryStage.setTitle("GMAE - GuildQuest Mini-Adventure Environment");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public void initialize() {
        profileManager = new ProfileManager();
        profileManager.loadProfiles();

        // TODO: CaravanTradeAdventure
        // TODO: RelicHuntAdventure

        menuController = new MenuController();
    }

    @Override
    public void stop() {
        if (profileManager != null) {
            profileManager.saveProfiles();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
