package mainapplication;

import mainapplication.model.Atom;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
    
    @FXML
    Label lblLewis;
    
    @FXML
    Label lbl3D;
    
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
     * Method which sets up the Lewis structure for a single solution.
     * @param solution the solution matrix
     * @param atomList the list of atoms contained in the solution
     * @param loops the indices of the rows which are part of a loop.
     * @return the covalent atom Group as a set of Rectangles and Text.
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

    /**
     * The recursive method which defines the position of an element in relation to the others.
     * @param currRow the current row of the solution matrix
     * @param prevRow the row from which this method was called
     * @param translateVec the translate vector
     * @param prevs the list of all rows on which the method was called
     * @param matrix the solution matrix
     * @param atomList the list of atoms which are part of the solution
     * @return an ArrayList of nodes, which are Rectangles and Text, to represent the Lewis structure.
     */
    public static ArrayList<Node> getRelativeLewis(int currRow, int prevRow, double[] translateVec, LinkedList<Integer> prevs, int[][] matrix, String[] atomList) {
        prevs.add(currRow);
        Atom[] atoms = MainAppCtrl.getAtoms();

        ArrayList<Node> returnList = new ArrayList<>();

        if(atomList[currRow].equals("H")) {
            Text temp = new Text(0, 0, "H");
            temp.setTextOrigin(VPos.TOP);
            temp.setFont(new Font(40));
            temp.setTextAlignment(TextAlignment.CENTER);
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
                    Point3D offsetVec = new Point3D(rectangleVec[0], rectangleVec[1], rectangleVec[2]).normalize();
                    
                    Rectangle rectangle = getRectangle(rectangleVec);

                    //calculate offset -> seperation from center + center of label
                    rectangle.setTranslateX(rectangle.getTranslateX() + LEWIS_OFFSET * offsetVec.getX() + temp.prefWidth(-1)/2);
                    rectangle.setTranslateY(rectangle.getTranslateY() + LEWIS_OFFSET * offsetVec.getY() + temp.prefHeight(-1)/2);
                    
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
                lonePairs = j + 1 <= 2 ? 2 - bondCount : lonePairs;
                formalCharge = atoms[j].getShells() - 2 * lonePairs - bondCount;
                break;
            }
        }
        
        Text label = new Text(0, 0, atomList[currRow]);
        label.setTextOrigin(VPos.TOP);
        label.setFont(new Font(40));
        label.setTextAlignment(TextAlignment.CENTER);
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
                        rectangle.setTranslateX(rectangle.getTranslateX() + LEWIS_OFFSET * offsetVec.getX() + label.prefWidth(-1)/2);
                        rectangle.setTranslateY(rectangle.getTranslateY() + LEWIS_OFFSET * offsetVec.getY() + label.prefHeight(-1)/2);
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
                    lonePairs = j + 1 <= 2 ? 2 - bondCount : lonePairs;
                    formalCharge = atoms[j].getShells() - 2 * lonePairs - bondCount;
                    break;
                }
            }

            Text lblLetter = new Text(0, 0, atomList[loopIndices[i]]);
            lblLetter.setTextOrigin(VPos.TOP);
            lblLetter.setFont(new Font(40));
            lblLetter.setTextAlignment(TextAlignment.CENTER);

            returnedList.add(lblLetter);

            translateVec = new double[] {sideLen, 0, 0};
            translateVec = makeRoation(translateVec, 0, 0, angleFromCenter * i);
            lblLetter.setTranslateX(translateVec[0]);
            lblLetter.setTranslateY(translateVec[1]);

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
                rectangle.setTranslateX(rectangle.getTranslateX() + translateVec[0] + LEWIS_OFFSET * offsetVec.getX() + lblLetter.prefWidth(-1)/2);
                rectangle.setTranslateY(rectangle.getTranslateY() + translateVec[1] + LEWIS_OFFSET * offsetVec.getY() + lblLetter.prefHeight(-1)/2);
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
                        rectangle.setTranslateX(rectangle.getTranslateX() + translateVec[0] + LEWIS_OFFSET * offsetVec.getX() + lblLetter.prefWidth(-1)/2);
                        rectangle.setTranslateY(rectangle.getTranslateY() + translateVec[1] + LEWIS_OFFSET * offsetVec.getY() + lblLetter.prefHeight(-1)/2);
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
     * @return 
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
    
    /**
     * 
     * @param solution
     * @param atomList
     * @param loop
     * @return
     */
    public static Pair<Group, Group> loadGroups(int [][] solution, String[] atomList, int[] loop) {
        Group group3D = set3D(solution, atomList, loop);
        group3D.setId("Cov");
        
        Group groupLewis = setLewis(solution, atomList, loop);
        groupLewis.setId("Cov");
        
        return new Pair<>(group3D, groupLewis);
    }
    
    /**
     *
     * @param solution
     * @param atomList
     * @param loop
     * @param metalList
     * @return
     */
    public static Pair<Group, Group> doAll(int [][] solution, String[] atomList, int[] loop, Atom[] metalList) {
        Pair<Group, Group> tempCovalent;
        if (solution.length != 0)   
            tempCovalent = loadGroups(solution, atomList, loop);
        else
            tempCovalent = new Pair<>(new Group(), new Group());
        
        Group ionsLewis = getIonsLewis(metalList);
        Group ions3D = getIons3d(metalList);

        Group lewis = new Group(tempCovalent.getValue());
        
        Group model3d = new Group(tempCovalent.getKey());
        
        ArrayList<Node> childrenLewis = new ArrayList<>(ionsLewis.getChildren());

        ArrayList<Node> children3d = new ArrayList<>(ions3D.getChildren());

        lewis.getChildren().addAll(childrenLewis);
        model3d.getChildren().addAll(children3d);
        
        //center the covalant
        Bounds bounds = tempCovalent.getKey().getBoundsInParent();
        double center = bounds.getMinX() + bounds.getMaxX();
        center /= 2;
        tempCovalent.getKey().setLayoutX(-center);
        center = bounds.getMinY() + bounds.getMaxY();
        center /= 2;
        tempCovalent.getKey().setLayoutY(-center);
        
        // Adding the ions 3D
        int numIons = children3d.size();
        double angle = (numIons == 0) ? 0 : Math.toRadians(360/numIons);
        double magnitudeCovalent = Math.sqrt((bounds.getWidth() * bounds.getWidth())/4 + (bounds.getHeight() * bounds.getHeight())/4);
        double magnitudeTranslation;
        for(int i = 0; i < numIons; i++){
            Node ionGroup = children3d.get(i);
            if(ionGroup instanceof Group) { 
                magnitudeTranslation = magnitudeCovalent;
                bounds = ionGroup.getBoundsInParent();
                magnitudeTranslation += Math.sqrt((bounds.getWidth() * bounds.getWidth())/4 + (bounds.getHeight() * bounds.getHeight())/4);
                ionGroup.setTranslateX((Math.sin(angle * i)) * magnitudeTranslation);
                ionGroup.setTranslateY(-(Math.cos(angle * i)) * magnitudeTranslation);
            }
        }
        
        bounds = tempCovalent.getValue().getBoundsInParent();
        center = bounds.getMinX() + bounds.getMaxX();
        center /= 2;
        tempCovalent.getValue().setLayoutX(-center);
        center = bounds.getMinY() + bounds.getMaxY();
        center /= 2;
        tempCovalent.getValue().setLayoutY(-center);
        
        double covalentWidth = bounds.getWidth()/2;
        double covalentHeight = bounds.getHeight()/2;
        double ionWidth;
        double ionHeight;
        magnitudeCovalent = Math.sqrt((covalentWidth * covalentWidth) + (covalentHeight * covalentHeight));
        for(int i = 0; i < numIons; i++){
            Node ionGroup = childrenLewis.get(i);
            if(ionGroup instanceof Group) { 
                magnitudeTranslation = magnitudeCovalent;
//                bounds = ionGroup.getBoundsInParent();
                bounds = ionGroup.getBoundsInParent();
                ionWidth = bounds.getWidth()/2;
                ionHeight = bounds.getHeight()/2;
                magnitudeTranslation += Math.sqrt((ionWidth * ionWidth)+ (ionHeight * ionHeight));
                ionGroup.setTranslateX((Math.sin(angle * i)) * magnitudeTranslation - ionWidth);
                ionGroup.setTranslateY(-(Math.cos(angle * i)) * magnitudeTranslation - ionHeight);
            }
        }
        Pair<Group, Group> listGroups = new Pair<>(model3d, lewis);
        
        return listGroups;
    }
    
    /**
     *
     * @param groups
     */
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
            if (node instanceof Text)
                addHoverLewis(node);
            if (node instanceof Group) {
                if (node.getId() != null && node.getId().equals("Cov"))
                    covalentGroupLewis = (Group) node;
                
                for (Node node2 : ((Group) node).getChildren())
                    if (node2 instanceof Text)
                        addHoverLewis(node2);
            }
        }
        
        realView.setRoot(atomGroup);
        
        lewisPane.getChildren().add(lewisGroup);
        
        realView.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                atomGroup.setLayoutX(0);
                Bounds bound = atomGroup.getBoundsInParent();
                double center = bound.getMinX() + bound.getMaxX();
                center /= 2;
                atomGroup.setLayoutX(newValue.doubleValue() / 2 - center);
            }
        });
        realView.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                atomGroup.setLayoutY(0);
                Bounds bound = atomGroup.getBoundsInParent();
                double center = bound.getMinY() + bound.getMaxY();
                center /= 2;
                atomGroup.setLayoutY(newValue.doubleValue() / 2 - center);
            }
        });
        lewisPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                lewisGroup.setLayoutX(0);
                Bounds bound = lewisGroup.getBoundsInParent();
                double center = bound.getMinX() + bound.getMaxX();
                center /= 2;
                lewisGroup.setLayoutX(newValue.doubleValue() / 2 - center);
            }
        });
        lewisPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                lewisGroup.setLayoutY(0);
                Bounds bound = lewisGroup.getBoundsInParent();
                double center = bound.getMinY() + bound.getMaxY();
                center /= 2;
                lewisGroup.setLayoutY(newValue.doubleValue() / 2 - center);
            }
        });
    }
    
    /**
     *
     * @param metalAtoms
     * @return
     */
    public static Group getIons3d(Atom[] metalAtoms) {
        Group returnGroup = new Group();
        
        //Must create a sphere for each ion element
        for (Atom atom : metalAtoms) {
            Group ion = new Group();
            Sphere temp = new Sphere(50);
            temp.setMaterial(new PhongMaterial(Color.web(atom.getColor())));
            temp.setId(atom.getSymbol());
            
            Label label = new Label("" + atom.getShells());
            label.setTranslateX(30);
            label.setTranslateY(-60);
            label.setTranslateZ(-30);
            label.setFont(new Font(40));
            
            ion.getChildren().addAll(temp, label);
            returnGroup.getChildren().add(ion);
        }
        
        return returnGroup;
    }
    
    /**
     *
     * @param metalAtoms
     * @return
     */
    public static Group getIonsLewis(Atom[] metalAtoms) {
        Group returnGroup = new Group();
        
        //Must create a label for each ion element
        for (Atom atom : metalAtoms) {
            Group ion = new Group();
            Text temp = new Text(0, 0, atom.getSymbol());
            temp.setTextOrigin(VPos.TOP);
            temp.setFont(new Font(40));
            temp.setTextAlignment(TextAlignment.CENTER);
            
            Label chargelbl = new Label("" + atom.getShells());
            chargelbl.setTranslateX(30 + 15 * (atom.getSymbol().length() - 1));
            chargelbl.setTranslateY(0);
            chargelbl.setFont(new Font(20));
            
            ion.getChildren().addAll(temp, chargelbl);
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
     * @param matrix
     * @param atomList
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
                lonePairs = i + 1 <= 2 ? 2 - bondCount : lonePairs;
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
        boolean isFirst = prevRow == -1;
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
                lblLewis.setStyle("-fx-text-fill: transparent;");
                lbl3D.setStyle("-fx-text-fill: transparent;");
                
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
                lblLewis.setStyle("-fx-text-fill: transparent;");
                lbl3D.setStyle("-fx-text-fill: transparent;");
                
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
     * @param event
     */
    @FXML
    public void handleMouseClick(MouseEvent event) {
        lblLewis.setStyle("-fx-text-fill: transparent;");
        lbl3D.setStyle("-fx-text-fill: transparent;");
        
        if (event.getButton() == MouseButton.PRIMARY) {
            initX = event.getX();
            initY = event.getY();

            movingGroup = currSelectedGroup;
            if (movingGroup == null)
                movingGroup = atomGroup;

            startTransX = movingGroup.translateXProperty().get();
            startTransY = movingGroup.translateYProperty().get();
        }
        else if (event.getButton() == MouseButton.SECONDARY) {
            initXAng = event.getX();
            initYAng = event.getY();
        }
    }

    /**
     * Handler which allows for the rotation and translation of the 3D group of atoms.
     * @param event
     */
    @FXML
    public void handleMouseDrag(MouseEvent event) {
        lblLewis.setStyle("-fx-text-fill: transparent;");
        lbl3D.setStyle("-fx-text-fill: transparent;");
        
        if (event.getButton() == MouseButton.PRIMARY) {
            movingGroup.translateXProperty().set(event.getX() - initX + startTransX);
            movingGroup.translateYProperty().set(event.getY() - initY + startTransY);
        }
        else if (event.getButton() == MouseButton.SECONDARY) {

            Rotate rotX = new Rotate((event.getX() - initXAng) * 360/ 200, new Point3D(0, 1, 0));
            Rotate rotY = new Rotate((event.getY() - initYAng) * 360/ 200, new Point3D(1, 0, 0));
            if (covalentGroup3D != null)
                covalentGroup3D.getTransforms().addAll(rotX, rotY);

            initXAng = event.getX();
            initYAng = event.getY();
        }
    }
    
    @FXML
    public void handleMouseClickLewis(MouseEvent event) {
        lblLewis.setStyle("-fx-text-fill: transparent;");
        lbl3D.setStyle("-fx-text-fill: transparent;");
        
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
        lblLewis.setStyle("-fx-text-fill: transparent;");
        lbl3D.setStyle("-fx-text-fill: transparent;");
        
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
                    lonePairs = j + 1 <= 2 ? 2 - bondCount : lonePairs;
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

    /**
     * Method which allows the taking of a snapshot of the 3D scene.
     * @return the snapshot of the 3D scene.
     */
    public WritableImage screenShotThreeD() {
        SnapshotParameters spa = new SnapshotParameters();
        spa.setTransform(Transform.scale(2, 2));
        return realView.snapshot(spa, null);
    }

    /**
     * Method which allows the taking of a snapshot of the Lewis scene.
     * @return the snapshot of the Lewis scene.
     */
    public WritableImage screenShotLewis() {
        SnapshotParameters spa = new SnapshotParameters();
        spa.setTransform(Transform.scale(2, 2));
        return lewisPane.snapshot(spa, null);
    }

    /**
     * Method which adds the on hover capability for the 3D scene.
     * @param s the Node to which the on hover capability will be added.
     */
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
        double[] rotAxis;
        if (Math.abs(attackVec[0]) < 0.0001 && Math.abs(attackVec[1]) < 0.0001)
            rotAxis = new double[] {1, 0, 0};
        else
            rotAxis = getAxis(axis, new double[] {0, 0, 1});

        switch (steric) {
            case 2:                     //This is the linear case, same as input vec
                break;
            case 3:                     //This is the trigonal planar case
                transVec = makeRot(attackVec, rotAxis, Math.PI / 3 - 2 * count * Math.PI / 3);
                break;
            case 4:                     //This is the tetrahedral case
                //gets the first case
                double rads = Math.toRadians(180 - 109.5);
                transVec = makeRot(attackVec, rotAxis, -rads);
                //modifies the first case depending on how many
                transVec = makeRot(transVec, axis, Math.toRadians(count * 120));
                break;
            case 5:                     //This is the trigonal bipyramidal case
                //the first case is just across, so same vec
                if (count == 0)
                    return transVec;

                //otherwise must make trigonal planar 90 degress from input
                transVec = makeRot(attackVec, rotAxis, -Math.PI / 2.0);
                //must get a turn 0, 120, 240
                transVec = makeRot(transVec, axis, Math.toRadians((count - 1) * 120));
                break;
            case 6:                     //This is the Octohedral case
                //the first case is just across, so same vec
                if (count == 0)
                    return transVec;

                //otherwise must make a square by making 2 opposites then 2 more opposites
                transVec = makeRot(attackVec, rotAxis, -Math.PI / 2.0);
                //must get a turn 0, 180, 90, 270
                transVec = makeRot(transVec, axis, Math.toRadians((count - 1) * 180 + ((count - 1) / 2) * 90));
                break;
            case 7:                     //This is the pentagonal bipyramidal case
                //the last case is just across, so same vec
                if (count == 6)
                    return transVec;

                //otherwise must make a square by making 2 opposites then 2 more opposites
                transVec = makeRot(attackVec, rotAxis, -Math.PI / 2.0);
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
