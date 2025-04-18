package hsegment.JObject.Swing.Text.xml;

import java.util.ArrayList;
import java.util.List;

public class ContentModel {
    private char splitOperator;
    private List<Content> values;
    private List<ContentModel> content;
    private char operator;

    public ContentModel(){
        values = new ArrayList<>();
        content = new ArrayList<>();
    }

    public char getSplitOperator() {
        return splitOperator;
    }

    public void setSplitOperator(char splitOperator) {
        this.splitOperator = splitOperator;
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

    @Override
    public String toString() {
        System.out.println("SplitOperator: " + splitOperator);
        System.out.println("Operator: " + operator);
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
