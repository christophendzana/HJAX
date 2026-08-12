package illustrations.model;

import java.awt.Graphics2D;

/**
 * Contrat minimal de tout objet graphique manipulable par Canvas : rendu, position/taille
 * (via GraphicTransform), détection de clic, sélection. Ne présuppose rien sur la nature
 * de l'objet (forme, texte, image...) ni sur ses paramètres d'ajustement éventuels.
 */
public interface GraphicObject {

    // Donne accès à l'état géométrique (position, dimensions, rotation) pour HandleType et Canvas
    GraphicTransform getTransform();

    // Dessine l'objet sur le contexte graphique
    void paint(Graphics2D g);

    // Détermine si un point écran (world) touche cet objet, avec tolérance pour les formes ouvertes
    boolean containsPoint(int worldMx, int worldMy);

    boolean isSelected();
    void setSelected(boolean selected);
}