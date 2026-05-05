/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hsupertable;

import java.awt.Color;
import java.awt.Font;

/**
 * HSuperTableStyle - Énumération des styles prédéfinis pour le composant
 * HTable.
 *
 * <p>
 * Chaque style définit une palette de couleurs cohérente pour :</p>
 * <ul>
 * <li>L'en-tête du tableau (fond et texte)</li>
 * <li>Les cellules (fond normal, fond alterné, texte)</li>
 * <li>Les états visuels (sélection, survol, surbrillance, focus)</li>
 * <li>La grille et les bordures</li>
 * </ul>
 *
 * @author FIDELE
 * @version 1.0
 * @see HTable
 */
public class HSuperTableStyle {

    // =========================================================================
    // CHAMPS — tous en lecture seule après construction
    // =========================================================================
    // -- En-tête --
    private final Color headerBackground;
    private final Color headerForeground;
    private final Font headerFont;

    // -- Cellules --
    private final Color cellBackground;
    private final Color cellAlternateBackground;  // lignes/colonnes alternées
    private final Color cellForeground;
    private final Font cellFont;

    // -- États interactifs --
    private final Color hoverBackground;
    private final Color highlightBackground;
    private final Color selectionBackground;
    private final Color focusBorderColor;

    // -- Options de style (ligne totale, 1ère/dernière colonne) --
    private final Color totalRowBackground;
    private final Color totalRowForeground;
    private final Color firstColumnBackground;
    private final Color lastColumnBackground;

    // -- Grille --
    private final Color gridColor;

    // =========================================================================
    // CONSTRUCTEUR PRIVÉ — on passe par le Builder
    // =========================================================================
    private HSuperTableStyle(Builder b) {
        this.headerBackground = b.headerBackground;
        this.headerForeground = b.headerForeground;
        this.headerFont = b.headerFont;
        this.cellBackground = b.cellBackground;
        this.cellAlternateBackground = b.cellAlternateBackground;
        this.cellForeground = b.cellForeground;
        this.cellFont = b.cellFont;
        this.hoverBackground = b.hoverBackground;
        this.highlightBackground = b.highlightBackground;
        this.selectionBackground = b.selectionBackground;
        this.focusBorderColor = b.focusBorderColor;
        this.totalRowBackground = b.totalRowBackground;
        this.totalRowForeground = b.totalRowForeground;
        this.firstColumnBackground = b.firstColumnBackground;
        this.lastColumnBackground = b.lastColumnBackground;
        this.gridColor = b.gridColor;
    }

    // =========================================================================
    // GETTERS
    // =========================================================================
    public Color getHeaderBackground() {
        return headerBackground;
    }

    public Color getHeaderForeground() {
        return headerForeground;
    }

    public Font getHeaderFont() {
        return headerFont;
    }

    public Color getCellBackground() {
        return cellBackground;
    }

    public Color getCellAlternateBackground() {
        return cellAlternateBackground;
    }

    public Color getCellForeground() {
        return cellForeground;
    }

    public Font getCellFont() {
        return cellFont;
    }

    public Color getHoverBackground() {
        return hoverBackground;
    }

    public Color getHighlightBackground() {
        return highlightBackground;
    }

    public Color getSelectionBackground() {
        return selectionBackground;
    }

    public Color getFocusBorderColor() {
        return focusBorderColor;
    }

    public Color getTotalRowBackground() {
        return totalRowBackground;
    }

    public Color getTotalRowForeground() {
        return totalRowForeground;
    }

    public Color getFirstColumnBackground() {
        return firstColumnBackground;
    }

    public Color getLastColumnBackground() {
        return lastColumnBackground;
    }

    public Color getGridColor() {
        return gridColor;
    }

    // =========================================================================
    // BUILDER
    // Permet de créer des styles custom sans passer par un constructeur à 17
    // paramètres illisible. Chaque champ a une valeur par défaut sensée.
    // =========================================================================
    public static class Builder {

