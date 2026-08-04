package illustrations;

import javax.swing.*;
import java.awt.*;
import illustrations.Shapes.ShapeType;

public class TestShapes {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Test Lot 1 - Lignes");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                ShapeType[] types = ShapeType.values();
                int cols = 3;
                int cellW = 280, cellH = 150;

                for (int i = 0; i < types.length; i++) {
                    int col = i % cols;
                    int row = i / cols;
                    int x = col * cellW + 40;
                    int y = row * cellH + 30;

                    Shapes shape = new Shapes(types[i], x, y, 180, 60);
                    shape.setFillColor(Color.ORANGE);
                    shape.setStrokeColor(Color.BLACK);
                    shape.setStrokeWidth(2f);
                    shape.paint(g2d);

                    g2d.setColor(Color.DARK_GRAY);
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    g2d.drawString(types[i].name(), x, y + 80);
                }
            }
        };
        panel.setBackground(Color.WHITE);

        frame.add(new JScrollPane(panel));
        frame.setVisible(true);
    }
}