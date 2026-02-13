/*
 * DefaultOverflowProxyFactory.java
 * 
 * Implémentation par défaut de OverflowProxyFactory.
 * 
 * RESPONSABILITÉ :
 * Garantir la création de proxies pour TOUS les types de composants
 * que DefaultGroupRenderer est capable de produire.
 * 
 * COUVERTURE EXHAUSTIVE :
 * ✅ JButton, JToggleButton
 * ✅ JTextField, JTextArea, JPasswordField, JFormattedTextField
 * ✅ JComboBox
 * ✅ JList, JTable
 * ✅ JSlider, JProgressBar, JSpinner
 * ✅ JCheckBox, JRadioButton
 * ✅ JLabel
 * ❌ JPanel, JTree, autres → retourne null (ignoré)
 * 
 * @author FIDELE
 * @version 1.0
 */
package rubban;

import hcomponents.HButton;
import hcomponents.HCheckBox;
import hcomponents.HComboBox;
import hcomponents.HFormattedTextField;
import hcomponents.HLabel;
import hcomponents.HList;
import hcomponents.HPasswordField;
import hcomponents.HProgressBar;
import hcomponents.HRadioButton;
import hcomponents.HSlider;
import hcomponents.HSpinner;
import hcomponents.HTable;
import hcomponents.HTextArea;
import hcomponents.HTextField;
import hcomponents.HToggleButton;
import javax.swing.*;
import javax.swing.text.Document;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

public class DefaultOverflowProxyFactory implements OverflowProxyFactory {

    @Override
    public JComponent createProxy(JComponent original) {
        if (original == null) return null;

        // ============================================================
        // BOUTONS - Partage d'Action ou forwarding des ActionListeners
        // ============================================================
        if (original instanceof JButton) {
            return createButtonProxy((JButton) original);
        }
        if (original instanceof JToggleButton) {
            return createToggleButtonProxy((JToggleButton) original);
        }

        // ============================================================
        // COMPOSANTS TEXTE - Partage du Document
        // ============================================================
        if (original instanceof JTextField) {
            return createTextFieldProxy((JTextField) original);
        }
        if (original instanceof JTextArea) {
            return createTextAreaProxy((JTextArea) original);
        }
        if (original instanceof JPasswordField) {
            return createPasswordFieldProxy((JPasswordField) original);
        }
        if (original instanceof JFormattedTextField) {
            return createFormattedTextFieldProxy((JFormattedTextField) original);
        }

        // ============================================================
        // SÉLECTION - Partage du modèle
        // ============================================================
        if (original instanceof JComboBox) {
            return createComboBoxProxy((JComboBox<?>) original);
        }
        if (original instanceof JList) {
            return createListProxy((JList<?>) original);
        }
        if (original instanceof JTable) {
            return createTableProxy((JTable) original);
        }

        // ============================================================
        // VALEURS - Partage du BoundedRangeModel / SpinnerModel
        // ============================================================
        if (original instanceof JSlider) {
            return createSliderProxy((JSlider) original);
        }
        if (original instanceof JProgressBar) {
            return createProgressBarProxy((JProgressBar) original);
        }
        if (original instanceof JSpinner) {
            return createSpinnerProxy((JSpinner) original);
        }

        // ============================================================
        // BOOLÉENS - Partage du ButtonModel
        // ============================================================
        if (original instanceof JCheckBox) {
            return createCheckBoxProxy((JCheckBox) original);
        }
        if (original instanceof JRadioButton) {
            return createRadioButtonProxy((JRadioButton) original);
        }

        // ============================================================
        // AFFICHAGE - Copie des propriétés (lecture seule)
        // ============================================================
        if (original instanceof JLabel) {
            return createLabelProxy((JLabel) original);
        }

        // ============================================================
        // TYPE NON SUPPORTÉ - Retourne null (ignoré silencieusement)
        // ============================================================
        return null;
    }

    // ------------------------------------------------------------------------
    // BOUTONS
    // ------------------------------------------------------------------------

