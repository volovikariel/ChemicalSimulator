package mainapplication;

/**
 *
 * @author Jorge
 */
public class Solution {
    private int[][] matrix;
    private int score;
    
    private String[] names;
    
    private int[] loop;

    public Solution(int[][] matrix, int score, int[] loop) {
        this.matrix = matrix;
        this.score = score;
        if (loop == null)
            this.loop = new int[0];
        else
            this.loop = loop;
    }

    public int[] getLoop() {
        return loop;
    }

    public void setLoop(int[] loop) {
        this.loop = loop;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public Solution(String[] names) {
        this.names = names;
    }    

    public int[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    
    
}
