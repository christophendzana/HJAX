package hsegment.JObject.Swing.Text.xml;

public class TagElement implements Constants {
    private Element element;
    private int type = ANY_TYPE;

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public TagElement(Element element, int type) {
        this.element = element;
        this.type = type;
    }
}
