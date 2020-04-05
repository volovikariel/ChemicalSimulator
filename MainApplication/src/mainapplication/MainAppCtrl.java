package mainapplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javax.imageio.ImageIO;
import javax.management.Notification;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Controller for the parent scene.
 * All scenes contained in subScene are affected by the handlers written in this class.
 * 
 * @author Ariel Volovik
 * @author Jorge Marcano
 * @author Samy Arab
 */
public class MainAppCtrl implements Initializable {
    
    @FXML
    private MenuBar menuBar;
    
    @FXML
    private MenuItem takeLewisPicture;
    @FXML
    private MenuItem take3DPicture;
    @FXML
    private MenuItem menuHelp;
    
    @FXML
    private SubScene subScene;
    
    // ParentScene -> ((AnchorPane -> SplitPane -> (SelectionPane & PeriodicPane)) & MenuBar)
    private Scene parentScene;
    private AnchorPane subPane;
    
    private SubSceneController controller;
    
    boolean isSelectionScene = true;

    private boolean menuBarIsDown = true;
    
    private boolean isAnimating = false;
    
    // Subscene locations
    private final String RESULTS_STR = "ResultScene.fxml";
    private final String SELECTION_STR = "SelectionScene.fxml";
    private final String LOADING_STR = "LoadingScene.fxml";
    
    // Key ASCII codes
    private final char ESCAPE = 27;
    private final char ENTER = 13;
    private final char BACKSPACE = 8;
    
    private static Atom[] atoms;
    
    private LinkedList<Solution> solutions = null;
    public Task<Void> algorithmTask;
    
    private Runtime run;
    public Process proc = null;
    
    private ArrayList<Pair<Group, Group>> groups = null;
    
    public static Atom[] getAtoms() {
        return atoms;
    }
    
