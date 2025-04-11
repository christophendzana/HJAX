package hsegment.JObject.Swing.Text.xml;

/**
 * A syntax rule implementation of xml content
 * @author Hyacinthe Tsague
 */
public class SyntaxRule {
    /**
     * First character rule validation
     * @param c the first character
     * @return <code>true</code> if it's valid, otherwise <code>false</code>
     */
    public static boolean firstCharacterRule(char c) {
        return switch (c) {
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', ':' -> true;
            default -> false;
        };
    }

    /**
     * Character rule validation. Used to validate characters that is not the first character
     * @param c the current character
     * @return <code>true</code> if it's valid, otherwise <code>false</code>
     */
    public static boolean charRuleOnName(char c) {
        return switch (c) {
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', ':', '-', '.' -> true;
            default -> false;
        };
    }

    /**
     * Invalid name validation
     * @param name a tag name or attribute name
     * @return <code>true</code> if it's valid, otherwise <code>false</code>
     */
    public static boolean invalidName(String name){
        return switch (name){
            case "xml" -> true;
            default -> false;
        };
    }
}
