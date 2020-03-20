package mainapplication;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

/**
 * FXML Controller class
 *
 * @author Jorge
 */
public class LoadingSceneController implements Initializable, SubSceneController {

    @FXML
    private Label lblLoading;
    @FXML
    private AnchorPane root;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        root.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                lblLoading.setFont(new Font(newValue.doubleValue() / 10));
            }
        });
    }    
    
}
