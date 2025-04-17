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
        System.out.println("🚀 Yahtzee gestartet!");

        gameLogic = new GameLogic();
        aiGameLogic = new GameLogic();
        aiPlayer = new AIPlayer(aiGameLogic);

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

                    for (int i = 0; i < gameLogic.getDiceList().size(); i++) {
                        gameLogic.getDiceList().get(i).setHeld(false);
                        diceImages.get(i).setStyle("");
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

        HBox diceBox = new HBox(10);
        for (Dice dice : gameLogic.getDiceList()) {
            ImageView diceView = new ImageView();
            diceView.setFitWidth(64);
            diceView.setFitHeight(64);
            diceView.setOnMouseClicked(e -> {
                if (gameLogic.getRollsLeft() < 3) {
                    dice.toggleHold();
                    diceView.setStyle(dice.isHeld()
                            ? "-fx-effect: innershadow(three-pass-box, #5fbf4a, 20, 0.5, 0, 0);"
                            : "");
                }
            });
            diceImages.add(diceView);
            diceBox.getChildren().add(diceView);
        }
        diceBox.setPadding(new Insets(10));
        diceBox.setStyle("-fx-alignment: center;");

        rollButton = new Button("Würfeln");
        rollLabel = new Label("Würfe übrig: 3");
        rollLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        rollButton.setOnAction(e -> {
            gameLogic.rollDice();
            updateDiceImages();
            rollLabel.setText("Würfe übrig: " + gameLogic.getRollsLeft());
            if (gameLogic.getRollsLeft() == 0) rollButton.setDisable(true);
        });

        HBox buttonBox = new HBox(20, rollButton, rollLabel);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        HBox scoreBoards = new HBox(50, playerScoreBoard, aiScoreBoard);
        scoreBoards.setPadding(new Insets(20));
        scoreBoards.setStyle("-fx-alignment: center;");

        VBox root = new VBox(20, scoreBoards, diceBox, buttonBox);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-alignment: center;");

        try {
            Image backgroundImg = new Image(getClass().getResourceAsStream("/images/background.png"));
            BackgroundImage backgroundImage = new BackgroundImage(
                    backgroundImg,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.DEFAULT,
                    new BackgroundSize(100, 100, true, true, false, false)
            );
            root.setBackground(new Background(backgroundImage));
        } catch (Exception ex) {
            System.out.println("❌ Hintergrundbild konnte nicht geladen werden!");
            ex.printStackTrace();
        }

        gameLogic.rollDice(); // erster Wurf
        updateDiceImages();
        updateAIScoreBoard();
        rollLabel.setText("Würfe übrig: " + gameLogic.getRollsLeft());

        Scene scene = new Scene(root, 1000, 720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Yahtzee mit KI");
        primaryStage.show();
    }

    private void updateDiceImages() {
        for (int i = 0; i < gameLogic.getDiceList().size(); i++) {
            Dice dice = gameLogic.getDiceList().get(i);
            int value = dice.getValue();
            try {
                Image img = new Image(getClass().getResourceAsStream("/images/dice_" + value + ".png"));
                diceImages.get(i).setImage(img);
            } catch (Exception e) {
                System.out.println("❌ Fehler beim Laden von dice_" + value + ".png");
                e.printStackTrace();
            }
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
                    : (playerTotal < aiTotal) ? "🤖 Die KI gewinnt!" : "Unentschieden!";

            // Erstelle Abschluss-Bildschirm
            Label endLabel = new Label("Spiel beendet!\n\n" +
                    "Deine Punkte: " + playerTotal + "\n" +
                    "KI-Punkte: " + aiTotal + "\n\n" +
                    winner);
            endLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-alignment: center;");

            Button restartButton = new Button("Neue Runde starten");
            Button closeButton = new Button("Schließen");

            restartButton.setOnAction(e -> {
                Stage stage = (Stage) restartButton.getScene().getWindow();
                Main newGame = new Main();
                try {
                    newGame.start(stage); // vollständiger Neustart
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            closeButton.setOnAction(e -> {
                ((Stage) closeButton.getScene().getWindow()).close();
            });

            HBox buttonBox = new HBox(20, restartButton, closeButton);
            buttonBox.setAlignment(Pos.CENTER);

            VBox endLayout = new VBox(30, endLabel, buttonBox);
            endLayout.setAlignment(Pos.CENTER);
            endLayout.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 40;");

            Scene endScene = new Scene(endLayout, 600, 400);
            Stage stage = (Stage) rollButton.getScene().getWindow();
            stage.setScene(endScene);
        }
    }

    private int calculateTotalPoints(List<ScoreCategory> categories) {
        return categories.stream().filter(ScoreCategory::isUsed).mapToInt(ScoreCategory::getPoints).sum();
    }

    private int calculateScore(String category, List<Dice> diceList) {
        int[] counts = new int[7];
        int sum = 0;
        for (Dice d : diceList) {
            int val = d.getValue();
            counts[val]++;
            sum += val;
        }
        return switch (category) {
            case "Einser" -> counts[1];
            case "Zweier" -> counts[2] * 2;
            case "Dreier" -> counts[3] * 3;
            case "Vierer" -> counts[4] * 4;
            case "Fünfer" -> counts[5] * 5;
            case "Sechser" -> counts[6] * 6;
            case "Dreierpasch" -> hasMinCount(counts, 3) ? sum : 0;
            case "Viererpasch" -> hasMinCount(counts, 4) ? sum : 0;
            case "Full House" -> isFullHouse(counts) ? 25 : 0;
            case "Kleine Straße" -> hasSmallStraight(counts) ? 30 : 0;
            case "Große Straße" -> hasLargeStraight(counts) ? 40 : 0;
            case "Kniffel" -> hasMinCount(counts, 5) ? 50 : 0;
            case "Chance" -> sum;
            default -> 0;
        };
    }

    private boolean hasMinCount(int[] counts, int n) {
        for (int i = 1; i <= 6; i++) {
            if (counts[i] >= n) return true;
        }
        return false;
    }

    private boolean isFullHouse(int[] counts) {
        boolean three = false, two = false;
        for (int i = 1; i <= 6; i++) {
            if (counts[i] == 3) three = true;
            if (counts[i] == 2) two = true;
        }
        return three && two;
    }

    private boolean hasSmallStraight(int[] c) {
        return (c[1] > 0 && c[2] > 0 && c[3] > 0 && c[4] > 0) ||
               (c[2] > 0 && c[3] > 0 && c[4] > 0 && c[5] > 0) ||
               (c[3] > 0 && c[4] > 0 && c[5] > 0 && c[6] > 0);
    }

    private boolean hasLargeStraight(int[] c) {
        return (c[1] == 1 && c[2] == 1 && c[3] == 1 && c[4] == 1 && c[5] == 1) ||
               (c[2] == 1 && c[3] == 1 && c[4] == 1 && c[5] == 1 && c[6] == 1);
    }

    public static void main(String[] args) {
        System.out.println("🔧 Main gestartet");
        launch(args);
    }
}