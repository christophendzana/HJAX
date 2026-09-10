/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package htextarea;

import IllustrationShape.HShape;
import IllustrationShape.HShapeRenderer;
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

    private final HShapeRenderer shapeRenderer = new HShapeRenderer();

    public HTextAreaShape() {
        installerGestionnaireDesFormes();
    }

    // À appeler dans chaque constructeur, après installerGestionnaireListeClavier() :
    private void installerGestionnaireDesFormes() {
        selectionModel.setSelectionMode(ViewModelSelection.SINGLE_SELECTION);
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
  
    @Override
    public void viewChanged(ViewEvent e) {
        repaint();
    }

    @Override
    public void valueChanged(ListViewSelectionEvent e) {
        for (HShape shape : viewModel.getShapes()) {
            shape.setSelected(selectionModel.isSelectedView(shape));
        }
        repaint();
    }

    public HShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    

}
