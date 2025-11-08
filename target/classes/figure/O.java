package figure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

public class O extends figures {
    public static Color BackgroundColor;
    public static Color TextColor;

    static {
        try {
            if (!prefs.nodeExists("")) {
                BackgroundColor = Color.WHITE;
                TextColor = Color.BLACK;
                prefs.putInt("OBackgroundColor", BackgroundColor.getRGB());
                prefs.putInt("OTextColor", TextColor.getRGB());
            } else {
                BackgroundColor = new Color(prefs.getInt("OBackgroundColor", Color.WHITE.getRGB()));
                TextColor = new Color(prefs.getInt("OTextColor", Color.BLACK.getRGB()));
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
            BackgroundColor = Color.WHITE;
            TextColor = Color.BLACK;
        }
    }

    public O(int x, int y, int s, int idO_in, int id_in, String nameF, String descriptionF) {
        this.x = x; // Левый верхний угол фигуры
        this.y = y;
        this.absoluteX = x;
        this.absoluteY = y;
        this.s = s;
        this.id = id_in;
        this.idO = idO_in; 
        //this.nameF = "O" + idO;
        this.coef = "1";
        this.nameF = nameF;
        this.descriptionF = descriptionF;           
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = new Font("Arial", Font.BOLD, (int)(24 * s / 100));
        g2.setFont(font);

        // Отрисовка ромба (параллелограмма)
        GeneralPath gp = new GeneralPath();
        gp.moveTo(x, y + s/2);          // Нижняя левая точка
        gp.lineTo(x + s/3, y);          // Верхняя левая точка
        gp.lineTo(x + s, y);            // Верхняя правая точка
        gp.lineTo(x + 2*s/3, y + s/2);  // Нижняя правая точка
        gp.closePath();                 // Замыкаем фигуру

        g2.setColor(BackgroundColor);
        g2.fill(gp);
        g2.setColor(TextColor);
        g2.setStroke(new BasicStroke(2));
        g2.draw(gp);

        // Точное центрирование текста
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
        rec = gp.getBounds2D();
    }



}