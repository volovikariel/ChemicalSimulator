/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainapplication;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.util.Pair;
import mainapplication.model.Atom;
import mainapplication.model.Solution;

/**
 * FXML Controller class
 *
 * @author Jorge
 */
public class ManualMenuCtrl implements Initializable {
    
    @FXML
    GridPane gridTable;
    
    @FXML
    TextField txAtoms;
    
    boolean loaded = false;
    
    TextField[][] numbers;
    
    MainAppCtrl parent;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    @FXML
    void loadTable(ActionEvent event) {
        try {
            int number = Integer.parseInt(txAtoms.getText());
            if (number <= 0)
                throw new NumberFormatException();
            
            loaded = true;
            
            RowConstraints tempRow = new RowConstraints();
            tempRow.setVgrow(Priority.ALWAYS);
            ColumnConstraints tempCol = new ColumnConstraints();
            tempCol.setHgrow(Priority.ALWAYS);

            numbers = new TextField[number + 1][number + 1];
            
            int rowCount = 0;
            int startOfCol = 0;
            for (int i = 0; i < number + 1; i++) {
                gridTable.getColumnConstraints().add(i, tempCol);
            }
            for (int i = 0; i < number + 1; i++) {
                gridTable.getRowConstraints().add(i, tempRow);
            }
            
            for (int i = 0; i < number + 1; i++) {
                for (int j = 0; j < number + 1; j++) {
                    TextField temp = new TextField();
                    
                    if (i == 0 && j == 0) {
                        Label lbl = new Label("Atoms:");
                        gridTable.add(lbl, i, j);
                        continue;
                    }
                    
                    temp.setAlignment(Pos.CENTER);
                    
                    numbers[i][j] = temp;
                    
                    temp.textProperty().addListener(new ChangeListener() {
                        @Override
                        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                            for (int a = 0; a < numbers.length; a++)
                                for (int b = 0; b < numbers[0].length; b++)
                                    if (numbers[a][b] != null && numbers[a][b].equals(temp))
                                        numbers[b][a].setText(newValue.toString());
                        }
                    });
                    
                    gridTable.add(temp, i, j);
                }
            }
            
        } catch (NumberFormatException e) {
            Alert help = new Alert(Alert.AlertType.ERROR);
            help.setTitle("Error");
            help.setHeaderText("ERROR");
            help.setContentText("You must enter a positive whole number!");
            help.show();
        }
        
    }
    
    @FXML
    void handleEnter(ActionEvent event) {
        if (!loaded) {
            Alert help = new Alert(Alert.AlertType.ERROR);
            help.setTitle("Error");
            help.setHeaderText("ERROR");
            help.setContentText("You must fill the table!");
            help.show();
        }
        LinkedList<Solution> solutions = getInput();
        
        if (solutions == null)
            return;
        
        ArrayList<Pair<Group, Group>> groups = new ArrayList<>(1);
        
        groups.add(0, TabTemplateCtrl.doAll(solutions.get(2).getMatrix(), solutions.get(0).getNames(), solutions.get(2).getLoop(), new Atom[0]));
        
        parent.loadSols(groups, solutions);
        
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    LinkedList<Solution> getInput() {
        LinkedList<Solution> returnArr = new LinkedList<>();
        
        String[] names = new String[numbers.length - 1];
        for (int atom = 0; atom < names.length; atom++) {
            names[atom] = numbers[0][atom + 1].getText();
        }
        
        returnArr.add(new Solution(names));
        returnArr.add(new Solution(new String[0]));
        
        int[][] matrix  = new int[numbers.length - 1][numbers.length - 1];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                try {
                    matrix[i][j] = Integer.parseInt(numbers[i+1][j+1].getText());
                } catch (NumberFormatException e) {
                    Alert help = new Alert(Alert.AlertType.ERROR);
                    help.setTitle("Error");
                    help.setHeaderText("ERROR");
                    help.setContentText("You must enter a positive whole number!");
                    help.show();
                    
                    return null;
                }
            }
        }
        
        returnArr.add(new Solution(matrix, 0, null));
        
        //TODO: LOOP
        return returnArr;
    }

    void setParent(MainAppCtrl parent) {
        this.parent = parent;
    }
    
}
