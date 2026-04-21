package htextarea;

import javax.swing.text.*;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 * Moteur de rendu des effets typographiques pour {@link hcomponents.HTextArea}.
 *
 * <p>Cette classe est appelée par {@link HBasicTextAreaUI} lors du rendu du composant.
 * Elle travaille segment par segment : pour chaque portion de texte portant un
 * {@link HTextEffect} différent de {@code NONE}, elle peint l'effet demandé
 * <em>avant</em> que Swing ne peigne le texte normal par-dessus.</p>
 *
 * <h3>Pourquoi peindre en deux passes ?</h3>
 * <p>Les effets comme l'ombre ou la lueur doivent apparaître <em>derrière</em>
 * le texte principal. On peint donc d'abord l'effet (passe 1), puis Swing
 * peint le texte normal via son pipeline standard (passe 2).</p>
 *
 * <h3>Pourquoi {@link TextLayout} ?</h3>
 * <p>{@code TextLayout} est l'abstraction Java2D qui représente une ligne de texte
 * mise en forme. Elle expose {@code draw()} pour remplir les glyphes et
 * {@code getOutline()} pour n'en obtenir que le contour — ce dont on a besoin
 * pour les effets OUTLINE, EMBOSS et ENGRAVE.</p>
 *
 * @author FIDELE
 * @version 1.0
 * @see HTextEffect
 * @see HBasicTextAreaUI
 */
public class HEffectPainter {

    // -------------------------------------------------------------------------
    // Clé d'attribut custom pour stocker HTextEffect dans le StyledDocument
    // -------------------------------------------------------------------------

    /**
     * Clé utilisée pour stocker un {@link HTextEffect} comme attribut de caractère
     * dans le {@link javax.swing.text.StyledDocument}.
     *
     * <p>Exemple d'utilisation dans {@code HTextArea} :</p>
     * <pre>
     *     SimpleAttributeSet attrs = new SimpleAttributeSet();
     *     attrs.addAttribute(HEffectPainter.EFFECT_ATTRIBUTE, HTextEffect.SHADOW);
     *     doc.setCharacterAttributes(debut, longueur, attrs, false);
     * </pre>
     */
    public static final Object EFFECT_ATTRIBUTE = new AttributeKey("HTextEffect");

    /**
     * Clé utilisée pour stocker la couleur de l'effet (ex: couleur du halo pour GLOW,
     * couleur du contour pour OUTLINE).
     */
    public static final Object EFFECT_COLOR_ATTRIBUTE = new AttributeKey("HTextEffectColor");

    // -------------------------------------------------------------------------
    // Paramètres visuels des effets (ajustables)
    // -------------------------------------------------------------------------

    /** Décalage en pixels de l'ombre portée (SHADOW). */
    private static final int SHADOW_OFFSET = 2;

    /** Transparence de l'ombre portée (0 = invisible, 255 = opaque). */
    private static final int SHADOW_ALPHA = 100;

    /** Épaisseur du trait pour l'effet OUTLINE. */
    private static final float OUTLINE_STROKE = 1.4f;

    /** Décalage des ombres pour EMBOSS et ENGRAVE. */
    private static final int RELIEF_OFFSET = 1;

    /** Nombre de couches pour l'effet GLOW. */
    private static final int GLOW_LAYERS = 5;

    /** Rayon maximum du halo GLOW en pixels. */
    private static final int GLOW_RADIUS = 4;


    // -------------------------------------------------------------------------
    // Point d'entrée principal
    // -------------------------------------------------------------------------

