/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HComboBoxStyle;
import hcomponents.vues.HMenuStyle;
import hcomponents.vues.HSliderStyle;
import hcomponents.vues.HTabbedPaneStyle;
import hcomponents.vues.HWindowStyle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * @author FIDELE
 */
public class Components {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

            ImageIcon open = new ImageIcon("open.png");
            ImageIcon folder = new ImageIcon("folder.png");
            ImageIcon paste = new ImageIcon("paste.png");
            ImageIcon save = new ImageIcon("save.png");
            ImageIcon copy = new ImageIcon("copy.png");
            ImageIcon cut = new ImageIcon("cut.png");
            ImageIcon close = new ImageIcon("close.png");           
                 
            
            // TEST HWINdow ========================================================================
            
            
             SwingUtilities.invokeLater(() -> {
            // Fenêtre propriétaire (exemple) — utile pour positionnement relatif
            JFrame owner = new JFrame("Fenêtre propriétaire / Demo");
            owner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            owner.setSize(900, 600);
            owner.setLocationRelativeTo(null);
            owner.setLayout(new FlowLayout());

            // Boutons pour déclencher chaque démonstration manuellement
            HButton btnSplash = new HButton("Afficher Splash");
            HButton btnNotification = new HButton("Afficher Notification");
            HButton btnTooltip = new HButton("Afficher Tooltip");
            HButton btnPopup = new HButton("Afficher Popup");
            HButton btnConfirm = new HButton("Afficher Confirm");
            HButton btnLoading = new HButton("Afficher Loading");
            HButton btnDraggable = new HButton("Fenêtre Draggable");
            HButton btnOpacity = new HButton("Changer Opacité + Fade");
            owner.add(btnSplash);
            owner.add(btnNotification);
            owner.add(btnTooltip);
            owner.add(btnPopup);
            owner.add(btnConfirm);
            owner.add(btnLoading);
            owner.add(btnDraggable);
            owner.add(btnOpacity);

            owner.setVisible(true);

            // ---------------------------
            // 1) Splash screen automatique (exécuté une fois au démarrage)
            // ---------------------------
            HWindow splash = new HWindow()
                .asSplashScreen("Mon Application", "Initialisation en cours…", true)
                .setCornerRadius(24)
                .setWindowOpacity(0.97f)
                .centerOnScreen();

            // Apparition avec fondu + fermeture automatique avec fondu
            splash.showWithFadeIn(600)
                  .autoClose(1800, true); // ferme après ~1.8s avec fondu sortant

            // ---------------------------
            // 2) Notification (exemple déclenché par bouton)
            // ---------------------------
            btnNotification.addActionListener(e -> {
                HWindow notif = new HWindow()
                    .asNotification("Sauvegarde terminée", "Tout s'est bien passé.", HWindowStyle.PRIMARY, true)
                    .setCornerRadius(14)
                    .setWindowOpacity(0.95f)
                    .positionBottomRight(20, 60);
                notif.showWithFadeIn(350)
                     .autoClose(4000, true); // auto-close avec fondu
            });

            // ---------------------------
            // 3) Tooltip personnalisé (position près du curseur)
            // ---------------------------
            btnTooltip.addActionListener(e -> {
                Point p = MouseInfo.getPointerInfo().getLocation();
                HWindow tooltip = new HWindow()
                    .asTooltip("<html><b>Astuce :</b> Ceci est un tooltip HTML</html>")
                    .setCornerRadius(8)
                    .setWindowOpacity(0.98f);
                tooltip.positionAt(p.x + 15, p.y + 15)
                       .showWithFadeIn(200)
                       .autoClose(2000, true);
            });

            // ---------------------------
            // 4) Popup centré avec contenu personnalisé
            // ---------------------------
            btnPopup.addActionListener(e -> {
                JPanel panel = new JPanel(new BorderLayout(8, 8));
                panel.setOpaque(false);
                JLabel content = new JLabel("<html>Voici un <b>popup</b> central avec du contenu personnalisé.</html>");
                content.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                panel.add(content, BorderLayout.CENTER);

                HWindow popup = new HWindow()
                    .asPopup("Popup Exemple", panel, 420, 220)
                    .setCornerRadius(18)
                    .setWindowOpacity(0.98f)
                    .centerOnScreen();
                popup.showWithFadeIn(350);
            });

            // ---------------------------
            // 5) Confirm dialog (avec callbacks)
            // ---------------------------
            btnConfirm.addActionListener(e -> {
                HWindow confirm = new HWindow()
                    .asConfirmDialog(
                        "Supprimer élément",
                        "Voulez-vous vraiment supprimer cet élément ? Cette action est irréversible.",
                        () -> {
                            // On confirme -> exemple d'action
                            // Afficher une petite notification pour feedback
                            new HWindow()
                                .asNotification("Suppression", "L'élément a été supprimé.", HWindowStyle.SUCCESS, true)
                                .positionBottomRight(20, 80)
                                .showWithFadeIn(300)
                                .autoClose(2500, true);
                        },
                        () -> System.out.println("Utilisateur a annulé l'action.")
                    )
                    .setCornerRadius(16)
                    .centerOnScreen();
                confirm.showWithFadeIn(250);
            });

            // ---------------------------
            // 6) Loading screen (ex: pendant une opération)
            // ---------------------------
            btnLoading.addActionListener(e -> {
                HWindow loading = new HWindow()
                    .asLoadingScreen("Chargement des données…")
                    .setCornerRadius(18)
                    .centerOnScreen();
                loading.showWithFadeIn(200);

                // Simulation d'une tâche en arrière-plan -> fermeture après 2s
                new Timer(2000, evt -> loading.hideWithFadeOut(350)).start();
            });

            // ---------------------------
            // 7) Fenêtre draggable + custom position + factory helper
            // ---------------------------
            btnDraggable.addActionListener(e -> {
                // Utilisation de la factory withStyle
                HWindow dragWin = HWindow.withStyle(owner, HWindowStyle.PRIMARY)
                    .asPopup("Fenêtre déplaçable", new JPanel(), 300, 140)
                    .setCornerRadius(14)
                    .setDraggable(true)       // permet le drag de la fenêtre
                    .setWindowOpacity(0.98f)
                    .positionAt(200, 150);

                // Contenu simple avec bouton pour fermer
                JPanel content = new JPanel(new FlowLayout(FlowLayout.CENTER));
                content.setOpaque(false);
                HButton close2 = new HButton("Fermer");
                close2.addActionListener(a -> dragWin.hideWithFadeOut(200));
                content.add(new JLabel("Glisser la fenêtre par n'importe où."), null);
                content.add(close2, null);

                dragWin.setContentPane(content);
                dragWin.showWithFadeIn(250);
            });

            // ---------------------------
            // 8) Opacité et animations manuelles
            // ---------------------------
            btnOpacity.addActionListener(e -> {
                HWindow w = new HWindow()
                    .asNotification("Réglage d'opacité", "Démonstration fondu / opacité", HWindowStyle.INFO, true)
                    .setCornerRadius(12)
                    .setWindowOpacity(0.85f)
                    .positionBottomLeft(20, 60);
                w.showWithFadeIn(400);

                // Après 2s : réduire l'opacité, puis faire un fade out
                new Timer(2000, evt -> {
                    w.setWindowOpacity(0.5f); // change l'opacité sans animation
                    // Puis fade out avec la méthode dédiée
                    w.hideWithFadeOut(600);
                }).setRepeats(false);
                new Timer(2000, evt -> ((Timer)evt.getSource()).stop()).start();
            });

            // ---------------------------
            // BONUS : Exemples d'utilisation de shadow / border si disponibles
            // ---------------------------
            // Si votre HShadow et HBorder disposent des constructeurs suivants, vous pouvez
            // activer l'ombre et la bordure comme ceci (décommenter si compatibles) :
            /*
            HWindow shadowDemo = new HWindow()
                .asNotification("Ombre & Bordure", "Exemple d'ombre et bordure personnalisée", HWindowStyle.PRIMARY, true)
                .setCornerRadius(16)
                .centerOnScreen();

            // Exemple supposé de constructeur : HShadow(Color, int blurRadius, int offsetX, int offsetY)
            shadowDemo.setShadow(new HShadow(new Color(0,0,0,100), 16, 0, 6));

            // Exemple supposé de constructeur concret HBorder(...) ou HAbstractBorder concrète
            // shadowDemo.setHBorder(new HBorder(Color.GRAY, 2, 10)); // adapter selon implémentation
            shadowDemo.showWithFadeIn(300).autoClose(3500, true);
            */

            // ---------------------------
            // FIN
            // ---------------------------
            // Note : vous pouvez aussi appeler directement `new HWindow().asTooltip(...).showWithFadeIn(...)`
            // ou utiliser les méthodes factory HWindow.withStyle(...) selon vos besoins.
        });
            
            
            // ======================================================================================
            
//             new FormulaireEmail().setVisible(true);            
            
            // TEST HDESKTOPPANE ====================================================================

//            SwingUtilities.invokeLater(() -> {
////             Création du JFrame principal
//            JFrame frame = new JFrame("Test HDesktopPane");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(1100, 700);
//            frame.setLocationRelativeTo(null);
//
////             === HDesktopPane avec motif grille ===
//            HDesktopPane desktop = HDesktopPane.withStyleAndPattern(
//                HDesktopPaneStyle.LIGHT, 
//                HDesktopPane.PatternType.GRID
//            );
//            desktop.setCornerRadius(0); // Pas de coins arrondis pour remplir le frame
//            desktop.setPatternSpacing(40);
//            desktop.setPatternColor(new Color(0, 0, 0, 15));
//            
//            frame.setContentPane(desktop);
//
////             === FENÊTRE INTERNE 1 - PRIMARY ===
//            HInternalFrame frame1 = HInternalFrame.withStyle("Document 1", HInternalFrameStyle.PRIMARY);
//            frame1.setSize(450, 350);
//            frame1.setLocation(50, 50);
//            
//            JPanel content1 = new JPanel(new BorderLayout());
//            content1.setBackground(Color.WHITE);
//            content1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//            
//            JTextArea textArea1 = new JTextArea(
//                "HDesktopPane Moderne\n\n" +
//                "✓ Fond personnalisé avec dégradé\n" +
//                "✓ Motif de grille ou points\n" +
//                "✓ Coins arrondis\n" +
//                "✓ Compatible avec HInternalFrame\n\n" +
//                "Essayez de déplacer et redimensionner\n" +
//                "les fenêtres !"
//            );
//            textArea1.setEditable(false);
//            textArea1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//            textArea1.setOpaque(false);
//            content1.add(textArea1, BorderLayout.CENTER);
//            
//            frame1.setContentPane(content1);
//            frame1.setVisible(true);
//            desktop.add(frame1);
//
////             === FENÊTRE INTERNE 2 - DARK ===
//            HInternalFrame frame2 = HInternalFrame.withStyle("Fenêtre Sombre", HInternalFrameStyle.DARK);
//            frame2.setSize(400, 280);
//            frame2.setLocation(530, 50);
//            
//            JPanel content2 = new JPanel(new GridBagLayout());
//            content2.setBackground(new Color(52, 58, 64));
//            
//            JLabel label2 = new JLabel(
//                "<html><div style='color: white; text-align: center;'>" +
//                "<h2>Mode Sombre</h2>" +
//                "<p>Design élégant avec motif de fond</p>" +
//                "<p>personnalisable</p>" +
//                "</div></html>"
//            );
//            content2.add(label2);
//            
//            frame2.setContentPane(content2);
//            frame2.setVisible(true);
//            desktop.add(frame2);
//
////             === FENÊTRE INTERNE 3 - SUCCESS ===
//            HInternalFrame frame3 = HInternalFrame.withStyle("Configuration", HInternalFrameStyle.SUCCESS);
//            frame3.setSize(380, 250);
//            frame3.setLocation(50, 430);
//            
//            JPanel content3 = new JPanel(new BorderLayout());
//            content3.setBackground(Color.WHITE);   
//            content3.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
//            
//            JPanel settingsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
//            settingsPanel.setOpaque(false);
//            
//            JCheckBox cb1 = new JCheckBox("Afficher la grille", true);
//            cb1.addActionListener(e -> {
//                desktop.setShowPattern(cb1.isSelected());
//            });
//            
//            JCheckBox cb2 = new JCheckBox("Utiliser le dégradé", true);
//            cb2.addActionListener(e -> {
//                desktop.setUseGradient(cb2.isSelected());
//            });
//            
//            HButton btnChangePattern = new HButton("Changer le motif");
//            btnChangePattern.addActionListener(e -> {
//                if (desktop.getPatternType() == HDesktopPane.PatternType.GRID) {
//                    desktop.setPatternType(HDesktopPane.PatternType.DOTS);
//                } else {
//                    desktop.setPatternType(HDesktopPane.PatternType.GRID);
//                }
//            });
//            
//            settingsPanel.add(cb1);
//            settingsPanel.add(cb2);
//            settingsPanel.add(btnChangePattern);
//            
//            content3.add(settingsPanel, BorderLayout.CENTER);
//            
//            frame3.setContentPane(content3);
//            frame3.setVisible(true);
//            desktop.add(frame3);
//
////             === FENÊTRE INTERNE 4 - INFO ===
//            HInternalFrame frame4 = HInternalFrame.withStyle("Informations", HInternalFrameStyle.INFO);
//            frame4.setSize(320, 200);
//            frame4.setLocation(460, 360);
//            
//            JPanel content4 = new JPanel(new BorderLayout());
//            content4.setBackground(Color.WHITE);
//            content4.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
//            
//            JLabel label4 = new JLabel(
//                "<html><div style='text-align: center;'>" +
//                "<h3>💡 Astuce</h3>" +
//                "<p>Utilisez les contrôles pour</p>" +
//                "<p>personnaliser le fond en temps réel</p>" +
//                "</div></html>",
//                SwingConstants.CENTER
//            );
//            content4.add(label4, BorderLayout.CENTER);
//            
//            frame4.setContentPane(content4);
//            frame4.setVisible(true);
//            desktop.add(frame4);
//
//            frame.setVisible(true);
//        });
//            
            
