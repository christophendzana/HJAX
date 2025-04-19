/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package hsegment.JObject.UnitaryTest;

import hsegment.JObject.Swing.Text.xml.dtd.ParserDTD;
import hsegment.JObject.Swing.Text.xml.handler.*;
import hsegment.JObject.Swing.Text.xml.handlerImpl.*;

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
        //ParserProcess parserProcess = new ParserProcess();
        //parserProcess.parse(new FileReader("src/main/java/hsegment/JObject/util/book.xml"));
        ParserDTD parserDTD = new ParserDTD();
        ErrorHandler errorHandler = new ErrorHandlerImpl();
        ElementHandler elementHandler = new ElementHandlerImpl(errorHandler);
        DTDAttributeHandler dtdAttributeHandler = new DTDAttributeHandlerImpl(errorHandler);
        parserDTD.setDtdAttributeHandler(dtdAttributeHandler);
        parserDTD.setElementHandler(elementHandler);
        parserDTD.setErrorHandler(errorHandler);
        parserDTD.parse(new FileReader("src/main/java/hsegment/JObject/util/catalog.dtd"));
    }
}
