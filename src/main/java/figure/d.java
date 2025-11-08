package figure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

public class  d extends figures{//document
//int s, x, y;
    public static Color BackgroundColor;
    public static Color TextColor;

    static {
        // Проверяем, существует ли узел
        try {
            if (!prefs.nodeExists("")) {
                // Узел не существует - устанавливаем значения по умолчанию и сохраняем их
                BackgroundColor = Color.WHITE;
                TextColor = Color.BLACK;

                prefs.putInt("IFBackgroundColor", BackgroundColor.getRGB());
                prefs.putInt("IFTextColor", TextColor.getRGB());
            } else {
                // Узел существует - загружаем значения
                BackgroundColor = new Color(prefs.getInt("IFBackgroundColor", Color.WHITE.getRGB()));
                TextColor = new Color(prefs.getInt("IFTextColor", Color.BLACK.getRGB()));
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
            // В случае ошибки устанавливаем значения по умолчанию
            BackgroundColor = Color.WHITE;
            TextColor = Color.BLACK;
        }
    }
    public d(int x, int y, int s, int idIF_in, int id_in, String nameF, String descriptionF) {
        this.x=x + s/2;
        this.y=y + s/4;
        this.absoluteX = this.x;
        this.absoluteY = this.y;
        this.s=s;
//        this.nameF = "D(IF)" + this.id;
        this.id = id_in;
        this.idIF = idIF_in; 
        //this.nameF = "IF" + idIF;
        this.nameF = nameF;
        this.descriptionF = descriptionF;           
    }
    Font font = new Font("Arial", Font.BOLD, 20);
    @Override
    public void paintComponent(Graphics g){
         Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = new Font("Arial", Font.BOLD, (int)(24*s/100));
        g2.setFont(font);

        // Отрисовка ромба (условного блока)
        GeneralPath gp = new GeneralPath();
        int centerX = x + s/2;  // Центр по X
        int centerY = y + s/4;  // Центр по Y
        
        // Вершины ромба относительно (x,y)
        gp.moveTo(centerX - s/4, centerY - s/4); // Верх
        gp.lineTo(centerX, centerY);              // Право
        gp.lineTo(centerX - s/4, centerY + s/4);  // Низ
        gp.lineTo(centerX - s/2, centerY);        // Лево
        gp.closePath();

        g2.setColor(BackgroundColor);
        g2.fill(gp);
        g2.setColor(TextColor);
        g2.setStroke(new BasicStroke(2));
        g2.draw(gp);

        // Центрирование текста
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(nameF);
        
        int textX = centerX - textWidth/2 - s/4;
        int textY = centerY + fm.getAscent()/2 - 2;
        
        g2.drawString(nameF, textX, textY);

        shape = gp;
        rec = gp.getBounds2D();
    }
    
    public List<Point2D> getConnectionPoints() {
        List<Point2D> points = new ArrayList<>();
        int centerX = x + s/2;
        int centerY = y + s/4;
        
        // Точки соединения в середине каждой стороны
        points.add(new Point2D.Double(centerX - s/4, centerY - s/8)); // Верх
        points.add(new Point2D.Double(centerX - s/8, centerY));       // Право
        points.add(new Point2D.Double(centerX - s/4, centerY + s/8)); // Низ
        points.add(new Point2D.Double(centerX - 3*s/8, centerY));     // Лево
        
        return points;
    }



}