            // TEST HITERNALFRAME ============================================================================
            
//            SwingUtilities.invokeLater(() -> {
////             Création du JFrame principal
//            HFrame frame = new HFrame("Test HInternalFrame");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(1000, 700);
//            frame.setLocationRelativeTo(null);
//
////          Création du JDesktopPane (conteneur pour les fenêtres internes)
//            JDesktopPane desktop = new JDesktopPane();
//            desktop.setBackground(new Color(240, 242, 245));
//            frame.setContentPane(desktop);
//
////             === FENÊTRE INTERNE 1 - Style PRIMARY ===
//            HInternalFrame internalFrame1 = HInternalFrame.withStyle("Fenêtre PRIMARY", HInternalFrameStyle.INFO);
//            internalFrame1.setSize(400, 300);
//            internalFrame1.setLocation(50, 50);
//            
//            JPanel content1 = new JPanel(new BorderLayout());
//            content1.setBackground(Color.WHITE);
//            content1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//            
//            JLabel label1 = new JLabel(
//                "<html><h2>Fenêtre Interne Moderne</h2>" +
//                "<p>✓ Barre de titre stylée</p>" +
//                "<p>✓ Boutons HButton personnalisés</p>" +
//                "<p>✓ Coins arrondis</p>" +
//                "<p>✓ Déplaçable et redimensionnable</p></html>"
//            );
//            content1.add(label1, BorderLayout.CENTER);
//            
//            internalFrame1.setContentPane(content1);
//            internalFrame1.setVisible(true);
//            desktop.add(internalFrame1);
//
////             === FENÊTRE INTERNE 2 - Style DARK ===
//            HInternalFrame internalFrame2 = HInternalFrame.withStyle("Fenêtre DARK", HInternalFrameStyle.DARK);
//            internalFrame2.setSize(350, 250);
//            internalFrame2.setLocation(480, 50);
//            
//            JPanel content2 = new JPanel(new GridBagLayout());
//            content2.setBackground(new Color(52, 58, 64));
//            
//            JLabel label2 = new JLabel(
//                "<html><div style='color: white; text-align: center;'>" +
//                "<h3>Mode Sombre</h3>" +
//                "<p>Design élégant et moderne</p>" +
//                "</div></html>"
//            );
//            label2.setForeground(Color.WHITE);
//            content2.add(label2);
//            
//            internalFrame2.setContentPane(content2);
//            internalFrame2.setVisible(true);
//            desktop.add(internalFrame2);
//
////             === FENÊTRE INTERNE 3 - Style SUCCESS ===
//            HInternalFrame internalFrame3 = HInternalFrame.withStyle("Fenêtre SUCCESS", HInternalFrameStyle.SUCCESS);
//            internalFrame3.setSize(400, 200);
//            internalFrame3.setLocation(50, 380);
//            
//            JPanel content3 = new JPanel(new BorderLayout());
//            content3.setBackground(Color.WHITE);
//            content3.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
//            
//            JLabel label3 = new JLabel(
//                "<html><div style='text-align: center;'>" +
//                "<h3 style='color: #198754;'>✓ Opération Réussie</h3>" +
//                "<p>Double-cliquez sur la barre de titre pour maximiser</p>" +
//                "</div></html>",
//                SwingConstants.CENTER
//            );
//            content3.add(label3, BorderLayout.CENTER);
//            
//            internalFrame3.setContentPane(content3);
//            internalFrame3.setVisible(true);
//            desktop.add(internalFrame3);
//
//            frame.setVisible(true);
//        });
            
            
            // TEST HTOOLBAR ========================================================================================
            
//            SwingUtilities.invokeLater(() -> {
//            // Création du HFrame
//            HFrame frame = new HFrame("Test HToolBar");
//            frame.setFrameStyle(HFrameStyle.LIGHT);
//            frame.setSize(900, 600);
//            frame.setLocationRelativeTo(null);
//
//            // Panel principal
//            JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
//            mainPanel.setOpaque(false);
//
//            // === TOOLBAR HORIZONTALE EN HAUT ===
//            HToolBar topToolBar = new HToolBar();
//            topToolBar.setToolBarStyle(HToolBarStyle.PRIMARY);
//            topToolBar.setToolBarHeight(55);
//
//            // Boutons fichier
//            HButton btnNew = HButton.withStyle("Nouveau", HButtonStyle.LIGHT);
//            HButton btnOpen = HButton.withStyle("Ouvrir", HButtonStyle.LIGHT);
//            HButton btnSave = HButton.withStyle("Enregistrer", HButtonStyle.SUCCESS);
//            
//            topToolBar.add(btnNew);
//            topToolBar.add(btnOpen);
//            topToolBar.add(btnSave);
//            topToolBar.addSeparator();
//
//            // Boutons édition
//            HButton btnCut = HButton.withStyle("Couper", HButtonStyle.LIGHT);
//            HButton btnCopy = HButton.withStyle("Copier", HButtonStyle.LIGHT);
//            HButton btnPaste = HButton.withStyle("Coller", HButtonStyle.LIGHT);
//            
//            topToolBar.add(btnCut);
//            topToolBar.add(btnCopy);
//            topToolBar.add(btnPaste);
//            topToolBar.addSeparator();
//
//            // Bouton avec espace flexible (pousse vers la droite)
//            topToolBar.addGlue();
//            
//            HButton btnSettings = HButton.withStyle("Paramètres", HButtonStyle.SECONDARY);
//            topToolBar.add(btnSettings);
//
//            mainPanel.add(topToolBar, BorderLayout.NORTH);
//
//            // === TOOLBAR VERTICALE À GAUCHE ===
//            HToolBar leftToolBar = new HToolBar(JToolBar.VERTICAL);
//            leftToolBar.setToolBarStyle(HToolBarStyle.DARK);
//            leftToolBar.setToolBarWidth(70);
//
//            HButton btnHome = new HButton("🏠");
//            btnHome.setButtonStyle(HButtonStyle.DARK);
//            btnHome.setPreferredSize(new Dimension(50, 50));
//            btnHome.setToolTipText("Accueil");
//
//            HButton btnSearch = new HButton("🔍");
//            btnSearch.setButtonStyle(HButtonStyle.DARK);
//            btnSearch.setPreferredSize(new Dimension(50, 50));
//            btnSearch.setToolTipText("Rechercher");
//
//            HButton btnSettings2 = new HButton("⚙");
//            btnSettings2.setButtonStyle(HButtonStyle.DARK);
//            btnSettings2.setPreferredSize(new Dimension(50, 50));
//            btnSettings2.setToolTipText("Paramètres");
//
//            leftToolBar.add(btnHome);
//            leftToolBar.add(btnSearch);
//            leftToolBar.addSeparator();
//            leftToolBar.add(btnSettings2);
//
//            mainPanel.add(leftToolBar, BorderLayout.WEST);
//
//            // === CONTENU CENTRAL ===
//            JPanel centerPanel = new JPanel(new GridBagLayout());
//            centerPanel.setOpaque(false);
//
//            JLabel label = new JLabel(
//                "<html><div style='text-align: center;'>" +
//                "<h1>HToolBar Moderne</h1>" +
//                "<p>✓ Toolbar horizontale en haut</p>" +
//                "<p>✓ Toolbar verticale à gauche</p>" +
//                "<p>✓ Séparateurs stylés</p>" +
//                "<p>✓ Support des HButton</p>" +
//                "<p>✓ Coins arrondis et dégradés</p>" +
//                "</div></html>",
//                SwingConstants.CENTER
//            );
//            label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
//            centerPanel.add(label);
//
//            mainPanel.add(centerPanel, BorderLayout.CENTER);
//
//            // Ajout au frame
//            frame.setContent(mainPanel);
//            frame.setVisible(true);
//        });
            
            
            // Test HFRAME ===========================================================================================
            
//            SwingUtilities.invokeLater(() -> {
//            // Création du HFrame avec style PRIMARY
//            HFrame frame = new HFrame("Ma Fenêtre Moderne");
//            frame.setResizable(true);
//            frame.setFrameStyle(HFrameStyle.PRIMARY);            
//            frame.setSize(900, 600);
//            frame.setLocationRelativeTo(null);
//
//            // Panel de contenu avec démo
//            JPanel content = new JPanel(new BorderLayout(20, 20));
//            content.setOpaque(false);
//            content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
//
//            // Titre de bienvenue
//            JLabel welcomeLabel = new JLabel(
//                "<html><div style='text-align: center;'>" +
//                "<h1>Bienvenue dans HFrame</h1>" +
//                "<p>Une fenêtre moderne avec barre de titre personnalisée</p>" +
//                "</div></html>",
//                SwingConstants.CENTER
//            );
//            welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
//            content.add(welcomeLabel, BorderLayout.NORTH);
//
//            // Panel central avec features
//            JPanel featuresPanel = new JPanel(new GridLayout(2, 2, 20, 20));
//            featuresPanel.setOpaque(false);
//
//            featuresPanel.add(createFeatureCard("✓ Coins Arrondis", "Design moderne et élégant"));
//            featuresPanel.add(createFeatureCard("✓ Draggable", "Déplacez la fenêtre par la barre de titre"));
//            featuresPanel.add(createFeatureCard("✓ Boutons HButton", "Minimiser, Maximiser, Fermer stylés"));
//            featuresPanel.add(createFeatureCard("✓ Styles Prédéfinis", "PRIMARY, DARK, LIGHT, etc."));
//
//            content.add(featuresPanel, BorderLayout.CENTER);
//
//            // Panel de boutons de test
//            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
//            buttonPanel.setOpaque(false);
//
//            HButton btnPrimary = HButton.withStyle("Style PRIMARY", HButtonStyle.PRIMARY);
//            btnPrimary.addActionListener(e -> frame.setFrameStyle(HFrameStyle.PRIMARY));
//
//            HButton btnDark = HButton.withStyle("Style DARK", HButtonStyle.DARK);
//            btnDark.addActionListener(e -> frame.setFrameStyle(HFrameStyle.DARK));
//
//            HButton btnSuccess = HButton.withStyle("Style SUCCESS", HButtonStyle.SUCCESS);
//            btnSuccess.addActionListener(e -> frame.setFrameStyle(HFrameStyle.SUCCESS));
//
//            buttonPanel.add(btnPrimary);
//            buttonPanel.add(btnDark);
//            buttonPanel.add(btnSuccess);
//
//            content.add(buttonPanel, BorderLayout.SOUTH);
//
//            // Ajout du contenu au frame
//            frame.setContent(content);
//            frame.setVisible(true);
//        });
            
            
            // TEST HDIALOG ================================================================================
            
//            SwingUtilities.invokeLater(() -> {
//            // Création de la fenêtre principale
//            JFrame frame = new JFrame("Test HDialog");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(800, 600);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//
//            // Création du dialog avec style PRIMARY
//            HDialog dialog = new HDialog(frame, "Mon Premier Dialog");
//            dialog.setDialogStyle(HDialogStyle.PRIMARY);
//            dialog.setDialogSize(450, 300);
//
//            // Contenu du dialog
//            JPanel content = new JPanel(new BorderLayout());
//            content.setOpaque(false);
//            content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//
//            JLabel message = new JLabel(
//                "<html><div style='text-align: center;'>" +
//                "Bienvenue dans HDialog !<br><br>" +
//                "Un composant moderne avec :<br>" +
//                "✓ Coins arrondis<br>" +
//                "✓ Animations fluides<br>" +
//                "✓ Overlay semi-transparent<br>" +
//                "✓ Styles personnalisables" +
//                "</div></html>"
//            );
//            message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//            message.setForeground(Color.WHITE);
//            message.setHorizontalAlignment(SwingConstants.CENTER);
//            content.add(message, BorderLayout.CENTER);
//
//            dialog.setContent(content);
//
//            // Boutons du footer
//            HButton btnOk = HButton.withStyle("OK", HButtonStyle.SUCCESS);
//            btnOk.addActionListener(e -> {
//                System.out.println("Bouton OK cliqué");
//                dialog.closeWithAnimation();
//            });
//
//            HButton btnCancel = HButton.withStyle("Annuler", HButtonStyle.SECONDARY);
//            btnCancel.addActionListener(e -> {
//                System.out.println("Bouton Annuler cliqué");
//                dialog.closeWithAnimation();
//            });
//
//            dialog.addFooterButton(btnCancel);
//            dialog.addFooterButton(btnOk);
//
//            // Affichage avec animation
//            dialog.showWithAnimation();
//        });
            
