/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsupertable.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

/**
 *
 * @author FIDELE
 */
public class HFormulaDialog extends JDialog {

        /**
         * Vrai si l'utilisateur a cliqué OK.
         */
        private boolean confirmed = false;

        /**
         * Champ de saisie de la formule.
         */
        private final JTextField formulaField;

        /**
         * Liste des formats de nombre.
         */
        private final JComboBox<String> numberFormatCombo;

        /**
         * Liste des fonctions disponibles.
         */
        private final JComboBox<String> functionCombo;

        /**
         * Formule finale saisie par l'utilisateur.
         */
        private String formula = "";

        /**
         * Format de nombre sélectionné.
         */
        private String numberFormat = "";

        /**
         * Crée la boîte de dialogue.
         *
         * @param parent fenêtre parente
         * @param initialFormula formule pré-remplie (ex: "=SUM(ABOVE)")
         */
        public HFormulaDialog(java.awt.Window parent, String initialFormula) {
            super(parent, "Formule", Dialog.ModalityType.APPLICATION_MODAL);

            setResizable(false);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            // ── Panneau principal ─────────────────────────────────────────
            JPanel main = new JPanel(new BorderLayout(10, 12));
            main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
            main.setBackground(Color.WHITE);

            // ── Titre ─────────────────────────────────────────────────────
            JLabel title = new JLabel("Insérer une formule");
            title.setFont(new Font("Segoe UI", Font.BOLD, 14));
            title.setForeground(new Color(15, 23, 42));
            main.add(title, BorderLayout.NORTH);

            // ── Formulaire central ────────────────────────────────────────
            JPanel form = new JPanel();
            form.setLayout(new java.awt.GridBagLayout());
            form.setBackground(Color.WHITE);
            java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 8);
            gbc.anchor = java.awt.GridBagConstraints.WEST;

            // ── Ligne 1 : Formule ─────────────────────────────────────────
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0;
            JLabel formulaLabel = new JLabel("Formule :");
            formulaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            formulaLabel.setForeground(new Color(51, 65, 85));
            form.add(formulaLabel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            formulaField = new JTextField(
                    initialFormula != null && !initialFormula.isEmpty()
                    ? initialFormula : "=");
            formulaField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            formulaField.setPreferredSize(new Dimension(300, 30));
            formulaField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            form.add(formulaField, gbc);

            // ── Ligne 2 : Format de nombre ────────────────────────────────
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0;
            gbc.fill = java.awt.GridBagConstraints.NONE;
            JLabel formatLabel = new JLabel("Format de nombre :");
            formatLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            formatLabel.setForeground(new Color(51, 65, 85));
            form.add(formatLabel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            numberFormatCombo = new JComboBox<>(new String[]{
                "",
                "0",
                "0.00",
                "0.000",
                "#,##0",
                "#,##0.00",
                "0%",
                "0.00%",
                "0.00E+00",
                "$#,##0.00",
                "FCFA #,##0"
            });
            numberFormatCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            form.add(numberFormatCombo, gbc);

            // ── Ligne 3 : Insérer une fonction ────────────────────────────
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 0;
            gbc.fill = java.awt.GridBagConstraints.NONE;
            JLabel funcLabel = new JLabel("Insérer une fonction :");
            funcLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            funcLabel.setForeground(new Color(51, 65, 85));
            form.add(funcLabel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            functionCombo = new JComboBox<>(new String[]{
                "-- Sélectionner une fonction --",
                "SUM()", "AVERAGE()", "COUNT()", "MAX()", "MIN()", "PRODUCT()",
                "IF(;;)", "AND(;)", "OR(;)", "NOT()",
                "ABS()", "INT()", "MOD(;)", "ROUND(;)", "SIGN()",
                "DEFINED()", "TRUE", "FALSE"
            });
            functionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            // Insertion de la fonction dans le champ formule au clic
            functionCombo.addActionListener(e -> {
                String selected = (String) functionCombo.getSelectedItem();
                if (selected != null
                        && !selected.startsWith("--")) {
                    insertIntoFormula(selected);
                    functionCombo.setSelectedIndex(0);
                }
            });
            form.add(functionCombo, gbc);

            // ── Ligne 4 : Aide sur les arguments de position ──────────────
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            JLabel hint = new JLabel(
                    "<html><small><i>Arguments de position : "
                    + "LEFT, RIGHT, ABOVE, BELOW<br>"
                    + "Références : A1, B2:D5, R1C2, R1C1:R3C3</i></small></html>");
            hint.setForeground(new Color(100, 116, 139));
            hint.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            form.add(hint, gbc);

            main.add(form, BorderLayout.CENTER);

            // ── Boutons ───────────────────────────────────────────────────
            JPanel buttons = new JPanel(
                    new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
            buttons.setBackground(Color.WHITE);

            JButton cancelBtn = new JButton("Annuler");
            cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cancelBtn.setForeground(new Color(51, 65, 85));
            cancelBtn.setBackground(new Color(241, 245, 249));
            cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                    BorderFactory.createEmptyBorder(6, 16, 6, 16)));
            cancelBtn.setFocusPainted(false);
            cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cancelBtn.addActionListener(e -> dispose());

            JButton okBtn = new JButton("OK");
            okBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            okBtn.setForeground(Color.WHITE);
            okBtn.setBackground(new Color(13, 110, 253));
            okBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(9, 88, 217), 1),
                    BorderFactory.createEmptyBorder(6, 20, 6, 20)));
            okBtn.setFocusPainted(false);
            okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            okBtn.addActionListener(e -> {
                formula = formulaField.getText().trim();
                numberFormat = (String) numberFormatCombo.getSelectedItem();
                confirmed = true;
                dispose();
            });

            getRootPane().setDefaultButton(okBtn);

            buttons.add(cancelBtn);
            buttons.add(okBtn);
            main.add(buttons, BorderLayout.SOUTH);

            setContentPane(main);
            pack();
            setMinimumSize(new Dimension(460, 0));
            setLocationRelativeTo(parent);

            // Focus initial sur le champ formule, curseur en fin de texte
            formulaField.requestFocusInWindow();
            formulaField.setCaretPosition(formulaField.getText().length());
        }

        /**
         * Insère le texte de la fonction sélectionnée dans le champ formule à
         * la position du curseur.
         */
        private void insertIntoFormula(String funcText) {
            int caretPos = formulaField.getCaretPosition();
            String current = formulaField.getText();
            String newText = current.substring(0, caretPos)
                    + funcText
                    + current.substring(caretPos);
            formulaField.setText(newText);
            // Positionner le curseur à l'intérieur des parenthèses
            int newPos = caretPos + funcText.indexOf('(') + 1;
            if (funcText.contains("(")) {
                formulaField.setCaretPosition(
                        Math.min(newPos, newText.length()));
            } else {
                formulaField.setCaretPosition(
                        caretPos + funcText.length());
            }
            formulaField.requestFocus();
        }

        /**
         * Vrai si l'utilisateur a cliqué OK.
         */
        public boolean isConfirmed() {
            return confirmed;
        }

        /**
         * Retourne la formule saisie.
         */
        public String getFormula() {
            return formula;
        }

        /**
         * Retourne le format de nombre sélectionné.
         */
        public String getNumberFormat() {
            return numberFormat;
        }
    }