package hsplitpane;

import hcomponents.HDialog;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Non modifiée dans cette refonte — hors périmètre des 4 décisions validées.
 *
 * Recommandation de suite (non appliquée) : mettreAJourTitre() retrouve le
 * JLabel du titre en itérant sur les enfants de getHeaderPanel() et en testant
 * "instanceof JLabel". C'est une dépendance à la STRUCTURE INTERNE de HDialog
 * plutôt qu'à un contrat public — si HDialog change l'implémentation de son
 * header, ce code casse silencieusement (pas d'erreur de compilation, juste
 * un titre qui ne se met plus à jour). Si HDialog expose un jour une méthode
 * setHeaderTitle() ou équivalent, ce serait préférable à cette introspection.
 */
public class HSplitFloatDialog extends HDialog {

    private final String titreZone;
    private Runnable onFermetureCallback;

    private boolean isClosing = false;

    public HSplitFloatDialog(String titreZone) {
        super();
        this.titreZone = titreZone != null ? titreZone : "Zone flottante";

        configurerDialog();
        brancherEcouteurFermeture();
    }

    private void configurerDialog() {
        setModal(false);
        setOverlayOpacity(0.0f);
        setCloseOnOverlayClick(false);
        setShowFooter(false);
        setTitle(titreZone);

        mettreAJourTitre();

        setDialogSize(600, 400);
        setResizable(true);

        ajouterDragSurHeader();
    }

    private void mettreAJourTitre() {
        JPanel header = getHeaderPanel();
        if (header != null) {
            java.awt.Component[] components = header.getComponents();
            for (java.awt.Component comp : components) {
                if (comp instanceof javax.swing.JLabel) {
                    ((javax.swing.JLabel) comp).setText(titreZone);
                    break;
                }
            }
        }
    }

    private void ajouterDragSurHeader() {
        JPanel header = getHeaderPanel();
        if (header == null) {
            return;
        }

        final Point[] pointDepart = {null};

        MouseAdapter dragAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pointDepart[0] = new Point(e.getLocationOnScreen());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (pointDepart[0] == null) {
                    return;
                }

                Point posActuelle = e.getLocationOnScreen();
                Point posDialog = getLocation();

                int deltaX = posActuelle.x - pointDepart[0].x;
                int deltaY = posActuelle.y - pointDepart[0].y;

                setLocation(posDialog.x + deltaX, posDialog.y + deltaY);

                pointDepart[0] = new Point(posActuelle);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pointDepart[0] = null;
            }
        };

        header.addMouseListener(dragAdapter);
        header.addMouseMotionListener(dragAdapter);
    }

    private void brancherEcouteurFermeture() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                declencherFermeture();
            }
        });
    }

    @Override
    public void closeWithAnimation() {
        declencherFermeture();
    }

    private void declencherFermeture() {

        if (isClosing) {
            return;
        }
        isClosing = true;

        if (onFermetureCallback != null) {
            Runnable callback = onFermetureCallback;
            onFermetureCallback = null;
            callback.run();
        }

        super.closeWithAnimation();
    }

    public void setContenu(JComponent composant) {
        setContent(composant);
    }

    public void setOnFermetureCallback(Runnable callback) {
        this.onFermetureCallback = callback;
    }

    public void afficher() {
        isClosing = false;
        showWithAnimation();
    }

    public void fermer() {
        if (isClosing) {
            return;
        }
        isClosing = true;
        super.closeWithAnimation();
    }

    public String getTitreZone() {
        return titreZone;
    }
}
