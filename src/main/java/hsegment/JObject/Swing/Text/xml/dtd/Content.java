package hsegment.JObject.Swing.Text.xml.dtd;

public class Content {
    private String name;
    private char operator;

    public Content(){}

    public Content(String name, char operator) {
        this.name = name;
        this.operator = operator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char getOperator() {
        return operator;
    }

    public void setOperator(char operator) {
        this.operator = operator;
    }
}
