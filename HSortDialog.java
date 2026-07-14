/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package htextarea.sort;

import hcomponents.HButton;
import hcomponents.HComboBox;
import hcomponents.HRadioButton;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Boîte de dialogue principale "Trier le texte" — reproduit fidèlement la boîte
 * de dialogue Word de la rubrique Paragraphe → Trier.
 *
 * <p>
 * Contient :</p>
 * <ul>
 * <li>La section "Ligne d'en-tête" (Oui / Non)</li>
 * <li>Trois niveaux de tri hiérarchiques ("Trier par", "Puis par", "Puis
 * par")</li>
 * <li>Le bouton "Options..." qui ouvre {@link HSortOptionsDialog}</li>
 * </ul>
 *
 * <h3>Utilisation</h3>
 * <pre>
 *     HSortDialog dlg = new HSortDialog(parent, paragraphes);
 *     dlg.setVisible(true);
 *     if (dlg.isConfirme()) {
 *         List criteres = dlg.getCriteres();
 *         HSortOptions options = dlg.getOptions();
 *         // appeler HSortMoteur.trier(...)
 *     }
 * </pre>
 *
 * @author FIDELE
 * @version 1.0
 */
public class HSortDialog extends JDialog {

    // =========================================================================
    // Constantes
    // =========================================================================
    /**
     * Libellé affiché quand aucun critère secondaire n'est sélectionné.
     */
    private static final String AUCUN = "(aucun)";

    // =========================================================================
    // État
    // =========================================================================
    /**
     * Options globales partagées avec HSortOptionsDialog.
     */
    private final HSortOptions options = new HSortOptions();

    /**
     * Paragraphes à trier (pour calculer les colonnes disponibles).
     */
    private final List<HParagrapheAvecStyle> paragraphes;

    /**
     * {@code true} si l'utilisateur a cliqué OK.
     */
    private boolean confirme = false;

    // =========================================================================
    // Composants — En-tête
    // =========================================================================
    private final HRadioButton radioEnTeteOui = new HRadioButton("Oui");
    private final HRadioButton radioEnTeteNon = new HRadioButton("Non");

    // =========================================================================
    // Composants — Blocs de critères (3 niveaux)
    // =========================================================================
    /**
     * Un bloc de critère : champ + type + sens.
     */
    private static class BlocCritere {

        HComboBox<String> comboChamp = new HComboBox<>();
        HComboBox<String> comboType = new HComboBox<>(
                new String[]{"Texte", "Nombre", "Date"});
        HRadioButton radioCroissant = new HRadioButton("Croissant");
        HRadioButton radioDecroissant = new HRadioButton("Décroissant");
        ButtonGroup groupeSens = new ButtonGroup();

        BlocCritere() {
            groupeSens.add(radioCroissant);
            groupeSens.add(radioDecroissant);
            radioCroissant.setSelected(true);
        }
    }

    private final BlocCritere bloc1 = new BlocCritere();
    private final BlocCritere bloc2 = new BlocCritere();
    private final BlocCritere bloc3 = new BlocCritere();

    // =========================================================================
    // Boutons
    // =========================================================================
    private final HButton btnOK = new HButton("OK");
    private final HButton btnAnnuler = new HButton("Annuler");
    private final HButton btnOptions = new HButton("Options...");

    // =========================================================================
    // Constructeur
    // =========================================================================
    /**
     * Crée la boîte de dialogue de tri.
     *
     * @param parent la fenêtre parente
     * @param paragraphes les paragraphes à trier (pour détecter les colonnes)
     */
    public HSortDialog(Window parent, List<HParagrapheAvecStyle> paragraphes) {
        super(parent, "Trier le texte", ModalityType.APPLICATION_MODAL);
        this.paragraphes = paragraphes;

        construireInterface();
        remplirCombosChamps();
        configurerListeners();

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    // =========================================================================
    // Construction de l'interface
    // =========================================================================
    private void construireInterface() {
        setLayout(new BorderLayout(10, 10));
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

        // Zone principale : en-tête + critères
        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.add(creerPanneauEnTete());
        centre.add(Box.createVerticalStrut(8));
        centre.add(creerPanneauCritere("Trier par", bloc1, false));
        centre.add(Box.createVerticalStrut(6));
        centre.add(creerPanneauCritere("Puis par", bloc2, true));
        centre.add(Box.createVerticalStrut(6));
        centre.add(creerPanneauCritere("Puis par", bloc3, true));

        root.add(centre, BorderLayout.CENTER);
        root.add(creerPanneauBoutons(), BorderLayout.SOUTH);
        add(root);
    }

    /**
     * Section "Ligne d'en-tête".
     */
    private JPanel creerPanneauEnTete() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Liste",
                TitledBorder.LEFT, TitledBorder.TOP));

        ButtonGroup grp = new ButtonGroup();
        grp.add(radioEnTeteNon);
        grp.add(radioEnTeteOui);
        radioEnTeteNon.setSelected(true);

        p.add(new JLabel("Ligne d'en-tête :"));
        p.add(radioEnTeteOui);
        p.add(radioEnTeteNon);
        return p;
    }

    /**
     * Crée un panneau pour un niveau de tri (bloc critère).
     *
     * @param titre "Trier par" ou "Puis par"
     * @param bloc le bloc de composants
     * @param avecAucun si {@code true}, ajoute "(aucun)" en premier choix
     */
    private JPanel creerPanneauCritere(String titre, BlocCritere bloc,
            boolean avecAucun) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                titre,
                TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 6, 3, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Ligne 1 : Champ + Type
        gbc.gridx = 0;
        gbc.gridy = 0;
        p.add(new JLabel("Champ :"), gbc);

        gbc.gridx = 1;
        bloc.comboChamp.setPreferredSize(new Dimension(160, 24));
        if (avecAucun) {
            bloc.comboChamp.addItem(AUCUN);
        }
        p.add(bloc.comboChamp, gbc);

        gbc.gridx = 2;
        p.add(new JLabel("Type :"), gbc);

        gbc.gridx = 3;
        bloc.comboType.setPreferredSize(new Dimension(100, 24));
        p.add(bloc.comboType, gbc);

        // Ligne 2 : Sens
        gbc.gridx = 0;
        gbc.gridy = 1;
        p.add(new JLabel("Sens :"), gbc);

        gbc.gridx = 1;
        p.add(bloc.radioCroissant, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        p.add(bloc.radioDecroissant, gbc);

        return p;
    }

    /**
     * Panneau des boutons OK / Annuler / Options.
     */
    private JPanel creerPanneauBoutons() {
        JPanel p = new JPanel(new BorderLayout());

        JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        droite.add(btnOK);
        droite.add(btnAnnuler);

        JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        gauche.add(btnOptions);

        p.add(gauche, BorderLayout.WEST);
        p.add(droite, BorderLayout.EAST);
        return p;
    }

    // =========================================================================
    // Remplissage des combos de champs
    // =========================================================================
    /**
     * Remplit les combos "Champ" des trois blocs selon le nombre de colonnes
     * détectées dans les paragraphes (via le séparateur défini dans options).
     *
     * <p>
     * Appelée à l'initialisation et à chaque changement d'options (quand
     * l'utilisateur modifie le séparateur).</p>
     */
    private void remplirCombosChamps() {
        int nbColonnes = HSortMoteur.compterColonnesMax(paragraphes, options);

        List<String> choix = new ArrayList<>();

        if (nbColonnes <= 1) {
            // Pas de séparateur actif — un seul choix
            choix.add("Paragraphes");
        } else {
            // Séparateur actif — afficher les colonnes détectées
            for (int i = 1; i <= nbColonnes; i++) {
                choix.add("Colonne " + i);
            }
        }

        // Mettre à jour les trois combos en préservant la sélection si possible
        majComboChamp(bloc1.comboChamp, choix, false);
        majComboChamp(bloc2.comboChamp, choix, true);
        majComboChamp(bloc3.comboChamp, choix, true);
    }

    /**
     * Met à jour un combo champ avec les nouveaux choix.
     */
    private void majComboChamp(HComboBox<String> combo, List<String> choix,
            boolean avecAucun) {
        String selectionActuelle = (String) combo.getSelectedItem();

        combo.removeAllItems();
        if (avecAucun) {
            combo.addItem(AUCUN);
        }
        for (String c : choix) {
            combo.addItem(c);
        }

        // Restaurer la sélection si elle est toujours valide
        if (selectionActuelle != null) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItemAt(i).equals(selectionActuelle)) {
                    combo.setSelectedIndex(i);
                    return;
                }
            }
        }
        combo.setSelectedIndex(0);
    }

    // =========================================================================
    // Listeners
    // =========================================================================
    private void configurerListeners() {
        btnOK.addActionListener(e -> {
            confirme = true;
            dispose();
        });

        btnAnnuler.addActionListener(e -> dispose());

        btnOptions.addActionListener(e -> {
            // Ouvrir la boîte Options — si confirmée, mettre à jour les combos
            HSortOptionsDialog dlg = new HSortOptionsDialog(this, options);
            dlg.setVisible(true);
            if (dlg.isConfirme()) {
                // Le séparateur a peut-être changé — recalculer les colonnes
                remplirCombosChamps();
                // Propager l'en-tête vers les options
                options.setLigneEnTete(radioEnTeteOui.isSelected());
            }
        });

        // Synchroniser l'en-tête vers les options à chaque changement
        radioEnTeteOui.addActionListener(e -> options.setLigneEnTete(true));
        radioEnTeteNon.addActionListener(e -> options.setLigneEnTete(false));
    }

    // =========================================================================
    // Accesseurs résultat
    // =========================================================================
    /**
     * Indique si l'utilisateur a confirmé avec OK.
     *
     * @return {@code true} si OK a été cliqué
     */
    public boolean isConfirme() {
        return confirme;
    }

    /**
     * Retourne les trois critères de tri construits depuis l'interface. Les
     * critères inactifs (champ = "(aucun)") ont {@code isActif() = false}.
     *
     * @return liste de 3 critères (certains peuvent être inactifs)
     */
    public List<HSortCritere> getCriteres() {
        List<HSortCritere> liste = new ArrayList<>();
        liste.add(construireCritere(bloc1));
        liste.add(construireCritere(bloc2));
        liste.add(construireCritere(bloc3));
        return liste;
    }

    /**
     * Retourne les options globales de tri.
     *
     * @return les options configurées (avec ou sans passage par Options...)
     */
    public HSortOptions getOptions() {
        // Synchroniser l'en-tête une dernière fois
        options.setLigneEnTete(radioEnTeteOui.isSelected());
        return options;
    }

    // =========================================================================
    // Utilitaire — conversion bloc → HSortCritere
    // =========================================================================
    /**
     * Construit un {@link HSortCritere} depuis un bloc de composants.
     */
    private HSortCritere construireCritere(BlocCritere bloc) {
        String champ = (String) bloc.comboChamp.getSelectedItem();

        // Si "(aucun)" est sélectionné → critère inactif
        if (champ == null || champ.equals(AUCUN)) {
            return new HSortCritere();
        }

        // Type
        HSortCritere.Type type = switch (bloc.comboType.getSelectedIndex()) {
            case 1 ->
                HSortCritere.Type.NOMBRE;
            case 2 ->
                HSortCritere.Type.DATE;
            default ->
                HSortCritere.Type.TEXTE;
        };

        // Sens
        HSortCritere.Sens sens = bloc.radioDecroissant.isSelected()
                ? HSortCritere.Sens.DECROISSANT
                : HSortCritere.Sens.CROISSANT;

        return new HSortCritere(champ, type, sens);
    }
}
