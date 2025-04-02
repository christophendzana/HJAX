package hsegment.JObject.Swing.Text.xml;

import java.util.List;

public class AttributeList implements  HDTDConstants {
    private String name;
    private String value;
    private List<?> values;
    private int type = ANY;
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

    public void setValue(String value) {
        this.value = value;
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

    public void checkNext(AttributeList next) {
        AttributeList temp = this.next;
        while (temp != null) {
            temp = temp.getNext();
        }
        temp = new AttributeList();
        temp.setName(next.getName());
        temp.setValue(next.getValue());
    }

    public void setNext(AttributeList next) {
        this.next = next;
    }

    public List<?> getValues() {
        return values;
    }
    public void setValues(List<?> values) {
        this.values = values;
    }

    public void setAttributeList(AttributeList attributeList) {
        this.next = attributeList;
    }

    public AttributeList(){}

    public AttributeList(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
