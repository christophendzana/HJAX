/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package HRIbbonTabs;

import HRIbbonTabs.view.HRibbonTabsTheme;
import hcomponents.HButton;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author FIDELE
 */
public class HRibbonTabsTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("HRibbonTabs — Test visuel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 500);
        frame.setLayout(new BorderLayout());

        HRibbonTabs ribbonTabs = new HRibbonTabs(150);
        ribbonTabs.setTheme(HRibbonTabsTheme.PRIMARY);
        ribbonTabs.setAutoCollapseEnabled(true);
        
        ribbonTabs.addTab("Accueil");
        ribbonTabs.addTab("Insertion");
        ribbonTabs.addTab("Mise en page");
        
//        ribbonTabs.setActionPanelWidth(150);
        
        HButton button = new HButton("Action1");
        HButton button2 = new HButton("Action2");
        HButton button3 = new HButton("Action3");
        
        ribbonTabs.addComponent(button);       
        ribbonTabs.addComponent(button2); 
        ribbonTabs.addComponent(button3); 
        
        // Vérifie que le nouveau point d'extension se déclenche bien
        ribbonTabs.addRibbonTabListener(e ->
            System.out.println(e.getType() + " -> \"" + e.getTabTitle() + "\" (index " + e.getTabIndex() + ")")
        );

        frame.add(ribbonTabs, BorderLayout.NORTH);
        frame.add(new JLabel("Contenu sous le ruban", SwingConstants.CENTER), BorderLayout.CENTER);

        frame.setVisible(true);
    }
    
    
}
