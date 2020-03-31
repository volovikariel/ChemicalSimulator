package mainapplication;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

/**
 * Controller for the TabTemplate FXML.
 * The Controller is in charge for the specific Tab is it associated to.
 * 
 * @author Ariel Volovik
 * @author Jorge Marcano
 * @author Samy Arab
 */
public class TabTemplateCtrl implements Initializable {

    int[][] matrix;
    String[] atomList;
    
    private SubScene realView;
    Atom[] atoms;
    
    @FXML
    StackPane bindAnchor;
    
    @FXML
    Pane lewisPane;
    
    final int BOND_SIZE = 125;
    
    double initX;
    double initY;
    double startTransX;
    double startTransY;
    
    double initXAng;
    double initYAng;
    double prevXAng = 0;
    double prevYAng = 0;
    
    PerspectiveCamera camera;
    
    private Label label;
    
    private Group atomGroup = new Group();
    
    /**
     * [NOT FINISHED DOC]
     * Method which sets up the Lewis structure for a single solution.
     * @param solution the solution matrix
     * @param atomList 
     */
    public void setLewis(int [][] solution, String[] atomList, int[] loops) {
        matrix = solution;
        this.atomList = atomList;
        ArrayList<Node> finalList;
        
        if (loops.length != 0)
            finalList = doLoop2D(loops);
        else
            finalList = getRelativeLewis(0, -1, new double[] {100, 0},new LinkedList<>());
        
        for(Node node : finalList) {
            node.setTranslateX(node.getTranslateX() + 100);
            node.setTranslateY(node.getTranslateY() + 300);
        }
        lewisPane.getChildren().add(new Group(finalList));
        System.out.println("Final List Lewis: " + finalList);
    }
    
    public ArrayList<Node> getRelativeLewis(int currRow, int prevRow, double[] translateVec, LinkedList<Integer> prevs) {
        prevs.add(currRow);
        
        ArrayList<Node> returnList = new ArrayList<>();
        
        if(atomList[currRow].equals("H")) {
            returnList.add(new Label("H"));
            
            if(prevRow != -1) 
                return returnList;
            
            for(int i = 0; i < matrix.length; i++) {
                if(matrix[currRow][i] != 0) {
                    Rectangle rectangle = new Rectangle(Math.sqrt(Math.pow(translateVec[0], 2) + Math.pow(translateVec[1], 2)), 1);
                    // Do transformations to the rectangle here (???)
                    returnList.add(rectangle);
                    ArrayList<Node> recursiveCall = getRelativeLewis(i, currRow, translateVec, prevs);
                    for(Node node : recursiveCall) {
                        node.setTranslateX(node.getTranslateX() + translateVec[0]);
                        node.setTranslateY(node.getTranslateY() + translateVec[1]);
                    }
                }
            }
            return returnList;
        }
        
        int thingsToBondWith = 0;
        for (int el : matrix[currRow]) {
            if (el != 0) 
                thingsToBondWith++;
        }
        Label label = new Label(atomList[currRow]);
        returnList.add(label);
        int amountFound = 0;
        int numBonds = 0;
        for(int i = 0; i < matrix.length; i++) {
            if(matrix[currRow][i] != 0) {
                numBonds = matrix[currRow][i];
                if(i != prevRow && !prevs.contains(i)) {
                    double[] translateLoop = new double[2];
                    switch(thingsToBondWith) {
                    case 2: // O - >O< - O
                        translateLoop = translateVec;
                        break;
                    //     O
                    //     |
                    //O - >O< - O  
                    case 3: 
                        if(amountFound == 0) {
                            translateLoop = new double[] {0, 100};
                        } else if(amountFound == 1) {
                            translateLoop = new double[] {100, 0};
                        }
                    default:
                        translateLoop = translateVec;
                        break;
                    }
                    // Add the lines
                    double translateModif = 0;
                    for(int j = 0; j < numBonds; j++) {
                        if(numBonds == 3) {
                            translateModif = j-1;
                        } 
                        else if (numBonds == 2) {
                            translateModif = j-0.5;
                        }
                        Rectangle rectangle = new Rectangle(Math.sqrt(Math.pow(translateLoop[0], 2) + Math.pow(translateLoop[1], 2)), 1);
                        rectangle.getTransforms().add(new Translate(0, 5 + translateModif*5));
                        returnList.add(rectangle);
                    }
                    ArrayList<Node> recursion = getRelativeLewis(i, currRow, translateLoop, prevs);

                    for (Node node : recursion) {
                        node.setTranslateX(node.getTranslateX() + translateLoop[0]);
                        node.setTranslateY(node.getTranslateY() + translateLoop[1]);
                        returnList.add(node);
                    }
                    
                    amountFound++;
                }
            }
        }
        return returnList;
    }
    
