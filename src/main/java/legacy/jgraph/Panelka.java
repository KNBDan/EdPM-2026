package legacy.jgraph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.JComponent;

public class Panelka extends JComponent {
    public int x2, y2;
        Font font = new Font("Arial", Font.BOLD, 18);
       @Override
        protected void paintComponent(Graphics g){
            g.setFont(font);
        x2 = 100000;
        y2 = 100000;
        g.setColor( Color.BLACK );
        g.drawLine( 0, 0, x2, y2);
        System.out.println("!Line!");
        }
    
}