            // TEST HSPINNER ===============================================================================
            
//                SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test HSpinner");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(700, 600);
//            frame.setLocationRelativeTo(null);
//            
//            JPanel panel = new JPanel();
//            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//            panel.setBackground(new Color(245, 245, 245));
//            panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
//            
//            // Spinner nombres PRIMARY
//            JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
//            row1.setBackground(new Color(245, 245, 245));
//            row1.setMaximumSize(new Dimension(600, 60));
//            row1.add(new JLabel("Quantité (PRIMARY):"));
//            HSpinner spinner1 = HSpinner.withStyle(new SpinnerNumberModel(5, 0, 100, 1), HSpinnerStyle.PRIMARY);
//            spinner1.setPreferredSize(new Dimension(120, 40));
//            row1.add(spinner1);
//            panel.add(row1);
//            panel.add(Box.createVerticalStrut(10));
//            
//            // Spinner nombres SUCCESS
//            JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
//            row2.setBackground(new Color(245, 245, 245));
//            row2.setMaximumSize(new Dimension(600, 60));
//            row2.add(new JLabel("Prix (SUCCESS):"));
//            HSpinner spinner2 = HSpinner.withStyle(new SpinnerNumberModel(50.0, 0.0, 1000.0, 5.0), HSpinnerStyle.SUCCESS);
//            spinner2.setPreferredSize(new Dimension(120, 40));
//            row2.add(spinner2);
//            panel.add(row2);
//            panel.add(Box.createVerticalStrut(10));
//            
//            // Spinner liste DANGER
//            JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
//            row3.setBackground(new Color(245, 245, 245));
//            row3.setMaximumSize(new Dimension(600, 60));
//            row3.add(new JLabel("Taille (DANGER):"));
//            HSpinner spinner3 = HSpinner.withStyle(
//                new SpinnerListModel(new String[]{"XS", "S", "M", "L", "XL", "XXL"}),
//                HSpinnerStyle.DANGER
//            );
//            spinner3.setPreferredSize(new Dimension(120, 40));
//            row3.add(spinner3);
//            panel.add(row3);
//            panel.add(Box.createVerticalStrut(10));
//            
//            // Spinner date WARNING
//            JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
//            row4.setBackground(new Color(245, 245, 245));
//            row4.setMaximumSize(new Dimension(600, 60));
//            row4.add(new JLabel("Date (WARNING):"));
//            HSpinner spinner4 = HSpinner.withStyle(
//                new SpinnerDateModel(),
//                HSpinnerStyle.WARNING
//            );
//            spinner4.setEditor(new JSpinner.DateEditor(spinner4, "dd/MM/yyyy"));
//            spinner4.setPreferredSize(new Dimension(150, 40));
//            row4.add(spinner4);
//            panel.add(row4);
//            panel.add(Box.createVerticalStrut(10));
//            
//            // Spinner INFO
//            JPanel row5 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
//            row5.setBackground(new Color(245, 245, 245));
//            row5.setMaximumSize(new Dimension(600, 60));
//            row5.add(new JLabel("Valeur (INFO):"));
//            HSpinner spinner5 = HSpinner.withStyle(new SpinnerNumberModel(10, -50, 50, 2), HSpinnerStyle.INFO);
//            spinner5.setPreferredSize(new Dimension(120, 40));
//            row5.add(spinner5);
//            panel.add(row5);
//            panel.add(Box.createVerticalStrut(10));
//            
//            // Spinner PURPLE
//            JPanel row6 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
//            row6.setBackground(new Color(245, 245, 245));
//            row6.setMaximumSize(new Dimension(600, 60));
//            row6.add(new JLabel("Niveau (PURPLE):"));
//            HSpinner spinner6 = HSpinner.withStyle(new SpinnerNumberModel(1, 1, 10, 1), HSpinnerStyle.PURPLE);
//            spinner6.setPreferredSize(new Dimension(120, 40));
//            row6.add(spinner6);
//            panel.add(row6);
//            panel.add(Box.createVerticalStrut(10));
//            
//            // Spinner OCEAN
//            JPanel row7 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
//            row7.setBackground(new Color(245, 245, 245));
//            row7.setMaximumSize(new Dimension(600, 60));
//            row7.add(new JLabel("Compteur (OCEAN):"));
//            HSpinner spinner7 = HSpinner.withStyle(new SpinnerNumberModel(100, 0, 999, 10), HSpinnerStyle.OCEAN);
//            spinner7.setPreferredSize(new Dimension(120, 40));
//            row7.add(spinner7);
//            panel.add(row7);
//            
//            frame.add(panel);
//            frame.setVisible(true);
//        });
            
            //TEST HSCROLLPANE ==============================================================================
            
//            SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test HScrollPane");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(900, 700);
//            frame.setLocationRelativeTo(null);
//            
//            JPanel mainPanel = new JPanel();
//            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//            mainPanel.setBackground(new Color(245, 245, 245));
//            mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
//            
//            // 1. HScrollPane avec texte PRIMARY
//            addLabel(mainPanel, "1. HScrollPane avec texte (PRIMARY)");
//            
//            JTextArea text1 = new JTextArea(8, 50);
//            text1.setText("Ceci est un exemple de HScrollPane avec style PRIMARY.\n\n" +
//                         "Le HScrollPane intègre automatiquement des HScrollBar modernes.\n" +
//                         "Les scrollbars ont des animations fluides au survol.\n\n" +
//                         "Ligne 5\nLigne 6\nLigne 7\nLigne 8\nLigne 9\nLigne 10\n" +
//                         "Ligne 11\nLigne 12\nLigne 13\nLigne 14\nLigne 15");
//            text1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//            text1.setLineWrap(true);
//            text1.setWrapStyleWord(true);
//            
//            HScrollPane scroll1 = HScrollPane.withStyle(text1, HScrollBarStyle.PRIMARY);
//            scroll1.setPreferredSize(new Dimension(800, 180));
//            scroll1.setMaximumSize(new Dimension(800, 180));
//            mainPanel.add(scroll1);
//            mainPanel.add(Box.createVerticalStrut(20));
//            
//            // 2. HScrollPane avec liste SUCCESS
//            addLabel(mainPanel, "2. HScrollPane avec liste (SUCCESS)");
//            
//            DefaultListModel<String> listModel = new DefaultListModel<>();
//            for (int i = 1; i <= 20; i++) {
//                listModel.addElement("Item " + i + " - Exemple de contenu dans une liste");
//            }
//            JList<String> list = new JList<>(listModel);
//            list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//            
//            HScrollPane scroll2 = HScrollPane.withStyle(list, HScrollBarStyle.SUCCESS);
//            scroll2.setPreferredSize(new Dimension(800, 150));
//            scroll2.setMaximumSize(new Dimension(800, 150));
//            mainPanel.add(scroll2);
//            mainPanel.add(Box.createVerticalStrut(20));
//            
//            // 3. HScrollPane avec panneau DANGER
//            addLabel(mainPanel, "3. HScrollPane avec panneau de composants (DANGER)");
//            
//            JPanel contentPanel = new JPanel();
//            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
//            contentPanel.setBackground(Color.WHITE);
//            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//            
//            for (int i = 1; i <= 15; i++) {
//                HButton btn = new HButton("Bouton " + i);
//                btn.setAlignmentX(Component.LEFT_ALIGNMENT);
//                btn.setMaximumSize(new Dimension(300, 35));
//                contentPanel.add(btn);
//                contentPanel.add(Box.createVerticalStrut(5));
//            }
//            
//            HScrollPane scroll3 = HScrollPane.withStyle(contentPanel, HScrollBarStyle.DANGER);
//            scroll3.setPreferredSize(new Dimension(800, 180));
//            scroll3.setMaximumSize(new Dimension(800, 180));
//            mainPanel.add(scroll3);
//            mainPanel.add(Box.createVerticalStrut(20));
//            
//            // 4. HScrollPane horizontal INFO
//            addLabel(mainPanel, "4. HScrollPane avec scroll horizontal (INFO)");
//            
//            JTextArea text2 = new JTextArea(5, 80);
//            text2.setText("Texte très long sans retour à la ligne pour forcer le scroll horizontal... " +
//                         "Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut labore et dolore magna aliqua");
//            text2.setLineWrap(false);
//            text2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//            
//            HScrollPane scroll4 = HScrollPane.withStyle(text2, HScrollBarStyle.INFO);
//            scroll4.setPreferredSize(new Dimension(800, 120));
//            scroll4.setMaximumSize(new Dimension(800, 120));
//            mainPanel.add(scroll4);
//            
//            frame.add(mainPanel);
//            frame.setVisible(true);
//        });
            
            //TEST HSCROLLBAR ================================================================================            
            
//            SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test HScrollBar");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(800, 600);
//            frame.setLocationRelativeTo(null);
//            
//            JPanel panel = new JPanel();
//            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//            panel.setBackground(new Color(245, 245, 245));
//            panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
//            
////             1. ScrollPane avec HScrollBar verticale PRIMARY
//            JLabel label1 = new JLabel("ScrollPane avec HScrollBar verticale (PRIMARY)");
//            label1.setFont(new Font("Segoe UI", Font.BOLD, 13));
//            panel.add(label1);
//            panel.add(Box.createVerticalStrut(10));
//            
//            JTextArea textArea1 = new JTextArea(10, 40);
//            textArea1.setText("Ligne 1\nLigne 2\nLigne 3\nLigne 4\nLigne 5\n" +
//                            "Ligne 6\nLigne 7\nLigne 8\nLigne 9\nLigne 10\n" +
//                            "Ligne 11\nLigne 12\nLigne 13\nLigne 14\nLigne 15\n" +
//                            "Ligne 16\nLigne 17\nLigne 18\nLigne 19\nLigne 20");
//            
//            JScrollPane scroll1 = new JScrollPane(textArea1);
//            scroll1.setVerticalScrollBar(HScrollBar.withStyle(JScrollBar.VERTICAL, HScrollBarStyle.PRIMARY));
//            scroll1.setMaximumSize(new Dimension(700, 200));
//            panel.add(scroll1);
//            panel.add(Box.createVerticalStrut(20));
//            
////             2. ScrollPane avec HScrollBar SUCCESS
//            JLabel label2 = new JLabel("ScrollPane avec scrollbars SUCCESS");
//            label2.setFont(new Font("Segoe UI", Font.BOLD, 13));
//            panel.add(label2);
//            panel.add(Box.createVerticalStrut(10));
//            
//            JTextArea textArea2 = new JTextArea(8, 30);
//            textArea2.setText("Texte très long pour tester la scrollbar horizontale... " +
//                            "Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor");
//            textArea2.setLineWrap(false);
//            
//            JScrollPane scroll2 = new JScrollPane(textArea2);
//            scroll2.setVerticalScrollBar(HScrollBar.withStyle(JScrollBar.VERTICAL, HScrollBarStyle.SUCCESS));
//            scroll2.setHorizontalScrollBar(HScrollBar.withStyle(JScrollBar.HORIZONTAL, HScrollBarStyle.SUCCESS));
//            scroll2.setMaximumSize(new Dimension(700, 150));
//            panel.add(scroll2);
//            panel.add(Box.createVerticalStrut(20));
//            
////             3. Scrollbar standalone DANGER
//            JLabel label3 = new JLabel("HScrollBar standalone (DANGER)");
//            label3.setFont(new Font("Segoe UI", Font.BOLD, 13));
//            panel.add(label3);
//            panel.add(Box.createVerticalStrut(10));
//            
//            HScrollBar scrollBar = HScrollBar.withStyle(JScrollBar.HORIZONTAL, HScrollBarStyle.DANGER);
//            scrollBar.setMaximum(200);
//            scrollBar.setVisibleAmount(50);
//            scrollBar.setMaximumSize(new Dimension(700, 15));
//            panel.add(scrollBar);
//            panel.add(Box.createVerticalStrut(20));
//            
////             4. Différents styles
//            JLabel label4 = new JLabel("Différents styles");
//            label4.setFont(new Font("Segoe UI", Font.BOLD, 13));
//            panel.add(label4);
//            panel.add(Box.createVerticalStrut(10));
//            
//            HScrollBarStyle[] styles = {
//                HScrollBarStyle.WARNING, HScrollBarStyle.INFO, 
//                HScrollBarStyle.PURPLE, HScrollBarStyle.OCEAN
//            };
//            
//            for (HScrollBarStyle style : styles) {
//                HScrollBar sb = HScrollBar.withStyle(JScrollBar.HORIZONTAL, style);
//                sb.setMaximum(150);
//                sb.setVisibleAmount(40);
//                sb.setMaximumSize(new Dimension(700, 15));
//                panel.add(sb);
//                panel.add(Box.createVerticalStrut(8));
//            }
//            
//            frame.add(new JScrollPane(panel));
//            frame.setVisible(true);
//        });
//            
            