    private ArrayList<Node> doLoop2D(int[] loopIndices) {
        ArrayList<Node> returnedList = new ArrayList<>();
        LinkedList<Integer> previous = new LinkedList<>();
        
        for (int num : loopIndices)
            previous.add(num);
        
        double[] translateVec;
        double[] rectangleVec;
        double angleFromCenter = Math.toRadians(360/loopIndices.length);
        double angleAtCorners = Math.toRadians(180 - 360/loopIndices.length);
        
        double sideLen = BOND_SIZE/ Math.sqrt(2 - 2 * Math.cos(angleFromCenter));
        
        for(int i = 0; i < loopIndices.length; i++) {
            
            int numBonds = 0;
            if (i != loopIndices.length - 1)
                numBonds = matrix[loopIndices[i]][loopIndices[i + 1]];
            else 
                numBonds = matrix[loopIndices[i]][loopIndices[0]];
            
            int bondCount = 0;          //see how many lone pairs
            int thingsBondedCount = 0;  //get steric number

            for (int el : matrix[loopIndices[i]]) {
                if (el != 0) {
                    bondCount += el;
                    thingsBondedCount++;
                }
            }

            Label label = new Label(atomList[loopIndices[i]]);
            translateVec = new double[] {sideLen, 0, 0};
            translateVec = makeRoation(translateVec, 0, 0, angleFromCenter * i);
            label.setTranslateX(label.getTranslateX() + translateVec[0]);
            label.setTranslateY(label.getTranslateY() + translateVec[1]);
            
            // Add rectangle
            rectangleVec = new double[] {50, 0, 0};
            rectangleVec = makeRoation(rectangleVec, 0, 0, angleFromCenter * i);
            rectangleVec = makeRoation(rectangleVec, 0, 0, Math.PI - angleAtCorners/2);
            double translateModif = 0;
            for(int j = 0; j < numBonds; j++) {
                if(numBonds == 3) {
                    translateModif = j-1;
                } 
                else if (numBonds == 2) {
                    translateModif = j-0.5;
                }
                Rectangle rectangle = new Rectangle(Math.sqrt(Math.pow(translateVec[0], 2) + Math.pow(translateVec[1], 2)), 1);
                Translate moveToMidpoint = new Translate(translateVec[0] / 2, translateVec[1] / 2);
                Point3D diff = new Point3D(translateVec[0], translateVec[1], 0);
                Point3D axisOfRot = diff.crossProduct(new Point3D(0, 1, 0));
                double angle = Math.acos(diff.normalize().dotProduct(new Point3D(0, 1, 0)));
                Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRot);

                rectangle.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
                returnedList.add(rectangle);
            }
            
            //call the recursive fct to get everything else connected to this element
            //find everything connected to this atom
            int amountFound = 0;
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[loopIndices[i]][j] != 0 && !previous.contains(j)) {
                    numBonds = matrix[loopIndices[i]][j];
                    
                    double[] attackVec = {50, 0};
                    attackVec = makeRoation(attackVec, 0, 0, angleFromCenter * i);
                    
                    //if there is not only one other thing
                    if (thingsBondedCount != 3) {
                        double[] axis = getAxis(attackVec, new double[] {0, 0, 1});
                        attackVec = makeRot(attackVec, axis, (2 * Math.PI / 3) * (amountFound - 0.5));
                        amountFound++;
                    }
                    
                    translateModif = 0;
                    for(int k = 0; k < numBonds; k++) {
                        if(numBonds == 3) {
                            translateModif = k-1;
                        } 
                        else if (numBonds == 2) {
                            translateModif = k-0.5;
                        }
                        Rectangle rectangle = new Rectangle(Math.sqrt(Math.pow(translateVec[0], 2) + Math.pow(translateVec[1], 2)), 1);
                        Translate moveToMidpoint = new Translate(translateVec[0] / 2, translateVec[1] / 2);
                        Point3D diff = new Point3D(translateVec[0], translateVec[1], 0);
                        Point3D axisOfRot = diff.crossProduct(new Point3D(0, 1, 0));
                        double angle = Math.acos(diff.normalize().dotProduct(new Point3D(0, 1, 0)));
                        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRot);

                        rectangle.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
                        returnedList.add(rectangle);
                    }
                    
                    //call recursive fct
                    ArrayList<Node> recursion = getRelativeLewis(j, loopIndices[i], attackVec, previous);

