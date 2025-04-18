package hsegment.JObject.util;

import hsegment.JObject.Swing.Text.xml.Element;
import hsegment.JObject.Swing.Text.xml.SyntaxRule;
import hsegment.JObject.Swing.Text.xml.TagElement;

/**
 * A utils functions
 * @author Hyacinthe Tsague
 */
public  class FunctionUtils {

    /**
     * Verify the letters after the first letter.The first letter is verify by <code>verifyStartName()</code> method
     * @param name a tag name or attribute name
     * @return <code>true</code> if verified characters are valid
     */
    public static boolean verifyName(String name){
        // Get the letters array of tag name or attribute name
        char[] letters = name.toCharArray();
        char c;
        boolean flag;
        for(int i = 1; i < letters.length; i++){
            c = letters[i];
            flag = SyntaxRule.charRuleOnName(c);
            // from ascii utf-8 table, invalid characters are between those intervals
            if(!flag){
                return false;
            }
        }
        return true;
    }

    /**
     * Verify if tag name or attribute name start with valid character. Character must be <code>_</code> or <code>:</code>
     * or lower/uppercase alphabetic letter.
     * @param name a tag name or attribute name
     * @return true if first character is valid.
     */
    public static boolean verifyStartName(String name){
        char c = name.charAt(0);
        return SyntaxRule.firstCharacterRule(c);
    }

    /**
     * A source of error, represent by a row and column of current reading character.
     * @param element an element
     * @return a string source
     */
    public static String getSourceError(Element element){
        return " In Row : "+element.getRowIndex()+", " + "Column : "+element.getColIndex()+" of xml file. ";
    }

    /**
     * A source of error, represent by a row and column of current reading character.
     * @param rowIndex a row in reading
     * @param colIndex a column in reading
     * @return a string source
     */
    public static String getSourceError(int rowIndex, int colIndex){
        return " In Row : "+rowIndex+", " + "Column : "+colIndex+" of xml file. ";
    }

    public static boolean verifyElementType(String elementType){
        return SyntaxRule.validElementType(elementType);
    }
}
