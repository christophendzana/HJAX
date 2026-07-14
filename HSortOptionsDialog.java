/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package htextarea.sort;

import hcomponents.HButton;
import hcomponents.HCheckBox;
import hcomponents.HComboBox;
import hcomponents.HRadioButton;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Locale;
import java.util.Set;

/**
 * Boîte de dialogue "Options de tri"  
 * 
 * Permet de configurer : * 
 * - Le séparateur de champs (Tabulations, Points-virgules, Autre)
 * - Le respect de la casse
 * - La locale de tri
 * </ul>
 *
 *
 * @author FIDELE
 * @version 1.0
 */
public class HSortOptionsDialog extends JDialog {

    // =========================================================================
    // Composants
    // =========================================================================
    // Séparateur de champs
    private final HRadioButton radioAucun = new HRadioButton("Aucun (paragraphe entier)");
    private final HRadioButton radioTabulation = new HRadioButton("Tabulations");
    private final HRadioButton radioPointVirgule = new HRadioButton("Points-virgules");
    private final HRadioButton radioAutre = new HRadioButton("Autre :");
    private final JTextField champAutre = new JTextField(3);

    // Options de tri
    private final HCheckBox checkCasse = new HCheckBox("Respecter la casse");

    // Locale
    private final HComboBox<LocaleItem> comboLocale = new HComboBox<>();

    // Boutons
    private final HButton btnOK = new HButton("OK");
    private final HButton btnAnnuler = new HButton("Annuler");

    // =========================================================================
    // État
    // =========================================================================
    /**
     * Les options à modifier (modifiées en place si l'utilisateur clique OK).
     */
    private final HSortOptions options;

    /**
     * {@code true} si l'utilisateur a confirmé avec OK.
     */
    private boolean confirme = false;

    // =========================================================================
    // Constructeur
    // =========================================================================
    /**
     * Crée la boîte de dialogue Options de tri.
     *
     * @param parent la fenêtre parente
     * @param options les options à éditer (modifiées en place si OK)
     */
    public HSortOptionsDialog(Window parent, HSortOptions options) {
        super(parent, "Options de tri", ModalityType.APPLICATION_MODAL);
        this.options = options;

        construireInterface();
        initialiserDepuisOptions();
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
        getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(creerPanneauSeparateur(), BorderLayout.NORTH);
        add(creerPanneauOptions(), BorderLayout.CENTER);
        add(creerPanneauBoutons(), BorderLayout.SOUTH);
    }

