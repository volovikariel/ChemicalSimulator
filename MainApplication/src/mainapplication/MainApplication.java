package mainapplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point of the JavaFX application
 * 
 * @author Ariel Volovik
 * @author Jorge Marcano
 * @author Samy Arab
 */
public class MainApplication extends Application {

    // Make these global so that they can be used in separate methods.
    //TODO: Use or remove resultScene
    private Stage primaryStage;
    private Scene scene;
    private Scene resultScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        /** 
         * Initialize the Stage and load the MainAppCtrl
         */
        
        // Load the first scene onto the stage
        this.primaryStage  = primaryStage;
        primaryStage.setMinHeight(800);
        primaryStage.setMinWidth(800);
        primaryStage.setTitle("Chemistry Simulator");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainApp.fxml"));
        Parent root = loader.load();
        this.scene = new Scene(root, 1280, 960);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        ((MainAppCtrl) loader.getController()).loadScene();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
