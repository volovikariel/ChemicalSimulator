package mainapplication;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * Controller for the loading scene FXML. 
 * This FXML is ran whilst the program waits for to receive the solutions sent by the algorithm.
 * 
 * @author Ariel Volovik
 * @author Jorge Marcano
 * @author Samy Arab
 */

public class LoadingSceneCtrl implements Initializable, SubSceneController {

    @FXML
    private Label lblLoading;
    @FXML
    private AnchorPane root;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Sphere H1 = new Sphere(40);
        Sphere H2 = new Sphere(40);
        Sphere O = new Sphere(50);
        O.setMaterial(new PhongMaterial(Color.RED));
        H1.getTransforms().add(new Translate(500, 800/2));
        H2.getTransforms().add(new Translate(700, 800/2));
        O.getTransforms().add(new Translate(600,  800/2.25));
        /***********************************************************************/
        
        Cylinder cylinder1 = new Cylinder(5, 100);
        PhongMaterial cylinderMaterial = new PhongMaterial(Color.BLACK);
        cylinderMaterial.setSpecularColor(Color.YELLOW);
        cylinder1.setMaterial(cylinderMaterial);

        Translate moveToMidpoint = new Translate(1050 / 2, 800/ 2,0);

        Point3D diff = new Point3D(500, -500, 0);

        Point3D axisOfRot = diff.crossProduct(new Point3D(0, 1, 0));
        double angle = Math.acos(diff.normalize().dotProduct(new Point3D(0, 1, 0)));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRot);

        cylinder1.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        
        /***********************************************************************/
        
        Cylinder cylinder2 = new Cylinder(5, 100);
        cylinder2.setMaterial(cylinderMaterial);

        moveToMidpoint = new Translate(1350 / 2, 800/ 2,0);

        diff = new Point3D(500, 500, 0);
        rotateAroundCenter = new Rotate(Math.toDegrees(angle), axisOfRot);
        cylinder2.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        
        /***********************************************************************/
        Group group = new Group(cylinder1, cylinder2, H1, H2, O);
        SubScene subScene = new SubScene(root, root.getPrefWidth(), root.getPrefHeight(), true, SceneAntialiasing.BALANCED);
        subScene.setRoot(group);
        root.getChildren().add(subScene);
        
        RotateTransition rt = new RotateTransition(Duration.seconds(30), group);
        rt.setToAngle(360*100);
        rt.setAxis(new Point3D(5, 5, 5));
        rt.play();
        
        
        root.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                subScene.widthProperty().set(newValue.intValue());
            }
        });
    }    
    
}
