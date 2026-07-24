package view;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author FIDELE
 */
public class ListView extends HView {

    /**
     * Liste de texte sur lesquels les listes seront appliqués. Chaque ligne
     * peut commencer par un marqueur de commande (\t, \r, \rN, \n) qui indique
     * comment le niveau évolue par rapport à la ligne précédente.
     */
    private List<String> texts;

    private int indent = 10;

    private PucesStyle puces = PucesStyle.ALPHA_NUMERIC;

    private int interligne = 3;

    /**
     * Espace entre la puce et le texte
     */
    private int gapList = 3;

    public ListView() {
        texts = new ArrayList<>();
    }

    public ListView(List<String> texts) {
        this.texts = texts;
    }

    public boolean add(String text) {
        return texts.add(text);
    }

    public boolean add(List<String> texts) {
        return this.texts.addAll(texts);
    }

    public void setPuces(PucesStyle puces) {
        this.puces = puces;
    }

    public void setInterligne(int interligne) {
        if (interligne < 0) {
            throw new IllegalArgumentException("Bad Argument: Interligne cannot be negative");
        }
        this.interligne = interligne;
    }

    public int getInterligne() {
        return interligne;
    }

    public int getGapList() {
        return gapList;
    }

    public void setGapList(int gap) {
        if (gap < 0) {
            throw new IllegalArgumentException("Bad Argument: GapList cannot be negative");
        }
        this.gapList = gap;
    }

    public PucesStyle getPuces() {
        return puces;
    }

    public enum PucesStyle {

        ROUND() {
            /**
             * Puces ronde vide
             */
            public int EMPTY = 0;
            /**
             * Puces ronde pleine
             */
            public int FILL = 0;
        },
        RECTANGLE() {
            /**
             * Puces carré vide
             */
            public int EMPTY = 0;
            /**
             * Puces carré plein
             */
            public int FILL = 1;
        },
        ALPHA_NUMERIC() {
            /**
             * Profondeur de la puce
             */
            private int deep;

            /*
            * Type de puce de caractère choisi
             */
            private static int typePuceCaracter = INTEGER;

            /**
             * Utilisé uniquement quand typePuceCaracter == INTEGER : décide si
             * les nombres s'affichent en chiffres romains ou en chiffres
             * normaux
             */
            private int type = FRENCH_TYPE;

            private char endChar = '.';

            public void setIntegerType(int type) {
                if (type != ROMAN_TYPE && type != FRENCH_TYPE) {
                    throw new IllegalArgumentException("bad Argument");
                }
                this.type = type;
            }

            /**
             * Chiffre Romain ou Normaux
             */
            @Override
            public int getIntegerType() {
                return type;
            }

            public void setDeep(int deep) {
                if (deep <= 0) {
                    throw new IllegalArgumentException("Deep cannot be less than 0");
                }
                this.deep = deep;
            }

            public int getDeep() {
                return deep;
            }

            @Override
            public void setEndChar(char c) {
                if (c != '.' && c != ')' && c != '-') {
                    throw new IllegalArgumentException("End caracter should be '.', ')' or '-' ");
                }
                this.endChar = c;
            }

            public void setCaracter(int typePuceCaracter) {
                if (typePuceCaracter != INTEGER && typePuceCaracter != MINLETTER && typePuceCaracter != MAJLETTER) {
                    throw new IllegalArgumentException("bad Argument");
                }
                this.typePuceCaracter = typePuceCaracter;
            }

            /*
            *Symbole après la puce
             */
            @Override
            public char getEndChar() {
                return endChar;
            }

            @Override
            public int getTypePuceCaracter() {
                return typePuceCaracter;
            }

        },
        IMAGE() {
            private java.awt.Image image;

            @Override
            public void setImage(java.awt.Image image) {
                this.image = image;
            }

            @Override
            java.awt.Image getImage() {
                return image;
            }

            @Override
            public int largeur() {
                return 10;
            }
        },
        FLECHE() {
            @Override
            public int largeur() {
                return 12;
            }
        },
        LOSANGE() {
            @Override
            public int largeur() {
                return 10;
            }
        },
        COCHE() {
            @Override
            public int largeur() {
                return 10;
            }
        },
        QUATRE_LOSANGES() {
            @Override
            public int largeur() {
                return 16;
            }
        },;

        private PucesStyle() {
        }

        public static final int INTEGER = 2;

        public static final int MINLETTER = 3;

        public static final int MAJLETTER = 4;

        public static final int ROMAN_TYPE = 0;

        public static final int FRENCH_TYPE = 1;

        public int getTypePuceCaracter() {
            return -1;
        }

        public int getIntegerType() {
            return FRENCH_TYPE;
        }

