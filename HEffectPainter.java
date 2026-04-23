package htextarea;

import javax.swing.text.*;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Moteur de rendu des effets typographiques pour {@link hcomponents.HTextArea}.
 *
 * <p>
 * Cette classe est appelée par {@link HBasicTextAreaUI} lors du rendu du
 * composant. Elle travaille segment par segment : pour chaque portion de texte
 * portant un {@link HTextEffect} différent de {@code NONE}, elle peint l'effet
 * demandé
 * <em>avant</em> que Swing ne peigne le texte normal par-dessus.</p>
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
     * Clé utilisée pour stocker un {@link HTextEffect} comme attribut de
     * caractère dans le {@link javax.swing.text.StyledDocument}.
     *
     * <p>
     * Exemple d'utilisation dans {@code HTextArea} :</p>
     * <pre>
     *     SimpleAttributeSet attrs = new SimpleAttributeSet();
     *     attrs.addAttribute(HEffectPainter.EFFECT_ATTRIBUTE, HTextEffect.SHADOW);
     *     doc.setCharacterAttributes(debut, longueur, attrs, false);
     * </pre>
     */
    public static final Object EFFECT_ATTRIBUTE = new AttributeKey("HTextEffect");

    /**
     * Clé utilisée pour stocker la couleur de l'effet (ex: couleur du halo pour
     * GLOW, couleur du contour pour OUTLINE).
     */
    public static final Object EFFECT_COLOR_ATTRIBUTE = new AttributeKey("HTextEffectColor");

    // -------------------------------------------------------------------------
    // Paramètres visuels des effets (ajustables)
    // -------------------------------------------------------------------------
    /**
     * Décalage en pixels de l'ombre portée (SHADOW).
     */
    private static final int SHADOW_OFFSET = 2;

    /**
     * Transparence de l'ombre portée (0 = invisible, 255 = opaque).
     */
    private static final int SHADOW_ALPHA = 100;

    /**
     * Épaisseur du trait pour l'effet OUTLINE.
     */
    private static final float OUTLINE_STROKE = 1.4f;

    /**
     * Décalage des ombres pour EMBOSS et ENGRAVE.
     */
    private static final int RELIEF_OFFSET = 1;

    /**
     * Nombre de couches pour l'effet GLOW.
     */
    private static final int GLOW_LAYERS = 5;

    /**
     * Rayon maximum du halo GLOW en pixels.
     */
    private static final int GLOW_RADIUS = 4;

    // -------------------------------------------------------------------------
    // Point d'entrée principal
    // -------------------------------------------------------------------------
    /**
     * Peint les effets typographiques pour toute la plage de texte visible.
     *
     * <p>
     * Cette méthode est appelée par
     * {@link HBasicTextAreaUI#paintSafely(Graphics)}
     * <em>avant</em> le rendu standard de Swing, pour que les effets
     * apparaissent derrière le texte.</p>
     *
     * <p>
     * Elle parcourt le document segment par segment (un segment = une suite de
     * caractères ayant les mêmes attributs). Pour chaque segment portant un
     * effet différent de {@code NONE}, elle délègue au bon moteur de
     * peinture.</p>
     *
     * @param g le contexte graphique (sera casté en {@link Graphics2D})
     * @param doc le document stylisé du composant
     * @param view la vue racine de Swing (permet de convertir position doc →
     * pixels)
     */
    public static void peindreEffets(Graphics g, StyledDocument doc, View view) {
        if (g == null || doc == null || view == null) {
            return;
        }

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
        if (!(valeurEffet instanceof HTextEffect effet) || effet == HTextEffect.NONE) {
            return;
        }

        int debut = element.getStartOffset();
        int fin = element.getEndOffset() - 1; // -1 pour exclure le '\n' de fin
        if (debut >= fin) {
            return;
        }

        try {
            String texte = doc.getText(debut, fin - debut);
            if (texte.isBlank()) {
                return;
            }

            // Convertir la position dans le document en coordonnées écran
            Rectangle rectDebut = calculerRectangle(view, debut);
            if (rectDebut == null) {
                return;
            }

            // Construire la Font depuis les attributs du segment
            Font police = resoudreFont(attrs);

            // Déléguer au bon moteur selon l'effet demandé
            Color couleurEffet = resoudreCouleurEffet(attrs, g2.getColor());
            int baseline = rectDebut.y + g2.getFontMetrics(police).getAscent();
            peindreEffet(g2, texte, police, rectDebut.x, baseline,
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

        if (texte == null || texte.isEmpty() || effet == HTextEffect.NONE) {
            return;
        }

        // TextLayout = base fiable pour tous les effets
        TextLayout layout = new TextLayout(
                texte,
                police,
                g2.getFontRenderContext()
        );

        // Couleur fallback propre
        Color color = (couleurEffet != null) ? couleurEffet : Color.GRAY;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        switch (effet) {

            case SHADOW ->
                peindreOmbre(g2, layout, police, x, y, color);

            case OUTLINE ->
                peindreContour(g2, layout, x, y, color);

            case GLOW ->
                peindreGlow(g2, layout, police, x, y, color);

            case REFLECTION ->
                peindreReflection(g2, layout, police, x, y);

            case LIGHT ->
                peindreLumiere(g2, layout, x, y);

            case NONE -> {
                // rien (le texte normal sera peint par Swing)
            }
        }
    }

    /**
     * Peint une ombre portée derrière le texte.
     *
     * <p>
     * Technique : on dessine le texte une première fois en gris
     * semi-transparent, décalé de {@value #SHADOW_OFFSET} pixels vers le
     * bas-droite. Swing peindra ensuite le texte normal par-dessus lors de sa
     * passe standard.</p>
     *
     * @param g2 le contexte graphique
     * @param layout le TextLayout du segment
     * @param x position horizontale de base
     * @param y position verticale de base (baseline)
     * @param couleurOmbre la couleur de l'ombre (généralement un gris)
     */
    private static void peindreOmbre(Graphics2D g2, TextLayout layout, Font font, int x, int y, Color color) {

        int w = (int) layout.getBounds().getWidth() + 20;
        int h = (int) layout.getBounds().getHeight() + 20;

        BufferedImage base = createTextImage(layout, w, h, font);

        BufferedImage shadow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = shadow.createGraphics();

        sg.drawImage(base, 0, 0, null);
        sg.setComposite(AlphaComposite.SrcIn);
        sg.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120));
        sg.fillRect(0, 0, w, h);
        sg.dispose();

        // blur simulé (multi draw)
        for (int i = 1; i <= 4; i++) {
            g2.drawImage(shadow, x + i, y + i, null);
        }
    }

    /**
     * Peint uniquement le contour des glyphes (effet outline).
     *
     * <p>
     * Technique : {@link TextLayout#getOutline(AffineTransform)} retourne la
     * {@link Shape} exacte du contour de chaque lettre. On la trace avec
     * {@link Graphics2D#draw(Shape)} sans la remplir.</p>
     *
     * @param g2 le contexte graphique
     * @param layout le TextLayout du segment
     * @param x position horizontale
     * @param y position verticale (baseline)
     * @param couleurContour la couleur du trait de contour
     */
    private static void peindreContour(Graphics2D g2, TextLayout layout, int x, int y, Color color) {

        Shape shape = layout.getOutline(AffineTransform.getTranslateInstance(x, y));

        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(color);
        g2.draw(shape);
    }

    private static void peindreLumiere(Graphics2D g2, TextLayout layout, int x, int y) {

        Shape shape = layout.getOutline(AffineTransform.getTranslateInstance(x, y));

        GradientPaint gp = new GradientPaint(
                x, y - 10,
                new Color(255, 255, 255, 180),
                x, y + 10,
                new Color(255, 255, 255, 0)
        );

        g2.setPaint(gp);
        g2.fill(shape);
    }

    private static void peindreReflection(Graphics2D g2, TextLayout layout, Font font, int x, int y) {

        int w = (int) layout.getBounds().getWidth() + 20;
        int h = (int) layout.getBounds().getHeight() + 20;

        BufferedImage base = createTextImage(layout, w, h, font);

        BufferedImage flipped = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D fg = flipped.createGraphics();

        fg.drawImage(base, 0, h, w, -h, null); // flip vertical
        fg.dispose();

        // fade gradient
        Graphics2D gFade = flipped.createGraphics();
        gFade.setComposite(AlphaComposite.DstIn);
        GradientPaint gp = new GradientPaint(0, 0, new Color(0, 0, 0, 150),
                0, h, new Color(0, 0, 0, 0));
        gFade.setPaint(gp);
        gFade.fillRect(0, 0, w, h);
        gFade.dispose();

        g2.drawImage(flipped, x, y + 5, null);
    }

    /**
     * Peint un halo lumineux autour du texte (effet glow).
     *
     * <p>
     * Technique : {@value #GLOW_LAYERS} couches d'ombres concentriques, chacune
     * de plus en plus transparente en s'éloignant du texte. Cela simule la
     * diffusion lumineuse d'un halo.</p>
     *
     * @param g2 le contexte graphique
     * @param layout le TextLayout du segment
     * @param x position horizontale
     * @param y position verticale (baseline)
     * @param couleurHalo la couleur du halo (souvent une couleur vive)
     */
    private static void peindreGlow(Graphics2D g2, TextLayout layout, Font font, int x, int y, Color color) {

        int w = (int) layout.getBounds().getWidth() + 30;
        int h = (int) layout.getBounds().getHeight() + 30;

        BufferedImage base = createTextImage(layout, w, h, font);

        for (int i = 6; i >= 1; i--) {
            float alpha = 0.08f * i;

            BufferedImage glow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gg = glow.createGraphics();

            gg.drawImage(base, 0, 0, null);
            gg.setComposite(AlphaComposite.SrcIn);
            gg.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
            gg.fillRect(0, 0, w, h);
            gg.dispose();

            g2.drawImage(glow, x - i, y - i, null);
            g2.drawImage(glow, x + i, y + i, null);
        }
    }

    // -------------------------------------------------------------------------
    // Méthodes utilitaires privées
    // -------------------------------------------------------------------------
    /**
     * Calcule le rectangle en pixels correspondant à une position dans le
     * document.
     *
     * <p>
     * {@code view.modelToView2D()} traduit un index de caractère (position dans
     * le document texte) en coordonnées écran. C'est le pont entre le modèle
     * (le texte) et la vue (les pixels).</p>
     *
     * @param view la vue racine du composant
     * @param pos la position dans le document
     * @return le rectangle en pixels, ou {@code null} en cas d'erreur
     */
    private static Rectangle calculerRectangle(View view, int pos) {
        try {
            // modelToView(int, Shape, Bias) est la signature correcte sur View
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
     * Reconstruit la {@link Font} à partir des attributs d'un segment du
     * document.
     *
     * @param attrs les attributs du segment
     * @return la Font correspondante
     */
    private static Font resoudreFont(AttributeSet attrs) {
        String famille = StyleConstants.getFontFamily(attrs);
        int taille = StyleConstants.getFontSize(attrs);
        boolean gras = StyleConstants.isBold(attrs);
        boolean italic = StyleConstants.isItalic(attrs);

        int style = Font.PLAIN;
        if (gras) {
            style |= Font.BOLD;
        }
        if (italic) {
            style |= Font.ITALIC;
        }

        // Valeurs de repli si les attributs sont absents
        if (famille == null || famille.isBlank()) {
            famille = "SansSerif";
        }
        if (taille <= 0) {
            taille = 12;
        }

        return new Font(famille, style, taille);
    }

    private static BufferedImage createTextImage(TextLayout layout, int w, int h, Font font) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        g.setFont(font);
        g.setColor(Color.WHITE);

        int baseline = (int) layout.getAscent();
        layout.draw(g, 0, baseline);
        g.dispose();
        return img;
    }

    /**
     * Lit la couleur d'effet depuis les attributs, ou retourne une couleur par
     * défaut.
     *
     * @param attrs les attributs du segment
     * @param couleurDefaut la couleur à utiliser si aucune n'est définie (ex:
     * gris pour SHADOW)
     * @return la couleur de l'effet
     */
    private static Color resoudreCouleurEffet(AttributeSet attrs, Color couleurDefaut) {
        Object valeur = attrs.getAttribute(EFFECT_COLOR_ATTRIBUTE);
        if (valeur instanceof Color couleur) {
            return couleur;
        }
        // Couleur par défaut selon le contexte : gris moyen
        return couleurDefaut != null ? couleurDefaut : Color.GRAY;
    }

    // -------------------------------------------------------------------------
    // Classe interne pour les clés d'attributs custom
    // -------------------------------------------------------------------------
    /**
     * Clé d'attribut typée pour le {@link StyledDocument}.
     *
     * <p>
     * Swing identifie les attributs par des objets utilisés comme clés dans une
     * {@code AttributeSet}. On crée nos propres clés pour éviter les collisions
     * avec les clés standard de {@link StyleConstants}.</p>
     *
     * <p>
     * La méthode {@code toString()} est importante : Swing l'utilise lors de la
     * sérialisation et du débogage des attributs.</p>
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
