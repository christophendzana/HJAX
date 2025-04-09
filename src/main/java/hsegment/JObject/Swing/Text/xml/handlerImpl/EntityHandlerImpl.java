package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.xml.handler.EntityHandler;

public class EntityHandlerImpl implements EntityHandler {
    @Override
    public void handleEntity(String name, String value) {

    }

    @Override
    public String handleEntity(String name) {
        String entityValue;
        entityValue = switch (name){
            case "&lt;" -> "<";
            case "&gt;" -> ">";
            case "&amp;" -> "&";
            case "&quot;" -> "\"";
            case "&apos;" -> "'";
            default -> getEntityFromDTD(name);
        };
        System.out.println("Entity name :"+name);
        System.out.println("Entity value :"+entityValue);
        return entityValue;
    }

    private String getEntityFromDTD(String entityName) {
        return "";
    }
}
