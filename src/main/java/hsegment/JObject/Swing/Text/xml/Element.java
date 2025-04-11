package hsegment.JObject.Swing.Text.xml;

/**
 * Implementation of data structure use to store a tag element.
 * @author Hyacinthe Tsague
 */
public class Element implements HDTDConstants {
    // the row where the tag is located in xml file
    private int rowIndex;
    // the column the tag is located in xml file
    private int colIndex;
    // the name of tag
    private String name;
    // the type of tag: open, close, empty, instruction tag
    private int type = ANY;
    // content model use in dtd
    private ContentModel contentModel;
    // the attribute of tag
    private AttributeList attributeList;

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public void setColIndex(int colIndex) {
        this.colIndex = colIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }

    public ContentModel getContentModel() {
        return contentModel;
    }
    public void setContentModel(ContentModel contentModel) {
        this.contentModel = contentModel;
    }
    public AttributeList getAttributeList() {
        return attributeList;
    }
    public void setAttributeList(AttributeList attributeList) {
        this.attributeList = attributeList;
    }
    public Element(int rowIndex, int colIndex, String name) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.name = name;
    }
}
