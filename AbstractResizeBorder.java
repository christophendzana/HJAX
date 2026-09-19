/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package IllustrationShape;

import IllustrationShape.vues.border.AbstractViewBorder;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author FIDELE
 */
public abstract class AbstractResizeBorder extends AbstractViewBorder {

    protected static final int HANDLE_SIZE = 8;

    protected final HShape shape;

    private final List<ViewHandle> handles = new ArrayList<>();

    protected AbstractResizeBorder(HShape shape) {
        this.shape = Objects.requireNonNull(shape);
    }

    protected AbstractResizeBorder(HShape shape, List<ViewHandle> handles) {
        this.shape = Objects.requireNonNull(shape);
        setHandles(handles);
    }

    public HShape getShape() {
        return shape;
    }

    /**
     * Retourne la liste des poignées configurées.
     */
    public List<ViewHandle> getHandles() {
        return Collections.unmodifiableList(handles);
    }

    /**
     * Remplace toutes les poignées par une nouvelle liste.
     */
    public void setHandles(List<ViewHandle> handles) {
        this.handles.clear();

        if (handles != null) {
            for (ViewHandle handle : handles) {
                if (handle != null && !this.handles.contains(handle)) {
                    this.handles.add(handle);
                }
            }
        }
    }

    /**
     * Ajoute une poignée à la bordure.
     */
    public void addHandle(ViewHandle handle) {
        if (handle != null && !handles.contains(handle)) {
            handles.add(handle);
        }
    }

    /**
     * Supprime une poignée.
     */
    public boolean removeHandle(ViewHandle handle) {
        return handles.remove(handle);
    }

    /**
     * Supprime toutes les poignées.
     */
    public void clearHandles() {
        handles.clear();
    }

    /**
     * Recherche la poignée située sous les coordonnées souris.
     */
    public ViewHandle handleAt(int worldMx, int worldMy) {

        for (ViewHandle handle : handles) {
            if (handle.contains(shape, worldMx, worldMy)) {
                return handle;
            }
        }

        return null;
    }

    protected void paintHandles(
        Graphics2D g,
        int x,
        int y,
        int width,
        int height
) {
    for (ViewHandle handle : handles) {
        handle.paint(g, shape, x, y, width, height);
    }
}
    
}
