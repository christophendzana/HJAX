package hsegment.JObject.Swing.Text.xml;


import java.util.ArrayList;
import java.util.List;

/**
 * A class use to implement attributes of open and empty tags of xml file.
 * @author Hyacinthe Tsague
 */
public class AttributeList implements  HDTDConstants {
    // the first attribute name
    private String name;
    // the first attribute value
    private String value;
    // the values of attribute with many values
    private List<String> values;
    private int type = ANY;
    // the rest of attributes
    private AttributeList next;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String attributeValue) {
        //if attribute has many values
        if(attributeValue.contains(" ")){
            // list value initialisation
            this.values = new ArrayList<>();
            // Get the values separate by space
            String[] values = attributeValue.split(" ");
            // save the default value
            this.value = values[0];
            // save the rest of values to the list
            for(int i = 1; i < values.length; i++){
                this.values.add(values[i]);
            }
            return;
        }
        this.value = attributeValue;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public AttributeList getNext() {
        return this.next;
    }

    /**
     * Check if attribute has store in linked list another attribute.
     * Used to save another attribute in the linked list.
     * @param next
     */
    public void checkNext(AttributeList next) {
        AttributeList temp = this.next;
        AttributeList current = this;
        while (temp != null) {
            current = temp;
            temp = temp.getNext();
        }
        temp = new AttributeList();
        temp.setName(next.getName());
        temp.setValue(next.getValue().trim());
        current.setNext(temp);
    }

    public void setNext(AttributeList next) {
        this.next = next;
    }

    public List<String> getValues() {
        return values;
    }
    public AttributeList(){}
}
