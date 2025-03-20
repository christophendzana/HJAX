/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package hsegment.JObject.UnitaryTest;

import hsegment.JObject.Swing.Text.CommentHandler;
import hsegment.JObject.Swing.Text.ValidatorHandler;
import hsegment.JObject.Swing.Text.html.parser.Parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;

/**
 *
 * @author DELL
 */
public class HSegmentJObjects {

    public static void main(String[] args) throws FileNotFoundException {
        boolean id = false , di = false; 
        
        /**JTextField text = new JTextField();
        JLabel label = new JLabel("bonjour c'est juste un test");
        HButton button = new HButton();
        button.setText("Voici");
        text.setPreferredSize(new Dimension(100, 20));
        HFrame f = new HFrame("bonjour");
        text.setBackground(Color.red);
        
        f.setSize(300, 300);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLocationRelativeTo(null);
        f.addTitleBarComponent(text);
        f.addTitleBarComponent(label, SwingConstants.RIGHT);
        f.addTitleBarComponent(button, SwingConstants.RIGHT);
        f.setVisible(true);**/
        TextHandlerUT textHandler = new TextHandlerUT();
        TagHandlerUT tagHandler = new TagHandlerUT();
        ErrorHandlerUT erreorHandler = new ErrorHandlerUT();
        CommentHandler commentHandler = new CommentHandlerUT();
        ValidatorHandler validator = new ValidatorHandlerUT();
        Parser parser = new Parser();
        parser.setTextHandler(textHandler);
        parser.setTagHandler(tagHandler);
        parser.setErrorHandler(erreorHandler);
        parser.setComHandler(commentHandler);
        parser.setValidatorHandler(validator);
        parser.parse(new FileReader("src/main/java/hsegment/JObject/util/book.xml"));
       //parser.parse(new StringReader("<user id=\"20\">lorem </user>"));
               //+"<!doctype test [<!ELEMENT Cours (intervenant, plan)>]>"
               //+ "bonjour je suis <em>"));
               //int bit = 10;
               //System.out.println("bit >> 1 = "+(bit >> 2));
    }
}
