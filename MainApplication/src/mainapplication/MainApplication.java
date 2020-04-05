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
    
    private MainAppCtrl controller;

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
        controller = loader.getController();
        this.scene = new Scene(root, 1280, 960);
        this.scene.getStylesheets().add(getClass().getResource("appDesign.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        
        controller.loadScene();
    }
    
    public static void main(String[] args) {
        launch(args);
    }  
    
    @Override
    public void stop() {
        if (controller.proc != null)
            controller.proc.destroyForcibly();
    }
}
