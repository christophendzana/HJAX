/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package hsegment.JObject.UnitaryTest;

import hsegment.JObject.Swing.Text.xml.Parser;
import hsegment.JObject.Swing.Text.xml.handler.InstructionTagHandler;

import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author DELL
 */
public class HSegmentJObjects {

    public static void main(String[] args) throws IOException {
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
        CommentHandlerUT commentHandlerUT = new CommentHandlerUT();
        TextHandlerUT textHandlerUT = new TextHandlerUT();
        TagHandlerUT tagHandlerUT = new TagHandlerUT();
        ErrorHandlerUT errorHandlerUT = new ErrorHandlerUT();
        ValidatorHandlerUT validatorHandlerUT = new ValidatorHandlerUT();
//        Parser parser = new Parser();
//        parser.setTextHandler(textHandler);
//        parser.setTagHandler(tagHandler);
//        parser.setErrorHandler(erreorHandler);
//        parser.setComHandler(commentHandler);
//        parser.setValidatorHandler(validator);
        Parser p = new Parser();
        p.setCommentHandler(commentHandlerUT);
        p.setTextHandler(textHandlerUT);
        p.setTagHandler(tagHandlerUT);
        p.parse(new FileReader("src/main/java/hsegment/JObject/util/book.xml"));
       //parser.parse(new StringReader("<user id=\"20\">lorem </user>"));
               //+"<!doctype test [<!ELEMENT Cours (intervenant, plan)>]>"
               //+ "bonjour je suis <em>"));
               //int bit = 10;
               //System.out.println("bit >> 1 = "+(bit >> 2));
    }
}