    /**
     * Groupe "Séparer les champs par".
     */
    private JPanel creerPanneauSeparateur() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Séparer les champs par",
                TitledBorder.LEFT, TitledBorder.TOP));

        ButtonGroup groupe = new ButtonGroup();
        groupe.add(radioAucun);
        groupe.add(radioTabulation);
        groupe.add(radioPointVirgule);
        groupe.add(radioAutre);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 8, 3, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        p.add(radioAucun, gbc);

        gbc.gridy = 1;
        p.add(radioTabulation, gbc);

        gbc.gridy = 2;
        p.add(radioPointVirgule, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        p.add(radioAutre, gbc);

        gbc.gridx = 1;
        champAutre.setPreferredSize(new Dimension(40, 22));
        champAutre.setEnabled(false);
        p.add(champAutre, gbc);

        // Activer le champ texte seulement quand "Autre" est sélectionné
        radioAutre.addActionListener(e -> champAutre.setEnabled(true));
        radioAucun.addActionListener(e -> champAutre.setEnabled(false));
        radioTabulation.addActionListener(e -> champAutre.setEnabled(false));
        radioPointVirgule.addActionListener(e -> champAutre.setEnabled(false));

        return p;
    }

    /**
     * Groupe "Options de tri".
     */
    private JPanel creerPanneauOptions() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Options de tri",
                TitledBorder.LEFT, TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 8, 3, 8);

        // Casse
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        p.add(checkCasse, gbc);

        // Locale
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        p.add(new JLabel("Langue :"), gbc);

        gbc.gridx = 1;
        remplirComboLocale();
        comboLocale.setPreferredSize(new Dimension(200, 24));
        p.add(comboLocale, gbc);

        return p;
    }

    /**
     * Panneau boutons OK / Annuler.
     */
    private JPanel creerPanneauBoutons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.add(btnOK);
        p.add(btnAnnuler);
        return p;
    }

    /**
     * Remplit le combo de locales avec les locales disponibles.
     */
    private void remplirComboLocale() {
        // Locales les plus courantes en premier
        Locale[] localesPrincipales = {
            Locale.FRENCH, Locale.ENGLISH, Locale.GERMAN,
            Locale.ITALIAN, Locale.getDefault()
        };

        Set<String> dejaAjoutes = new java.util.LinkedHashSet<>();
        for (Locale l : localesPrincipales) {
            if (dejaAjoutes.add(l.toLanguageTag())) {
                comboLocale.addItem(new LocaleItem(l));
            }
        }

        // Ajouter le reste des locales disponibles
        for (Locale l : Locale.getAvailableLocales()) {
            if (l.getDisplayName().isBlank()) {
                continue;
            }
            if (dejaAjoutes.add(l.toLanguageTag())) {
                comboLocale.addItem(new LocaleItem(l));
            }
        }
    }

    // =========================================================================
    // Initialisation depuis les options existantes
    // =========================================================================
    private void initialiserDepuisOptions() {
        // Séparateur
        switch (options.getSeparateur()) {
            case TABULATION ->
                radioTabulation.setSelected(true);
            case POINT_VIRGULE ->
                radioPointVirgule.setSelected(true);
            case AUTRE -> {
                radioAutre.setSelected(true);
                champAutre.setEnabled(true);
                champAutre.setText(String.valueOf(options.getSeparateurAutre()));
            }
            default ->
                radioAucun.setSelected(true);
        }

        // Options
        checkCasse.setSelected(options.isRespecterCasse());

        // Locale
        Locale localeActuelle = options.getLocale();
        for (int i = 0; i < comboLocale.getItemCount(); i++) {
            if (comboLocale.getItemAt(i).locale.toLanguageTag()
                    .equals(localeActuelle.toLanguageTag())) {
                comboLocale.setSelectedIndex(i);
                break;
            }
        }
    }

    // =========================================================================
    // Listeners
    // =========================================================================
    private void configurerListeners() {
        btnOK.addActionListener(e -> {
            appliquerVersOptions();
            confirme = true;
            dispose();
        });

        btnAnnuler.addActionListener(e -> dispose());
    }

    /**
     * Recopie les valeurs de l'interface dans l'objet {@link HSortOptions}.
     */
    private void appliquerVersOptions() {
        // Séparateur
        if (radioTabulation.isSelected()) {
            options.setSeparateur(HSortOptions.Separateur.TABULATION);
        } else if (radioPointVirgule.isSelected()) {
            options.setSeparateur(HSortOptions.Separateur.POINT_VIRGULE);
        } else if (radioAutre.isSelected()) {
            options.setSeparateur(HSortOptions.Separateur.AUTRE);
            String texte = champAutre.getText();
            if (!texte.isEmpty()) {
                options.setSeparateurAutre(texte.charAt(0));
            }
        } else {
            options.setSeparateur(HSortOptions.Separateur.AUCUN);
        }

        // Options
        options.setRespecterCasse(checkCasse.isSelected());

        // Locale
        LocaleItem item = (LocaleItem) comboLocale.getSelectedItem();
        if (item != null) {
            options.setLocale(item.locale);
        }
    }

    // =========================================================================
    // Accesseur résultat
    // =========================================================================
    /**
     * Indique si l'utilisateur a confirmé avec OK.
     *
     * @return {@code true} si OK a été cliqué
     */
    public boolean isConfirme() {
        return confirme;
    }

    // =========================================================================
    // Classe interne — élément de la combo des locales
    // =========================================================================
    /**
     * Encapsule une {@link Locale} pour l'affichage dans le JComboBox.
     */
    private static class LocaleItem {

        final Locale locale;

        LocaleItem(Locale locale) {
            this.locale = locale;
        }

        @Override
        public String toString() {
            return locale.getDisplayName(Locale.FRENCH);
        }
    }
}
