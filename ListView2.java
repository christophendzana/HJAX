package view;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author FIDELE
 */
public class ListView2 extends HView {

    /**
     * Lignes brutes, telles que fournies par l'appelant.
     */
    private final ArrayList<String> texts = new ArrayList<>();

    /**
     * Décalage horizontal appliqué par niveau de profondeur
     */
    private int indent = 20;

    /**
     * Espace entre la puce/numéro et le texte qui suit, garanti constant quelle
     * que soit la police active (voir calcul dans Paint()).
     */
    private int listGap = 5;

    /**
     * Mode d'affichage de la liste : numérotée ou à puces
     */
    private TypeListe typeListe = TypeListe.NUMEROTEE;

    /**
     * Puce utilisée uniquement en mode PUCES, pour tous les niveaux
     */
    private PucesStyle puces = PucesStyle.ROND;

    /**
     * Format des nombres/lettres par niveau, utilisé uniquement en mode
     * NUMEROTEE
     */
    private FormatNumero[] formatsParNiveau = {
        FormatNumero.ARABE,
        FormatNumero.ARABE,
        FormatNumero.ARABE,};

    /**
     * Symbole affiché après chaque segment numéroté, ex: "1." "1)" "1-"
     */
    private char symboleFinal = '.';

    public ListView2() {
    }

    public ListView2(List<String> lignes) {
        texts.addAll(lignes);
    }

    /**
     * Enregistre une ligne telle quelle.
     */
    public boolean add(String texte) {
        return texts.add(texte);
    }

    public boolean add(List<String> lignes) {
        return texts.addAll(lignes);
    }

    public void setTypeListe(TypeListe typeListe) {
        this.typeListe = typeListe;
    }

    public TypeListe getTypeListe() {
        return typeListe;
    }

    public void setPuces(PucesStyle puces) {
        this.puces = puces;
    }

    public PucesStyle getPuces() {
        return puces;
    }

    public void setFormatsParNiveau(FormatNumero... formats) {
        if (formats == null || formats.length == 0) {
            throw new IllegalArgumentException("Il faut au moins un format");
        }
        this.formatsParNiveau = formats;
    }

    public FormatNumero[] getFormatsParNiveau() {
        return formatsParNiveau;
    }

    public void setSymboleFinal(char symbole) {
        if (symbole != '.' && symbole != ')' && symbole != '-') {
            throw new IllegalArgumentException("Symbole final autorisé : '.', ')' ou '-'");
        }
        this.symboleFinal = symbole;
    }

    public char getSymboleFinal() {
        return symboleFinal;
    }

    public void setIndent(int indent) {
        if (indent <= 0) {
            throw new IllegalArgumentException("Bad Argument: Indent");
        }
        this.indent = indent;
    }

    public void setListGap(int listGap) {
        if (listGap < 0) {
            throw new IllegalArgumentException("ListGap ne peut pas être négatif");
        }
        this.listGap = listGap;
    }

    public int getListGap() {
        return listGap;
    }

    /**
     * Action de changement de niveau détectée en tête d'une ligne.
     */
    private enum ActionNiveau {
        AVANCER, RECULER, RESET, AUCUNE
    }

    /**
     * Résultat de l'analyse du marqueur en tête d'une ligne
     */
    private record CommandeNiveau(ActionNiveau action, int valeur, int longueurMarqueur) {

    }

    /**
     * Lit le marqueur de niveau en tête de ligne, s'il y en a un. - '\t' ->
     * avancer d'un niveau - '\n' -> retour au niveau 0 - '\r' seul -> reculer
     * d'un niveau - '\r' suivi de chiffres -> reculer du nombre indiqué (ex:
     * "\r12") - rien de tout ça -> aucune action, le niveau reste inchangé
     */
    private CommandeNiveau lireCommande(String ligne) {
        if (ligne.isEmpty()) {
            return new CommandeNiveau(ActionNiveau.AUCUNE, 0, 0);
        }

        char premier = ligne.charAt(0);

        if (premier == '\t') {
            return new CommandeNiveau(ActionNiveau.AVANCER, 1, 1);
        }
        if (premier == '\n') {
            return new CommandeNiveau(ActionNiveau.RESET, 0, 1);
        }
        if (premier == '\r') {
            int fin = 1;
            while (fin < ligne.length() && Character.isDigit(ligne.charAt(fin))) {
                fin++;
            }
            // Pas de chiffre après le \r -> on recule d'un seul niveau par défaut
            int valeur = (fin == 1) ? 1 : Integer.parseInt(ligne.substring(1, fin));
            return new CommandeNiveau(ActionNiveau.RECULER, valeur, fin);
        }

        return new CommandeNiveau(ActionNiveau.AUCUNE, 0, 0);
    }