    // Handler for the Menubar scrolling 
    @FXML
    private void handleMouse(MouseEvent event) {
        if(event.getY() < 15 && !isAnimating && !menuBarIsDown) {
            isAnimating = true;
            Timeline scrollIn = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    menuBarIsDown = true;
                    isAnimating = false;
                }
            }, new KeyValue(menuBar.translateYProperty(), 0)));
            scrollIn.play();
        }
        else if (event.getY() > 30 && menuBarIsDown && !isAnimating) {
            isAnimating = true;
            Timeline scrollOut = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    menuBarIsDown = false;
                    isAnimating = false;
                }
            }, new KeyValue(menuBar.translateYProperty(), -30)));
            scrollOut.play();
        }
    }
    
    @FXML
    private void handleKeyTyped(KeyEvent keyEvent) throws IOException {
        // Close the Stage 
        if (keyEvent.getCharacter().charAt(0) == ESCAPE) {
            ((Stage) (parentScene.getWindow())).close();
        }
        else if (keyEvent.getCharacter().charAt(0) == ENTER) {
            if (isSelectionScene) { // Parse input if it's in the selection scene and Enter is pressed
                String[] input = ((SelectionSceneCtrl) controller).parseInput();
                
                if (input.length == 0)
                    return;
                
                //String[] input = {"H", "6", "C", "3"};
                String inputStr = getInputStr(input);
                
                loadSubscene(LOADING_STR);
                
                //call the algorithm on a seperate thread
                algorithmTask = new Task<Void>() {
                    @Override
                    protected Void call() {
                        solutions = callAlgorithm(inputStr);
                        
                        if (solutions.size() <= 2) {
                            System.out.println("No solutions found!!");

                            Alert alert = new Alert(AlertType.WARNING, "These atoms yielded no solutions!");
                            alert.setTitle("No Solutions Found!!!");
                            alert.show();

                            loadSubscene(SELECTION_STR);
                            
                            groups = null;
                            
                            return null;
                        }
                            
                        // Get atom list
                        String[] atomList = solutions.getFirst().getNames();
                        solutions.removeFirst();
                        // Get metal list
                        String[] metalList = solutions.getFirst().getNames();
                        solutions.removeFirst();
                        Atom[] metalAtoms = new Atom[metalList.length];

                        int offset = 0;
                        for (int i = 0; i < metalList.length; i++) {
                            for (int j = offset; j < atoms.length; j++) {
                                if (atoms[j].getSymbol().equals(metalList[i])) {
                                    metalAtoms[i] = atoms[j];
                                    offset = j;
                                    break;
                                }
                            }
                        }

                        //get all the groups
                         groups = new ArrayList<>(solutions.size());

                        for (int i = 0; i < solutions.size(); i++) {
                            groups.add(i, TabTemplateCtrl.loadGroups(solutions.get(i).getMatrix(), atomList, solutions.get(i).getLoop()));
                        }
                        
                        return null;
                    }
                };
                
                algorithmTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        
                        if (groups == null)
                            return;
                        
                        loadSubscene(RESULTS_STR);

                        //System.out.println(Arrays.toString(atomList));
                        ((ResultSceneCtrl) controller).setTabs(groups, solutions);
                        
                        algorithmTask = null;
                        
                        take3DPicture.setDisable(false);
                        takeLewisPicture.setDisable(false);
                    }
                });
                
                algorithmTask.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        proc.destroyForcibly();
                        proc = null;
                    }
                    
                });
                
                algorithmTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        proc.destroyForcibly();
                        proc = null;
                    }
                    
                });
                
                Thread thread = new Thread(algorithmTask);
                thread.setDaemon(true);
                thread.start();
                
            } 
            else { // If you're in the Selection scene and Enter is pressed, go back to the Selection Scene
                //TODO: Ask user for confirmation to go back
                if (algorithmTask != null)
                    algorithmTask.cancel();
                loadSubscene(SELECTION_STR);
            } 
        }
        else if (Character.isLetterOrDigit(keyEvent.getCharacter().charAt(0)) && isSelectionScene) {
            ((SelectionSceneCtrl) controller).appendInput(keyEvent.getCharacter());
        }
        else if (keyEvent.getCharacter().charAt(0) == BACKSPACE) {
            // Removes a character at a time            
            ((SelectionSceneCtrl) controller).removeElement();
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Parsing CSV
        atoms = new Atom[118];
        Path currentRelativePath = Paths.get("res/elements.csv");
        String s = currentRelativePath.toAbsolutePath().toString();
        try (CSVParser csvParser = new CSVParser(new FileReader(s), CSVFormat.DEFAULT.withHeader());) {
            int index = 0;
            for (CSVRecord csvRecord : csvParser) {
                atoms[index] = new Atom(csvRecord.get(0), csvRecord.get(1), csvRecord.get(2), csvRecord.get(3), index + 1, csvRecord.get(4));
                index++;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        subPane = new AnchorPane();
        subScene.setRoot(subPane);
        loadSubscene(SELECTION_STR);
        ((SelectionSceneCtrl) controller).setAtoms(atoms);
    }
    
    public void loadScene() {
        parentScene = subScene.getScene();
        // Makes the subscene resize with its parent scene
        subScene.heightProperty().bind(parentScene.heightProperty().subtract(35)); // Subtract 35 to make room for the menuBar
        subScene.widthProperty().bind(parentScene.widthProperty());
        parentScene.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                try {
                    handleKeyTyped(event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        
        parentScene.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleMouse(event);
            }
        });
    }
    
    private void loadSubscene(String subsceneFile) {
        take3DPicture.setDisable(true);
        takeLewisPicture.setDisable(true);
        isSelectionScene = subsceneFile.equals(SELECTION_STR);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(subsceneFile));
        try {
            Parent root = loader.load();
            //subScene.setRoot(root);
            subPane.getChildren().clear();
            subPane.getChildren().add(root);
            controller = (SubSceneController) loader.getController();
            
            if (isSelectionScene) {
                ((SelectionSceneCtrl) controller).loadTable(atoms);
                ((SelectionSceneCtrl) controller).setAtoms(atoms);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that runs the algorithm given a user's input and returns the ordered solutions.
     * @param input the list of atoms
     * @return ordered list of matrices
     */
    private LinkedList<Solution> callAlgorithm(String input) {
    	try {
            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();
            File dir = new File(s);
            run = Runtime.getRuntime();
            
            // Runs the executable specific to the operating system
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win"))
                proc = run.exec(String.format("b.exe%s", input), null, dir);
            else if (osName.contains("mac"))
                proc = run.exec(String.format("./b%s", input), null, dir);
            else
                proc = run.exec(String.format("./b.out%s", input), null, dir);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            boolean isReading = false;
            LinkedList<Solution> solutionSet = new LinkedList<>();
            int[][] tempArray = null;
            int currMatrixLine = 0;
            int matrixSize = 1;
            int tempScore = 0;
            int[] loopInfo = null;
            
            while ((s = stdInput.readLine()) != null) {
                // Reached the end of the file
                if(s.equals("END")) {
                    return solutionSet;
                }
                
                if(s.contains("+ ")) {
                    solutionSet.add(new Solution(s.substring(2, s.length()).split(" ")));
                }
                if(s.contains("- ")) {
                    solutionSet.add(new Solution(s.substring(2, s.length()).split(" ")));
                }

                if (s.equals(">>>>")) {
                	if (isReading) {
                		System.out.println("ERROR, new matrix started without ending last one");
                		return null;
                	}

                	isReading = true;
                	currMatrixLine = -1;
                	matrixSize = 1;
                        loopInfo = null;
                }
                else if (isReading) {
                	if (s.equals("<<<<")) {
                		solutionSet.add(new Solution(tempArray, tempScore, loopInfo));

                		isReading = false;
                	}
                	else {
                                if (currMatrixLine == -1) {
                                    tempScore = Integer.parseInt(s);
                                    currMatrixLine++;
                                    continue;
                                }
                                
                                String[] valuesStr = s.split(" ");
                                
                                if (currMatrixLine == 0) {
	                		matrixSize = valuesStr.length;
	                		tempArray = new int[matrixSize][matrixSize];
                                        for (int i = 0; i < matrixSize; i++) {
                                            tempArray[currMatrixLine][i] = Integer.parseInt(valuesStr[i]);
                                        }
	                	}
	                	else if (currMatrixLine >= matrixSize) {
                                    //loop info
                                    if (s.contains("? ")) {
                                        loopInfo = new int[valuesStr.length - 1];
                                        for (int i = 1; i < valuesStr.length; i++)
                                        {
                                            loopInfo[i - 1] = Integer.parseInt(valuesStr[i]);
                                        }
                                    }
	                	}
                                else {
                                    for (int i = 0; i < matrixSize; i++) {
                                            tempArray[currMatrixLine][i] = Integer.parseInt(valuesStr[i]);
                                    }
                                }

	                	currMatrixLine++;
                                
                	}
                }
                //System.out.println(s);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Did not find \"END\"");
        return new LinkedList<>();
    }
    
    
    /**
     * Modifies the parsed input into a format compatible with the algorithm's input.
     * @param input [P,H,O]
     * @return P H O
     */
    private String getInputStr(String[] input) {
        String returnStr = "";
        
        for (String element : input) {
            returnStr +=  " " + element;
        }
        return returnStr;
    }
    
    @FXML
    void saveScene(ActionEvent event) {
        try {
            WritableImage snapshot = parentScene.snapshot(null);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Picture As...");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
            File output = fileChooser.showSaveDialog(parentScene.getWindow());

            if(output != null) {
                output.mkdirs();
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    void saveThreeDim(ActionEvent event) {
        try {
            WritableImage snapshot = ((ResultSceneCtrl) controller).screenShotThreeD();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Picture As...");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
            File output = fileChooser.showSaveDialog(parentScene.getWindow());

            if(output != null) {
                output.mkdirs();
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    void saveLewis(ActionEvent event) {
        try {
            WritableImage snapshot = ((ResultSceneCtrl) controller).screenShotLewis();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Picture As...");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
            File output = fileChooser.showSaveDialog(parentScene.getWindow());

            if(output != null) {
                output.mkdirs();
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    @FXML 
    void showHelp(ActionEvent event) {
        Alert help = new Alert(AlertType.INFORMATION);
        help.setTitle("Help Information");
        help.getDialogPane().setMinWidth(550);
        help.setHeaderText("Information");
        help.setGraphic(null);
        help.setContentText("Notice: The menu is accessed from the top of the window."
                + "\n\n[Proceed]:"
                + "\n\t-ENTER (requires Elements to have been added)"
                + "\n\n[Add Elements]:"
                + "\n\t-Drag and Drop from the periodic table to the top portion of the screen"
                + "\n\t-Typing them, for instance: H2O"
                + "\n\n[Remove An Element]:"
                + "\n\t-If the elements are on the top portion of the screen, RIGHT CLICK the specific element"
                + "\n\t-Press Backspace"
                + "\n\n[Remove All Elements]:"
                + "\n\t-Access the menu EDIT-> CLEAR SCREEN"
                + "\n\n[Save Picture File]:"
                + "\n\t-Access the menu SAVE, then select what you want to save:"
                + "\n\t\t Save Screen"
                + "\n\t\t\t or "
                + "\n\t\t Save 3D"
                + "\n\t\t\t or"
                + "\n\t\t Save Lewis"
                + "\n\n[Go Back To Selection]:"
                + "\n\t-Press ENTER once the solutions have loaded"
                + "\n\n[Move Lewis or 3D]:"
                + "\n\t-Hold right click over the object and drag the cursor"
                + "\n\n[Rotate 3D]"
                + "\n\t-Hold left click over the object and drag the cursor");
        help.show();
    }
    
    @FXML
    void handleClose(ActionEvent event) {
        ((Stage) (parentScene.getWindow())).close();
    }
}    
