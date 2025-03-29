package hsegment.JObject.Swing.Text.xml;

public class Element implements HDTDConstants {
    private int rowIndex;
    private int colIndex;
    private String name;
    private int type = ANY;
    private ContentModel contentModel;
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
