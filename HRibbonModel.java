package rubban;

import java.util.List;

/**
 * Interface définissant le contrat pour les modèles de ruban.
 * Un ruban est organisé en groupes, chaque groupe contenant une liste d'éléments.
 * 
 * @author FIDELE
 */
public interface HRibbonModel {
    
    // =========================================================================
    // MÉTHODES DE GESTION DES GROUPES
    // =========================================================================
    
    /**
     * Retourne le nombre de groupes dans le modèle.
     * Le ruban utilise cette méthode pour déterminer combien de groupes afficher.
     *
     * @return le nombre de groupes dans le modèle
     */
    public int getGroupCount();
    
    /**
     * Retourne l'identifiant du groupe à l'index spécifié.
     * Cet identifiant est utilisé pour initialiser l'en-tête du groupe.
     * Note : l'identifiant n'a pas besoin d'être unique.
     *
     * @param groupIndex l'index du groupe
     * @return l'identifiant du groupe
     */
    public Object getGroupIdentifier(int groupIndex);
    
    /**
     * Ajoute un nouveau groupe vide avec l'identifiant spécifié.
     *
     * @param groupIdentifier l'identifiant du groupe à ajouter
     */
    public void addGroup(Object groupIdentifier);
    
    
    // =========================================================================
    // MÉTHODES D'ACCÈS AUX VALEURS PAR INDEX
    // =========================================================================
    
    /**
     * Retourne le nombre de valeurs dans le groupe à l'index spécifié.
     *
     * @param groupIndex l'index du groupe
     * @return le nombre de valeurs dans le groupe
     */
    public int getValueCount(int groupIndex);
    
    /**
     * Retourne la valeur à la position spécifiée dans le groupe à l'index spécifié.
     *
     * @param position la position dans le groupe
     * @param groupIndex l'index du groupe
     * @return la valeur à la position spécifiée
     */
    public Object getValueAt(int position, int groupIndex);
    
    /**
     * Définit la valeur à la position spécifiée dans le groupe à l'index spécifié.
     *
     * @param value la nouvelle valeur
     * @param position la position dans le groupe
     * @param groupIndex l'index du groupe
     */
    public void setValueAt(Object value, int position, int groupIndex);
    
    /**
     * Ajoute une valeur à la fin du groupe à l'index spécifié.
     *
     * @param value la valeur à ajouter
     * @param groupIndex l'index du groupe
     */
    public void addValue(Object value, int groupIndex);
    
    /**
     * Insère une valeur à la position spécifiée dans le groupe à l'index spécifié.
     *
     * @param value la valeur à insérer
     * @param position la position d'insertion
     * @param groupIndex l'index du groupe
     */
    public void insertValueAt(Object value, int position, int groupIndex);
    
    
    // =========================================================================
    // MÉTHODES D'ACCÈS AUX VALEURS PAR IDENTIFIANT DE GROUPE
    // =========================================================================
    
    /**
     * Retourne le nombre de valeurs dans le groupe ayant l'identifiant spécifié.
     *
     * @param groupIdentifier l'identifiant du groupe
     * @return le nombre de valeurs dans le groupe
     */
    public int getValueCount(Object groupIdentifier);
    
    /**
     * Retourne la valeur à la position spécifiée dans le groupe ayant l'identifiant spécifié.
     *
     * @param position la position dans le groupe
     * @param groupIdentifier l'identifiant du groupe
     * @return la valeur à la position spécifiée
     */
    public Object getValueAt(int position, Object groupIdentifier);
    
    /**
     * Définit la valeur à la position spécifiée dans le groupe ayant l'identifiant spécifié.
     *
     * @param value la nouvelle valeur
     * @param position la position dans le groupe
     * @param groupIdentifier l'identifiant du groupe
     */
    public void setValueAt(Object value, int position, Object groupIdentifier);
    
    /**
     * Ajoute une valeur à la fin du groupe ayant l'identifiant spécifié.
     *
     * @param value la valeur à ajouter
     * @param groupIdentifier l'identifiant du groupe
     */
    public void addValue(Object value, Object groupIdentifier);
    
    /**
     * Insère une valeur à la position spécifiée dans le groupe ayant l'identifiant spécifié.
     *
     * @param value la valeur à insérer
     * @param position la position d'insertion
     * @param groupIdentifier l'identifiant du groupe
     */
    public void insertValueAt(Object value, int position, Object groupIdentifier);
    
    
    // =========================================================================
    // MÉTHODES UTILITAIRES
    // =========================================================================
    
    /**
     * Retourne une liste de tous les composants (valeurs) du groupe à l'index spécifié.
     *
     * @param groupIndex l'index du groupe
     * @return une liste des valeurs du groupe
     */
    public List<Object> getComponentsAt(int groupIndex);
    
    
    // =========================================================================
    // GESTION DES LISTENERS
    // =========================================================================
    
    /**
     * Ajoute un listener pour être notifié des changements du modèle.
     *
     * @param l le listener à ajouter
     */
    public void addRibbonModelListener(HRibbonModelListener l);
    
    /**
     * Supprime un listener.
     *
     * @param l le listener à supprimer
     */
    public void removeModelListener(HRibbonModelListener l);
    
    
    // =========================================================================
    // GESTION DE VERSION
    // =========================================================================
    
    /**
     * Retourne le numéro de version du modèle.
     * Ce compteur est incrémenté à CHAQUE modification du modèle.
     * Permet aux composants de détecter si le modèle a changé.
     *
     * @return la version actuelle du modèle
     */
    public long getVersion();
}