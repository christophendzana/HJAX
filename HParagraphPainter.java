package htextarea.paragraph;

import htextarea.attribute.HAttributeKeys;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

/**
 * Moteur de rendu des fonctionnalités de paragraphe pour {@link htextarea.HTextArea}.
 * Lit le {@link HParagraphConfig} stocké sous {@link HAttributeKeys#PARAGRAPH_STYLE}
 * et dessine puces, numérotation, bordures, trame et marques invisibles.
 */
public class HParagraphPainter {

    private static final float TAILLE_POLICE_SYMBOLE = 12f;
    private static final float TAILLE_POLICE_MARQUE = 10f;

    private static final Color COULEUR_MARQUE = new Color(100, 120, 200, 160);
    private static final Color COULEUR_SYMBOLE = new Color(60, 65, 90);

    /**
     * Point d'entrée principal. Parcourt tous les paragraphes du document,
     * lit leur configuration et délègue le dessin.
     *
     * @param g         le contexte graphique
     * @param doc       le document stylisé
     * @param view      la vue racine
     * @param composant le JTextPane
     */
    public static void peindre(Graphics g, StyledDocument doc,
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

                Rectangle rect = calculerRectangleParagraphe(view, para);
                if (rect == null) {
                    continue;
                }

                if (config.hasTrame()) {
                    peindreTrame(g2, rect, config, composant);
                }

                if (config.hasBordure()) {
                    peindreBordure(g2, rect, config, composant);
                }

                if (config.hasListe()) {
                    int compteur = calculerCompteurNumerotation(doc, i, config);
                    peindreListe(g2, doc, view, para, config, compteur, composant);
                }

                if (config.isShowMarks()) {
                    peindreMarques(g2, doc, view, para);
                }
            }

        } finally {
            g2.dispose();
        }
    }

    /**
     * Peint la trame de fond sur toute la largeur du composant.
     */
    private static void peindreTrame(Graphics2D g2, Rectangle rect,
                                     HParagraphConfig config,
                                     JTextPane composant) {
        Insets insets = composant.getInsets();
        int x = insets.left;
        int largeur = composant.getWidth() - insets.left - insets.right;

        g2.setColor(config.getBackground());
        g2.fillRect(x, rect.y, largeur, rect.height);
    }

    /**
     * Peint les bordures selon la configuration.
     */
    private static void peindreBordure(Graphics2D g2, Rectangle rect,
                                       HParagraphConfig config,
                                       JTextPane composant) {
        HBorderConfig bc = config.getBorderConfig();
        Insets insets = composant.getInsets();
        float padding = bc.getPadding();

        float x = insets.left - padding;
        float y = rect.y - padding;
        float largeur = composant.getWidth() - insets.left - insets.right + padding * 2;
        float hauteur = rect.height + padding * 2;

        g2.setColor(bc.getCouleur());
        g2.setStroke(new BasicStroke(
                bc.getEpaisseur(),
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        switch (bc.getType()) {
            case ALL -> g2.drawRect((int) x, (int) y, (int) largeur, (int) hauteur);
            case TOP -> g2.drawLine((int) x, (int) y, (int) (x + largeur), (int) y);
            case BOTTOM -> g2.drawLine((int) x, (int) (y + hauteur), (int) (x + largeur), (int) (y + hauteur));
            case LEFT -> g2.drawLine((int) x, (int) y, (int) x, (int) (y + hauteur));
            case RIGHT -> g2.drawLine((int) (x + largeur), (int) y, (int) (x + largeur), (int) (y + hauteur));
            case TOP_BOTTOM -> {
                g2.drawLine((int) x, (int) y, (int) (x + largeur), (int) y);
                g2.drawLine((int) x, (int) (y + hauteur), (int) (x + largeur), (int) (y + hauteur));
            }
        }
    }

    /**
     * Peint le symbole de liste (puce ou numéro) devant le paragraphe.
     *
     * @param compteur le numéro à afficher pour les listes numérotées
     */
    private static void peindreListe(Graphics2D g2, StyledDocument doc,
                                     View view, Element para,
                                     HParagraphConfig config,
                                     int compteur, JTextPane composant) {
        HListConfig lc = config.getListConfig();

        Insets insets = composant.getInsets();
        float indentationTotale = lc.calculerIndentation();
        float xSymbole = insets.left
                + indentationTotale
                - HListConfig.LARGEUR_ZONE_PUCE / 2f;

        Rectangle rectPremierChar = calculerRectangle(view, para.getStartOffset());
        if (rectPremierChar == null) return;

        Font policeSymbole = composant.getFont()
                .deriveFont(TAILLE_POLICE_SYMBOLE);
        FontRenderContext frc = g2.getFontRenderContext();

        if (lc.estUnePuce()) {
            dessinerPuce(g2, lc, policeSymbole, frc,
                    xSymbole, rectPremierChar, composant);
        } else {
            dessinerNumero(g2, lc, policeSymbole, frc,
                    xSymbole, rectPremierChar, compteur);
        }
    }

    /**
     * Dessine une puce (icône ou caractère Unicode).
     */
    private static void dessinerPuce(Graphics2D g2, HListConfig lc,
                                     Font police, FontRenderContext frc,
                                     float xSymbole, Rectangle rectLigne,
                                     JTextPane composant) {
        Icon icone = lc.getSymboleIcon();

        if (icone != null) {
            int xIcone = (int) xSymbole - icone.getIconWidth() / 2;
            int yIcone = rectLigne.y + (rectLigne.height - icone.getIconHeight()) / 2;
            icone.paintIcon(composant, g2, xIcone, yIcone);
        } else {
            String symbole = lc.getSymboleParDefaut();
            TextLayout layout = new TextLayout(symbole, police, frc);
            float baseline = rectLigne.y + layout.getAscent();

            g2.setFont(police);
            g2.setColor(COULEUR_SYMBOLE);
            g2.drawString(symbole, xSymbole, baseline);
        }
    }

    /**
     * Dessine un numéro formaté aligné à droite dans la zone de puce.
     */
    private static void dessinerNumero(Graphics2D g2, HListConfig lc,
                                       Font police, FontRenderContext frc,
                                       float xSymbole, Rectangle rectLigne,
                                       int compteur) {
        String texte = lc.formaterNumero(compteur);
        TextLayout layout = new TextLayout(texte, police, frc);
        float baseline = rectLigne.y + layout.getAscent();

        float xAjuste = xSymbole - layout.getAdvance() / 2f;

        g2.setFont(police);
        g2.setColor(COULEUR_SYMBOLE);
        g2.drawString(texte, xAjuste, baseline);
    }

    /**
     * Dessine les caractères invisibles (espaces, tabulations, sauts de ligne).
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
                    case " " -> "·";
                    case "\t" -> "→";
                    case "\n" -> "¶";
                    default -> null;
                };

                if (marque == null) continue;

                Rectangle rect = calculerRectangle(view, pos);
                if (rect == null) continue;

                g2.drawString(marque, rect.x, rect.y + rect.height - 2);
            }
        } catch (BadLocationException e) {
            // ignore
        }
    }

    /**
     * Calcule le numéro à afficher pour un paragraphe numéroté,
     * en remontant dans les paragraphes précédents.
     *
     * @param doc          le document
     * @param indexCourant l'index du paragraphe courant
     * @param config       la config du paragraphe courant
     * @return le numéro à afficher
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
            Element para = racine.getElement(i);
            HParagraphConfig configPara = lireConfig(para);

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

    /**
     * Lit le {@link HParagraphConfig} stocké dans un élément paragraphe.
     *
     * @param para l'élément paragraphe
     * @return la config, ou {@code null} si absente
     */
    private static HParagraphConfig lireConfig(Element para) {
        Object valeur = para.getAttributes()
                .getAttribute(HAttributeKeys.PARAGRAPH_STYLE);
        if (valeur instanceof HParagraphConfig config) {
            return config;
        }
        return null;
    }

    /**
     * Calcule le rectangle en pixels occupé par un paragraphe entier.
     */
    private static Rectangle calculerRectangleParagraphe(View view, Element para) {
        Rectangle rectDebut = calculerRectangle(view, para.getStartOffset());
        Rectangle rectFin = calculerRectangle(
                view, Math.max(para.getStartOffset(), para.getEndOffset() - 1));

        if (rectDebut == null || rectFin == null) return null;

        int y = rectDebut.y;
        int hauteur = (rectFin.y + rectFin.height) - rectDebut.y;
        return new Rectangle(rectDebut.x, y, rectDebut.width,
                Math.max(hauteur, rectDebut.height));
    }

    /**
     * Calcule le rectangle en pixels correspondant à une position dans le document.
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