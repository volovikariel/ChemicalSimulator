package mainapplication;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Controller for the result scene FXML.
 * This FXML is shown once the algorithm returns the solutions.
 * This scene contains the TabTemplate FXML inside of it.
 * 
 * @author Ariel Volovik
 * @author Jorge Marcano
 * @author Samy Arab
 */
public class ResultSceneCtrl implements Initializable, SubSceneController {

    @FXML
    private TabPane resultID;
    
    /**
     * Method that creates each individual Tab inside of the TabPane.
     * Each Tab corresponds to a separate solution.
     * @param list the list of solutions
     * @param atomList the list of non-metals 
     * @param metalList the list of metals
     */
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    
}
