/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package rubban;

import hcomponents.HButton;
import hcomponents.HCheckBox;
import hcomponents.HComboBox;
import hcomponents.HLabel;
import hcomponents.HPasswordField;
import hcomponents.HProgressBar;
import hcomponents.HSpinner;
import hcomponents.HTextArea;
import hcomponents.HTextField;
import hcomponents.HToggleButton;
import hcomponents.vues.HButtonStyle;
import hcomponents.vues.HComboBoxStyle;
import hcomponents.vues.shadow.HShadowFloating;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/**
 *
 * @author FIDELE
 */
public class Rubban {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

//        SwingUtilities.invokeLater(() -> {
//    // 1. Fenêtre
//    JFrame frame = new JFrame("Test Collapse Auto");
//    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    frame.setSize(1200, 600);
//    frame.setLocationRelativeTo(null);
//    frame.setLayout(new BorderLayout());
//
//    // 2. Ruban
//    Ribbon ribbon = new Ribbon();
//    
//    // 3. Modèle avec 5 groupes
//    DefaultHRibbonModel model = new DefaultHRibbonModel();
//    model.addGroup("Fichier");
//    model.addGroup("Édition");
//    model.addGroup("Affichage");
//    model.addGroup("Insertion");
//    model.addGroup("Outils");
//    
//    ribbon.setModel(model);
//    ribbon.setAutoCreateGroupsFromModel(true);
//    
//    // 4. Configuration
//    ribbon.setHeaderAlignment(Ribbon.HEADER_SOUTH);
//    ribbon.setGroupMargin(5);
//    ribbon.setHeaderMargin(3);
//    ribbon.setHeaderHeight(28);
        ////    ribbon.setFixedHeight(300);
//    
//    // Couleurs
//    ribbon.setDefaultHeaderBackground(new Color(230, 240, 255));
//    ribbon.setDefaultHeaderForeground(Color.BLACK);
//    ribbon.setDefaultHeaderFontSize(13);
//    ribbon.setDefaultHeaderCornerRadius(8);
//    
//    // 5. Remplir chaque groupe avec 5 composants
//    for (int g = 0; g < 5; g++) {
//        for (int i = 0; i < 5; i++) {
//            JButton btn = new JButton("Btn " + (i+1) + " - G" + g);
//            ribbon.addValue(btn, -1,g);
//        }
//    }
//    
//    // 6. Configurer le collapse auto
//    ribbon.setAutoCollapseEnabled(true);
//    ribbon.setCollapsedHeight(35);
//    
//    
//    // 7. Ajouter à la frame
//    frame.add(ribbon, BorderLayout.NORTH);
//    frame.setVisible(true);
//});
        