            //TEST HPROGRESSBAR================================================================================
            
//            SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test HProgressBar");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(700, 600);
//            frame.setLocationRelativeTo(null);
//            
//            JPanel panel = new JPanel();
//            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//            panel.setBackground(new Color(245, 245, 245));
//            panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
//            
//            // Titre
//            JLabel title = new JLabel("HProgressBar - Exemples");
//            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
//            title.setAlignmentX(Component.CENTER_ALIGNMENT);
//            panel.add(title);
//            panel.add(Box.createVerticalStrut(30));
//            
//            // 1. Progress bar simple
//            panel.add(createLabel("1. Progress Bar Simple (50%)"));
//            HProgressBar pb1 = HProgressBar.withStyle(HProgressBarStyle.PRIMARY);
//            pb1.setValue(50);
//            pb1.setStringPainted(true);
//            pb1.setPreferredSize(new Dimension(600, 30));
//            pb1.setMaximumSize(new Dimension(600, 30));
//            panel.add(pb1);
//            panel.add(Box.createVerticalStrut(20));
//            
//            // 2. Progress bar SUCCESS
//            panel.add(createLabel("2. Style SUCCESS (75%)"));
//            HProgressBar pb2 = HProgressBar.withStyle(HProgressBarStyle.SUCCESS);
//            pb2.setValue(75);
//            pb2.setStringPainted(true);
//            pb2.setPreferredSize(new Dimension(600, 30));
//            pb2.setMaximumSize(new Dimension(600, 30));
//            panel.add(pb2);
//            panel.add(Box.createVerticalStrut(20));
//            
//            // 3. Progress bar avec rayures
//            panel.add(createLabel("3. Avec Rayures Animées"));
//            HProgressBar pb3 = HProgressBar.withStyle(HProgressBarStyle.DANGER);
//            pb3.setValue(60);
//            pb3.setStringPainted(true);
//            pb3.setStriped(true);
//            pb3.setAnimated(true);
//            pb3.setPreferredSize(new Dimension(600, 30));
//            pb3.setMaximumSize(new Dimension(600, 30));
//            panel.add(pb3);
//            panel.add(Box.createVerticalStrut(20));
//            
//            // 4. Progress bar indéterminée
//            panel.add(createLabel("4. Indéterminée (Chargement...)"));
//            HProgressBar pb4 = HProgressBar.withStyle(HProgressBarStyle.INFO);
//            pb4.setIndeterminate(true);
//            pb4.setPreferredSize(new Dimension(600, 30));
//            pb4.setMaximumSize(new Dimension(600, 30));
//            panel.add(pb4);
//            panel.add(Box.createVerticalStrut(30));
//            
//            // Progress bar animée avec boutons
//            panel.add(createLabel("5. Progress Bar Animée avec Contrôles"));
//            HProgressBar pb5 = HProgressBar.withStyle(HProgressBarStyle.WARNING);
//            pb5.setValue(0);
//            pb5.setStringPainted(true);
//            pb5.setStriped(true);
//            pb5.setAnimated(true);
//            pb5.setPreferredSize(new Dimension(600, 30));
//            pb5.setMaximumSize(new Dimension(600, 30));
//            panel.add(pb5);
//            panel.add(Box.createVerticalStrut(10));
//            
//            // Boutons de contrôle
//            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
//            buttonPanel.setBackground(new Color(245, 245, 245));
//            buttonPanel.setMaximumSize(new Dimension(600, 50));
//            
//            HButton startBtn = new HButton("Démarrer");
//            startBtn.setPreferredSize(new Dimension(100, 35));
//            
//            HButton resetBtn = new HButton("Reset");
//            resetBtn.setPreferredSize(new Dimension(100, 35));
//            
//            // Timer pour animer la progress bar
//            Timer timer = new Timer(50, e -> {
//                int value = pb5.getValue();
//                if (value < 100) {
//                    pb5.setValue(value + 1);
//                } else {
//                    ((Timer) e.getSource()).stop();
//                }
//            });
//            
//            startBtn.addActionListener(e -> {
//                pb5.setValue(0);
//                timer.start();
//            });
//            
//            resetBtn.addActionListener(e -> {
//                timer.stop();
//                pb5.setValue(0);
//            });
//            
//            buttonPanel.add(startBtn);
//            buttonPanel.add(resetBtn);
//            panel.add(buttonPanel);
//            panel.add(Box.createVerticalStrut(30));
//            
//            // Tous les styles
//            panel.add(createLabel("6. Tous les Styles"));
//            HProgressBarStyle[] styles = HProgressBarStyle.values();
//            for (int i = 0; i < Math.min(6, styles.length); i++) {
//                HProgressBar pb = HProgressBar.withStyle(styles[i]);
//                pb.setValue(40 + i * 10);
//                pb.setStringPainted(true);
//                pb.setPreferredSize(new Dimension(600, 25));
//                pb.setMaximumSize(new Dimension(600, 25));
//                panel.add(pb);
//                panel.add(Box.createVerticalStrut(8));
//            }
//            
//            frame.add(new JScrollPane(panel));
//            frame.setVisible(true);
//        });
            
            
            //TEST HSLIDER ======================================================================================
                        
//            SwingUtilities.invokeLater(() -> {
//            // Créer la fenêtre
//            JFrame frame = new JFrame("Test HSlider - Sliders Modernes");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(900, 800);
//            frame.setLocationRelativeTo(null);
//            
//            // Panneau principal
//            JPanel mainPanel = new JPanel();
//            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//            mainPanel.setBackground(new Color(245, 245, 245));
//            mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
//            
//            // Titre
//            JLabel titleLabel = new JLabel("HSlider - Sliders Modernes & Animés");
//            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
//            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//            mainPanel.add(titleLabel);
//            mainPanel.add(Box.createVerticalStrut(10));
//            
//            JLabel subtitleLabel = new JLabel("Curseurs avec animations fluides et styles prédéfinis");
//            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//            subtitleLabel.setForeground(Color.GRAY);
//            subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//            mainPanel.add(subtitleLabel);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 1. Slider simple PRIMARY ==========
//            addSection(mainPanel, "1. Slider Simple - Style PRIMARY", 
//                "Slider horizontal basique avec valeur affichée");
//            
//            HSlider slider1 = HSlider.withStyle(0, 100, 50, HSliderStyle.PRIMARY);
//            slider1.setShowValue(true);
//            slider1.setValueSuffix("%");
//            addSliderPanel(mainPanel, slider1, "Volume");
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 2. Slider SUCCESS avec graduations ==========
//            addSection(mainPanel, "2. Slider avec Graduations - Style SUCCESS", 
//                "Avec ticks majeurs et mineurs");
//            
//            HSlider slider2 = HSlider.withStyleAndTicks(0, 100, 75, 25, 5, HSliderStyle.SUCCESS);
//            slider2.setShowValue(true);
//            addSliderPanel(mainPanel, slider2, "Progression");
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 3. Slider DANGER température ==========
//            addSection(mainPanel, "3. Contrôle Température - Style DANGER", 
//                "Slider avec préfixe et suffixe");
//            
//            HSlider slider3 = HSlider.withStyle(0, 40, 20, HSliderStyle.DANGER);
//            slider3.setShowValue(true);
//            slider3.setValuePrefix("");
//            slider3.setValueSuffix("°C");
//            addSliderPanel(mainPanel, slider3, "Température");
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 4. Slider WARNING prix ==========
//            addSection(mainPanel, "4. Contrôle de Prix - Style WARNING", 
//                "Avec symbole monétaire");
//            
//            HSlider slider4 = HSlider.withStyle(0, 1000, 500, HSliderStyle.WARNING);
//            slider4.setShowValue(true);
//            slider4.setValuePrefix("$");
//            slider4.setMajorTickSpacing(250);
//            slider4.setPaintTicks(true);
//            addSliderPanel(mainPanel, slider4, "Prix Maximum");
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 5. Slider INFO zoom ==========
//            addSection(mainPanel, "5. Niveau de Zoom - Style INFO", 
//                "Slider avec labels personnalisés");
//            
//            HSlider slider5 = HSlider.withStyle(25, 200, 100, HSliderStyle.INFO);
//            slider5.setShowValue(true);
//            slider5.setValueSuffix("%");
//            slider5.setMajorTickSpacing(25);
//            slider5.setPaintTicks(true);
//            slider5.setPaintLabels(true);
//            addSliderPanel(mainPanel, slider5, "Zoom");
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 6. Slider PURPLE vitesse ==========
//            addSection(mainPanel, "6. Vitesse de Lecture - Style PURPLE", 
//                "Avec graduations fines");
//            
//            HSlider slider6 = HSlider.withStyle(0, 200, 100, HSliderStyle.PURPLE);
//            slider6.setShowValue(true);
//            slider6.setValueSuffix("x");
//            slider6.setMajorTickSpacing(50);
//            slider6.setMinorTickSpacing(10);
//            slider6.setPaintTicks(true);
//            addSliderPanel(mainPanel, slider6, "Vitesse");
//            mainPanel.add(Box.createVerticalStrut(20));
//            
//            // Panneau de comparaison des styles
//            JPanel stylesPanel = createStylesComparisonPanel();
//            mainPanel.add(stylesPanel);
//            mainPanel.add(Box.createVerticalStrut(20));
//            
//            // Informations
//            JPanel infoPanel = createInfoPanel();
//            mainPanel.add(infoPanel);
//            
//            // ScrollPane
//            JScrollPane scrollPane = new JScrollPane(mainPanel);
//            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
//            
//            frame.add(scrollPane);
//            frame.setVisible(true);
//        });
            
            
            // TEST HFORMATEDTEXTFIELD ===========================================================================
                        
//            SwingUtilities.invokeLater(() -> {
//            // Créer la fenêtre
//            JFrame frame = new JFrame("Test HFormattedTextField - Champs Formatés");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(900, 800);
//            frame.setLocationRelativeTo(null);
//            
//            // Panneau principal
//            JPanel mainPanel = new JPanel();
//            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//            mainPanel.setBackground(new Color(245, 245, 245));
//            mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
//            
//            // Titre
//            JLabel titleLabel = new JLabel("HFormattedTextField - Formats & Validation");
//            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
//            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//            mainPanel.add(titleLabel);
//            mainPanel.add(Box.createVerticalStrut(10));
//            
//            JLabel subtitleLabel = new JLabel("Champs avec formats prédéfinis et validation automatique");
//            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//            subtitleLabel.setForeground(Color.GRAY);
//            subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//            mainPanel.add(subtitleLabel);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 1. Nombre entier ==========
//            addSection(mainPanel, "1. Nombre Entier (INTEGER)", 
//                "Accepte uniquement des nombres entiers");
//            
//            HFormattedTextField intField = HFormattedTextField.withFormat(
//                HFormattedTextField.FormatType.INTEGER, 
//                HFormattedTextFieldStyle.PRIMARY,
//                "Ex: 12345"
//            );
//            intField.setPreferredSize(new Dimension(800, 45));
//            intField.setMaximumSize(new Dimension(800, 45));
//            mainPanel.add(intField);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 2. Nombre décimal ==========
//            addSection(mainPanel, "2. Nombre Décimal (DECIMAL)", 
//                "Nombre avec 2 décimales obligatoires");
//            
//            HFormattedTextField decimalField = HFormattedTextField.withFormat(
//                HFormattedTextField.FormatType.DECIMAL,
//                HFormattedTextFieldStyle.INFO,
//                "Ex: 123.45"
//            );
//            decimalField.setPreferredSize(new Dimension(800, 45));
//            decimalField.setMaximumSize(new Dimension(800, 45));
//            mainPanel.add(decimalField);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 3. Devise ==========
//            addSection(mainPanel, "3. Devise (CURRENCY)", 
//                "Format monétaire automatique");
//            
//            HFormattedTextField currencyField = HFormattedTextField.withFormat(
//                HFormattedTextField.FormatType.CURRENCY,
//                HFormattedTextFieldStyle.SUCCESS,
//                "Ex: 1234.56"
//            );
//            currencyField.setPreferredSize(new Dimension(800, 45));
//            currencyField.setMaximumSize(new Dimension(800, 45));
//            mainPanel.add(currencyField);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 4. Pourcentage ==========
//            addSection(mainPanel, "4. Pourcentage (PERCENT)", 
//                "Format pourcentage (0.15 = 15%)");
//            
//            HFormattedTextField percentField = HFormattedTextField.withFormat(
//                HFormattedTextField.FormatType.PERCENT,
//                HFormattedTextFieldStyle.WARNING,
//                "Ex: 0.15"
//            );
//            percentField.setPreferredSize(new Dimension(800, 45));
//            percentField.setMaximumSize(new Dimension(800, 45));
//            mainPanel.add(percentField);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 5. Date ==========
//            addSection(mainPanel, "5. Date (DATE)", 
//                "Format: jj/mm/aaaa");
//            
//            HFormattedTextField dateField = HFormattedTextField.withFormat(
//                HFormattedTextField.FormatType.DATE,
//                HFormattedTextFieldStyle.PRIMARY,
//                "Ex: 25/12/2024"
//            );
//            dateField.setPreferredSize(new Dimension(800, 45));
//            dateField.setMaximumSize(new Dimension(800, 45));
//            mainPanel.add(dateField);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 6. Heure ==========
//            addSection(mainPanel, "6. Heure (TIME)", 
//                "Format: HH:mm:ss");
//            
//            HFormattedTextField timeField = HFormattedTextField.withFormat(
//                HFormattedTextField.FormatType.TIME,
//                HFormattedTextFieldStyle.INFO,
//                "Ex: 14:30:00"
//            );
//            timeField.setPreferredSize(new Dimension(800, 45));
//            timeField.setMaximumSize(new Dimension(800, 45));
//            mainPanel.add(timeField);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 7. Téléphone ==========
//            addSection(mainPanel, "7. Téléphone (PHONE)", 
//                "Format: (XXX) XXX-XXXX");
//            
//            HFormattedTextField phoneField = HFormattedTextField.withFormat(
//                HFormattedTextField.FormatType.PHONE,
//                HFormattedTextFieldStyle.SECONDARY,
//                "(___) ___-____"
//            );
//            phoneField.setPreferredSize(new Dimension(800, 45));
//            phoneField.setMaximumSize(new Dimension(800, 45));
//            mainPanel.add(phoneField);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 8. Email avec validation ==========
//            addSection(mainPanel, "8. Email (EMAIL)", 
//                "Validation automatique du format email");
//            
//            HFormattedTextField emailField = HFormattedTextField.withFormat(
//                HFormattedTextField.FormatType.EMAIL,
//                HFormattedTextFieldStyle.PRIMARY,
//                "exemple@email.com"
//            );
//            emailField.setPreferredSize(new Dimension(800, 45));
//            emailField.setMaximumSize(new Dimension(800, 45));
//            mainPanel.add(emailField);
//            mainPanel.add(Box.createVerticalStrut(20));            
//        
//            // ScrollPane
//            JScrollPane scrollPane = new JScrollPane(mainPanel);
//            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
//            
//            frame.add(scrollPane);
//            frame.setVisible(true);
//        });
            
