package hsegment.JObject.Swing.Text.xml.dtd;

public class DTDAttributeList {
    private String attributeName;
    private int attributeType;
    private DefaultValue defaultValue;
    private DTDAttributeList next;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
    public int getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(int attributeType) {
        this.attributeType = attributeType;
    }

    public DefaultValue getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(DefaultValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    public DTDAttributeList getNext() {
        return next;
    }

    public void setNext(DTDAttributeList next) {
        this.next = next;
    }

    public void checkNext(DTDAttributeList next){
        DTDAttributeList current = this;
        DTDAttributeList temp = this.next;
        while (temp != null){
            current = temp;
            temp = temp.getNext();
        }
        current.setNext(next);
    }

    @Override
    public String toString() {
        DTDAttributeList list = this;
        while(list != null) {
            System.out.println("-----------------------------------");
            System.out.println("attributeName: " + list.getAttributeName());
            System.out.println("attributeType: " + list.getAttributeType());
            System.out.println("defaultValue value: " + list.getDefaultValue().getValue());
            System.out.println("defaultValue type: " + list.getDefaultValue().getType());
            System.out.println("-----------------------------------");
            list = list.getNext();
        }
        return null;
    }
}
