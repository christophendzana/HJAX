package rubban;

import hcomponents.HButton;
import hcomponents.HPopupMenu;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Composant de bouton de débordement pour ruban (Ribbon Overflow Button)
 * 
 * Fonctionne comme le bouton "▼" dans Microsoft Office qui regroupe les éléments
 * qui n'ont pas assez d'espace pour s'afficher dans le ruban.
 * 
 * Ce composant peut stocker n'importe quel JComponent et les afficher dans
 * un menu popup lorsqu'on clique dessus.
 */
public class RibbonOverflowButton extends HButton {
    
    /** Menu popup qui contiendra les composants masqués */
    private final HPopupMenu popupMenu;
    
    /** Liste des composants actuellement cachés dans le bouton */
    private final List<JComponent> hiddenComponents;
    
    /** Panneau principal du popup pour organiser les composants */
    private final JPanel contentPanel;
    
    /** Largeur minimale du popup en pixels */
    private static final int MIN_POPUP_WIDTH = 180;
    
    /** Hauteur maximale du popup avant d'ajouter des scrollbars */
    private static final int MAX_POPUP_HEIGHT = 400;
    
    /** Marge intérieure entre les composants dans le popup */
    private static final int COMPONENT_MARGIN = 4;

    /**
     * Constructeur par défaut
     * Crée un bouton avec le texte "..." par défaut
     */
    public RibbonOverflowButton() {
        this("...");
    }
    
    /**
     * Constructeur avec texte personnalisé
     * @param text Texte à afficher sur le bouton
     */
    public RibbonOverflowButton(String text) {
        super(text);
        
        // Initialiser la liste des composants cachés
        hiddenComponents = new ArrayList<>();
        
        // Créer le popup menu
        popupMenu = new HPopupMenu();
        popupMenu.setLayout(new BorderLayout());
        
        // Créer le panneau de contenu avec gestion du défilement si nécessaire
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Ajouter le panneau de contenu dans un JScrollPane pour gérer les nombreux éléments
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        
        popupMenu.add(scrollPane, BorderLayout.CENTER);
        
        // Configurer l'action du bouton
        setupButtonAction();
        
        // Configurer l'apparence du bouton
        setupAppearance();
    }
    
    /**
     * Configure l'action du bouton (afficher/masquer le popup)
     */
    private void setupButtonAction() {
        this.addActionListener(e -> {
            if (hiddenComponents.isEmpty()) {
                return; // Ne rien faire si aucun composant n'est caché
            }
            
            if (popupMenu.isVisible()) {
                popupMenu.setVisible(false);
            } else {
                updatePopupSize();
                popupMenu.show(this, 0, this.getHeight());
            }
        });
    }
    
    /**
     * Configure l'apparence du bouton
     */
    private void setupAppearance() {
        // Style simple pour le bouton
        this.setFocusPainted(false);
        this.setMargin(new Insets(2, 6, 2, 6));
        this.setFont(this.getFont().deriveFont(Font.PLAIN, 11f));
    }
    
    /**
     * Ajoute un composant Swing au menu de débordement
     * Le composant sera caché dans le ruban et accessible via le popup
     * 
     * @param component Le composant à ajouter (JButton, JComboBox, etc.)
     */
    public void addComponent(JComponent component) {
        if (component == null) {
            return;
        }
        
        // Ajouter à la liste des composants cachés
        hiddenComponents.add(component);
        
        // Configurer les contraintes GridBag pour l'organisation
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(COMPONENT_MARGIN / 2, 0, COMPONENT_MARGIN / 2, 0);
        
        // Créer un conteneur pour le composant avec des marges
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(
            COMPONENT_MARGIN, COMPONENT_MARGIN, COMPONENT_MARGIN, COMPONENT_MARGIN
        ));
        container.add(component, BorderLayout.CENTER);
        
        // Ajouter le conteneur au panneau de contenu
        contentPanel.add(container, gbc);
        
        // Forcer la mise à jour de l'interface
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * Ajoute un séparateur dans le menu popup
     * Utile pour organiser les composants par groupe
     */
    public void addSeparator() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 0);
        
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        contentPanel.add(separator, gbc);
    }
    
    /**
     * Supprime tous les composants du menu de débordement
     */
    public void clearComponents() {
        hiddenComponents.clear();
        contentPanel.removeAll();
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * Met à jour la taille du popup en fonction des composants qu'il contient
     */
    private void updatePopupSize() {
        if (hiddenComponents.isEmpty()) {
            return;
        }
        
        // Calculer la taille préférée du panneau de contenu
        Dimension preferredSize = contentPanel.getPreferredSize();
        
        // Définir une largeur minimale et une hauteur maximale
        int width = Math.max(MIN_POPUP_WIDTH, preferredSize.width + 20);
        int height = Math.min(MAX_POPUP_HEIGHT, preferredSize.height + 10);
        
        // Définir la taille préférée du popup
        popupMenu.setPreferredSize(new Dimension(width, height));
    }
    
    /**
     * Récupère la liste des composants actuellement cachés
     * 
     * @return Copie de la liste des composants cachés
     */
    public List<JComponent> getHiddenComponents() {
        return new ArrayList<>(hiddenComponents);
    }
    
    /**
     * Vérifie si le bouton contient des composants cachés
     * 
     * @return true si au moins un composant est caché, false sinon
     */
    public boolean hasHiddenComponents() {
        return !hiddenComponents.isEmpty();
    }
    
    /**
     * Obtient le nombre de composants cachés
     * 
     * @return Le nombre de composants dans le menu de débordement
     */
    public int getHiddenComponentCount() {
        return hiddenComponents.size();
    }
    
    /**
     * Affiche le menu popup manuellement
     * Utile pour les tests ou pour déclencher l'affichage par programmation
     */
    public void showPopup() {
        if (hasHiddenComponents()) {
            updatePopupSize();
            popupMenu.show(this, 0, this.getHeight());
        }
    }
    
    /**
     * Masque le menu popup s'il est visible
     */
    public void hidePopup() {
        if (popupMenu.isVisible()) {
            popupMenu.setVisible(false);
        }
    }
    
    /**
     * Vérifie si le menu popup est actuellement visible
     * 
     * @return true si le popup est affiché, false sinon
     */
    public boolean isPopupVisible() {
        return popupMenu.isVisible();
    }
}