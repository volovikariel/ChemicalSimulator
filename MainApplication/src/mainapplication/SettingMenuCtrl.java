/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * FXML Controller class
 *
 * @author Jorge
 */
public class SettingMenuCtrl implements Initializable {
    
    @FXML
    Slider sliBonds;
    
    MainAppCtrl controller;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    @FXML
    public void handleCancel(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
    
    @FXML
    public void handleApply(ActionEvent event) {
        controller.applySettings((int) sliBonds.getValue());
        
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
    
    @FXML
    public void handleReset(ActionEvent event) {
        sliBonds.setValue(8);
    }
    
    public void setParams(int currValue, MainAppCtrl controller) {
        sliBonds.setValue(currValue);
        this.controller = controller;
    }
}