    private JButton createButtonProxy(JButton original) {
        HButton proxy = new HButton();

        // PRIORITÉ 1 : Partager l'Action (meilleure pratique)
        Action action = original.getAction();
        if (action != null) {
            proxy.setAction(action);
            return proxy;
        }

        // PRIORITÉ 2 : Copier les propriétés et forwarder les listeners
        proxy.setText(original.getText());
        proxy.setIcon(original.getIcon());
        proxy.setMnemonic(original.getMnemonic());
        proxy.setToolTipText(original.getToolTipText());
        proxy.setEnabled(original.isEnabled());
        proxy.setSelected(original.isSelected()); // Pour les boutons togglables

        // Forwarder les ActionListeners
        for (ActionListener al : original.getActionListeners()) {
            proxy.addActionListener(al);
        }

        // Forwarder les PropertyChangeListeners (pour enabled, text, etc.)
        for (PropertyChangeListener pcl : original.getPropertyChangeListeners()) {
            proxy.addPropertyChangeListener(pcl);
        }

        return proxy;
    }

    private JToggleButton createToggleButtonProxy(JToggleButton original) {
        HToggleButton proxy = new HToggleButton();

        // Même logique que JButton
        Action action = original.getAction();
        if (action != null) {
            proxy.setAction(action);
            return proxy;
        }

        proxy.setText(original.getText());
        proxy.setIcon(original.getIcon());
        proxy.setSelected(original.isSelected());
        proxy.setEnabled(original.isEnabled());

        for (ActionListener al : original.getActionListeners()) {
            proxy.addActionListener(al);
        }

        return proxy;
    }

    // ------------------------------------------------------------------------
    // COMPOSANTS TEXTE
    // ------------------------------------------------------------------------

    private JTextField createTextFieldProxy(JTextField original) {
        HTextField proxy = new HTextField();

        // PARTAGE CRITIQUE : le Document contient tout l'état
        Document doc = original.getDocument();
        if (doc != null) {
            proxy.setDocument(doc);
        }

        // Propriétés visuelles
        proxy.setText(original.getText()); // redondant mais sécurise
        proxy.setColumns(original.getColumns());
        proxy.setEditable(original.isEditable());
        proxy.setEnabled(original.isEnabled());

        return proxy;
    }

    private JTextArea createTextAreaProxy(JTextArea original) {
        HTextArea proxy = new HTextArea();

        Document doc = original.getDocument();
        if (doc != null) {
            proxy.setDocument(doc);
        }

        proxy.setText(original.getText());
        proxy.setRows(original.getRows());
        proxy.setColumns(original.getColumns());
        proxy.setLineWrap(original.getLineWrap());
        proxy.setWrapStyleWord(original.getWrapStyleWord());
        proxy.setEditable(original.isEditable());
        proxy.setEnabled(original.isEnabled());

        return proxy;
    }

    private JPasswordField createPasswordFieldProxy(JPasswordField original) {
        HPasswordField proxy = new HPasswordField();

        Document doc = original.getDocument();
        if (doc != null) {
            proxy.setDocument(doc);
        }

        proxy.setColumns(original.getColumns());
        proxy.setEditable(original.isEditable());
        proxy.setEnabled(original.isEnabled());

        return proxy;
    }

    private JFormattedTextField createFormattedTextFieldProxy(JFormattedTextField original) {
        HFormattedTextField proxy = new HFormattedTextField();

        // Partager le formatter ET le document
        proxy.setFormatterFactory(original.getFormatterFactory());
        
        Document doc = original.getDocument();
        if (doc != null) {
            proxy.setDocument(doc);
        }

        proxy.setValue(original.getValue());
        proxy.setColumns(original.getColumns());
        proxy.setEditable(original.isEditable());
        proxy.setEnabled(original.isEnabled());

        return proxy;
    }

    // ------------------------------------------------------------------------
    // SÉLECTION
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private JComboBox<Object> createComboBoxProxy(JComboBox<?> original) {
        HComboBox<Object> proxy = new HComboBox<>();

        // PARTAGE CRITIQUE : le ComboBoxModel
        ComboBoxModel<?> model = original.getModel();
        if (model != null) {
            proxy.setModel((ComboBoxModel<Object>) model);
        }

        proxy.setSelectedItem(original.getSelectedItem());
        proxy.setEditable(original.isEditable());
        proxy.setEnabled(original.isEnabled());

        return proxy;
    }

    @SuppressWarnings("unchecked")
    private HList<Object> createListProxy(JList<?> original) {
        HList<Object> proxy = new HList<>();

        // PARTAGE : ListModel
        ListModel<?> model = original.getModel();
        if (model != null) {
            proxy.setModel((ListModel<Object>) model);
        }

        // PARTAGE : ListSelectionModel
        ListSelectionModel selectionModel = original.getSelectionModel();
        if (selectionModel != null) {
            proxy.setSelectionModel(selectionModel);
        }

        proxy.setSelectedIndex(original.getSelectedIndex());
        proxy.setEnabled(original.isEnabled());

        return proxy;
    }

