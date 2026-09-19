package hsplitpane;

import java.awt.Dimension;

/**
 * Config des tailles initiales des zones. La piste "config porte aussi le
 * contenu" (Component par zone) a été explorée puis écartée : le contenu se
 * câble désormais via HSplitPane.add(Component, ZonePosition), pas via cet
 * objet — un seul chemin pour ajouter du contenu, cohérent avec la façade
 * réduite.
 *
 * RAPPEL (TODO en attente) : getCenterSize()/setCenterSize() sont des méthodes
 * mortes — HSplitPaneRootLayout ne lit jamais cette valeur pour CENTER, et
 * HSplitZone.setInitialSize() lève désormais une exception pour CENTER. Pas
 * corrigé ici, à traiter séparément comme convenu.
 */
public class HSplitPaneConfig {

    private Integer northSize;

    private Integer southSize;

    private Integer westSize;

    private Integer eastSize;

    private Integer centerSize;

    private boolean showCenter = true;

    public Integer getNorthSize() {
        return northSize;
    }

    public void setNorthSize(Integer northSize) {
        this.northSize = northSize;
    }

    public Integer getSouthSize() {
        return southSize;
    }

    public void setSouthSize(Integer southSize) {
        this.southSize = southSize;
    }

    public Integer getWestSize() {
        return westSize;
    }

    public void setWestSize(Integer westSize) {
        this.westSize = westSize;
    }

    public Integer getEastSize() {
        return eastSize;
    }

    public void setEastSize(Integer eastSize) {
        this.eastSize = eastSize;
    }

    public Integer getCenterSize() {
        return centerSize;
    }

    public void setCenterSize(Integer centerSize) {
        this.centerSize = centerSize;
    }

    public boolean isShowCenter() {
        return showCenter;
    }

    public void setShowCenter(boolean showCenter) {
        this.showCenter = showCenter;
    }
}
