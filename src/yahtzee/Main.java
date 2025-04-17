package yahtzee;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import yahtzee.model.*;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private final List<ImageView> diceImages = new ArrayList<>();
    private final List<Label> scoreLabels = new ArrayList<>();
    private final List<Label> aiScoreLabels = new ArrayList<>();
    private GameLogic gameLogic;
    private GameLogic aiGameLogic;
    private AIPlayer aiPlayer;
    private Button rollButton;
    private Label rollLabel;

    @Override
    public void start(Stage primaryStage) {
        gameLogic = new GameLogic();
        aiGameLogic = new GameLogic();
        aiPlayer = new AIPlayer(aiGameLogic);

        // Scoreboards
        VBox playerScoreBoard = new VBox(5);
        Label playerLabel = new Label("Spieler:");
        playerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black; -fx-font-size: 21px;");
        playerScoreBoard.getChildren().add(playerLabel);

        for (ScoreCategory cat : gameLogic.getScoreCategories()) {
            Label label = new Label(cat.getName() + ": -");
            label.setStyle("-fx-text-fill: white; -fx-font-size: 21px;");
            label.setOnMouseClicked(e -> {
                if (!cat.isUsed() && gameLogic.getRollsLeft() < 3) {
                    int score = calculateScore(cat.getName(), gameLogic.getDiceList());
                    cat.setPoints(score);
                    label.setText(cat.getName() + ": " + score);
                    label.setStyle("-fx-text-fill: green; -fx-font-size: 21px;");

                    gameLogic.resetRound();
                    updateDiceImages();
                    rollLabel.setText("Würfe übrig: 3");
                    rollButton.setDisable(false);

                    // Alle Würfel abwählen
                    for (int i = 0; i < gameLogic.getDiceList().size(); i++) {
                        gameLogic.getDiceList().get(i).setHeld(false);
                        diceImages.get(i).setStyle(""); // grünen Effekt entfernen
                    }

                    Label kiLabel = new Label("Die KI ist dran...");
                    StackPane kiPane = new StackPane(kiLabel);
                    Scene kiScene = new Scene(kiPane, 200, 100);
                    Stage kiStage = new Stage();
                    kiStage.setScene(kiScene);
                    kiStage.setTitle("KI spielt");
                    kiStage.show();

                    PauseTransition pause = new PauseTransition(Duration.seconds(1));
                    pause.setOnFinished(ev -> {
                        kiStage.close();
                        aiPlayer.playTurn();
                        updateAIScoreBoard();
                        checkForGameEnd();
                    });
                    pause.play();
                }
            });
            scoreLabels.add(label);
            playerScoreBoard.getChildren().add(label);
        }

        VBox aiScoreBoard = new VBox(5);
        Label aiLabel = new Label("KI:");
        aiLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black; -fx-font-size: 21px;");
        aiScoreBoard.getChildren().add(aiLabel);

        for (ScoreCategory cat : aiGameLogic.getScoreCategories()) {
            Label label = new Label(cat.getName() + ": -");
            label.setStyle("-fx-text-fill: white; -fx-font-size: 21px;");
            aiScoreLabels.add(label);
            aiScoreBoard.getChildren().add(label);
        }

        // Würfelanzeige
        HBox diceBox = new HBox(10);
        for (Dice dice : gameLogic.getDiceList()) {
            ImageView diceView = new ImageView();
            diceView.setFitWidth(64);
            diceView.setFitHeight(64);
            diceView.setOnMouseClicked(e -> {
                if (gameLogic.getRollsLeft() < 3) {
                    dice.toggleHold();
                    if (dice.isHeld()) {
                        diceView.setStyle("-fx-effect: innershadow(three-pass-box, #5fbf4a, 20, 0.5, 0, 0);");
                    } else {
                        diceView.setStyle("");
                    }
                }
            });
            diceImages.add(diceView);
            diceBox.getChildren().add(diceView);
        }
        diceBox.setPadding(new Insets(10));
        diceBox.setStyle("-fx-alignment: center;");

        // Buttons
        rollButton = new Button("Würfeln");
        rollLabel = new Label("Würfe übrig: 3");
        rollLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        rollButton.setOnAction(e -> {
            gameLogic.rollDice();
            updateDiceImages();
            rollLabel.setText("Würfe übrig: " + gameLogic.getRollsLeft());
            if (gameLogic.getRollsLeft() == 0) {
                rollButton.setDisable(true);
            }
        });


        HBox buttonBox = new HBox(20, rollButton, rollLabel);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        // Hauptlayout
        HBox scoreBoards = new HBox(50, playerScoreBoard, aiScoreBoard);
        scoreBoards.setPadding(new Insets(20));
        scoreBoards.setStyle("-fx-alignment: center;");

        VBox root = new VBox(20, scoreBoards, diceBox, buttonBox);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-alignment: center;"); // Hintergrundfarbe hier entfernen oder weglassen

        BackgroundImage backgroundImage = new BackgroundImage(
            new Image(getClass().getResource("/images/background.png").toExternalForm()),
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.DEFAULT,
            new BackgroundSize(100, 100, true, true, false, false)
        );
        root.setBackground(new Background(backgroundImage));

        updateDiceImages();
        updateAIScoreBoard();

        Scene scene = new Scene(root, 1000, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Yahtzee mit KI");
        primaryStage.show();
    }

    private void updateDiceImages() {
        for (int i = 0; i < gameLogic.getDiceList().size(); i++) {
            Dice dice = gameLogic.getDiceList().get(i);
            int value = dice.getValue();
            Image img = new Image(getClass().getResourceAsStream("/images/dice_" + value + ".png"));
            diceImages.get(i).setImage(img);
        }
    }

    private void updateAIScoreBoard() {
        List<ScoreCategory> categories = aiGameLogic.getScoreCategories();
        for (int i = 0; i < categories.size(); i++) {
            ScoreCategory cat = categories.get(i);
            String points = cat.isUsed() ? String.valueOf(cat.getPoints()) : "-";
            aiScoreLabels.get(i).setText(cat.getName() + ": " + points);
            if (cat.isUsed()) {
            	aiScoreLabels.get(i).setStyle("-fx-text-fill: green; -fx-font-size: 21px;");
            }
        }
    }

    private void checkForGameEnd() {
        boolean playerDone = gameLogic.getScoreCategories().stream().filter(ScoreCategory::isUsed).count() == 13;
        boolean aiDone = aiGameLogic.getScoreCategories().stream().filter(ScoreCategory::isUsed).count() == 13;

        if (playerDone && aiDone) {
            int playerTotal = calculateTotalPoints(gameLogic.getScoreCategories());
            int aiTotal = calculateTotalPoints(aiGameLogic.getScoreCategories());

            String winner = (playerTotal > aiTotal) ? "🎉 Du gewinnst!"
                            : (playerTotal < aiTotal) ? "🤖 Die KI gewinnt!"
                            : "Unentschieden!";

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Spiel beendet");
            alert.setHeaderText("Endstand");
            alert.setContentText("Deine Punkte: " + playerTotal +
                                 "\nKI-Punkte: " + aiTotal +
                                 "\n\n" + winner);
            alert.show();
            
            gameLogic = new GameLogic();
            aiGameLogic = new GameLogic();
            aiPlayer = new AIPlayer(aiGameLogic);

            // Würfel + Punkte zurücksetzen
            updateDiceImages();
            updateAIScoreBoard();
            rollLabel.setText("Würfe übrig: 3");
            rollButton.setDisable(false);
            for (ImageView iv : diceImages) {
                iv.setOpacity(1.0);
            }

            // Labels aktualisieren
            for (int i = 0; i < scoreLabels.size(); i++) {
                ScoreCategory cat = gameLogic.getScoreCategories().get(i);
                scoreLabels.get(i).setText(cat.getName() + ": -");
                scoreLabels.get(i).setStyle("-fx-text-fill: black;");
            }

            for (int i = 0; i < aiScoreLabels.size(); i++) {
                aiScoreLabels.get(i).setText(aiGameLogic.getScoreCategories().get(i).getName() + ": -");
                aiScoreLabels.get(i).setStyle("-fx-text-fill: black;");
            }
        }
    }

    private int calculateTotalPoints(List<ScoreCategory> categories) {
        return categories.stream()
                .filter(ScoreCategory::isUsed)
                .mapToInt(ScoreCategory::getPoints)
                .sum();
    }

    private int calculateScore(String category, List<Dice> diceList) {
        int[] counts = new int[7];
        int sum = 0;
        for (Dice d : diceList) {
            int val = d.getValue();
            counts[val]++;
            sum += val;
        }

        switch (category) {
            case "Einser": return counts[1];
            case "Zweier": return counts[2] * 2;
            case "Dreier": return counts[3] * 3;
            case "Vierer": return counts[4] * 4;
            case "Fünfer": return counts[5] * 5;
            case "Sechser": return counts[6] * 6;
            case "Dreierpasch":
                for (int i = 1; i <= 6; i++) if (counts[i] >= 3) return sum;
                return 0;
            case "Viererpasch":
                for (int i = 1; i <= 6; i++) if (counts[i] >= 4) return sum;
                return 0;
            case "Full House":
                boolean three = false, two = false;
                for (int i = 1; i <= 6; i++) {
                    if (counts[i] == 3) three = true;
                    if (counts[i] == 2) two = true;
                }
                return (three && two) ? 25 : 0;
            case "Kleine Straße":
                if ((counts[1] > 0 && counts[2] > 0 && counts[3] > 0 && counts[4] > 0) ||
                    (counts[2] > 0 && counts[3] > 0 && counts[4] > 0 && counts[5] > 0) ||
                    (counts[3] > 0 && counts[4] > 0 && counts[5] > 0 && counts[6] > 0)) return 30;
                return 0;
            case "Große Straße":
                if ((counts[1]==1 && counts[2]==1 && counts[3]==1 && counts[4]==1 && counts[5]==1) ||
                    (counts[2]==1 && counts[3]==1 && counts[4]==1 && counts[5]==1 && counts[6]==1)) return 40;
                return 0;
            case "Kniffel":
                for (int i = 1; i <= 6; i++) if (counts[i] == 5) return 50;
                return 0;
            case "Chance": return sum;
            default: return 0;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}