package hsegment.JObject.Swing.Text.xml;

import hsegment.JObject.Swing.Text.xml.dtd.ContentModel;

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
    private int index;
    private char cardinal;

    public int getRowIndex() {
        return rowIndex;
    }

    protected void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    protected void setColIndex(int colIndex) {
        this.colIndex = colIndex;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }
    protected void setType(int type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    protected void setIndex(int index) {
        this.index = index;
    }

    public char getCardinal() {
        return cardinal;
    }

    protected void setCardinal(char cardinal) {
        this.cardinal = cardinal;
    }

    public ContentModel getContentModel() {
        return contentModel;
    }
    protected void setContentModel(ContentModel contentModel) {
        this.contentModel = contentModel;
    }
    public AttributeList getAttributeList() {
        return attributeList;
    }
    protected void setAttributeList(AttributeList attributeList) {
        this.attributeList = attributeList;
    }
    public Element(){}
    public Element(String name){
        this.name = name;
    }
    public Element(int rowIndex, int colIndex, String name) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.name = name;
    }
}
