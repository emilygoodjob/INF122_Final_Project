package gmae.menu;

import gmae.adventure.Action;
import gmae.adventure.ActionType;
import gmae.adventure.AdventureState;
import gmae.adventure.MiniAdventure;
import gmae.adventure.Result;
import gmae.adventures.CaravanTradeAdventure;
import gmae.adventures.RelicHuntAdventure;
import gmae.adventures.TradeList;
import gmae.adventures.TradeOrder;
import gmae.model.ItemAdapter;
import gmae.model.Player;
import gmae.model.RealmView;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameView {

    private final MiniAdventure adventure;
    private final Player player1;
    private final Player player2;
    private final Runnable onBackToMenu;

    private Player currentPlayer;
    private boolean player1Done;

    private Label roundLabel;
    private Label statusLabel;
    private VBox p1InfoBox;
    private VBox p2InfoBox;
    private VBox actionPanel;
    private VBox logBox;

    public GameView(Player p1, Player p2, MiniAdventure adventure, Runnable onBackToMenu) {
        this.player1 = p1;
        this.player2 = p2;
        this.adventure = adventure;
        this.onBackToMenu = onBackToMenu;
    }

    public Parent buildView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setStyle("-fx-font-family: 'System';");

        // Top bar: adventure name + round info
        Label title = new Label(adventure.getName());
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        roundLabel = new Label();
        roundLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #666;");
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2a7ae2;");
        VBox topBox = new VBox(4, title, roundLabel, statusLabel);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(0, 0, 12, 0));
        root.setTop(topBox);

        // Left: Player 1 info
        p1InfoBox = new VBox(6);
        p1InfoBox.setPadding(new Insets(10));
        p1InfoBox.setStyle("-fx-background-color: #f0f7ff; -fx-background-radius: 8;");
        p1InfoBox.setPrefWidth(220);

        // Right: Player 2 info
        p2InfoBox = new VBox(6);
        p2InfoBox.setPadding(new Insets(10));
        p2InfoBox.setStyle("-fx-background-color: #fff5f0; -fx-background-radius: 8;");
        p2InfoBox.setPrefWidth(220);

        // Center: action panel + log
        actionPanel = new VBox(8);
        actionPanel.setPadding(new Insets(10));
        actionPanel.setStyle("-fx-background-color: #f8f8f8; -fx-background-radius: 8;");

        logBox = new VBox(4);
        logBox.setPadding(new Insets(8));
        ScrollPane logScroll = new ScrollPane(logBox);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(160);
        logScroll.setStyle("-fx-background: white; -fx-background-color: white;");

        Label logTitle = new Label("Game Log");
        logTitle.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #888;");

        VBox centerBox = new VBox(10, actionPanel, new Separator(), logTitle, logScroll);
        centerBox.setPadding(new Insets(0, 14, 0, 14));
        HBox.setHgrow(centerBox, Priority.ALWAYS);

        HBox mainRow = new HBox(10, p1InfoBox, centerBox, p2InfoBox);
        root.setCenter(mainRow);

        // Initialize game
        adventure.init(player1, player2);
        currentPlayer = player1;
        player1Done = false;

        refreshAll();
        showActionChoices();

        return root;
    }

    // ========================
    // Refresh display
    // ========================

    private void refreshAll() {
        refreshRoundInfo();
        refreshPlayerInfo(p1InfoBox, player1, "Player 1", "#2a7ae2");
        refreshPlayerInfo(p2InfoBox, player2, "Player 2", "#e2522a");
    }

    private void refreshRoundInfo() {
        AdventureState state = adventure.getState();
        String roundText = "Round " + state.getCurrentRound();
        if (state.getMaxRounds() > 0) {
            roundText += " / " + state.getMaxRounds();
        }

        if (adventure instanceof CaravanTradeAdventure cta) {
            roundText += "  |  Gold target: " + cta.getGoldTarget();
        } else if (adventure instanceof RelicHuntAdventure rha) {
            roundText += "  |  Relic target: " + rha.getTargetRelics();
        }
        roundLabel.setText(roundText);

        if (!adventure.isFinished()) {
            String name = currentPlayer.getProfile().getPlayerName();
            statusLabel.setText(name + "'s turn");
            statusLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: " +
                    (currentPlayer == player1 ? "#2a7ae2" : "#e2522a") + ";");
        }
    }

    private void refreshPlayerInfo(VBox box, Player player, String label, String color) {
        box.getChildren().clear();

        boolean isCurrent = (player == currentPlayer && !adventure.isFinished());
        box.setStyle("-fx-background-color: " + (isCurrent ? (player == player1 ? "#dbe9fa" : "#fae0db") : (player == player1 ? "#f0f7ff" : "#fff5f0")) + "; -fx-background-radius: 8;" + (isCurrent ? " -fx-border-color: " + color + "; -fx-border-radius: 8; -fx-border-width: 2;" : ""));

        Label nameLabel = new Label(label + ": " + player.getProfile().getPlayerName());
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        box.getChildren().add(nameLabel);

        RealmView realm = player.getPosition();
        if (realm != null) {
            addInfoLine(box, "Location", realm.getName());
        }

        if (adventure instanceof CaravanTradeAdventure cta) {
            addInfoLine(box, "Gold", String.valueOf(cta.getGoldOf(player)));
            String goods = player.getProfile().getInventory().getItems().stream()
                    .map(ItemAdapter::getName)
                    .collect(Collectors.groupingBy(n -> n, Collectors.counting()))
                    .entrySet().stream()
                    .map(e -> e.getKey() + " x" + e.getValue())
                    .collect(Collectors.joining(", "));
            addInfoLine(box, "Goods", goods.isEmpty() ? "none" : goods);

            if (realm != null) {
                TradeList market = cta.getMarketAt(realm);
                if (market != null) {
                    Label mktLabel = new Label("Market here:");
                    mktLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #666;");
                    box.getChildren().add(mktLabel);
                    for (String item : market.getItemNames()) {
                        Label priceLabel = new Label("  " + item + ": buy " + market.getBuyPrice(item) + "g / sell " + market.getSellPrice(item) + "g");
                        priceLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #555;");
                        box.getChildren().add(priceLabel);
                    }
                }
            }
        } else if (adventure instanceof RelicHuntAdventure rha) {
            addInfoLine(box, "Relics", String.valueOf(rha.getRelicCount(player)));
            int invSize = player.getProfile().getInventory().size();
            addInfoLine(box, "Items", invSize == 0 ? "none" : invSize + " power-up(s)");
        }

        addInfoLine(box, "Score", String.valueOf(player.getScore()));
    }

    private void addInfoLine(VBox box, String label, String value) {
        Label line = new Label(label + ": " + value);
        line.setStyle("-fx-font-size: 12;");
        box.getChildren().add(line);
    }

    // ========================
    // Action handling
    // ========================

    private void showActionChoices() {
        actionPanel.getChildren().clear();

        if (adventure.isFinished()) {
            showGameOver();
            return;
        }

        Label prompt = new Label("Choose an action:");
        prompt.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
        actionPanel.getChildren().add(prompt);

        List<ActionType> actions = adventure.getValidActions(currentPlayer);
        FlowPane buttons = new FlowPane(8, 8);
        for (ActionType type : actions) {
            Button btn = new Button(formatActionType(type));
            btn.setStyle("-fx-font-size: 12; -fx-padding: 6 16;");
            btn.setOnAction(e -> handleActionType(type));
            buttons.getChildren().add(btn);
        }
        actionPanel.getChildren().add(buttons);

        // Show open orders for Caravan Trade
        showOpenOrders();
    }

    private void handleActionType(ActionType type) {
        switch (type) {
            case MOVE -> showMoveOptions();
            case BUY -> showBuyOptions();
            case SELL -> showSellOptions();
            case TRADE -> showTradeOptions();
            case USE_ITEM -> showUseItemOptions();
            case DEFEND, PASS -> applyAndAdvance(Action.of(type));
        }
    }

    // -- MOVE --
    private void showMoveOptions() {
        actionPanel.getChildren().clear();
        Label prompt = new Label("Choose a destination:");
        prompt.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
        actionPanel.getChildren().add(prompt);

        List<RealmView> neighbors = adventure.getRealmMap().neighborsOf(currentPlayer.getPosition());
        for (RealmView realm : neighbors) {
            String label = realm.getName();
            if (adventure instanceof RelicHuntAdventure rha) {
                label += " (relics: " + rha.getRelicsInRealm(realm) + ")";
            }
            Button btn = new Button(label);
            btn.setStyle("-fx-font-size: 12; -fx-padding: 4 12;");
            btn.setOnAction(e -> applyAndAdvance(Action.of(ActionType.MOVE, "target", realm)));
            actionPanel.getChildren().add(btn);
        }
        addCancelButton();
    }

    // -- BUY --
    private void showBuyOptions() {
        if (!(adventure instanceof CaravanTradeAdventure cta)) return;
        actionPanel.getChildren().clear();

        TradeList market = cta.getMarketAt(currentPlayer.getPosition());
        if (market == null) return;

        Label prompt = new Label("Choose a good to buy:");
        prompt.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
        actionPanel.getChildren().add(prompt);

        int playerGold = cta.getGoldOf(currentPlayer);
        for (String itemName : market.getItemNames()) {
            int price = market.getBuyPrice(itemName);
            if (price > 0 && playerGold >= price) {
                int maxQty = Math.min(3, playerGold / price);
                Button btn = new Button(itemName + " (" + price + "g each)");
                btn.setStyle("-fx-font-size: 12; -fx-padding: 4 12;");
                btn.setOnAction(e -> showQuantityPicker(itemName, maxQty, ActionType.BUY));
                actionPanel.getChildren().add(btn);
            }
        }
        addCancelButton();
    }

    // -- SELL --
    private void showSellOptions() {
        if (!(adventure instanceof CaravanTradeAdventure cta)) return;
        actionPanel.getChildren().clear();

        TradeList market = cta.getMarketAt(currentPlayer.getPosition());
        if (market == null) return;

        Label prompt = new Label("Choose a good to sell:");
        prompt.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
        actionPanel.getChildren().add(prompt);

        Map<String, Long> owned = currentPlayer.getProfile().getInventory().getItems().stream()
                .map(ItemAdapter::getName)
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));

        for (Map.Entry<String, Long> entry : owned.entrySet()) {
            String name = entry.getKey();
            int count = entry.getValue().intValue();
            int sellPrice = market.getSellPrice(name);
            if (sellPrice > 0) {
                Button btn = new Button(name + " x" + count + " (sell: " + sellPrice + "g each)");
                btn.setStyle("-fx-font-size: 12; -fx-padding: 4 12;");
                btn.setOnAction(e -> showQuantityPicker(name, count, ActionType.SELL));
                actionPanel.getChildren().add(btn);
            }
        }
        addCancelButton();
    }

    private void showQuantityPicker(String itemName, int maxQty, ActionType type) {
        actionPanel.getChildren().clear();
        Label prompt = new Label("How many " + itemName + "?");
        prompt.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
        actionPanel.getChildren().add(prompt);

        FlowPane buttons = new FlowPane(8, 8);
        for (int q = 1; q <= maxQty; q++) {
            int qty = q;
            Button btn = new Button(String.valueOf(q));
            btn.setStyle("-fx-font-size: 12; -fx-padding: 4 16;");
            btn.setOnAction(e -> applyAndAdvance(Action.of(type, Map.of(
                    "itemName", itemName,
                    "quantity", qty
            ))));
            buttons.getChildren().add(btn);
        }
        actionPanel.getChildren().add(buttons);
        addCancelButton();
    }

    // -- TRADE (deliver order) --
    private void showTradeOptions() {
        if (!(adventure instanceof CaravanTradeAdventure cta)) return;
        actionPanel.getChildren().clear();

        Label prompt = new Label("Choose an order to deliver:");
        prompt.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
        actionPanel.getChildren().add(prompt);

        List<TradeOrder> orders = cta.getFulfillableOrdersForPlayer(currentPlayer);
        for (TradeOrder order : orders) {
            Button btn = new Button(order.summary());
            btn.setStyle("-fx-font-size: 12; -fx-padding: 4 12;");
            btn.setOnAction(e -> applyAndAdvance(Action.of(ActionType.TRADE, "orderId", order.getId())));
            actionPanel.getChildren().add(btn);
        }
        addCancelButton();
    }

    // -- USE_ITEM --
    private void showUseItemOptions() {
        actionPanel.getChildren().clear();

        Label prompt = new Label("Choose an item to use:");
        prompt.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
        actionPanel.getChildren().add(prompt);

        List<ItemAdapter> items = currentPlayer.getProfile().getInventory().getItems();
        for (ItemAdapter item : items) {
            Button btn = new Button(item.getName() + " - " + item.getDescription());
            btn.setStyle("-fx-font-size: 12; -fx-padding: 4 12;");
            btn.setOnAction(e -> applyAndAdvance(Action.of(ActionType.USE_ITEM, "itemId", item.getId())));
            actionPanel.getChildren().add(btn);
        }
        addCancelButton();
    }

    // ========================
    // Game flow
    // ========================

    private void applyAndAdvance(Action action) {
        String playerName = currentPlayer.getProfile().getPlayerName();
        addLog(playerName + ": " + formatAction(action));

        adventure.applyAction(currentPlayer, action);

        if (adventure.isFinished()) {
            refreshAll();
            showGameOver();
            return;
        }

        if (!player1Done) {
            // Player 1 just finished, switch to Player 2
            player1Done = true;
            currentPlayer = player2;
        } else {
            // Player 2 finished, end round
            player1Done = false;
            adventure.endRound();
            currentPlayer = player1;

            if (adventure.isFinished()) {
                refreshAll();
                showGameOver();
                return;
            }

            AdventureState state = adventure.getState();
            addLog("--- Round " + state.getCurrentRound() + " ---");
        }

        refreshAll();
        showActionChoices();
    }

    private void showGameOver() {
        actionPanel.getChildren().clear();

        Result result = adventure.getResult();
        if (result == null) return;

        // Update profiles
        player1.getProfile().incrementGamesPlayed();
        player2.getProfile().incrementGamesPlayed();
        if (!result.isTie() && result.getWinner() != null) {
            result.getWinner().getProfile().incrementGamesWin();
        }

        Label gameOverLabel = new Label("Game Over!");
        gameOverLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        Label resultLabel = new Label(result.toString());
        resultLabel.setStyle("-fx-font-size: 14;");
        resultLabel.setWrapText(true);

        statusLabel.setText("Game Over!");
        statusLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #333;");

        Button backBtn = new Button("Back to Menu");
        backBtn.setStyle("-fx-font-size: 13; -fx-padding: 8 24;");
        backBtn.setOnAction(e -> onBackToMenu.run());

        Button playAgainBtn = new Button("Play Again");
        playAgainBtn.setStyle("-fx-font-size: 13; -fx-padding: 8 24;");
        playAgainBtn.setOnAction(e -> {
            adventure.init(player1, player2);
            currentPlayer = player1;
            player1Done = false;
            logBox.getChildren().clear();
            refreshAll();
            showActionChoices();
        });

        HBox buttons = new HBox(10, playAgainBtn, backBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        actionPanel.getChildren().addAll(gameOverLabel, resultLabel, buttons);
        addLog("=== " + result + " ===");
    }

    // ========================
    // Open orders display (for Caravan Trade)
    // ========================

    private void showOpenOrders() {
        if (!(adventure instanceof CaravanTradeAdventure cta)) return;
        List<TradeOrder> orders = cta.getOpenOrdersList();
        if (orders.isEmpty()) return;

        Label header = new Label("Open Orders:");
        header.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #666;");
        actionPanel.getChildren().add(header);

        for (TradeOrder order : orders) {
            Label orderLabel = new Label("  " + order.summary());
            orderLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #555;");
            actionPanel.getChildren().add(orderLabel);
        }
    }

    // ========================
    // Helpers
    // ========================

    private void addCancelButton() {
        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-font-size: 11; -fx-text-fill: #999; -fx-padding: 2 10;");
        cancel.setOnAction(e -> showActionChoices());
        actionPanel.getChildren().add(cancel);
    }

    private void addLog(String message) {
        Label log = new Label(message);
        log.setStyle("-fx-font-size: 11; -fx-text-fill: #444;");
        log.setWrapText(true);
        logBox.getChildren().add(log);
    }

    private String formatActionType(ActionType type) {
        return switch (type) {
            case MOVE -> "Move";
            case BUY -> "Buy";
            case SELL -> "Sell";
            case TRADE -> "Deliver Order";
            case DEFEND -> "Defend";
            case USE_ITEM -> "Use Item";
            case PASS -> "Pass";
        };
    }

    private String formatAction(Action action) {
        ActionType type = action.getType();
        return switch (type) {
            case MOVE -> {
                RealmView target = (RealmView) action.getParam("target");
                yield "moved to " + (target != null ? target.getName() : "?");
            }
            case BUY -> "bought " + action.getParam("quantity") + " " + action.getParam("itemName");
            case SELL -> "sold " + action.getParam("quantity") + " " + action.getParam("itemName");
            case TRADE -> "delivered an order";
            case DEFEND -> "is defending";
            case USE_ITEM -> "used an item";
            case PASS -> "passed";
        };
    }
}
