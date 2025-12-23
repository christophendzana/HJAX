/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HButtonStyle;
import hcomponents.vues.HFrameStyle;
import hcomponents.vues.border.HAbstractBorder;
import hcomponents.vues.border.HBorder;
import hcomponents.vues.shadow.HShadow;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Composant HFrame - Une fenêtre Swing personnalisée avec design moderne.
 * Étend JFrame pour offrir des fonctionnalités avancées : barre de titre stylée,
 * coins arrondis, ombres, animations fluides et contrôles personnalisés.
 * 
 * <p>Ce composant fournit une API cohérente avec les autres composants HComponents
 * pour une expérience utilisateur uniforme.</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see JFrame
 * @see HFrameStyle
 */
public class HFrame extends JFrame {

    /** Bordure personnalisée de la fenêtre */
    private HBorder hBorder;
    
    /** Ombre personnalisée de la fenêtre */
    private HShadow hShadow;
    
    /** Rayon des coins arrondis (en pixels) */
    private int cornerRadius = 15;
    
    /** Style visuel appliqué à la fenêtre */
    private HFrameStyle frameStyle = HFrameStyle.PRIMARY;
    
    /** Panel principal contenant tout le contenu */
    private JPanel mainPanel;
    
    /** Panel de la barre de titre personnalisée */
    private JPanel titleBar;
    
    /** Label du titre */
    private JLabel titleLabel;
    
    /** Panel contenant les boutons de contrôle */
    private JPanel controlsPanel;
    
    /** Bouton de réduction */
    private HButton minimizeButton;
    
    /** Bouton de maximisation/restauration */
    private HButton maximizeButton;
    
    /** Bouton de fermeture */
    private HButton closeButton;
    
    /** Panel de contenu utilisateur */
    private JPanel contentPanel;
    
    /** Hauteur de la barre de titre */
    private int titleBarHeight = 45;
    
    /** Position X de la souris lors du drag */
    private int mouseX;
    
    /** Position Y de la souris lors du drag */
    private int mouseY;
    
    /** Indique si la fenêtre est maximisée */
    private boolean isMaximized = false;
    
    /** Dimensions et position avant maximisation */
    private Rectangle normalBounds;
    
    /** Indique si les boutons de contrôle sont affichés */
    private boolean showControls = true;
    
    /** Icône de la fenêtre */
    private Icon frameIcon;

    /**
     * Constructeur avec titre.
     * 
     * @param title le titre de la fenêtre
     */
    public HFrame(String title) {
        super();
        init(title);
    }

    /**
     * Constructeur par défaut.
     */
    public HFrame() {
        this("");
    }

    /**
     * Initialise la fenêtre avec ses composants et sa structure.
     */
    private void init(String title) {
        // Configuration de base
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Support de la transparence
        try {
            Class<?> awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
            java.lang.reflect.Method setWindowOpaque = awtUtilitiesClass.getMethod(
                "setWindowOpaque", Window.class, boolean.class);
            setWindowOpaque.invoke(null, this, false);
        } catch (Exception e) {
            // Si la méthode n'existe pas, on continue (Java 11+)
        }
        
        // Création du panel principal
        mainPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Dessin de l'ombre
                if (hShadow != null) {
                    hShadow.paint(g2, this, getWidth(), getHeight(), cornerRadius);
                }
                
                // Fond de la fenêtre
                g2.setColor(frameStyle.getBackgroundColor());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
                
                // Bordure personnalisée
                if (hBorder != null) {
                    hBorder.paint(g2, this, getWidth(), getHeight(), cornerRadius);
                }
                
                g2.dispose();
            }
            
            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Création de la barre de titre
        createTitleBar(title);

