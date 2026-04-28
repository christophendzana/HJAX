package htextarea;

import javax.swing.text.*;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Moteur de rendu des effets typographiques pour {@link HTextArea}.
 *
 * <p>Appelé par {@link HBasicTextAreaUI#paintSafely(Graphics)} avant le rendu
 * standard de Swing. Parcourt le document segment par segment et peint tous
 * les effets enregistrés dans la liste {@link #EFFECTS_LIST_ATTRIBUTE}.</p>
 *
 * <h3>Architecture multi-effets</h3>
 * <p>Chaque segment peut porter une {@code List<HEffetEntree>} — une liste
 * ordonnée d'effets. {@link HEffectPainter} les peint dans l'ordre, de sorte
 * qu'un segment peut simultanément avoir une ombre ET un contour par exemple.</p>
 *
 * <h3>Clés stockées dans le StyledDocument</h3>
 * <ul>
 *   <li>{@link #EFFECTS_LIST_ATTRIBUTE} — {@code List<HEffetEntree>} (nouvelle clé principale)</li>
 *   <li>{@link #EFFECT_ATTRIBUTE}        — rétrocompatibilité lecture seule</li>
 *   <li>{@link #EFFECT_COLOR_ATTRIBUTE}  — rétrocompatibilité lecture seule</li>
 *   <li>{@link #EFFECT_CONFIG_ATTRIBUTE} — rétrocompatibilité lecture seule</li>
 * </ul>
 *
 * @author FIDELE
 * @version 3.0
 */
public class HEffectPainter {

    // =========================================================================
    // Clés d'attributs dans le StyledDocument
    // =========================================================================

    /**
     * Clé principale (v3) : stocke une {@code List<HEffetEntree>}.
     * C'est cette liste qu'on lit pour peindre les effets.
     */
    public static final Object EFFECTS_LIST_ATTRIBUTE = new AttributeKey("HEffectsList");

    /**
     * Clé legacy (v1/v2) — conservée pour la rétrocompatibilité.
     * Plus utilisée en écriture depuis la v3.
     */
    public static final Object EFFECT_ATTRIBUTE = new AttributeKey("HTextEffect");

    /** Clé legacy : couleur d'effet. */
    public static final Object EFFECT_COLOR_ATTRIBUTE = new AttributeKey("HTextEffectColor");

    /** Clé legacy : configuration d'effet. */
    public static final Object EFFECT_CONFIG_ATTRIBUTE = new AttributeKey("HTextEffectConfig");

    // =========================================================================
    // Constantes visuelles
    // =========================================================================

    /** Décalage en pixels de l'ombre portée (SHADOW). */
    private static final int SHADOW_OFFSET = 2;

    /** Épaisseur par défaut du trait pour l'effet OUTLINE. */
    private static final float OUTLINE_STROKE = 1.4f;

    /** Nombre de couches pour l'effet LIGHT (halo). */
    private static final int GLOW_LAYERS = 5;

    /** Rayon maximum du halo LIGHT en pixels. */
    private static final int GLOW_RADIUS = 4;

    // =========================================================================
    // Point d'entrée principal
    // =========================================================================

    /**
     * Peint tous les effets typographiques enregistrés sur le document.
     *
     * <p>Appelée avant {@code super.paintSafely()} dans {@link HBasicTextAreaUI}
     * pour que les effets apparaissent visuellement derrière le texte principal.</p>
     *
     * @param g    contexte graphique (sera casté en {@link Graphics2D})
     * @param doc  le document stylisé du composant
     * @param view la vue racine de Swing (pour convertir position → pixels)
     */
    public static void peindreEffets(Graphics g, StyledDocument doc, View view) {
        if (g == null || doc == null || view == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            parcourirElements(g2, doc, view, doc.getDefaultRootElement());

        } finally {
            g2.dispose();
        }
    }

    // =========================================================================
    // Parcours du document
    // =========================================================================

    /**
     * Parcourt récursivement les éléments du document.
     * Seuls les éléments feuilles portent du texte et des attributs.
     */
    private static void parcourirElements(Graphics2D g2, StyledDocument doc,
            View view, Element element) {
        if (element.isLeaf()) {
            traiterSegment(g2, doc, view, element);
        } else {
            for (int i = 0; i < element.getElementCount(); i++) {
                parcourirElements(g2, doc, view, element.getElement(i));
            }
        }
    }

    /**
     * Traite un segment feuille : récupère sa liste d'effets et les peint tous.
     *
     * <h3>Correction bug 2 & 3 — le -1 conditionnel</h3>
     * <p>Chaque paragraphe dans un {@code StyledDocument} se termine par un
     * caractère {@code '\n'} (code 10) implicite ajouté par Swing. Ce caractère
     * appartient toujours au <em>dernier</em> segment du paragraphe.
     * L'ancienne version faisait systématiquement {@code endOffset - 1}, ce qui
     * excluait le dernier caractère visible de tous les segments, même ceux
     * qui ne contiennent pas de saut de ligne. Désormais on lit le vrai dernier
     * caractère : si c'est {@code '\n'}, on soustrait 1 ; sinon on prend
     * {@code endOffset} tel quel.</p>
     */
    private static void traiterSegment(Graphics2D g2, StyledDocument doc,
            View view, Element element) {

        AttributeSet attrs = element.getAttributes();

        // Lire la liste d'effets (architecture v3)
        List<HEffetEntree> listeEffets = lireListeEffets(attrs);
        if (listeEffets == null || listeEffets.isEmpty()) {
            return;
        }

        int debut = element.getStartOffset();
        int finBrut = element.getEndOffset();

        // ----------------------------------------------------------------
        // Correction bug 2 : le -1 n'est appliqué QUE si le dernier
        // caractère du segment est un '\n' (saut de paragraphe implicite).
        // ----------------------------------------------------------------
        int fin;
        try {
            String dernierChar = doc.getText(finBrut - 1, 1);
            // '\n' = code 10 → c'est le saut implicite, on l'exclut
            fin = (dernierChar.charAt(0) == '\n') ? finBrut - 1 : finBrut;
        } catch (BadLocationException e) {
            // En cas de position invalide, on exclut prudemment le dernier char
            fin = finBrut - 1;
        }

        if (debut >= fin) {
            return;
        }

        try {
            String texte = doc.getText(debut, fin - debut);
            if (texte.isBlank()) {
                return;
            }

            // Position du segment dans le composant (coordonnées pixels)
            Rectangle rectDebut = calculerRectangle(view, debut);
            if (rectDebut == null) {
                return;
            }

            Font police = resoudreFont(attrs);
            TextLayout layout = new TextLayout(texte, police, g2.getFontRenderContext());

            int x        = rectDebut.x;
            int baseline = rectDebut.y + (int) Math.ceil(layout.getAscent());

            // Peindre TOUS les effets de la liste dans l'ordre d'insertion
            for (HEffetEntree entree : listeEffets) {
                if (entree.getEffet() == HTextEffect.NONE) {
                    continue;
                }
                Color couleur = resoudreCouleurEffet(attrs, entree.getCouleur());
                peindreEffet(g2, layout, police, x, baseline,
                        entree.getEffet(), couleur, entree.getConfig());
            }

        } catch (BadLocationException e) {
            // Segment hors limites — on ignore silencieusement
        }
    }

    // =========================================================================
    // Dispatch
    // =========================================================================

    /**
     * Dispatche vers le moteur de peinture correspondant à l'effet.
     */
    private static void peindreEffet(Graphics2D g2, TextLayout layout, Font police,
            int x, int y, HTextEffect effet, Color couleurEffet, HTextEffectConfig config) {

        if (layout == null || effet == null || effet == HTextEffect.NONE) {
            return;
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        switch (effet) {
            case SHADOW     -> peindreOmbre(g2, layout, x, y, couleurEffet, config);
            case OUTLINE    -> peindreContour(g2, layout, x, y, couleurEffet, config);
            case LIGHT      -> peindreLumiere(g2, layout, x, y, couleurEffet, config);
            case REFLECTION -> peindreReflection(g2, layout, x, y, config);
            case NONE       -> { /* jamais atteint */ }
        }
    }

    // =========================================================================
    // SHADOW
    // =========================================================================

    /**
     * Peint une ombre portée derrière le texte.
     *
     * <p>La {@link HEffectDirection} est traduite en vecteur {@code (dx, dy)}.
     * Le flou est simulé par plusieurs couches de plus en plus transparentes
     * et légèrement décalées. {@code CENTREE} peint dans les 4 diagonales.</p>
     */
    private static void peindreOmbre(Graphics2D g2, TextLayout layout,
            int x, int y, Color couleurOmbre, HTextEffectConfig config) {

        int distance     = config.getDistance();
        int flou         = config.getFlou();
        int transparence = config.getTransparence();
        HEffectDirection direction = config.getDirection();

        int nbCouches = flou + 1;

        if (direction == HEffectDirection.CENTREE) {
            int[] signesX = {+1, -1, +1, -1};
            int[] signesY = {+1, +1, -1, -1};
            for (int d = 0; d < 4; d++) {
                peindreOmbreDansDirection(g2, layout, x, y, couleurOmbre,
                        transparence, nbCouches, distance, signesX[d], signesY[d]);
            }
        } else {
            int[] vecteur = resoudreVecteurDirection(direction);
            peindreOmbreDansDirection(g2, layout, x, y, couleurOmbre,
                    transparence, nbCouches, distance, vecteur[0], vecteur[1]);
        }
    }

    /**
     * Peint toutes les couches de flou d'une ombre dans une seule direction.
     *
     * <p>Couche 0 = la plus proche du texte (la plus opaque et la moins décalée).
     * Dernière couche = la plus éloignée (la plus transparente).</p>
     */
    private static void peindreOmbreDansDirection(Graphics2D g2, TextLayout layout,
            int x, int y, Color couleur, int transparence, int nbCouches,
            int distance, int signeX, int signeY) {

        for (int couche = 0; couche < nbCouches; couche++) {

            float facteurOpacite = (nbCouches == 1)
                    ? 1f
                    : 1f - ((float) couche / (nbCouches - 1)) * 0.7f;

            int alpha = Math.clamp(Math.round(transparence * facteurOpacite), 0, 255);

            float facteurDecalage = (nbCouches == 1)
                    ? 1f
                    : 0.5f + 0.5f * ((float) couche / (nbCouches - 1));

            int dx = Math.round(distance * facteurDecalage * signeX);
            int dy = Math.round(distance * facteurDecalage * signeY);

            g2.setColor(new Color(
                    couleur.getRed(),
                    couleur.getGreen(),
                    couleur.getBlue(),
                    alpha
            ));
            layout.draw(g2, x + dx, y + dy);
        }
    }

    /**
     * Traduit une {@link HEffectDirection} en vecteur de signes {@code [signeX, signeY]}.
     */
    private static int[] resoudreVecteurDirection(HEffectDirection direction) {
        return switch (direction) {
            case BAS_DROITE  -> new int[]{ 1,  1};
            case BAS_GAUCHE  -> new int[]{-1,  1};
            case HAUT_DROITE -> new int[]{ 1, -1};
            case HAUT_GAUCHE -> new int[]{-1, -1};
            default          -> new int[]{ 1,  1};
        };
    }

    // =========================================================================
    // OUTLINE
    // =========================================================================

    /**
     * Peint le contour vectoriel des glyphes.
     *
     * <p>{@code config.getDistance()} contrôle l'épaisseur du trait (min 0.5px).
     * La forme est obtenue via {@link TextLayout#getOutline(AffineTransform)}
     * translateée à la position exacte du segment.</p>
     */
    private static void peindreContour(Graphics2D g2, TextLayout layout,
            int x, int y, Color couleurContour, HTextEffectConfig config) {

        float epaisseur  = Math.max(0.5f, config.getDistance());
        int transparence = config.getTransparence();

        Shape contour = layout.getOutline(AffineTransform.getTranslateInstance(x, y));

        g2.setStroke(new BasicStroke(epaisseur, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(
                couleurContour.getRed(),
                couleurContour.getGreen(),
                couleurContour.getBlue(),
                Math.clamp(transparence, 0, 255)
        ));
        g2.draw(contour);
    }

    // =========================================================================
    // LIGHT
    // =========================================================================

    /**
     * Peint un halo lumineux autour du texte.
     *
     * <p>Technique : {@value #GLOW_LAYERS} couches concentriques, chacune
     * décalée dans les 8 directions et de plus en plus transparente en
     * s'éloignant du centre. {@code config.getDistance()} = rayon maximal.</p>
     */
    private static void peindreLumiere(Graphics2D g2, TextLayout layout,
            int x, int y, Color couleurHalo, HTextEffectConfig config) {

        int rayonMax     = Math.max(1, config.getDistance());
        int transparence = config.getTransparence();

        // Du plus éloigné vers le centre pour que les couches intérieures dominent
        for (int couche = GLOW_LAYERS; couche >= 1; couche--) {
            float facteur = (float) couche / GLOW_LAYERS;
            int   rayon   = Math.round(facteur * rayonMax);

            int alpha = Math.round((1f - facteur) * transparence + 20);
            alpha = Math.clamp(alpha, 0, 255);

            g2.setColor(new Color(
                    couleurHalo.getRed(),
                    couleurHalo.getGreen(),
                    couleurHalo.getBlue(),
                    alpha
            ));

            for (int dx = -rayon; dx <= rayon; dx += Math.max(1, rayon)) {
                for (int dy = -rayon; dy <= rayon; dy += Math.max(1, rayon)) {
                    if (dx == 0 && dy == 0) continue;
                    layout.draw(g2, x + dx, y + dy);
                }
            }
        }
    }

    // =========================================================================
    // REFLECTION
    // =========================================================================

    /**
     * Peint un reflet en miroir sous le texte.
     *
     * <p>Algorithme :</p>
     * <ol>
     *   <li>Forme vectorielle du texte via {@link TextLayout#getOutline}.</li>
     *   <li>Axe de symétrie = baseline + descent + espacement.</li>
     *   <li>Transformation miroir : {@code translate(0, 2*axe) ∘ scale(1,-1)}.</li>
     *   <li>Remplissage avec un dégradé opaque→transparent vers le bas.</li>
     * </ol>
     *
     * <p>{@code config.getDistance()} = espace entre le bas du texte et le reflet.</p>
     */
    private static void peindreReflection(Graphics2D g2, TextLayout layout,
            int x, int y, HTextEffectConfig config) {

        int espacement   = Math.max(0, config.getDistance());
        int transparence = Math.clamp(config.getTransparence(), 0, 255);

        Shape formeOriginale = layout.getOutline(AffineTransform.getTranslateInstance(x, y));

        float descent   = layout.getDescent();
        float axeMiroir = y + descent + espacement;

        AffineTransform miroir = new AffineTransform();
        miroir.translate(0, 2.0 * axeMiroir);
        miroir.scale(1, -1);

        Shape formeReflet = miroir.createTransformedShape(formeOriginale);
        Rectangle2D bounds = formeReflet.getBounds2D();

        float yDebut = (float) bounds.getMinY();
        float yFin   = (float) bounds.getMaxY();
        if (yFin <= yDebut) return;

        GradientPaint degrade = new GradientPaint(
                0, yDebut, new Color(0, 0, 0, transparence),
                0, yFin,   new Color(0, 0, 0, 0)
        );

        g2.setPaint(degrade);
        g2.fill(formeReflet);
    }

    // =========================================================================
    // Utilitaires privés
    // =========================================================================

    /**
     * Lit la liste d'effets depuis les attributs d'un segment.
     *
     * <p>Si aucune liste n'est trouvée (segment sans effet ou segment créé
     * avant la v3), retourne {@code null}.</p>
     */
    @SuppressWarnings("unchecked")
    private static List<HEffetEntree> lireListeEffets(AttributeSet attrs) {
        Object valeur = attrs.getAttribute(EFFECTS_LIST_ATTRIBUTE);
        if (valeur instanceof List<?> liste && !liste.isEmpty()
                && liste.get(0) instanceof HEffetEntree) {
            return (List<HEffetEntree>) liste;
        }
        return null;
    }

    /**
     * Calcule le rectangle en pixels correspondant à une position dans le document.
     *
     * <p>{@code view.modelToView()} est le pont entre le modèle (index de caractère)
     * et la vue (coordonnées pixels dans le composant).</p>
     */
    private static Rectangle calculerRectangle(View view, int pos) {
        try {
            java.awt.Shape forme = view.modelToView(
                    pos,
                    new Rectangle(0, 0, 10000, 10000),
                    Position.Bias.Forward
            );
            return forme != null ? forme.getBounds() : null;
        } catch (BadLocationException e) {
            return null;
        }
    }

    /**
     * Reconstruit la {@link Font} à partir des attributs du segment.
     * Applique des valeurs de repli si les attributs sont absents.
     */
    private static Font resoudreFont(AttributeSet attrs) {
        String famille = StyleConstants.getFontFamily(attrs);
        int    taille  = StyleConstants.getFontSize(attrs);
        boolean gras   = StyleConstants.isBold(attrs);
        boolean italic = StyleConstants.isItalic(attrs);

        int style = Font.PLAIN;
        if (gras)   style |= Font.BOLD;
        if (italic) style |= Font.ITALIC;

        if (famille == null || famille.isBlank()) famille = "SansSerif";
        if (taille <= 0)                          taille  = 12;

        return new Font(famille, style, taille);
    }

    /**
     * Résout la couleur à utiliser pour un effet.
     *
     * <p>Ordre de priorité :</p>
     * <ol>
     *   <li>La couleur stockée dans l'{@link HEffetEntree} (choix explicite de l'utilisateur)</li>
     *   <li>La couleur de texte du segment ({@code StyleConstants.Foreground})</li>
     *   <li>Gris neutre par défaut</li>
     * </ol>
     *
     * @param attrs          attributs du segment (pour le fallback Foreground)
     * @param couleurEntree  couleur stockée dans l'entrée d'effet ({@code null} possible)
     */
    private static Color resoudreCouleurEffet(AttributeSet attrs, Color couleurEntree) {
        if (couleurEntree != null) {
            return couleurEntree;
        }
        Color foreground = StyleConstants.getForeground(attrs);
        if (foreground != null) {
            return foreground;
        }
        return Color.GRAY;
    }

    // =========================================================================
    // Classe interne — clé d'attribut custom
    // =========================================================================

    /**
     * Clé d'attribut typée pour le {@link StyledDocument}.
     *
     * <p>Swing identifie les attributs par identité d'objet ({@code ==}) dans
     * une {@code AttributeSet}. On crée nos propres instances pour éviter toute
     * collision avec les clés standard de {@link StyleConstants}.</p>
     */
    private static final class AttributeKey {
        private final String nom;

        AttributeKey(String nom) {
            this.nom = nom;
        }

        @Override
        public String toString() {
            return nom;
        }
    }
}
