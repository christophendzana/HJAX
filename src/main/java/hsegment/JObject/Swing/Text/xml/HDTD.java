package hsegment.JObject.Swing.Text.xml;

import hsegment.JObject.Swing.Text.html.parser.Notation;
import hsegment.JObject.Swing.Text.xml.dtd.ContentModel;
import hsegment.JObject.Swing.Text.xml.dtd.Entity;
import hsegment.JObject.util.Dictionnary;

public class HDTD {
    private Dictionnary<Element> elements;
    private Dictionnary<Entity> entities;
    private Dictionnary<Notation> notations;
    private String name;

    public HDTD(String name) {
        this.name = name;
        elements = new Dictionnary<>(27, 2,true,false);
    }

    public Element getElement(String name) {
        Element element = elements.get(name);
        if (element == null) {
            element = new Element(name);
            elements.add(element);
        }
        return element;
    }

    public Notation getNotations(String notation){
        return null;
    }

    public Element defineElement(String name){
        return null;
    }

    public Element defineElement(String name, int type, boolean omitEnd, ContentModel contentModel, AttributeList atts){
        Element element = getElement(name);
        element.setType(type);
        element.setAttributeList(atts);
        element.setContentModel(contentModel);
        return element;
    }

    public Element defineElement(int rowIndex, int columnIndex, Element element){
        element.setRowIndex(rowIndex);
        element.setColIndex(columnIndex);
        return element;
    }

    public Notation defineNotation(String notation){
        return null;
    }
}
