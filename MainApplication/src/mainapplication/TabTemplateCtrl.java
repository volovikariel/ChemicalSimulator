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
import javafx.geometry.Pos;
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
import javafx.util.Pair;

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
    
    @FXML
    Label lblScore;

    final static int BOND_SIZE = 125;
    final static int LEWIS_BOND_SIZE = 40;
    final static int LEWIS_OFFSET = 20;

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
    private Group lewisGroup = new Group();
    
    private Group covalentGroup3D;
    private Group covalentGroupLewis;
    
    private Group currSelectedGroup = null;
    private Group movingGroup = null;

    /**
     * [NOT FINISHED DOC]
     * Method which sets up the Lewis structure for a single solution.
     * @param solution the solution matrix
     * @param atomList
     */
    public static Group setLewis(int [][] solution, String[] atomList, int[] loops) {
        Group returnGroup;
        ArrayList<Node> finalList;

        if (loops.length != 0)
            finalList = doLoop2D(loops, solution, atomList);
        else
            finalList = getRelativeLewis(0, -1, new double[] {LEWIS_BOND_SIZE + 2 * LEWIS_OFFSET, 0, 0},new LinkedList<>(), solution, atomList);

        
        returnGroup = new Group(finalList);
        
        return returnGroup;
        
        //System.out.println("Final List Lewis: " + finalList);
    }

    public static ArrayList<Node> getRelativeLewis(int currRow, int prevRow, double[] translateVec, LinkedList<Integer> prevs, int[][] matrix, String[] atomList) {
        prevs.add(currRow);
        Atom[] atoms = MainAppCtrl.getAtoms();

        ArrayList<Node> returnList = new ArrayList<>();

        if(atomList[currRow].equals("H")) {
            Label temp = new Label("H");
            temp.setFont(new Font(40));
            temp.setAlignment(Pos.CENTER);
            returnList.add(temp);            

            if(prevRow != -1)
                return returnList;
            
            if (matrix.length == 1) {
                Label chargelbl = new Label("1");
                returnList.add(chargelbl);
                chargelbl.setTranslateX(30);
                chargelbl.setTranslateY(0);
                chargelbl.setFont(new Font(20));
            }

            for(int i = 0; i < matrix.length; i++) {
                if(matrix[currRow][i] != 0) {
                    // Add rectangle
                    double[] rectangleVec = new double[] {1, 0, 0};
                    Point3D offsetVec = new Point3D(rectangleVec[0], rectangleVec[1], rectangleVec[2]);
                    
                    Rectangle rectangle = getRectangle(rectangleVec);

                    //calculate offset -> seperation from center + center of label
                    rectangle.setTranslateX(rectangle.getTranslateX() + LEWIS_OFFSET * offsetVec.getX() + 20);
                    rectangle.setTranslateY(rectangle.getTranslateY() + LEWIS_OFFSET * offsetVec.getY() + 30);
                    
                    returnList.add(rectangle);
                    
                    ArrayList<Node> recursiveCall = getRelativeLewis(i, currRow, translateVec, prevs, matrix, atomList);
                    
                    for(Node node : recursiveCall) {
                        node.setTranslateX(node.getTranslateX() + translateVec[0]);
                        node.setTranslateY(node.getTranslateY() + translateVec[1]);
                        returnList.add(node);
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

        String color = null;
        int formalCharge = bondCount;
        int lonePairs = 0;
        //int atomicNumber = 0;
        for (int j = 0; j < atoms.length; j++) {
            if (atomList[currRow].equals(atoms[j].getSymbol()))
            {
                color = atoms[j].getColor();
                lonePairs = j + 1 <= 10 ? 4 - bondCount : (int) Math.ceil((atoms[j].getShells() - bondCount) / 2.0);
                formalCharge = atoms[j].getShells() - 2 * lonePairs - bondCount;
                break;
            }
        }
        
        Label label = new Label(atomList[currRow]);
        label.setFont(new Font(40));
        label.setAlignment(Pos.CENTER);
        returnList.add(label);
        
        //if theres formal charge, add a label
        if (formalCharge != 0) {
            Label chargelbl = new Label("" + formalCharge);
            returnList.add(chargelbl);
            chargelbl.setTranslateX(30);
            chargelbl.setTranslateY(0);
            chargelbl.setFont(new Font(20));
        }
        
        int amountFound = 0;
        int numBonds = 0;
        for(int i = 0; i < matrix.length; i++) {
            if(matrix[currRow][i] != 0) {
                numBonds = matrix[currRow][i];
                if(i != prevRow && !prevs.contains(i)) {
                    double[] translateLoop = translateVec;
                    double angle = Math.PI + (amountFound + 1) * 2 * Math.PI / thingsBondedCount;
                    translateLoop = makeRoation(translateLoop, 0, 0, angle);
                   
                    // Add rectangle
                    Point3D offsetVec = (new Point3D(translateLoop[0], translateLoop[1], translateLoop[2])).normalize();
                    double translateModif = 0;
                    for(int k = 0; k < numBonds; k++) {
                        translateModif = k - (numBonds - 1) / 2.0;
                        
                        Rectangle rectangle = getRectangle(translateLoop);

                        //calculate offset -> seperation from center + center of label
                        rectangle.setTranslateX(rectangle.getTranslateX() + LEWIS_OFFSET * offsetVec.getX() + 20);
                        rectangle.setTranslateY(rectangle.getTranslateY() + LEWIS_OFFSET * offsetVec.getY() + 30);
                        rectangle.getTransforms().add(new Translate(10 * translateModif, 0));
                        returnList.add(rectangle);
                    }
                    
                    ArrayList<Node> recursion = getRelativeLewis(i, currRow, translateLoop, prevs, matrix, atomList);

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

    private static ArrayList<Node> doLoop2D(int[] loopIndices, int[][] matrix, String[] atomList) {
        Atom[] atoms = MainAppCtrl.getAtoms();
        
        ArrayList<Node> returnedList = new ArrayList<>();
        LinkedList<Integer> previous = new LinkedList<>();

        for (int num : loopIndices)
            previous.add(num);

        double[] translateVec;
        double[] rectangleVec;
        double angleFromCenter = Math.toRadians(360/loopIndices.length);
        double angleAtCorners = Math.PI - angleFromCenter;

        double sideLen = (LEWIS_BOND_SIZE + 2 * LEWIS_OFFSET)/ Math.sqrt(2 - 2 * Math.cos(angleFromCenter));

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
            int lonePairs = 0;
            //int atomicNumber = 0;
            for (int j = 0; j < atoms.length; j++) {
                if (atomList[loopIndices[i]].equals(atoms[j].getSymbol()))
                {
                    color = atoms[j].getColor();
                    lonePairs = j + 1 <= 10 ? 4 - bondCount : (int) Math.ceil((atoms[j].getShells() - bondCount) / 2.0);
                    formalCharge = atoms[j].getShells() - 2 * lonePairs - bondCount;
                    break;
                }
            }

            Label lblLetter = new Label(atomList[loopIndices[i]]);
            lblLetter.setFont(new Font(40));
            lblLetter.setAlignment(Pos.CENTER);

            returnedList.add(lblLetter);

            translateVec = new double[] {sideLen, 0, 0};
            translateVec = makeRoation(translateVec, 0, 0, angleFromCenter * i);
            lblLetter.setTranslateX(lblLetter.getTranslateX() + translateVec[0]);
            lblLetter.setTranslateY(lblLetter.getTranslateY() + translateVec[1]);

            //if theres formal charge, add a label
            if (formalCharge != 0) {
                Label label = new Label("" + formalCharge);
                returnedList.add(label);
                label.setTranslateX(translateVec[0] + 20);
                label.setTranslateY(translateVec[1] - 10);
                label.setFont(new Font(20));
            }

            // Add rectangle
            rectangleVec = new double[] {LEWIS_BOND_SIZE, 0, 0};
            rectangleVec = makeRoation(rectangleVec, 0, 0, angleFromCenter * i);
            rectangleVec = makeRoation(rectangleVec, 0, 0, Math.PI - angleAtCorners/2);
            Point3D offsetVec = (new Point3D(rectangleVec[0], rectangleVec[1], rectangleVec[2])).normalize();
            double translateModif = 0;
            for(int j = 0; j < numBonds; j++) {
                translateModif = j - (numBonds - 1) / 2.0;
                
                Rectangle rectangle = getRectangle(rectangleVec);
                
                //calculate offset -> location + seperation from center + center of label
                rectangle.setTranslateX(rectangle.getTranslateX() + translateVec[0] + LEWIS_OFFSET * offsetVec.getX() + 20);
                rectangle.setTranslateY(rectangle.getTranslateY() + translateVec[1] + LEWIS_OFFSET * offsetVec.getY() + 30);
                rectangle.getTransforms().add(new Translate(10 * translateModif, 0));
                returnedList.add(rectangle);
            }

            //call the recursive fct to get everything else connected to this element
            //find everything connected to this atom
            int amountFound = 0;
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[loopIndices[i]][j] != 0 && !previous.contains(j)) {
                    numBonds = matrix[loopIndices[i]][j];

                    double[] attackVec = {LEWIS_BOND_SIZE + 2 * LEWIS_OFFSET, 0, 0};
                    attackVec = makeRoation(attackVec, 0, 0, angleFromCenter * i);

                    //if there is not only one other thing
                    if (thingsBondedCount != 3) {
                        double alpha = (2 * Math.PI - angleAtCorners) / (thingsBondedCount - 1);
                        double angle = 0;
                        
                        angle = - alpha * (thingsBondedCount - 1) / 2.0;
                            
                        angle += alpha * (amountFound + 1);
                        attackVec = makeRoation(attackVec, 0, 0, angle);
                        amountFound++;
                    }
                    
                    // Add rectangle
                    offsetVec = (new Point3D(attackVec[0], attackVec[1], attackVec[2])).normalize();
                    translateModif = 0;
                    for(int k = 0; k < numBonds; k++) {
                        translateModif = k - (numBonds - 1) / 2.0;
                        
                        Rectangle rectangle = getRectangle(attackVec);

                        //calculate offset -> location + seperation from center + center of label
                        rectangle.setTranslateX(rectangle.getTranslateX() + translateVec[0] + LEWIS_OFFSET * offsetVec.getX() + 20);
                        rectangle.setTranslateY(rectangle.getTranslateY() + translateVec[1] + LEWIS_OFFSET * offsetVec.getY() + 30);
                        rectangle.getTransforms().add(new Translate(10 * translateModif, 0));
                        returnedList.add(rectangle);
                    }

                    //call recursive fct
                    ArrayList<Node> recursion = getRelativeLewis(j, loopIndices[i], attackVec, previous, matrix, atomList);

                    for (Node node : recursion) {
                        node.setTranslateX(node.getTranslateX() + attackVec[0] + translateVec[0]);
                        node.setTranslateY(node.getTranslateY() + attackVec[1] + translateVec[1]);
                        returnedList.add(node);
                    }
                }
            }
        }
        return returnedList;
    }

    /**
     * Method which sets up and displays the 3D representation of the chemical compound.
     * @param solution a solution matrix
     * @param atomList a list of non-metals
     * @param loop the indexes of the rows which are part of a loop
     */
    public static Group set3D(int [][] solution, String[] atomList, int[] loop) {
        Group returnGroup = new Group();

        ArrayList<Node> finalList;

        if (loop.length != 0)
            finalList = doLoop(loop, solution, atomList);
        else
            finalList = getRelativeLocation(0, -1, new double[] {BOND_SIZE, 0, 0}, new LinkedList<>(), solution, atomList);

        returnGroup.getChildren().addAll(finalList);
        
        return returnGroup;
    }
    
    public static Pair<Group, Group> loadGroups(int [][] solution, String[] atomList, int[] loop) {
        Group group3D = set3D(solution, atomList, loop);
        group3D.setId("Cov");
        
        Group groupLewis = setLewis(solution, atomList, loop);
        groupLewis.setId("Cov");
        
        return new Pair<>(group3D, groupLewis);
    }
    
    public static Pair<Group, Group> doAll(int [][] solution, String[] atomList, int[] loop, Atom[] metalList) {
        Pair<Group, Group> tempCovalent;
        if (solution.length != 0)   
            tempCovalent = loadGroups(solution, atomList, loop);
        else
            tempCovalent = new Pair<>(new Group(), new Group());
        
        Group ionsLewis = getIonsLewis(metalList);
        Group ions3D = getIons3d(metalList);

        Group lewis = new Group(tempCovalent.getValue());
        lewis.getChildren().addAll(ionsLewis.getChildren());
        Group model3d = new Group(tempCovalent.getKey());
        
        // Adding the ions in their respective positions
        Group ions3DTranslated = new Group();
        int numIons = ions3D.getChildren().size();
        double angle = Math.toRadians(360/numIons);
        double translation = Math.sqrt((ions3D.getBoundsInParent().getWidth() * ions3D.getBoundsInParent().getWidth())/4 + (ions3D.getBoundsInParent().getHeight() * ions3D.getBoundsInParent().getHeight())/4);
        for(Node ionGroup : ions3D.getChildren()) {
            if(ionGroup instanceof Group) {
                for(int i = 0; i < ((Group) ionGroup).getChildren().size(); i++) {
                    Node node = ((Group) ionGroup).getChildren().get(i);
                    if(node instanceof Sphere) {
                        node.setTranslateX((Math.sin(angle) * i) * translation);
                        node.setTranslateY((Math.cos(angle) * i) * translation);
                    }
                }
            }
        }
        
        
        model3d.getChildren().addAll(ions3D.getChildren());
        Pair<Group, Group> listGroups = new Pair<>(model3d, lewis);
        
        return listGroups;
    }
    
    public void setNodes(Pair<Group, Group> groups) {
        atomGroup = groups.getKey();
        lewisGroup = groups.getValue();
        
        //add the hovers
        for (Node node : atomGroup.getChildren()) {
            if (node instanceof Sphere)
                addHover((Sphere) node);
            else if (node instanceof Group) {
                for (Node node2 : ((Group) node).getChildren())
                    if (node2 instanceof Sphere)
                        addHover((Sphere) node2);
            
                if (node.getId() != null && node.getId().equals("Cov"))
                    covalentGroup3D = (Group) node;
            }
        }
        
        for (Node node : lewisGroup.getChildren()) {
            if (node instanceof Label)
                addHoverLewis(node);
            if (node instanceof Group) {
                if (node.getId() != null && node.getId().equals("Cov"))
                    covalentGroupLewis = (Group) node;
                
                for (Node node2 : ((Group) node).getChildren())
                    if (node2 instanceof Label)
                        addHoverLewis(node2);
            }
        }
        
        realView.setRoot(atomGroup);
        
        lewisPane.getChildren().add(lewisGroup);
        
        atomGroup.layoutXProperty().bind(realView.widthProperty().divide(2));
        atomGroup.layoutYProperty().bind(realView.heightProperty().divide(2));
        lewisGroup.layoutXProperty().bind(lewisPane.widthProperty().divide(2));
        lewisGroup.layoutYProperty().bind(lewisPane.heightProperty().divide(2));
    }
    
    public static Group getIons3d(Atom[] metalAtoms) {
        Group returnGroup = new Group();
        
        //Must create a sphere for each ion element
        for (Atom atom : metalAtoms) {
            Group ion = new Group();
            Sphere temp = new Sphere(50);
            temp.setMaterial(new PhongMaterial(Color.web(atom.getColor())));
            temp.setId(atom.getSymbol());
            
            ion.getChildren().addAll(temp);
            returnGroup.getChildren().add(ion);
        }
        
        return returnGroup;
    }
    
    public static Group getIonsLewis(Atom[] metalAtoms) {
        Group returnGroup = new Group();
        
        //Must create a label for each ion element
        for (Atom atom : metalAtoms) {
            Group ion = new Group();
            Label temp = new Label(atom.getSymbol());
            temp.setFont(new Font(40));
            
            ion.getChildren().addAll(temp);
            returnGroup.getChildren().add(ion);
        }
        
        return returnGroup;
    }

    /**
     * Method which builds the 3D representation of the chemical compound.
     * @param currRow the current row
     * @param prevRow the row from which this method was called
     * @param vec the attack vector
     * @param prevs the list of all the previous rows
     * @return an ArrayList of Nodes (Spheres and Cylinders)
     */
    public static ArrayList<Node> getRelativeLocation(int currRow, int prevRow, double[] vec, LinkedList<Integer> prevs, int[][] matrix, String[] atomList) {
        prevs.add(currRow);
        
        Atom[] atoms = MainAppCtrl.getAtoms();

        ArrayList<Node> returnList = new ArrayList<>();

        if (atomList[currRow].equals("H")) {
            Sphere temp = new Sphere(40);
            temp.setMaterial(new PhongMaterial(Color.web(atoms[0].getColor())));
            temp.setId("H");
            returnList.add(temp);

            if (prevRow != -1) {
                return returnList;
            }
            
            //if there is only one
            if (atomList.length == 1) {
                Label label = new Label("1");
                returnList.add(label);
                label.setTranslateX(30);
                label.setTranslateY(-60);
                label.setTranslateZ(-30);
                label.setFont(new Font(40));
            }

            for (int i = 0; i < matrix.length; i++) {
                if (matrix[currRow][i] != 0) {
                    double[] transVec = {BOND_SIZE, 0, 0};
                    Cylinder bond = getCylinder(transVec);
                    returnList.add(bond);
                    ArrayList<Node> recursion = getRelativeLocation(i, currRow, transVec, prevs, matrix, atomList);

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


        String color = null;
        int formalCharge = bondCount;
        int lonePairs = 0;
        //int atomicNumber = 0;
        for (int i = 0; i < atoms.length; i++) {
            if (atomList[currRow].equals(atoms[i].getSymbol()))
            {
                //atomicNumber = i + 1;
                color = atoms[i].getColor();
                //formalCharge -= i > 2 ? 8 - atoms[i].getShells() : 2 - atoms[i].getShells();
                lonePairs = i + 1 <= 10 ? 4 - bondCount : (int) Math.ceil((atoms[i].getShells() - bondCount) / 2.0);
                formalCharge = atoms[i].getShells() - 2 * lonePairs - bondCount;
                break;
            }
        }
        //lonePairs = 4 - bondCount;
        int stericNumber = thingsBondedCount + lonePairs;

        Sphere temp = new Sphere(50);
        temp.setMaterial(new PhongMaterial(Color.web(color)));
        temp.setId(atomList[currRow]);
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
                numBonds = matrix[currRow][i];
                if (i != prevRow && !prevs.contains(i)) {
                    double[] transVec = getTransVec(stericNumber, amountFound, vec);

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
                        translateModif = j - (numBonds - 1) / 2.0;
                        
                        Cylinder bond = getCylinder(transVec);
                        //bond.getTransforms().add(new Translate(translateModif*transVec[0]/3, translateModif*transVec[1] * -1/4, translateModif*transVec[2]/6));
                        bond.getTransforms().add(new Translate(20 * translateModif, 0, 0));
                        returnList.add(bond);
                    }
                    ArrayList<Node> recursion = getRelativeLocation(i, currRow, transVec, prevs, matrix, atomList);

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

    static double[] getAxis(double[] a, double[] b) {
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

    static double[] normalize(double [] a) {
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
    static double[] makeRot(double[] input, double[] axis, double rad) {
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
    static double[] makeRoation(double[] input, double x, double y, double z) {
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
        // Handle the scrolling for 2D
        lewisPane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                if(event.getDeltaY() > 0) {
                    lewisGroup.scaleXProperty().set(lewisGroup.getScaleX() * 1.25);
                    lewisGroup.scaleYProperty().set(lewisGroup.getScaleY() * 1.25);
                    lewisGroup.scaleZProperty().set(lewisGroup.getScaleZ() * 1.25);
                }
                else {
                    lewisGroup.scaleXProperty().set(lewisGroup.getScaleX() * 0.75);
                    lewisGroup.scaleYProperty().set(lewisGroup.getScaleY() * 0.75);
                    lewisGroup.scaleZProperty().set(lewisGroup.getScaleZ() * 0.75);
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

            movingGroup = currSelectedGroup;
            if (movingGroup == null)
                movingGroup = atomGroup;

            startTransX = movingGroup.translateXProperty().get();
            startTransY = movingGroup.translateYProperty().get();
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
            movingGroup.translateXProperty().set(event.getX() - initX + startTransX);
            movingGroup.translateYProperty().set(event.getY() - initY + startTransY);
        }
        else if (event.getButton() == MouseButton.PRIMARY) {

            Rotate rotX = new Rotate((event.getX() - initXAng) * 360/ 200, new Point3D(0, 1, 0));
            Rotate rotY = new Rotate((event.getY() - initYAng) * 360/ 200, new Point3D(1, 0, 0));
            if (covalentGroup3D != null)
                covalentGroup3D.getTransforms().addAll(rotX, rotY);

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
    
    @FXML
    public void handleMouseClickLewis(MouseEvent event) {
        initX = event.getX();
        initY = event.getY();
        
        movingGroup = currSelectedGroup;
        if (movingGroup == null)
            movingGroup = lewisGroup;
        
        startTransX = movingGroup.translateXProperty().get();
        startTransY = movingGroup.translateYProperty().get();
    }

    @FXML
    public void handleMouseDragLewis(MouseEvent event) {
        movingGroup.translateXProperty().set(event.getX() - initX + startTransX);
        movingGroup.translateYProperty().set(event.getY() - initY + startTransY);
    }

    /**
     * Method which places a cylinder and properly orients it based on the vector provided.
     * @param transVec the translate vector by which the cylinder orients itself
     * @return the Cylinder
     */
    private static Cylinder getCylinder(double[] transVec) {
        double length = 0;
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
    private static ArrayList<Node> doLoop(int[] loopIndices, int[][] matrix, String[] atomList) {
        Atom[] atoms = MainAppCtrl.getAtoms();

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
            int lonePairs = 0;
            //int atomicNumber = 0;
            for (int j = 0; j < atoms.length; j++) {
                if (atomList[loopIndices[i]].equals(atoms[j].getSymbol()))
                {
                    color = atoms[j].getColor();
                    lonePairs = j + 1 <= 10 ? 4 - bondCount : (int) Math.ceil((atoms[j].getShells() - bondCount) / 2.0);
                    formalCharge = atoms[j].getShells() - 2 * lonePairs - bondCount;
                    break;
                }
            }

            Sphere tempSphere = new Sphere(50);
            tempSphere.setId(atomList[i]);
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
                translateModif = j - (numBonds - 1) / 2.0;
                        
                Cylinder cylinder = getCylinder(cylVec);
                cylinder.setTranslateX(cylinder.getTranslateX() + transVec[0]);
                cylinder.setTranslateY(cylinder.getTranslateY() + transVec[1]);
                cylinder.setTranslateZ(cylinder.getTranslateZ() + transVec[2]);
                //cylinder.getTransforms().add(new Translate(translateModif*transVec[0]/3, translateModif*transVec[1]/3, translateModif*transVec[2]/3));
                cylinder.getTransforms().add(new Translate(20 * translateModif, 0, 0));
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
                        //cylinder.getTransforms().add(new Translate(translateModif*attackVec[0]/3, translateModif*attackVec[1] * -1/4, translateModif*attackVec[2]/6));
                        cylinder.getTransforms().add(new Translate(20 * translateModif, 0, 0));
                        returnedList.add(cylinder);
                    }

                    //call recursive fct
                    ArrayList<Node> recursion = getRelativeLocation(j, loopIndices[i], attackVec, previous, matrix, atomList);

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
                currSelectedGroup = (Group) s.getParent();
            }
        });
        s.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bindAnchor.getChildren().remove(label);
                currSelectedGroup = null;
            }
        });
    }

    static double[] getTransVec(int steric, int count, double[] attackVec) {
        double[] transVec = attackVec;

        double[] axis = normalize(attackVec);

        switch (steric) {
            case 2:                     //This is the linear case, same as input vec
                break;
            case 3:                     //This is the trigonal planar case
                transVec = makeRoation(attackVec, 0, 0, Math.PI / 3 + count * 4 * Math.PI / 3);
                break;
            case 4:                     //This is the tetrahedral case
                //gets the first case
                double rads = Math.toRadians(180 - 109.5);
                double[] rotAxis = getAxis(axis, new double[] {0, 0, 1});
                transVec = makeRot(attackVec, rotAxis, -rads);
                //modifies the first case depending on how many
                transVec = makeRot(transVec, axis, Math.toRadians(count * 120));
                break;
            case 5:                     //This is the trigonal bipyramidal case
                //the first case is just across, so same vec
                if (count == 0)
                    return transVec;

                //otherwise must make trigonal planar 90 degress from input
                double[] firstAxis = getAxis(axis, new double[] {0, 0, 1});
                transVec = makeRot(attackVec, firstAxis, -Math.PI / 2.0);
                //must get a turn 0, 120, 240
                transVec = makeRot(transVec, axis, Math.toRadians((count - 1) * 120));
                break;
            case 6:                     //This is the Octohedral case
                //the first case is just across, so same vec
                if (count == 0)
                    return transVec;

                //otherwise must make a square by making 2 opposites then 2 more opposites
                double[] firstTrunAxis = getAxis(axis, new double[] {0, 0, 1});
                transVec = makeRot(attackVec, firstTrunAxis, -Math.PI / 2.0);
                //must get a turn 0, 180, 90, 270
                transVec = makeRot(transVec, axis, Math.toRadians((count - 1) * 180 + ((count - 1) / 2) * 90));
                break;
            case 7:                     //This is the pentagonal bipyramidal case
                //the last case is just across, so same vec
                if (count == 6)
                    return transVec;

                //otherwise must make a square by making 2 opposites then 2 more opposites
                double[] firstTurnAxis = getAxis(axis, new double[] {0, 0, 1});
                transVec = makeRot(attackVec, firstTurnAxis, -Math.PI / 2.0);
                //must get a turn 0, 180, 90, 270
                transVec = makeRot(transVec, axis, Math.toRadians(count * 72));
                break;
            case 1:                     //This is a useless case since it will deal with it later
                break;
            default:                    //This means an invalid steric number was given
                System.out.println("Invalid steric number:" + steric);
                break;
        }

        return transVec;
    }

    private static Rectangle getRectangle(double[] transVec) {
        Point3D comingVec = new Point3D(transVec[0], transVec[1], transVec[2]);
        comingVec = comingVec.normalize();
        
        double length = LEWIS_BOND_SIZE;
        
        Rectangle rectangle = new Rectangle(1, length);
        
        Point3D axisOfRot = new Point3D(0, 0, 1);
        
        //dot product and determinant of that and {0, 1, 0}
        double dotProd = comingVec.getY();
        double det = comingVec.getX();
        double angle = Math.atan2(det, dotProd);
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRot);

        rectangle.getTransforms().add(rotateAroundCenter);
        
        return rectangle;
    }

    void setScore(int score) {
        lblScore.setText("Score: " + score);
    }

    private void addHoverLewis(Node s) {
        s.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                currSelectedGroup = (Group) s.getParent();
            }
        });
        s.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                currSelectedGroup = null;
            }
        });
    }
}