    /**
     * Peint les effets typographiques pour toute la plage de texte visible.
     *
     * <p>Cette méthode est appelée par {@link HBasicTextAreaUI#paintSafely(Graphics)}
     * <em>avant</em> le rendu standard de Swing, pour que les effets apparaissent
     * derrière le texte.</p>
     *
     * <p>Elle parcourt le document segment par segment (un segment = une suite de
     * caractères ayant les mêmes attributs). Pour chaque segment portant un effet
     * différent de {@code NONE}, elle délègue au bon moteur de peinture.</p>
     *
     * @param g    le contexte graphique (sera casté en {@link Graphics2D})
     * @param doc  le document stylisé du composant
     * @param view la vue racine de Swing (permet de convertir position doc → pixels)
     */
    public static void peindreEffets(Graphics g, StyledDocument doc, View view) {
        if (g == null || doc == null || view == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            // Antialiasing obligatoire pour des effets nets
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_QUALITY);

            // Parcourir tous les éléments (segments) du document
            Element racine = doc.getDefaultRootElement();
            parcourirElements(g2, doc, view, racine);

        } finally {
            g2.dispose();
        }
    }

    /**
     * Parcourt récursivement les éléments du document pour trouver les segments
     * feuilles (ceux qui portent du texte et des attributs).
     */
    private static void parcourirElements(Graphics2D g2, StyledDocument doc,
                                          View view, Element element) {
        if (element.isLeaf()) {
            // C'est un segment de texte — on vérifie s'il a un effet
            traiterSegment(g2, doc, view, element);
        } else {
            // C'est un conteneur (paragraphe, racine…) — on descend dedans
            for (int i = 0; i < element.getElementCount(); i++) {
                parcourirElements(g2, doc, view, element.getElement(i));
            }
        }
    }

    /**
     * Traite un segment feuille : si un effet est défini, le peint.
     */
    private static void traiterSegment(Graphics2D g2, StyledDocument doc,
                                       View view, Element element) {
        AttributeSet attrs = element.getAttributes();
        Object valeurEffet = attrs.getAttribute(EFFECT_ATTRIBUTE);

        // Pas d'effet défini ou effet NONE → rien à faire, Swing s'en charge
        if (!(valeurEffet instanceof HTextEffect effet) || effet == HTextEffect.NONE) return;

        int debut = element.getStartOffset();
        int fin   = element.getEndOffset() - 1; // -1 pour exclure le '\n' de fin
        if (debut >= fin) return;

        try {
            String texte = doc.getText(debut, fin - debut);
            if (texte.isBlank()) return;

            // Convertir la position dans le document en coordonnées écran
            Rectangle rectDebut = calculerRectangle(view, debut);
            if (rectDebut == null) return;

            // Construire la Font depuis les attributs du segment
            Font police = resoudreFont(attrs);

            // Déléguer au bon moteur selon l'effet demandé
            Color couleurEffet = resoudreCouleurEffet(attrs, g2.getColor());
            peindreEffet(g2, texte, police, rectDebut.x, rectDebut.y + rectDebut.height - 3,
                         effet, couleurEffet);

        } catch (BadLocationException e) {
            // Segment hors limites — on l'ignore silencieusement
        }
    }


    // -------------------------------------------------------------------------
    // Moteurs de peinture par effet
    // -------------------------------------------------------------------------

    /**
     * Dispatche vers le bon moteur selon l'effet demandé.
     */
    private static void peindreEffet(Graphics2D g2, String texte, Font police,
                                     int x, int y, HTextEffect effet, Color couleurEffet) {
        // On crée un TextLayout pour avoir accès au contour des glyphes
        FontMetrics fm = g2.getFontMetrics(police);
        TextLayout layout = new TextLayout(texte,
                police, g2.getFontRenderContext());

        switch (effet) {
            case SHADOW  -> peindreOmbre(g2, layout, x, y, couleurEffet);
            case OUTLINE -> peindreContour(g2, layout, x, y, couleurEffet);
            case EMBOSS  -> peindreRelief(g2, layout, x, y, false);
            case ENGRAVE -> peindreRelief(g2, layout, x, y, true);
            case GLOW    -> peindreGlow(g2, layout, x, y, couleurEffet);
            case NONE    -> { /* rien */ }
        }
    }

    /**
     * Peint une ombre portée derrière le texte.
     *
     * <p>Technique : on dessine le texte une première fois en gris semi-transparent,
     * décalé de {@value #SHADOW_OFFSET} pixels vers le bas-droite. Swing peindra
     * ensuite le texte normal par-dessus lors de sa passe standard.</p>
     *
     * @param g2           le contexte graphique
     * @param layout       le TextLayout du segment
     * @param x            position horizontale de base
     * @param y            position verticale de base (baseline)
     * @param couleurOmbre la couleur de l'ombre (généralement un gris)
     */
    private static void peindreOmbre(Graphics2D g2, TextLayout layout,
                                     int x, int y, Color couleurOmbre) {
        // Créer une version semi-transparente de la couleur d'ombre
        Color ombreAvecAlpha = new Color(
            couleurOmbre.getRed(),
            couleurOmbre.getGreen(),
            couleurOmbre.getBlue(),
            SHADOW_ALPHA
        );

        g2.setColor(ombreAvecAlpha);
        // Peindre le texte décalé → c'est la copie "ombre"
        layout.draw(g2, x + SHADOW_OFFSET, y + SHADOW_OFFSET);
    }

    /**
     * Peint uniquement le contour des glyphes (effet outline).
     *
     * <p>Technique : {@link TextLayout#getOutline(AffineTransform)} retourne la
     * {@link Shape} exacte du contour de chaque lettre. On la trace avec
     * {@link Graphics2D#draw(Shape)} sans la remplir.</p>
     *
     * @param g2             le contexte graphique
     * @param layout         le TextLayout du segment
     * @param x              position horizontale
     * @param y              position verticale (baseline)
     * @param couleurContour la couleur du trait de contour
     */
    private static void peindreContour(Graphics2D g2, TextLayout layout,
                                       int x, int y, Color couleurContour) {
        // getOutline() retourne la forme vectorielle exacte des glyphes
        AffineTransform translation = AffineTransform.getTranslateInstance(x, y);
        Shape contour = layout.getOutline(translation);

        g2.setColor(couleurContour);
        g2.setStroke(new BasicStroke(OUTLINE_STROKE));
        g2.draw(contour); // draw = contour seulement, pas de remplissage
        // Note : Swing peindra ensuite le texte normal (rempli) par-dessus
    }

    /**
     * Peint un effet de relief (emboss) ou de gravure (engrave).
     *
     * <p>Technique : deux ombres décalées de {@value #RELIEF_OFFSET} pixel.</p>
     * <ul>
     *   <li>EMBOSS : ombre claire en haut-gauche + ombre sombre en bas-droite</li>
     *   <li>ENGRAVE : ombre sombre en haut-gauche + ombre claire en bas-droite</li>
     * </ul>
     *
     * @param g2      le contexte graphique
     * @param layout  le TextLayout du segment
     * @param x       position horizontale
     * @param y       position verticale (baseline)
     * @param graver  {@code true} pour ENGRAVE, {@code false} pour EMBOSS
     */
    private static void peindreRelief(Graphics2D g2, TextLayout layout,
                                      int x, int y, boolean graver) {
        Color ombreClaire = new Color(255, 255, 255, 180); // blanc semi-transparent
        Color ombreSombre = new Color(0,   0,   0,   120); // noir semi-transparent

        Color couleurHautGauche = graver ? ombreSombre : ombreClaire;
        Color couleurBasDroite  = graver ? ombreClaire : ombreSombre;

        // Ombre haut-gauche
        g2.setColor(couleurHautGauche);
        layout.draw(g2, x - RELIEF_OFFSET, y - RELIEF_OFFSET);

        // Ombre bas-droite
        g2.setColor(couleurBasDroite);
        layout.draw(g2, x + RELIEF_OFFSET, y + RELIEF_OFFSET);

        // Swing peindra ensuite le texte normal au centre pour compléter l'effet
    }

    /**
     * Peint un halo lumineux autour du texte (effet glow).
     *
     * <p>Technique : {@value #GLOW_LAYERS} couches d'ombres concentriques,
     * chacune de plus en plus transparente en s'éloignant du texte. Cela
     * simule la diffusion lumineuse d'un halo.</p>
     *
     * @param g2           le contexte graphique
     * @param layout       le TextLayout du segment
     * @param x            position horizontale
     * @param y            position verticale (baseline)
     * @param couleurHalo  la couleur du halo (souvent une couleur vive)
     */
    private static void peindreGlow(Graphics2D g2, TextLayout layout,
                                    int x, int y, Color couleurHalo) {
        // On peint du plus loin au plus proche du texte
        // pour que les couches internes soient plus opaques
        for (int couche = GLOW_LAYERS; couche >= 1; couche--) {
            // Décalage de cette couche (en pixels à partir du centre)
            float facteur = (float) couche / GLOW_LAYERS;
            int   rayon   = Math.round(facteur * GLOW_RADIUS);

            // Transparence : plus on est loin, plus c'est transparent
            int alpha = Math.round((1f - facteur) * 180 + 20);
            Color couleurCouche = new Color(
                couleurHalo.getRed(),
                couleurHalo.getGreen(),
                couleurHalo.getBlue(),
                Math.min(255, alpha)
            );

            g2.setColor(couleurCouche);

            // Peindre dans les 8 directions cardinales pour un halo uniforme
            for (int dx = -rayon; dx <= rayon; dx += Math.max(1, rayon)) {
                for (int dy = -rayon; dy <= rayon; dy += Math.max(1, rayon)) {
                    if (dx == 0 && dy == 0) continue; // le centre sera peint par Swing
                    layout.draw(g2, x + dx, y + dy);
                }
            }
        }
    }


    // -------------------------------------------------------------------------
    // Méthodes utilitaires privées
    // -------------------------------------------------------------------------

    /**
     * Calcule le rectangle en pixels correspondant à une position dans le document.
     *
     * <p>{@code view.modelToView2D()} traduit un index de caractère (position
     * dans le document texte) en coordonnées écran. C'est le pont entre le
     * modèle (le texte) et la vue (les pixels).</p>
     *
     * @param view  la vue racine du composant
     * @param pos   la position dans le document
     * @return le rectangle en pixels, ou {@code null} en cas d'erreur
     */
    private static Rectangle calculerRectangle(View view, int pos) {
        try {
            // modelToView(int, Shape, Bias) est la signature correcte sur View
            java.awt.Shape forme = view.modelToView(pos, new Rectangle(),
                    Position.Bias.Forward);
            return forme != null ? forme.getBounds() : null;
        } catch (BadLocationException e) {
            return null;
        }
    }

    /**
     * Reconstruit la {@link Font} à partir des attributs d'un segment du document.
     *
     * @param attrs les attributs du segment
     * @return la Font correspondante
     */
    private static Font resoudreFont(AttributeSet attrs) {
        String famille = StyleConstants.getFontFamily(attrs);
        int    taille  = StyleConstants.getFontSize(attrs);
        boolean gras   = StyleConstants.isBold(attrs);
        boolean italic = StyleConstants.isItalic(attrs);

        int style = Font.PLAIN;
        if (gras)   style |= Font.BOLD;
        if (italic) style |= Font.ITALIC;

        // Valeurs de repli si les attributs sont absents
        if (famille == null || famille.isBlank()) famille = "SansSerif";
        if (taille  <= 0)                         taille  = 12;

        return new Font(famille, style, taille);
    }

    /**
     * Lit la couleur d'effet depuis les attributs, ou retourne une couleur par défaut.
     *
     * @param attrs         les attributs du segment
     * @param couleurDefaut la couleur à utiliser si aucune n'est définie (ex: gris pour SHADOW)
     * @return la couleur de l'effet
     */
    private static Color resoudreCouleurEffet(AttributeSet attrs, Color couleurDefaut) {
        Object valeur = attrs.getAttribute(EFFECT_COLOR_ATTRIBUTE);
        if (valeur instanceof Color couleur) return couleur;
        // Couleur par défaut selon le contexte : gris moyen
        return new Color(100, 100, 100);
    }


    // -------------------------------------------------------------------------
    // Classe interne pour les clés d'attributs custom
    // -------------------------------------------------------------------------

    /**
     * Clé d'attribut typée pour le {@link StyledDocument}.
     *
     * <p>Swing identifie les attributs par des objets utilisés comme clés dans
     * une {@code AttributeSet}. On crée nos propres clés pour éviter les
     * collisions avec les clés standard de {@link StyleConstants}.</p>
     *
     * <p>La méthode {@code toString()} est importante : Swing l'utilise lors
     * de la sérialisation et du débogage des attributs.</p>
     */
    private static final class AttributeKey {
        private final String nom;

        AttributeKey(String nom) { this.nom = nom; }

        @Override public String toString() { return nom; }
    }
}