        SwingUtilities.invokeLater(() -> {
            // 1. Créer la fenêtre
            JFrame frame = new JFrame("Rezise HRibbon");
            frame.setResizable(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1500, 700);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            // 2. Créer le ruban AVEC auto-création activée
            Ribbon rubban = new Ribbon();
//            rubban.setRibbonHeigth(300);
            
            DefaultHRibbonModel rubbanModel = new DefaultHRibbonModel();
            rubbanModel.addGroup("Fichier");            
            rubbanModel.addGroup("Affichage");
            rubbanModel.addGroup("Outils");
            rubbanModel.addGroup("Aide");
            rubbanModel.addGroup("A propos");
            rubbanModel.addGroup("Groupe");
            
            rubban.setAutoCreateGroupsFromModel(true); // IMPORTANT !
            rubban.setModel(rubbanModel);
            
            rubban.setAutoCollapseEnabled(true);
//            rubban.setFixedHeight(270);
            rubban.setHeaderAlignment(Ribbon.HEADER_SOUTH);
            rubban.setHeaderHeight(30);
            rubban.setGroupMargin(5);
            rubban.setHeaderMargin(5);

            // Configuration globale
            rubban.setDefaultHeaderBackground(new Color(217, 218, 238));
            rubban.setDefaultHeaderForeground(Color.BLACK);
            rubban.setDefaultHeaderFontSize(14);
            rubban.setDefaultHeaderCornerRadius(15);
            rubban.setGroupMargin(5);
//            rubban.setUseOnlyWidth(true);
            rubban.setRibbonHeight(300);
            rubban.setHeaderAlignment(Ribbon.HEADER_SOUTH);
//            rubban.setDefaultGroupWidth(210);
            
            HRibbonGroup group = rubban.getGroup(1);
            group.setHeaderBackground(Color.CYAN);
            group.setHeaderFontBold(true);
            group.setHeaderCornerRadius(10);

//            rubban.getGroup(0).setWidth(225);
//            rubban.getGroup(1).setWidth(190);
//            rubban.getGroup(2).setWidth(180);                               
//            rubban.getGroup(3).setWidth(260);
//            rubban.getGroup(4).setWidth(270);
//            rubban.getGroup(5).setWidth(140);
            
            HButton btnNouveau = new HButton("Nouveau");
            btnNouveau.setButtonStyle(HButtonStyle.DANGER);
            
            HButton btnOuvrir = new HButton("Ouvrir");
            HButton btnEnregistrer = new HButton("Enregistrer");
            HTextField field1 = new HTextField("TextField simple", 20);
            field1.setButtonStyle(HButtonStyle.SUCCESS);
            HToggleButton htoggle = new HToggleButton("HToggle", true);
            htoggle.setPreferredSize(new Dimension(180, 50));
            htoggle.setButtonStyle(HButtonStyle.SUCCESS);
            htoggle.setShadow(new HShadowFloating());
            String[] fruits = {"Pomme", "Banane", "Orange", "Fraise", "Raisin", "Mangue"};
            HComboBox<String> combo = HComboBox.withStyle(fruits, HComboBoxStyle.INFO);
            HComboBox<String> combo2 = HComboBox.withStyle(fruits, HComboBoxStyle.SUCCESS);
            
            rubban.addValue(btnOuvrir, -1, 0);
            rubban.addValue(btnEnregistrer, -1, 0);
            rubban.addValue(htoggle, -1, 0);
            rubban.addValue(combo, -1, 0);
            rubban.addValue(btnNouveau, -1, 0);
            
            HButton btnColler = new HButton("Coller");
            HButton btnCouper = new HButton("Couper");
            
            rubban.addValue(btnColler, -1, 1);
            rubban.addValue(btnCouper, -1, 1);
            rubban.addValue(combo2, -1, 1);
            rubban.removeComponent(4, 1);
            rubban.insertComponent(new HButton("Annuler"), 0, 0);
            
            /// Group2
            HButton btnZoomIn = new HButton("Zoom +");
            HButton btnZoomOut = new HButton("Zoom -");
            HCheckBox chkGrid = new HCheckBox("Afficher grille");
            HCheckBox chkSnap = new HCheckBox("Snap actif");
            
            HTextField searchField = new HTextField("Rechercher...", 15);
            HToggleButton toggleDarkMode = new HToggleButton("Dark Mode", false);
            
            rubban.addValue(btnZoomIn, -1, 2);
            rubban.addValue(btnZoomOut, -1, 2);
            rubban.addValue(chkGrid, -1, 2);
            rubban.addValue(chkSnap, -1, 2);
            
            rubban.addValue(searchField, -1, 2);
            rubban.addValue(toggleDarkMode, -1, 2);
            
            HButton btnOptions = new HButton("Options");
            HButton btnPlugins = new HButton("Plugins");
            HProgressBar progress = new HProgressBar(0, 100);
            progress.setValue(40);
            HSpinner spinner = new HSpinner(new SpinnerNumberModel(5, 0, 100, 1));
            HComboBox<String> toolCombo = HComboBox.withStyle(
                    new String[]{"Outil A", "Outil B", "Outil C"},
                    HComboBoxStyle.WARNING
            );
            HPasswordField passwordField = new HPasswordField(12);
            HToggleButton toggleAutoSave = new HToggleButton("Auto-save", true);
            
            rubban.addValue(btnOptions, -1, 3);
            rubban.addValue(btnPlugins, -1, 3);
            rubban.addValue(progress, -1, 3);
            rubban.addValue(spinner, -1, 3);
            rubban.addValue(toolCombo, -1, 3);
            rubban.addValue(passwordField, -1, 3);
            rubban.addValue(toggleAutoSave, -1, 3);            
            
            HButton btnHelp = new HButton("Aide");
            HButton btnDocs = new HButton("Documentation");
            HButton btnAbout = new HButton("À propos");
            HLabel lblStatus = new HLabel("Statut : OK");
            HTextArea txtLogs = new HTextArea(3, 22);
            txtLogs.setText("Logs...");
            HCheckBox chkTips = new HCheckBox("Afficher astuces");
            HComboBox<String> languageCombo = HComboBox.withStyle(
                    new String[]{"FR", "EN", "DE"},
                    HComboBoxStyle.DANGER
            );
            
            rubban.addValue(btnHelp, -1, 4);
            rubban.addValue(btnDocs, -1, 4);
            rubban.addValue(btnAbout, -1, 4);
            rubban.addValue(lblStatus, -1, 4);
            rubban.addValue(txtLogs, -1, 4);
            rubban.addValue(chkTips, -1, 4);
            rubban.addValue(languageCombo, -1, 4);
            rubban.addValue(new HButton("sdfsd"), -1, 4);
            
            rubban.addValue(new HButton("Button"), -1, 5);            
            
            frame.add(rubban, BorderLayout.NORTH);
            
            frame.setVisible(true);
            
        });

// SwingUtilities.invokeLater(() -> {
//         
//       // Créer la fenêtre principale
//        JFrame frame = new JFrame("Exemple JScrollPane");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(400, 200); // Fenêtre de taille moyenne
//        
//        // 1. Créer le JPanel qui contiendra les boutons
//        JPanel panelBoutons = new JPanel();
//        panelBoutons.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Disposition horizontale
//        
//        // 2. Ajouter 5 boutons
//        for (int i = 1; i <= 5; i++) {
//            JButton bouton = new JButton("Bouton " + i);
//            bouton.setPreferredSize(new Dimension(100, 50)); // Taille fixe pour chaque bouton
//            panelBoutons.add(bouton);
//        }
//        
//        // 3. Créer le JScrollPane et y placer le panel des boutons
//        JScrollPane scrollPane = new JScrollPane(panelBoutons);
//        
//        // 4. Configurer les barres de défilement
//        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
//        
//        // 5. Ajouter le scrollPane à la fenêtre
//        frame.add(scrollPane, BorderLayout.CENTER);
//        
//        // Afficher la fenêtre
//        frame.setVisible(true);
//        
//        // Petit message pour expliquer le test
//        System.out.println("Test : Réduis la largeur de la fenêtre progressivement...");
//        System.out.println("La barre de défilement horizontale apparaîtra quand les boutons ne tiendront plus !");
//     
//     
//        });

//SwingUtilities.invokeLater(Rubban::createUI);


    }

//    private static void createUI() {
//        JFrame frame = new JFrame("PreferredSize Diagnostic");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        // FlowLayout respecte la preferredSize
//        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
//        
//        HButton comp = new HButton("Test Component");
//
//        // Bordure rouge pour visualiser la vraie bounding box
//        comp.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
//
//        // Log des tailles
//        Dimension pref = comp.getPreferredSize();
//        Dimension min = comp.getMinimumSize();
//        Dimension max = comp.getMaximumSize();
//
//        System.out.println("Preferred : " + pref);
//        System.out.println("Minimum   : " + min);
//        System.out.println("Maximum   : " + max);
//
//        panel.add(comp);
//
//        frame.setContentPane(panel);
//        frame.pack(); // important : utilise preferredSize
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);
//
//        // Log après affichage (après validation UI)
//        SwingUtilities.invokeLater(() -> {
//            System.out.println("After display Preferred : " + comp.getPreferredSize());
//            System.out.println("Actual size             : " + comp.getSize());
//        });
//    }
    
}