    private JTable createTableProxy(JTable original) {
        HTable proxy = new HTable();

        // PARTAGE : TableModel
        if (original.getModel() != null) {
            proxy.setModel(original.getModel());
        }

        // PARTAGE : ColumnModel
        if (original.getColumnModel() != null) {
            proxy.setColumnModel(original.getColumnModel());
        }

        // PARTAGE : SelectionModel
        if (original.getSelectionModel() != null) {
            proxy.setSelectionModel(original.getSelectionModel());
        }

        proxy.setEnabled(original.isEnabled());
        proxy.setRowHeight(original.getRowHeight());

        return proxy;
    }

    // ------------------------------------------------------------------------
    // VALEURS
    // ------------------------------------------------------------------------

    private JSlider createSliderProxy(JSlider original) {
        HSlider proxy = new HSlider();

        // PARTAGE CRITIQUE : BoundedRangeModel
        BoundedRangeModel model = original.getModel();
        if (model != null) {
            proxy.setModel(model);
        }

        proxy.setOrientation(original.getOrientation());
        proxy.setMajorTickSpacing(original.getMajorTickSpacing());
        proxy.setMinorTickSpacing(original.getMinorTickSpacing());
        proxy.setPaintTicks(original.getPaintTicks());
        proxy.setPaintLabels(original.getPaintLabels());
        proxy.setSnapToTicks(original.getSnapToTicks());

        if (original.getLabelTable() != null) {
            proxy.setLabelTable(original.getLabelTable());
        }

        proxy.setEnabled(original.isEnabled());

        return proxy;
    }

    private JProgressBar createProgressBarProxy(JProgressBar original) {
        HProgressBar proxy = new HProgressBar();

        BoundedRangeModel model = original.getModel();
        if (model != null) {
            proxy.setModel(model);
        }

        proxy.setOrientation(original.getOrientation());
        proxy.setStringPainted(original.isStringPainted());
        proxy.setString(original.getString());
        proxy.setEnabled(original.isEnabled());

        return proxy;
    }

    private JSpinner createSpinnerProxy(JSpinner original) {
        HSpinner proxy = new HSpinner();

        // PARTAGE : SpinnerModel
        SpinnerModel model = original.getModel();
        if (model != null) {
            proxy.setModel(model);
        }

        proxy.setValue(original.getValue());
        proxy.setEnabled(original.isEnabled());

        return proxy;
    }

    // ------------------------------------------------------------------------
    // BOOLÉENS
    // ------------------------------------------------------------------------

    private JCheckBox createCheckBoxProxy(JCheckBox original) {
        HCheckBox proxy = new HCheckBox();

        // PARTAGE CRITIQUE : ButtonModel (état coché/décoché)
        ButtonModel model = original.getModel();
        if (model != null) {
            proxy.setModel(model);
        }

        proxy.setText(original.getText());
        proxy.setIcon(original.getIcon());
        proxy.setSelectedIcon(original.getSelectedIcon());
        proxy.setEnabled(original.isEnabled());

        return proxy;
    }

    private JRadioButton createRadioButtonProxy(JRadioButton original) {
        HRadioButton proxy = new HRadioButton();

        ButtonModel model = original.getModel();
        if (model != null) {
            proxy.setModel(model);
        }

        proxy.setText(original.getText());
        proxy.setIcon(original.getIcon());
        proxy.setSelectedIcon(original.getSelectedIcon());
        proxy.setEnabled(original.isEnabled());

        return proxy;
    }

    // ------------------------------------------------------------------------
    // AFFICHAGE
    // ------------------------------------------------------------------------

    private JLabel createLabelProxy(JLabel original) {
        HLabel proxy = new HLabel();

        // JLabel = lecture seule, copie des propriétés
        proxy.setText(original.getText());
        proxy.setIcon(original.getIcon());
        proxy.setHorizontalAlignment(original.getHorizontalAlignment());
        proxy.setVerticalAlignment(original.getVerticalAlignment());
        proxy.setHorizontalTextPosition(original.getHorizontalTextPosition());
        proxy.setVerticalTextPosition(original.getVerticalTextPosition());
        proxy.setIconTextGap(original.getIconTextGap());
        proxy.setToolTipText(original.getToolTipText());
        proxy.setEnabled(original.isEnabled());

        return proxy;
    }
}