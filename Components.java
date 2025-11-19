/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package hcomponents;

import hcomponents.models.HTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


/**
 *
 * @author FIDELE
 */
public class Components {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

//        
          SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test HPasswordField");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(450, 150);

//            HPasswordField hp = new HPasswordField();
//                                    
//            hp.setPreferredSize(new Dimension(280, 36));

//              HButton bt = new HButton("Button");

                HTextField tf = new HTextField("Placeholder");

              tf.setPreferredSize(new Dimension(280, 36));
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(Color.WHITE);
            panel.add(tf);

            frame.setContentPane(panel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });        
        
//       SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test HTextArea Ombre");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(500, 300);
//
//            HTextArea area = new HTextArea(6, 28);
//            area.setText("Bordure arrondie, rouge claire");
//
//            JPanel panel = new JPanel(new GridBagLayout());
//            panel.setBackground(Color.WHITE);
//            panel.add(area);
//
//            frame.setContentPane(panel);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });
        
//        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch(Exception e){}
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test HCheckBox");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            HCheckBox hcheckbox = new HCheckBox("J'accepte l'accord");
//            hcheckbox.setPreferredSize(new Dimension(220, 32));
//            JPanel panel = new JPanel(new GridBagLayout());
//            panel.setBackground(Color.WHITE);
//            panel.add(hcheckbox);
//            frame.setContentPane(panel);
//            frame.setSize(350, 100);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });
        
//         Test ToggleButton
        
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test HToggleButton");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//            HToggleButton htoggle = new HToggleButton("HToggle");
//            htoggle.setPreferredSize(new Dimension(180, 40));
//
//            JPanel panel = new JPanel(new GridBagLayout());
//            panel.setBackground(Color.WHITE);
//            panel.add(htoggle);
//
//            frame.setContentPane(panel);
//            frame.setSize(320, 120);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });
        
        
        /// Test ComboBox
      
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test HComboBox");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//            String[] items = {"Rouge", "Vert", "Bleu"};
//            HComboBox<String> combo = new HComboBox<>(items);
//            combo.setPreferredSize(new Dimension(220, 40)); // Taille customisée
//
//            // Panneau centré avec marges
//            JPanel panel = new JPanel();
//            panel.setLayout(new GridBagLayout());
//            panel.setBackground(Color.WHITE); // Pour bien voir la bordure/ombre
//            panel.add(combo);
//
//            frame.setContentPane(panel);
//            frame.setSize(350, 120);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });
//        
        
//        HTableModel model = createModel();
//
//        HTable table = new HTable(model); 
//        table.AjustColumnsToFitContent();
//
//        createDemoFrame(table);

    }

    private static HTableModel createModel() {
        HTableModel model = new HTableModel();

        // Définition des colonnes avec types
        model.addColumn("ID", Integer.class, true);
        model.addColumn("Nom", String.class, true);
        model.addColumn("Département", String.class, true);
        model.addColumn("Date Embauche", String.class, true);

        // Ajout des données de démonstration
        model.addRow(Arrays.asList(1, "Alice ", "Ressources Humaines", "2022-03-15"));
        model.addRow(Arrays.asList(2, "Bruno ", "Développement", "2021-07-22"));
        model.addRow(Arrays.asList(3, "Clara ", "Marketing", "2023-01-10"));
        model.addRow(Arrays.asList(4, "David ", "Finance", "2020-11-30"));
        model.addRow(Arrays.asList(5, "Émilie ", "Développement",  "2022-09-05"));
        model.addRow(Arrays.asList(6, "François ", "Support",  "2023-03-18"));
        model.addRow(Arrays.asList(7, "Gabrielle ", "Marketing",  "2021-12-12"));
        model.addRow(Arrays.asList(8, "Hugo Laurent", "Finance", "2020-05-25"));
        model.addRow(Arrays.asList(9, "Isabelle ", "Ressources Humaines",  "2022-11-08"));
        model.addRow(Arrays.asList(10, "Jérôme ", "Direction", "2019-08-14"));

        
        
        return model;
    }

    private static void createDemoFrame(HTable table) {
        JFrame frame = new JFrame("HTable Demo - Table Moderne Java Swing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        
        // ScrollPane pour la table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
     
        frame.add(scrollPane, BorderLayout.CENTER);       

        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null); // Centrer
        frame.setVisible(true);

    }


}
