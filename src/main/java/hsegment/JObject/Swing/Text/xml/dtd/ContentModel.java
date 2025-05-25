package hsegment.JObject.Swing.Text.xml.dtd;

import hsegment.JObject.Swing.Text.xml.Element;

import java.util.ArrayList;
import java.util.List;

public class ContentModel {
    private List<Content> values;
    private List<ContentModel> content;
    private char operator;
    private int type;
    private char cardinal;
    private int[] indexList;

    public ContentModel(){
        values = new ArrayList<>();
        content = new ArrayList<>();
    }

    public List<Content> getValues() {
        return values;
    }

    public void setValues(Content value) {
        this.values.add(value);
    }

    public List<ContentModel> getContent() {
        return content;
    }

    public void setContent(ContentModel content) {
        this.content.add(content);
    }

    public char getOperator() {
        return operator;
    }

    public void setOperator(char operator) {
        this.operator = operator;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public char getCardinal() {
        return cardinal;
    }

    public void setCardinal(char cardinal) {
        this.cardinal = cardinal;
    }

    protected boolean addElementIndex(String name){return false;}
    protected Element getElement(){
        return null;
    }
    protected void getElementList(List<String> elements){}

    @Override
    public String toString() {
        System.out.println("SplitOperator: " + operator);
        System.out.println("Operator: " + cardinal);
        values.forEach(content1 -> {
            System.out.println("--------------");
            System.out.println("name "+content1.getName());
            System.out.println("content operator "+content1.getOperator());
            System.out.println("----------------");
        });
        content.forEach(contentModel -> {
            System.out.println("***********Content child**************");
            System.out.println(contentModel);
            System.out.println("***************************************");
        });
        return "";
    }
}
