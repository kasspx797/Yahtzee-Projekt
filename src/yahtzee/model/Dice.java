package yahtzee.model;

import java.util.Random;

public class Dice {
    private int value;
    private boolean held;
    private static final Random random = new Random();

    public Dice() {
        roll();
        held = false;
    }

    public void roll() {
        if (!held) {
            value = random.nextInt(6) + 1;
        }
    }

    public int getValue() {
        return value;
    }

    public void setHeld(boolean held) {
        this.held = held;
    }

    public boolean isHeld() {
        return held;
    }

    public void toggleHold() {
        held = !held;
    }
}
