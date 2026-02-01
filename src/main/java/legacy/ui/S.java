package legacy.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import javax.swing.*;

public class S extends JComponent{

    static Color BackgroundColor;
    static Color TextColor;
    int s, x, y;
    Shape shape;
    String str="Start/End";
    public S(int s, int x, double zoom) {
        this.s=s;
        this.x=x;
        this.y=y;
    }
   
    Font font = new Font("Times new Roman", Font.PLAIN, 18);
    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        g2.setColor(new Color(113,171, 226));
        g2.fillRoundRect(x/2-s/2, y/2-s/4, s, s/2, 50, 100);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRoundRect(x/2-s/2, y/2-s/4, s, s/2, 50, 100);
        g2.setFont(font);
        g2.drawString(str, x/2, y/2);// в дальнейшем кнопка   
    }
}
