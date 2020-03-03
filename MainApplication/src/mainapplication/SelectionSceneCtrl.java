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
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;

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
    TilePane tilePane;
    @FXML
    AnchorPane paneSimulation;
    
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
        // [In Progress]
        Image tstImage = new Image(getClass().getResourceAsStream("tstImage.png"), 100, 100, true, true);
        ImageView tstImageView = new ImageView(tstImage);
        tilePane.getChildren().add(0, tstImageView);
        tstImageView.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Dragboard db = tstImageView.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putImage(tstImageView.getImage());
                db.setContent(content);
                event.consume();
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

    
}
