package mainapplication;

import java.io.Serializable;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 *
 * @author cstuser
 */
public class TableElement implements Serializable{
    private String elementName;
    private String elementNumber;

    public TableElement(String elementName, String elementNumber) {
        this.elementName = elementName;
        this.elementNumber = elementNumber;
    }
    
    public TableElement(ObservableList<Node> children) {
        this.elementName = ((Label) children.get(1)).getText();
        this.elementNumber = ((Label) children.get(0)).getText();
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
