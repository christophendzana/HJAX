/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.controllers.HListController;
import hcomponents.models.HListModel;
import hcomponents.vues.HListTheme;
import hcomponents.vues.HListUI;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JList;

/**
 *
 * @author FIDELE
 * @param <E>
 */
public class HList<E> extends JList<E> {

    private final HListModel<E> hModel;
    private final HListController controller;
    private HListTheme theme;

    /**
     * Constructeur par défaut
     */
    public HList() {
        this(new HListModel<>());
    }
    
    /**
     * Constructeur avec modèle personnalisé
     * @param model
     */
    public HList(HListModel<E> model) {
       
        super(model);
        if (model == null) {
            throw new IllegalArgumentException("dataModel must be non null");
        }        
        this.hModel = model;        
        this.theme = new HListTheme(); // Thème par défaut
        initializeComponent();
        this.controller = new HListController(this, model);
    }    

    /**
     * Constructeur avec tableau de données
     * @param listData
     */
    public HList(E[] listData) {
        this();
        setListData(listData);
    }

    /**
     * Initialise l'apparence du composant
     */
    private void initializeComponent() {
        setUI(new HListUI(theme));
        applyThemeSettings();
    }

    /**
     * Applique les paramètres du thème actuel
     */
    private void applyThemeSettings() {
        setFont(getFont().deriveFont(Font.PLAIN, 14f));
        setFixedCellHeight(theme.getItemHeight());
        setFixedCellWidth(-1); // Largeur automatique
        setOpaque(false);
    }

    /**
     * Définit un thème personnalisé
     * @param theme
     * @return 
     */
    public HList<E> setHTheme(HListTheme theme) {
        this.theme = theme;
        refreshUI();
        return this; 
    }

    /**
     * Méthode rapide pour définir la bordure
     * @param borderColor
     * @param borderRadius
     * @return 
     */
    public HList<E> setHBorder(Color borderColor, int borderRadius) {
        this.theme.setBorderColor(borderColor).setBorderRadius(borderRadius);
        refreshUI();
        return this;
    }

    /**
     * Méthode rapide pour définir les couleurs de sélection
     * @param background
     * @param foreground
     * @return 
     */
    public HList<E> setHSelection(Color background, Color foreground) {
        this.theme.setSelectionBackground(background).setSelectionForeground(foreground);
        refreshUI();
        return this;
    }

    /**
     * Méthode rapide pour définir l'apparence des items
     * @param normalBg
     * @param normalFg
     * @param itemRadius
     * @return 
     */
    public HList<E> setHItems(Color normalBg, Color normalFg, int itemRadius) {
        this.theme.setNormalBackground(normalBg)
                .setNormalForeground(normalFg)
                .setItemRadius(itemRadius);
        refreshUI();
        return this;
    }

    /**
     * Rafraîchit l'interface utilisateur
     */
    public void refreshUI() {
        setUI(new HListUI(theme));
        applyThemeSettings();
        repaint();
    }

    /**
     * Récupère le thème actuel (pour modifications incrémentielles)
     * @return 
     */
    public HListTheme getHTheme() {
        return theme;
    }
    
    @Override
    public void setListData(E[] data) {
        hModel.removeAllElements();
        for (E item : data) {
            hModel.addElement(item);
        }
    }

    public void addItem(E element) {
        hModel.addElement(element);
    }

    public HListModel<E> getHModel() {
        return hModel;
    }

    public void addStateListener(Runnable listener) {
        hModel.addStateListener(listener);
    }

}
