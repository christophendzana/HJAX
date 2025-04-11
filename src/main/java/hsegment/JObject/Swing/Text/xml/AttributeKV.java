package hsegment.JObject.Swing.Text.xml;

/**
 * Use to store temporary attribute name/value of open and empty tag when the xml file is reading.
 */
public class AttributeKV {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public AttributeKV(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
