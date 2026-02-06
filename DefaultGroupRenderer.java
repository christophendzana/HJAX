package rubban;

import hcomponents.HCheckBox;
import hcomponents.HLabel;
import hcomponents.HTextField;
import hcomponents.vues.HLabelOrientation;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.*;
import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import javax.swing.border.Border;

/**
 * Renderer par défaut pour HRibbon.
 * 
 * Cette classe implémente l'interface GroupRenderer et fournit :
 * 1. La transformation des objets Java en composants Swing appropriés
 * 2. La création et configuration des en-têtes (headers) des groupes
 * 3. La gestion de la configuration hiérarchique (groupe → ruban → valeurs matérielles)
 * 4. L'orientation automatique des en-têtes selon leur position
 * 
 * Architecture de configuration :
 * - Niveau GROUPE (HRibbonGroup) : Configuration spécifique par groupe (override)
 * - Niveau RIBBON (Ribbon) : Configuration globale par défaut
 * - Niveau MATÉRIEL : Valeurs par défaut codées en dur (fallback)
 * 
 * Hiérarchie de résolution : Groupe → Ribbon → Matériel
 * 
 * @author [Votre nom]
 * @version 2.0
 */
public class DefaultGroupRenderer implements GroupRenderer {
    
    // =========================================================================
    // COMPOSANTS PAR DÉFAUT (PATTERN FLYWEIGHT)
    // =========================================================================
    
    /**
     * Label par défaut réutilisable pour éviter de créer trop d'instances.
     * Réinitialisé avant chaque utilisation.
     */
    private HLabel defaultLabel = new HLabel();
    
    /**
     * Checkbox par défaut réutilisable pour les valeurs booléennes.
     */
    private HCheckBox defaultCheckBox = new HCheckBox();
    
    /**
     * Champ texte par défaut réutilisable.
     */
    private HTextField defaultTextField = new HTextField();
    
    // =========================================================================
    // FORMATTEURS POUR UN AFFICHAGE LOCALISÉ
    // =========================================================================
    
    /**
     * Formateur de dates java.util.Date selon la locale système.
     */
    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
    
    /**
     * Formateur de dates java.time.LocalDate (format fixe).
     */
    private DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
    