    private FormatNumero formatPourNiveau(int niveau) {
        int index = Math.min(niveau, formatsParNiveau.length - 1);
        return formatsParNiveau[index];
    }

    /**
     * Construit le numéro hiérarchique correspondant au niveau demandé. Le
     * numéro est obtenu en concaténant les compteurs de tous les niveaux, du
     * niveau 0 jusqu'au niveau courant, en appliquant le format de numérotation
     * défini pour chaque niveau (arabe, romain, alphabétique, etc.).
     *
     * @param niveau le niveau hiérarchique courant.
     * @param counters les compteurs de numérotation de chaque niveau.
     * @return le numéro hiérarchique formaté.
     */
    private String construireNumero(int niveau, List<Integer> counters) {
        StringBuilder numero = new StringBuilder();
        for (int i = 0; i <= niveau; i++) {
            numero.append(formatPourNiveau(i).convertir(counters.get(i)))
                    .append(symboleFinal);
        }
        return numero.toString();
    }

    @Override
    public void Paint(Graphics g, int x, int y) {

        // Tout est recalculé ici, à chaque appel : niveau courant, compteurs,
        // numérotation. Rien n'est conservé d'un appel à l'autre.
        int niveauCourant = 0;
        List<Integer> counters = new ArrayList<>();
        counters.add(0); // compteur du niveau 0
        FontMetrics fm = g.getFontMetrics();

        for (String ligneBrute : texts) {

            CommandeNiveau commande = lireCommande(ligneBrute);

            switch (commande.action()) {
                case AVANCER ->
                    niveauCourant++;
                case RECULER ->
                    niveauCourant = Math.max(0, niveauCourant - commande.valeur());
                case RESET ->
                    niveauCourant = 0;
                case AUCUNE -> {
                    // le niveau ne change pas
                }
            }

            ensureCapacity(counters, niveauCourant);

            String texte = ligneBrute.substring(commande.longueurMarqueur());

            // Règle unique de mise à jour des compteurs, inchangée depuis le début
            for (int i = niveauCourant + 1; i < counters.size(); i++) {
                counters.set(i, 0);
            }
            counters.set(
                    niveauCourant,
                    counters.get(niveauCourant) + 1
            );

            y += fm.getAscent();
            int xNiveau = x + niveauCourant * indent;

            if (typeListe == TypeListe.NUMEROTEE) {
                String numero = construireNumero(niveauCourant, counters);
                g.drawString(numero, xNiveau, y);

                // ListGap mesuré sur la largeur réelle du numéro dans la police
                // active : reste visuellement constant quelle que soit la taille
                // de police
                int largeurNumero = fm.stringWidth(numero);
                g.drawString(texte, xNiveau + largeurNumero + listGap, y);

            } else {
                puces.dessiner(g, xNiveau, y);

                int largeurPuce = puces.largeur();
                g.drawString(texte, xNiveau + largeurPuce + listGap, y);
            }
        }
    }

    /**
     * S'assure que la liste des compteurs possède un élément pour le niveau
     * demandé. Si le niveau n'existe pas encore, des compteurs initialisés à 0
     * sont ajoutés. Évite les IndexOutOfBoundsException lors des accès avec
     * get() ou set().
     */
    private static void ensureCapacity(List<Integer> counters, int niveau) {
        while (counters.size() <= niveau) {
            counters.add(0);
        }
    }

    /**
     * Mode d'affichage global de la liste
     */
    public enum TypeListe {
        NUMEROTEE,
        PUCES
    }

    /**
     * Format de conversion d'un compteur en texte, utilisé uniquement en mode
     * NUMEROTEE. Chaque constante sait convertir un entier vers sa propre
     * représentation.
     */
    public enum FormatNumero {

        ARABE {
            @Override
            String convertir(int valeur) {
                return String.valueOf(valeur);
            }
        },
        ROMAIN_MAJUSCULE {
            @Override
            String convertir(int valeur) {
                return versRomain(valeur);
            }
        },
        ROMAIN_MINUSCULE {
            @Override
            String convertir(int valeur) {
                return versRomain(valeur).toLowerCase();
            }
        },
        ALPHA_MAJUSCULE {
            @Override
            String convertir(int valeur) {
                return versAlpha(valeur);
            }
        },
        ALPHA_MINUSCULE {
            @Override
            String convertir(int valeur) {
                return versAlpha(valeur).toLowerCase();
            }
        };

        abstract String convertir(int valeur);

