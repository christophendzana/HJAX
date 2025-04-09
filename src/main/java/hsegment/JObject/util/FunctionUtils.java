package hsegment.JObject.util;

import hsegment.JObject.Swing.Text.xml.SyntaxRule;
import hsegment.JObject.Swing.Text.xml.TagElement;

public  class FunctionUtils {

    /**
     * Verify the letters after the first letter.The first letter is verify by <code>verifyStartName()</code> method
     * @param name
     * @return true if verified characters are valid
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
     * @param name
     * @return true if first character is valid.
     */
    public static boolean verifyStartName(String name){
        char c = name.charAt(0);
        return SyntaxRule.firstCharacterRule(c);
    }

    public static String getSourceError(TagElement tag){
        return " In Row : "+tag.getElement().getRowIndex()+", " + "Column : "+tag.getElement().getColIndex()+" of xml file. ";
    }
}
