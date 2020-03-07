/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainapplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * FXML Controller class
 *
 * @author cstuser
 */
public class MainAppCtrl implements Initializable {
    
    @FXML
    private MenuBar menuBar;
    @FXML
    private SubScene subScene;
    
    boolean isSelecting = true;
    
    // ParentScene -> ((AnchorPane -> SplitPane -> (SelectionPane & PeriodicPane)) & MenuBar)
    private Scene parentScene;
    private AnchorPane subPane;
    
    private SubSceneController controller;
    
    private boolean isDown = true;
    
    private boolean isAnimating = false;
    
    //subscene locations
    private final String RESULTS_STR = "ResultScene.fxml";
    private final String SELECTION_STR = "SelectionScene.fxml";
    private final String LOADING_STR = "LoadingScene.fxml";
    
    // Keys
    private final char ESCAPE = 27;
    private final char ENTER = 13;
    private final char BACKSPACE = 8;
    
    private Atom[] atoms;
    
    @FXML
    private void handleMouse(MouseEvent event) {
        if(event.getY() < 15 && !isAnimating && !isDown) {
            isAnimating = true;
            Timeline scrollIn = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    isDown = true;
                    isAnimating = false;
                }
            }, new KeyValue(menuBar.translateYProperty(), 0)));
            scrollIn.play();
        }
        else if (event.getY() > 30 && isDown && !isAnimating) {
            isAnimating = true;
            Timeline scrollOut = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    isDown = false;
                    isAnimating = false;
                }
            }, new KeyValue(menuBar.translateYProperty(), -30)));
            scrollOut.play();
        }
    }
    
    @FXML
    private void handleKeyTyped(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCharacter().charAt(0) == ESCAPE) {
            ((Stage) (parentScene.getWindow())).close();
        } 
        else if (keyEvent.getCharacter().charAt(0) == ENTER) {
            if (isSelecting) {
                String[] input = ((SelectionSceneCtrl) controller).parseInput();
                //String[] input = {"H", "6", "C", "3"};
                String inputStr = getInputStr(input);
                loadSubscene(LOADING_STR);
                LinkedList<int[][]> solutions = callAlgorithm(inputStr);
                if (solutions.isEmpty()) {
                    System.out.println("No solutions found!!");
                    
                    loadSubscene(SELECTION_STR);
                    
                    return;
                }
                loadSubscene(RESULTS_STR);
                String[] atomList = SelectionSceneCtrl.getAtoms(input);
                //System.out.println(Arrays.toString(atomList));
                ((ResultSceneCtrl) controller).resultList(solutions, atomList);
                
            } 
            else {
                loadSubscene(SELECTION_STR);
            } 
        }
        else if (Character.isLetterOrDigit(keyEvent.getCharacter().charAt(0)) && isSelecting) {
            ((SelectionSceneCtrl) controller).appendInput(keyEvent.getCharacter());
        }
        else if (keyEvent.getCharacter().charAt(0) == BACKSPACE) {
            // Removes a character at a time
            ((SelectionSceneCtrl) controller).removeChar();
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
                atoms[index] = new Atom(csvRecord.get(0), csvRecord.get(1), csvRecord.get(2), index + 1);
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
        subScene.heightProperty().bind(parentScene.heightProperty().subtract(35));
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
        isSelecting = subsceneFile.equals(SELECTION_STR);
        FXMLLoader loader = new FXMLLoader(getClass().getResource(subsceneFile));
        try {
            Parent root = loader.load();
            //subScene.setRoot(root);
            subPane.getChildren().clear();
            subPane.getChildren().add(root);
            controller = (SubSceneController) loader.getController();
            
            if (isSelecting) {
                ((SelectionSceneCtrl) controller).loadTable(atoms);
                ((SelectionSceneCtrl) controller).setAtoms(atoms);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private LinkedList<int[][]> callAlgorithm(String input) {
    	try {
            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();
            File dir = new File(s);
            Runtime run = Runtime.getRuntime();
            
            Process proc = run.exec(String.format("b.exe%s", input), null, dir);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            boolean isReading = false;
            LinkedList<int[][]> solutionSet = new LinkedList<>();
            int[][] tempArray = null;
            int currMatrixLine = 0;
            int matrixSize = 1;
            while ((s = stdInput.readLine()) != null) {
                // Reached the end of the file
                if(s.equals("END")) {
                    return solutionSet;
                }

                if (s.equals(">>>>")) {
                	if (isReading) {
                		System.out.println("ERROR, new matrix started without ending last one");
                		return null;
                	}

                	isReading = true;
                	currMatrixLine = 0;
                	matrixSize = 1;
                }
                else if (isReading) {
                	if (s.equals("<<<<")) {
                		if (currMatrixLine != matrixSize) {
                			System.out.println("ERROR, matrix ended before size reached");
                			return null;
                		}

                		isReading = false;
                	}
                	else {
	                	String[] valuesStr = s.split(" ");

	                	if (currMatrixLine == 0) {
	                		matrixSize = valuesStr.length;
	                		tempArray = new int[matrixSize][matrixSize];
	                	}
	                	else if (currMatrixLine > matrixSize) {
	                		System.out.println("ERROR, matrix size mismatch");
	                		return null;
	                	}
	                	else if (currMatrixLine == matrixSize - 1) {
	                		solutionSet.add(tempArray);
	                	}

	                	for (int i = 0; i < matrixSize; i++) {
	                		tempArray[currMatrixLine][i] = Integer.parseInt(valuesStr[i]);
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
    
    
    
    private String getInputStr(String[] input) {
        String returnStr = "";
        
        for (String element : input) {
            returnStr +=  " " + element;
        }
        return returnStr;
    }
}    
