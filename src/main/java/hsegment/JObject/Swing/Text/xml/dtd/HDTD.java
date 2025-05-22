package hsegment.JObject.Swing.Text.xml.dtd;

import hsegment.JObject.Swing.Text.html.parser.Notation;
import hsegment.JObject.Swing.Text.xml.AttributeList;
import hsegment.JObject.Swing.Text.xml.Element;
import hsegment.JObject.util.Dictionnary;

public class HDTD {
    private Dictionnary<Element> elements;
    private Dictionnary<Entity> entities;
    private Dictionnary<Notation> notations;

    public HDTD(String name) {}

    public Element getElement(String name) {
        return null;
    }

    public Notation getNotations(String notation){
        return null;
    }

    public Element defineElement(String name){
        return null;
    }

    public Element defineElement(String name, int type, boolean omitEnd, ContentModel contentModel, AttributeList atts){
        return null;
    }

    public Notation defineNotation(String notation){
        return null;
    }
}
