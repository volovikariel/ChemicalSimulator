package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

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
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void initializeInputs() {
        System.out.println("Initialize Inputs: " + this);
        paneSimulation.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getY() <= 15f) {
                    initializeInputs();
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
                System.out.println("Key Typed: " + keyEvent.getCode());
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
    public void passData(Stage stage, Scene selectionScene, AnchorPane paneSimulation, AnchorPane panePeriodic, MenuBar menuBar, TextField txtManual) {
        System.out.println("Passing Data");
        this.primaryStage = stage;
        this.selectionScene = selectionScene;
        this.panePeriodic = panePeriodic;
        this.paneSimulation = paneSimulation;
        this.menuBar = menuBar;
        this.txtManual = txtManual;
        System.out.println("Controller after passing data " + this);
        initializeInputs();
    }

    @Override
    public String toString() {
        return "Controller{" +
                "paneSimulation=" + paneSimulation +
                ", panePeriodic=" + panePeriodic +
                ", menuBar=" + menuBar +
                ", txtManual=" + txtManual +
                ", primaryStage=" + primaryStage +
                ", selectionScene=" + selectionScene +
                '}';
    }
}
