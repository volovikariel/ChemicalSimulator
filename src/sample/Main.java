package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @FXML
    private AnchorPane paneSimulation;
    @FXML
    private AnchorPane panePeriodic;
    @FXML
    private MenuBar menuBar;
    @FXML
    private TextField txtManual;

    // Make these global so that they can be used in separate methods.
    private Stage primaryStage;
    private Scene selectionScene;
    private Scene resultScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the first scene onto the stage
        this.primaryStage  = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SelectionScene.fxml"));
        // Setup the main controller
        loader.setController(this);
        Parent root = loader.load();
        primaryStage.setTitle("Chemistry Simulator");
        this.selectionScene = new Scene(root, 600, 550);
        primaryStage.setScene(selectionScene);
        primaryStage.show();

        initializeInputs();
    }

    public void initializeInputs() {
        paneSimulation.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getY() <= 15f) {
                    menuBar.setVisible(true);
                }
                else {
                    menuBar.setVisible(false);
                }
            }
        });

        selectionScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.ESCAPE) {
                    primaryStage.close();
                }
                if(keyEvent.getCode() == KeyCode.SPACE) {
                    if(primaryStage.getScene() == selectionScene) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("ResultsScene.fxml"));
                        try {
                            Parent root = loader.load();
                            Group group = new Group();
                            Rectangle rectangle = new Rectangle(280, 250, 80, 30);
                            group.getChildren().addAll(root, rectangle);
                            resultScene = new Scene(group, 600, 550);
                            resultScene.setOnKeyPressed(selectionScene.getOnKeyPressed());
                            primaryStage.setScene(resultScene);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        primaryStage.setScene(selectionScene);
                    }
                }
                if(keyEvent.getCode().isLetterKey()) {
                    txtManual.setVisible(true);
                    txtManual.setText(keyEvent.getCode().getChar());
                    txtManual.requestFocus();
                }
            }
        });

        txtManual.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.ENTER) {
                    System.out.println("Text Entered: " + txtManual.getText());
                    txtManual.setVisible(false);
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
