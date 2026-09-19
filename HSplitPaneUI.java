/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsplitpane;

import javax.swing.plaf.PanelUI;

/** 
 * Contrat abstrait du UI delegate de HSplitPane.
 *
 * Même rôle que SplitPaneUI pour JSplitPane : une classe
 * abstraite VIDE, qui ne sert que de type — pour UIManager (recherche par
 * getUIClassID()) et pour HSplitPane.setUI(HSplitPaneUI). Toute la logique
 * concrète vit dans les implémentations (BasicHSplitPaneUI aujourd'hui).
 *
 * @author FIDELE
 */
public class HSplitPaneUI extends PanelUI {
}
