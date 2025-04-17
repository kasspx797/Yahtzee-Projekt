package yahtzee.model;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {
    private List<Dice> diceList;
    private int rollsLeft;
    private List<ScoreCategory> scoreCategories;

    public GameLogic() {
        diceList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            diceList.add(new Dice());
        }
        rollsLeft = 3;

        scoreCategories = new ArrayList<>();
        scoreCategories.add(new ScoreCategory("Einser"));
        scoreCategories.add(new ScoreCategory("Zweier"));
        scoreCategories.add(new ScoreCategory("Dreier"));
        scoreCategories.add(new ScoreCategory("Vierer"));
        scoreCategories.add(new ScoreCategory("Fünfer"));
        scoreCategories.add(new ScoreCategory("Sechser"));

        scoreCategories.add(new ScoreCategory("Dreierpasch"));
        scoreCategories.add(new ScoreCategory("Viererpasch"));
        scoreCategories.add(new ScoreCategory("Full House"));
        scoreCategories.add(new ScoreCategory("Kleine Straße"));
        scoreCategories.add(new ScoreCategory("Große Straße"));
        scoreCategories.add(new ScoreCategory("Kniffel"));
        scoreCategories.add(new ScoreCategory("Chance"));
    }

    public void rollDice() {
        if (rollsLeft > 0) {
            for (Dice dice : diceList) {
                dice.roll();
            }
            rollsLeft--;
        }
    }

    public List<Dice> getDiceList() {
        return diceList;
    }

    public int getRollsLeft() {
        return rollsLeft;
    }

    public void resetRound() {
        for (Dice dice : diceList) {
            dice.setHeld(false);
        }
        rollsLeft = 3;
    }

    public List<ScoreCategory> getScoreCategories() {
        return scoreCategories;
    }
}