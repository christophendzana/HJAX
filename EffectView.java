package view;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

/**
 * Vue autonome pour le rendu d'effets typographiques sur du texte brut.
 *
 * <p>
 * Cette classe étend {@link HView} et permet d'appliquer un ou plusieurs effets
 * (ombre, contour, lumière, réflexion) sur un texte donné, sans aucune
 * dépendance externe.</p>
 *
 * <p>
 * L'utilisateur peut :
 * <ul>
 * <li>Définir le texte à afficher</li>
 * <li>Ajouter des effets dans l'ordre souhaité</li>
 * <li>Configurer chaque paramètre d'effet via des setters publics</li>
 * <li>Appeler {@link #Paint(Graphics, int, int)} pour dessiner</li>
 * </ul>
 * </p>
 *
 * @author FIDELE
 * @version 1.0
 */
public class EffectView extends HView {

    // =========================================================================
    // Constantes par défaut
    // =========================================================================
    /**
     * Police par défaut.
     */
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12);

    /**
     * Couleur de texte par défaut.
     */
    private static final Color DEFAULT_TEXT_COLOR = Color.BLACK;

    /**
     * Décalage d'ombre par défaut.
     */
    private static final int DEFAULT_SHADOW_OFFSET = 2;

    /**
     * Transparence d'ombre par défaut.
     */
    private static final int DEFAULT_SHADOW_ALPHA = 128;

    /**
     * Nombre de couches de flou par défaut.
     */
    private static final int DEFAULT_SHADOW_BLUR_LAYERS = 5;

    /**
     * Épaisseur de contour par défaut.
     */
    private static final float DEFAULT_OUTLINE_STROKE = 1.4f;

    /**
     * Transparence de contour par défaut.
     */
    private static final int DEFAULT_OUTLINE_ALPHA = 255;

    /**
     * Rayon de lumière par défaut.
     */
    private static final int DEFAULT_GLOW_RADIUS = 4;

    /**
     * Transparence de lumière par défaut.
     */
    private static final int DEFAULT_GLOW_ALPHA = 180;

    /**
     * Intensité de lumière par défaut (nombre de couches).
     */
    private static final int DEFAULT_GLOW_LAYERS = 5;

    /**
     * Espacement de réflexion par défaut.
     */
    private static final int DEFAULT_REFLECTION_SPACING = 2;

    /**
     * Transparence de réflexion par défaut.
     */
    private static final int DEFAULT_REFLECTION_ALPHA = 128;

    /**
     * Hauteur de réflexion par défaut (100%).
     */
    private static final float DEFAULT_REFLECTION_HEIGHT = 1.0f;

    // =========================================================================
    // Données
    // =========================================================================
    /**
     * Le texte à afficher.
     */
    private String texte;

    /**
     * La police extraite du texte (ou par défaut).
     */
    private Font font;

    /**
     * La couleur par défaut du texte.
     */
    private Color defaultColor;

    /**
     * La liste ordonnée des effets à appliquer.
     */
    private List<EffetApplique> effets;

    // =========================================================================
    // Paramètres configurables pour chaque effet
    // =========================================================================
    // --- SHADOW ---
    private int ombreDecalageX = DEFAULT_SHADOW_OFFSET;
    private int ombreDecalageY = DEFAULT_SHADOW_OFFSET;
    private int ombreTransparence = DEFAULT_SHADOW_ALPHA;
    private int ombreFlou = DEFAULT_SHADOW_BLUR_LAYERS;
    private HEffectDirection ombreDirection = HEffectDirection.BAS_DROITE;

    // --- OUTLINE ---
    private float contourEpaisseur = DEFAULT_OUTLINE_STROKE;
    private int contourTransparence = DEFAULT_OUTLINE_ALPHA;

    // --- LIGHT ---
    private int lumiereRayon = DEFAULT_GLOW_RADIUS;
    private int lumiereTransparence = DEFAULT_GLOW_ALPHA;
    private int lumiereIntensite = DEFAULT_GLOW_LAYERS;

    // --- REFLECTION ---
    private int refletEspacement = DEFAULT_REFLECTION_SPACING;
    private int refletTransparence = DEFAULT_REFLECTION_ALPHA;
    private float refletHauteur = DEFAULT_REFLECTION_HEIGHT;

    // =========================================================================
    // Constructeurs
    // =========================================================================
    /**
     * Constructeur avec texte uniquement. Utilise la police par défaut et la
     * couleur de texte par défaut.
     *
     * @param texte le texte à afficher
     */
    public EffectView(String texte) {
        this(texte, DEFAULT_FONT, DEFAULT_TEXT_COLOR);
    }

    /**
     * Constructeur avec texte et police.
     *
     * @param texte le texte à afficher
     * @param font la police à utiliser
     */
    public EffectView(String texte, Font font) {
        this(texte, font, DEFAULT_TEXT_COLOR);
    }

    /**
     * Constructeur complet.
     *
     * @param texte le texte à afficher
     * @param font la police à utiliser
     * @param defaultColor la couleur par défaut pour les effets
     */
    public EffectView(String texte, Font font, Color defaultColor) {
        this.texte = texte != null ? texte : "";
        this.font = font != null ? font : DEFAULT_FONT;
        this.defaultColor = defaultColor != null ? defaultColor : DEFAULT_TEXT_COLOR;
        this.effets = new ArrayList<>();
    }

    // =========================================================================
    // API publique - Gestion du texte
    // =========================================================================
    /**
     * Modifie le texte à afficher.
     *
     * @param texte le nouveau texte
     */
    public void setTexte(String texte) {
        this.texte = texte != null ? texte : "";
    }

    /**
     * Retourne le texte actuel.
     *
     * @return le texte
     */
    public String getTexte() {
        return texte;
    }

    /**
     * Modifie la police.
     *
     * @param font la nouvelle police
     */
    public void setFont(Font font) {
        this.font = font != null ? font : DEFAULT_FONT;
    }

    /**
     * Retourne la police actuelle.
     *
     * @return la police
     */
    public Font getFont() {
        return font;
    }

    /**
     * Modifie la couleur par défaut.
     *
     * @param defaultColor la nouvelle couleur par défaut
     */
    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor != null ? defaultColor : DEFAULT_TEXT_COLOR;
    }

    /**
     * Retourne la couleur par défaut.
     *
     * @return la couleur par défaut
     */
    public Color getDefaultColor() {
        return defaultColor;
    }

    // =========================================================================
    // API publique - Gestion des effets
    // =========================================================================
    /**
     * Ajoute un effet avec les paramètres par défaut.
     *
     * @param type le type d'effet à ajouter
     * @return cette instance (fluent API)
     */
    public EffectView ajouterEffet(HTextEffect type) {
        return ajouterEffet(type, null);
    }

    /**
     * Ajoute un effet avec une couleur spécifique.
     *
     * @param type le type d'effet à ajouter
     * @param couleur la couleur pour cet effet (null pour utiliser la couleur
     * par défaut)
     * @return cette instance (fluent API)
     */
    public EffectView ajouterEffet(HTextEffect type, Color couleur) {
        if (type != null && type != HTextEffect.NONE) {
            effets.add(new EffetApplique(type, couleur));
        }
        return this;
    }

    /**
     * Supprime tous les effets.
     *
     * @return cette instance (fluent API)
     */
    public EffectView viderEffets() {
        effets.clear();
        return this;
    }

    /**
     * Supprime un effet à un index donné.
     *
     * @param index l'index de l'effet à supprimer
     * @return cette instance (fluent API)
     */
    public EffectView supprimerEffet(int index) {
        if (index >= 0 && index < effets.size()) {
            effets.remove(index);
        }
        return this;
    }

    /**
     * Retourne le nombre d'effets configurés.
     *
     * @return le nombre d'effets
     */
    public int getNombreEffets() {
        return effets.size();
    }

    /**
     * Réinitialise tous les paramètres d'effets à leurs valeurs par défaut.
     */
    public void reinitialiserParametres() {
        ombreDecalageX = DEFAULT_SHADOW_OFFSET;
        ombreDecalageY = DEFAULT_SHADOW_OFFSET;
        ombreTransparence = DEFAULT_SHADOW_ALPHA;
        ombreFlou = DEFAULT_SHADOW_BLUR_LAYERS;
        ombreDirection = HEffectDirection.BAS_DROITE;

        contourEpaisseur = DEFAULT_OUTLINE_STROKE;
        contourTransparence = DEFAULT_OUTLINE_ALPHA;

        lumiereRayon = DEFAULT_GLOW_RADIUS;
        lumiereTransparence = DEFAULT_GLOW_ALPHA;
        lumiereIntensite = DEFAULT_GLOW_LAYERS;

        refletEspacement = DEFAULT_REFLECTION_SPACING;
        refletTransparence = DEFAULT_REFLECTION_ALPHA;
        refletHauteur = DEFAULT_REFLECTION_HEIGHT;
    }

    // =========================================================================
    // API publique - Setters pour les paramètres d'effets
    // =========================================================================
    // --- SHADOW ---
    public EffectView setOmbreDecalageX(int px) {
        this.ombreDecalageX = Math.max(0, px);
        return this;
    }

    public EffectView setOmbreDecalageY(int px) {
        this.ombreDecalageY = Math.max(0, px);
        return this;
    }

    public EffectView setOmbreTransparence(int alpha) {
        this.ombreTransparence = Math.clamp(alpha, 0, 255);
        return this;
    }

    public EffectView setOmbreFlou(int couches) {
        this.ombreFlou = Math.max(1, Math.min(20, couches));
        return this;
    }

    public EffectView setOmbreDirection(HEffectDirection direction) {
        this.ombreDirection = direction != null ? direction : HEffectDirection.BAS_DROITE;
        return this;
    }

    // --- OUTLINE ---
    public EffectView setContourEpaisseur(float px) {
        this.contourEpaisseur = Math.max(0.5f, px);
        return this;
    }

    public EffectView setContourTransparence(int alpha) {
        this.contourTransparence = Math.clamp(alpha, 0, 255);
        return this;
    }

    // --- LIGHT ---
    public EffectView setLumiereRayon(int px) {
        this.lumiereRayon = Math.max(1, px);
        return this;
    }

    public EffectView setLumiereTransparence(int alpha) {
        this.lumiereTransparence = Math.clamp(alpha, 0, 255);
        return this;
    }

    public EffectView setLumiereIntensite(int couches) {
        this.lumiereIntensite = Math.max(1, Math.min(20, couches));
        return this;
    }

    // --- REFLECTION ---
    public EffectView setRefletEspacement(int px) {
        this.refletEspacement = Math.max(0, px);
        return this;
    }

    public EffectView setRefletTransparence(int alpha) {
        this.refletTransparence = Math.clamp(alpha, 0, 255);
        return this;
    }

    public EffectView setRefletHauteur(float pourcentage) {
        this.refletHauteur = Math.clamp(pourcentage, 0.0f, 1.0f);
        return this;
    }

    // =========================================================================
    // API publique - Getters pour les paramètres d'effets
    // =========================================================================
    public int getOmbreDecalageX() {
        return ombreDecalageX;
    }

    public int getOmbreDecalageY() {
        return ombreDecalageY;
    }

    public int getOmbreTransparence() {
        return ombreTransparence;
    }

    public int getOmbreFlou() {
        return ombreFlou;
    }

    public HEffectDirection getOmbreDirection() {
        return ombreDirection;
    }

    public float getContourEpaisseur() {
        return contourEpaisseur;
    }

    public int getContourTransparence() {
        return contourTransparence;
    }

    public int getLumiereRayon() {
        return lumiereRayon;
    }

    public int getLumiereTransparence() {
        return lumiereTransparence;
    }

    public int getLumiereIntensite() {
        return lumiereIntensite;
    }

    public int getRefletEspacement() {
        return refletEspacement;
    }

    public int getRefletTransparence() {
        return refletTransparence;
    }

    public float getRefletHauteur() {
        return refletHauteur;
    }

    // =========================================================================
    // Méthode principale de rendu
    // =========================================================================
    /**
     * Dessine le texte avec tous les effets configurés à la position donnée.
     *
     * @param g le contexte graphique
     * @param x la coordonnée X de départ
     * @param y la coordonnée Y de départ (baseline)
     */
    @Override
    public void Paint(Graphics g, int x, int y) {
        // Vérifications préliminaires
        if (g == null || texte == null || texte.isEmpty() || effets.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        Color colorText = g2.getColor();
        try {
            // Configuration des hints pour un rendu de qualité
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            // Création du layout à partir du texte et de la police
            FontRenderContext frc = g2.getFontRenderContext();
            TextLayout layout = new TextLayout(texte, font, frc);

            // Calcul de la baseline
            int baseline = y + (int) Math.ceil(layout.getAscent());

            // Application de chaque effet dans l'ordre
            for (EffetApplique effet : effets) {
                appliquerEffet(g2, layout, x, baseline, effet);
            }

            g2.setColor(colorText);
            layout.draw(g2, x, baseline);
        } finally {
            g2.dispose();
        }
    }

    /**
     * Dessine le texte avec tous les effets configurés à la position donnée.
     * Variante avec position en Point.
     *
     * @param g le contexte graphique
     * @param position la position de départ
     */
    public void Paint(Graphics g, Point position) {
        Paint(g, position.x, position.y);
    }

    // =========================================================================
    // Moteur d'application des effets
    // =========================================================================
    /**
     * Applique un effet individuel sur le layout.
     */
    private void appliquerEffet(Graphics2D g2, TextLayout layout,
            int x, int y, EffetApplique effet) {

        HTextEffect type = effet.type;
        Color couleur = effet.couleur != null ? effet.couleur : defaultColor;

        switch (type) {
            case SHADOW ->
                peindreOmbre(g2, layout, x, y, couleur);
            case OUTLINE ->
                peindreContour(g2, layout, x, y, couleur);
            case LIGHT ->
                peindreLumiere(g2, layout, x, y, couleur);
            case REFLECTION ->
                peindreReflection(g2, layout, x, y);
            case NONE -> {
                /* Ne rien faire */ }
        }
    }

    // =========================================================================
    // SHADOW - Implémentation
    // =========================================================================
    /**
     * Peint une ombre portée derrière le texte.
     *
     * <p>
     * Le flou est simulé par plusieurs couches de plus en plus transparentes et
     * légèrement décalées. La direction CENTREE peint dans les 4
     * diagonales.</p>
     */
    private void peindreOmbre(Graphics2D g2, TextLayout layout,
            int x, int y, Color couleurOmbre) {

        int distance = ombreDecalageX; // On utilise X comme distance de base
        int flou = ombreFlou;
        int transparence = ombreTransparence;
        HEffectDirection direction = ombreDirection;

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
     */
    private void peindreOmbreDansDirection(Graphics2D g2, TextLayout layout,
            int x, int y, Color couleur, int transparence,
            int nbCouches, int distance,
            int signeX, int signeY) {

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
     * Traduit une {@link HEffectDirection} en vecteur de signes.
     */
    private int[] resoudreVecteurDirection(HEffectDirection direction) {
        return switch (direction) {
            case BAS_DROITE ->
                new int[]{1, 1};
            case BAS_GAUCHE ->
                new int[]{-1, 1};
            case HAUT_DROITE ->
                new int[]{1, -1};
            case HAUT_GAUCHE ->
                new int[]{-1, -1};
            default ->
                new int[]{1, 1};
        };
    }

    // =========================================================================
    // OUTLINE - Implémentation
    // =========================================================================
    /**
     * Peint le contour vectoriel des glyphes.
     */
    private void peindreContour(Graphics2D g2, TextLayout layout,
            int x, int y, Color couleurContour) {

        float epaisseur = Math.max(0.5f, contourEpaisseur);
        int transparence = contourTransparence;

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
    // LIGHT - Implémentation
    // =========================================================================
    /**
     * Peint un halo lumineux autour du texte.
     */
    private void peindreLumiere(Graphics2D g2, TextLayout layout,
            int x, int y, Color couleurHalo) {

        int rayonMax = Math.max(1, lumiereRayon);
        int transparence = lumiereTransparence;
        int intensite = lumiereIntensite;

        // Du plus éloigné vers le centre pour que les couches intérieures dominent
        for (int couche = intensite; couche >= 1; couche--) {
            float facteur = (float) couche / intensite;
            int rayon = Math.round(facteur * rayonMax);

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
                    if (dx == 0 && dy == 0) {
                        continue;
                    }
                    layout.draw(g2, x + dx, y + dy);
                }
            }
        }
    }

    // =========================================================================
    // REFLECTION - Implémentation
    // =========================================================================
    /**
     * Peint un reflet en miroir sous le texte.
     */
    private void peindreReflection(Graphics2D g2, TextLayout layout,
            int x, int y) {

        int espacement = refletEspacement;
        int transparence = refletTransparence;
        float hauteur = refletHauteur;

        Shape formeOriginale = layout.getOutline(AffineTransform.getTranslateInstance(x, y));

        float descent = layout.getDescent();
        float axeMiroir = y + descent + espacement;

        AffineTransform miroir = new AffineTransform();
        miroir.translate(0, 2.0 * axeMiroir);
        miroir.scale(1, -1);

        Shape formeReflet = miroir.createTransformedShape(formeOriginale);
        Rectangle2D bounds = formeReflet.getBounds2D();

        // Application de la hauteur de reflet
        if (hauteur < 1.0f) {
            float hauteurTexte = (float) bounds.getHeight();
            float hauteurCible = hauteurTexte * hauteur;
            float yCentre = (float) (bounds.getMinY() + bounds.getMaxY()) / 2f;
            float yMin = yCentre - hauteurCible / 2f;
            float yMax = yCentre + hauteurCible / 2f;

            // On ajuste les bornes pour le dégradé
            bounds.setRect(bounds.getMinX(), yMin, bounds.getWidth(), hauteurCible);
        }

        float yDebut = (float) bounds.getMinY();
        float yFin = (float) bounds.getMaxY();
        if (yFin <= yDebut) {
            return;
        }

        GradientPaint degrade = new GradientPaint(
                0, yDebut, new Color(0, 0, 0, transparence),
                0, yFin, new Color(0, 0, 0, 0)
        );

        g2.setPaint(degrade);
        g2.fill(formeReflet);
    }

    // =========================================================================
    // Classe interne - EffetApplique
    // =========================================================================
    /**
     * Représente un effet appliqué avec sa couleur personnalisée.
     *
     * <p>
     * Cette classe est immutable et interne à EffectView pour encapsuler la
     * logique de stockage des effets.</p>
     */
    private static final class EffetApplique {

        private final HTextEffect type;
        private final Color couleur;

        EffetApplique(HTextEffect type, Color couleur) {
            this.type = type;
            this.couleur = couleur;
        }
    }

    public enum HTextEffect {

        /**
         * Aucun effet — rendu standard de Swing.
         */
        NONE,
        /**
         * Ombre portée : le texte est dupliqué en gris semi-transparent, décalé
         * de quelques pixels vers le bas-droite.
         */
        SHADOW,
        /**
         * Contour : seul le contour des glyphes est dessiné, le remplissage
         * intérieur reste transparent.
         */
        OUTLINE,
        LIGHT,
        REFLECTION
    }

    public enum HEffectDirection {

        BAS_DROITE,
        /**
         * Ombre projetée vers le bas et vers la gauche.
         */
        BAS_GAUCHE,
        /**
         * Ombre projetée vers le haut et vers la droite.
         */
        HAUT_DROITE,
        /**
         * Ombre projetée vers le haut et vers la gauche.
         */
        HAUT_GAUCHE,
        /**
         * Ombre centrée : projetée dans les quatre directions simultanément.
         * Produit un effet "halo d'ombre" uniforme autour de chaque lettre,
         * similaire à l'option "Flou" de Word sans décalage directionnel.
         */
        CENTREE
    }

}
