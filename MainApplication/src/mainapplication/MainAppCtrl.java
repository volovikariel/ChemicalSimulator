/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainapplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    private final String RESULTS_STR = "ResultsScene.fxml";
    private final String SELECTION_STR = "SelectionScene.fxml";
    private final String LOADING_STR = "LoadingScene.fxml";

    
    // Keys
    private final char ESCAPE = 27;
    private final char ENTER = 13;
    
    
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
                callAlgorithm();
                loadSubscene(LOADING_STR);
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

    private void callAlgorithm() throws IOException {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        File dir = new File(s);
        Runtime run = Runtime.getRuntime();

        String input = parseInput();

        Process proc = run.exec(String.format("b.exe %s", input), null, dir);

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
                loadSubscene(RESULTS_STR);
                return;
            }
            
            if (s.equals(">>>>")) {
                if (isReading) {
                        System.out.println("ERROR, new matrix started without ending last one");
                        return;
                }
                isReading = true;
                currMatrixLine = 0;
                matrixSize = 1;
            }
            else if (isReading) {
                if (s.equals("<<<<")) {
                    if (currMatrixLine != matrixSize) {
                        System.out.println("ERROR, matrix ended before size reached");
                        return;
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
                        return;
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
            System.out.println(s);
        }
    }

    private String parseInput() {
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
        System.out.println(llSymbols);
        return text;
    }
}
