package htextarea;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;

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

     /**
     * Clé custom pour marquer un caractère comme exposant.
     * Stocke un {@code Boolean} dans le {@code StyledDocument}.
     * N'utilise PAS {@code StyleConstants.Superscript} pour éviter
     * le bug de hauteur de ligne dans {@code GlyphView}.
     */
    private static final Object SUPERSCRIPT_CUSTOM = new Object() {
        @Override public String toString() { return "HTextArea_Superscript"; }
    };

    /**
     * Clé custom pour marquer un caractère comme indice.
     * Même principe que {@link #SUPERSCRIPT_CUSTOM}.
     */
    private static final Object SUBSCRIPT_CUSTOM = new Object() {
        @Override public String toString() { return "HTextArea_Subscript"; }
    };
    
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
        int debut = getSelectionStart();
        int longueur = getSelectionEnd() - debut;
        if (longueur == 0) {
            return;
        }

        StyledDocument doc = getStyledDocument();

        for (int i = debut; i < debut + longueur; i++) {
            AttributeSet attrsExistants = doc.getCharacterElement(i).getAttributes();
            int tailleCourante = StyleConstants.getFontSize(attrsExistants);

            SimpleAttributeSet attrs = new SimpleAttributeSet();

            if (superscript) {
                // Désactiver l'indice si actif, pour éviter un état incohérent
                attrs.addAttribute(SUBSCRIPT_CUSTOM, false);
                attrs.addAttribute(SUPERSCRIPT_CUSTOM, true);

                // Réduire à 58 % de la taille courante, minimum 7pt pour rester lisible
                // On ne touche pas à la taille si elle est déjà réduite (évite la cascade)
                boolean dejaActif = Boolean.TRUE.equals(
                        attrsExistants.getAttribute(SUPERSCRIPT_CUSTOM));
                if (!dejaActif) {
                    int nouvelleTaille = Math.max(7, Math.round(tailleCourante * 0.58f));
                    StyleConstants.setFontSize(attrs, nouvelleTaille);
                }
            } else {
                // Désactivation : on restaure la taille d'origine (approximation)
                attrs.addAttribute(SUPERSCRIPT_CUSTOM, false);
                boolean etaitActif = Boolean.TRUE.equals(
                        attrsExistants.getAttribute(SUPERSCRIPT_CUSTOM));
                if (etaitActif) {
                    // Restaurer en divisant par 0.58 — arrondi à l'entier le plus proche
                    int tailleRestauree = Math.round(tailleCourante / 0.58f);
                    StyleConstants.setFontSize(attrs, tailleRestauree);
                }
            }

            doc.setCharacterAttributes(i, 1, attrs, false);
        }
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
        int debut = getSelectionStart();
        int longueur = getSelectionEnd() - debut;
        if (longueur == 0) {
            return;
        }

        StyledDocument doc = getStyledDocument();

        for (int i = debut; i < debut + longueur; i++) {
            AttributeSet attrsExistants = doc.getCharacterElement(i).getAttributes();
            int tailleCourante = StyleConstants.getFontSize(attrsExistants);

            SimpleAttributeSet attrs = new SimpleAttributeSet();

            if (subscript) {
                attrs.addAttribute(SUPERSCRIPT_CUSTOM, false);
                attrs.addAttribute(SUBSCRIPT_CUSTOM, true);

                boolean dejaActif = Boolean.TRUE.equals(
                        attrsExistants.getAttribute(SUBSCRIPT_CUSTOM));
                if (!dejaActif) {
                    int nouvelleTaille = Math.max(7, Math.round(tailleCourante * 0.58f));
                    StyleConstants.setFontSize(attrs, nouvelleTaille);
                }
            } else {
                attrs.addAttribute(SUBSCRIPT_CUSTOM, false);
                boolean etaitActif = Boolean.TRUE.equals(
                        attrsExistants.getAttribute(SUBSCRIPT_CUSTOM));
                if (etaitActif) {
                    int tailleRestauree = Math.round(tailleCourante / 0.58f);
                    StyleConstants.setFontSize(attrs, tailleRestauree);
                }
            }

            doc.setCharacterAttributes(i, 1, attrs, false);
        }
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
     * <p>
     * La nouvelle architecture stocke une {@code List<HEffetEntree>} sous la
     * clé {@link HEffectPainter#EFFECTS_LIST_ATTRIBUTE}. Chaque appel à
     * {@code setTextEffect} <em>ajoute</em> une entrée à cette liste s'il
     * n'existe pas déjà un effet du même type — dans ce cas il le remplace
     * (comportement intuitif : réappliquer SHADOW avec de nouveaux paramètres
     * met à jour l'ombre existante sans dupliquer).</p>
     *
     * @param effet l'effet à appliquer (ne doit pas être {@code null})
     * @param couleurEffet la couleur de l'effet ({@code null} = couleur du
     * texte)
     * @param config les paramètres ({@code null} = valeurs par défaut)
     */
    public void setTextEffect(HTextEffect effet, Color couleurEffet, HTextEffectConfig config) {
        if (effet == null) {
            return;
        }

        int debut = getSelectionStart();
        int longueur = getSelectionEnd() - debut;

        // Si NONE → on efface tous les effets de la sélection
        if (effet == HTextEffect.NONE) {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(HEffectPainter.EFFECTS_LIST_ATTRIBUTE, new ArrayList<HEffetEntree>());
            appliquerAttribut(attrs);
            repaint();
            return;
        }

        HTextEffectConfig configEffective = (config != null) ? config : new HTextEffectConfig();
        HEffetEntree nouvelleEntree = new HEffetEntree(effet, couleurEffet, configEffective);

        if (longueur > 0) {
            // Appliquer segment par segment pour préserver les listes d'effets
            // existantes sur chaque sous-segment de la sélection
            StyledDocument doc = getStyledDocument();
            for (int i = debut; i < debut + longueur;) {
                Element elem = doc.getCharacterElement(i);
                int finElem = Math.min(elem.getEndOffset(), debut + longueur);

                // Lire la liste existante sur ce segment et y ajouter/remplacer l'effet
                List<HEffetEntree> listeExistante = lireListeEffets(elem.getAttributes());
                List<HEffetEntree> nouvelleList = construireListeAvecEffet(listeExistante, nouvelleEntree);

                SimpleAttributeSet attrs = new SimpleAttributeSet();
                attrs.addAttribute(HEffectPainter.EFFECTS_LIST_ATTRIBUTE, nouvelleList);
                doc.setCharacterAttributes(i, finElem - i, attrs, false);

                i = finElem;
            }
        } else {
            // Pas de sélection → mémoriser pour la prochaine frappe
            // On lit la liste sur le curseur courant
            AttributeSet attrsActuels = getInputAttributes();
            List<HEffetEntree> listeExistante = lireListeEffets(attrsActuels);
            List<HEffetEntree> nouvelleList = construireListeAvecEffet(listeExistante, nouvelleEntree);

            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(HEffectPainter.EFFECTS_LIST_ATTRIBUTE, nouvelleList);
            getInputAttributes().addAttributes(attrs);
        }

        repaint();
    }

    /**
     * Applique un effet avec une couleur et la configuration par défaut.
     *
     * @param effet        l'effet à appliquer
     * @param couleurEffet la couleur de l'effet
     */
    public void setTextEffect(HTextEffect effet, Color couleurEffet) {
        setTextEffect(effet, couleurEffet, new HTextEffectConfig());
    }

     /**
     * Applique un effet avec couleur et config par défaut.
     *
     * @param effet l'effet à appliquer
     */
    public void setTextEffect(HTextEffect effet) {
        setTextEffect(effet, null, new HTextEffectConfig());
    }

    // -------------------------------------------------------------------------
    // Méthodes spécialisées SHADOW — les plus utilisées
    // -------------------------------------------------------------------------
    /**
     * Applique une ombre portée avec contrôle complet sur tous les paramètres.
     *
     * @param couleur couleur de l'ombre
     * @param direction direction de projection ({@link HEffectDirection})
     * @param distance décalage en pixels
     * @param flou niveau de flou (0 = net, 5 = très flou)
     * @param transparence alpha de l'ombre (0 = invisible, 255 = opaque)
     */
    public void setShadow(Color couleur, HEffectDirection direction,
                          int distance, int flou, int transparence) {
        HTextEffectConfig config = HTextEffectConfig.ombre(direction, distance, flou, transparence);
        setTextEffect(HTextEffect.SHADOW, couleur, config);
    }

    /**
     * Applique une ombre portée vers le bas-droite (direction classique).
     *
     * @param couleur couleur de l'ombre
     * @param distance décalage en pixels
     * @param flou niveau de flou (0–5)
     * @param transparence alpha (0–255)
     */
    public void setShadowBasDroite(Color couleur, int distance, int flou, int transparence) {
        setShadow(couleur, HEffectDirection.BAS_DROITE, distance, flou, transparence);
    }

    /**
     * Applique une ombre portée vers le bas-gauche.
     *
     * @param couleur couleur de l'ombre
     * @param distance décalage en pixels
     * @param flou niveau de flou (0–5)
     * @param transparence alpha (0–255)
     */
    public void setShadowBasGauche(Color couleur, int distance, int flou, int transparence) {
        setShadow(couleur, HEffectDirection.BAS_GAUCHE, distance, flou, transparence);
    }

    /**
     * Applique une ombre portée vers le haut-droite.
     *
     * @param couleur couleur de l'ombre
     * @param distance décalage en pixels
     * @param flou niveau de flou (0–5)
     * @param transparence alpha (0–255)
     */
    public void setShadowHautDroite(Color couleur, int distance, int flou, int transparence) {
        setShadow(couleur, HEffectDirection.HAUT_DROITE, distance, flou, transparence);
    }

    /**
     * Applique une ombre portée vers le haut-gauche.
     *
     * @param couleur couleur de l'ombre
     * @param distance décalage en pixels
     * @param flou niveau de flou (0–5)
     * @param transparence alpha (0–255)
     */
    public void setShadowHautGauche(Color couleur, int distance, int flou, int transparence) {
        setShadow(couleur, HEffectDirection.HAUT_GAUCHE, distance, flou, transparence);
    }

    /**
     * Applique une ombre centrée (projetée dans les 4 directions
     * simultanément).
     *
     * <p>
     * Produit un halo d'ombre uniforme autour de chaque lettre, similaire au
     * flou "centré" de Word.</p>
     *
     * @param couleur couleur de l'ombre
     * @param distance rayon de l'ombre en pixels
     * @param flou niveau de flou (0–5)
     * @param transparence alpha (0–255)
     */
    public void setShadowCentree(Color couleur, int distance, int flou, int transparence) {
        setShadow(couleur, HEffectDirection.CENTREE, distance, flou, transparence);
    }

    // -------------------------------------------------------------------------
    // Méthodes spécialisées OUTLINE — contour
    // -------------------------------------------------------------------------
    /**
     * Applique un effet contour (outline) avec contrôle complet.
     *
     * @param couleur couleur du contour
     * @param epaisseur épaisseur du trait en pixels (min 0.5)
     * @param transparence alpha du contour (0–255)
     */
    public void setOutline(Color couleur, int epaisseur, int transparence) {
        setTextEffect(HTextEffect.OUTLINE, couleur, HTextEffectConfig.contour(epaisseur, transparence));
    }

    /**
     * Applique un effet contour avec épaisseur et transparence par défaut.
     *
     * @param couleur couleur du contour
     */
    public void setOutline(Color couleur) {
        setTextEffect(HTextEffect.OUTLINE, couleur, new HTextEffectConfig());
    }

    // -------------------------------------------------------------------------
    // Méthodes spécialisées LIGHT — halo lumineux
    // -------------------------------------------------------------------------
    /**
     * Applique un halo lumineux avec contrôle complet.
     *
     * @param couleur couleur du halo
     * @param rayon rayon du halo en pixels
     * @param transparence alpha maximal du halo (0–255)
     */
    public void setLight(Color couleur, int rayon, int transparence) {
        setTextEffect(HTextEffect.LIGHT, couleur, HTextEffectConfig.lumiere(rayon, transparence));
    }

    /**
     * Applique un halo lumineux avec rayon et transparence par défaut.
     *
     * @param couleur couleur du halo
     */
    public void setLight(Color couleur) {
        setTextEffect(HTextEffect.LIGHT, couleur, new HTextEffectConfig());
    }

    // -------------------------------------------------------------------------
    // Méthodes spécialisées REFLECTION — reflet
    // -------------------------------------------------------------------------
    /**
     * Applique un effet de reflet sous le texte avec contrôle complet.
     *
     * @param espacement espace en pixels entre le bas du texte et le reflet
     * @param transparence opacité maximale du reflet (0–255)
     */
    public void setReflection(int espacement, int transparence) {
        setTextEffect(HTextEffect.REFLECTION, null,
                HTextEffectConfig.reflection(espacement, transparence));
    }

    /**
     * Applique un effet de reflet avec les valeurs par défaut (espacement 2px,
     * transparence 110).
     */
    public void setReflection() {
        setTextEffect(HTextEffect.REFLECTION, null, new HTextEffectConfig());
    }

    // -------------------------------------------------------------------------
    // Suppression d'effet
    // -------------------------------------------------------------------------
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
     * Supprime un effet spécifique de la sélection, en conservant les autres.
     *
     * <p>Exemple : supprimer uniquement l'ombre sans toucher au contour
     * ni au halo.</p>
     *
     * @param effetASupprimer le type d'effet à retirer
     */
    public void removeTextEffect(HTextEffect effetASupprimer) {
        if (effetASupprimer == null || effetASupprimer == HTextEffect.NONE) return;

        int debut    = getSelectionStart();
        int longueur = getSelectionEnd() - debut;
        if (longueur == 0) return;

        StyledDocument doc = getStyledDocument();

        for (int i = debut; i < debut + longueur; ) {
            Element elem = doc.getCharacterElement(i);
            int finElem  = Math.min(elem.getEndOffset(), debut + longueur);

            List<HEffetEntree> listeExistante = lireListeEffets(elem.getAttributes());
            List<HEffetEntree> nouvelleList   = construireSansEffet(listeExistante, effetASupprimer);

            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(HEffectPainter.EFFECTS_LIST_ATTRIBUTE, nouvelleList);
            doc.setCharacterAttributes(i, finElem - i, attrs, false);

            i = finElem;
        }
        repaint();
    }
    
     /**
     * Retourne l'effet typographique principal actif au niveau du curseur.
     *
     * <p>Si plusieurs effets sont actifs, retourne le premier de la liste.
     * Pour lire la liste complète, utiliser {@link #getSelectionTextEffects()}.</p>
     *
     * @return le premier effet actif, ou {@link HTextEffect#NONE} si aucun
     */
    public HTextEffect getSelectionTextEffect() {
        List<HEffetEntree> liste = lireListeEffets(getInputAttributes());
        if (liste != null && !liste.isEmpty()) {
            return liste.get(0).getEffet();
        }
        return HTextEffect.NONE;
    }
    
     /**
     * Retourne la liste complète des effets actifs au niveau du curseur.
     *
     * @return la liste des effets (jamais {@code null}, peut être vide)
     */
    public List<HEffetEntree> getSelectionTextEffects() {
        List<HEffetEntree> liste = lireListeEffets(getInputAttributes());
        return (liste != null) ? new ArrayList<>(liste) : new ArrayList<>();
    }       

    // -------------------------------------------------------------------------
    // Interrogation de l'effet courant
    // -------------------------------------------------------------------------
    /**
     * Retourne la configuration du premier effet actif au niveau du curseur.
     *
     * @return la config courante, ou une config par défaut si aucun effet
     */
    public HTextEffectConfig getSelectionTextEffectConfig() {
        List<HEffetEntree> liste = lireListeEffets(getInputAttributes());
        if (liste != null && !liste.isEmpty()) {
            return liste.get(0).getConfig();
        }
        return new HTextEffectConfig();
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
     * <p>
     * Lit l'attribut custom {@link #SUPERSCRIPT_CUSTOM} plutôt que
     * {@code StyleConstants.Superscript} pour rester cohérent avec notre
     * implémentation custom.</p>
     *
     * @return {@code true} si le style courant est exposant
     */
    public boolean isSelectionSuperscript() {
        Object val = getInputAttributes().getAttribute(SUPERSCRIPT_CUSTOM);
        return Boolean.TRUE.equals(val);
    }

    /**
     * Indique si le texte au niveau du curseur est en indice.
     *
     * @return {@code true} si le style courant est indice
     */
    public boolean isSelectionSubscript() {
        Object val = getInputAttributes().getAttribute(SUBSCRIPT_CUSTOM);
        return Boolean.TRUE.equals(val);
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
     * Applique un jeu d'attributs à la sélection courante.
     *
     * <p>Point central par lequel passent toutes les méthodes de style de base
     * (gras, italique, taille…). Les effets typographiques passent quant à eux
     * directement par {@link #setTextEffect} qui gère la logique de liste.</p>
     *
     * <p>{@code replace=false} signifie qu'on <em>fusionne</em> les nouveaux
     * attributs avec ceux déjà présents, sans écraser les autres.</p>
     *
     * @param attrs les attributs à appliquer
     */
    private void appliquerAttribut(SimpleAttributeSet attrs) {
        int debut    = getSelectionStart();
        int longueur = getSelectionEnd() - debut;

        StyledDocument doc = getStyledDocument();

        if (longueur > 0) {
            doc.setCharacterAttributes(debut, longueur, attrs, false);
        } else {
            // Rien de sélectionné → mémoriser pour la prochaine frappe
            getInputAttributes().addAttributes(attrs);
        }
    }
    
     /**
     * Lit la liste d'effets depuis un {@link AttributeSet}.
     *
     * @param attrs les attributs à lire
     * @return la liste existante, ou {@code null} si absente
     */
    @SuppressWarnings("unchecked")
    private List<HEffetEntree> lireListeEffets(AttributeSet attrs) {
        Object valeur = attrs.getAttribute(HEffectPainter.EFFECTS_LIST_ATTRIBUTE);
        if (valeur instanceof List<?> liste && !liste.isEmpty()
                && liste.get(0) instanceof HEffetEntree) {
            return (List<HEffetEntree>) liste;
        }
        return null;
    }

    /**
     * Construit une nouvelle liste en ajoutant ou remplaçant un effet.
     *
     * <p>Règle : si la liste contient déjà un effet du même type que
     * {@code nouvelleEntree}, il est remplacé à la même position (mise à jour
     * des paramètres). Sinon, l'entrée est ajoutée en fin de liste.</p>
     *
     * <p>Ce comportement est intuitif : réappliquer SHADOW met à jour l'ombre
     * existante sans la dupliquer, mais ajouter OUTLINE sur une sélection qui
     * a déjà SHADOW conserve les deux effets.</p>
     *
     * @param listeExistante la liste actuelle (peut être {@code null})
     * @param nouvelleEntree l'entrée à ajouter ou remplacer
     * @return une nouvelle liste (l'originale n'est jamais modifiée)
     */
    private List<HEffetEntree> construireListeAvecEffet(List<HEffetEntree> listeExistante,
                                                         HEffetEntree nouvelleEntree) {
        List<HEffetEntree> result = new ArrayList<>();

        if (listeExistante != null) {
            boolean remplace = false;
            for (HEffetEntree entree : listeExistante) {
                if (entree.getEffet() == nouvelleEntree.getEffet()) {
                    // Même type → on remplace par les nouveaux paramètres
                    result.add(nouvelleEntree);
                    remplace = true;
                } else {
                    result.add(entree);
                }
            }
            if (!remplace) {
                // Type nouveau → on ajoute en fin de liste
                result.add(nouvelleEntree);
            }
        } else {
            // Pas de liste existante → on crée avec ce seul effet
            result.add(nouvelleEntree);
        }

        return result;
    }

    /**
     * Construit une nouvelle liste en retirant tous les effets d'un type donné.
     *
     * @param listeExistante la liste actuelle (peut être {@code null})
     * @param effetARetirer  le type d'effet à supprimer de la liste
     * @return une nouvelle liste sans les effets du type spécifié
     */
    private List<HEffetEntree> construireSansEffet(List<HEffetEntree> listeExistante,
                                                    HTextEffect effetARetirer) {
        if (listeExistante == null) return new ArrayList<>();

        List<HEffetEntree> result = new ArrayList<>();
        for (HEffetEntree entree : listeExistante) {
            if (entree.getEffet() != effetARetirer) {
                result.add(entree);
            }
        }
        return result;
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
     *
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
     * pour initialiser : fond normal (background), couleurs de bordure, ombre,
     * etc. Elle ne modifie pas les attributs typographiques du texte.
     * </p>
     *
     * @param style le style à appliquer (ne doit pas être {@code null})
     */
    public void applyStyle(HTextAreaStyle style) {
        if (style == null) {
            return;
        }

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
