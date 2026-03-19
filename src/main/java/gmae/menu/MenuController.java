package gmae.menu;

import gmae.GMAE_APP;
import gmae.adventure.AdventureRegistry;
import gmae.adventure.MiniAdventure;
import gmae.model.Player;
import gmae.model.PlayerProfile;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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

        // Bottom: adventurer roster
        VBox rosterBox = buildRosterBox(p1Field, p2Field);
        rosterBox.setPadding(new Insets(20, 0, 0, 0));
        root.setBottom(rosterBox);

        return root;
    }

    private VBox buildRosterBox(TextField p1Field, TextField p2Field) {
        Label rosterTitle = new Label("Adventurer Roster");
        rosterTitle.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #444;");

        GridPane grid = buildRosterGrid(p1Field.getText().trim(), p2Field.getText().trim());

        // Rebuild grid whenever either name changes
        p1Field.textProperty().addListener((obs, o, n) ->
                refreshRoster(grid, p1Field.getText().trim(), p2Field.getText().trim()));
        p2Field.textProperty().addListener((obs, o, n) ->
                refreshRoster(grid, p1Field.getText().trim(), p2Field.getText().trim()));

        VBox box = new VBox(6, rosterTitle, grid);
        box.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-padding: 12;");
        return box;
    }

    private GridPane buildRosterGrid(String activeName1, String activeName2) {
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(4);
        ColumnConstraints nameCol = new ColumnConstraints(160);
        ColumnConstraints playedCol = new ColumnConstraints(80);
        ColumnConstraints winsCol = new ColumnConstraints(70);
        ColumnConstraints lossesCol = new ColumnConstraints(80);
        ColumnConstraints rateCol = new ColumnConstraints(100);
        grid.getColumnConstraints().addAll(nameCol, playedCol, winsCol, lossesCol, rateCol);

        // Header row
        String headerStyle = "-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #888;";
        grid.add(styledLabel("NAME",     headerStyle), 0, 0);
        grid.add(styledLabel("PLAYED",   headerStyle), 1, 0);
        grid.add(styledLabel("WINS",     headerStyle), 2, 0);
        grid.add(styledLabel("LOSSES",   headerStyle), 3, 0);
        grid.add(styledLabel("WIN RATE", headerStyle), 4, 0);

        populateRosterRows(grid, activeName1, activeName2);
        return grid;
    }

    private void refreshRoster(GridPane grid, String activeName1, String activeName2) {
        grid.getChildren().removeIf(n -> GridPane.getRowIndex(n) != null && GridPane.getRowIndex(n) > 0);
        populateRosterRows(grid, activeName1, activeName2);
    }

    private void populateRosterRows(GridPane grid, String activeName1, String activeName2) {
        java.util.List<PlayerProfile> profiles = new java.util.ArrayList<>(profileManager.getAllProfiles());
        profiles.sort(java.util.Comparator.comparingInt(PlayerProfile::getGamesWin).reversed());

        java.util.Set<String> shown = new java.util.LinkedHashSet<>();
        profiles.forEach(p -> shown.add(p.getPlayerName()));

        int row = 1;
        for (PlayerProfile profile : profiles) {
            boolean active = profile.getPlayerName().equalsIgnoreCase(activeName1)
                    || profile.getPlayerName().equalsIgnoreCase(activeName2);
            String rowStyle = active
                    ? "-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #2a7ae2;"
                    : "-fx-font-size: 12; -fx-text-fill: #333;";
            int played = profile.getGamesPlayed();
            int wins   = profile.getGamesWin();
            int losses = profile.getGamesLost();
            int winPct = played == 0 ? 0 : (int) Math.round(wins * 100.0 / played);

            grid.add(styledLabel(profile.getPlayerName(), rowStyle), 0, row);
            grid.add(styledLabel(String.valueOf(played),  rowStyle), 1, row);
            grid.add(styledLabel(String.valueOf(wins),    rowStyle), 2, row);
            grid.add(styledLabel(String.valueOf(losses),  rowStyle), 3, row);
            grid.add(styledLabel(winPct + "%",            rowStyle), 4, row);
            row++;
        }

        // New adventurers entered but not yet saved
        for (String name : new String[]{activeName1, activeName2}) {
            if (!name.isEmpty() && !shown.contains(name)) {
                String newStyle = "-fx-font-size: 12; -fx-font-style: italic; -fx-text-fill: #888;";
                grid.add(styledLabel(name,              newStyle), 0, row);
                grid.add(styledLabel("—",               newStyle), 1, row);
                grid.add(styledLabel("—",               newStyle), 2, row);
                grid.add(styledLabel("—",               newStyle), 3, row);
                grid.add(styledLabel("New adventurer",  newStyle), 4, row);
                row++;
            }
        }

        if (row == 1) {
            String emptyStyle = "-fx-font-size: 12; -fx-text-fill: #aaa; -fx-font-style: italic;";
            grid.add(styledLabel("No adventurers yet", emptyStyle), 0, 1);
        }
    }

    private Label styledLabel(String text, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        return l;
    }

}
