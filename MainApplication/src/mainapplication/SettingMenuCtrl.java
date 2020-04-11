package mainapplication;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

/**
 * Controller class for the settings window.
 *
 * @author Jorge
 */
public class SettingMenuCtrl implements Initializable {
    
    @FXML
    Slider sliBonds;
    
    MainAppCtrl controller;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    /**
     * Closes the window if the user presses Cancel.
     */
    @FXML
    public void handleCancel(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
    
    /**
     * Applies the user defined values if the user presses Apply.
     */
    @FXML
    public void handleApply(ActionEvent event) {
        controller.applySettings((int) sliBonds.getValue());
        
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
    
    /**
     * Resets the slider to its initial value if the user presses Reset
     */
    @FXML
    public void handleReset(ActionEvent event) {
        sliBonds.setValue(8);
    }
    
    /**
     * Method which sets the user defined parameters.
     * @param currValue the new value to which it is to be set.
     * @param controller the MainAppController.
     */
    public void setParams(int currValue, MainAppCtrl controller) {
        sliBonds.setValue(currValue);
        this.controller = controller;
    }
}
