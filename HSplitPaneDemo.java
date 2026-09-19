/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package hsplitpane;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author FIDELE
 */
public class HSplitPaneDemo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test HSplitPane");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);

            // ---- Cas 1 : sans HSplitPaneConfig ----
            // updateUI() s'exécute avec une config par défaut créée en interne ;
            // les zones NORTH/SOUTH/WEST/EAST ont leurs tailles par défaut
            // (150px de hauteur / 200px de largeur), CENTER prend le reste.
            HSplitPane sansConfig = new HSplitPane();
            sansConfig.add(new JLabel("WEST", SwingConstants.CENTER), ZonePosition.WEST);
            sansConfig.add(new JLabel("CENTER", SwingConstants.CENTER), ZonePosition.CENTER);

            // ---- Cas 2 : avec HSplitPaneConfig ----
            // Utilisée quand on veut fixer une taille précise avant même la
            // construction.
            HSplitPaneConfig config = new HSplitPaneConfig();

            HSplitPane avecConfig = new HSplitPane(config);

            avecConfig.getZone(ZonePosition.EAST).setBackground(Color.BLUE);
            avecConfig.getZone(ZonePosition.WEST).setBackground(Color.cyan.darker());

            avecConfig.add(new JButton("Le button"), ZonePosition.WEST);
            avecConfig.add(new JButton("Le button"), ZonePosition.CENTER);
            avecConfig.add(new JButton("Le button"), ZonePosition.SOUTH);
            avecConfig.add(new JButton("Le button"), ZonePosition.EAST);
            avecConfig.add(new JButton("Le button"), ZonePosition.NORTH);

//            listerEnfants(avecConfig.getZone(ZonePosition.EAST).getContentPanel());

            frame.setContentPane(avecConfig);
            frame.setVisible(true);

//            System.out.println(avecConfig.getZone(ZonePosition.EAST).getBounds());
//            System.out.println(avecConfig.getZone(ZonePosition.EAST).getScrollPane().getBounds());
//            System.out.println(avecConfig.getZone(ZonePosition.EAST).getScrollPane().getViewport().getBounds());
//            System.out.println(avecConfig.getZone(ZonePosition.EAST).getContentPanel().getBounds());
        });
    }

    public static void listerEnfants(JPanel panel) {
        Component[] composants = panel.getComponents();

        if (composants.length == 0) {
            System.out.println("Le JPanel ne contient aucun composant enfant.");
            return;
        }

        System.out.println("--- Liste des composants enfants ---");
       for (int i = 0; i < composants.length; i++) {
            Component comp = composants[i];
            String typeComposant = comp.getClass().getName();
            String detailsEnfant = comp.toString();
            System.out.println("Enfant n°" + (i + 1) + " : " + typeComposant);
            System.out.println("Détails : " + detailsEnfant);
            System.out.println("------------------------------------");
        }

    }
}
