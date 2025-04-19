package hsegment.JObject.Swing.Text.xml.dtd;

import java.util.ArrayList;
import java.util.List;

public class DTDAttribute {
    private String elementName;
    private final List<DTDAttributeContent> attributeList;

    public DTDAttribute(){
        attributeList = new ArrayList<>();
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public List<DTDAttributeContent> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(DTDAttributeContent next) {
        this.attributeList.add(next);
    }

    public void setAll(List<DTDAttributeContent> attributeList) {
        this.attributeList.addAll(attributeList);
    }

    @Override
    public String toString() {
        System.out.println("--------------------");
        System.out.println("elementName: " + elementName);
        attributeList.forEach(a -> {
            System.out.println("--------");
            System.out.println("attributeName: " + a.getAttributeName());
            System.out.println("attributeType: " + a.getAttributeType());
            System.out.println("default value type :"+a.getDefaultValue().getType());
            System.out.println("default value value :"+a.getDefaultValue().getValue());
            System.out.println("--------");
        });
        System.out.println("-----------------------");
        return "";
    }
}
