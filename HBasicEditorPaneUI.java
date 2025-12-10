/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HEditorPane;
import hcomponents.vues.border.HBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.StringReader;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicEditorPaneUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * UI personnalisée pour HEditorPane
 *
 * @author FIDELE
 */
public class HBasicEditorPaneUI extends BasicEditorPaneUI {

     private boolean hasFocus = false;
     private boolean contentError = false;

    // ---- Animation du placeholder ----
    private float placeholderY;       // Position actuelle
    private float placeholderFont;   // Taille actuelle


    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        JTextComponent txt = (JTextComponent) c;
        
        // Gestion du focus
        c.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                hasFocus = true;
                c.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                hasFocus = false;
                c.repaint();
            }
        });

        
        // Listener du texte       
        txt.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) {  }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) {  }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) {  }
        });
    }

   

    //DESSIN DU COMPOSANT
    @Override
    protected void paintBackground(Graphics g) {
        JComponent c = getComponent();
        if (!(c instanceof HEditorPane)) {
            super.paintBackground(g);
            return;
        }

        HEditorPane editorPane = (HEditorPane) c;
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = c.getWidth();
        int height = c.getHeight();

        g2.setColor(editorPane.getEditorStyle().getBackground());
        g2.fillRoundRect(0, 0, width, height, editorPane.getCornerRadius(), editorPane.getCornerRadius());

        g2.dispose();
    }

    @Override
    protected void paintSafely(Graphics g) {
        JComponent c = getComponent();
        if (!(c instanceof HEditorPane)) {
            super.paintSafely(g);
            return;
        }

        HEditorPane editorPane = (HEditorPane) c;
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fond
        paintBackground(g2);

        // Texte normal
        super.paintSafely(g2);

        // Placeholder
        paintPlaceholder(g2, editorPane);

        // Bordure custom
        HBorder border = editorPane.getHBorder();
        if (border != null) {
            border.paint(g2, editorPane, c.getWidth(), c.getHeight(), editorPane.getCornerRadius());
        }

        // Focus border
        if (hasFocus && editorPane.getEditorStyle().getFocusBorderColor() != null) {
            g2.setColor(editorPane.getEditorStyle().getFocusBorderColor());
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3,
                    editorPane.getCornerRadius(), editorPane.getCornerRadius());
        }

        g2.dispose();
    }

    private void paintPlaceholder(Graphics2D g2, HEditorPane editorPane) {
        JTextComponent txt = (JTextComponent) getComponent();
        String text = txt.getText();

        if (text != null && !text.trim().isEmpty())
            return; // pas de placeholder

        String placeholder = editorPane.getPlaceholder();
        if (placeholder == null || placeholder.isEmpty())
            return;

        Insets in = getComponent().getInsets();

        g2.setColor(editorPane.getEditorStyle().getPlaceholderColor());
        g2.setFont(getComponent().getFont().deriveFont(placeholderFont));

        g2.drawString(placeholder, in.left + 4, placeholderY);
    }
    
    private boolean validateContent(JComponent c){
        JTextComponent txt = (JTextComponent) getComponent();
        String text = txt.getText();

        HEditorPane pane = (HEditorPane) c;
        
    // 1. Vérification simple : texte vide
    if (text == null || text.trim().isEmpty()) {
        this.contentError = true;        
    }

    // 2. Vérification de longueur maximale
    if (text.trim().length() < pane.getMaxLength()) {
        this.contentError = true;        
    }

    // 3. Si l’editor est en mode HTML, vérifier que le HTML est valide
    if (pane.getEditorKit() instanceof HTMLEditorKit) {
        HTMLEditorKit kit = new HTMLEditorKit();
        try {
            kit.read(new StringReader(text), new HTMLDocument(), 0);
        } catch (IOException | BadLocationException e) {
            // Le HTML est mal formé 
             this.contentError = true;       
            //Renvoyer le message d'erreur 
        }
    }
    return contentError;
}
    
}