            // TEST HTABBEDPANE ==================================================================================
            
//            SwingUtilities.invokeLater(() -> {
//            // Créer la fenêtre
//            JFrame frame = new JFrame("Test HTabbedPane - Onglets Modernes");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(900, 700);
//            frame.setLocationRelativeTo(null);
//            
//            // Panneau principal
//            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
//            mainPanel.setBackground(new Color(245, 245, 245));
//            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//            
//            // Titre
//            JLabel titleLabel = new JLabel("HTabbedPane - Démonstration", SwingConstants.CENTER);
//            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
//            mainPanel.add(titleLabel, BorderLayout.NORTH);
//            
//            // Créer le HTabbedPane principal
//            HTabbedPane tabbedPane = HTabbedPane.withStyle(HTabbedPaneStyle.PRIMARY);
//            tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//            
//            // ========== Onglet 1 : Présentation ==========
//            JPanel presentationPanel = createPresentationPanel();
//            tabbedPane.addTab("Présentation", createIcon(new Color(13, 110, 253)), 
//                             presentationPanel, "Vue d'ensemble des fonctionnalités");
//            
//            // ========== Onglet 2 : Styles ==========
//            JPanel stylesPanel = createStylesPanel(frame);
//            tabbedPane.addTab("Styles", createIcon(new Color(156, 39, 176)), 
//                             stylesPanel, "Différents styles disponibles");
//            
//            // ========== Onglet 3 : Paramètres ==========
//            JPanel settingsPanel = createSettingsPanel(tabbedPane);
//            tabbedPane.addTab("Paramètres", createIcon(new Color(255, 193, 7)), 
//                             settingsPanel, "Configurer l'apparence");
//            
//            // ========== Onglet 4 : Exemple ==========
//            JPanel examplePanel = createExamplePanel();
//            tabbedPane.addTab("Exemple", createIcon(new Color(25, 135, 84)), 
//                             examplePanel, "Exemple avec contenu");
//            
//            // ========== Onglet 5 : À propos ==========
//            JPanel aboutPanel = createAboutPanel();
//            tabbedPane.addTab("À propos", createIcon(new Color(13, 202, 240)), 
//                             aboutPanel, "Informations sur HComponents");
//            
//            mainPanel.add(tabbedPane, BorderLayout.CENTER);
//            
//            frame.add(mainPanel);
//            frame.setVisible(true);
//        });          
            
            
            //  TEST HOPTIONPANE ============================================================
           
//             SwingUtilities.invokeLater(() -> {
//            // Créer la fenêtre
//            JFrame frame = new JFrame("Test HOptionPane - Dialogs Modernes");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(700, 650);
//            frame.setLocationRelativeTo(null);
//            
//            // Panneau principal
//            JPanel mainPanel = new JPanel();
//            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//            mainPanel.setBackground(new Color(245, 245, 245));
//            mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
//            
//            // Titre
//            JLabel titleLabel = new JLabel("HOptionPane - Dialogs Modernes");
//            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
//            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//            mainPanel.add(titleLabel);
//            mainPanel.add(Box.createVerticalStrut(10));
//            
//            JLabel subtitleLabel = new JLabel("Testez différents types de dialogs avec animations");
//            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//            subtitleLabel.setForeground(Color.GRAY);
//            subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//            mainPanel.add(subtitleLabel);
//            mainPanel.add(Box.createVerticalStrut(40));
//            
//            // ========== Messages simples ==========
//            addSectionTitle(mainPanel, "Messages Simples");
//            
//            addButtonRow(mainPanel, "Information", "Message d'information classique", () -> {
//                HOptionPane.showMessageDialog(
//                    frame,
//                    "Ceci est un message d'information.\nLes données ont été sauvegardées avec succès.",
//                    "Information",
//                    HOptionPane.INFORMATION_MESSAGE
//                );
//            });
//            
//            addButtonRow(mainPanel, "Succès", "Message de confirmation", () -> {
//                HOptionPane.showMessageDialog(
//                    frame,
//                    "Opération réussie !\nTous les fichiers ont été traités correctement.",
//                    "Succès",
//                    HOptionPane.SUCCESS_MESSAGE
//                );
//            });
//            
//            addButtonRow(mainPanel, "Avertissement", "Message d'alerte", () -> {
//                HOptionPane.showMessageDialog(
//                    frame,
//                    "Attention : L'espace disque est faible.\nVeuillez libérer de l'espace.",
//                    "Avertissement",
//                    HOptionPane.WARNING_MESSAGE
//                );
//            });
//            
//            addButtonRow(mainPanel, "Erreur", "Message d'erreur", () -> {
//                HOptionPane.showMessageDialog(
//                    frame,
//                    "Une erreur s'est produite !\nImpossible de se connecter au serveur.",
//                    "Erreur",
//                    HOptionPane.ERROR_MESSAGE
//                );
//            });
//            
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== Dialogues de confirmation ==========
//            addSectionTitle(mainPanel, "Dialogues de Confirmation");
//            
//            addButtonRow(mainPanel, "Oui/Non", "Confirmation simple", () -> {
//                int result = HOptionPane.showConfirmDialog(
//                    frame,
//                    "Voulez-vous vraiment supprimer ce fichier ?",
//                    "Confirmation",
//                    HOptionPane.YES_NO_OPTION
//                );
//                
//                String message = (result == HOptionPane.YES_OPTION) ? "Vous avez choisi : OUI" : "Vous avez choisi : NON";
//                HOptionPane.showMessageDialog(frame, message, "Résultat", HOptionPane.INFORMATION_MESSAGE);
//            });
//            
//            addButtonRow(mainPanel, "Oui/Non/Annuler", "Confirmation avec annulation", () -> {
//                int result = HOptionPane.showConfirmDialog(
//                    frame,
//                    "Voulez-vous enregistrer les modifications ?",
//                    "Enregistrer",
//                    HOptionPane.YES_NO_CANCEL_OPTION
//                );
//                
//                String message;
//                if (result == HOptionPane.YES_OPTION) message = "Vous avez choisi : OUI";
//                else if (result == HOptionPane.NO_OPTION) message = "Vous avez choisi : NON";
//                else message = "Vous avez choisi : ANNULER";
//                
//                HOptionPane.showMessageDialog(frame, message, "Résultat", HOptionPane.INFORMATION_MESSAGE);
//            });
//            
//            addButtonRow(mainPanel, "OK/Annuler", "Confirmation OK/Annuler", () -> {
//                int result = HOptionPane.showConfirmDialog(
//                    frame,
//                    "Êtes-vous sûr de vouloir continuer cette action ?",
//                    "Continuer",
//                    HOptionPane.OK_CANCEL_OPTION,
//                    HOptionPane.QUESTION_MESSAGE
//                );
//                
//                String message = (result == HOptionPane.OK_OPTION) ? "Vous avez choisi : OK" : "Vous avez choisi : ANNULER";
//                HOptionPane.showMessageDialog(frame, message, "Résultat", HOptionPane.INFORMATION_MESSAGE);
//            });
//            
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== Dialogues de saisie ==========
//            addSectionTitle(mainPanel, "Dialogues de Saisie");
//            
//            addButtonRow(mainPanel, "Saisie de texte", "Entrer une valeur", () -> {
//                String input = HOptionPane.showInputDialog(
//                    frame,
//                    "Veuillez entrer votre nom :",
//                    "Saisie",
//                    HOptionPane.QUESTION_MESSAGE
//                );
//                
//                if (input != null && !input.trim().isEmpty()) {
//                    HOptionPane.showMessageDialog(
//                        frame,
//                        "Bonjour, " + input + " !",
//                        "Bienvenue",
//                        HOptionPane.SUCCESS_MESSAGE
//                    );
//                }
//            });
//            
//            mainPanel.add(Box.createVerticalStrut(20));
//                     
//            frame.add(mainPanel);
//            frame.setVisible(true);
//        });                        
            
            // TEST EDITORPANE ================================================================================
            
//             SwingUtilities.invokeLater(() -> {
//            // Créer la fenêtre
//            JFrame frame = new JFrame("Test HEditorPane - Validation et Erreurs");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(900, 800);
//            frame.setLocationRelativeTo(null);
//            
//            // Panneau principal
//            JPanel mainPanel = new JPanel();
//            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//            mainPanel.setBackground(new Color(245, 245, 245));
//            mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
//            
//            // ========== 1. EditorPane avec validation longueur minimale ==========
//            addSection(mainPanel, "1. Validation : Minimum 10 caractères", 
//                "Tapez au moins 10 caractères pour valider");
//            
//            HEditorPane editor1 = HEditorPane.withStyle(HEditorPaneStyle.PRIMARY, 
//                "Entrez votre texte ici (min. 10 caractères)...");
//            editor1.setPreferredSize(new Dimension(500, 100));
//            editor1.setMaximumSize(new Dimension(500, 100));
//            
//            // Validateur : longueur minimale
//            editor1.addValidator(text -> text.length() >= 10);
//            editor1.setAutoValidate(true);
//            
//            mainPanel.add(editor1);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 2. EditorPane avec validation email ==========
//            addSection(mainPanel, "2. Validation : Format Email", 
//                "Doit contenir un '@' et un '.'");
//            
//            HEditorPane editor2 = HEditorPane.withStyle(HEditorPaneStyle.SUCCESS, 
//                "exemple@email.com");
//            editor2.setPreferredSize(new Dimension(500, 80));
//            editor2.setMaximumSize(new Dimension(500, 80));
//            
//            // Validateur : format email simple
//            editor2.addValidator(text -> {
//                if (text.isEmpty()) return true; // Vide = pas d'erreur
//                return text.contains("@") && text.contains(".") && text.indexOf("@") < text.lastIndexOf(".");
//            });
//            
//            mainPanel.add(editor2);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 3. EditorPane avec validation nombre ==========
//            addSection(mainPanel, "3. Validation : Nombre entre 1 et 100", 
//                "Entrez un nombre valide");
//            
//            HEditorPane editor3 = HEditorPane.withStyle(HEditorPaneStyle.WARNING, 
//                "50");
//            editor3.setPreferredSize(new Dimension(500, 80));
//            editor3.setMaximumSize(new Dimension(500, 80));
//            
//            // Validateur : nombre entre 1 et 100
//            editor3.addValidator(text -> {
//                if (text.isEmpty()) return true;
//                try {
//                    int num = Integer.parseInt(text.trim());
//                    return num >= 1 && num <= 100;
//                } catch (NumberFormatException e) {
//                    return false;
//                }
//            });
//            
//            mainPanel.add(editor3);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 4. EditorPane avec validation personnalisée multiple ==========
//            addSection(mainPanel, "4. Validation Multiple : Pas de chiffres + Min 5 caractères", 
//                "Le texte ne doit pas contenir de chiffres");
//            
//            HEditorPane editor4 = HEditorPane.withStyle(HEditorPaneStyle.INFO, 
//                "Texte sans chiffres...");
//            editor4.setPreferredSize(new Dimension(500, 100));
//            editor4.setMaximumSize(new Dimension(500, 100));
//            
//            // Validateur 1 : pas de chiffres
//            editor4.addValidator(text -> !text.matches(".*\\d.*"));
//            
//            // Validateur 2 : minimum 5 caractères
//            editor4.addValidator(text -> text.length() >= 5);
//            
//            mainPanel.add(editor4);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ========== 5. EditorPane avec validation manuelle ==========
//            addSection(mainPanel, "5. Validation Manuelle (cliquez sur le bouton)", 
//                "Validation désactivée automatiquement");
//            
//            JPanel manualPanel = new JPanel(new BorderLayout(10, 10));
//            manualPanel.setBackground(new Color(245, 245, 245));
//            manualPanel.setMaximumSize(new Dimension(800, 150));
//            
//            HEditorPane editor5 = HEditorPane.withStyle(HEditorPaneStyle.DANGER, 
//                "Ce champ nécessite une validation manuelle...");
//            editor5.setPreferredSize(new Dimension(500, 100));
//            editor5.setAutoValidate(false); // Désactiver validation auto
//            
//            // Validateur : non vide
//            editor5.addValidator(text -> !text.trim().isEmpty());
//            
//            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
//            buttonPanel.setBackground(new Color(245, 245, 245));
//            
//            HButton validateBtn = new HButton("Valider");
//            validateBtn.setPreferredSize(new Dimension(120, 40));
//            validateBtn.addActionListener(e -> {
//                if (editor5.validateContent()) {
//                    editor5.setError(false, "");
//                    JOptionPane.showMessageDialog(frame, "✓ Validation réussie !", 
//                        "Succès", JOptionPane.INFORMATION_MESSAGE);
//                } else {
//                    editor5.setError(true, "Le champ ne peut pas être vide");
//                }
//            });
//            
//            HButton clearBtn = new HButton("Effacer erreur");
//            clearBtn.setPreferredSize(new Dimension(120, 40));
//            clearBtn.addActionListener(e -> editor5.setError(false));
//            
//            buttonPanel.add(validateBtn);
//            buttonPanel.add(clearBtn);
//            
//            manualPanel.add(editor5, BorderLayout.CENTER);
//            manualPanel.add(buttonPanel, BorderLayout.EAST);
//            
//            mainPanel.add(manualPanel);
//            mainPanel.add(Box.createVerticalStrut(20));
//            
//            // ScrollPane
//            JScrollPane scrollPane = new JScrollPane(mainPanel);
//            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
//            
//            frame.add(scrollPane);
//            frame.setVisible(true);
//        });
            
            
        // TEST COMBOBOX ===============================================================================================
//        SwingUtilities.invokeLater(() -> {
//            // Créer la fenêtre
//            JFrame frame = new JFrame("Test HComboBox");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(900, 700);
//            frame.setLocationRelativeTo(null);
//            
//            // Panneau principal
//            JPanel mainPanel = new JPanel();
//            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//            mainPanel.setBackground(new Color(245, 245, 245));
//            mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));           
//            
//            
//            // Données pour les combobox
//            String[] pays = {"France", "États-Unis", "Canada", "Allemagne", "Japon", "Royaume-Uni"};
//            String[] langages = {"Java", "Python", "JavaScript", "C++", "C#", "Ruby", "Go"};
//            String[] couleurs = {"Rouge", "Bleu", "Vert", "Jaune", "Orange", "Violet"};
//            String[] fruits = {"Pomme", "Banane", "Orange", "Fraise", "Raisin", "Mangue"};
//            
//            // Style PRIMARY
//            addComboBoxPanel(mainPanel, "Style PRIMARY", pays, HComboBoxStyle.PRIMARY);
//            mainPanel.add(Box.createVerticalStrut(20));
//            
//            // Style SUCCESS
//            addComboBoxPanel(mainPanel, "Style SUCCESS", langages, HComboBoxStyle.SUCCESS);
//            mainPanel.add(Box.createVerticalStrut(20));
//            
//            // Style DANGER
//            addComboBoxPanel(mainPanel, "Style DANGER", couleurs, HComboBoxStyle.DANGER);
//            mainPanel.add(Box.createVerticalStrut(20));
//            
//            // Style WARNING
//            addComboBoxPanel(mainPanel, "Style WARNING", fruits, HComboBoxStyle.WARNING);
//            mainPanel.add(Box.createVerticalStrut(20));
//            
//            // Style INFO
//            String[] mois = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin"};
//            addComboBoxPanel(mainPanel, "Style INFO", mois, HComboBoxStyle.INFO);
//            mainPanel.add(Box.createVerticalStrut(20));
//            
//            // Style DARK
//            String[] systemes = {"Windows", "macOS", "Linux", "Android", "iOS"};
//            addComboBoxPanel(mainPanel, "Style DARK", systemes, HComboBoxStyle.DARK);
//            mainPanel.add(Box.createVerticalStrut(30));
//            
//            // ScrollPane
//            JScrollPane scrollPane = new JScrollPane(mainPanel);
//            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
//            
//            frame.add(scrollPane);
//            frame.setVisible(true);
//        });
        
//         TEST HPOPUPMENU ==========================================================================================
//         SwingUtilities.invokeLater(() -> {
//            // Créer la fenêtre
//            JFrame frame = new JFrame("Test HPopupMenu - Menu Contextuel Moderne");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(800, 400);
//            frame.setLocationRelativeTo(null);
//            
//            // Créer le panneau principal
//            JPanel mainPanel = new JPanel(new BorderLayout());
//            mainPanel.setBackground(new Color(245, 245, 245));
//            
//            // Zone de texte pour tester le popup
//            HTextArea textArea = new HTextArea();
//            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//            textArea.setMargin(new Insets(20, 20, 20, 20));
//            textArea.setLineWrap(true);
//            textArea.setWrapStyleWord(true);
//            
//            // Créer un popup pour la zone de texte (style PRIMARY)
//            HPopupMenu textPopup = createTextPopup();
//            textArea.setComponentPopupMenu(textPopup);
//            
//            JScrollPane scrollPane = new JScrollPane(textArea);
//            scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
//            mainPanel.add(scrollPane, BorderLayout.CENTER);
//            
//            // Panneau inférieur avec bouton
//            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
//            bottomPanel.setBackground(new Color(66, 165, 245));
//            bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//            
//            // Bouton avec son propre popup (style SUCCESS)
//            HButton button = new HButton("Clic droit sur moi !");
//            button.setPreferredSize(new Dimension(200, 50));
//            
//            HPopupMenu buttonPopup = createButtonPopup();
//            button.setComponentPopupMenu(buttonPopup);
//            
//            bottomPanel.add(button);
//            
//            // Ajouter un popup pour le panneau bleu (style DARK)
//            HPopupMenu panelPopup = createPanelPopup();
//            bottomPanel.setComponentPopupMenu(panelPopup);
//            
//            mainPanel.add(bottomPanel, BorderLayout.SOUTH);
//                       
//            
//            frame.add(mainPanel);
//            frame.setVisible(true);
//        });


