package mainapplication;

import mainapplication.model.Solution;
import mainapplication.model.Atom;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.WritableImage;
import javafx.util.Pair;
import org.controlsfx.control.Notifications;

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
    
    private HashMap<Integer, TabTemplateCtrl> controllers;
    
    /**
     * Method that creates each individual Tab inside of the TabPane.
     * Each Tab corresponds to a separate solution.
     * @param list the list of solutions
     * @param atomList the list of non-metals 
     * @param metalList the list of metals
     */
    public void resultList(LinkedList<Solution> list, String[] atomList, Atom[] metalList) {
        resultID.requestFocus();
        controllers = new HashMap<>();
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
            
            Solution current = list.get(i);
            
            //controller.sendSolution(list.get(i));
            //String[] temp = {"H", "H", "H", "H", "C", "C"};
            controller.setLewis(current.getMatrix(), atomList, current.getLoop());
            controller.set3D(current.getMatrix(), atomList, current.getLoop());
            controller.setScore(current.getScore());
            
            resultID.getTabs().add(newTab);
            
            controllers.put(i, controller);
        }
        // Notifying the user that the algorithm has finished its job
        Notifications.create().title("Chemical Simulator").text("Done!").showInformation();
    }
    
    /**
     * Method which creates and adds individual tabs to the TabPane.
     * @param groups the list of Pairs which which contain as Key, the 3D parts, and as Value, the Lewis parts.
     * @param list the list of solutions, as calculated by the algorithm.
     */
    public void setTabs(ArrayList<Pair<Group, Group>> groups, LinkedList<Solution> list) {
        resultID.requestFocus();
        controllers = new HashMap<>();
        for (int i = 0; i < groups.size(); i++) {
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
            
            Solution current = list.get(i);
            
            controller.setScore(current.getScore());
            controller.setNodes(groups.get(i));
            
            resultID.getTabs().add(newTab);
            
            controllers.put(i, controller);
        }
        Notifications.create().text("Press ENTER to try another molecule").position(Pos.CENTER).showInformation();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
    
    /**
     * Method which takes a snapshot of the 3D scene and returns it.
     * @return the image of the 3D scene.
     */
    public WritableImage screenShotThreeD() {
        TabTemplateCtrl ctrl = controllers.get(resultID.getSelectionModel().getSelectedIndex());
        return ctrl.screenShotThreeD();
    }
    
    /**
     * Method which takes a snapshot of the Lewis scene and returns it.
     * @return the image of the Lewis scene.
     */
    public WritableImage screenShotLewis() {
        TabTemplateCtrl ctrl = controllers.get(resultID.getSelectionModel().getSelectedIndex());
        return ctrl.screenShotLewis();
    }

    int getIndex() {
        return resultID.getSelectionModel().getSelectedIndex();
    }
}
