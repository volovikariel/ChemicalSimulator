/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainapplication;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Line;
import javafx.scene.shape.Sphere;

/**
 * FXML Controller class
 *
 * @author cstuser
 */
public class TabTemplateCtrl implements Initializable {

    int[][] matrix;
    String[] atomList;
    
    @FXML
    SubScene realView;
    @FXML
    StackPane bindAnchor;
    
    @FXML
    Canvas lewisCanvasID;
    
    double initX;
    double initY;
    double startTransX;
    double startTransY;
    
    private Group root = new Group();
    
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
    
    
    public void set3D(int [][] solution, String[] atomList) {
        matrix = solution;
        this.atomList = atomList;
        
        ArrayList<Sphere> finalList = getRelativeLocation(0, -1, new double[] {100, 0, 0});
        
        double[] transVec = {0, 0, 0};
        
        for (Sphere sphere : finalList) {
            sphere.setTranslateX(sphere.getTranslateX() + transVec[0]);
            sphere.setTranslateY(sphere.getTranslateY() + transVec[1]);
            sphere.setTranslateZ(sphere.getTranslateZ() + transVec[2]);
        }        
        
        root.getChildren().addAll(finalList);
        
        realView.setRoot(root);
    }
    
    public ArrayList<Sphere> getRelativeLocation(int currRow, int prevRow, double[] vec) {
        ArrayList<Sphere> returnList = new ArrayList<>();
        
        if (atomList[currRow].equals("H")) {
            Sphere temp = new Sphere(40);
            temp.setMaterial(new PhongMaterial(Color.WHITE));
            returnList.add(temp);
            
            if (prevRow != -1) {
                return returnList;
            }
            
            for (int i = 0; i < matrix.length; i++) {
                if (matrix[currRow][i] != 0) {
                    double[] transVec = {100, 0, 0};
                    ArrayList<Sphere> recursion = getRelativeLocation(i, currRow, transVec);
                    
                    for (Sphere sphere : recursion) {
                        sphere.setTranslateX(sphere.getTranslateX() + transVec[0]);
                        sphere.setTranslateY(sphere.getTranslateY() + transVec[1]);
                        sphere.setTranslateZ(sphere.getTranslateZ() + transVec[2]);
                        
                        returnList.add(sphere);
                    }
                }
            }
            
            return returnList;
        }
        
        int bondCount = 0;          //see how many lone pairs
        int thingsBondedCount = 0;  //get steric number

        for (int el : matrix[currRow]) {
            if (el != 0) {
                bondCount += el;
                thingsBondedCount++;
            }
        }

        int lonePairs = 4 - bondCount;
        int stericNumber = thingsBondedCount + lonePairs;

        Sphere temp = new Sphere(50);
        temp.setMaterial(new PhongMaterial(Color.RED));
        returnList.add(temp);

        for (int i = 0; i < matrix.length; i++) {
            if (matrix[currRow][i] != 0) {
                if (i != prevRow) {
                    double[] transVec = {100, 0, 0};
                    ArrayList<Sphere> recursion = getRelativeLocation(i, currRow, transVec);

                    for (Sphere sphere : recursion) {
                        sphere.setTranslateX(sphere.getTranslateX() + transVec[0]);
                        sphere.setTranslateY(sphere.getTranslateY() + transVec[1]);
                        sphere.setTranslateZ(sphere.getTranslateZ() + transVec[2]);

                        returnList.add(sphere);
                    }
                }
            }
        }
        
        return returnList;
    } 
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        realView.setManaged(false);
        realView.heightProperty().bind(bindAnchor.heightProperty());
        realView.widthProperty().bind(bindAnchor.widthProperty());
        root.layoutXProperty().bind(realView.widthProperty().divide(2));
        root.layoutYProperty().bind(realView.heightProperty().divide(2));
    }
    
    @FXML
    public void handleMouseClick(MouseEvent event) {
        initX = event.getX();
        initY = event.getY();
        
        //startTransX = root.getTranslateX();
        //startTransY = root.getTranslateY();
        
        startTransX = root.translateXProperty().get();
        startTransY = root.translateYProperty().get();
    }
            
    @FXML
    public void handleMouseDrag(MouseEvent event) {
        root.translateXProperty().set(event.getX() - initX + startTransX);
        root.translateYProperty().set(event.getY() - initY + startTransY);
        //root.setTranslateX(event.getX() - initX + startTransX);
        //root.setTranslateY(event.getY() - initY + startTransY);
    }
}
