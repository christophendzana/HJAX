/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package htextarea;

import IllustrationShape.HShape;
import IllustrationShape.HShapeResizer;
import IllustrationShape.HView;
import IllustrationShape.model.DefaultViewModel;
import IllustrationShape.model.DefaultViewModelSelection;
import IllustrationShape.model.ListViewSelectionEvent;
import IllustrationShape.model.ListViewSelectionListener;
import IllustrationShape.model.ViewEvent;
import IllustrationShape.model.ViewModelListener;
import IllustrationShape.model.ViewModelSelection;

/**
 *
 * @author FIDELE
 */
public class HTextAreaShape extends HTextArea implements ViewModelListener, ListViewSelectionListener {
    
    private final DefaultViewModel viewModel = new DefaultViewModel();
    private final ViewModelSelection selectionModel = new DefaultViewModelSelection();
    private final HShapeResizer shapeResizer = new HShapeResizer();
    
    public HTextAreaShape() {
        installerGestionnaireDesFormes();
    }

    // À appeler dans chaque constructeur, après installerGestionnaireListeClavier() :
    private void installerGestionnaireDesFormes() {
        selectionModel.setSelectionMode(ViewModelSelection.MULTIPLE_SELECTION);
        viewModel.addListener(this);
        selectionModel.addListSelectionListener(this);
    }
    
    public void addShape(HView view) {
        viewModel.addView(view);
    }
    
    public DefaultViewModel getViewModel() {
        return viewModel;
    }
    
    public ViewModelSelection getSelectionModel() {
        return selectionModel;
    }
    
    public HShapeResizer getShapeResizer() {
        return shapeResizer;
    }
    
    @Override
    public void viewChanged(ViewEvent e) {
        repaint();
    }
    
    @Override
    public void valueChanged(ListViewSelectionEvent e) {
        HShape newlySelected = (selectionModel.getViewSelected() instanceof HShape s) ? s : null;
        shapeResizer.setTargetShape(newlySelected);
        
        if (e.getView() instanceof HShape shape) {
            shape.setSelected(selectionModel.isSelectedView(shape));
        }
        repaint();
    }
    
}
