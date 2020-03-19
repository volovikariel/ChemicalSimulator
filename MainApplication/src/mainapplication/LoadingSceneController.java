package mainapplication;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressIndicator;

/**
 * Main entry point of the JavaFX application
 * 
 * @author Ariel Volovik
 * @author Jorge Marcano
 * @author Samy Arab
 */

public class LoadingSceneController implements Initializable, SubSceneController {

    @FXML
    private ProgressIndicator progIndic;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
    
}
