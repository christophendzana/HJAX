package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.xml.handler.EntityHandler;

/**
 * An implementation of EntityHandler interface.
 * @author Hyacinthe Tsague
 */
public class EntityHandlerImpl implements EntityHandler {
    // use with entity key value name/value
    @Override
    public void handleEntity(String name, String value) {
        //todo be implemented
    }

    // use to find an entity value depends on a name
    @Override
    public String handleEntity(String name) {
        String entityValue;
        // The default xml entity
        entityValue = switch (name){
            case "&lt;" -> "<";
            case "&gt;" -> ">";
            case "&amp;" -> "&";
            case "&quot;" -> "\"";
            case "&apos;" -> "'";
            // if it's not a default xml entity, we can get it from dtd or schema
            default -> getExternalEntity(name);
        };
        System.out.println("Entity name :"+name);
        System.out.println("Entity value :"+entityValue);
        return entityValue;
    }

    /**
     * Get the entity value in dtd or schema
     * @param entityName
     * @return entity value
     */
    private String getExternalEntity(String entityName) {
        //todo to be implemented
        return "";
    }
}
