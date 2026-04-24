package htextarea;

import javax.swing.text.*;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Moteur de rendu des effets typographiques pour {@link HTextArea}.
 *
 * <p>Cette classe est appelée par {@link HBasicTextAreaUI} lors du rendu du
 * composant, <em>avant</em> que Swing ne peigne le texte normal. Elle travaille
 * segment par segment : pour chaque portion de texte portant un
 * {@link HTextEffect} différent de {@code NONE}, elle lit la
 * {@link HTextEffectConfig} associée et délègue au moteur de peinture
 * correspondant.</p>
 *
 * <h3>Clés d'attributs stockées dans le StyledDocument</h3>
 * <ul>
 *   <li>{@link #EFFECT_ATTRIBUTE}        — l'enum {@link HTextEffect}</li>
 *   <li>{@link #EFFECT_COLOR_ATTRIBUTE}  — la {@link Color} de l'effet</li>
 *   <li>{@link #EFFECT_CONFIG_ATTRIBUTE} — le {@link HTextEffectConfig}</li>
 * </ul>
 *
 * @author FIDELE
 * @version 2.0
 * @see HTextEffect
 * @see HTextEffectConfig
 * @see HEffectDirection
 * @see HBasicTextAreaUI
 */
public class HEffectPainter {

    // =========================================================================
    // Clés d'attributs custom pour le StyledDocument
    // =========================================================================

    /**
     * Clé pour stocker le type d'effet ({@link HTextEffect}) dans le document.
     */
    public static final Object EFFECT_ATTRIBUTE = new AttributeKey("HTextEffect");

    /**
     * Clé pour stocker la couleur de l'effet ({@link Color}) dans le document.
     */
    public static final Object EFFECT_COLOR_ATTRIBUTE = new AttributeKey("HTextEffectColor");

    /**
     * Clé pour stocker la configuration de l'effet ({@link HTextEffectConfig})
     * dans le document.
     */
    public static final Object EFFECT_CONFIG_ATTRIBUTE = new AttributeKey("HTextEffectConfig");

    // =========================================================================
    // Point d'entrée principal
    // =========================================================================

    /**
     * Peint les effets typographiques pour toute la plage de texte visible.
     *
     * <p>Appelée par {@link HBasicTextAreaUI#paintSafely(Graphics)} avant le
     * rendu standard de Swing, pour que les effets apparaissent derrière le
     * texte.</p>
     *
     * @param g    le contexte graphique (sera casté en {@link Graphics2D})
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

            // Parcourir tous les éléments (segments) du document
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
     * Seuls les éléments feuilles portent du texte et des attributs de style.
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
     * Traite un segment feuille : si un effet est défini, calcule la position
     * exacte du texte dans le composant et délègue au moteur de peinture.
     *
     * <p>La position est calculée via {@code modelToView()} qui traduit un index
     * de caractère (espace document) en coordonnées pixels (espace composant).
     * L'ascent du {@link TextLayout} est ajouté au {@code y} du rectangle pour
     * obtenir la baseline — le point d'ancrage que {@code layout.draw()} attend.</p>
     */
    private static void traiterSegment(Graphics2D g2, StyledDocument doc,
            View view, Element element) {

        AttributeSet attrs = element.getAttributes();
        Object valeurEffet = attrs.getAttribute(EFFECT_ATTRIBUTE);

        // Pas d'effet ou effet NONE → rien à faire, Swing s'en charge
        if (!(valeurEffet instanceof HTextEffect effet) || effet == HTextEffect.NONE) {
            return;
        }

        int debut = element.getStartOffset();
        int fin   = element.getEndOffset() - 1; // -1 pour exclure le '\n' de fin
        if (debut >= fin) {
            return;
        }

        try {
            String texte = doc.getText(debut, fin - debut);
            if (texte.isBlank()) {
                return;
            }

            // Coordonnées du premier caractère du segment dans l'espace composant
            Rectangle rectDebut = calculerRectangle(view, debut);
            if (rectDebut == null) {
                return;
            }

            Font police = resoudreFont(attrs);

            // TextLayout pour mesurer la typométrie exacte de ce segment
            TextLayout layout = new TextLayout(texte, police, g2.getFontRenderContext());

            // x = bord gauche du segment
            // baseline = haut de la ligne + ascent du layout (comme Swing le fait en interne)
            int x        = rectDebut.x;
            int baseline = rectDebut.y + (int) Math.ceil(layout.getAscent());

            // Lire la config — si absente, on utilise une config par défaut
            HTextEffectConfig config = resoudreConfig(attrs);

            // Lire la couleur de l'effet
            Color couleurEffet = resoudreCouleurEffet(attrs);

            peindreEffet(g2, layout, police, x, baseline, effet, couleurEffet, config);

        } catch (BadLocationException e) {
            // Segment hors limites — on ignore silencieusement
        }
    }

    // =========================================================================
    // Dispatch vers le bon moteur
    // =========================================================================

    /**
     * Dispatche vers le moteur de peinture correspondant à l'effet demandé.
     *
     * @param g2           contexte graphique
     * @param layout       layout du segment, déjà calculé
     * @param police       police résolue du segment
     * @param x            position X du segment (bord gauche)
     * @param y            baseline du segment
     * @param effet        effet à appliquer
     * @param couleurEffet couleur de l'effet
     * @param config       paramètres de l'effet
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
            case NONE       -> {  }
        }
    }

    // =========================================================================
    // Moteurs de peinture — SHADOW
    // =========================================================================

    /**
     * Peint une ombre portée derrière le texte.
     *
     * <h3>Direction</h3>
     * <p>La {@link HEffectDirection} contenue dans {@code config} est traduite
     * en un vecteur {@code (dx, dy)} :</p>
     * <ul>
     *   <li>BAS_DROITE  → (+distance, +distance)</li>
     *   <li>BAS_GAUCHE  → (-distance, +distance)</li>
     *   <li>HAUT_DROITE → (+distance, -distance)</li>
     *   <li>HAUT_GAUCHE → (-distance, -distance)</li>
     *   <li>CENTREE     → ombre dans les 4 diagonales simultanément</li>
     * </ul>
     *
     * <h3>Flou simulé</h3>
     * <p>Le flou est simulé par plusieurs couches d'ombres superposées, chacune
     * légèrement décalée et de plus en plus transparente. Plus {@code config.getFlou()}
     * est élevé, plus il y a de couches (0 = 1 couche, 5 = 6 couches).</p>
     *
     * @param g2           contexte graphique
     * @param layout       layout du segment
     * @param x            position X du texte (bord gauche)
     * @param y            baseline du texte
     * @param couleurOmbre couleur de l'ombre
     * @param config       paramètres : direction, distance, flou, transparence
     */
    private static void peindreOmbre(Graphics2D g2, TextLayout layout,
            int x, int y, Color couleurOmbre, HTextEffectConfig config) {

        int distance    = config.getDistance();
        int flou        = config.getFlou();        // 0 à 5
        int transparence = config.getTransparence(); // 0 à 255
        HEffectDirection direction = config.getDirection();

        // Nombre de couches de flou : 1 couche si flou=0, 6 couches si flou=5
        int nbCouches = flou + 1;

        if (direction == HEffectDirection.CENTREE) {
            // Ombre centrée : on peint dans les 4 directions diagonales
            int[] signesX = {+1, -1, +1, -1};
            int[] signesY = {+1, +1, -1, -1};

            for (int dir = 0; dir < 4; dir++) {
                peindreOmbreDansDirection(g2, layout, x, y,
                        couleurOmbre, transparence, nbCouches, distance,
                        signesX[dir], signesY[dir]);
            }
        } else {
            // Ombre directionnelle classique
            int[] vecteur = resoudreVecteurDirection(direction);
            peindreOmbreDansDirection(g2, layout, x, y,
                    couleurOmbre, transparence, nbCouches, distance,
                    vecteur[0], vecteur[1]);
        }
    }

    /**
     * Peint toutes les couches de flou d'une ombre dans une seule direction.
     *
     * <p>Chaque couche est légèrement plus décalée et plus transparente que la
     * précédente, créant l'illusion d'une diffusion progressive de l'ombre.</p>
     *
     * @param g2          contexte graphique
     * @param layout      layout du segment
     * @param x           position X de base du texte
     * @param y           baseline de base du texte
     * @param couleur     couleur de base de l'ombre
     * @param transparence alpha maximal de l'ombre (couche la plus proche)
     * @param nbCouches   nombre de couches à peindre (1 + niveau de flou)
     * @param distance    décalage maximal en pixels
     * @param signeX      signe du décalage horizontal (-1 ou +1)
     * @param signeY      signe du décalage vertical (-1 ou +1)
     */
    private static void peindreOmbreDansDirection(Graphics2D g2, TextLayout layout,
            int x, int y, Color couleur, int transparence, int nbCouches,
            int distance, int signeX, int signeY) {

        for (int couche = 0; couche < nbCouches; couche++) {
            // La couche 0 est la plus proche du texte (la plus opaque)
            // La dernière couche est la plus éloignée (la plus transparente)
            float facteurOpacite = (nbCouches == 1)
                    ? 1f
                    : 1f - ((float) couche / (nbCouches - 1)) * 0.7f;

            int alpha = Math.clamp(Math.round(transparence * facteurOpacite), 0, 255);

            // Le décalage augmente légèrement d'une couche à l'autre pour simuler la diffusion
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
     * Traduit une {@link HEffectDirection} en vecteur de signe {@code [signeX, signeY]}.
     *
     * @param direction la direction à traduire
     * @return un tableau {@code [signeX, signeY]} dont chaque valeur est -1 ou +1
     */
    private static int[] resoudreVecteurDirection(HEffectDirection direction) {
        return switch (direction) {
            case BAS_DROITE  -> new int[]{ 1,  1};
            case BAS_GAUCHE  -> new int[]{-1,  1};
            case HAUT_DROITE -> new int[]{ 1, -1};
            case HAUT_GAUCHE -> new int[]{-1, -1};
            // CENTREE est traité séparément dans peindreOmbre()
            default          -> new int[]{ 1,  1};
        };
    }

    // =========================================================================
    // Moteurs de peinture — OUTLINE
    // =========================================================================

    /**
     * Peint uniquement le contour vectoriel des glyphes (effet outline).
     *
     * <p>{@link TextLayout#getOutline(AffineTransform)} retourne la {@link Shape}
     * exacte du contour de chaque lettre, translatée à la position du segment.
     * On trace ce contour sans remplissage avec un trait dont l'épaisseur
     * correspond à {@code config.getDistance()}.</p>
     *
     * <p>Swing repeint le texte par-dessus après cette passe, ce qui donne
     * l'effet "lettre remplie avec un contour visible".</p>
     *
     * @param g2           contexte graphique
     * @param layout       layout du segment
     * @param x            position X du texte
     * @param y            baseline du texte
     * @param couleurContour couleur du trait de contour
     * @param config       paramètres : distance (= épaisseur), transparence
     */
    private static void peindreContour(Graphics2D g2, TextLayout layout,
            int x, int y, Color couleurContour, HTextEffectConfig config) {

        // distance = épaisseur du trait (minimum 0.5px pour rester visible)
        float epaisseur  = Math.max(0.5f, config.getDistance());
        int transparence = config.getTransparence();

        // Translate la forme vectorielle des lettres à la position exacte du segment
        Shape contour = layout.getOutline(
                AffineTransform.getTranslateInstance(x, y)
        );

        g2.setStroke(new BasicStroke(
                epaisseur,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));
        g2.setColor(new Color(
                couleurContour.getRed(),
                couleurContour.getGreen(),
                couleurContour.getBlue(),
                Math.clamp(transparence, 0, 255)
        ));
        g2.draw(contour);
    }

    // =========================================================================
    // Moteurs de peinture — LIGHT
    // =========================================================================

    /**
     * Peint un halo lumineux (effet lumière/glow) autour du texte.
     *
     * <p>Technique : on dessine le {@link TextLayout} plusieurs fois en couches
     * concentriques, chacune décalée d'un pixel supplémentaire dans les 8
     * directions cardinales et de plus en plus transparente en s'éloignant du
     * centre. L'ensemble simule la diffusion lumineuse d'un halo.</p>
     *
     * <p>Le paramètre {@code config.getDistance()} contrôle le rayon maximal du
     * halo. {@code config.getTransparence()} contrôle l'alpha de la couche
     * intérieure (la plus opaque).</p>
     *
     * @param g2           contexte graphique
     * @param layout       layout du segment
     * @param x            position X du texte
     * @param y            baseline du texte
     * @param couleurHalo  couleur du halo
     * @param config       paramètres : distance (= rayon), transparence
     */
    private static void peindreLumiere(Graphics2D g2, TextLayout layout,
            int x, int y, Color couleurHalo, HTextEffectConfig config) {

        // Nombre de couches fixe (5 couches suffisent pour un rendu fluide)
        final int NB_COUCHES = 5;

        // Le rayon est contrôlé par config.getDistance()
        int rayonMax     = Math.max(1, config.getDistance());
        int transparence = config.getTransparence();

        // On peint du plus éloigné vers le centre pour que les couches
        // intérieures (plus opaques) soient dessinées en dernier et dominent
        for (int couche = NB_COUCHES; couche >= 1; couche--) {
            float facteur = (float) couche / NB_COUCHES;
            int   rayon   = Math.round(facteur * rayonMax);

            // Transparence décroissante en s'éloignant du centre :
            // couche intérieure = transparence config, couche externe ≈ 20
            int alpha = Math.round((1f - facteur) * transparence + 20);
            alpha = Math.clamp(alpha, 0, 255);

            g2.setColor(new Color(
                    couleurHalo.getRed(),
                    couleurHalo.getGreen(),
                    couleurHalo.getBlue(),
                    alpha
            ));

            // Peindre dans les 8 directions pour un halo uniforme
            for (int dx = -rayon; dx <= rayon; dx += Math.max(1, rayon)) {
                for (int dy = -rayon; dy <= rayon; dy += Math.max(1, rayon)) {
                    if (dx == 0 && dy == 0) {
                        continue; // le centre est peint par Swing
                    }
                    layout.draw(g2, x + dx, y + dy);
                }
            }
        }
    }

    // =========================================================================
    // Moteurs de peinture — REFLECTION
    // =========================================================================

    /**
     * Peint un reflet en miroir sous le texte (effet réflexion).
     *
     * <h3>Algorithme</h3>
     * <ol>
     *   <li>Obtenir la {@link Shape} vectorielle du texte via
     *       {@link TextLayout#getOutline(AffineTransform)}.</li>
     *   <li>Calculer l'axe de symétrie : {@code baseline + descent + espacement}.</li>
     *   <li>Appliquer une transformation miroir (scale 1, -1) autour de cet axe.</li>
     *   <li>Remplir la forme obtenue avec un dégradé vertical qui s'efface
     *       progressivement vers le bas.</li>
     * </ol>
     *
     * <p>{@code config.getDistance()} contrôle l'espace en pixels entre le bas
     * du texte et le début du reflet. {@code config.getTransparence()} contrôle
     * l'opacité maximale du reflet (en haut).</p>
     *
     * @param g2     contexte graphique
     * @param layout layout du segment
     * @param x      position X du texte
     * @param y      baseline du texte
     * @param config paramètres : distance (= espacement), transparence
     */
    private static void peindreReflection(Graphics2D g2, TextLayout layout,
            int x, int y, HTextEffectConfig config) {

        int espacement   = Math.max(0, config.getDistance());
        int transparence = Math.clamp(config.getTransparence(), 0, 255);

        // Étape 1 — Forme vectorielle du texte à sa position réelle
        Shape formeOriginale = layout.getOutline(
                AffineTransform.getTranslateInstance(x, y)
        );

        // Étape 2 — Axe de symétrie = pied du texte + espacement voulu
        // descent = distance entre la baseline et le bas des lettres descendantes
        float descent    = layout.getDescent();
        float axeMiroir  = y + descent + espacement;

        // Étape 3 — Transformation miroir vertical autour de l'axe
        // Réflexion : y' = 2 * axeMiroir - y
        // Décomposée en : translate(0, 2*axe) ∘ scale(1, -1)
        AffineTransform miroir = new AffineTransform();
        miroir.translate(0, 2.0 * axeMiroir);
        miroir.scale(1, -1);

        Shape formeReflet = miroir.createTransformedShape(formeOriginale);
        Rectangle2D bounds = formeReflet.getBounds2D();

        // Étape 4 — Dégradé vertical : opaque en haut du reflet → transparent en bas
        float yDebut  = (float) bounds.getMinY();
        float yFin    = (float) bounds.getMaxY();

        // On évite une hauteur nulle qui ferait planter GradientPaint
        if (yFin <= yDebut) {
            return;
        }

        GradientPaint degrade = new GradientPaint(
                0, yDebut, new Color(0, 0, 0, transparence),
                0, yFin,   new Color(0, 0, 0, 0)
        );

        // Étape 5 — Peinture du reflet
        g2.setPaint(degrade);
        g2.fill(formeReflet);
    }

    // =========================================================================
    // Méthodes utilitaires privées
    // =========================================================================

    /**
     * Calcule le rectangle en pixels correspondant à une position dans le document.
     *
     * <p>{@code view.modelToView()} traduit un index de caractère en coordonnées
     * écran. C'est le pont entre le modèle (le texte) et la vue (les pixels).</p>
     *
     * @param view la vue racine du composant
     * @param pos  la position dans le document
     * @return le rectangle en pixels, ou {@code null} en cas d'erreur
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
     * Reconstruit la {@link Font} à partir des attributs d'un segment.
     *
     * @param attrs les attributs du segment
     * @return la police correspondante, avec des valeurs de repli si besoin
     */
    private static Font resoudreFont(AttributeSet attrs) {
        String famille = StyleConstants.getFontFamily(attrs);
        int    taille  = StyleConstants.getFontSize(attrs);
        boolean gras   = StyleConstants.isBold(attrs);
        boolean italic = StyleConstants.isItalic(attrs);

        int style = Font.PLAIN;
        if (gras)   style |= Font.BOLD;
        if (italic) style |= Font.ITALIC;

        // Valeurs de repli si les attributs sont absents du segment
        if (famille == null || famille.isBlank()) famille = "SansSerif";
        if (taille <= 0)                          taille  = 12;

        return new Font(famille, style, taille);
    }

    /**
     * Lit la couleur d'effet depuis les attributs du segment.
     *
     * <p>Ordre de priorité :</p>
     * <ol>
     *   <li>L'attribut custom {@link #EFFECT_COLOR_ATTRIBUTE}</li>
     *   <li>La couleur de texte (Foreground) du segment</li>
     *   <li>Gris neutre par défaut</li>
     * </ol>
     *
     * @param attrs les attributs du segment
     * @return la couleur à utiliser pour l'effet
     */
    private static Color resoudreCouleurEffet(AttributeSet attrs) {
        // 1. Couleur explicitement choisie pour l'effet
        Object valeur = attrs.getAttribute(EFFECT_COLOR_ATTRIBUTE);
        if (valeur instanceof Color couleur) {
            return couleur;
        }
        // 2. Couleur de texte du segment
        Color foreground = StyleConstants.getForeground(attrs);
        if (foreground != null) {
            return foreground;
        }
        // 3. Repli neutre
        return Color.GRAY;
    }

    /**
     * Lit la configuration d'effet depuis les attributs du segment.
     *
     * <p>Si aucune config n'est trouvée (l'effet a été appliqué sans config),
     * on retourne une instance par défaut pour ne pas casser les appels
     * existants faits avec les anciennes méthodes de {@link HTextArea}.</p>
     *
     * @param attrs les attributs du segment
     * @return la config trouvée, ou une config par défaut
     */
    private static HTextEffectConfig resoudreConfig(AttributeSet attrs) {
        Object valeur = attrs.getAttribute(EFFECT_CONFIG_ATTRIBUTE);
        if (valeur instanceof HTextEffectConfig config) {
            return config;
        }
        // Compatibilité ascendante : si pas de config, on utilise les valeurs par défaut
        return new HTextEffectConfig();
    }

    // =========================================================================
    // Classe interne — clé d'attribut custom
    // =========================================================================

    /**
     * Clé d'attribut typée pour le {@link StyledDocument}.
     *
     * <p>Swing identifie les attributs par des objets utilisés comme clés dans
     * une {@code AttributeSet}. On crée nos propres clés pour éviter toute
     * collision avec les clés standard de {@link StyleConstants}.</p>
     *
     * <p>La méthode {@code toString()} est importante : Swing l'utilise lors
     * de la sérialisation et du débogage des attributs.</p>
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
