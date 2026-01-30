package hcomponents.HRibbon;

import java.awt.Component;
import javax.swing.*;
import java.io.File;
import java.util.Date;

/**
 * Renderer par défaut pour HRibbon.
 * Gère les types Java courants et fournit un rendu approprié.
 */
public class DefaultGroupRenderer implements GroupRenderer {
    
    /** Cache pour éviter de créer trop d'instances de composants */
    private JLabel defaultLabel = new JLabel();
    private JCheckBox defaultCheckBox = new JCheckBox();
    private JTextField defaultTextField = new JTextField();
    
    @Override
    public Component getGroupComponent(HRibbon ribbon, Object value,
                                      int groupIndex, int position,
                                      boolean isSelected, boolean hasFocus) {
        
        // 1. CAS NULL
        if (value == null) {
            return createNullComponent();
        }
        
        // 2. DÉLÉGATION PAR TYPE
        Class<?> valueClass = value.getClass();
        
        // 2.1 Chaînes de caractères
        if (value instanceof String) {
            return createStringComponent((String) value);
        }
        
        // 2.2 Nombres (Integer, Double, Float, etc.)
        if (value instanceof Number) {
            return createNumberComponent((Number) value);
        }
        
        // 2.3 Booléens
        if (value instanceof Boolean) {
            return createBooleanComponent((Boolean) value);
        }
        
        // 2.4 Icônes et Images
        if (value instanceof Icon) {
            return createIconComponent((Icon) value);
        }
        
        if (value instanceof java.awt.Image) {
            return createImageComponent((java.awt.Image) value);
        }
        
        // 2.5 Fichiers
        if (value instanceof File) {
            return createFileComponent((File) value);
        }
        
        // 2.6 Dates
        if (value instanceof Date) {
            return createDateComponent((Date) value);
        }
        
        // 2.7 Composants Swing (déjà prêts)
        if (value instanceof Component) {
            return (Component) value; // Retourne tel quel
        }
        
        // 2.8 Autres objets : utilisation de toString()
        return createDefaultComponent(value);
    }
    
    // =========================================================================
    // MÉTHODES DE CRÉATION SPÉCIFIQUES PAR TYPE
    // =========================================================================
    
    private Component createNullComponent() {
        defaultLabel.setText("");
        defaultLabel.setIcon(null);
        return defaultLabel;
    }
    
    private Component createStringComponent(String text) {
        defaultLabel.setText(text);
        defaultLabel.setIcon(null);
        return defaultLabel;
    }
    
    private Component createNumberComponent(Number number) {
        defaultLabel.setText(number.toString());
        defaultLabel.setIcon(null);
        return defaultLabel;
    }
    
    private Component createBooleanComponent(Boolean bool) {
        defaultCheckBox.setSelected(bool);
        defaultCheckBox.setText(bool ? "Oui" : "Non");
        return defaultCheckBox;
    }
    
    private Component createIconComponent(Icon icon) {
        defaultLabel.setText("");
        defaultLabel.setIcon(icon);
        return defaultLabel;
    }
    
    private Component createImageComponent(java.awt.Image image) {
        defaultLabel.setText("");
        defaultLabel.setIcon(new ImageIcon(image));
        return defaultLabel;
    }
    
    private Component createFileComponent(File file) {
        JButton button = new JButton(file.getName());
        button.setToolTipText(file.getAbsolutePath());
        return button;
    }
    
    private Component createDateComponent(Date date) {
        defaultLabel.setText(date.toString());
        return defaultLabel;
    }
    
    private Component createDefaultComponent(Object value) {
        defaultLabel.setText(value.toString());
        defaultLabel.setToolTipText("Classe: " + value.getClass().getName());
        return defaultLabel;
    }
    
    // =========================================================================
    // MÉTHODES UTILITAIRES POUR LA PERSONNALISATION
    // =========================================================================
    
    /**
     * Configure un composant avec le style par défaut du ruban.
     */
    protected void configureComponentStyle(JComponent component) {
        // À compléter avec le style du ruban
        component.setOpaque(true);
        component.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    }
}