                    for (Node node : recursion) {
                        node.setTranslateX(node.getTranslateX() + attackVec[0] + translateVec[0]);
                        node.setTranslateY(node.getTranslateY() + attackVec[1] + translateVec[1]);
                        returnedList.add(node);
                    }
                }
            }
            
            returnedList.add(label);
        }
        return returnedList;
    }
    
    /**
     * Method which sets up and displays the 3D representation of the chemical compound.
     * @param solution a solution matrix
     * @param atomList a list of non-metals
     * @param loop the indexes of the rows which are part of a loop
     */
    public void set3D(int [][] solution, String[] atomList, int[] loop) {
        matrix = solution;
        this.atomList = atomList;
        
        ArrayList<Node> finalList;
                
        if (loop.length != 0)
            finalList = doLoop(loop);
        else
            finalList = getRelativeLocation(0, -1, new double[] {BOND_SIZE, 0, 0}, new LinkedList<>());
        
        double[] transVec = {0, 0, 0};
        
        for (Node sphere : finalList) {
            sphere.setTranslateX(sphere.getTranslateX() + transVec[0]);
            sphere.setTranslateY(sphere.getTranslateY() + transVec[1]);
            sphere.setTranslateZ(sphere.getTranslateZ() + transVec[2]);
        }        
        
        atomGroup.getChildren().addAll(finalList);
        realView.setRoot(atomGroup);
    }
    
    /**
     * Method which builds the 3D representation of the chemical compound.
     * @param currRow the current row
     * @param prevRow the row from which this method was called
     * @param vec the attack vector
     * @param prevs the list of all the previous rows
     * @return an ArrayList of Nodes (Spheres and Cylinders)
     */
    public ArrayList<Node> getRelativeLocation(int currRow, int prevRow, double[] vec, LinkedList<Integer> prevs) {
        prevs.add(currRow);
        
        ArrayList<Node> returnList = new ArrayList<>();
        
        if (atomList[currRow].equals("H")) {
            Sphere temp = new Sphere(40);
            temp.setMaterial(new PhongMaterial(Color.web(atoms[0].getColor())));
            temp.setId("H");
            addHover(temp);
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
                break;
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
        boolean isFirst = prevRow == -1 && amountFound == 0;
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[currRow][i] != 0) {
                temp.setId(atomList[currRow]);
                addHover(temp);
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
                            double[] rotAxis = getAxis(axis, new double[] {0, 0, 1});
                            transVec = makeRot(vec, rotAxis, -rads);
                            switch (amountFound) {
                                // \ -> /
                                case 1:
                                    transVec = makeRot(transVec, axis, Math.toRadians(120));
                                    break;
                                // / -> |
                                case 2:
                                    transVec = makeRot(transVec, axis, Math.toRadians(240));
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    
                    //if first element, must make special case,
                    //update the transVec to be simply where you "came from"
                    if (isFirst) {
                        transVec = makeRoation(vec, Math.PI, Math.PI, Math.PI);
                        isFirst = false;
                        amountFound--;
                    }
                    
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
        
        returnAxis[0] = a[1] * b[2] - a[2] * b[1];
        returnAxis[1] = a[2] * b[0] - a[0] * b[2];
        returnAxis[2] = a[0] * b[1] - a[1] * b[0];
        
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
    /**
     * Method which rotates a vector about a given axis.
     * @param input the vector which gets rotated
     * @param axis the axis about which you make a rotation
     * @param rad the angle by which you rotate (in radians)
     * @return the modified input vector
     */
    double[] makeRot(double[] input, double[] axis, double rad) {
//        double[][] W = {{0, -axis[2], axis[1]}, {axis[2], 0, -axis[0]}, {-axis[1], axis[0], 0}};
//        double[][] I = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
//        
//        double[][] result = new double[3][3];
//        
//        double sin = Math.sin(rad);
//        double cos = Math.cos(rad);
//        
//        double[][] W2 = new double[3][3];
//        
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                int sum = 0;
//                for (int k = 0; k < 3; k++) {
//                    sum += W[i][k] * W[k][j];
//                }
//                
//                W2[i][j] = sum;
//            }
//        }
//        
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                result[i][j] = I[i][j] + sin * W[i][j] + (1 - cos) * W2[i][j];
//            }
//        }
//        
//        double[] returnVec = new double[3];
//        
//        for (int i = 0 ; i < 3; i++) {
//            double sum = 0;
//            
//            for (int j = 0; j < 3; j++) {
//                sum += result[i][j] * input[j];
//            }
//            
//            returnVec[i] = sum;
//        }
//        
//        return returnVec;

        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        
        double[][] result = new double[3][3];
        
        result[0][0] = cos + axis[0] * axis[0] * (1 - cos);
        result[0][1] = axis[0] * axis[1] * (1 - cos) - axis[2] * sin;
        result[0][2] = axis[0] * axis[2] * (1 - cos) + axis[1] * sin;
        
        result[1][0] = axis[1] * axis[0] * (1 - cos) + axis[2] * sin;
        result[1][1] = cos + axis[1] * axis[1] * ( 1- cos);
        result[1][2] = axis[1] * axis[2] * (1 - cos) - axis[0] * sin;
        
        result[2][0] = axis[2] * axis[0] * (1 - cos) - axis[1] * sin;
        result[2][1] = axis[2] * axis[1] * (1 - cos) + axis[0] * sin;
        result[2][2] = cos + axis[2] * axis[2] * (1 - cos);
        
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
    
    /**
     * Method which rotates an inputted vector by the X axis, then the Y axis, and finally by the Z axis.
     * @param input the inputted vector
     * @param x the angle (in radians) by which the vector gets rotated about the X axis 
     * @param y the angle (in radians) by which the vector gets rotated about the Y axis 
     * @param z the angle (in radians) by which the vector gets rotated about the Z axis 
     * @return the modified input vector
     */
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
        bindAnchor.getChildren().get(0).requestFocus();
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
    
    /**
     * Handler which allows for the detection of the initial mouse press.
     * This allows the mouse drag handler to work properly.
     */
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
            
    /**
     * Handler which allows for the rotation and translation of the 3D group of atoms.
     */
    @FXML
    public void handleMouseDrag(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            atomGroup.translateXProperty().set(event.getX() - initX + startTransX);
            atomGroup.translateYProperty().set(event.getY() - initY + startTransY);
        }
        else if (event.getButton() == MouseButton.PRIMARY) {

            Rotate rotX = new Rotate((event.getX() - initXAng) * 360/ 200, new Point3D(0, 1, 0));
            Rotate rotY = new Rotate((event.getY() - initYAng) * 360/ 200, new Point3D(1, 0, 0));
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
    
    /**
     * Method which places a cylinder and properly orients it based on the vector provided.
     * @param transVec the translate vector by which the cylinder orients itself
     * @return the Cylinder
     */
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
    
    /**
     * Method which creates the 3D structure if there's a loop inside of the solution.
     * @param loopIndices the indexes of the rows which are part of a loop
     * @return an ArrayList of Nodes (Spheres and Cylinders)
     */
    private ArrayList<Node> doLoop(int[] loopIndices) {
        
        ArrayList<Node> returnedList = new ArrayList<>();
        LinkedList<Integer> previous = new LinkedList<>();
        
        for (int num : loopIndices)
            previous.add(num);
        
        double[] transVec;
        double[] cylVec;
        double angleFromCenter = Math.toRadians(360/loopIndices.length);
        double angleAtCorners = Math.toRadians(180 - 360/loopIndices.length);
        
        double sideLen = BOND_SIZE/ Math.sqrt(2 - 2 * Math.cos(angleFromCenter));
        
        for(int i = 0; i < loopIndices.length; i++) {
            
            int numBonds = 0;
            if (i != loopIndices.length - 1)
                numBonds = matrix[loopIndices[i]][loopIndices[i + 1]];
            else 
                numBonds = matrix[loopIndices[i]][loopIndices[0]];
            
            int bondCount = 0;          //see how many lone pairs
            int thingsBondedCount = 0;  //get steric number

            for (int el : matrix[loopIndices[i]]) {
                if (el != 0) {
                    bondCount += el;
                    thingsBondedCount++;
                }
            }

            String color = null;
            int formalCharge = bondCount;
            for (int j = 0; j < atoms.length; j++) {
                if (atomList[loopIndices[i]].equals(atoms[j].getSymbol()))
                {
                    color = atoms[j].getColor();
                    formalCharge -= j > 2 ? 8 - atoms[j].getShells() : 2 - atoms[j].getShells();
                    break;
                }
            }
            
            Sphere tempSphere = new Sphere(50);
            tempSphere.setId(atomList[i]);
            addHover(tempSphere);
            tempSphere.setMaterial(new PhongMaterial(Color.web(color)));
            
            transVec = new double[] {sideLen, 0, 0};
            transVec = makeRoation(transVec, 0, 0, angleFromCenter * i);
            tempSphere.setTranslateX(tempSphere.getTranslateX() + transVec[0]);
            tempSphere.setTranslateY(tempSphere.getTranslateY() + transVec[1]);
            tempSphere.setTranslateZ(tempSphere.getTranslateZ() + transVec[2]);
            
            //if theres formal charge, add a label
            if (formalCharge != 0) {
                Label label = new Label("" + formalCharge);
                returnedList.add(label);
                label.setTranslateX(transVec[0] + 30);
                label.setTranslateY(transVec[1] - 60);
                label.setTranslateZ(transVec[2] - 30);
                label.setFont(new Font(40));
            }
            
            //add cylinder
            cylVec = new double[] {BOND_SIZE, 0, 0};
            cylVec = makeRoation(cylVec, 0, 0, angleFromCenter * i);
            cylVec = makeRoation(cylVec, 0, 0, Math.PI - angleAtCorners/2);
            double translateModif = 0;
            for(int j = 0; j < numBonds; j++) {
                if(numBonds == 3) {
                    translateModif = j-1;
                } 
                else if (numBonds == 2) {
                    translateModif = j-0.5;
                }
                Cylinder cylinder = getCylinder(cylVec);
                cylinder.setTranslateX(cylinder.getTranslateX() + transVec[0]);
                cylinder.setTranslateY(cylinder.getTranslateY() + transVec[1]);
                cylinder.setTranslateZ(cylinder.getTranslateZ() + transVec[2]);
                cylinder.getTransforms().add(new Translate(translateModif*transVec[0]/3, translateModif*transVec[1] * -1/4, translateModif*transVec[2]/6));
                returnedList.add(cylinder);
            }
            
            //call the recursive fct to get everything else connected to this sphere
            //find everything connected to this atom
            int amountFound = 0;
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[loopIndices[i]][j] != 0 && !previous.contains(j)) {
                    numBonds = matrix[loopIndices[i]][j];
                    
                    double[] attackVec = {BOND_SIZE, 0, 0};
                    attackVec = makeRoation(attackVec, 0, 0, angleFromCenter * i);
                    
                    //if there is not only one other thing
                    if (thingsBondedCount != 3) {
                        double[] axis = getAxis(attackVec, new double[] {0, 0, 1});
                        attackVec = makeRot(attackVec, axis, (2 * Math.PI / 3) * (amountFound - 0.5));
                        amountFound++;
                    }
                    
                    translateModif = 0;
                    for(int k = 0; k < numBonds; k++) {
                        if(numBonds == 3) {
                            translateModif = k-1;
                        } 
                        else if (numBonds == 2) {
                            translateModif = k-0.5;
                        }
                        Cylinder cylinder = getCylinder(attackVec);
                        cylinder.setTranslateX(cylinder.getTranslateX() + transVec[0]);
                        cylinder.setTranslateY(cylinder.getTranslateY() + transVec[1]);
                        cylinder.setTranslateZ(cylinder.getTranslateZ() + transVec[2]);
                        cylinder.getTransforms().add(new Translate(translateModif*attackVec[0]/3, translateModif*attackVec[1] * -1/4, translateModif*attackVec[2]/6));
                        returnedList.add(cylinder);
                    }
                    
                    //call recursive fct
                    ArrayList<Node> recursion = getRelativeLocation(j, loopIndices[i], attackVec, previous);

                    for (Node sphere : recursion) {
                        sphere.setTranslateX(sphere.getTranslateX() + attackVec[0] + transVec[0]);
                        sphere.setTranslateY(sphere.getTranslateY() + attackVec[1] + transVec[1]);
                        sphere.setTranslateZ(sphere.getTranslateZ() + attackVec[2] + transVec[2]);
                        returnedList.add(sphere);
                    }
                }
            }
            
            returnedList.add(tempSphere);
        }
        return returnedList;
    }
    
    public WritableImage screenShotThreeD() {
        SnapshotParameters spa = new SnapshotParameters();
        spa.setTransform(Transform.scale(2, 2));
        return realView.snapshot(spa, null);
    }
    
    public WritableImage screenShotLewis() {
        SnapshotParameters spa = new SnapshotParameters();
        spa.setTransform(Transform.scale(2, 2));
        return lewisPane.snapshot(spa, null);
    }
    
    public void addHover(Node s) {
        s.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                label = new Label();
                label.setText(s.getId());
                label.setFont(new Font(20));
                label.setTranslateY(realView.getHeight()/3);
                bindAnchor.getChildren().add(label);
            }
        });
        s.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bindAnchor.getChildren().remove(label);
            }
        });
    }
}
