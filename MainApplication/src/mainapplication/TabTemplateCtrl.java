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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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
    AnchorPane lewisAncPaneID;
    
    public void sendSolution(int [][] solution, String[] elements) {
        matrix = solution;
        Line line = new Line(162, 8, 168, 8);
        int elementCount = matrix.length; //rows
        int bondCount = 0;
        //lewisAncPaneID.getChildren().addAll(oxygen, hydrogen, line);
        
        
        //lewisAncPaneID.setRightAnchor(oxygen, 20.0);
        //lewisAncPaneID.setRightAnchor(hydrogen, 40.0);
        
        for (int row = 0; row < matrix.length; row++) {
            Label tempLbl = new Label(elements[row]);

            lewisAncPaneID.getChildren().add(tempLbl);
            lewisAncPaneID.setTopAnchor(tempLbl, 20.0*row);
            
            for (int col = 0; col < matrix[0].length; col++) {
                Label numLbl = new Label("" + matrix[row][col]);

                lewisAncPaneID.getChildren().add(numLbl);
                lewisAncPaneID.setTopAnchor(numLbl, 20.0*row);
                lewisAncPaneID.setLeftAnchor(numLbl, 20.0*(col+1));

                if (matrix[row][col] != 0) {
                    //matrix[row][col] = bondCount;
                    
                    //need the elements corresponding to the index of each matrix
                    //row -> element x, col -> element y // (x,y) -> bond
                    //print elements in the lewis pane
                    
                    
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
