package IllustrationShape.model;

/**
 * Capacité opt-in : seules les HShape concrètes qui en ont besoin
 * l'implémentent (une flèche ou une ligne n'a pas de texte).
 */
public interface HTextContent {
    String getText();
    void setText(String text);
}