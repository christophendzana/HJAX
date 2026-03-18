/*
 * HBasicRibbonTabbedPaneUI.java
 *
 * UI Delegate exclusif pour HRibbonTabbedPane.
 * Toutes les couleurs sont lues depuis HRibbonTabsTheme via getActiveTheme()
 * qui remonte jusqu'au HRibbonTabs parent dans la hiérarchie Swing.
 *
 * FONCTIONNALITÉS :
 * - Fond des onglets avec effet de survol animé
 * - Fond de la zone de contenu cohérent avec le thème
 * - Texte des onglets avec couleur selon sélection
 * - Coins arrondis configurables depuis HRibbonTabbedPane
 * - Aucune dépendance à HTabbedPane ou HTabbedPaneStyle
 *
 * COULEURS DE FALLBACK :
 * Si aucun thème n'est défini, des couleurs neutres claires sont utilisées
 * pour garantir un rendu correct même sans HRibbonTabs parent.
 */
package rubban;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * HBasicRibbonTabbedPaneUI — UI delegate moderne pour HRibbonTabbedPane.
 *
 * Lit ses couleurs depuis HRibbonTabsTheme. Si aucun thème n'est disponible,
 * des couleurs neutres de fallback sont utilisées.
 *
 * @author FIDELE
 * @version 1.0
 */
public class HBasicRibbonTabbedPaneUI extends BasicTabbedPaneUI {

    // =========================================================================
    // ANIMATIONS DE SURVOL
    // =========================================================================
    /**
     * Progression de l'animation de survol par onglet. Clé : index de l'onglet.
     * Valeur : progression entre 0.0 et 1.0.
     */
    private final Map<Integer, Float> hoverProgressMap = new HashMap<>();

    /**
     * Timers actifs pour les animations de survol. Clé : index de l'onglet.
     * Valeur : Timer gérant l'animation.
     */
    private final Map<Integer, Timer> hoverTimerMap = new HashMap<>();

    /**
     * Index de l'onglet actuellement survolé. -1 si aucun.
     */
    private int hoveredTab = -1;

    // =========================================================================
    // CONSTANTES D'ANIMATION
    // =========================================================================
    /**
     * Durée d'une animation de survol en millisecondes.
     */
    private static final int ANIMATION_DURATION = 200;

    /**
     * Délai entre les frames — 60 FPS.
     */
    private static final int FRAME_DELAY = 1000 / 60;

    // =========================================================================
    // COULEURS DE FALLBACK
    // Ces couleurs sont utilisées si aucun HRibbonTabsTheme n'est disponible
    // =========================================================================
    private static final Color FALLBACK_TAB_BG = new Color(233, 236, 239);
    private static final Color FALLBACK_TAB_SELECTED_BG = Color.WHITE;
    private static final Color FALLBACK_TAB_HOVER_BG = new Color(241, 243, 245);
    private static final Color FALLBACK_TEXT_COLOR = new Color(73, 80, 87);
    private static final Color FALLBACK_TEXT_SELECTED = new Color(33, 37, 41);
    private static final Color FALLBACK_CONTENT_BG = Color.WHITE;
    private static final Color FALLBACK_CONTENT_BORDER = new Color(222, 226, 230);
    private static final Color FALLBACK_TAB_BAR_BG = new Color(248, 249, 250);

    // =========================================================================
    // INSTALLATION
    // =========================================================================
    /**
     * Installe l'UI delegate sur le composant. Enregistre les listeners de
     * souris pour les animations de survol.
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        // Listener de mouvement — détecte l'onglet survolé
        c.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e, c);
            }
        });

        // Listener de sortie — déclenche l'animation de sortie
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredTab != -1) {
                    animateHover(hoveredTab, false, c);
                    hoveredTab = -1;
                }
            }
        });
    }

    // =========================================================================
    // GESTION DU SURVOL
    // =========================================================================
    /**
     * Détecte l'onglet sous le curseur et lance les animations appropriées.
     *
     * @param e événement souris
     * @param c le composant TabbedPane
     */
    private void handleMouseMoved(MouseEvent e, JComponent c) {
        int tabIndex = tabForCoordinate((JTabbedPane) c, e.getX(), e.getY());

        if (tabIndex != hoveredTab) {
            // Animer la sortie de l'ancien onglet survolé
            if (hoveredTab != -1) {
                animateHover(hoveredTab, false, c);
            }

            hoveredTab = tabIndex;

            // Animer l'entrée sur le nouvel onglet
            if (hoveredTab != -1) {
                animateHover(hoveredTab, true, c);
            }
        }
    }

