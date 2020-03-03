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
import javafx.scene.control.Label;
import javafx.scene.shape.Line;

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
    Canvas lewisCanvasID;
    
    public void sendSolution(int [][] solution, String[] atomList) {
        matrix = solution;
        Line line = new Line(162, 8, 168, 8);
        int elementCount = matrix.length; //rows
        int bondCount = 0;
        //lewisCanvasID.getChildren().addAll(oxygen, hydrogen, line);
        
        //lewisCanvasID.setRightAnchor(oxygen, 20.0);
        //lewisCanvasID.setRightAnchor(hydrogen, 40.0);
        
        for (int row = 0; row < matrix.length; row++) {
            Label tempLbl = new Label(atomList[row]);

//            lewisCanvasID.getChildren().add(tempLbl);
//            lewisCanvasID.setTopAnchor(tempLbl, 20.0 * row);
            
            for (int col = 0; col < matrix[0].length; col++) {
                

                if (matrix[row][col] != 0) {
//                    matrix[row][col] = bondCount;
                    
//                    Label numLbl = new Label("" + matrix[row][col]);

//                    lewisCanvasID.getChildren().add(numLbl);
//                    lewisCanvasID.setTopAnchor(numLbl, 20.0 * row);
//                    lewisCanvasID.setLeftAnchor(numLbl, 20.0 * (col + 1));
                    
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
