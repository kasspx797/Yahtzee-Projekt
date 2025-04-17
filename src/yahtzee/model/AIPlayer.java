package yahtzee.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIPlayer {
    private final GameLogic gameLogic;

    public AIPlayer(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public void playTurn() {
        // 3x würfeln
        for (int i = 0; i < 3; i++) {
            gameLogic.rollDice();
        }

        // Beste Kategorie wählen
        Map<String, Integer> scores = new HashMap<>();
        for (ScoreCategory cat : gameLogic.getScoreCategories()) {
            if (!cat.isUsed()) {
                int score = calculateScore(cat.getName(), gameLogic.getDiceList());
                scores.put(cat.getName(), score);
            }
        }

        String bestCategory = null;
        int bestScore = -1;
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestCategory = entry.getKey();
            }
        }

        // Punkte eintragen
        for (ScoreCategory cat : gameLogic.getScoreCategories()) {
            if (cat.getName().equals(bestCategory)) {
                cat.setPoints(bestScore);
                break;
            }
        }

        gameLogic.resetRound();
    }

    private int calculateScore(String category, List<Dice> diceList) {
        int target = switch (category) {
            case "Einser" -> 1;
            case "Zweier" -> 2;
            case "Dreier" -> 3;
            case "Vierer" -> 4;
            case "Fünfer" -> 5;
            case "Sechser" -> 6;
            default -> -1;
        };

        if (target == -1) return 0;

        int sum = 0;
        for (Dice d : diceList) {
            if (d.getValue() == target) {
                sum += target;
            }
        }
        return sum;
    }
}
