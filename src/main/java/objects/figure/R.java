package objects.figure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.*;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

public class R extends figures {
    public static Color BackgroundColor;
    public static Color TextColor;

    static {
        try {
            if (!prefs.nodeExists("")) {
                BackgroundColor = Color.WHITE;
                TextColor = Color.BLACK;
                prefs.putInt("RBackgroundColor", BackgroundColor.getRGB());
                prefs.putInt("RTextColor", TextColor.getRGB());
            } else {
                BackgroundColor = new Color(prefs.getInt("RBackgroundColor", Color.WHITE.getRGB()));
                TextColor = new Color(prefs.getInt("RTextColor", Color.BLACK.getRGB()));
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
            BackgroundColor = Color.WHITE;
            TextColor = Color.BLACK;
        }
    }

    public R(int x, int y, int s, int idR_in, int id_in, String nameF, String descriptionF) {
        this.x = x; // Теперь (x, y) — левый верхний угол
        this.y = y;
        this.absoluteX = x;
        this.absoluteY = y;
        this.s = s;
        this.id = id_in;
        this.idR = idR_in; 
        //this.nameF = "R" + idR;
        this.nameF = nameF;
        this.descriptionF = descriptionF;        
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = new Font("Arial", Font.BOLD, (int)(24 * s / 100));
        g2.setFont(font);

        // Отрисовка прямоугольника с (x, y) в левом верхнем углу
        int width = s;
        int height = s / 2; // Сохраняем пропорции (ширина = 2 * высота)
        shape = new Rectangle(x, y, width, height);

        g2.setColor(BackgroundColor);
        g2.fill(shape);
        g2.setColor(TextColor);
        g2.setStroke(new BasicStroke(2));
        g2.draw(shape);

        // Центрирование текста
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

        rec = shape.getBounds2D();
    }



}