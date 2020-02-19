package mainapplication;

import java.io.IOException;
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

/**
 *
 * @author cstuser
 */
public class MainApplication extends Application {

    // Make these global so that they can be used in separate methods.
    private Stage primaryStage;
    private Scene scene;
    private Scene resultScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the first scene onto the stage
        this.primaryStage  = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainApp.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Chemistry Simulator");
        this.scene = new Scene(root, 600, 550);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        ((MainAppCtrl) loader.getController()).loadScene();

       
        // Running a .exe file
//        try {
//            Process p = Runtime.getRuntime().exec("/home/pshychozpath/Desktop/program.exe");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