        /*
         *Symbole après la puce
         */
        public char getEndChar() {
            return '.';
        }

        public void setEndChar(char c) {
            // no-op par défaut : ROUND et RECTANGLE n'ont pas de symbole à définir
        }

        public void setCaracter(int typeCaracter) {
        }

        public void setImage(java.awt.Image image) {
            // no-op par défaut : seule IMAGE a une image à stocker
        }

        java.awt.Image getImage() {
            return null;
        }

        /**
         * Largeur/hauteur en pixels occupée par cette puce à l'affichage. Sert
         * à calculer où commence le texte (gapList) et le décalage vertical du
         * dessin, pour que les deux restent toujours cohérents entre eux.
         */
        public int largeur() {
            return 6;
        }

    }

    public void setIndent(int indent) {
        if (indent <= 0) {
            throw new IllegalArgumentException("Bad Argument: Indent");
        }
        this.indent = indent;
    }

    @Override
    public void Paint(Graphics g, int x, int y) {

        StringBuffer buffer;

        int level = 0;

        List<Integer> compteurs = new ArrayList<>();
        compteurs.add(0); // niveau 0

        FontMetrics fm = g.getFontMetrics();

        for (int i = 0; i < texts.size(); i++) {

            buffer = new StringBuffer(texts.get(i));

            // Détection du marqueur en tête de ligne et mise à jour du niveau.            
            char premier = buffer.length() > 0 ? buffer.charAt(0) : ' ';

            switch (premier) {
                case '\t' -> {
                    level++;
                    buffer.deleteCharAt(0);
                }
                case '\n' -> {
                    level = 0;
                    buffer.deleteCharAt(0);
                }
                case '\r' -> {
                    int fin = 1;
                    while (fin < buffer.length() && Character.isDigit(buffer.charAt(fin))) {
                        fin++;
                    }
                    int recul = (fin == 1) ? 1 : Integer.parseInt(buffer.substring(1, fin));
                    level = Math.max(0, level - recul);
                    buffer.delete(0, fin);
                }
                default -> {
                    // aucun marqueur : le niveau ne change pas
                }
            }

            ensureCapacity(compteurs, level);

            // Règle unique de mise à jour des compteurs : reset des niveaux plus
            // profonds que le niveau courant, puis incrément du niveau courant.
            for (int p = level + 1; p < compteurs.size(); p++) {
                compteurs.set(p, 0);
            }
            compteurs.set(level, compteurs.get(level) + 1);

            if (i > 0) {
                y += fm.getAscent() + interligne;
            } else {
                y += fm.getAscent();
            }

            int decalageX = x + level * indent;

            if (puces == PucesStyle.ALPHA_NUMERIC) {

                // Construction du numéro hiérarchique complet, ex: "1.2.1."
                StringBuilder numero = new StringBuilder();
                for (int p = 0; p <= level; p++) {
                    numero.append(findPuceCaracter(
                            puces.ALPHA_NUMERIC.getTypePuceCaracter(),
                            compteurs.get(p)));
                    numero.append(puces.ALPHA_NUMERIC.getEndChar());
                }

                g.drawString(numero.toString(), decalageX, y);

                int largeurNumero = fm.stringWidth(numero.toString());
                int gap = decalageX + largeurNumero;
                for (int j = 0; j < gapList; j++) {
                    gap++;
                }

                g.drawString(buffer.toString(), gap, y);

            } else {
                // Puces graphiques ROUND / RECTANGLE : pas de String, juste une forme
                switch (puces) {
                    case ROUND ->
                        g.fillOval(decalageX, y - puces.largeur(), puces.largeur(), puces.largeur());
                    case RECTANGLE ->
                        g.fillRect(decalageX, y - puces.largeur(), puces.largeur(), puces.largeur());
                    case IMAGE -> {
                        java.awt.Image image = puces.getImage();
                        if (image == null) {
                            throw new IllegalStateException("Aucune image définie");
                        }
                        g.drawImage(image, decalageX, y - puces.largeur(), puces.largeur(), puces.largeur(), null);
                    }
                    case FLECHE ->
                        dessinerFleche((Graphics2D) g.create(), decalageX, y - puces.largeur(), puces.largeur());
                    case LOSANGE ->
                        dessinerLosange((Graphics2D) g.create(), decalageX, y - puces.largeur(), puces.largeur());
                    case COCHE ->
                        dessinerCoche((Graphics2D) g.create(), decalageX, y - puces.largeur(), puces.largeur());
                    case QUATRE_LOSANGES ->
                        dessinerQuatreLosanges(
                                (Graphics2D) g.create(),
                                decalageX,
                                y - puces.largeur() + 2,
                                puces.largeur());
                    default -> {
                    }
                }

                int gap = decalageX + puces.largeur() + gapList;
                g.drawString(buffer.toString(), gap, y);
            }
        }

    }

