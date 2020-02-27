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
                String[] input = parseInput();
                //String[] input = {"H", "6", "C", "3"};
                String inputStr = getInputStr(input);
                loadSubscene(LOADING_STR);
                LinkedList<int[][]> solutions = callAlgorithm(inputStr);
                loadSubscene(RESULTS_STR);
                String[] atomList = getAtoms(input, solutions.get(0).length);
                //System.out.println(Arrays.toString(atomList));
                ((ResultSceneCtrl) controller).resultList(solutions, atomList);
//                loadSubscene(LOADING_STR);
            } 
            else {
                loadSubscene(SELECTION_STR);
            } 
        }
        else if (Character.isLetterOrDigit(keyEvent.getCharacter().charAt(0)) && isSelecting) {
            ((SelectionSceneCtrl) controller).appendInput(keyEvent.getCharacter());
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Parsing CSV
        atoms = new Atom[110];
        Path currentRelativePath = Paths.get("res/elements.csv");
        String s = currentRelativePath.toAbsolutePath().toString();
        try (CSVParser csvParser = new CSVParser(new FileReader(s), CSVFormat.DEFAULT.withHeader());) {
            int index = 0;
            for (CSVRecord csvRecord : csvParser) {
                atoms[index] = new Atom(csvRecord.get(0), csvRecord.get(1), csvRecord.get(2));
                index++;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        subPane = new AnchorPane();
        subScene.setRoot(subPane);
        loadSubscene(SELECTION_STR);
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
        return null;
    }
    
    private String[] getAtoms(String[] input, int size) {
        String[] returnStr = new String[size];
        
        int offset = 0;
        int prevOffset = 0;
        for (int i = 0; i < input.length - 1; i+=2) {
            prevOffset = offset;
            for (; offset < prevOffset + Integer.parseInt(input[i+1]); offset++)
            {
                returnStr[offset] = input[i];
            }
        }
        
        return returnStr;
    }
    
    private String getInputStr(String[] input) {
        String returnStr = "";
        
        for (String element : input) {
            returnStr +=  " " + element;
        }
        
        return returnStr;
    }

    private String[] parseInput() {
        String text = ((SelectionSceneCtrl) controller).txtManual.getText();
        char[] llInput = new char[text.length()];
        // Converting input to a linked list
        for(int i = 0; i < text.length(); i++) {
            llInput[i] = text.charAt(i);
        }
        LinkedList<String> llSymbols = new LinkedList<>();
        
        // Getting ready for parsing
        boolean started = false;
        boolean startedNumber = false;
        String symbol = "";
        // Converting linked list array of character
        for(int j = 0; j < llInput.length; j++) {
            // >>H<<Co2C4, H>>C<<o2C4, HCo>>2<<C4, HCo2>>C<<4, HCo2C>>4<<
            if(!started) {
                if(Character.isDigit(llInput[j])) {
                    startedNumber = true;
                    llSymbols.add(symbol);
                    symbol = Character.toString(llInput[j]);  
                }
                else {
                    started = true;
                    symbol += llInput[j];
                    startedNumber = false;
                }
            }
            // HC>>o<<2C4
            else if(Character.isLowerCase(llInput[j])) {
                symbol += llInput[j];
                if (startedNumber) {
                    System.out.println("error");
                }
            }
            // H>>C<<o2C4
            else if(Character.isUpperCase(llInput[j])) {
                llSymbols.add(symbol);
                startedNumber = false;
                symbol = Character.toString(llInput[j]);
            }
            // HCo>>2<<C4, HCo2C>>4<<
            else if(Character.isDigit(llInput[j])) {
                if (startedNumber)
                    symbol += llInput[j];
                else {
                    startedNumber = true;
                    llSymbols.add(symbol);
                    symbol = Character.toString(llInput[j]); 
                }
            }
        }
        llSymbols.add(symbol);
        
        ArrayList<String> alFormatted = new ArrayList<>();
        // Formatting the output and checking if the total number of atoms exceeds the limit (20)
        for(int i = 0; i <= llSymbols.size() - 1; i++) {
            //TODO: Check if it's a valid atom - if not - error and return
            //TODO: Check for repetitions COOH -> CO2H 
            //TODO: Order in the order of the atoms after the fact
            // If it's the last - must be letter
            if(i == llSymbols.size() - 1) {
                alFormatted.add(llSymbols.getLast());
                alFormatted.add(""+1);
            }
            // If it doesn't contain a number
            else if(!llSymbols.get(i).matches(".*\\d+.*")) {
                // If the next value is a number, add the symbol and the number to the list
                if(llSymbols.get(i + 1).matches(".*\\d+.*")) {
                    alFormatted.add(llSymbols.get(i));
                    alFormatted.add(llSymbols.get(i + 1));
                    i++;
                }
                // If it doesn't contain a number after the symbol, add a 1 to the list 
                else if(!llSymbols.get(i + 1).matches(".*\\d+.*")) {
                    alFormatted.add(llSymbols.get(i));
                    alFormatted.add(""+1);
                }
            }
        }
        System.out.println(alFormatted);
        System.out.println("Atoms: " + Arrays.toString(atoms));
        return alFormatted.toArray(new String[alFormatted.size()]);
    }
}    