    /**
     * Lance une animation de survol sur un onglet. Si les animations sont
     * désactivées dans HRibbonTabbedPane, l'état final est appliqué directement
     * sans transition.
     *
     * @param tabIndex index de l'onglet à animer
     * @param in true pour l'entrée, false pour la sortie
     * @param c le composant à redessiner
     */
    private void animateHover(int tabIndex, boolean in, JComponent c) {
        if (tabIndex < 0 || tabIndex >= tabPane.getTabCount()) {
            return;
        }

        // Vérifier si les animations sont activées dans HRibbonTabbedPane
        boolean animEnabled = true;
        if (tabPane instanceof HRibbonTabbedPane) {
            animEnabled = ((HRibbonTabbedPane) tabPane).isAnimationsEnabled();
        }

        if (!animEnabled) {
            // Appliquer l'état final directement sans animation
            hoverProgressMap.put(tabIndex, in ? 1f : 0f);
            c.repaint();
            return;
        }

        // Arrêter un timer existant pour cet onglet
        Timer existing = hoverTimerMap.get(tabIndex);
        if (existing != null) {
            existing.stop();
        }

        float startProgress = hoverProgressMap.getOrDefault(tabIndex, in ? 0f : 1f);
        long startTime = System.currentTimeMillis();

        Timer timer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);

                // Interpolation depuis startProgress vers la cible
                float current = in
                        ? startProgress + (1f - startProgress) * progress
                        : startProgress - startProgress * progress;

                hoverProgressMap.put(tabIndex, current);
                c.repaint();

                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                    hoverTimerMap.remove(tabIndex);
                }
            }
        });

        hoverTimerMap.put(tabIndex, timer);
        timer.start();
    }

    // =========================================================================
    // ACCÈS AU THÈME ACTIF
    // =========================================================================
    /**
     * Remonte la hiérarchie de composants pour trouver le HRibbonTabs parent et
     * retourner son thème actif.
     *
     * Retourne null si aucun thème n'est défini ou si le composant n'est pas
     * dans un HRibbonTabs — les couleurs de fallback seront alors utilisées.
     *
     * @return le thème actif, ou null
     */
    private HRibbonTabsTheme getActiveTheme() {
        if (tabPane == null) {
            return null;
        }
        // Remonter jusqu'au HRibbonTabs dans la hiérarchie Swing
        Container parent = tabPane.getParent();
        while (parent != null) {
            if (parent instanceof HRibbonTabs) {
                return ((HRibbonTabs) parent).getTheme();
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Retourne le cornerRadius depuis HRibbonTabbedPane. Valeur de fallback :
     * 6px.
     *
     * @return rayon des coins arrondis en pixels
     */
    private int getCornerRadius() {
        if (tabPane instanceof HRibbonTabbedPane) {
            return ((HRibbonTabbedPane) tabPane).getCornerRadius();
        }
        return 6;
    }

    // =========================================================================
    // RENDU — FOND DES ONGLETS
    // =========================================================================
    /**
     * Dessine le fond de chaque onglet avec coins arrondis et effet de survol.
     *
     * Couleurs lues depuis le thème actif — fallback neutres si absent. L'effet
     * de survol est une interpolation animée entre tabBackground et
     * tabHoverBackground.
     */
    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        HRibbonTabsTheme theme = getActiveTheme();
        int radius = getCornerRadius();

        // Déterminer la couleur de fond selon l'état
        Color bgColor;
        if (isSelected) {
            // Onglet sélectionné — couleur distincte depuis le thème
            bgColor = (theme != null)
                    ? theme.getTabSelectedBackground()
                    : FALLBACK_TAB_SELECTED_BG;
        } else {
            // Onglet normal — interpolation avec le hover
            Color normalBg = (theme != null) ? theme.getTabBackground() : FALLBACK_TAB_BG;
            Color hoverBg = (theme != null) ? theme.getTabHoverBackground() : FALLBACK_TAB_HOVER_BG;
            float progress = hoverProgressMap.getOrDefault(tabIndex, 0f);
            bgColor = interpolateColor(normalBg, hoverBg, progress);
        }

        g2.setColor(bgColor);

        if (tabPlacement == TOP) {
            // Onglets en haut : coins arrondis uniquement en haut
            RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                    x + 2, y + 2, w - 4, h - 2, radius, radius
            );
            g2.fill(roundRect);
        } else if (tabPlacement == BOTTOM) {
            // Onglets en bas : coins arrondis uniquement en bas
            RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                    x + 2, y - radius, w - 4, h + radius, radius, radius
            );
            g2.fill(roundRect);
        } else {
            // Onglets gauche/droite : rectangles simples
            g2.fillRect(x, y, w, h);
        }

        g2.dispose();
    }

    // =========================================================================
    // RENDU — BORDURE DES ONGLETS
    // =========================================================================
    /**
     * Désactive les bordures des onglets — style minimaliste moderne. La
     * séparation visuelle est assurée par les couleurs de fond.
     */
    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w, int h, boolean isSelected) {
        // Intentionnellement vide — pas de bordure sur les onglets
    }

    // =========================================================================
    // RENDU — ZONE DE CONTENU
    // =========================================================================
    /**
     * Dessine le fond et la bordure de la zone de contenu du TabbedPane.
     *
     * La couleur de fond est lue depuis getContentBackground() du thème actif
     * pour créer une continuité visuelle avec l'onglet sélectionné.
     */
    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        HRibbonTabsTheme theme = getActiveTheme();
        int radius = getCornerRadius();
        int width = tabPane.getWidth();
        int height = tabPane.getHeight();

        // Calcul de la zone de contenu — sous la barre d'onglets
        Insets insets = tabPane.getInsets();
        int x = insets.left;
        int y = insets.top;
        int w = width - insets.left - insets.right;
        int h = height - insets.top - insets.bottom;

        // Ajuster selon le placement des onglets
        switch (tabPlacement) {
            case TOP:
                // Le contenu commence sous la barre d'onglets
                int tabAreaH = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                y += tabAreaH;
                h -= tabAreaH;
                break;
            case BOTTOM:
                h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                break;
            case LEFT:
                int tabAreaW = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                x += tabAreaW;
                w -= tabAreaW;
                break;
            case RIGHT:
                w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                break;
        }

        // Fond du contenu depuis le thème — fallback blanc
        Color contentBg = (theme != null)
                ? theme.getContentBackground()
                : FALLBACK_CONTENT_BG;

        g2.setColor(contentBg);
        RoundRectangle2D contentRect = new RoundRectangle2D.Float(x, y, w, h, radius, radius);
        g2.fill(contentRect);

        // Bordure subtile autour du contenu