        ///// TEST HMENU ET HMENUITEM ===================================================================================
        
//         SwingUtilities.invokeLater(() -> {
//            // Créer la fenêtre
//            JFrame frame = new JFrame("Test HMenu");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(800, 600);
//            frame.setLocationRelativeTo(null);
//
//            // Créer la barre de menu avec style PRIMARY
//            HMenuBar menuBar = HMenuBar.withStyle(HMenuStyle.PRIMARY);
//            
//            // ========== Menu Fichier ==========
//            HMenu menuFichier = HMenu.withStyle("Fichier", HMenuStyle.PRIMARY);   
//            menuFichier.setFont(new Font("Segio UI", Font.PLAIN, 16));
//
//            HMenuItem itemNouveau = HMenuItem.withStyle("Nouveau", HMenuStyle.PRIMARY);             
//            itemNouveau.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
//            itemNouveau.setIcon(folder);
//            itemNouveau.addActionListener(e -> showMessage("Nouveau fichier créé"));
//
//            HMenuItem itemOuvrir = HMenuItem.withStyle("Ouvrir", HMenuStyle.PRIMARY);
//            itemOuvrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
//            itemOuvrir.setIcon(open);
//            itemOuvrir.addActionListener(e -> showMessage("Ouvrir un fichier"));
//
//            HMenuItem itemEnregistrer = HMenuItem.withStyle("Enregistrer", HMenuStyle.PRIMARY);
//            itemEnregistrer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
//            itemEnregistrer.setIcon(save);
//            itemEnregistrer.addActionListener(e -> showMessage("Fichier enregistré"));
//
//            menuFichier.add(itemNouveau);
//            menuFichier.add(itemOuvrir);
//            menuFichier.add(new HSeparator(HMenuStyle.PRIMARY));
//            menuFichier.add(itemEnregistrer);
//            menuFichier.addSeparator();
//
//            // Sous-menu "Récents"
//            HMenu sousMenuRecents = HMenu.withStyle("Fichiers récents", HMenuStyle.PRIMARY);
//            sousMenuRecents.setIcon(createIcon(Color.MAGENTA));
//            for (int i = 1; i <= 3; i++) {
//                HMenuItem recent = HMenuItem.withStyle("Document " + i + ".txt", HMenuStyle.PRIMARY);
//                final int num = i;
//                recent.addActionListener(e -> showMessage("Ouverture de Document " + num));
//                sousMenuRecents.add(recent);
//            }
//            menuFichier.add(sousMenuRecents);
//
//            menuFichier.addSeparator();
//            HMenuItem itemQuitter = HMenuItem.withStyle("Quitter", HMenuStyle.PRIMARY);
//            itemQuitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
//            itemQuitter.setIcon(close);
//            itemQuitter.addActionListener(e -> System.exit(0));
//            menuFichier.add(itemQuitter);
//
//            // ========== Menu Édition ==========
//            HMenu menuEdition = HMenu.withStyle("Édition", HMenuStyle.PRIMARY);
//            menuEdition.setFont(new Font("Segio UI", Font.PLAIN, 16));
//
//            HMenuItem itemCopier = HMenuItem.withStyle("Copier", HMenuStyle.PRIMARY);
//            itemCopier.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
//            itemCopier.addActionListener(e -> showMessage("Texte copié"));
//
//            HMenuItem itemColler = HMenuItem.withStyle("Coller", HMenuStyle.PRIMARY);
//            itemColler.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
//            itemColler.addActionListener(e -> showMessage("Texte collé"));
//
//            HMenuItem itemCouper = HMenuItem.withStyle("Couper", HMenuStyle.PRIMARY);
//            itemCouper.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
//            itemCouper.addActionListener(e -> showMessage("Texte coupé"));
//
//            menuEdition.add(itemCopier);
//            menuEdition.add(itemColler);
//            menuEdition.add(itemCouper);
//
//            // ========== Menu Affichage ==========
//            HMenu menuAffichage = HMenu.withStyle("Affichage", HMenuStyle.PRIMARY);
//            menuAffichage.setFont(new Font("Segio UI", Font.PLAIN, 16));
//
//            HMenuItem itemZoomIn = HMenuItem.withStyle("Zoom +", HMenuStyle.PRIMARY);
//            itemZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
//            itemZoomIn.addActionListener(e -> showMessage("Zoom avant"));
//
//            HMenuItem itemZoomOut = HMenuItem.withStyle("Zoom -", HMenuStyle.PRIMARY);
//            itemZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
//            itemZoomOut.addActionListener(e -> showMessage("Zoom arrière"));
//
//            menuAffichage.add(itemZoomIn);
//            menuAffichage.add(itemZoomOut);
//
//            // ========== Menu Aide ==========
//            HMenu menuAide = HMenu.withStyle("Aide", HMenuStyle.PRIMARY);
//            menuAide.setFont(new Font("Segio UI", Font.PLAIN, 16));
//
//            HMenuItem itemDocumentation = HMenuItem.withStyle("Documentation", HMenuStyle.PRIMARY);
//            itemDocumentation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
//            itemDocumentation.addActionListener(e -> showMessage("Ouverture de la documentation"));
//
//            HMenuItem itemAPropos = HMenuItem.withStyle("À propos", HMenuStyle.PRIMARY);
//            itemAPropos.addActionListener(e -> showMessage("HComponents v1.0\nPar FIDELE"));
//
//            menuAide.add(itemDocumentation);
//            menuAide.add(new HSeparator(HMenuStyle.PRIMARY));
//            menuAide.add(itemAPropos);
//
//            // Ajouter les menus à la barre
//            menuBar.add(menuFichier);
//            menuBar.add(menuEdition);
//            menuBar.add(menuAffichage);
//            menuBar.add(menuAide);
//
//            // Créer un panneau central avec informations
//            JPanel contentPanel = new JPanel(new BorderLayout());
//            contentPanel.setBackground(Color.WHITE);
//
//            // Configurer la fenêtre
//            frame.setJMenuBar(menuBar);
//            frame.add(contentPanel);
//            frame.setVisible(true);
//        });

