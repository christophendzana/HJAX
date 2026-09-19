/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package IllustrationShape;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author FIDELE
 */
public class testShapeController {

    private HShapeEngineHandler shapeController;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> new testShapeController().afficher());
    }
    
    public void afficher() {
        JFrame frame = new JFrame("Test HShape");
        frame.setSize(1200, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (shapeController != null) {
                    shapeController.paint(g);
                }
            }
        };
        this.shapeController = new HShapeEngineHandler(panel);
        
        this.shapeController.addView(ShapeFactory.createShapeRectangle(100, 100, 120, 90));

        // Mise en page principale : panneau de contrôle à gauche, texte à droite
               
        frame.add(panel);
        frame.setVisible(true);
    }
    
}