        private static final int[] VALEURS_ROMAINES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        private static final String[] SYMBOLES_ROMAINS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        private static String versRomain(int valeur) {
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

        private static String versAlpha(int valeur) {
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
    }

    /**
     * Style de puce, utilisé uniquement en mode PUCES. Chaque constante sait se
     * dessiner elle-même.
     */
    public enum PucesStyle {

        ROND {
            @Override
            public void dessiner(Graphics g, int x, int y) {
                g.fillOval(x, y - 8, TAILLE, TAILLE);
            }
        },
        CARRE {
            @Override
            public void dessiner(Graphics g, int x, int y) {
                g.fillRect(x, y - 8, TAILLE, TAILLE);
            }
        },
        IMAGE {
            private Image image;

            @Override
            public void setImage(Image image) {
                this.image = image;
            }

            @Override
            public void dessiner(Graphics g, int x, int y) {
                if (image == null) {
                    throw new IllegalStateException("Aucune image définie : appeler puces.setImage(...) avant Paint()");
                }
                g.drawImage(image, x, y - largeur(), largeur(), largeur(), null);
            }

            @Override
            public int largeur() {
                return 10;
            }
        },
        FLECHE {
            @Override
            public void dessiner(Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = largeur();
                int yTop = y - size;
                Path2D.Double arrow = new Path2D.Double();
                arrow.moveTo(x + size, yTop + size / 2.0);
                arrow.lineTo(x + size - 4, yTop + 2);
                arrow.moveTo(x + size, yTop + size / 2.0);
                arrow.lineTo(x + size - 4, yTop + size - 2);
                arrow.moveTo(x + 2, yTop + size / 2.0);
                arrow.lineTo(x + size - 2, yTop + size / 2.0);
                g2d.setStroke(new BasicStroke(2.5f));
                g2d.draw(arrow);
            }

            @Override
            public int largeur() {
                return 12;
            }
        },
        LOSANGE {
            @Override
            public void dessiner(Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = largeur();
                int yTop = y - size;
                Path2D.Double diamond = new Path2D.Double();
                diamond.moveTo(x + size / 2.0, yTop);
                diamond.lineTo(x + size, yTop + size / 2.0);
                diamond.lineTo(x + size / 2.0, yTop + size);
                diamond.lineTo(x, yTop + size / 2.0);
                diamond.closePath();
                g2d.fill(diamond);
            }

            @Override
            public int largeur() {
                return 10;
            }
        },
        COCHE {
            @Override
            public void dessiner(Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = largeur();
                int yTop = y - size;
                Path2D.Double checkmark = new Path2D.Double();
                checkmark.moveTo(x + 2, yTop + size * 0.6);
                checkmark.lineTo(x + size * 0.4, yTop + size * 0.9);
                checkmark.lineTo(x + size - 2, yTop + 2);
                g2d.setStroke(new BasicStroke(3.0f));
                g2d.draw(checkmark);
            }

            @Override
            public int largeur() {
                return 10;
            }
        },
        QUATRE_LOSANGES {
            @Override
            public void dessiner(Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = largeur();
                int yTop = y - size + 2;
                int d = size / 3;
                int centerX = x + size / 2;
                int centerY = yTop + size / 2;
                int offset = d;
                dessinerUnLosange(g2d, centerX, centerY - offset, d);
                dessinerUnLosange(g2d, centerX - offset, centerY, d);
                dessinerUnLosange(g2d, centerX + offset, centerY, d);
                dessinerUnLosange(g2d, centerX, centerY + offset, d);
            }

            @Override
            public int largeur() {
                return 16;
            }
        },;

        /**
         * Largeur/hauteur en pixels d'une puce, utilisée pour le calcul du
         * ListGap
         */
        static final int TAILLE = 6;

        private static void dessinerUnLosange(Graphics2D g2d, int cx, int cy, int d) {
            Path2D.Double diamond = new Path2D.Double();
            diamond.moveTo(cx, cy - d / 2.0);
            diamond.lineTo(cx + d / 2.0, cy);
            diamond.lineTo(cx, cy + d / 2.0);
            diamond.lineTo(cx - d / 2.0, cy);
            diamond.closePath();
            g2d.fill(diamond);
        }

        public abstract void dessiner(Graphics g, int x, int y);

        /**
         * Largeur réellement occupée par cette puce, utilisée pour calculer où
         * commence le texte. Par défaut, la taille standard des puces
         * graphiques.
         */
        public int largeur() {
            return TAILLE;
        }

        public void setImage(Image image) {
            // no-op par défaut, seule IMAGE la redéfinit
        }
    }
}