        // Création du panel de contenu
        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BorderLayout());

        // Assemblage
        mainPanel.add(titleBar, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Ajout du système de redimensionnement
        enableResizing();
    }

    /**
     * Crée la barre de titre avec les contrôles.
     */
    private void createTitleBar(String title) {
        titleBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fond de la barre de titre avec dégradé
                GradientPaint gradient = new GradientPaint(
                    0, 0, frameStyle.getTitleBarColor().brighter(),
                    0, getHeight(), frameStyle.getTitleBarColor()
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
                
                // Rectangle pour cacher les coins arrondis du bas
                g2.setColor(frameStyle.getTitleBarColor());
                g2.fillRect(0, getHeight() - cornerRadius, getWidth(), cornerRadius);
                
                g2.dispose();
            }
            
            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        titleBar.setPreferredSize(new Dimension(0, titleBarHeight));
        titleBar.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 10));

        // Panel gauche avec icône et titre
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        // Label du titre
        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(frameStyle.getTitleColor());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(
            (titleBarHeight - 20) / 2, 0, 0, 0
        ));
        leftPanel.add(titleLabel);

        // Panel des boutons de contrôle
        createControlButtons();

        titleBar.add(leftPanel, BorderLayout.WEST);
        titleBar.add(controlsPanel, BorderLayout.EAST);

        // Gestion du drag pour déplacer la fenêtre
        MouseAdapter dragListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isMaximized) {
                    setLocation(getLocation().x + e.getX() - mouseX, 
                               getLocation().y + e.getY() - mouseY);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Double-clic pour maximiser/restaurer
                if (e.getClickCount() == 2) {
                    toggleMaximize();
                }
            }
        };
        
        titleBar.addMouseListener(dragListener);
        titleBar.addMouseMotionListener(dragListener);
    }

    /**
     * Crée les boutons de contrôle (minimiser, maximiser, fermer) avec HButton.
     */
    private void createControlButtons() {
        controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, (titleBarHeight - 30) / 2));
    controlsPanel.setOpaque(false);

    // Bouton minimiser 
    minimizeButton = new HButton(minimizeIcon(Color.WHITE)); 
    minimizeButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
    styleControlButton(minimizeButton, frameStyle.getMinimizeButtonStyle());
    minimizeButton.addActionListener(e -> setState(Frame.ICONIFIED));

    // Bouton maximiser/restaurer 
    maximizeButton = new HButton(maximizeIcon(Color.WHITE)); 
    maximizeButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
    styleControlButton(maximizeButton, frameStyle.getMaximizeButtonStyle());
    maximizeButton.addActionListener(e -> toggleMaximize());

    // Bouton fermer 
    closeButton = new HButton(closeIcon(Color.WHITE)); 
    closeButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
    styleControlButton(closeButton, frameStyle.getCloseButtonStyle());
    closeButton.addActionListener(e -> {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    });

    if (showControls) {
        controlsPanel.add(minimizeButton);
        controlsPanel.add(maximizeButton);
        controlsPanel.add(closeButton);
    }        
    }

    /**
     * Applique le style à un bouton de contrôle.
     */
    private void styleControlButton(HButton button, HButtonStyle style) {
         int tbh = ((HFrame) this).getTitleBarHeight();
        button.setButtonStyle(style);
        button.setPreferredSize(new Dimension(30, (tbh * 75) / 100));
        button.setCornerRadius(6);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Forcer la peinture centrée de l'icône
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
    }

    /**
     * Bascule entre l'état maximisé et normal.
     */
    private void toggleMaximize() {
        if (isMaximized) {
            // Restaurer
            setBounds(normalBounds);
            isMaximized = false;
            maximizeButton.setText("□");
        } else {
            // Maximiser
            normalBounds = getBounds();
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle maxBounds = env.getMaximumWindowBounds();
            setBounds(maxBounds);
            isMaximized = true;
            maximizeButton.setText("❐");
        }
    }
    
    /**
     * Active le système de redimensionnement manuel de la fenêtre.
     * 
     * <p>Comme la fenêtre est "undecorated" (sans décoration native), le système
     * d'exploitation ne fournit plus les bordures de redimensionnement par défaut.
     * Cette méthode implémente un système manuel qui détecte les bords et coins
     * de la fenêtre pour permettre le redimensionnement par glisser-déposer.</p>
     * 
     * <p><b>Fonctionnement :</b></p>
     * <ul>
     *   <li>Une marge de 5 pixels sur chaque bord est définie comme zone de redimensionnement</li>
     *   <li>Le curseur change automatiquement selon la zone survolée (↔, ↕, ↖, etc.)</li>
     *   <li>8 zones sont détectées : 4 coins (diagonales) + 4 bords (horizontaux/verticaux)</li>
     *   <li>Le redimensionnement est désactivé en mode maximisé</li>
     *   <li>Des dimensions minimales (300x200) sont appliquées pour éviter une fenêtre trop petite</li>
     * </ul>
     */
    private void enableResizing() {
        // Marge de détection des bordures (en pixels)
        // Une zone de 5 pixels sur chaque bord réagira au survol de la souris
        final int RESIZE_MARGIN = 5;
        
        // Création du listener qui gère tout le comportement de redimensionnement
        MouseAdapter resizeListener = new MouseAdapter() {
            /** Type de curseur actuel (indique la direction du redimensionnement) */
            private int cursor;
            
            /** Position de la souris au moment du clic (point de départ du drag) */
            private Point startPoint;
            
            /** Dimensions et position de la fenêtre au moment du clic */
            private Rectangle startBounds;
            
            /**
             * Détecte la zone de redimensionnement survolée et change le curseur.
             * Appelé continuellement quand la souris se déplace sur la fenêtre.
             * 
             * @param e l'événement de mouvement de souris
             */
            @Override
            public void mouseMoved(MouseEvent e) {
                // En mode maximisé, pas de redimensionnement possible
                if (isMaximized) {
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }
                
                // Récupération de la position actuelle de la souris
                Point point = e.getPoint();
                int width = getWidth();
                int height = getHeight();
                
                // Détection des zones : la souris est-elle près d'un bord ?
                boolean top = point.y < RESIZE_MARGIN;                    // Bord supérieur
                boolean bottom = point.y > height - RESIZE_MARGIN;        // Bord inférieur
                boolean left = point.x < RESIZE_MARGIN;                   // Bord gauche
                boolean right = point.x > width - RESIZE_MARGIN;          // Bord droit
                
                // Détermination du curseur approprié selon la zone
                // Les coins (combinaisons de 2 bords) ont priorité sur les bords simples
                if (top && left) {
                    // Coin supérieur gauche : redimensionnement diagonal ↖
                    cursor = Cursor.NW_RESIZE_CURSOR;
                } else if (top && right) {
                    // Coin supérieur droit : redimensionnement diagonal ↗
                    cursor = Cursor.NE_RESIZE_CURSOR;
                } else if (bottom && left) {
                    // Coin inférieur gauche : redimensionnement diagonal ↙
                    cursor = Cursor.SW_RESIZE_CURSOR;
                } else if (bottom && right) {
                    // Coin inférieur droit : redimensionnement diagonal ↘
                    cursor = Cursor.SE_RESIZE_CURSOR;
                } else if (top) {
                    // Bord supérieur uniquement : redimensionnement vertical ↕
                    cursor = Cursor.N_RESIZE_CURSOR;
                } else if (bottom) {
                    // Bord inférieur uniquement : redimensionnement vertical ↕
                    cursor = Cursor.S_RESIZE_CURSOR;
                } else if (left) {
                    // Bord gauche uniquement : redimensionnement horizontal ↔
                    cursor = Cursor.W_RESIZE_CURSOR;
                } else if (right) {
                    // Bord droit uniquement : redimensionnement horizontal ↔
                    cursor = Cursor.E_RESIZE_CURSOR;
                } else {
                    // Centre de la fenêtre : curseur par défaut (flèche)
                    cursor = Cursor.DEFAULT_CURSOR;
                }
                
                // Application du curseur correspondant
                setCursor(Cursor.getPredefinedCursor(cursor));
            }
            
            /**
             * Enregistre la position et les dimensions initiales au début du drag.
             * Ces valeurs serviront de référence pour calculer le nouveau redimensionnement.
             * 
             * @param e l'événement de clic de souris
             */
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();      // Position de la souris au clic
                startBounds = getBounds();      // Rectangle (x, y, width, height) de la fenêtre
            }
            
            /**
             * Effectue le redimensionnement en temps réel pendant le drag.
             * Calcule les nouvelles dimensions selon la direction du redimensionnement.
             * 
             * @param e l'événement de drag de souris
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                // Pas de redimensionnement en mode maximisé ou si le curseur est normal
                if (isMaximized || cursor == Cursor.DEFAULT_CURSOR) {
                    return;
                }
                
                // Calcul du déplacement de la souris depuis le clic initial
                int deltaX = e.getX() - startPoint.x;  // Déplacement horizontal
                int deltaY = e.getY() - startPoint.y;  // Déplacement vertical
                
                // Initialisation des nouvelles valeurs avec les valeurs de départ
                int newX = startBounds.x;           // Position X (coin supérieur gauche)
                int newY = startBounds.y;           // Position Y (coin supérieur gauche)
                int newWidth = startBounds.width;   // Largeur
                int newHeight = startBounds.height; // Hauteur
                
                // Application du redimensionnement selon la direction détectée
                switch (cursor) {
                    case Cursor.N_RESIZE_CURSOR:
                        // Redimensionnement par le bord supérieur
                        // On déplace le haut de la fenêtre ET on ajuste la hauteur
                        newY += deltaY;           // Le haut monte/descend
                        newHeight -= deltaY;      // La hauteur diminue/augmente inversement
                        break;
                        
                    case Cursor.S_RESIZE_CURSOR:
                        // Redimensionnement par le bord inférieur
                        // Seule la hauteur change (le haut reste fixe)
                        newHeight += deltaY;
                        break;
                        
                    case Cursor.W_RESIZE_CURSOR:
                        // Redimensionnement par le bord gauche
                        // On déplace le côté gauche ET on ajuste la largeur
                        newX += deltaX;           // Le côté gauche bouge
                        newWidth -= deltaX;       // La largeur diminue/augmente inversement
                        break;
                        
                    case Cursor.E_RESIZE_CURSOR:
                        // Redimensionnement par le bord droit
                        // Seule la largeur change (le côté gauche reste fixe)
                        newWidth += deltaX;
                        break;
                        
                    case Cursor.NW_RESIZE_CURSOR:
                        // Redimensionnement par le coin supérieur gauche
                        // Combine N_RESIZE et W_RESIZE
                        newX += deltaX;
                        newY += deltaY;
                        newWidth -= deltaX;
                        newHeight -= deltaY;
                        break;
                        
                    case Cursor.NE_RESIZE_CURSOR:
                        // Redimensionnement par le coin supérieur droit
                        // Combine N_RESIZE et E_RESIZE
                        newY += deltaY;
                        newWidth += deltaX;
                        newHeight -= deltaY;
                        break;
                        
                    case Cursor.SW_RESIZE_CURSOR:
                        // Redimensionnement par le coin inférieur gauche
                        // Combine S_RESIZE et W_RESIZE
                        newX += deltaX;
                        newWidth -= deltaX;
                        newHeight += deltaY;
                        break;
                        
                    case Cursor.SE_RESIZE_CURSOR:
                        // Redimensionnement par le coin inférieur droit
                        // Combine S_RESIZE et E_RESIZE
                        newWidth += deltaX;
                        newHeight += deltaY;
                        break;
                }
                
                // Application des dimensions minimales pour éviter une fenêtre trop petite
                // Ces valeurs garantissent que la fenêtre reste utilisable
                if (newWidth < 300) newWidth = 300;      // Largeur minimale : 300px
                if (newHeight < 200) newHeight = 200;    // Hauteur minimale : 200px
                
                // Application des nouvelles dimensions et position à la fenêtre
                setBounds(newX, newY, newWidth, newHeight);
            }
            
            /**
             * Restaure le curseur par défaut quand la souris quitte la fenêtre.
             * Évite que le curseur de redimensionnement reste actif hors de la fenêtre.
             * 
             * @param e l'événement de sortie de souris
             */
            @Override
            public void mouseExited(MouseEvent e) {
                if (!isMaximized) {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        // Enregistrement du listener sur la fenêtre
        // Les deux listeners sont nécessaires pour détecter les mouvements ET les drags
        addMouseListener(resizeListener);         // Pour mousePressed, mouseExited
        addMouseMotionListener(resizeListener);   // Pour mouseMoved, mouseDragged
    }

    /**
     * Définit le contenu principal de la fenêtre.
     * 
     * @param content le composant à afficher dans le corps de la fenêtre
     */
    public void setContent(JComponent content) {
        contentPanel.removeAll();
        content.setOpaque(false);
        contentPanel.add(content, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // ========== GETTERS ET SETTERS ==========

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
    }

    public HBorder getHBorder() {
        return hBorder;
    }

    public void setHBorder(HAbstractBorder border) {
        this.hBorder = border;
        repaint();
    }

    public HShadow getShadow() {
        return hShadow;
    }

    public void setShadow(HShadow shadow) {
        this.hShadow = shadow;
        repaint();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public HFrameStyle getFrameStyle() {
        return frameStyle;
    }

    public void setFrameStyle(HFrameStyle style) {
        this.frameStyle = style;
        titleLabel.setForeground(style.getTitleColor());
        
        // Mise à jour des styles des boutons
        if (minimizeButton != null) {
            minimizeButton.setButtonStyle(style.getMinimizeButtonStyle());
        }
        if (maximizeButton != null) {
            maximizeButton.setButtonStyle(style.getMaximizeButtonStyle());
        }
        if (closeButton != null) {
            closeButton.setButtonStyle(style.getCloseButtonStyle());
        }
        
        repaint();
    }

    public int getTitleBarHeight() {
        return titleBarHeight;
    }

    public void setTitleBarHeight(int height) {
        this.titleBarHeight = height;
        titleBar.setPreferredSize(new Dimension(0, height));
        revalidate();
        repaint();
    }

    public boolean isShowControls() {
        return showControls;
    }

    public void setShowControls(boolean show) {
        this.showControls = show;
        controlsPanel.removeAll();
        if (show) {
            controlsPanel.add(minimizeButton);
            controlsPanel.add(maximizeButton);
            controlsPanel.add(closeButton);
        }
        revalidate();
        repaint();
    }

    public JPanel getTitleBar() {
        return titleBar;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    
     private static Icon closeIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = c.getWidth();
                int height = c.getHeight();
                int iconSize = Math.min(width, height) - 15; // Taille de l'icône avec marge

                // Calculer la position pour centrer l'icône
                int centerX = (width - iconSize) / 3;
                int centerY = (height - iconSize) / 3;

                // Dessiner le X centré
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2));

                // Première diagonale
                g2d.drawLine(
                        centerX,
                        centerY,
                        centerX + iconSize,
                        centerY + iconSize
                );

                // Seconde diagonale
                g2d.drawLine(
                        centerX + iconSize,
                        centerY,
                        centerX,
                        centerY + iconSize
                );

                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        };
    }

    private Icon maximizeIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = c.getWidth();
                int height = c.getHeight();
                int iconSize = Math.min(width, height) - 15; // Taille de l'icône 

                // Calculer la position pour centrer l'icône
                int centerX = (width - iconSize) / 3;
                int centerY = (height - iconSize) / 3;

                // Dessiner le carré centré
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(centerX, centerY, iconSize, iconSize);

                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        };
    }

    private static Icon minimizeIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = c.getWidth();
                int height = c.getHeight();
                int iconSize = Math.min(width, height) - 15; // Taille de l'icône avec marge

                // Calculer la position pour centrer l'icône
                int centerX = (width - iconSize) / 3;
                int centerY = (height - iconSize) / 3;

                // Dessiner la ligne horizontale centrée
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2));

                int lineY = centerY + iconSize / 2;
                g2d.drawLine(
                        centerX,
                        lineY,
                        centerX + iconSize,
                        lineY
                );

                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        };
    }

    
    /**
     * Méthode factory pour créer un HFrame avec un style prédéfini.
     * 
     * @param title le titre de la fenêtre
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HFrame configurée avec le style spécifié
     */
    public static HFrame withStyle(String title, HFrameStyle style) {
        HFrame frame = new HFrame(title);
        frame.setFrameStyle(style);
        return frame;
    }
}