        // Valeurs par défaut (style PRIMARY)
        private Color headerBackground = new Color(13, 110, 253);
        private Color headerForeground = Color.WHITE;
        private Font headerFont = new Font("Segoe UI", Font.BOLD, 13);
        private Color cellBackground = Color.WHITE;
        private Color cellAlternateBackground = new Color(245, 248, 255);
        private Color cellForeground = new Color(33, 37, 41);
        private Font cellFont = new Font("Segoe UI", Font.PLAIN, 13);
        private Color hoverBackground = new Color(13, 110, 253, 20);
        private Color highlightBackground = new Color(13, 110, 253, 40);
        private Color selectionBackground = new Color(13, 110, 253, 30);
        private Color focusBorderColor = new Color(13, 110, 253);
        private Color totalRowBackground = new Color(13, 110, 253, 50);
        private Color totalRowForeground = new Color(33, 37, 41);
        private Color firstColumnBackground = new Color(13, 110, 253, 25);
        private Color lastColumnBackground = new Color(13, 110, 253, 25);
        private Color gridColor = new Color(222, 226, 230);

        public Builder headerBackground(Color c) {
            this.headerBackground = c;
            return this;
        }

        public Builder headerForeground(Color c) {
            this.headerForeground = c;
            return this;
        }

        public Builder headerFont(Font f) {
            this.headerFont = f;
            return this;
        }

        public Builder cellBackground(Color c) {
            this.cellBackground = c;
            return this;
        }

        public Builder cellAlternateBackground(Color c) {
            this.cellAlternateBackground = c;
            return this;
        }

        public Builder cellForeground(Color c) {
            this.cellForeground = c;
            return this;
        }

        public Builder cellFont(Font f) {
            this.cellFont = f;
            return this;
        }

        public Builder hoverBackground(Color c) {
            this.hoverBackground = c;
            return this;
        }

        public Builder highlightBackground(Color c) {
            this.highlightBackground = c;
            return this;
        }

        public Builder selectionBackground(Color c) {
            this.selectionBackground = c;
            return this;
        }

        public Builder focusBorderColor(Color c) {
            this.focusBorderColor = c;
            return this;
        }

        public Builder totalRowBackground(Color c) {
            this.totalRowBackground = c;
            return this;
        }

        public Builder totalRowForeground(Color c) {
            this.totalRowForeground = c;
            return this;
        }

        public Builder firstColumnBackground(Color c) {
            this.firstColumnBackground = c;
            return this;
        }

        public Builder lastColumnBackground(Color c) {
            this.lastColumnBackground = c;
            return this;
        }

        public Builder gridColor(Color c) {
            this.gridColor = c;
            return this;
        }

        public HSuperTableStyle build() {
            return new HSuperTableStyle(this);
        }
    }

