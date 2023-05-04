public class Player {
    public String name;
    public int attempts;
    public int score;

    public Player(String name, int attempts, int score) {
        this.name = name;
        this.attempts = attempts;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getScore() {
        return score;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public void setScore(int score) {
        this.score = score;
    }
}