//        Color borderColor = (theme != null)
//                ? theme.getContentBorderColor()
//                : FALLBACK_CONTENT_BORDER;
//
//        g2.setColor(borderColor);
//        g2.setStroke(new BasicStroke(1f));
//        g2.draw(contentRect);

        g2.dispose();
    }

    // =========================================================================
    // RENDU — TEXTE DES ONGLETS
    // =========================================================================
    /**
     * Dessine le texte des onglets avec anti-crénelage. Texte en gras pour
     * l'onglet sélectionné. Couleurs lues depuis le thème actif.
     */
    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
            int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        HRibbonTabsTheme theme = getActiveTheme();

        // Couleur du texte depuis le thème — fallback neutre
        Color textColor = isSelected
                ? (theme != null ? theme.getTabSelectedTextColor() : FALLBACK_TEXT_SELECTED)
                : (theme != null ? theme.getTabTextColor() : FALLBACK_TEXT_COLOR);

        g2.setColor(textColor);

        // Police en gras pour l'onglet sélectionné
        Font tabFont = isSelected ? font.deriveFont(Font.BOLD) : font;
        g2.setFont(tabFont);

        FontMetrics fm = g2.getFontMetrics();
        int textX = textRect.x;
        int textY = textRect.y + fm.getAscent();

        g2.drawString(title, textX, textY);
        g2.dispose();
    }

    // =========================================================================
    // RENDU — INDICATEUR DE FOCUS
    // =========================================================================
    /**
     * Désactive l'indicateur de focus standard de BasicTabbedPaneUI. La
     * sélection est indiquée par la couleur de fond de l'onglet.
     */
    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
            int tabIndex, Rectangle iconRect, Rectangle textRect,
            boolean isSelected) {
        // Intentionnellement vide — la couleur de fond suffit à indiquer la sélection
    }

    // =========================================================================
    // RENDU — FOND GÉNÉRAL DU TABBEDPANE
    // =========================================================================
    /**
     * Dessine le fond général de la barre d'onglets. Couleur lue depuis
     * getTabBarBackground() du thème actif.
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        HRibbonTabsTheme theme = getActiveTheme();

        // Peindre le fond de la barre d'onglets
        Color barBg = (theme != null)
                ? theme.getTabBarBackground()
                : FALLBACK_TAB_BAR_BG;

        g.setColor(barBg);
        g.fillRect(0, 0, c.getWidth(), c.getHeight());

        // Déléguer le reste du rendu à BasicTabbedPaneUI
        super.paint(g, c);
    }

    // =========================================================================
    // DIMENSIONS
    // =========================================================================
    /**
     * Espace entre la barre d'onglets et la zone de contenu. 5px en haut pour
     * aérer visuellement.
     */
    @Override
    protected Insets getContentBorderInsets(int tabPlacement) {
        // 5px d'espace entre la barre d'onglets et le Ribbon
        return new Insets(5, 0, 0, 0);
    }

    /**
     * Hauteur des onglets — 10px de plus que la valeur standard.
     */
    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 10;
    }

    /**
     * Largeur des onglets — 20px de plus que la valeur standard.
     */
    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 20;
    }

    // =========================================================================
    // NETTOYAGE
    // =========================================================================
    /**
     * Arrête tous les timers d'animation et vide les maps lors du démontage.
     */
    @Override
    public void uninstallUI(JComponent c) {
        // Arrêter tous les timers de survol
        for (Timer t : hoverTimerMap.values()) {
            if (t != null) {
                t.stop();
            }
        }
        hoverTimerMap.clear();
        hoverProgressMap.clear();

        super.uninstallUI(c);
    }

    // =========================================================================
    // MÉTHODE UTILITAIRE
    // =========================================================================
    /**
     * Interpole linéairement entre deux couleurs selon une progression.
     *
     * @param c1 couleur de départ
     * @param c2 couleur d'arrivée
     * @param progress progression entre 0.0 et 1.0
     * @return la couleur interpolée
     */
    private Color interpolateColor(Color c1, Color c2, float progress) {
        progress = Math.max(0f, Math.min(1f, progress));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * progress);
        return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b)),
                Math.max(0, Math.min(255, a))
        );
    }
}
