package gmae.menu;

import gmae.GMAE_APP;
import gmae.adventure.AdventureRegistry;
import gmae.adventure.MiniAdventure;
import gmae.model.Player;
import gmae.profile.ProfileManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MenuController {

    private final GMAE_APP app;
    private final ProfileManager profileManager;

    public MenuController(GMAE_APP app, ProfileManager profileManager) {
        this.app = app;
        this.profileManager = profileManager;
    }

    public Parent buildView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(28));

        // Title
        Label title = new Label("GuildQuest Mini-Adventure Environment");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        Label subtitle = new Label("Select an adventure and enter player names to begin");
        subtitle.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        VBox titleBox = new VBox(4, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);
        root.setTop(titleBox);
        BorderPane.setMargin(titleBox, new Insets(0, 0, 24, 0));

        // Adventure list
        Label adventureLabel = new Label("Available Mini-Adventures:");
        adventureLabel.setStyle("-fx-font-weight: bold;");

        ListView<MiniAdventure> adventureList = new ListView<>();
        adventureList.getItems().addAll(AdventureRegistry.getAll());
        adventureList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MiniAdventure item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + "\n  " + item.getDescription());
                    setStyle("-fx-font-size: 12; -fx-padding: 8;");
                }
            }
        });
        adventureList.setPrefHeight(200);
        if (!AdventureRegistry.getAll().isEmpty()) {
            adventureList.getSelectionModel().selectFirst();
        }

        VBox adventureBox = new VBox(6, adventureLabel, adventureList);
        HBox.setHgrow(adventureBox, Priority.ALWAYS);

        // Player inputs
        Label p1Label = new Label("Player 1 name:");
        TextField p1Field = new TextField("Player 1");
        p1Field.setPrefWidth(200);

        Label p2Label = new Label("Player 2 name:");
        TextField p2Field = new TextField("Player 2");
        p2Field.setPrefWidth(200);

        Label errorLabel = new Label(" ");
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11;");

        Button startBtn = new Button("Start Adventure");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 8 0;");

        startBtn.setOnAction(e -> {
            MiniAdventure selected = adventureList.getSelectionModel().getSelectedItem();
            String name1 = p1Field.getText().trim();
            String name2 = p2Field.getText().trim();
            if (selected == null) { errorLabel.setText("Please select a mini-adventure."); return; }
            if (name1.isEmpty() || name2.isEmpty()) { errorLabel.setText("Both player names are required."); return; }
            if (name1.equalsIgnoreCase(name2)) { errorLabel.setText("Players must have different names."); return; }
            Player p1 = new Player(profileManager.getOrCreate(name1));
            Player p2 = new Player(profileManager.getOrCreate(name2));
            app.showGame(p1, p2, selected);
        });

        VBox playersBox = new VBox(8, p1Label, p1Field, p2Label, p2Field, startBtn, errorLabel);
        playersBox.setPadding(new Insets(0, 0, 0, 24));
        playersBox.setPrefWidth(240);
        playersBox.setAlignment(Pos.TOP_LEFT);

        HBox centerRow = new HBox(adventureBox, playersBox);
        centerRow.setAlignment(Pos.TOP_LEFT);

        root.setCenter(centerRow);
        return root;
    }
}
