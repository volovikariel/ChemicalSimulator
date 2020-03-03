package mainapplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        primaryStage.setMinHeight(800);
        primaryStage.setMinWidth(800);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainApp.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Chemistry Simulator");
        this.scene = new Scene(root, 1280, 960);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        ((MainAppCtrl) loader.getController()).loadScene();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
