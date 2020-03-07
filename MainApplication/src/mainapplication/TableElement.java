package mainapplication;

import java.io.Serializable;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.paint.Paint;

/**
 *
 * @author cstuser
 */
public class TableElement implements Serializable{
    private String elementName;
    private String elementNumber;
    private String color;

    public TableElement(String elementName, String elementNumber, String color) {
        this.elementName = elementName;
        this.elementNumber = elementNumber;
        this.color = color;
    }
    
    public TableElement(ObservableList<Node> children, Background color) {
        this.elementName = ((Label) children.get(1)).getText();
        this.elementNumber = ((Label) children.get(0)).getText();
        this.color = color.getFills().get(0).getFill().toString();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getElementNumber() {
        return elementNumber;
    }

    public void setElementNumber(String elementNumber) {
        this.elementNumber = elementNumber;
    }
}
