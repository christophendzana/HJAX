package htextarea.paragraph;

import htextarea.attribute.HAttributeKeys;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

/**
 * Moteur de rendu des fonctionnalités de paragraphe pour
 * {@link htextarea.HTextArea}.
 *
 * <p>
 * Ce painter travaille en coordonnées absolues du composant (0,0 = coin
 * supérieur gauche du JTextPane), sans translation préalable du Graphics2D — ce
 * qui est cohérent avec les coordonnées retournées par
 * {@code modelToView()}.</p>
 *
 * <h3>Deux passes de rendu</h3>
 * <pre>
 *   peindreArrierePlan()  ← AVANT super.paintSafely() : trame de fond
 *   super.paintSafely()   ← Swing dessine le texte
 *   peindrePremierPlan()  ← APRÈS super.paintSafely() : puces, bordures, marques
 * </pre>
 *
 * @author FIDELE
 * @version 5.0
 */
public class HParagraphPainter {

    // =========================================================================
    // Constantes visuelles
    // =========================================================================
    /**
     * Taille de la police pour les symboles de liste.
     */
    private static final float TAILLE_POLICE_SYMBOLE = 12f;

    /**
     * Taille de la police pour les marques invisibles.
     */
    private static final float TAILLE_POLICE_MARQUE = 10f;

    /**
     * Couleur des marques invisibles.
     */
    private static final Color COULEUR_MARQUE = new Color(100, 120, 200, 160);

    /**
     * Couleur par défaut des puces et numéros.
     */
    private static final Color COULEUR_SYMBOLE = new Color(60, 65, 90);

    /**
     * Espace vertical ajouté au-dessus et en dessous du texte pour les bordures
     * et la trame. Correspond à l'espace visuel entre le texte et le bord du
     * cadre — comme le padding interne dans Word.
     */
    private static final int MARGE_VERTICALE = 4;

    // =========================================================================
    // PASSE 1 — Arrière-plan (appelée AVANT super.paintSafely)
    // =========================================================================
    /**
     * Dessine la trame de fond des paragraphes, avant que Swing dessine le
     * texte.
     */
    public static void peindreArrierePlan(Graphics g, StyledDocument doc,
            View view, JTextPane composant) {
        if (g == null || doc == null || view == null || composant == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Element racine = doc.getDefaultRootElement();
            int nbParagraphes = racine.getElementCount();

            for (int i = 0; i < nbParagraphes; i++) {
                Element para = racine.getElement(i);
                HParagraphConfig config = lireConfig(para);

                if (config == null || !config.hasTrame()) {
                    continue;
                }

                // On calcule le rectangle ÉTENDU — avec les marges verticales
                // pour que la trame couvre toute la zone visuelle du paragraphe
                Rectangle rect = calculerRectangleEtendu(view, para);
                if (rect == null) {
                    continue;
                }

                peindreTrame(g2, rect, config, composant);
            }
        } finally {
            g2.dispose();
        }
    }

    // =========================================================================
    // PASSE 2 — Premier plan (appelée APRÈS super.paintSafely)
    // =========================================================================
    /**
     * Dessine les puces, bordures et marques invisibles, après que Swing a
     * dessiné le texte.
     */
    public static void peindrePremierPlan(Graphics g, StyledDocument doc,
            View view, JTextPane composant) {
        if (g == null || doc == null || view == null || composant == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            Element racine = doc.getDefaultRootElement();
            int nbParagraphes = racine.getElementCount();

            for (int i = 0; i < nbParagraphes; i++) {
                Element para = racine.getElement(i);
                HParagraphConfig config = lireConfig(para);

                if (config == null || config.estNatifSwingSeul()) {
                    continue;
                }

                if (config.hasBordure()) {
                    // Les bordures utilisent le rectangle étendu —
                    // même zone que la trame pour qu'elles coïncident visuellement
                    Rectangle rect = calculerRectangleEtendu(view, para);
                    if (rect != null) {
                        peindreBordure(g2, rect, composant, config);
                    }
                }

                if (config.hasListe()) {
                    int compteur = calculerCompteurNumerotation(doc, i, config);
                    peindreListe(g2, view, para, config, compteur, composant);
                }

                if (config.isShowMarks()) {
                    peindreMarques(g2, doc, view, para);
                }
            }
        } finally {
            g2.dispose();
        }
    }

