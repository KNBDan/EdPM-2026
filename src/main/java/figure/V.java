package figure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import javax.swing.*;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

public class V extends figures {
    public static Color BackgroundColor;
    public static Color TextColor;
  
    static {
        try {
            if (!prefs.nodeExists("")) {
                BackgroundColor = Color.WHITE;
                TextColor = Color.BLACK;

                prefs.putInt("VBackgroundColor", BackgroundColor.getRGB());
                prefs.putInt("VTextColor", TextColor.getRGB());
            } else {
                BackgroundColor = new Color(prefs.getInt("VBackgroundColor", Color.WHITE.getRGB()));
                TextColor = new Color(prefs.getInt("VTextColor", Color.BLACK.getRGB()));
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
            BackgroundColor = Color.WHITE;
            TextColor = Color.BLACK;
        }
    }

    public V(int x, int y, int s, int idV_in, int id_in, String nameF, String descriptionF) {
        this.x = x;
        this.y = y;
        this.absoluteX = x;
        this.absoluteY = y;
        this.s = s;
        this.id = id_in;
        this.idV = idV_in;
        //this.nameF = "V" + idV;
        this.nameF = nameF;
        this.descriptionF = descriptionF;
        this.vSelected = "Экспонента (exp(x))";
    }

    Font font = new Font("Arial", Font.BOLD, 24);

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Font font = new Font("Arial", Font.BOLD, (int)(24*s/100));
        g2.setFont(font);        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        
        GeneralPath gp = new GeneralPath();
        // Точки шестиугольника, где (x,y) - левый верхний угол
        gp.moveTo(x + s/4, y);               // верхняя середина
        gp.lineTo(x + 3*s/4, y);             // верхняя правая
        gp.lineTo(x + s, y + s/4);           // правая середина
        gp.lineTo(x + 3*s/4, y + s/2);        // нижняя правая
        gp.lineTo(x + s/4, y + s/2);           // нижняя середина
        gp.lineTo(x, y + s/4);               // левая середина
        gp.closePath();                      // замыкаем путь
        
        g2.setColor(BackgroundColor);
        g2.fill(gp);
        g2.setColor(TextColor);
        g2.setStroke(new BasicStroke(2));
        g2.draw(gp);

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(nameF);
        
        // Центр фигуры по X
        int centerX = x + s/2;
        // Центр фигуры по Y (учитывая форму ромба)
        int centerY = y + s/4;
        
        // Координаты для текста
        int textX = centerX - textWidth/2;
        int textY = centerY + fm.getAscent()/2 - 2;
        
        g2.drawString(nameF, textX, textY);
        
        shape = gp;
        rec = shape.getBounds2D();
    }


    
}