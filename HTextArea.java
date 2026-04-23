package htextarea;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.awt.Insets;
import javax.swing.BorderFactory;

/**
 * Composant HTextArea - Un text area Swing personnalisé.
 *
 * <p>
 * Basé sur {@link JTextPane} afin de prendre en charge les styles par caractère
 * : police, taille, gras, italique, souligné, barré, couleur et casse.</p>
 *
 * <p>
 * Toutes les méthodes de style agissent sur la <strong>sélection
 * courante</strong>. Si aucun texte n'est sélectionné, le style est mémorisé et
 * s'appliquera au prochain texte saisi (comportement standard de
 * {@link JTextPane}).</p>
 *
 * @author FIDELE
 * @see JTextPane
 */
public class HTextArea extends JTextPane {

    /**
     * Rayon des coins arrondis (en pixels).
     */
    private int cornerRadius = 10;

    /**
     * Style visuel appliqué au composant.
     */
    private HTextAreaStyle textAreaStyle = HTextAreaStyle.PRIMARY;

// Couleurs de fond selon l'état
    private Color backgroundNormal;
    private Color backgroundHover;
    private Color backgroundFocus;

// Couleurs de bordure selon l'état
    private Color borderNormal;
    private Color borderHover;
    private Color borderFocus;

// Épaisseur de la bordure (en pixels)
    private int borderThickness = 1;

// Ombre portée du composant
    private boolean componentShadowEnabled = false;
    private Color componentShadowColor = new Color(0, 0, 0, 80);
    private int componentShadowOffset = 3;
    private float componentShadowBlur = 4f;

    private Insets padding = new Insets(8, 8, 8, 8);
    
    // -------------------------------------------------------------------------
    // Constructeurs
    // -------------------------------------------------------------------------
    /**
     * Constructeur par défaut.
     */
    public HTextArea() {
        super();
        updateUI();
        configureDefaults();
    }

    /**
     * Constructeur avec texte initial.
     *
     * @param text le texte initial à afficher
     */
    public HTextArea(String text) {
        super();
        updateUI();
        configureDefaults();
        setText(text);
    }

    /**
     * Constructeur avec un document stylisé existant.
     *
     * @param doc le document stylisé à utiliser comme modèle
     */
    public HTextArea(DefaultStyledDocument doc) {
        super(doc);
        updateUI();
        configureDefaults();
    }

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------
    /**
     * Configure les valeurs visuelles par défaut du composant.
     *
     * <p>
     * Note : {@code setLineWrap} et {@code setWrapStyleWord} n'existent pas sur
     * {@link JTextPane}. Le retour à la ligne automatique est géré nativement
     * par son modèle de rendu (basé sur
     * {@link javax.swing.text.WrappedPlainView}).</p>
     */
    private void configureDefaults() {
        setOpaque(false);
        setBorder(null);
        setBorder(BorderFactory.createEmptyBorder(padding.top, padding.left, padding.bottom, padding.right));
    }

    /**
     * Installe le UI delegate.
     */
    @Override
    public void updateUI() {
        setUI(new HBasicTextAreaUI());
    }

