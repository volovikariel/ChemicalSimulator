package mainapplication;

/**
 *
 * @author Jorge
 */
public class Solution {
    private int[][] matrix;
    private int score;
    
    private String[] names;

    public Solution(int[][] matrix, int score) {
        this.matrix = matrix;
        this.score = score;
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