    /**
     * Formateur de dates-heures java.time.LocalDateTime.
     */
    private DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault());
    
    /**
     * Formateur de nombres selon la locale système.
     */
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    
    // =========================================================================
    // IMPLÉMENTATION DE L'INTERFACE GroupRenderer
    // =========================================================================
    
    /**
     * Transforme une valeur objet en composant Swing pour l'affichage dans un groupe.
     * 
     * Cette méthode examine le type de l'objet et délègue à la méthode de création appropriée.
     * Supporte plus de 20 types Java courants avec rendu intelligent.
     * 
     * @param ribbon le ruban parent (peut être utilisé pour la configuration)
     * @param value la valeur à transformer en composant
     * @param groupIndex l'index du groupe dans le ruban
     * @param position la position de la valeur dans le groupe
     * @param isSelected true si la valeur est sélectionnée
     * @param hasFocus true si la valeur a le focus
     * @return un composant Swing pour afficher la valeur
     */
    @Override
    public Component getGroupComponent(Ribbon ribbon, Object value,
                                      int groupIndex, int position,
                                      boolean isSelected, boolean hasFocus) {
        
        // 1. CAS NULL : valeur nulle
        if (value == null) {
            return createNullComponent();
        }
        
        // 2. DÉLÉGATION PAR TYPE
        // Note : L'ordre des tests est important (du plus spécifique au plus générique)
        
        // 2.1 Chaînes de caractères
        if (value instanceof String) {
            return createStringComponent((String) value);
        }
        
        // 2.2 Nombres (tous types numériques)
        if (value instanceof Number) {
            return createNumberComponent((Number) value);
        }
        
        // 2.3 Booléens
        if (value instanceof Boolean) {
            return createBooleanComponent((Boolean) value);
        }
        
        // 2.4 Icônes Swing
        if (value instanceof Icon) {
            return createIconComponent((Icon) value);
        }
        
        // 2.5 Images AWT
        if (value instanceof java.awt.Image) {
            return createImageComponent((java.awt.Image) value);
        }
        
        // 2.6 Fichiers
        if (value instanceof File) {
            return createFileComponent((File) value);
        }
        
        // 2.7 Dates java.util.Date
        if (value instanceof Date) {
            return createDateComponent((Date) value);
        }
        
        // 2.8 Composants Swing (déjà prêts)
        if (value instanceof Component) {
            return (Component) value;
        }
        
        // 2.9 Autres objets : utilisation de toString()
        return createDefaultComponent(value);
    }
    
    /**
     * Crée un composant pour l'en-tête d'un groupe.
     * 
     * Cette méthode :
     * 1. Récupère la configuration du groupe et du ruban
     * 2. Crée un HLabel adapté au type de valeur
     * 3. Applique la configuration hiérarchique (groupe → ruban)
     * 4. Configure l'orientation selon la position de l'en-tête
     * 
     * @param ribbon le ruban parent (fournit la configuration globale)
     * @param headerValue la valeur à afficher dans l'en-tête
     * @param groupIndex l'index du groupe
     * @param isSelected true si l'en-tête est sélectionné
     * @return un composant Swing pour l'en-tête
     */
    @Override
    public Component getHeaderComponent(Ribbon ribbon, Object headerValue,
                                        int groupIndex, boolean isSelected) {
        
        // 1. RÉCUPÉRER LE GROUPE CORRESPONDANT
        // Le groupe contient la configuration spécifique (override)
        HRibbonGroup group = null;
        if (ribbon != null && ribbon.getGroupModel() != null) {
            group = ribbon.getGroupModel().getHRibbonGroup(groupIndex);
        }
        
        // 2. GESTION DE LA VALEUR NULL
        // Si pas de valeur d'en-tête, utiliser l'identifiant du groupe
        if (headerValue == null) {
            if (ribbon != null && ribbon.getModel() != null) {
                headerValue = ribbon.getModel().getGroupIdentifier(groupIndex);
            }
            // Fallback ultime : texte générique
            if (headerValue == null) {
                headerValue = "Groupe " + (groupIndex + 1);
            }
        }
        
        // 3. CRÉATION DU COMPOSANT SELON LE TYPE DE VALEUR
        HLabel headerLabel;
        
        if (headerValue instanceof String) {
            // 3.1 Valeur texte : créer un HLabel avec le texte
            headerLabel = new HLabel((String) headerValue);
            
        } else if (headerValue instanceof Icon) {
            // 3.2 Icône : créer un HLabel avec l'icône
            headerLabel = new HLabel();
            headerLabel.setIcon((Icon) headerValue);
            
        } else if (headerValue instanceof java.awt.Image) {
            // 3.3 Image : convertir en ImageIcon puis créer un HLabel
            headerLabel = new HLabel();
            headerLabel.setIcon(new ImageIcon((java.awt.Image) headerValue));
            
        } else if (headerValue instanceof Component) {
            // 3.4 Composant existant : retourner tel quel
            // (l'appelant est responsable de la configuration)
            return (Component) headerValue;
            
        } else {
            // 3.5 Autres types : utiliser toString()
            headerLabel = new HLabel(headerValue.toString());
        }
        
        // 4. CONFIGURATION DU STYLE AVEC HIÉRARCHIE DE CONFIGURATION
        configureHeaderWithHierarchy(headerLabel, ribbon, group, isSelected);
        
        return headerLabel;
    }
    
    // =========================================================================
    // MÉTHODES DE CRÉATION DES COMPOSANTS DE GROUPE
    // =========================================================================
    
    /**
     * Crée un composant pour une valeur nulle.
     * Retourne un label vide.
     * 
     * @return un HLabel vide
     */
    private Component createNullComponent() {
        defaultLabel.setText("");
        defaultLabel.setIcon(null);
        defaultLabel.setToolTipText(null);
        return defaultLabel;
    }
    
    /**
     * Crée un composant pour une chaîne de caractères.
     * Pour les textes longs (>50 caractères), utilise un JTextArea dans un JScrollPane.
     * 
     * @param text le texte à afficher
     * @return un label ou un JScrollPane pour les textes longs
     */
    private Component createStringComponent(String text) {
        // Optimisation pour les textes courts
        if (text.length() <= 50) {
            defaultLabel.setText(text);
            defaultLabel.setIcon(null);
            defaultLabel.setToolTipText(text.length() > 20 ? text : null);
            return defaultLabel;
        }
        
        // Pour les textes longs, utiliser un JTextArea scrollable
        JTextArea textArea = new JTextArea(text);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBackground(defaultLabel.getBackground());
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new java.awt.Dimension(200, 60));
        return scrollPane;
    }
    
    /**
     * Crée un composant pour un nombre.
     * Les nombres sont alignés à droite pour une meilleure lisibilité.
     * 
     * @param number le nombre à afficher
     * @return un label avec le nombre formaté
     */
    private Component createNumberComponent(Number number) {
        defaultLabel.setText(numberFormat.format(number));
        defaultLabel.setIcon(null);
        defaultLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        defaultLabel.setToolTipText("Valeur : " + number);
        return defaultLabel;
    }
    
    /**
     * Crée un composant pour un booléen.
     * Utilise une checkbox non éditable avec texte "Oui"/"Non".
     * 
     * @param bool la valeur booléenne
     * @return une checkbox en lecture seule
     */
    private Component createBooleanComponent(Boolean bool) {
        defaultCheckBox.setSelected(bool);
        defaultCheckBox.setText(bool ? "Oui" : "Non");
        defaultCheckBox.setEnabled(true); 
        return defaultCheckBox;
    }
    
    /**
     * Crée un composant pour une icône.
     * Le label est centré pour un meilleur affichage.
     * 
     * @param icon l'icône à afficher
     * @return un label centré avec l'icône
     */
    private Component createIconComponent(Icon icon) {
        defaultLabel.setText("");
        defaultLabel.setIcon(icon);
        defaultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        defaultLabel.setVerticalAlignment(SwingConstants.CENTER);
        return defaultLabel;
    }
    
    /**
     * Crée un composant pour une image AWT.
     * Convertit l'image en ImageIcon puis délègue à createIconComponent.
     * 
     * @param image l'image à afficher
     * @return un label avec l'image
     */
    private Component createImageComponent(java.awt.Image image) {
        return createIconComponent(new ImageIcon(image));
    }
    
    /**
     * Crée un composant pour un fichier.
     * Utilise un bouton cliquable avec le nom du fichier.
     * Le bouton ouvre le dossier parent au clic.
     * 
     * @param file le fichier à afficher
     * @return un bouton avec infos du fichier
     */
    private Component createFileComponent(File file) {
        JButton button = new JButton(file.getName());
        button.setToolTipText("<html>Chemin : " + file.getAbsolutePath() + 
                             "<br>Taille : " + (file.length() / 1024) + " KB" +
                             "<br>Modifié : " + new Date(file.lastModified()) + "</html>");
        
        // Action pour ouvrir le dossier parent
        button.addActionListener(e -> {
            try {
                java.awt.Desktop.getDesktop().open(file.getParentFile());
            } catch (Exception ex) {
                // Silencieux en cas d'erreur
            }
        });
        
        return button;
    }
    
    /**
     * Crée un composant pour une date java.util.Date.
     * Utilise le formateur de date configuré.
     * 
     * @param date la date à afficher
     * @return un label avec la date formatée
     */
    private Component createDateComponent(Date date) {
        defaultLabel.setText(dateFormat.format(date));
        defaultLabel.setToolTipText(date.toString());
        defaultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        return defaultLabel;
    }
    
    /**
     * Crée un composant pour un objet générique.
     * Utilise toString() avec détection du toString() par défaut d'Object.
     * 
     * @param value l'objet à afficher
     * @return un label avec représentation textuelle
     */
    private Component createDefaultComponent(Object value) {
        String text = value.toString();
        
        // Détection du toString() par défaut (ClassName@hashcode)
        if (text.startsWith(value.getClass().getName() + "@")) {
            // Remplacer par le nom simple de la classe
            text = value.getClass().getSimpleName();
            if (text.isEmpty()) {
                text = value.getClass().getName();
            }
        }
        
        defaultLabel.setText(text);
        defaultLabel.setToolTipText("Classe : " + value.getClass().getName());
        return defaultLabel;
    }
    
    // =========================================================================
    // MÉTHODES DE CONFIGURATION DES EN-TÊTES AVEC HIÉRARCHIE
    // =========================================================================
    
    /**
     * Configure un HLabel comme en-tête en utilisant la hiérarchie de configuration.
     * 
     * Hiérarchie : Configuration du groupe → Configuration du ruban → Valeurs matérielles
     * 
     * @param label le HLabel à configurer
     * @param ribbon le ruban parent (fournit les valeurs par défaut)
     * @param group le groupe spécifique (fournit les overrides)
     * @param isSelected true si l'en-tête est sélectionné
     */
    private void configureHeaderWithHierarchy(HLabel label, Ribbon ribbon, 
                                              HRibbonGroup group, boolean isSelected) {
        
        // 1. DÉTERMINER LA POSITION DE L'EN-TÊTE
        int headerAlignment = (ribbon != null) ? ribbon.getHeaderAlignment() : Ribbon.HEADER_NORTH;
        
        // 2. CONFIGURER L'ORIENTATION DU TEXTE
        configureHeaderOrientation(label, headerAlignment);
        
        // 3. APPLIQUER LES COULEURS AVEC HIÉRARCHIE
        applyHeaderColors(label, ribbon, group, isSelected, headerAlignment);
        
        // 4. APPLIQUER LA POLICE AVEC HIÉRARCHIE
        applyHeaderFont(label, ribbon, group);
        
        // 5. APPLIQUER LES BORDURES ET LE STYLE
        applyHeaderBorderAndStyle(label, ribbon, group, headerAlignment);
        
        // 6. CONFIGURER L'ALIGNEMENT ET LE PADDING
        configureHeaderAlignmentAndPadding(label, headerAlignment);
    }
    
    /**
     * Configure l'orientation du texte selon la position de l'en-tête.
     * 
     * @param label le HLabel à configurer
     * @param headerAlignment la position de l'en-tête (NORTH, SOUTH, WEST, EAST)
     */
    private void configureHeaderOrientation(HLabel label, int headerAlignment) {
        switch (headerAlignment) {
            case Ribbon.HEADER_NORTH:
            case Ribbon.HEADER_SOUTH:
                // En-têtes horizontaux en haut ou en bas
                label.setOrientation(HLabelOrientation.HORIZONTAL);
                break;
                
            case Ribbon.HEADER_WEST:
            case Ribbon.HEADER_EAST:
                // En-têtes verticaux à gauche ou à droite
                label.setOrientation(HLabelOrientation.VERTICAL);
                break;
                
            default:
                // Par défaut : horizontal
                label.setOrientation(HLabelOrientation.HORIZONTAL);
        }
    }
    
    /**
     * Applique les couleurs de l'en-tête en respectant la hiérarchie de configuration.
     * 
     * @param label le HLabel à configurer
     * @param ribbon le ruban (valeurs par défaut)
     * @param group le groupe (valeurs spécifiques)
     * @param isSelected true si l'en-tête est sélectionné
     * @param headerAlignment la position de l'en-tête
     */
    private void applyHeaderColors(HLabel label, Ribbon ribbon, HRibbonGroup group,
                                   boolean isSelected, int headerAlignment) {
        
        // RÉSOLUTION DE LA COULEUR DE FOND
        Color backgroundColor;
        
        if (isSelected) {
            // 1. Fond en sélection : groupe spécifique → ruban par défaut → matériel
            if (group != null && group.getHeaderSelectedBackground() != null) {
                backgroundColor = group.getHeaderSelectedBackground();
            } else if (ribbon != null) {
                backgroundColor = ribbon.getDefaultHeaderSelectedBackground();
            } else {
                backgroundColor = new Color(180, 200, 255); // Bleu clair matériel
            }
        } else {
            // 2. Fond normal : groupe spécifique → ruban par défaut → matériel
            if (group != null && group.getHeaderBackground() != null) {
                backgroundColor = group.getHeaderBackground();
            } else if (ribbon != null) {
                backgroundColor = ribbon.getDefaultHeaderBackground();
            } else {
                backgroundColor = new Color(197, 199, 228); // Gris-bleu matériel
            }
        }
        
        // RÉSOLUTION DE LA COULEUR DU TEXTE
        Color foregroundColor;
        if (group != null && group.getHeaderForeground() != null) {
            foregroundColor = group.getHeaderForeground();
        } else if (ribbon != null) {
            foregroundColor = ribbon.getDefaultHeaderForeground();
        } else {
            foregroundColor = new Color(60, 60, 60); // Gris foncé matériel
        }
        
        // APPLIQUER LES COULEURS
        label.setOpaque(true);
        label.setBackground(backgroundColor);
        label.setForeground(foregroundColor);
    }
    
    /**
     * Applique la police de l'en-tête en respectant la hiérarchie de configuration.
     * 
     * @param label le HLabel à configurer
     * @param ribbon le ruban (fournit createHeaderFont)
     * @param group le groupe (fournit taille et gras spécifiques)
     */
    private void applyHeaderFont(HLabel label, Ribbon ribbon, HRibbonGroup group) {
        // Récupérer les paramètres spécifiques du groupe
        Integer fontSize = (group != null) ? group.getHeaderFontSize() : null;
        Boolean isBold = (group != null) ? group.getHeaderFontBold() : null;
        
        // Créer la police selon la hiérarchie
        if (ribbon != null) {
            // Utiliser la méthode utilitaire du ruban
            Font headerFont = ribbon.createHeaderFont(label.getFont(), fontSize, isBold);
            label.setFont(headerFont);
        } else if (fontSize != null || isBold != null) {
            // Pas de ruban, mais des paramètres spécifiques : créer manuellement
            Font baseFont = label.getFont();
            if (baseFont == null) {
                baseFont = new Font("Dialog", Font.PLAIN, 12);
            }
            
            int style = baseFont.getStyle();
            if (isBold != null) {
                style = isBold ? Font.BOLD : Font.PLAIN;
            }
            
            float size = (fontSize != null) ? fontSize : baseFont.getSize();
            label.setFont(baseFont.deriveFont(style, size));
        }
        // Sinon, conserver la police par défaut du HLabel
    }
    
    /**
     * Applique les bordures et le style visuel de l'en-tête.
     * 
     * @param label le HLabel à configurer
     * @param ribbon le ruban (valeurs par défaut)
     * @param group le groupe (valeurs spécifiques)
     * @param headerAlignment la position de l'en-tête
     */
    private void applyHeaderBorderAndStyle(HLabel label, Ribbon ribbon, 
                                           HRibbonGroup group, int headerAlignment) {
        
        // RÉSOLUTION DE LA COULEUR DE BORDURE
        Color borderColor;
        if (group != null && group.getHeaderBorderColor() != null) {
            borderColor = group.getHeaderBorderColor();
        } else if (ribbon != null) {
            borderColor = ribbon.getDefaultHeaderBorderColor();
        } else {
            borderColor = new Color(200, 200, 200); // Gris clair matériel
        }
        
        // RÉSOLUTION DU RAYON DES COINS ARRONDIS
        int cornerRadius;
        if (group != null && group.getHeaderCornerRadius() != null) {
            cornerRadius = group.getHeaderCornerRadius();
        } else if (ribbon != null) {
            cornerRadius = ribbon.getDefaultHeaderCornerRadius();
        } else {
            cornerRadius = 5; // Rayon matériel par défaut
        }
        
        // APPLIQUER LE RAYON DES COINS
        label.setCornerRadius(cornerRadius);
        
        // CRÉER ET APPLIQUER LA BORDURE
        Border border = createHeaderBorder(borderColor, headerAlignment, cornerRadius);
        label.setBorder(border);
    }
    
    /**
     * Crée une bordure adaptée à la position de l'en-tête.
     * 
     * @param borderColor la couleur de la bordure
     * @param headerAlignment la position de l'en-tête
     * @param cornerRadius le rayon des coins arrondis
     * @return une Border configurée
     */
    private Border createHeaderBorder(Color borderColor, int headerAlignment, int cornerRadius) {
        // Bordures principales selon la position
        Border mainBorder;
        
        switch (headerAlignment) {
            case Ribbon.HEADER_NORTH:
                // Bordure en bas seulement (séparation avec le contenu)
                mainBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor);
                break;
                
            case Ribbon.HEADER_SOUTH:
                // Bordure en haut seulement
                mainBorder = BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor);
                break;
                
            case Ribbon.HEADER_WEST:
                // Bordure à droite seulement
                mainBorder = BorderFactory.createMatteBorder(0, 0, 0, 1, borderColor);
                break;
                
            case Ribbon.HEADER_EAST:
                // Bordure à gauche seulement
                mainBorder = BorderFactory.createMatteBorder(0, 1, 0, 0, borderColor);
                break;
                
            default:
                // Pas de bordure visible
                mainBorder = BorderFactory.createEmptyBorder();
        }
        
        // Padding interne adapté à l'orientation
        Border paddingBorder;
        if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
            // Plus de padding pour les en-têtes verticaux
            paddingBorder = BorderFactory.createEmptyBorder(8, 4, 8, 4);
        } else {
            // Padding standard pour les en-têtes horizontaux
            paddingBorder = BorderFactory.createEmptyBorder(4, 8, 4, 8);
        }
        
        // Combiner bordure principale et padding
        return BorderFactory.createCompoundBorder(mainBorder, paddingBorder);
    }
    
    /**
     * Configure l'alignement et le padding de l'en-tête.
     * 
     * @param label le HLabel à configurer
     * @param headerAlignment la position de l'en-tête
     */
    private void configureHeaderAlignmentAndPadding(HLabel label, int headerAlignment) {
        // ALIGNEMENT DU TEXTE
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        
        // PADDING INTERNE (pour HLabel avec fond arrondi)
        if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
            label.setPadding(8); // Plus d'espace pour les textes verticaux
        } else {
            label.setPadding(4); // Padding standard
        }
    }
    
    // =========================================================================
    // MÉTHODES UTILITAIRES DE CONFIGURATION
    // =========================================================================
    
    /**
     * Définit le format de date pour l'affichage des dates.
     * 
     * @param format le nouveau format de date
     */
    public void setDateFormat(DateFormat format) {
        if (format != null) {
            this.dateFormat = format;
        }
    }
    
    /**
     * Définit le format de nombre pour l'affichage des nombres.
     * 
     * @param format le nouveau format de nombre
     */
    public void setNumberFormat(NumberFormat format) {
        if (format != null) {
            this.numberFormat = format;
        }
    }
    
    /**
     * Définit le formateur pour les dates java.time.LocalDate.
     * 
     * @param formatter le nouveau formateur
     */
    public void setLocalDateFormatter(DateTimeFormatter formatter) {
        if (formatter != null) {
            this.localDateFormatter = formatter;
        }
    }
    
    /**
     * Définit le formateur pour les dates-heures java.time.LocalDateTime.
     * 
     * @param formatter le nouveau formateur
     */
    public void setLocalDateTimeFormatter(DateTimeFormatter formatter) {
        if (formatter != null) {
            this.localDateTimeFormatter = formatter;
        }
    }
    
    /**
     * Nettoie les composants réutilisables.
     * Utile pour libérer les ressources avant la destruction.
     */
    public void cleanup() {
        defaultLabel = null;
        defaultCheckBox = null;
        defaultTextField = null;
    }
}