    // =========================================================================
    // Trame de fond
    // =========================================================================
    /**
     * Peint la trame de fond sur toute la largeur du composant.     
     */
    private static void peindreTrame(Graphics2D g2, Rectangle rect,
            HParagraphConfig config,
            JTextPane composant) {
        g2.setColor(config.getBackground());
        // Toute la largeur du composant, rectangle étendu en hauteur
        g2.fillRect(0, rect.y, composant.getWidth(), rect.height);
    }

    // =========================================================================
    // Bordures de paragraphe
    // =========================================================================
    /**
     * Peint les traits de bordure autour du paragraphe.
     *     
     */
    private static void peindreBordure(Graphics2D g2, Rectangle rect,
            JTextPane composant, HParagraphConfig config) {
        HBorderConfig bc = config.getBorderConfig();
        float ep = bc.getEpaisseur();
        float padding = bc.getPadding();
        Insets ins = composant.getInsets();

        // Largeur de la zone de texte (sans les insets)
        float largeurTexte = composant.getWidth() - ins.left - ins.right;

        // Bordure gauche : à -padding depuis le bord gauche du texte
        // Bordure droite : à largeurTexte + padding depuis le bord gauche du texte
        float x = -padding + ep / 2f;
        float largeur = largeurTexte + padding * 2 - ep;

        // Bordure haut/bas : depuis le rect du paragraphe (inchangé — correct)
        float y = rect.y + ep / 2f;
        float hauteur = rect.height - ep;

        g2.setColor(bc.getCouleur());
        g2.setStroke(new BasicStroke(ep, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int ix = (int) x;
        int iy = (int) y;
        int iw = (int) largeur;
        int ih = (int) hauteur;

        switch (bc.getType()) {
            case ALL ->
                g2.drawRect(ix, iy, iw, ih);
            case TOP ->
                g2.drawLine(ix, iy, ix + iw, iy);
            case BOTTOM ->
                g2.drawLine(ix, iy + ih, ix + iw, iy + ih);
            case LEFT ->
                g2.drawLine(ix, iy, ix, iy + ih);
            case RIGHT ->
                g2.drawLine(ix + iw, iy, ix + iw, iy + ih);
            case TOP_BOTTOM -> {
                g2.drawLine(ix, iy, ix + iw, iy);
                g2.drawLine(ix, iy + ih, ix + iw, iy + ih);
            }
        }
    }

    // =========================================================================
    // Puces et numérotation
    // =========================================================================
    /**
     * Peint le symbole de liste (puce ou numéro) devant le paragraphe.
     *    
     */
    private static void peindreListe(Graphics2D g2, View view,
            Element para, HParagraphConfig config,
            int compteur, JTextPane composant) {
        HListConfig lc = config.getListConfig();

        // Trouver le premier caractère visible non blanc du paragraphe
        // pour obtenir la bonne position Y 
        int offsetPremierVisible = trouverPremierCaractereVisible(para);
        if (offsetPremierVisible < 0) {
            return;
        }

        Rectangle rectPremierChar = calculerRectangle(view, offsetPremierVisible);
        if (rectPremierChar == null) {
            return;
        }

        // Position X du symbole : centré dans la zone libre à gauche du texte        
        float xCentreZone = rectPremierChar.x / 2f;

        Font police = composant.getFont().deriveFont(TAILLE_POLICE_SYMBOLE);
        FontRenderContext frc = g2.getFontRenderContext();

        if (lc.estUnePuce()) {
            dessinerPuce(g2, lc, police, frc, xCentreZone, rectPremierChar, composant);
        } else {
            dessinerNumero(g2, lc, police, frc, xCentreZone, rectPremierChar, compteur);
        }
    }

    /**
     * Cherche l'offset du premier caractère non-whitespace dans un paragraphe.
     *
     * <p>
     * On parcourt les caractères depuis {@code startOffset} jusqu'à
     * {@code endOffset}. On s'arrête dès qu'on trouve un caractère qui n'est ni
     * un espace, ni un \n, ni une tabulation.</p>
     *
     * <p>
     * Si le paragraphe ne contient que des whitespaces (ligne vide), on
     * retourne {@code startOffset + 1} comme position de repli.</p>
     *
     * @param para l'élément paragraphe
     * @return l'offset du premier caractère visible, ou -1 si le paragraphe est
     * vide
     */
    private static int trouverPremierCaractereVisible(Element para) {
        int debut = para.getStartOffset();
        int fin = para.getEndOffset();

        if (fin <= debut) {
            return -1;
        }

        // Récupérer le document depuis l'élément
        Document doc = para.getDocument();
        if (doc == null) {
            return -1;
        }

        try {
            for (int pos = debut; pos < fin; pos++) {
                String c = doc.getText(pos, 1);
                // On cherche le premier caractère qui n'est pas un séparateur
                if (!c.equals(" ") && !c.equals("\n") && !c.equals("\t") && !c.equals("\r")) {
                    return pos;
                }
            }
            // Paragraphe vide — position de repli
            return (fin > debut + 1) ? debut + 1 : debut;
        } catch (BadLocationException e) {
            return debut;
        }
    }

    /**
     * Dessine une puce — icône Swing si disponible, sinon caractère Unicode.
     */
    private static void dessinerPuce(Graphics2D g2, HListConfig lc,
            Font police, FontRenderContext frc,
            float xCentreZone, Rectangle rectLigne,
            JTextPane composant) {
        Icon icone = lc.getSymboleIcon();

        if (icone != null) {
            // Icône : centrée horizontalement dans la zone, alignée verticalement
            int xIcone = (int) xCentreZone - icone.getIconWidth() / 2;
            int yIcone = rectLigne.y + (rectLigne.height - icone.getIconHeight()) / 2;
            icone.paintIcon(composant, g2, xIcone, yIcone);

        } else {
            // Texte Unicode : aligné sur la baseline de la première ligne
            String symbole = lc.getSymboleParDefaut();
            TextLayout layout = new TextLayout(symbole, police, frc);

            // Baseline = haut du rectangle + ascent de la police
            // C'est la même formule que Swing utilise pour aligner le texte
            float baseline = rectLigne.y + layout.getAscent();

            // Centrer horizontalement dans la zone libre
            float xCentre = xCentreZone - layout.getAdvance() / 2f;

            g2.setFont(police);
            g2.setColor(COULEUR_SYMBOLE);
            g2.drawString(symbole, xCentre, baseline);
        }
    }

    /**
     * Dessine un numéro formaté (1. a) A. I. etc.).
     */
    private static void dessinerNumero(Graphics2D g2, HListConfig lc,
            Font police, FontRenderContext frc,
            float xCentreZone, Rectangle rectLigne,
            int compteur) {
        String texte = lc.formaterNumero(compteur);
        TextLayout layout = new TextLayout(texte, police, frc);
        float baseline = rectLigne.y + layout.getAscent();
        float xCentre = xCentreZone - layout.getAdvance() / 2f;

        g2.setFont(police);
        g2.setColor(COULEUR_SYMBOLE);
        g2.drawString(texte, xCentre, baseline);
    }

    // =========================================================================
    // Marques invisibles
    // =========================================================================
    /**
     * Dessine les caractères invisibles : espace → · , tabulation → → , \n → ¶
     */
    private static void peindreMarques(Graphics2D g2, StyledDocument doc,
            View view, Element para) {
        Font policeMarque = new Font("Dialog", Font.PLAIN, (int) TAILLE_POLICE_MARQUE);
        g2.setFont(policeMarque);
        g2.setColor(COULEUR_MARQUE);

        int debut = para.getStartOffset();
        int fin = para.getEndOffset();

        try {
            for (int pos = debut; pos < fin; pos++) {
                String caractere = doc.getText(pos, 1);
                String marque = switch (caractere) {
                    case " " ->
                        "·";
                    case "\t" ->
                        "→";
                    case "\n" ->
                        "¶";
                    default ->
                        null;
                };
                if (marque == null) {
                    continue;
                }

                Rectangle rect = calculerRectangle(view, pos);
                if (rect == null) {
                    continue;
                }

                g2.drawString(marque, rect.x, rect.y + rect.height - 2);
            }
        } catch (BadLocationException e) {
            // Position invalide — ignorée silencieusement
        }
    }

    // =========================================================================
    // Calcul du compteur de numérotation
    // =========================================================================
    /**
     * Calcule le numéro à afficher pour un paragraphe numéroté. Remonte dans
     * les paragraphes précédents tant qu'ils ont le même type et le même
     * niveau.
     */
    private static int calculerCompteurNumerotation(StyledDocument doc,
            int indexCourant,
            HParagraphConfig config) {
        HListConfig lcCourant = config.getListConfig();
        if (lcCourant == null || lcCourant.estUnePuce()) {
            return 1;
        }

        Element racine = doc.getDefaultRootElement();
        int compteur = 1;

        for (int i = indexCourant - 1; i >= 0; i--) {
            HParagraphConfig configPara = lireConfig(racine.getElement(i));
            if (configPara == null || !configPara.hasListe()) {
                break;
            }

            HListConfig lc = configPara.getListConfig();
            if (lc.getType() != lcCourant.getType()
                    || lc.getNiveau() != lcCourant.getNiveau()) {
                break;
            }
            if (lc.isRestart() && i < indexCourant - 1) {
                break;
            }

            compteur++;
        }

        return lcCourant.getDebut() + compteur - 1;
    }

    // =========================================================================
    // Utilitaires privés — calcul des rectangles
    // =========================================================================
    /**
     * Lit le {@link HParagraphConfig} depuis les attributs d'un paragraphe.
     * Retourne {@code null} si aucune config n'est définie sur ce paragraphe.
     */
    private static HParagraphConfig lireConfig(Element para) {
        Object valeur = para.getAttributes()
                .getAttribute(HAttributeKeys.PARAGRAPH_STYLE);
        return (valeur instanceof HParagraphConfig config) ? config : null;
    }

    /**
     * Calcule le rectangle étendu d'un paragraphe, avec une marge verticale
     * ajoutée au-dessus et en dessous.
     *
     * <h3>Pourquoi "étendu" ?</h3>
     * <p>
     * {@code modelToView()} retourne le rectangle *exact* du texte — il colle
     * aux caractères. Pour que la trame et les bordures aient l'air
     * d'envelopper le paragraphe avec un peu d'espace (comme dans Word), on
     * ajoute {@link #MARGE_VERTICALE} pixels en haut et en bas.</p>
     *
     * <p>
     * La même extension est appliquée à la trame et aux bordures, donc les deux
     * coïncident parfaitement visuellement.</p>
     *
     * @param view la vue racine
     * @param para l'élément paragraphe
     * @return le rectangle étendu, ou {@code null} en cas d'erreur
     */
    private static Rectangle calculerRectangleEtendu(View view, Element para) {
        int startOffset = para.getStartOffset();
        int endOffset = para.getEndOffset();

        // On cherche le premier caractère réel pour le bord supérieur
        int offsetDebut = (endOffset > startOffset + 1)
                ? startOffset + 1
                : startOffset;

        Rectangle rectDebut = calculerRectangle(view, offsetDebut);
        Rectangle rectFin = calculerRectangle(view, Math.max(offsetDebut, endOffset - 1));

        if (rectDebut == null) {
            return null;
        }
        if (rectFin == null) {
            rectFin = rectDebut;
        }

        // Rectangle brut qui couvre exactement le texte
        int yBrut = rectDebut.y;
        int hauteurBrut = (rectFin.y + rectFin.height) - rectDebut.y;
        hauteurBrut = Math.max(hauteurBrut, rectDebut.height);

        // Rectangle étendu : on ajoute la marge verticale en haut et en bas
        // pour créer l'espace entre le texte et les bords (trame et bordures)
        return new Rectangle(
                rectDebut.x,
                yBrut - MARGE_VERTICALE,
                rectDebut.width,
                hauteurBrut + MARGE_VERTICALE * 2
        );
    }

    /**
     * Convertit une position dans le document (index de caractère) en rectangle
     * en pixels via {@code view.modelToView()}.
     *
     * <p>
     * Les coordonnées retournées sont absolues par rapport au composant (0,0 =
     * coin supérieur gauche du JTextPane).</p>
     *
     * @param view la vue racine
     * @param pos la position dans le document
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
}
