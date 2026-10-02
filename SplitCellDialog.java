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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

/**
 *
 * @author FIDELE
 */
    public class SplitCellDialog extends JDialog {

        /**
         * Résultat de la boîte de dialogue.
         */
        private boolean confirmed = false;

        /**
         * Spinner pour le nombre de lignes.
         */
        private final JSpinner rowSpinner;

        /**
         * Spinner pour le nombre de colonnes.
         */
        private final JSpinner colSpinner;

        /**
         * Crée la boîte de dialogue.
         *
         * @param parent fenêtre parente
         */
        public SplitCellDialog(java.awt.Window parent) {
            super(parent, "Subdiviser en grille", Dialog.ModalityType.APPLICATION_MODAL);

            setResizable(false);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            // ── Panneau principal ─────────────────────────────────────────
            JPanel main = new JPanel(new BorderLayout(10, 10));
            main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
            main.setBackground(Color.WHITE);

            // ── Titre ─────────────────────────────────────────────────────
            JLabel title = new JLabel("Subdiviser la cellule en grille");
            title.setFont(new Font("Segoe UI", Font.BOLD, 14));
            title.setForeground(new Color(15, 23, 42));
            main.add(title, BorderLayout.NORTH);

            // ── Formulaire ────────────────────────────────────────────────
            JPanel form = new JPanel(new java.awt.GridLayout(2, 2, 12, 10));
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

            JLabel rowLabel = new JLabel("Nombre de lignes :");
            rowLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            rowLabel.setForeground(new Color(51, 65, 85));

            JLabel colLabel = new JLabel("Nombre de colonnes :");
            colLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            colLabel.setForeground(new Color(51, 65, 85));

            rowSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
            colSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));

            // Style des spinners
            styleSpinner(rowSpinner);
            styleSpinner(colSpinner);

            form.add(rowLabel);
            form.add(rowSpinner);
            form.add(colLabel);
            form.add(colSpinner);

            main.add(form, BorderLayout.CENTER);

            // ── Boutons ───────────────────────────────────────────────────
            JPanel buttons = new JPanel(new java.awt.FlowLayout(
                    java.awt.FlowLayout.RIGHT, 8, 0));
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
                confirmed = true;
                dispose();
            });

            // Valider avec Enter
            getRootPane().setDefaultButton(okBtn);

            buttons.add(cancelBtn);
            buttons.add(okBtn);
            main.add(buttons, BorderLayout.SOUTH);

            setContentPane(main);
            pack();
            setLocationRelativeTo(parent);
        }

        /**
         * Applique un style cohérent à un JSpinner.
         */
        private void styleSpinner(JSpinner spinner) {
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            spinner.setPreferredSize(new Dimension(80, 30));
            ((JSpinner.DefaultEditor) spinner.getEditor())
                    .getTextField().setHorizontalAlignment(JTextField.CENTER);
        }

        /**
         * Vrai si l'utilisateur a cliqué OK.
         *
         * @return true si confirmé
         */
        public boolean isConfirmed() {
            return confirmed;
        }

        /**
         * Retourne le nombre de lignes saisi.
         *
         * @return nombre de lignes
         */
        public int getRows() {
            return (Integer) rowSpinner.getValue();
        }

        /**
         * Retourne le nombre de colonnes saisi.
         *
         * @return nombre de colonnes
         */
        public int getCols() {
            return (Integer) colSpinner.getValue();
        }
    }

