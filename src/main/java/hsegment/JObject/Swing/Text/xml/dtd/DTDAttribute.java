package hsegment.JObject.Swing.Text.xml.dtd;

public class DTDAttribute {
    private String elementName;
    private DTDAttributeList attributeList;

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public DTDAttributeList getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(DTDAttributeList attributeList) {
        this.attributeList = attributeList;
    }

    @Override
    public String toString() {
        System.out.println("########################");
        System.out.println("elementName: " + elementName);
        System.out.println(attributeList);
        return null;
    }
}
