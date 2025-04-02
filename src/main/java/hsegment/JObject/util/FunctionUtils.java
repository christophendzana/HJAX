package hsegment.JObject.util;

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
        for(int i = 1; i < letters.length; i++){
            c = letters[i];
            // from ascii utf-8 table, invalid characters are between those intervals
            if((c < 45 || c == 47) || (c > 58 && c < 65) || (c > 90 && c < 95 || c == 96) || (c > 122)){
                return false;
            }
        }
        return true;
    }

    /**
     * Verify if tag name or attribute name is empty
     * @param name
     * @return boolean true if the tag name is not blank
     */
    public static boolean verifyEmptyName(String name){
        if(name.isEmpty()){
            return true;
        }
        char[] letters = name.toCharArray();
        int count = 0;
        for(char c : letters){
            if(c == ' '){
                count++;
            }
        }
        return count == letters.length;
    }

    /**
     * Verify if tag name or attribute name start with valid character. Character must be <code>_</code> or <code>:</code>
     * or lower/uppercase alphabetic letter.
     * @param name
     * @return true if first character is valid.
     */
    public static boolean verifyStartName(String name){
        char c = name.charAt(0);
        return c == '_' || c == ':' || (c >= 65 && c <= 90) || (c >= 97 && c <= 122);
    }

    public static String getSourceError(TagElement tag){
        return " In Row : "+tag.getElement().getRowIndex()+", " + "Column : "+tag.getElement().getColIndex()+" of xml file. ";
    }
}
