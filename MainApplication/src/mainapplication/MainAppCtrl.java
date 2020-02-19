/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainapplication;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

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
    
    private FXMLLoader loader;
    
    @FXML
    private void handleMouse(MouseEvent event) {
        if(event.getY() <= 15f) {
            menuBar.setVisible(true);
        }
        else {
            menuBar.setVisible(false);
        }
    }
    
    @FXML
    private void handleKeyTyped(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            ((Stage) (scene.getWindow())).close();
        }
        else if(keyEvent.getCode() == KeyCode.SPACE) {
            if(isSelecting) {
                isSelecting = false;
                loader = new FXMLLoader(getClass().getResource("ResultsScene.fxml"));
                try {
                    Parent root = loader.load();
                    subScene.setRoot(root);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                isSelecting = true;
                loader = new FXMLLoader(getClass().getResource("SelectionScene.fxml"));
                try {
                    Parent root = loader.load();
                    subScene.setRoot(root);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(Character.isLetterOrDigit(keyEvent.getCharacter().charAt(0)) && isSelecting) {
            ((SelectionSceneCtrl) loader.getController()).appendInput(keyEvent.getCharacter());
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loader = new FXMLLoader(getClass().getResource("SelectionScene.fxml"));
        Parent root;
        
        try {
            root = loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        
        subScene.setRoot(root);
    }
    
    public void loadScene() {
        scene = subScene.getScene();
        
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
    
}