    // =========================================================================
    // GALERIE DE STYLES PRÉDÉFINIS
    //
    // Organisation : Clairs (PLAIN_*) → Moyens (couleurs saturées) → Sombres (DARK_*)
    // Chaque famille couvre les mêmes teintes que Word : bleu, vert, rouge,
    // orange, violet, gris, cyan.
    // =========================================================================
    // ── CLAIRS ────────────────────────────────────────────────────────────────
    /**
     * Style neutre sans décorations — tableau minimaliste, juste une grille
     * légère. Idéal pour des tableaux de données brutes sans mise en forme.
     */
    public static final HSuperTableStyle PLAIN = new Builder()
            .headerBackground(new Color(248, 249, 250))
            .headerForeground(new Color(33, 37, 41))
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(248, 249, 250))
            .gridColor(new Color(206, 212, 218))
            .hoverBackground(new Color(0, 0, 0, 12))
            .highlightBackground(new Color(0, 0, 0, 20))
            .selectionBackground(new Color(0, 0, 0, 15))
            .focusBorderColor(new Color(108, 117, 125))
            .totalRowBackground(new Color(233, 236, 239))
            .totalRowForeground(new Color(33, 37, 41))
            .firstColumnBackground(new Color(233, 236, 239))
            .lastColumnBackground(new Color(233, 236, 239))
            .build();

    /**
     * Style clair bleu — en-tête discret, alternance bleu très pâle. Le plus
     * polyvalent pour des applications professionnelles.
     */
    public static final HSuperTableStyle PLAIN_BLUE = new Builder()
            .headerBackground(new Color(219, 234, 254))
            .headerForeground(new Color(30, 64, 175))
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(239, 246, 255))
            .gridColor(new Color(191, 219, 254))
            .hoverBackground(new Color(59, 130, 246, 18))
            .highlightBackground(new Color(59, 130, 246, 35))
            .selectionBackground(new Color(59, 130, 246, 25))
            .focusBorderColor(new Color(59, 130, 246))
            .totalRowBackground(new Color(219, 234, 254))
            .totalRowForeground(new Color(30, 64, 175))
            .firstColumnBackground(new Color(239, 246, 255))
            .lastColumnBackground(new Color(239, 246, 255))
            .build();

    /**
     * Style clair vert — convient aux tableaux de validation ou résultats
     * positifs.
     */
    public static final HSuperTableStyle PLAIN_GREEN = new Builder()
            .headerBackground(new Color(220, 252, 231))
            .headerForeground(new Color(20, 83, 45))
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(240, 253, 244))
            .gridColor(new Color(187, 247, 208))
            .hoverBackground(new Color(34, 197, 94, 18))
            .highlightBackground(new Color(34, 197, 94, 35))
            .selectionBackground(new Color(34, 197, 94, 25))
            .focusBorderColor(new Color(34, 197, 94))
            .totalRowBackground(new Color(220, 252, 231))
            .totalRowForeground(new Color(20, 83, 45))
            .firstColumnBackground(new Color(240, 253, 244))
            .lastColumnBackground(new Color(240, 253, 244))
            .build();

    /**
     * Style clair rouge — adapté aux alertes ou tableaux d'erreurs.
     */
    public static final HSuperTableStyle PLAIN_RED = new Builder()
            .headerBackground(new Color(254, 226, 226))
            .headerForeground(new Color(153, 27, 27))
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(255, 241, 242))
            .gridColor(new Color(254, 202, 202))
            .hoverBackground(new Color(239, 68, 68, 18))
            .highlightBackground(new Color(239, 68, 68, 35))
            .selectionBackground(new Color(239, 68, 68, 25))
            .focusBorderColor(new Color(239, 68, 68))
            .totalRowBackground(new Color(254, 226, 226))
            .totalRowForeground(new Color(153, 27, 27))
            .firstColumnBackground(new Color(255, 241, 242))
            .lastColumnBackground(new Color(255, 241, 242))
            .build();

    /**
     * Style clair orange — tableaux d'avertissements ou budgets.
     */
    public static final HSuperTableStyle PLAIN_ORANGE = new Builder()
            .headerBackground(new Color(255, 237, 213))
            .headerForeground(new Color(154, 52, 18))
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(255, 247, 237))
            .gridColor(new Color(254, 215, 170))
            .hoverBackground(new Color(249, 115, 22, 18))
            .highlightBackground(new Color(249, 115, 22, 35))
            .selectionBackground(new Color(249, 115, 22, 25))
            .focusBorderColor(new Color(249, 115, 22))
            .totalRowBackground(new Color(255, 237, 213))
            .totalRowForeground(new Color(154, 52, 18))
            .firstColumnBackground(new Color(255, 247, 237))
            .lastColumnBackground(new Color(255, 247, 237))
            .build();

    /**
     * Style clair violet — tableaux créatifs ou catégories.
     */
    public static final HSuperTableStyle PLAIN_PURPLE = new Builder()
            .headerBackground(new Color(243, 232, 255))
            .headerForeground(new Color(88, 28, 135))
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(250, 245, 255))
            .gridColor(new Color(233, 213, 255))
            .hoverBackground(new Color(168, 85, 247, 18))
            .highlightBackground(new Color(168, 85, 247, 35))
            .selectionBackground(new Color(168, 85, 247, 25))
            .focusBorderColor(new Color(168, 85, 247))
            .totalRowBackground(new Color(243, 232, 255))
            .totalRowForeground(new Color(88, 28, 135))
            .firstColumnBackground(new Color(250, 245, 255))
            .lastColumnBackground(new Color(250, 245, 255))
            .build();

    // ── MOYENS (couleurs franches) ────────────────────────────────────────────
    /**
     * Style bleu standard — le style par défaut de HSuperTable. En-tête bleu
     * Bootstrap, alternance bleu très pâle.
     */
    public static final HSuperTableStyle PRIMARY = new Builder()
            .headerBackground(new Color(13, 110, 253))
            .headerForeground(Color.WHITE)
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(245, 248, 255))
            .gridColor(new Color(222, 226, 230))
            .hoverBackground(new Color(13, 110, 253, 20))
            .highlightBackground(new Color(13, 110, 253, 45))
            .selectionBackground(new Color(13, 110, 253, 30))
            .focusBorderColor(new Color(13, 110, 253))
            .totalRowBackground(new Color(13, 110, 253, 55))
            .totalRowForeground(new Color(33, 37, 41))
            .firstColumnBackground(new Color(13, 110, 253, 20))
            .lastColumnBackground(new Color(13, 110, 253, 20))
            .build();

    /**
     * Style vert — succès, validation, données positives.
     */
    public static final HSuperTableStyle SUCCESS = new Builder()
            .headerBackground(new Color(25, 135, 84))
            .headerForeground(Color.WHITE)
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(242, 252, 247))
            .gridColor(new Color(200, 235, 218))
            .hoverBackground(new Color(25, 135, 84, 20))
            .highlightBackground(new Color(25, 135, 84, 45))
            .selectionBackground(new Color(25, 135, 84, 30))
            .focusBorderColor(new Color(25, 135, 84))
            .totalRowBackground(new Color(25, 135, 84, 55))
            .totalRowForeground(new Color(20, 83, 45))
            .firstColumnBackground(new Color(25, 135, 84, 20))
            .lastColumnBackground(new Color(25, 135, 84, 20))
            .build();

    /**
     * Style rouge — erreurs, alertes critiques, données négatives.
     */
    public static final HSuperTableStyle DANGER = new Builder()
            .headerBackground(new Color(220, 53, 69))
            .headerForeground(Color.WHITE)
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(255, 245, 246))
            .gridColor(new Color(248, 194, 198))
            .hoverBackground(new Color(220, 53, 69, 20))
            .highlightBackground(new Color(220, 53, 69, 45))
            .selectionBackground(new Color(220, 53, 69, 30))
            .focusBorderColor(new Color(220, 53, 69))
            .totalRowBackground(new Color(220, 53, 69, 55))
            .totalRowForeground(new Color(153, 27, 27))
            .firstColumnBackground(new Color(220, 53, 69, 20))
            .lastColumnBackground(new Color(220, 53, 69, 20))
            .build();

    /**
     * Style orange — avertissements, seuils, métriques à surveiller.
     */
    public static final HSuperTableStyle WARNING = new Builder()
            .headerBackground(new Color(255, 153, 0))
            .headerForeground(new Color(60, 30, 0))
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(255, 250, 240))
            .gridColor(new Color(255, 220, 160))
            .hoverBackground(new Color(255, 153, 0, 20))
            .highlightBackground(new Color(255, 153, 0, 45))
            .selectionBackground(new Color(255, 153, 0, 30))
            .focusBorderColor(new Color(255, 153, 0))
            .totalRowBackground(new Color(255, 153, 0, 55))
            .totalRowForeground(new Color(60, 30, 0))
            .firstColumnBackground(new Color(255, 153, 0, 20))
            .lastColumnBackground(new Color(255, 153, 0, 20))
            .build();

    /**
     * Style gris — informationnel, neutre, secondaire.
     */
    public static final HSuperTableStyle SECONDARY = new Builder()
            .headerBackground(new Color(108, 117, 125))
            .headerForeground(Color.WHITE)
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(248, 249, 250))
            .gridColor(new Color(206, 212, 218))
            .hoverBackground(new Color(108, 117, 125, 20))
            .highlightBackground(new Color(108, 117, 125, 45))
            .selectionBackground(new Color(108, 117, 125, 30))
            .focusBorderColor(new Color(108, 117, 125))
            .totalRowBackground(new Color(108, 117, 125, 55))
            .totalRowForeground(new Color(33, 37, 41))
            .firstColumnBackground(new Color(108, 117, 125, 20))
            .lastColumnBackground(new Color(108, 117, 125, 20))
            .build();

    /**
     * Style cyan — tableaux d'informations, dashboards techniques.
     */
    public static final HSuperTableStyle INFO = new Builder()
            .headerBackground(new Color(13, 202, 240))
            .headerForeground(new Color(10, 60, 80))
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(240, 252, 255))
            .gridColor(new Color(180, 238, 250))
            .hoverBackground(new Color(13, 202, 240, 20))
            .highlightBackground(new Color(13, 202, 240, 45))
            .selectionBackground(new Color(13, 202, 240, 30))
            .focusBorderColor(new Color(13, 202, 240))
            .totalRowBackground(new Color(13, 202, 240, 55))
            .totalRowForeground(new Color(10, 60, 80))
            .firstColumnBackground(new Color(13, 202, 240, 20))
            .lastColumnBackground(new Color(13, 202, 240, 20))
            .build();

    /**
     * Style violet — rapports analytiques, tableaux de catégories.
     */
    public static final HSuperTableStyle PURPLE = new Builder()
            .headerBackground(new Color(111, 66, 193))
            .headerForeground(Color.WHITE)
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(248, 245, 255))
            .gridColor(new Color(220, 208, 245))
            .hoverBackground(new Color(111, 66, 193, 20))
            .highlightBackground(new Color(111, 66, 193, 45))
            .selectionBackground(new Color(111, 66, 193, 30))
            .focusBorderColor(new Color(111, 66, 193))
            .totalRowBackground(new Color(111, 66, 193, 55))
            .totalRowForeground(new Color(88, 28, 135))
            .firstColumnBackground(new Color(111, 66, 193, 20))
            .lastColumnBackground(new Color(111, 66, 193, 20))
            .build();

    /**
     * Style rose — tableaux créatifs, interfaces grand public.
     */
    public static final HSuperTableStyle PINK = new Builder()
            .headerBackground(new Color(214, 51, 132))
            .headerForeground(Color.WHITE)
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(255, 245, 250))
            .gridColor(new Color(248, 187, 217))
            .hoverBackground(new Color(214, 51, 132, 20))
            .highlightBackground(new Color(214, 51, 132, 45))
            .selectionBackground(new Color(214, 51, 132, 30))
            .focusBorderColor(new Color(214, 51, 132))
            .totalRowBackground(new Color(214, 51, 132, 55))
            .totalRowForeground(new Color(131, 24, 67))
            .firstColumnBackground(new Color(214, 51, 132, 20))
            .lastColumnBackground(new Color(214, 51, 132, 20))
            .build();

    /**
     * Style marron/terre — tableaux financiers, rapports annuels.
     */
    public static final HSuperTableStyle BROWN = new Builder()
            .headerBackground(new Color(120, 72, 33))
            .headerForeground(Color.WHITE)
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(253, 248, 243))
            .gridColor(new Color(222, 198, 175))
            .hoverBackground(new Color(120, 72, 33, 20))
            .highlightBackground(new Color(120, 72, 33, 45))
            .selectionBackground(new Color(120, 72, 33, 30))
            .focusBorderColor(new Color(120, 72, 33))
            .totalRowBackground(new Color(120, 72, 33, 55))
            .totalRowForeground(new Color(60, 30, 10))
            .firstColumnBackground(new Color(120, 72, 33, 20))
            .lastColumnBackground(new Color(120, 72, 33, 20))
            .build();

    /**
     * Style turquoise — tableaux écologiques, environnementaux.
     */
    public static final HSuperTableStyle TEAL = new Builder()
            .headerBackground(new Color(32, 178, 170))
            .headerForeground(Color.WHITE)
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(240, 253, 252))
            .gridColor(new Color(178, 232, 229))
            .hoverBackground(new Color(32, 178, 170, 20))
            .highlightBackground(new Color(32, 178, 170, 45))
            .selectionBackground(new Color(32, 178, 170, 30))
            .focusBorderColor(new Color(32, 178, 170))
            .totalRowBackground(new Color(32, 178, 170, 55))
            .totalRowForeground(new Color(15, 80, 77))
            .firstColumnBackground(new Color(32, 178, 170, 20))
            .lastColumnBackground(new Color(32, 178, 170, 20))
            .build();

    // ── SOMBRES ───────────────────────────────────────────────────────────────
    /**
     * Style sombre bleu nuit — interfaces dark mode, tableaux premium. Texte
     * blanc sur fond sombre, alternance subtile.
     */
    public static final HSuperTableStyle DARK_BLUE = new Builder()
            .headerBackground(new Color(15, 23, 42))
            .headerForeground(new Color(148, 163, 184))
            .headerFont(new Font("Segoe UI", Font.BOLD, 13))
            .cellBackground(new Color(30, 41, 59))
            .cellAlternateBackground(new Color(51, 65, 85)) // un cran plus clair
            .cellForeground(new Color(226, 232, 240))
            .gridColor(new Color(71, 85, 105))
            .hoverBackground(new Color(99, 102, 241, 35))
            .highlightBackground(new Color(99, 102, 241, 60))
            .selectionBackground(new Color(99, 102, 241, 45))
            .focusBorderColor(new Color(99, 102, 241))
            .totalRowBackground(new Color(15, 23, 42))
            .totalRowForeground(new Color(148, 163, 184))
            .firstColumnBackground(new Color(51, 65, 85))
            .lastColumnBackground(new Color(51, 65, 85))
            .build();

    /**
     * Style sombre gris charbon — neutre et élégant pour les dark interfaces.
     */
    public static final HSuperTableStyle DARK_GRAY = new Builder()
            .headerBackground(new Color(31, 31, 31))
            .headerForeground(new Color(200, 200, 200))
            .cellBackground(new Color(45, 45, 45))
            .cellAlternateBackground(new Color(58, 58, 58))
            .cellForeground(new Color(220, 220, 220))
            .gridColor(new Color(70, 70, 70))
            .hoverBackground(new Color(255, 255, 255, 18))
            .highlightBackground(new Color(255, 255, 255, 35))
            .selectionBackground(new Color(255, 255, 255, 25))
            .focusBorderColor(new Color(160, 160, 160))
            .totalRowBackground(new Color(31, 31, 31))
            .totalRowForeground(new Color(200, 200, 200))
            .firstColumnBackground(new Color(58, 58, 58))
            .lastColumnBackground(new Color(58, 58, 58))
            .build();

    /**
     * Style sombre vert terminal — interfaces techniques, logs, monitoring.
     */
    public static final HSuperTableStyle DARK_GREEN = new Builder()
            .headerBackground(new Color(5, 46, 22))
            .headerForeground(new Color(134, 239, 172))
            .cellBackground(new Color(20, 83, 45))
            .cellAlternateBackground(new Color(22, 101, 52))
            .cellForeground(new Color(220, 252, 231))
            .gridColor(new Color(34, 197, 94, 80))
            .hoverBackground(new Color(134, 239, 172, 25))
            .highlightBackground(new Color(134, 239, 172, 45))
            .selectionBackground(new Color(134, 239, 172, 35))
            .focusBorderColor(new Color(134, 239, 172))
            .totalRowBackground(new Color(5, 46, 22))
            .totalRowForeground(new Color(134, 239, 172))
            .firstColumnBackground(new Color(22, 101, 52))
            .lastColumnBackground(new Color(22, 101, 52))
            .build();

    /**
     * Style sombre rouge — alertes critiques, systèmes d'erreurs en dark mode.
     */
    public static final HSuperTableStyle DARK_RED = new Builder()
            .headerBackground(new Color(69, 10, 10))
            .headerForeground(new Color(252, 165, 165))
            .cellBackground(new Color(127, 29, 29))
            .cellAlternateBackground(new Color(153, 27, 27))
            .cellForeground(new Color(254, 226, 226))
            .gridColor(new Color(239, 68, 68, 80))
            .hoverBackground(new Color(252, 165, 165, 25))
            .highlightBackground(new Color(252, 165, 165, 45))
            .selectionBackground(new Color(252, 165, 165, 35))
            .focusBorderColor(new Color(252, 165, 165))
            .totalRowBackground(new Color(69, 10, 10))
            .totalRowForeground(new Color(252, 165, 165))
            .firstColumnBackground(new Color(153, 27, 27))
            .lastColumnBackground(new Color(153, 27, 27))
            .build();

    /**
     * Style sombre violet — tableaux analytiques premium en dark mode.
     */
    public static final HSuperTableStyle DARK_PURPLE = new Builder()
            .headerBackground(new Color(46, 16, 101))
            .headerForeground(new Color(216, 180, 254))
            .cellBackground(new Color(88, 28, 135))
            .cellAlternateBackground(new Color(107, 33, 168))
            .cellForeground(new Color(243, 232, 255))
            .gridColor(new Color(168, 85, 247, 80))
            .hoverBackground(new Color(216, 180, 254, 25))
            .highlightBackground(new Color(216, 180, 254, 45))
            .selectionBackground(new Color(216, 180, 254, 35))
            .focusBorderColor(new Color(216, 180, 254))
            .totalRowBackground(new Color(46, 16, 101))
            .totalRowForeground(new Color(216, 180, 254))
            .firstColumnBackground(new Color(107, 33, 168))
            .lastColumnBackground(new Color(107, 33, 168))
            .build();

    // ── SPÉCIAUX ──────────────────────────────────────────────────────────────
    /**
     * Style zébré classique — alternance noir/blanc très contrastée. Comme les
     * tableaux de rapport Word les plus sobres.
     */
    public static final HSuperTableStyle ZEBRA = new Builder()
            .headerBackground(new Color(33, 37, 41))
            .headerForeground(Color.WHITE)
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(new Color(233, 236, 239))
            .gridColor(new Color(206, 212, 218))
            .hoverBackground(new Color(0, 0, 0, 12))
            .highlightBackground(new Color(0, 0, 0, 22))
            .selectionBackground(new Color(0, 0, 0, 15))
            .focusBorderColor(new Color(33, 37, 41))
            .totalRowBackground(new Color(33, 37, 41))
            .totalRowForeground(Color.WHITE)
            .firstColumnBackground(new Color(233, 236, 239))
            .lastColumnBackground(new Color(233, 236, 239))
            .build();

    /**
     * Style sans bordures — fond blanc uni, aucune grille, séparation
     * minimaliste. Pour des interfaces modernes type "carte de données".
     */
    public static final HSuperTableStyle BORDERLESS = new Builder()
            .headerBackground(Color.WHITE)
            .headerForeground(new Color(33, 37, 41))
            .headerFont(new Font("Segoe UI", Font.BOLD, 13))
            .cellBackground(Color.WHITE)
            .cellAlternateBackground(Color.WHITE) // pas d'alternance
            .cellForeground(new Color(33, 37, 41))
            .gridColor(new Color(255, 255, 255, 0)) // grille invisible
            .hoverBackground(new Color(0, 0, 0, 8))
            .highlightBackground(new Color(13, 110, 253, 20))
            .selectionBackground(new Color(13, 110, 253, 15))
            .focusBorderColor(new Color(13, 110, 253))
            .totalRowBackground(new Color(248, 249, 250))
            .totalRowForeground(new Color(33, 37, 41))
            .firstColumnBackground(Color.WHITE)
            .lastColumnBackground(Color.WHITE)
            .build();

    // =========================================================================
    // toString — utile pour le debug
    // =========================================================================
    @Override 
    public String toString() {
        // On retourne la couleur d'en-tête comme identifiant approximatif du style
        return "HSuperTableStyle{header=" + headerBackground + "}";
    }
}