    /**
     * S'assure que la liste des compteurs possède un élément pour le niveau
     * demandé. Si le niveau n'existe pas encore, des compteurs initialisés à 0
     * sont ajoutés. Évite les IndexOutOfBoundsException lors des accès avec
     * get() ou set().
     */
    private static void ensureCapacity(List<Integer> compteurs, int niveau) {
        while (compteurs.size() <= niveau) {
            compteurs.add(0);
        }
    }

    /**
     * Renvoie le caractère (ou groupe de caractères) correspondant à une valeur
     * de compteur, selon le type de puce choisi.
     */
    private String findPuceCaracter(int caracterType, int valeur) {
        switch (caracterType) {
            case PucesStyle.INTEGER -> {
                if (puces.getIntegerType() == PucesStyle.ROMAN_TYPE) {
                    return versRomain(valeur);
                }
                return String.valueOf(valeur);
            }
            case PucesStyle.MINLETTER -> {
                return versAlpha(valeur).toLowerCase();
            }
            case PucesStyle.MAJLETTER -> {
                return versAlpha(valeur);
            }
            default ->
                throw new IllegalArgumentException("Bad caracter value");
        }
    }

    private static final int[] VALEURS_ROMAINES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] SYMBOLES_ROMAINS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    private String versRomain(int valeur) {
        if (valeur <= 0) {
            throw new IllegalArgumentException("Un chiffre romain ne peut représenter que des valeurs positives");
        }
        StringBuilder resultat = new StringBuilder();
        for (int i = 0; i < VALEURS_ROMAINES.length; i++) {
            while (valeur >= VALEURS_ROMAINES[i]) {
                valeur -= VALEURS_ROMAINES[i];
                resultat.append(SYMBOLES_ROMAINS[i]);
            }
        }
        return resultat.toString();
    }

    /**
     * Conversion : A..Z, puis AA, AB... Ça évite d'être limité à 26 éléments
     * par niveau.
     */
    private String versAlpha(int valeur) {
        if (valeur <= 0) {
            throw new IllegalArgumentException("Une lettre ne peut représenter que des valeurs positives");
        }
        StringBuilder resultat = new StringBuilder();
        while (valeur > 0) {
            valeur--;
            resultat.insert(0, (char) ('A' + (valeur % 26)));
            valeur /= 26;
        }
        return resultat.toString();
    }

    private void dessinerFleche(Graphics2D g2d, int x, int y, int size) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Path2D.Double arrow = new Path2D.Double();
        arrow.moveTo(x + size, y + size / 2.0);
        arrow.lineTo(x + size - 4, y + 2);
        arrow.moveTo(x + size, y + size / 2.0);
        arrow.lineTo(x + size - 4, y + size - 2);
        arrow.moveTo(x + 2, y + size / 2.0);
        arrow.lineTo(x + size - 2, y + size / 2.0);
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.draw(arrow);
    }

    private void dessinerLosange(Graphics2D g2d, int x, int y, int size) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Path2D.Double diamond = new Path2D.Double();
        diamond.moveTo(x + size / 2.0, y);
        diamond.lineTo(x + size, y + size / 2.0);
        diamond.lineTo(x + size / 2.0, y + size);
        diamond.lineTo(x, y + size / 2.0);
        diamond.closePath();
        g2d.fill(diamond);
    }

    private void dessinerCoche(Graphics2D g2d, int x, int y, int size) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Path2D.Double checkmark = new Path2D.Double();
        checkmark.moveTo(x + 2, y + size * 0.6);
        checkmark.lineTo(x + size * 0.4, y + size * 0.9);
        checkmark.lineTo(x + size - 2, y + 2);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.draw(checkmark);
    }

    private void dessinerQuatreLosanges(Graphics2D g2d, int x, int y, int size) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int d = size / 3;
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int offset = size / 2 - d / 2;
        dessinerUnLosange(g2d, centerX, centerY - offset, d);
        dessinerUnLosange(g2d, centerX - offset, centerY, d);
        dessinerUnLosange(g2d, centerX + offset, centerY, d);
        dessinerUnLosange(g2d, centerX, centerY + offset, d);
    }

    private void dessinerUnLosange(Graphics2D g2d, int cx, int cy, int d) {
        Path2D.Double diamond = new Path2D.Double();
        diamond.moveTo(cx, cy - d / 2.0);
        diamond.lineTo(cx + d / 2.0, cy);
        diamond.lineTo(cx, cy + d / 2.0);
        diamond.lineTo(cx - d / 2.0, cy);
        diamond.closePath();
        g2d.fill(diamond);
    }

}
