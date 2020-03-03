/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainapplication;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/**
 *
 * @author cstuser
 */
public class SelectionSceneCtrl implements Initializable, SubSceneController {
    
    @FXML
    TextField txtManual;
    @FXML
    SplitPane splitPane;
    @FXML
    GridPane gridPane;
    @FXML
    AnchorPane paneSimulation;
    
    DataFormat data;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /*splitPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                txtManual.getParent().requestFocus();
                txtManual.setText("");
                txtManual.setVisible(false);
            }
        });
        paneSimulation.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if(event.getDragboard().hasImage()) {
                    event.acceptTransferModes(TransferMode.COPY);
                }
                event.consume();
            }
        });
        paneSimulation.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                ImageView newImageView = new ImageView(event.getDragboard().getImage());
                newImageView.setLayoutX(event.getX() - newImageView.getImage().getWidth()/2);
                newImageView.setLayoutY(event.getY() - newImageView.getImage().getHeight()/2);
                //VBox newImageView = new VBox((VBox) event.getDragboard().getContent(data));
                paneSimulation.getChildren().add(newImageView);
                
                event.consume();
            }
        });*/
    }

    public void appendInput(String text) {
        txtManual.setVisible(true);
        txtManual.setText(txtManual.getText() + text);
    }
    
    public void removeChar() {
        if(txtManual.getLength() >= 1) {
            txtManual.setText(txtManual.getText().substring(0, txtManual.getLength() - 1));   
        }
    }
    
    public void loadTable(Atom[] atoms) {
        RowConstraints tempRow = new RowConstraints();
        tempRow.setVgrow(Priority.ALWAYS);
        ColumnConstraints tempCol = new ColumnConstraints();
        tempCol.setHgrow(Priority.ALWAYS);
        
        int rowCount = 0;
        int startOfCol = 0;
        for (int i = 0; i < 18; i++) {
            gridPane.getColumnConstraints().add(i, tempCol);
        }
        for (int i = 0; i < 9; i++) {
            gridPane.getRowConstraints().add(i, tempRow);
        }
        VBox tempPane;
        for (int i = 0; i < atoms.length; i++) {
            switch (i) {
                case 2:
                case 10:
                case 18:
                case 36:
                case 54:
                case 86:
                    rowCount++;
                    startOfCol = i;
                    break;
            }
            int colIndx = 0;
            switch(rowCount) {
                case 0:
                    colIndx = i * 17;
                    break;
                case 1:
                case 2:
                    if (i < startOfCol + 2)
                        colIndx = i - startOfCol;
                    else
                        colIndx = i - startOfCol + 10;
                    break;
                case 6:
                case 5:
                    if (i < startOfCol + 17)
                        colIndx = i - startOfCol;
                    else
                        colIndx = i - startOfCol - 14;
                    break;
                default:
                    colIndx = i - startOfCol;
            }
            int rowIndx = rowCount;
            
            if ((i > 56 && i < 71) || (i > 88 && i < 103)) {
                rowIndx += 2;
            }
            
            tempPane = new VBox(new Label( "" + (i + 1)), new Label(atoms[i].getSymbol()));
            tempPane.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    Dragboard db = ((VBox) event.getSource()).startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    content.putImage(((VBox) event.getSource()).snapshot(null, null));
                    db.setContent(content);
                    event.consume();
                }
            });
            gridPane.add(tempPane, colIndx, rowIndx);
        }
    }
}
