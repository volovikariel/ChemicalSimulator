/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainapplication;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * FXML Controller class
 *
 * @author cstuser
 */
public class TabTemplateCtrl implements Initializable {

    int[][] matrix;
    String[] atomList;
    
    public SubScene realView;
    Atom[] atoms;
    
    @FXML
    StackPane bindAnchor;
    
    @FXML
    Canvas lewisCanvasID;
    
    final int BOND_SIZE = 125;
    
    double initX;
    double initY;
    double startTransX;
    double startTransY;
    
    double initXAng;
    double initYAng;
    double prevXAng = 0;
    double prevYAng = 0;
    
    public PerspectiveCamera camera;
        
    private Group atomGroup = new Group();
    
    public void setLewisStructure(int [][] solution, String[] atomList) {
        matrix = solution;
        int [][] triMatrix = new int [matrix.length - 1][matrix.length - 1];
        int elementCount = matrix.length; //rows
        int bondCount = 0;
        int bond = 0;
        GraphicsContext gc = lewisCanvasID.getGraphicsContext2D();
        
        //iterates through the list of atoms
//        for (int i = 0; i < atomList.length; i++) {
//            String tempElement = atomList[i];
//            
//            gc.strokeText(tempElement, 150 + 20*i, 175);
//            
//            Paint p = Color.BLACK;
//            gc.strokeLine(i+100, 150, 175*i, 150);
//        }
        
        //this is only the upper half of the matrix which contains the solution once
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 1; col < matrix[0].length; col++) {
                triMatrix[row][col] = matrix[row][col];
            }
        }
        
        
        for (int row = 0; row < triMatrix.length; row++) {
            System.out.println(atomList[row]);
            
            for (int col = 0; col < triMatrix[0].length; col++) {
                System.out.println(atomList[col]);
                
                if (triMatrix[row][col] != 0) {
                    bondCount++;
                    System.out.println("(" + col + ", " + row + ")");
                }
            }
        }
        // boundCount --> amount of bonds in the molecule
        //
        
        
        
        //iterates through the solution matrix
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[0].length; col++) {
                if (matrix[row][col] != 0) {
                    String tempElement = atomList[row];
                    String tempElement2 = atomList[col];
                    bond = matrix[row][col];
                    
                    
                    
                    
                    
                    gc.strokeText(tempElement, 150 + 20 * row, 175);
                    gc.strokeText(tempElement2, 150 + 20 * row, 175);
                    
                    Paint p = Color.BLACK;
                    gc.strokeLine(row + 100, 150, 175 * row, 150);
                    
                }
            }
        }
        
    }
    
    public void printLewis(String [] atoms, int bonds) {
        
    }
    
    
    public void set3D(int [][] solution, String[] atomList) {
        matrix = solution;
        this.atomList = atomList;
        
        ArrayList<Node> finalList = getRelativeLocation(0, -1, new double[] {BOND_SIZE, 0, 0}, new LinkedList<>());
        
        double[] transVec = {0, 0, 0};
        
        for (Node sphere : finalList) {
            sphere.setTranslateX(sphere.getTranslateX() + transVec[0]);
            sphere.setTranslateY(sphere.getTranslateY() + transVec[1]);
            sphere.setTranslateZ(sphere.getTranslateZ() + transVec[2]);
        }        
        
        atomGroup.getChildren().addAll(finalList);
        realView.setRoot(atomGroup);
    }
    
    public ArrayList<Node> getRelativeLocation(int currRow, int prevRow, double[] vec, LinkedList<Integer> prevs) {
        prevs.add(currRow);
        
        ArrayList<Node> returnList = new ArrayList<>();
        
        if (atomList[currRow].equals("H")) {
            Sphere temp = new Sphere(40);
            temp.setMaterial(new PhongMaterial(Color.web(atoms[0].getColor())));
            returnList.add(temp);
            
            if (prevRow != -1) {
                return returnList;
            }
            
            for (int i = 0; i < matrix.length; i++) {
                if (matrix[currRow][i] != 0) {
                    double[] transVec = {BOND_SIZE, 0, 0};
                    Cylinder bond = getCylinder(transVec);
                    returnList.add(bond);
                    ArrayList<Node> recursion = getRelativeLocation(i, currRow, transVec, prevs);
                    
                    for (Node sphere : recursion) {
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
       
        String color = null;
        int formalCharge = bondCount;
        for (int i = 0; i < atoms.length; i++) {
            if (atomList[currRow].equals(atoms[i].getSymbol()))
            {
                color = atoms[i].getColor();
                formalCharge -= i > 2 ? 8 - atoms[i].getShells() : 2 - atoms[i].getShells();
            }
        }
        Sphere temp = new Sphere(50);
        temp.setMaterial(new PhongMaterial(Color.web(color)));
        returnList.add(temp);
        //if theres formal charge, add a label
        if (formalCharge != 0) {
            Label label = new Label("" + formalCharge);
            returnList.add(label);
            label.setTranslateX(30);
            label.setTranslateY(-60);
            label.setTranslateZ(-30);
            label.setFont(new Font(40));
        }
        
        int amountFound = 0;
        int numBonds = 0;
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[currRow][i] != 0) {
                numBonds = matrix[currRow][i];
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
                    
                    //if first element, must make special case,
                    //update the transVec to be simply where you "came from"
                    if (prevRow == -1 && amountFound == 0)
                        transVec = makeRoation(vec, Math.PI, Math.PI, Math.PI);
                    
                    //add cylinder
                    double translateModif = 0;
                    for(int j = 0; j < numBonds; j++) {
                        if(numBonds == 3) {
                            translateModif = j-1;
                        } 
                        else if (numBonds == 2) {
                            translateModif = j-0.5;
                        }
                        Cylinder bond = getCylinder(transVec);
                        bond.getTransforms().add(new Translate(translateModif*transVec[0]/3, translateModif*transVec[1] * -1/4, translateModif*transVec[2]/6));
                        returnList.add(bond);
                    }
                    ArrayList<Node> recursion = getRelativeLocation(i, currRow, transVec, prevs);

                    for (Node sphere : recursion) {
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
        realView = new SubScene(bindAnchor, 100, 100, true, SceneAntialiasing.BALANCED);
        bindAnchor.getChildren().add(realView);
        realView.setManaged(false);
        realView.heightProperty().bind(bindAnchor.heightProperty());
        realView.widthProperty().bind(bindAnchor.widthProperty());
        atomGroup.layoutXProperty().bind(realView.widthProperty().divide(2));
        atomGroup.layoutYProperty().bind(realView.heightProperty().divide(2));
        camera = new PerspectiveCamera(false);
        camera.setFieldOfView(70);
        realView.setCamera(camera);
        
        atoms = MainAppCtrl.getAtoms();
        
        // Handle the scrolling for 3D
        bindAnchor.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override 
            public void handle(ScrollEvent event) {
                if(event.getDeltaY() > 0) {
                    atomGroup.scaleXProperty().set(atomGroup.getScaleX() * 1.25);
                    atomGroup.scaleYProperty().set(atomGroup.getScaleY() * 1.25);
                    atomGroup.scaleZProperty().set(atomGroup.getScaleZ() * 1.25);
                }
                else {
                    atomGroup.scaleXProperty().set(atomGroup.getScaleX() * 0.75);
                    atomGroup.scaleYProperty().set(atomGroup.getScaleY() * 0.75);
                    atomGroup.scaleZProperty().set(atomGroup.getScaleZ() * 0.75);
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

            startTransX = atomGroup.translateXProperty().get();
            startTransY = atomGroup.translateYProperty().get();
        }
        else if (event.getButton() == MouseButton.PRIMARY) {
            initXAng = event.getX();
            initYAng = event.getY();
        }
    }
            
    @FXML
    public void handleMouseDrag(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            atomGroup.translateXProperty().set(event.getX() - initX + startTransX);
            atomGroup.translateYProperty().set(event.getY() - initY + startTransY);
        }
        else if (event.getButton() == MouseButton.PRIMARY) {

            Rotate rotX = new Rotate((event.getX() - initXAng) * 360/ 200, Rotate.Y_AXIS);
            Rotate rotY = new Rotate((event.getY() - initYAng) * 360/ 200, Rotate.X_AXIS);
            atomGroup.getTransforms().addAll(rotX, rotY);
            
//            root.setRotationAxis(Rotate.Y_AXIS);
//            prevXAng += (event.getX() - initXAng) * 360/ 100;
//            root.setRotate(prevXAng);
//            

//            root.setRotationAxis(Rotate.X_AXIS);
//            prevYAng += (event.getY() - initYAng) * 360/ 100;
//            root.setRotate(prevYAng);
            
            initXAng = event.getX();
            initYAng = event.getY();
        }
    }

    private Cylinder getCylinder(double[] transVec) {
        double length = 1;
        for (double num : transVec)
            length += num * num;
        
        length = Math.sqrt(length);
        
        Cylinder cylinder = new Cylinder(5, length);
        PhongMaterial cylinderMaterial = new PhongMaterial(Color.BLACK);
        cylinderMaterial.setSpecularColor(Color.GRAY);
        cylinder.setMaterial(cylinderMaterial);
        
        Translate moveToMidpoint = new Translate(transVec[0] / 2, transVec[1] / 2, transVec[2] / 2);
        
        Point3D diff = new Point3D(transVec[0], transVec[1], transVec[2]);
        
        Point3D axisOfRot = diff.crossProduct(new Point3D(0, 1, 0));
        double angle = Math.acos(diff.normalize().dotProduct(new Point3D(0, 1, 0)));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRot);
        
        cylinder.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        
        return cylinder;
    }
}
