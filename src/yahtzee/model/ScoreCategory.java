package yahtzee.model;

public class ScoreCategory {
    private final String name;
    private int points;
    private boolean used;

    public ScoreCategory(String name) {
        this.name = name;
        this.points = -1; // -1 = noch nicht belegt
        this.used = false;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
        this.used = true;
    }

    public boolean isUsed() {
        return used;
    }
}