        // TEST HTREE ============================================================================================
//        SwingUtilities.invokeLater(() -> {
//            // Créer la fenêtre
//            JFrame frame = new JFrame("Test HTree");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(600, 500);
//            frame.setLocationRelativeTo(null);
//            
//            // Créer la structure de l'arbre
//            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Entreprise");
//            
//            DefaultMutableTreeNode direction = new DefaultMutableTreeNode("Direction");
//            DefaultMutableTreeNode rh = new DefaultMutableTreeNode("Ressources Humaines");
//            DefaultMutableTreeNode it = new DefaultMutableTreeNode("Informatique");
//            
//            root.add(direction);
//            root.add(rh);
//            root.add(it);
//            
//            direction.add(new DefaultMutableTreeNode("PDG"));
//            direction.add(new DefaultMutableTreeNode("Directeur Général"));
//            
//            rh.add(new DefaultMutableTreeNode("Recrutement"));
//            rh.add(new DefaultMutableTreeNode("Formation"));
//            rh.add(new DefaultMutableTreeNode("Paie"));
//            
//            it.add(new DefaultMutableTreeNode("Développement"));
//            it.add(new DefaultMutableTreeNode("Infrastructure"));
//            it.add(new DefaultMutableTreeNode("Support"));
//            
//            // Créer le HTree avec style PRIMARY
//            HTree tree = HTree.withStyle(root, HTreeStyle.PRIMARY);
//            
//            // Personnaliser le HTree
//            tree.setCornerRadius(12);
//            tree.setIconTextGap(10);
//            tree.setNodeVerticalSpacing(6);
//            tree.setLevelIndent(25);
//            tree.setConnectionLineThickness(2);
//            tree.setAnimationsEnabled(true);
//            tree.setHoverEnabled(true);
//            tree.setShowConnectionLines(true);
//            
//            // Étendre quelques nœuds pour voir la structure
//            tree.expandRow(0);  // root
//            tree.expandRow(1);  // direction
//            tree.expandRow(2);  // rh
//            
//            HButton button = new HButton("Button");
//            button.setBounds(120, 120, 220, 65);
//            
//            // Ajouter dans un JScrollPane
//            JScrollPane scrollPane = new JScrollPane(tree);
//            
//            // Configuration du panneau
//            JPanel mainPanel = new JPanel(new BorderLayout());
//            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//            mainPanel.add(scrollPane, BorderLayout.CENTER);
//            
//            // Ajouter au frame et afficher
//            frame.add(mainPanel);
//            frame.setVisible(true);
//        });
        


//        Runnable maTache = ()->{
//            for (int i = 0; i <= 5; i++) {
//                System.out.println("c'est facile ! " +i);
//                try {
//                    Thread.sleep(500);
//                            
//                } catch (Exception e) {
//                    
//                }
//            }  
//        };
//        
//        new Thread(maTache).start();
//        
//        System.out.println("C'est parti !");


//        Test HPasswordField ===============================================================================

//          SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test Bordures");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(600, 400);
//            
//            HPasswordField pass = new HPasswordField();
//            pass.setButtonStyle(HButtonStyle.FIELD);
//            pass.setMinLengthRule(true, 8);
//            pass.setLowercaseRule(true);
//            pass.setBounds(120, 120, 250,50);
//            pass.setCornerRadius(48);
//            pass.setHBorder(new GlowBorder(new Color(24, 27, 71), 1));
//            JPanel panel = new JPanel(null);
//            panel.setBackground(Color.WHITE);
//            panel.add(pass);
//
//            frame.setContentPane(panel);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });



// Test HTextField =====================================================================================       

//SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test HTextField");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            
//            frame.setSize(600, 500);
//            
//            // === CRÉATION DES TEXTFIELDS ===
//            
//            // TextField 1 : Simple sans compteur
//            HTextField field1 = new HTextField("TextField simple", 20);
//            field1.setButtonStyle(HButtonStyle.DANGER);
//            field1.setBounds(50, 30, 500, 45);
//            
//            // TextField 2 : Avec compteur (max 50 caractères)
//            HTextField field2 = new HTextField(20);
//            field2.setMaxCharacters(10);
//            field2.setButtonStyle(HButtonStyle.FIELD);
//            field2.addForbiddenCharacter('@');
//            field2.setBounds(50, 100, 280, 45);
//            System.out.println("Field2 maxCharacters: " + field2.getMaxCharacters());
//            
//            // TextField 3 : Avec caractères interdits (@, #, $)
//            HTextField field3 = new HTextField("Essayez @, # ou $", 20);
//            field3.setMaxCharacters(30);
//            field3.addForbiddenCharacter('@');
//            field3.addForbiddenCharacter('#');
//            field3.addForbiddenCharacter('$');
//            field3.setBounds(50, 170, 240, 45);
//            
//            // TextField 4 : Style SUCCESS avec bordure personnalisée
//            HTextField field4 = new HTextField("Style SUCCESS", 20);
//            field4.setButtonStyle(HButtonStyle.SUCCESS);
//            field4.setMaxCharacters(100);
//            field4.setCornerRadius(15);
//            field4.setHBorder(new SolidBorder(new Color(40, 167, 69), 2, 15));
//            field4.setBounds(50, 240, 240, 45);
//            
//            // TextField 5 : Bordure rouge épaisse
//            HTextField field5 = new HTextField("Bordure personnalisée", 20);
//            field5.setButtonStyle(HButtonStyle.FIELD);
//            field5.setHBorder(new SolidBorder(Color.RED, 3, 10));
//            field5.setMaxCharacters(25);
//            field5.setBounds(50, 310, 240, 45);
//            
//            // TextField 6 : Padding élevé
//            HTextField field6 = new HTextField("Padding 20px", 20);
//            field6.setVerticalPadding(20);
//            field6.setHorizontalPadding(20);
//            field6.setMaxCharacters(40);
//            field6.setBounds(50, 380, 240, 65);
//            
//            // Panel principal
//            JPanel panel = new JPanel(null);
//            panel.setBackground(new Color(245, 245, 245));
//            
//            // Ajout des TextFields
//            panel.add(field1);
//            panel.add(field2);
//            panel.add(field3);
//            panel.add(field4);
//            panel.add(field5);
//            panel.add(field6);            
//           
//            frame.setContentPane(panel);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });


//       SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test HTextArea Ombre");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(500, 300);
//
//            HTextArea area = new HTextArea(6, 28);
//            JPanel panel = new JPanel(new GridBagLayout());
//            panel.setBackground(Color.WHITE);
//            panel.add(area);
//
//            frame.setContentPane(panel);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });


//         Test HCHeckBox et HRadioButton ===================================================================

//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test HToggleButton");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//            HCheckBox check = new HCheckBox("ckecker moi");
//            check.setBounds(120, 120, 100, 100);
//            check.setCheckmarkSize(30);   
//            check.setVerticalAlignment(SwingConstants.NORTH);
//            check.setButtonStyle(HButtonStyle.DANGER);
//
//            HRadioButton radio = new HRadioButton("RadioButton");
//            radio.setBounds(120, 250, 150, 250);
//            radio.setButtonStyle(HButtonStyle.SUCCESS);
//            radio.setVerticalAlignment(SwingConstants.EAST);
//
//            JPanel panel = new JPanel(new GridBagLayout());
//            panel.setBackground(Color.GRAY);
//            panel.add(radio);
//
//            frame.setContentPane(panel);
//            frame.setSize(520, 320);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });


//         Test ToggleButton =================================================================================

//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Test HToggleButton");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//            HToggleButton htoggle = new HToggleButton("HToggle", true);
//            htoggle.setPreferredSize(new Dimension(180, 50));
//            htoggle.setButtonStyle(HButtonStyle.PRIMARY);
//            htoggle.setShadow(new HShadowFloating());
//
//            JPanel panel = new JPanel(new GridBagLayout());
//            panel.setBackground(Color.WHITE);
//            panel.add(htoggle);
//
//            frame.setContentPane(panel);
//            frame.setSize(320, 120);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });
//    
//

//TEST HLABEL STYLE ========================================================

//             JFrame frame = new JFrame("Tous les styles HLabel");
//        frame.setLayout(new GridLayout(0, 2, 10, 10));
//        frame.getContentPane().setBackground(new Color(240, 240, 240));
//        
////         Tester tous les styles
//        for (HLabelStyle style : HLabelStyle.values()) {
//            HLabel label = new HLabel(style.name());
//            label.setLabelStyle(style);
//            label.setHasRoundedBackground(true);
//            label.setPadding(10);
//            label.setHorizontalAlignment(SwingConstants.CENTER);
//            frame.add(label);
//        }
//        
//        frame.setSize(500, 400);
//        frame.setLocationRelativeTo(null);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setVisible(true);


//            TEST ORIENTATION HLABEL ========================================
//SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Orientations HLabel");
//            frame.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
//            frame.getContentPane().setBackground(Color.WHITE);
//            
//            
//            // Vertical UP avec styles primaires
//            frame.add(new JLabel(" ")); // Espace
//      
//            
//            HLabelStyle[] mainStyles = {HLabelStyle.PRIMARY, HLabelStyle.SUCCESS, 
//                                       HLabelStyle.DANGER, HLabelStyle.WARNING, HLabelStyle.INFO};
//            
//            for (HLabelStyle style : mainStyles) {
//                HLabel label = new HLabel(style.name(), HLabelOrientation.VERTICAL_UP);
//                label.setLabelStyle(style);
//                label.setHasRoundedBackground(true);
//                label.setPadding(12);
//                label.setPreferredSize(new Dimension(70, 120));
//                frame.add(label);
//            }
//            
//            // Vertical DOWN avec styles secondaires
//            frame.add(new JLabel(" ")); // Espace
//            
//            
//            
//            for (HLabelStyle style : mainStyles) {
//                HLabel label = new HLabel(style.name(), HLabelOrientation.VERTICAL_DOWN);
//                label.setLabelStyle(style);
//                label.setHasRoundedBackground(true);
//                label.setPadding(12);
//                label.setPreferredSize(new Dimension(70, 120));
//                frame.add(label);
//            }
//            
//            frame.setSize(900, 400);
//            frame.setLocationRelativeTo(null);
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setVisible(true);
//        });

     
//        HTableModel model = createModel();
//        HTable table = new HTable(model); 
//        table.AjustColumnsToFitContent();
//        createDemoFrame(table);
//
//    }
//
//    private static HTableModel createModel() {
//        HTableModel model = new HTableModel();
//
//        Définition des colonnes avec types
//        model.addColumn("ID", Integer.class, true);
//        model.addColumn("Nom", String.class, true);
//        model.addColumn("Département", String.class, true);
//        model.addColumn("Date Embauche", String.class, true);
//
//        Ajout des données de démonstration
//        model.addRow(Arrays.asList(1, "Alice ", "Ressources Humaines", "2022-03-15"));
//        model.addRow(Arrays.asList(2, "Bruno ", "Développement", "2021-07-22"));
//        model.addRow(Arrays.asList(3, "Clara ", "Marketing", "2023-01-10"));
//        model.addRow(Arrays.asList(4, "David ", "Finance", "2020-11-30"));
//        model.addRow(Arrays.asList(5, "Émilie ", "Développement", "2022-09-05"));
//        model.addRow(Arrays.asList(6, "François ", "Support", "2023-03-18"));
//        model.addRow(Arrays.asList(7, "Gabrielle ", "Marketing", "2021-12-12"));
//        model.addRow(Arrays.asList(8, "Hugo Laurent", "Finance", "2020-05-25"));
//        model.addRow(Arrays.asList(9, "Isabelle ", "Ressources Humaines", "2022-11-08"));
//        model.addRow(Arrays.asList(10, "Jérôme ", "Direction", "2019-08-14"));
//
//        return model;
//    }

//    private static void createDemoFrame(HTable table) {
//        JFrame frame = new JFrame("HTable Demo - Table Moderne Java Swing");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLayout(new BorderLayout());
//
//         ScrollPane pour la table
//        JScrollPane scrollPane = new JScrollPane(table);
//        scrollPane.setBorder(BorderFactory.createEmptyBorder());
//        scrollPane.getViewport().setBackground(Color.WHITE);
//
//        frame.add(scrollPane, BorderLayout.CENTER);
//
//        frame.setSize(1000, 700);
//        frame.setLocationRelativeTo(null); // Centrer
//        frame.setVisible(true);
//
//    }
//    private static String resultToString(int code) {
//        switch (code) {
//            case JOptionPane.YES_OPTION:
//                return "YES_OPTION";
//            case JOptionPane.NO_OPTION:
//                return "NO_OPTION";
//            case JOptionPane.CANCEL_OPTION:
//                return "CANCEL_OPTION";
//            case JOptionPane.CLOSED_OPTION:
//                return "CLOSED_OPTION";
//            default:
//                return String.valueOf(code);
//        }
//    }


        }
    
    private static void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Action", JOptionPane.INFORMATION_MESSAGE);
    }

    private static Icon createIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(x, y, 16, 16);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }

    /**
     * Crée un popup pour la zone de texte (édition)
     */
    private static HPopupMenu createTextPopup() {
        
         ImageIcon paste = new ImageIcon("paste.png");
         ImageIcon copy = new ImageIcon("copy.png");
         ImageIcon cut = new ImageIcon("cut.png");
        
        HPopupMenu popup = HPopupMenu.withStyle(HMenuStyle.PRIMARY);

        HMenuItem itemCouper = HMenuItem.withStyle("Couper", createIcon(Color.RED), HMenuStyle.PRIMARY);
        itemCouper.setIcon(cut);
        itemCouper.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        itemCouper.addActionListener(e -> showMessage("Texte coupé"));

        HMenuItem itemCopier = HMenuItem.withStyle("Copier", createIcon(Color.BLUE), HMenuStyle.PRIMARY);
        itemCopier.setIcon(copy);
        itemCopier.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        itemCopier.addActionListener(e -> showMessage("Texte copié"));

        HMenuItem itemColler = HMenuItem.withStyle("Coller", createIcon(Color.GREEN), HMenuStyle.PRIMARY);
        itemColler.setIcon(paste);
        itemColler.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        itemColler.addActionListener(e -> showMessage("Texte collé"));

        popup.addHMenuItem(itemCouper);
        popup.addHMenuItem(itemCopier);
        popup.addHMenuItem(itemColler);
        popup.addHSeparator();

        HMenuItem itemTout = HMenuItem.withStyle("Sélectionner tout", HMenuStyle.PRIMARY);
        itemTout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        itemTout.addActionListener(e -> showMessage("Tout sélectionné"));
        popup.addHMenuItem(itemTout);

        return popup;
    }

    /**
     * Crée un popup pour le bouton (actions)
     */
    private static HPopupMenu createButtonPopup() {
        HPopupMenu popup = HPopupMenu.withStyle(HMenuStyle.SUCCESS);

        HMenuItem itemAction1 = HMenuItem.withStyle("Action rapide", createIcon(Color.GREEN), HMenuStyle.SUCCESS);
        itemAction1.addActionListener(e -> showMessage("Action rapide exécutée"));

        HMenuItem itemAction2 = HMenuItem.withStyle("Paramètres", createIcon(Color.ORANGE), HMenuStyle.SUCCESS);
        itemAction2.addActionListener(e -> showMessage("Ouvrir les paramètres"));

        popup.addHMenuItem(itemAction1);
        popup.addHMenuItem(itemAction2);
        popup.addHSeparator();

        // Sous-menu
        HMenu sousMenu = HMenu.withStyle("Plus d'options", HMenuStyle.SUCCESS);
        sousMenu.setIcon(createIcon(Color.MAGENTA));

        for (int i = 1; i <= 3; i++) {
            HMenuItem subItem = HMenuItem.withStyle("Option " + i, HMenuStyle.SUCCESS);
            final int num = i;
            subItem.addActionListener(e -> showMessage("Option " + num + " sélectionnée"));
            sousMenu.add(subItem);
        }

        popup.addHMenu(sousMenu);

        return popup;
    }

    /**
     * Crée un popup pour le panneau (style DARK)
     */
    private static HPopupMenu createPanelPopup() {
        HPopupMenu popup = HPopupMenu.withStyle(HMenuStyle.DARK);

        HMenuItem itemInfo = HMenuItem.withStyle("Informations", createIcon(Color.CYAN), HMenuStyle.DARK);
        itemInfo.addActionListener(e -> showMessage("Panneau bleu - Zone d'actions"));

        HMenuItem itemCouleur = HMenuItem.withStyle("Changer la couleur", createIcon(Color.YELLOW), HMenuStyle.DARK);
        itemCouleur.addActionListener(e -> showMessage("Changement de couleur..."));

        HMenuItem itemCache = HMenuItem.withStyle("Masquer", createIcon(Color.RED), HMenuStyle.DARK);
        itemCache.addActionListener(e -> showMessage("Masquage du panneau"));

        popup.addHMenuItem(itemInfo);
        popup.addHSeparator();
        popup.addHMenuItem(itemCouleur);
        popup.addHMenuItem(itemCache);

        return popup;
    }

    private static void addComboBoxPanel(JPanel parent, String label, String[] items, HComboBoxStyle style) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);        
       
        // ComboBox
        HComboBox<String> combo = HComboBox.withStyle(items, style);
        combo.setPreferredSize(new Dimension(250, 40));
        combo.setMaximumSize(new Dimension(250, 40));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Listener pour afficher la sélection
        combo.addActionListener(e -> {
            String selected = (String) combo.getSelectedItem();
            if (selected != null) {
                System.out.println(label + " - Sélection: " + selected);
            }
        });

        panel.add(combo);        

        parent.add(panel);
    }
    
