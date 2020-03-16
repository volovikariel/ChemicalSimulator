/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainapplication;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Camera;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Line;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

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
    
    double initXAng;
    double initYAng;
    double prevXAng = 0;
    double prevYAng = 0;
    
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
            
            Paint p = Color.BLACK;
            gc.strokeLine(i+100, 150, 175*i, 150);
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
        
        ArrayList<Sphere> finalList = getRelativeLocation(0, -1, new double[] {100, 0, 0}, new LinkedList<>());
        
        double[] transVec = {0, 0, 0};
        
        for (Sphere sphere : finalList) {
            sphere.setTranslateX(sphere.getTranslateX() + transVec[0]);
            sphere.setTranslateY(sphere.getTranslateY() + transVec[1]);
            sphere.setTranslateZ(sphere.getTranslateZ() + transVec[2]);
        }        
        
        root.getChildren().addAll(finalList);
        
        realView.setRoot(root);
    }
    
    public ArrayList<Sphere> getRelativeLocation(int currRow, int prevRow, double[] vec, LinkedList<Integer> prevs) {
        prevs.add(currRow);
        
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
                    ArrayList<Sphere> recursion = getRelativeLocation(i, currRow, transVec, prevs);
                    
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
        
        int amountFound = 0;

        for (int i = 0; i < matrix.length; i++) {
            if (matrix[currRow][i] != 0) {
                if (i != prevRow && !prevs.contains(i)) {
                    double[] transVec = new double[3];
                    
                    switch (stericNumber) {
                        case 2:
                            //the initial link is 180 degress from where u come from
                            //transVec = makeRoation(vec, Math.PI, Math.PI, Math.PI);
                            transVec = vec;
                            break;
                        case 3:
                            transVec = makeRoation(vec, 0, 0, Math.PI / 3 + amountFound * 4 * Math.PI / 3);
                            break;
                        case 4:
                            double rads = Math.toRadians(180 - 109.5);
                            double[] axis = normalize(vec);
                            switch (amountFound) {
                                // l -> \
                                case 0:
                                    transVec = makeRoation(vec, 0, 0, -rads);
                                    break;
                                // \ -> /
                                case 1:
                                    transVec = makeRoation(vec, 0, 0, -rads);
                                    transVec = makeRot(transVec, axis, Math.toRadians(120));
                                    break;
                                // / -> |
                                case 2:
                                    transVec = makeRoation(vec, 0, 0, -rads);
                                    transVec = makeRot(transVec, axis, Math.toRadians(-120));
                                    break;
                                default:
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    
                    
                    ArrayList<Sphere> recursion = getRelativeLocation(i, currRow, transVec, prevs);

                    for (Sphere sphere : recursion) {
                        sphere.setTranslateX(sphere.getTranslateX() + transVec[0]);
                        sphere.setTranslateY(sphere.getTranslateY() + transVec[1]);
                        sphere.setTranslateZ(sphere.getTranslateZ() + transVec[2]);

                        returnList.add(sphere);
                    }
                    
                    amountFound++;
                }
            }
        }
        
        return returnList;
    } 
    
    double[] getAxis(double[] a, double[] b) {
        double[] returnAxis = new double[3];
        
        returnAxis[0] = a[1] * b[2];
        returnAxis[1] = a[2] * b[0];
        returnAxis[2] = a[0] * b[1];
        
        double length = 0;
        
        for (int i = 0; i < 3; i++) {
            length += returnAxis[i] * returnAxis[i];
        }
        
        length = Math.sqrt(length);
        
        for (int i = 0; i < 3; i++) {
            returnAxis[i] /= length;
        }
        
        return returnAxis;
    }
    
    double[] normalize(double [] a) {
        double[] returnAxis = new double[3];
        
        double length = 0;
        
        for (int i = 0; i < 3; i++) {
            length += a[i] * a[i];
        }
        
        length = Math.sqrt(length);
        
        for (int i = 0; i < 3; i++) {
            returnAxis[i] = a[i] / length;
        }
        
        return returnAxis;
    }
    
    double[] makeRot(double[] input, double[] axis, double rad) {
        double[][] W = {{0, -axis[2], axis[1]}, {axis[2], 0, -axis[0]}, {-axis[1], axis[0], 0}};
        double[][] I = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        
        double[][] result = new double[3][3];
        
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);
        
        double[][] W2 = new double[3][3];
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int sum = 0;
                for (int k = 0; k < 3; k++) {
                    sum += W[i][k] * W[k][j];
                }
                
                W2[i][j] = sum;
            }
        }
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = I[i][j] + sin * W[i][j] + (1 - cos) * W2[i][j];
            }
        }
        
        double[] returnVec = new double[3];
        
        for (int i = 0 ; i < 3; i++) {
            double sum = 0;
            
            for (int j = 0; j < 3; j++) {
                sum += result[i][j] * input[j];
            }
            
            returnVec[i] = sum;
        }
        
        return returnVec;
    }
    
    double[] makeRoation(double[] input, double x, double y, double z) {
        double[] result = new double[3];
        
        double[][] xRot = {{1, 0, 0},{0, Math.cos(x), -Math.sin(x)},{0, Math.sin(x), Math.cos(x)}};
        
        double[][] yRot = {{Math.cos(y), 0, Math.sin(y)}, {0 , 1, 0}, {-Math.sin(y), 0, Math.cos(y)}};
        
        double[][] zRot = {{Math.cos(z), -Math.sin(z), 0}, {Math.sin(z), Math.cos(z), 0}, {0, 0, 1}};
        
        for (int i = 0 ; i < 3; i++) {
            double sum = 0;
            
            for (int j = 0; j < 3; j++) {
                sum += xRot[i][j] * input[j];
            }
            
            result[i] = sum;
        }
        
        for (int i = 0 ; i < 3; i++) {
            double sum = 0;
            
            for (int j = 0; j < 3; j++) {
                sum += yRot[i][j] * input[j];
            }
            
            result[i] = sum;
        }
        
        for (int i = 0 ; i < 3; i++) {
            double sum = 0;
            
            for (int j = 0; j < 3; j++) {
                sum += zRot[i][j] * input[j];
            }
            
            result[i] = sum;
        }
        
        return result;
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

        // Handle the scrolling for 3D
        bindAnchor.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                if(event.getDeltaY() > 0) {
                    root.scaleXProperty().set(root.getScaleX() * 1.05);
                    root.scaleYProperty().set(root.getScaleY() * 1.05);
                    root.scaleZProperty().set(root.getScaleZ() * 1.05);
                }
                else {
                    root.scaleXProperty().set(root.getScaleX() * 0.95);
                    root.scaleYProperty().set(root.getScaleY() * 0.95);
                    root.scaleZProperty().set(root.getScaleZ() * 0.95);
                }
            }
        });
    }
    
    @FXML
    public void handleMouseClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            initX = event.getX();
            initY = event.getY();

            //startTransX = root.getTranslateX();
            //startTransY = root.getTranslateY();

            startTransX = root.translateXProperty().get();
            startTransY = root.translateYProperty().get();
        }
        else if (event.getButton() == MouseButton.PRIMARY) {
            initXAng = event.getX();
            initYAng = event.getY();
        }
    }
            
    @FXML
    public void handleMouseDrag(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            root.translateXProperty().set(event.getX() - initX + startTransX);
            root.translateYProperty().set(event.getY() - initY + startTransY);
            //root.setTranslateX(event.getX() - initX + startTransX);
            //root.setTranslateY(event.getY() - initY + startTransY);
        }
        else if (event.getButton() == MouseButton.PRIMARY) {
            root.setRotationAxis(Rotate.Y_AXIS);
            prevXAng += (event.getX() - initXAng) * 360/ 100;
            root.setRotate(prevXAng);
            root.setRotationAxis(Rotate.X_AXIS);
            prevYAng += (event.getY() - initYAng) * 360/ 100;
            root.setRotate(prevYAng);
            
            initXAng = event.getX();
            initYAng = event.getY();
        }
    }
}
