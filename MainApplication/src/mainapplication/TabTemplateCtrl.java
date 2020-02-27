/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainapplication;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author cstuser
 */
public class TabTemplateCtrl implements Initializable {

    int[][] matrix;
    
    Label hydrogen = new Label("H");
    Label oxygen = new Label("O");
    
    @FXML
    Pane lewisPaneID;
    
    public void sendSolution(int [][] solution) {
        matrix = solution;
        
        int elementCount = matrix.length; //rows
        int bondCount = 0;
        lewisPaneID.getChildren().addAll(oxygen, hydrogen);
        
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[0].length; col++) {
                if (matrix[row][col] != 0) {
                    matrix[row][col] = bondCount;
                    
                    //need the elements corresponding to the index of each matrix
                    //row -> element x, col -> element y
                    //print elements in the lewis pane
                    
                    //lewisPaneID.getChildren().addAll(oxygen, hydrogen);
                }
            }
        }
    }
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
}