//    private static void addSection(JPanel parent, String title, String description) {
//        JPanel sectionPanel = new JPanel();
//        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
//        sectionPanel.setBackground(new Color(245, 245, 245));
//        sectionPanel.setMaximumSize(new Dimension(800, 60));
//        
//        JLabel titleLabel = new JLabel(title);
//        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
//        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        
//        JLabel descLabel = new JLabel(description);
//        descLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
//        descLabel.setForeground(Color.GRAY);
//        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        
//        sectionPanel.add(titleLabel);
//        sectionPanel.add(Box.createVerticalStrut(3));
//        sectionPanel.add(descLabel);
//        sectionPanel.add(Box.createVerticalStrut(8));
//        
//        parent.add(sectionPanel);
//    }

    
    private static void addSectionTitle(JPanel parent, String title) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionLabel.setForeground(new Color(13, 110, 253));
        sectionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        parent.add(sectionLabel);
        parent.add(Box.createVerticalStrut(15));
    }
    
    private static void addButtonRow(JPanel parent, String buttonText, String description, Runnable action) {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        rowPanel.setMaximumSize(new Dimension(600, 60));
        
        // Description
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(buttonText);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(Color.GRAY);
        
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(descLabel);
        
        rowPanel.add(textPanel);
        rowPanel.add(Box.createHorizontalGlue());
        
        // Bouton
        HButton button = new HButton("Tester");
        button.setPreferredSize(new Dimension(100, 35));
        button.setMaximumSize(new Dimension(100, 35));
        button.addActionListener(e -> action.run());
        
        rowPanel.add(button);
        
        parent.add(rowPanel);
        parent.add(Box.createVerticalStrut(10));
    }
    
//    private static JPanel createInfoPanel() {
//        JPanel infoPanel = new JPanel();
//        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
//        infoPanel.setBackground(new Color(240, 245, 255));
//        infoPanel.setBorder(BorderFactory.createCompoundBorder(
//            BorderFactory.createLineBorder(new Color(13, 110, 253), 2, true),
//            BorderFactory.createEmptyBorder(15, 15, 15, 15)
//        ));
//        infoPanel.setMaximumSize(new Dimension(600, 180));
//        
//        JLabel infoTitle = new JLabel("✨ Fonctionnalités");
//        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
//        infoTitle.setForeground(new Color(13, 110, 253));
//        
//        String[] features = {
//            "• Bordures arrondies avec ombres douces",
//            "• Animation de fade-in à l'ouverture",
//            "• Icônes personnalisées pour chaque type",
//            "• En-tête avec fond coloré",
//            "• Boutons stylisés HButton",
//            "• Styles prédéfinis (INFO, SUCCESS, DANGER, etc.)",
//            "• Support des dialogs de saisie"
//        };
//        
//        infoPanel.add(infoTitle);
//        infoPanel.add(Box.createVerticalStrut(8));
//        
//        for (String feature : features) {
//            JLabel label = new JLabel(feature);
//            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//            label.setAlignmentX(Component.LEFT_ALIGNMENT);
//            infoPanel.add(label);
//        }
//        
//        return infoPanel;
//    }
    
    private static JPanel createPresentationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        JLabel title = new JLabel("✨ Fonctionnalités HTabbedPane");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(13, 110, 253));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        
        String[] features = {
            "• Bordures arrondies pour un look moderne",
            "• Indicateur coloré animé sous l'onglet sélectionné",
            "• Effet hover fluide avec transition progressive",
            "• Animation de glissement de l'indicateur",
            "• Texte en gras pour l'onglet actif",
            "• Support des icônes personnalisées",
            "• 10 styles prédéfinis (PRIMARY, SUCCESS, DANGER, etc.)",
            "• Fond du contenu avec bordures arrondies",
            "• Tooltips sur chaque onglet",
            "• Configuration flexible (hauteur indicateur, radius, etc.)"
        };
        
        for (String feature : features) {
            JLabel label = new JLabel(feature);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            panel.add(label);
        }
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private static JPanel createStylesPanel(JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        JLabel title = new JLabel("🎨 Styles Disponibles");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(156, 39, 176));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        
        JLabel description = new JLabel("Cliquez sur un bouton pour voir le style en action :");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(description);
        panel.add(Box.createVerticalStrut(15));
        
        HTabbedPaneStyle[] styles = HTabbedPaneStyle.values();
        
        for (HTabbedPaneStyle style : styles) {
            HButton button = new HButton("Voir style " + style.name());
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(250, 40));
            button.addActionListener(e -> showStyleDemo(frame, style));
            panel.add(button);
            panel.add(Box.createVerticalStrut(10));
        }
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private static JPanel createSettingsPanel(HTabbedPane tabbedPane) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        JLabel title = new JLabel("⚙️ Paramètres");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(255, 193, 7));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        
        // Rayon des coins
        addSliderSetting(panel, "Rayon des coins", 0, 30, 8, value -> {
            tabbedPane.setCornerRadius(value);
        });
        
        // Hauteur de l'indicateur
        addSliderSetting(panel, "Hauteur de l'indicateur", 1, 10, 3, value -> {
            tabbedPane.setIndicatorHeight(value);
        });
        
        // Checkboxes
        panel.add(Box.createVerticalStrut(20));
        
        JCheckBox animationsCheck = new JCheckBox("Activer les animations", true);
        animationsCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        animationsCheck.setBackground(Color.WHITE);
        animationsCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        animationsCheck.addActionListener(e -> tabbedPane.setAnimationsEnabled(animationsCheck.isSelected()));
        panel.add(animationsCheck);
        
        panel.add(Box.createVerticalStrut(10));
        
        JCheckBox hoverCheck = new JCheckBox("Activer l'effet hover", true);
        hoverCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hoverCheck.setBackground(Color.WHITE);
        hoverCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        hoverCheck.addActionListener(e -> tabbedPane.setHoverEnabled(hoverCheck.isSelected()));
        panel.add(hoverCheck);
        
        panel.add(Box.createVerticalStrut(10));
        
        JCheckBox indicatorCheck = new JCheckBox("Afficher l'indicateur", true);
        indicatorCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        indicatorCheck.setBackground(Color.WHITE);
        indicatorCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        indicatorCheck.addActionListener(e -> tabbedPane.setShowIndicator(indicatorCheck.isSelected()));
        panel.add(indicatorCheck);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private static void addSliderSetting(JPanel panel, String label, int min, int max, 
                                        int initial, java.util.function.IntConsumer onChange) {
        JLabel titleLabel = new JLabel(label + ": " + initial);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));
        
        JSlider slider = new JSlider(min, max, initial);
        slider.setBackground(Color.WHITE);
        slider.setMaximumSize(new Dimension(400, 40));
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.addChangeListener(e -> {
            int value = slider.getValue();
            titleLabel.setText(label + ": " + value);
            onChange.accept(value);
        });
        panel.add(slider);
        panel.add(Box.createVerticalStrut(20));
    }
    
    private static JPanel createExamplePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("📝 Exemple avec Contenu", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);
        
        JTextArea textArea = new JTextArea();
        textArea.setText("Ceci est un exemple de contenu dans un onglet.\n\n" +
                        "Vous pouvez mettre n'importe quel composant Swing ici :\n" +
                        "- Des formulaires\n" +
                        "- Des tableaux\n" +
                        "- Des graphiques\n" +
                        "- D'autres HTabbedPane imbriqués\n" +
                        "- Et bien plus encore !");
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private static JPanel createAboutPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        JLabel title = new JLabel("HComponents");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(13, 202, 240));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        
        JLabel version = new JLabel("Version 1.0");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        version.setForeground(Color.GRAY);
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(version);
        panel.add(Box.createVerticalStrut(30));
        
        JLabel description = new JLabel("<html><center>Bibliothèque de composants Swing modernes<br>" +
                                       "avec animations fluides et design contemporain</center></html>");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(description);
        panel.add(Box.createVerticalStrut(20));
        
        JLabel author = new JLabel("Par FIDELE");
        author.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        author.setForeground(Color.GRAY);
        author.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(author);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private static void showStyleDemo(JFrame parent, HTabbedPaneStyle style) {
        JDialog dialog = new JDialog(parent, "Style : " + style.name(), true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(parent);
        
        HTabbedPane demoPane = HTabbedPane.withStyle(style);
        demoPane.addTab("Onglet 1", new JLabel("Contenu 1", SwingConstants.CENTER));
        demoPane.addTab("Onglet 2", new JLabel("Contenu 2", SwingConstants.CENTER));
        demoPane.addTab("Onglet 3", new JLabel("Contenu 3", SwingConstants.CENTER));
        
        dialog.add(demoPane);
        dialog.setVisible(true);
    }
    
   private static void addSection2(JPanel parent, String title, String description) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBackground(new Color(245, 245, 245));
        sectionPanel.setMaximumSize(new Dimension(800, 60));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sectionPanel.add(titleLabel);
        sectionPanel.add(Box.createVerticalStrut(3));
        sectionPanel.add(descLabel);
        sectionPanel.add(Box.createVerticalStrut(8));
        
        parent.add(sectionPanel);
    }
    
    private static JPanel createInfoPanel2() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(240, 245, 255));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(13, 110, 253), 2, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        infoPanel.setMaximumSize(new Dimension(800, 220));
        
        JLabel infoTitle = new JLabel("💡 Fonctionnalités");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoTitle.setForeground(new Color(13, 110, 253));
        
        String[] features = {
            "• Bordures animées selon l'état (normal → focus → erreur/succès)",
            "• Validation en temps réel pendant la frappe",
            "• Icônes de statut (✓ valide / ✗ erreur)",
            "• Messages de validation sous le champ",
            "• Fond coloré selon l'état (erreur = rouge, succès = vert)",
            "• Placeholder avec style italic",
            "• 8 formats prédéfinis : INTEGER, DECIMAL, CURRENCY, PERCENT, DATE, TIME, PHONE, EMAIL",
            "• Bordure verte pour validation réussie",
            "• Bordure rouge pour erreur de format",
            "• Animations fluides à 60 FPS"
        };
        
        Font infoFont = new Font("Segoe UI", Font.PLAIN, 12);
        
        infoPanel.add(infoTitle);
        infoPanel.add(Box.createVerticalStrut(8));
        
        for (String feature : features) {
            JLabel label = new JLabel(feature);
            label.setFont(infoFont);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoPanel.add(label);
        }
        
        return infoPanel;
    }
    
    private static void addSliderPanel(JPanel parent, HSlider slider, String label) {
        JPanel sliderPanel = new JPanel(new BorderLayout(10, 10));
        sliderPanel.setBackground(Color.WHITE);
        sliderPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        sliderPanel.setMaximumSize(new Dimension(800, 100));
        
        // Label à gauche
        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setPreferredSize(new Dimension(120, 30));
        sliderPanel.add(nameLabel, BorderLayout.WEST);
        
        // Slider au centre
        sliderPanel.add(slider, BorderLayout.CENTER);
        
        // Label de valeur à droite
        JLabel valueLabel = new JLabel(String.valueOf(slider.getValue()));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setPreferredSize(new Dimension(60, 30));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        sliderPanel.add(valueLabel, BorderLayout.EAST);
        
        // Listener pour mettre à jour la valeur
        slider.addChangeListener(e -> {
            valueLabel.setText(String.valueOf(slider.getValue()));
        });
        
        parent.add(sliderPanel);
    }
    
    private static JPanel createStylesComparisonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setMaximumSize(new Dimension(800, 350));
        
        JLabel title = new JLabel("🎨 Tous les Styles Disponibles");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        
        HSliderStyle[] styles = {
            HSliderStyle.PRIMARY, HSliderStyle.SECONDARY, HSliderStyle.SUCCESS,
            HSliderStyle.DANGER, HSliderStyle.WARNING, HSliderStyle.INFO,
            HSliderStyle.LIGHT, HSliderStyle.DARK, HSliderStyle.OCEAN, HSliderStyle.PURPLE
        };
        
        for (HSliderStyle style : styles) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(Color.WHITE);
            row.setMaximumSize(new Dimension(760, 40));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel styleLabel = new JLabel(style.name());
            styleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            styleLabel.setPreferredSize(new Dimension(100, 30));
            
            HSlider miniSlider = HSlider.withStyle(0, 100, 60, style);
            miniSlider.setPreferredSize(new Dimension(600, 30));
            
            row.add(styleLabel, BorderLayout.WEST);
            row.add(miniSlider, BorderLayout.CENTER);
            
            panel.add(row);
            panel.add(Box.createVerticalStrut(5));
        }
        
        return panel;
    }
    
    
    private static void addSection(JPanel parent, String title, String description) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBackground(new Color(245, 245, 245));
        sectionPanel.setMaximumSize(new Dimension(800, 60));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sectionPanel.add(titleLabel);
        sectionPanel.add(Box.createVerticalStrut(3));
        sectionPanel.add(descLabel);
        sectionPanel.add(Box.createVerticalStrut(8));
        
        parent.add(sectionPanel);
    }
    
    private static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
     private static void addLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));
    }
    
     /**
     * Crée une carte de feature stylée.
     */
    private static JPanel createFeatureCard(String title, String description) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(13, 110, 253));

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(108, 117, 125));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);

        return card;
    }
    
    
    
}
