/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package htextarea;

import IllustrationShape.HShape;
import IllustrationShape.HShapeEditor;
import IllustrationShape.HShapeRenderer;
import IllustrationShape.HShapeResizer;
import IllustrationShape.HView;
import IllustrationShape.model.DefaultViewModel;
import IllustrationShape.model.DefaultViewModelSelection;
import IllustrationShape.model.ListViewSelectionEvent;
import IllustrationShape.model.ListViewSelectionListener;
import IllustrationShape.model.ViewEvent;
import IllustrationShape.model.ViewModelListener;
import IllustrationShape.model.ViewModelSelection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author FIDELE
 */
public class HTextAreaShape extends HTextArea implements ViewModelListener, ListViewSelectionListener {

    private final DefaultViewModel viewModel = new DefaultViewModel();
    private final ViewModelSelection selectionModel = new DefaultViewModelSelection();
    private final HShapeResizer shapeResizer = new HShapeResizer();
    private final Map<HShape, HShapeResizer> shapeResizers = new LinkedHashMap<>();

    private final HShapeRenderer shapeRenderer = new HShapeRenderer();
    private final HShapeEditor shapeEditor = new HShapeEditor();

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

    public HShapeResizer getShapeResizer() {
        return shapeResizer;
    }

    public Collection<HShapeResizer> getShapeResizers() {
        return shapeResizers.values();
    }

    @Override
    public void viewChanged(ViewEvent e) {
        repaint();
    }

    @Override
    public void valueChanged(ListViewSelectionEvent e) {
        List<HView> currentlySelected = selectionModel.getListViewSelected();

        for (HShape shape : new ArrayList<>(shapeResizers.keySet())) {
            if (!currentlySelected.contains(shape)) {
                shape.setSelected(false);
                shapeResizers.remove(shape);
            }
        }

        for (HView view : currentlySelected) {
            if (view instanceof HShape shape && !shapeResizers.containsKey(shape)) {
                shape.setSelected(true);
                HShapeResizer resizer = new HShapeResizer();
                resizer.setTargetShape(shape);
                shapeResizers.put(shape, resizer);
            }
        }

        repaint();
    }

    public HShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public HShapeEditor getShapeEditor() {
        return shapeEditor;
    }

}
