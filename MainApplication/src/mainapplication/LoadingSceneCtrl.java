package mainapplication;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
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

    private Label lblLoading = new Label("Loading Results");
    @FXML
    private AnchorPane root;
    
    private Group molecule;
    
    private VBox subRoot;
    
    private final double SCALE = 0.65; 
    
    /**
     * Initializes the Loading Scene controller by adding the 3D molecule to the scene and rotating it.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Sphere H1 = new Sphere(40);
        Sphere H2 = new Sphere(40);
        Sphere O = new Sphere(50);
        O.setMaterial(new PhongMaterial(Color.RED));
        H1.getTransforms().add(new Translate(TabTemplateCtrl.BOND_SIZE, 0, 0));
        double angle = 109 * Math.PI / 180;
        H2.getTransforms().add(new Translate(TabTemplateCtrl.BOND_SIZE * Math.cos(angle), TabTemplateCtrl.BOND_SIZE * Math.sin(angle), 0));
        /***********************************************************************/
        
        Cylinder cylinder1 = new Cylinder(5, TabTemplateCtrl.BOND_SIZE);
        PhongMaterial cylinderMaterial = new PhongMaterial(Color.BLACK);
        cylinderMaterial.setSpecularColor(Color.YELLOW);
        cylinder1.setMaterial(cylinderMaterial);
        
        Rotate rotateAroundCenter = new Rotate(90, Rotate.Z_AXIS);
        Translate trans = new Translate(TabTemplateCtrl.BOND_SIZE / 2, 0, 0);

        cylinder1.getTransforms().addAll(trans, rotateAroundCenter);
        
        /***********************************************************************/
        
        Cylinder cylinder2 = new Cylinder(5, TabTemplateCtrl.BOND_SIZE);
        cylinder2.setMaterial(cylinderMaterial);

        Rotate rotateAroundCenter2 = new Rotate(Math.toDegrees(angle) + 90, Rotate.Z_AXIS);
        Translate trans2 = new Translate(TabTemplateCtrl.BOND_SIZE * Math.cos(angle) / 2, TabTemplateCtrl.BOND_SIZE * Math.sin(angle) / 2, 0);
        
        cylinder2.getTransforms().addAll(trans2, rotateAroundCenter2);
        
        /***********************************************************************/
        molecule = new Group(cylinder1, cylinder2, H1, H2, O);
        subRoot = new VBox(lblLoading, molecule);
        subRoot.setAlignment(Pos.CENTER);
        SubScene subScene = new SubScene(root, root.getPrefWidth(), root.getPrefHeight(), true, SceneAntialiasing.BALANCED);
        subScene.setRoot(subRoot);
        PerspectiveCamera camera = new PerspectiveCamera(false);
        camera.setFieldOfView(70);
        subScene.setCamera(camera);
        subScene.heightProperty().bind(root.heightProperty());
        subScene.widthProperty().bind(root.widthProperty());
        
        //subRoot.layoutXProperty().bind(subScene.widthProperty().divide(2));
        //subRoot.layoutYProperty().bind(subScene.heightProperty().divide(2));
        root.getChildren().add(subScene);
        
        Scale scale = new Scale(SCALE, SCALE, SCALE);
        
        molecule.getTransforms().add(scale);
        
        RotateTransition rt = new RotateTransition(Duration.seconds(3), molecule);
        rt.setFromAngle(35.5);
        rt.setToAngle(720* 2 + 35.5);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.setInterpolator(Interpolator.EASE_BOTH);
        rt.setAxis(new Point3D(0, 0, 1));
        rt.play();
        
        lblLoading.setFont(new Font(40));
    }    
    
}
