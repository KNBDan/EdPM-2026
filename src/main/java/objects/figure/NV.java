package objects.figure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

public class NV extends figures {

    public static Color BackgroundColor;
    public static Color TextColor;

    static {
        try {
            if (!prefs.nodeExists("")) {
                BackgroundColor = Color.WHITE;
                TextColor = Color.BLACK;
                prefs.putInt("NVBackgroundColor", BackgroundColor.getRGB());
                prefs.putInt("NVTextColor", TextColor.getRGB());
            } else {
                BackgroundColor = new Color(prefs.getInt("NVBackgroundColor", Color.WHITE.getRGB()));
                TextColor = new Color(prefs.getInt("NVTextColor", Color.BLACK.getRGB()));
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
            BackgroundColor = Color.WHITE;
            TextColor = Color.BLACK;
        }
    }

    public NV(int x, int y, int s, int idNV_in, int id_in, String nameF, String descriptionF) {
        this.x = x; // Теперь (x, y) — левый верхний угол
        this.y = y;
        this.absoluteX = x;
        this.absoluteY = y;
        this.s = s;
        this.id = id_in;
        this.idNV = idNV_in;
        //this.nameF = "NV" + idNV;
        this.nameF = nameF;
        this.descriptionF = descriptionF;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = new Font("Arial", Font.BOLD, (int) (24 * s / 100));
        g2.setFont(font);

        // Отрисовка скругленного прямоугольника с (x, y) в левом верхнем углу
        int width = s;
        int height = s / 2; // Сохраняем пропорции (ширина = 2 * высота)
        shape = new RoundRectangle2D.Double(x, y, width, height, 30, 30); // Радиус скругления = 30

        g2.setColor(BackgroundColor);
        g2.fill(shape);
        g2.setColor(TextColor);
        g2.setStroke(new BasicStroke(2));
        g2.draw(shape);

        // Центрирование текста
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(nameF);

        // Центр фигуры по X
        int centerX = x + s / 2;
        // Центр фигуры по Y (учитывая форму ромба)
        int centerY = y + s / 4;

        // Координаты для текста
        int textX = centerX - textWidth / 2;
        int textY = centerY + fm.getAscent() / 2 - 2;

        g2.drawString(nameF, textX, textY);

        rec = shape.getBounds2D();
    }

}
