/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package hsegment.JObject.UnitaryTest;

import hsegment.JObject.Swing.Text.xml.TagElement;
import hsegment.JObject.Swing.Text.xml.handler.*;
import hsegment.JObject.Swing.Text.html.parser.DTDParser;
import hsegment.JObject.Swing.Text.xml.Parser;
import hsegment.JObject.Swing.Text.xml.handlerImpl.*;
import hsegment.JObject.Swing.Text.xml.process.HandleDoctype;
import hsegment.JObject.Swing.Text.xml.process.HandlePrologue;

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
        ErrorHandler errorHandlerUT = new ErrorHandlerImpl();
        CommentHandler commentHandlerUT = new CommentHandlerImpl(errorHandlerUT);
        TextHandler textHandlerUT = new TextHandlerImpl();
        TagHandler tagHandlerUT = new TagHandlerImpl(errorHandlerUT);
        InstructionTagHandler instructionTagHandlerUT = new InstructionTagHandlerImpl();
        HandleDoctype doctypeHandler = new DoctypeHandlerImpl();
        HandlePrologue prologueHandler = new PrologueHandlerImpl();
        EntityHandler entityHandler = new EntityHandlerImpl();
        //TagStackHandler tagStackHandlerUT = new TagStackHandlerImpl();
        DequeueHandler<TagElement> dequeueHandler = new TagDequeueHandlerImpl();
        Parser p = new Parser();
        p.setCommentHandler(commentHandlerUT);
        p.setTextHandler(textHandlerUT);
        p.setTagHandler(tagHandlerUT);
        p.setErrorHandler(errorHandlerUT);
        p.setDoctypeHandler(doctypeHandler);
        p.setPrologueHandler(prologueHandler);
        p.setEntityHandler(entityHandler);
        p.setInstructionTagHandler(instructionTagHandlerUT);
        p.setDequeueHandler(dequeueHandler);
        p.parse(new FileReader("src/main/java/hsegment/JObject/util/book.xml"));
       //parser.parse(new StringReader("<user id=\"20\">lorem </user>"));
               //+"<!doctype test [<!ELEMENT Cours (intervenant, plan)>]>"
               //+ "bonjour je suis <em>"));
               //int bit = 10;
        // char c = 48;
              // System.out.println(c);
    }
}
