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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.shape.Line;

/**
 * FXML Controller class
 *
 * @author cstuser
 */
public class TabTemplateCtrl implements Initializable {

    int[][] matrix;
    String[] atomList;
    
    @FXML
    Canvas lewisCanvasID;
    
    public void setLewisStructure(int [][] solution, String[] atomList) {
        matrix = solution;
        int elementCount = matrix.length; //rows
        int bondCount = 0;
        GraphicsContext gc = lewisCanvasID.getGraphicsContext2D();
        
        //iterates through the list of atoms
        for (int i = 0; i < atomList.length; i++) {
            String tempElement = atomList[i];
            
            gc.strokeText(tempElement, 150 + 20*i, 175);
        }
        
        
        //iterates through the solution matrix
        for (int row = 0; row < matrix.length; row++) {

            
            for (int col = 0; col < matrix[0].length; col++) {
                

                if (matrix[row][col] != 0) {
                    
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
