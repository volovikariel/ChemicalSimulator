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
    
    private Scene scene;
    private StackPane mainSubPane;
    
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
    private void handleKeyTyped(KeyEvent keyEvent) {
        if (keyEvent.getCharacter().charAt(0) == ESCAPE) {
            ((Stage) (scene.getWindow())).close();
        } 
        else if (keyEvent.getCharacter().charAt(0) == ENTER) {
            if (isSelecting) {
                loadSubscene(LOADING_STR);
                try {
                    String filepath = getClass().getResource("b.exe").toString();
                    Path currentRelativePath = Paths.get("");
                    String s = currentRelativePath.toAbsolutePath().toString();
                    File dir = new File(s);
                    Runtime run = Runtime.getRuntime();
                    Process proc = run.exec("b.exe C 1 O 2", null, dir);
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    String line = null;
                    while ((s = stdInput.readLine()) != null) {
                        System.out.println(s);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
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
        mainSubPane = new StackPane();
        subScene.setRoot(mainSubPane);
        loadSubscene(SELECTION_STR);
    }
    
    public void loadScene() {
        scene = subScene.getScene();
        // Makes the subscene resize with its parent scene
        subScene.heightProperty().bind(scene.heightProperty().subtract(35));
        subScene.widthProperty().bind(scene.widthProperty());
        
        scene.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                handleKeyTyped(event);
            }
        });
        
        scene.setOnMouseMoved(new EventHandler<MouseEvent>() {
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
            mainSubPane.getChildren().clear();
            mainSubPane.getChildren().add(root);
            controller = (SubSceneController) loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
