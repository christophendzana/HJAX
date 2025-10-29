/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIXPath;

/**
 * Exception levée lorsqu'une erreur de syntaxe est détectée
 * dans une expression XPath.
 *
 * Elle permet de préciser le message d'erreur ainsi que la position
 * du caractère fautif dans l'expression.
 * 
 * @author FIDELE
 */
public class XpathSyntaxException extends Exception{
    
     // Position du caractère où l'erreur s'est produite (facultatif)
    private int position;

    /**
     * Constructeur simple avec message.
     *
     * @param message Description de l'erreur.
     */
    public XpathSyntaxException(String message) {
        super(message);
        this.position = -1; // position inconnue
    }

    /**
     * Constructeur complet avec message et position.
     *
     * @param message  Description de l'erreur.
     * @param position Index du caractère fautif (si connu).
     */
    public XpathSyntaxException(String message, int position) {
        super(message + " (à la position " + position + ")");
        this.position = position;
    }

    /**
     * Retourne la position de l'erreur.
     */
    public int getPosition() {
        return position;
    }
    
}