    // =========================================================================
    // GROUPE 1 — Police & taille
    // =========================================================================
    /**
     * Applique une famille de police à la sélection courante.
     *
     * @param family le nom de la famille de police
     */
    public void setFontFamily(String family) {
        if (family == null || family.isBlank()) {
            return;
        }
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, family);
        appliquerAttribut(attrs);
    }

    /**
     * Applique une taille de police (en points) à la sélection courante.
     *
     * @param size la taille en points (doit être &gt; 0)
     */
    public void setFontSize(int size) {
        if (size <= 0) {
            return;
        }
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontSize(attrs, size);
        appliquerAttribut(attrs);
    }

    /**
     * Augmente la taille de la police de la sélection courante d'un pas donné.
     *
     * @param step le nombre de points à ajouter (ex: 2)
     */
    public void increaseFontSize(int step) {
        modifierTaillePolice(+step);
    }

    /**
     * Réduit la taille de la police de la sélection courante d'un pas donné.
     *
     * @param step le nombre de points à retirer (ex: 2)
     */
    public void decreaseFontSize(int step) {
        modifierTaillePolice(-step);
    }

    /**
     * Réinitialise tous les attributs de style de la sélection courante.
     *
     * <p>
     * Équivalent du bouton "Effacer la mise en forme" de Word.</p>
     */
    public void clearFormatting() {
        int debut = getSelectionStart();
        int longueur = getSelectionEnd() - debut;
        if (longueur == 0) {
            return;
        }

        // Un SimpleAttributeSet vide avec replace=true supprime tous les attributs
        getStyledDocument().setCharacterAttributes(debut, longueur,
                new SimpleAttributeSet(), true);
    }

    // =========================================================================
    // GROUPE 2 — Style du texte
    // =========================================================================
    /**
     * Active ou désactive le gras sur la sélection courante.
     *
     * @param bold {@code true} pour mettre en gras, {@code false} pour retirer
     */
    public void setBold(boolean bold) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBold(attrs, bold);
        appliquerAttribut(attrs);
    }

    /**
     * Active ou désactive l'italique sur la sélection courante.
     *
     * @param italic {@code true} pour mettre en italique, {@code false} pour
     * retirer
     */
    public void setItalic(boolean italic) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setItalic(attrs, italic);
        appliquerAttribut(attrs);
    }

    /**
     * Active ou désactive le soulignement sur la sélection courante.
     *
     * @param underline {@code true} pour souligner, {@code false} pour retirer
     */
    public void setUnderline(boolean underline) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setUnderline(attrs, underline);
        appliquerAttribut(attrs);
    }

    /**
     * Active ou désactive le barré sur la sélection courante.
     *
     * @param strikethrough {@code true} pour barrer, {@code false} pour retirer
     */
    public void setStrikethrough(boolean strikethrough) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setStrikeThrough(attrs, strikethrough);
        appliquerAttribut(attrs);
    }

    /**
     * Bascule le gras : si la sélection est déjà en gras, le retire — sinon
     * l'active.
     *
     * <p>
     * Pratique pour un bouton toggle dans une barre d'outils.</p>
     */
    public void toggleBold() {
        setBold(!isSelectionBold());
    }

    /**
     * Bascule l'italique sur la sélection courante.
     */
    public void toggleItalic() {
        setItalic(!isSelectionItalic());
    }

    /**
     * Bascule le soulignement sur la sélection courante.
     */
    public void toggleUnderline() {
        setUnderline(!isSelectionUnderline());
    }

    /**
     * Bascule le barré sur la sélection courante.
     */
    public void toggleStrikethrough() {
        setStrikethrough(!isSelectionStrikethrough());
    }

    // =========================================================================
    // GROUPE 2b — Position (exposant / indice)
    // =========================================================================
    /**
     * Active ou désactive l'exposant sur la sélection courante.
     *
     * <p>
     * L'exposant élève le texte au-dessus de la ligne de base et le réduit
     * légèrement. Exemple : E = mc² → le "2" est en exposant.</p>
     *
     * <p>
     * Note : exposant et indice sont mutuellement exclusifs. Activer l'un
     * désactive automatiquement l'autre.</p>
     *
     * @param superscript {@code true} pour activer, {@code false} pour
     * désactiver
     */
    public void setSuperscript(boolean superscript) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setSuperscript(attrs, superscript);
        // Désactiver l'indice pour éviter un état incohérent
        if (superscript) {
            StyleConstants.setSubscript(attrs, false);
        }
        appliquerAttribut(attrs);
    }

    /**
     * Active ou désactive l'indice sur la sélection courante.
     *
     * <p>
     * L'indice abaisse le texte sous la ligne de base et le réduit légèrement.
     * Exemple : H₂O → le "2" est en indice.</p>
     *
     * @param subscript {@code true} pour activer, {@code false} pour désactiver
     */
    public void setSubscript(boolean subscript) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setSubscript(attrs, subscript);
        // Désactiver l'exposant pour éviter un état incohérent
        if (subscript) {
            StyleConstants.setSuperscript(attrs, false);
        }
        appliquerAttribut(attrs);
    }

    /**
     * Bascule l'exposant sur la sélection courante. Si déjà en exposant, le
     * retire — sinon l'active.
     */
    public void toggleSuperscript() {
        setSuperscript(!isSelectionSuperscript());
    }

    /**
     * Bascule l'indice sur la sélection courante. Si déjà en indice, le retire
     * — sinon l'active.
     */
    public void toggleSubscript() {
        setSubscript(!isSelectionSubscript());
    }

    // =========================================================================
    // GROUPE 3 — Couleur & surlignage
    // =========================================================================
    /**
     * Applique une couleur au texte de la sélection courante.
     *
     * @param color la couleur à appliquer (ne doit pas être {@code null})
     */
    public void setTextColor(Color color) {
        if (color == null) {
            return;
        }
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, color);
        appliquerAttribut(attrs);
    }

    /**
     * Applique une couleur de surlignage (fond du texte) à la sélection
     * courante.
     *
     * <p>
     * Équivalent du surlignage fluo de Word.</p>
     *
     * @param color la couleur de fond (ne doit pas être {@code null})
     */
    public void setHighlightColor(Color color) {
        if (color == null) {
            return;
        }
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBackground(attrs, color);
        appliquerAttribut(attrs);
    }

    /**
     * Supprime le surlignage de la sélection courante.
     */
    public void clearHighlight() {
        // On repasse la couleur de fond à null — JTextPane interprète null comme "pas de fond"
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBackground(attrs, getBackground());
        appliquerAttribut(attrs);
    }

    // =========================================================================
    // GROUPE 4 — Casse
    // =========================================================================
    /**
     * Convertit le texte sélectionné en MAJUSCULES.
     */
    public void toUpperCase() {
        transformerCasse(texte -> texte.toUpperCase(java.util.Locale.getDefault()));
    }

    /**
     * Convertit le texte sélectionné en minuscules.
     */
    public void toLowerCase() {
        transformerCasse(texte -> texte.toLowerCase(java.util.Locale.getDefault()));
    }

    /**
     * Met la première lettre de chaque mot en majuscule (style "Titre").
     *
     * <p>
     * Exemple : "bonjour monde" → "Bonjour Monde"</p>
     */
    public void toTitleCase() {
        transformerCasse(texte -> {
            if (texte.isEmpty()) {
                return texte;
            }
            StringBuilder sb = new StringBuilder(texte.length());
            boolean prochainEnMaj = true;
            for (char c : texte.toCharArray()) {
                if (Character.isWhitespace(c)) {
                    prochainEnMaj = true;
                    sb.append(c);
                } else if (prochainEnMaj) {
                    sb.append(Character.toUpperCase(c));
                    prochainEnMaj = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
            return sb.toString();
        });
    }

    /**
     * Inverse la casse de chaque caractère de la sélection.
     *
     * <p>
     * Exemple : "BoNjOuR" → "bOnJoUr"</p>
     */
    public void invertCase() {
        transformerCasse(texte -> {
            StringBuilder sb = new StringBuilder(texte.length());
            for (char c : texte.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    sb.append(Character.toLowerCase(c));
                } else if (Character.isLowerCase(c)) {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        });
    }

    /**
     * Simule les petites majuscules (Small Caps) sur la sélection courante.
     *
     * <p>
     * Swing n'ayant pas de support natif des petites majuscules, l'effet est
     * approché en passant le texte en MAJUSCULES et en réduisant la taille de
     * police de 2 points.</p>
     */
    public void toSmallCaps() {
        // Étape 1 : convertir en majuscules (en conservant les attributs)
        toUpperCase();
        // Étape 2 : réduire légèrement la taille pour simuler l'effet small caps
        modifierTaillePolice(-2);
    }

    // =========================================================================
    // GROUPE 5 — Effets typographiques
    // =========================================================================
    /**
     * Applique un effet typographique à la sélection courante.
     *
     * <p>
     * L'effet est stocké comme attribut custom dans le {@link StyledDocument}
     * via la clé {@link HEffectPainter#EFFECT_ATTRIBUTE}, puis lu et peint par
     * {@link hcomponents.vues.HBasicTextAreaUI} à chaque cycle de rendu.</p>
     *
     * <p>
     * Passer {@link HTextEffect#NONE} supprime tout effet sur la sélection.</p>
     *
     * @param effet l'effet à appliquer (ne doit pas être {@code null})
     */
    public void setTextEffect(HTextEffect effet) {
        if (effet == null) {
            return;
        }
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(HEffectPainter.EFFECT_ATTRIBUTE, effet);
        appliquerAttribut(attrs);
        // Forcer un repaint complet — les effets sont peints hors du pipeline standard
        repaint();
    }

    /**
     * Applique un effet typographique avec une couleur personnalisée.
     *
     * <p>
     * La couleur est utilisée différemment selon l'effet :</p>
     * <ul>
     * <li>{@code SHADOW} → couleur de l'ombre (ex: gris sombre)</li>
     * <li>{@code OUTLINE} → couleur du trait de contour</li>
     * <li>{@code GLOW} → couleur du halo lumineux</li>
     * <li>{@code EMBOSS} / {@code ENGRAVE} → la couleur est ignorée (les ombres
     * blanc/noir sont fixes pour un rendu réaliste)</li>
     * </ul>
     *
     * @param effet l'effet à appliquer
     * @param couleurEffet la couleur associée à l'effet
     */
    public void setTextEffect(HTextEffect effet, Color couleurEffet) {
        if (effet == null) {
            return;
        }
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(HEffectPainter.EFFECT_ATTRIBUTE, effet);
        if (couleurEffet != null) {
            attrs.addAttribute(HEffectPainter.EFFECT_COLOR_ATTRIBUTE, couleurEffet);
        }
        appliquerAttribut(attrs);
        repaint();
    }

    /**
     * Supprime tout effet typographique de la sélection courante.
     *
     * <p>
     * Raccourci pour {@code setTextEffect(HTextEffect.NONE)}.</p>
     */
    public void clearTextEffect() {
        setTextEffect(HTextEffect.NONE);
    }

    /**
     * Retourne l'effet typographique actif au niveau du curseur (ou de la
     * sélection).
     *
     * @return l'effet courant, ou {@link HTextEffect#NONE} si aucun n'est
     * défini
     */
    public HTextEffect getSelectionTextEffect() {
        Object valeur = getInputAttributes().getAttribute(HEffectPainter.EFFECT_ATTRIBUTE);
        if (valeur instanceof HTextEffect effet) {
            return effet;
        }
        return HTextEffect.NONE;
    }

    // =========================================================================
    // Interrogation de l'état courant (utile pour les boutons toggle)
    // =========================================================================
    /**
     * Indique si le texte au niveau du curseur (ou de la sélection) est en
     * gras.
     *
     * @return {@code true} si le style courant est gras
     */
    public boolean isSelectionBold() {
        return StyleConstants.isBold(getInputAttributes());
    }

    /**
     * Indique si le texte au niveau du curseur (ou de la sélection) est en
     * italique.
     *
     * @return {@code true} si le style courant est italique
     */
    public boolean isSelectionItalic() {
        return StyleConstants.isItalic(getInputAttributes());
    }

    /**
     * Indique si le texte au niveau du curseur (ou de la sélection) est
     * souligné.
     *
     * @return {@code true} si le style courant est souligné
     */
    public boolean isSelectionUnderline() {
        return StyleConstants.isUnderline(getInputAttributes());
    }

    /**
     * Indique si le texte au niveau du curseur (ou de la sélection) est barré.
     *
     * @return {@code true} si le style courant est barré
     */
    public boolean isSelectionStrikethrough() {
        return StyleConstants.isStrikeThrough(getInputAttributes());
    }

    /**
     * Indique si le texte au niveau du curseur est en exposant.
     *
     * @return {@code true} si le style courant est exposant
     */
    public boolean isSelectionSuperscript() {
        return StyleConstants.isSuperscript(getInputAttributes());
    }

    /**
     * Indique si le texte au niveau du curseur est en indice.
     *
     * @return {@code true} si le style courant est indice
     */
    public boolean isSelectionSubscript() {
        return StyleConstants.isSubscript(getInputAttributes());
    }

    // =========================================================================
    // Accesseurs visuels (cornerRadius, textAreaStyle)
    // =========================================================================
    /**
     * Retourne le rayon des coins arrondis.
     *
     * @return le rayon en pixels
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Définit le rayon des coins arrondis.
     *
     * @param radius le nouveau rayon en pixels
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    /**
     * Retourne le style visuel actuel du composant.
     *
     * @return le {@link HTextAreaStyle} appliqué
     */
    public HTextAreaStyle getTextAreaStyle() {
        return textAreaStyle;
    }

    /**
     * Définit le style visuel du composant.
     *
     * @param style le nouveau style à appliquer
     */
    public void setTextAreaStyle(HTextAreaStyle style) {
    this.textAreaStyle = style;
    applyStyle(style);
}

    // =========================================================================
    // Méthodes factory
    // =========================================================================
    /**
     * Crée un {@code HTextArea} avec un texte initial et un style visuel.
     *
     * @param text le texte initial
     * @param style le style visuel à appliquer
     * @return une nouvelle instance configurée
     */
    public static HTextArea withStyle(String text, HTextAreaStyle style) {
        HTextArea textArea = new HTextArea(text);
        textArea.setTextAreaStyle(style);
        return textArea;
    }

    /**
     * Crée un {@code HTextArea} vide avec un style visuel.
     *
     * @param style le style visuel à appliquer
     * @return une nouvelle instance configurée
     */
    public static HTextArea withStyle(HTextAreaStyle style) {
        HTextArea textArea = new HTextArea();
        textArea.setTextAreaStyle(style);
        return textArea;
    }

    // =========================================================================
    // Méthodes utilitaires privées
    // =========================================================================
    /**
     * Applique un jeu d'attributs ({@link SimpleAttributeSet}) à la sélection
     * courante.
     *
     * <p>
     * C'est le point central par lequel passent toutes les méthodes de style.
     * Le paramètre {@code replace=false} signifie qu'on <em>fusionne</em> les
     * nouveaux attributs avec ceux déjà présents, sans écraser les autres.</p>
     *
     * @param attrs les attributs à appliquer
     */
    private void appliquerAttribut(SimpleAttributeSet attrs) {
        int debut = getSelectionStart();
        int longueur = getSelectionEnd() - debut;

        StyledDocument doc = getStyledDocument();

        if (longueur > 0) {
            // Du texte est sélectionné → on l'applique directement au document
            doc.setCharacterAttributes(debut, longueur, attrs, false);
        } else {
            // Rien n'est sélectionné → on mémorise le style pour la prochaine frappe
            // getInputAttributes() retourne les attributs "en attente" sur le curseur
            getInputAttributes().addAttributes(attrs);
        }
    }

    /**
     * Modifie la taille de police de la sélection courante d'un delta donné.
     *
     * <p>
     * Lit la taille actuelle de chaque caractère sélectionné pour calculer la
     * nouvelle valeur, en s'assurant qu'elle reste toujours ≥ 1 point.</p>
     *
     * @param delta la variation en points (positif = agrandir, négatif =
     * réduire)
     */
    private void modifierTaillePolice(int delta) {
        int debut = getSelectionStart();
        int longueur = getSelectionEnd() - debut;
        if (longueur == 0) {
            return;
        }

        StyledDocument doc = getStyledDocument();

        // On parcourt chaque caractère pour respecter sa taille individuelle
        for (int i = debut; i < debut + longueur; i++) {
            int tailleCourante = StyleConstants.getFontSize(
                    doc.getCharacterElement(i).getAttributes());
            int nouvelleTaille = Math.max(1, tailleCourante + delta);

            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontSize(attrs, nouvelleTaille);
            doc.setCharacterAttributes(i, 1, attrs, false);
        }
    }

    /**
     * Transforme le texte sélectionné selon une fonction de conversion de
     * casse.
     *
     * <p>
     * Après la transformation, les attributs de style (gras, couleur…) sont
     * préservés — seul le contenu textuel change.</p>
     *
     * @param convertisseur une fonction qui reçoit le texte brut et retourne sa
     * version transformée
     */
    private void transformerCasse(java.util.function.UnaryOperator<String> convertisseur) {
        int debut = getSelectionStart();
        int fin = getSelectionEnd();
        int longueur = fin - debut;
        if (longueur == 0) {
            return;
        }

        StyledDocument doc = getStyledDocument();

        try {
            // Récupérer le texte brut sélectionné
            String texteOriginal = doc.getText(debut, longueur);
            String texteTransforme = convertisseur.apply(texteOriginal);

            // Remplacer le texte sans toucher aux attributs de style
            doc.remove(debut, longueur);
            doc.insertString(debut, texteTransforme, null);

            // Restaurer la sélection visuelle
            setSelectionStart(debut);
            setSelectionEnd(debut + texteTransforme.length());

        } catch (BadLocationException e) {
            // Ne devrait pas arriver car on lit les positions depuis getSelectionStart/End
            throw new IllegalStateException("Erreur lors de la transformation de casse", e);
        }
    }
    
    // =========================================================================
// Gestion des couleurs de fond (normal, hover, focus)
// =========================================================================

public Color getBackgroundNormal() {
    return backgroundNormal;
}

/**
 * Définit la couleur de fond à l'état normal.
 * @param color nouvelle couleur
 */
public void setBackgroundNormal(Color color) {
    this.backgroundNormal = color;
    repaint();
}

public Color getBackgroundHover() {
    return backgroundHover;
}

public void setBackgroundHover(Color color) {
    this.backgroundHover = color;
    repaint();
}

public Color getBackgroundFocus() {
    return backgroundFocus;
}

public void setBackgroundFocus(Color color) {
    this.backgroundFocus = color;
    repaint();
}

// =========================================================================
// Gestion des couleurs de bordure (normal, hover, focus)
// =========================================================================

public Color getBorderNormal() {
    return borderNormal;
}

public void setBorderNormal(Color color) {
    this.borderNormal = color;
    repaint();
}

public Color getBorderHover() {
    return borderHover;
}

public void setBorderHover(Color color) {
    this.borderHover = color;
    repaint();
}

public Color getBorderFocus() {
    return borderFocus;
}

public void setBorderFocus(Color color) {
    this.borderFocus = color;
    repaint();
}

// =========================================================================
// Épaisseur de la bordure
// =========================================================================

public int getBorderThickness() {
    return borderThickness;
}

public void setBorderThickness(int thickness) {
    this.borderThickness = Math.max(0, thickness);
    repaint();
}

// =========================================================================
// Ombre portée du composant
// =========================================================================

public boolean isComponentShadowEnabled() {
    return componentShadowEnabled;
}

public void setComponentShadowEnabled(boolean enabled) {
    this.componentShadowEnabled = enabled;
    repaint();
}

public Color getComponentShadowColor() {
    return componentShadowColor;
}

public void setComponentShadowColor(Color color) {
    this.componentShadowColor = color;
    repaint();
}

public int getComponentShadowOffset() {
    return componentShadowOffset;
}

public void setComponentShadowOffset(int offset) {
    this.componentShadowOffset = offset;
    repaint();
}

public float getComponentShadowBlur() {
    return componentShadowBlur;
}

public void setComponentShadowBlur(float blur) {
    this.componentShadowBlur = Math.max(0, blur);
    repaint();
}

public Insets getPadding() {
    return padding;
}

public void setPadding(Insets padding) {
    this.padding = padding;
    setBorder(BorderFactory.createEmptyBorder(padding.top, padding.left, padding.bottom, padding.right));
}

// =========================================================================
// Utilitaire pour appliquer un style prédéfini (HTextAreaStyle)
// =========================================================================

/**
 * Applique un style visuel prédéfini à tous les attributs du composant.
 * <p>
 * Cette méthode utilise les valeurs de l'énumération {@link HTextAreaStyle}
 * pour initialiser : fond normal (background), couleurs de bordure,
 * ombre, etc. Elle ne modifie pas les attributs typographiques du texte.
 * </p>
 *
 * @param style le style à appliquer (ne doit pas être {@code null})
 */
public void applyStyle(HTextAreaStyle style) {
    if (style == null) return;

    // Fond : même couleur pour normal, hover et focus (mais on peut les différencier plus tard)
    setBackgroundNormal(style.getBackgroundColor());
    setBackgroundHover(style.getHoverBackground());
    setBackgroundFocus(style.getFocusBackground());

    // Bordure
    setBorderNormal(style.getBorderColor());
    setBorderHover(style.getHoverBorderColor());
    setBorderFocus(style.getFocusBorderColor());

    // Ombre
    setComponentShadowColor(style.getShadowColor());
    // On peut aussi activer l'ombre par défaut pour les styles sombres
    setComponentShadowEnabled(true);

    // On conserve le textAreaStyle si vous voulez garder la compatibilité
    this.textAreaStyle = style;
}
    
}
