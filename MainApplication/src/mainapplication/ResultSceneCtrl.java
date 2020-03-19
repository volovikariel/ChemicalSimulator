/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainapplication;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Main entry point of the JavaFX application
 * 
 * @author Ariel Volovik
 * @author Jorge Marcano
 * @author Samy Arab
 */
public class ResultSceneCtrl implements Initializable, SubSceneController {

    @FXML
    private TabPane resultID;
    
    public void resultList(LinkedList<Solution> list, String[] atomList, Atom[] metalList) {
        for (int i = 0; i < list.size(); i++) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TabTemplate.fxml"));
            Parent root = null;
            TabTemplateCtrl controller;
            try {
                root = loader.load();
            } catch (IOException ex) {
                ex.printStackTrace();
                continue;
            }
            
            controller = (TabTemplateCtrl) loader.getController();
            Tab newTab = new Tab("" + (i + 1));
            newTab.setContent(root);
            
            //controller.sendSolution(list.get(i));
            //String[] temp = {"H", "H", "H", "H", "C", "C"};
            //controller.setLewisStructure(list.get(i).getMatrix(), atomList);
            controller.set3D(list.get(i).getMatrix(), atomList, list.get(i).getLoop());
            
            resultID.getTabs().add(newTab);
        }
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    
}
