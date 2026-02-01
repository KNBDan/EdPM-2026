package objects.figure;
//package figure;
//
//import java.awt.Color;
//import java.awt.Font;
//import java.awt.FontMetrics;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;
//import java.awt.Shape;
//import java.awt.geom.Ellipse2D;
//import java.awt.geom.Point2D;
//import java.awt.geom.Rectangle2D;
//import java.awt.geom.RoundRectangle2D;
//import javax.swing.*;
//
//public class S extends figures {
//    //id
////    private static int counter = 0;
////    public final int id;
////    double xx;
//    Color BackgroundColor = Color.WHITE;
//    Color TextColor = Color.BLACK;    
//    String str = "Start/End";
//
//    public S(int x, int y, int s) {
//        this.x = x;
//        this.y = y;
//        this.s = s;
//        id = nextId.incrementAndGet();
//        this.BackgroundColor = Color.WHITE;
//        this.TextColor = Color.BLACK;        
//    }
//
//    Font font = new Font("Times new Roman", Font.PLAIN, 18);
//
//    @Override
//    public void paintComponent(Graphics g) {
//        Graphics2D g2 = (Graphics2D) g;
//        shape = new RoundRectangle2D.Double(x, y, s, s/2, 50, 100);
//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.setColor(new Color(113, 171, 226));
//        g2.fill(shape);
//        g2.setColor(Color.DARK_GRAY);
//        g2.draw(shape);
//        g2.setFont(font);
//        g2.drawString(str, 0, s / 4); // отрисовка текста в верхнем левом углу фигуры
//    }
//}
