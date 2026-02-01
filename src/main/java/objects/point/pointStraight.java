package objects.point;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class pointStraight extends points {


    
    
    public pointStraight(Rectangle2D r) {
        this.r = r;
        this.x = r.getCenterX();
        this.y = r.getCenterY();
        this.w = r.getWidth();
        this.h = r.getHeight();
        this.p1 = new Point2D.Double(x - w / 2, y);
        this.p2 = new Point2D.Double(x, y - h / 2);
        this.p3 = new Point2D.Double(x + w / 2, y);
        this.p4 = new Point2D.Double(x, y + h / 2);
        this.point.add(p1);
        this.point.add(p2);
        this.point.add(p3);
        this.point.add(p4);
        for (Point2D b : point) {
            if (b == p1) {
                shape = new Arc2D.Double(b.getX() - ss / 2, b.getY() - ss / 2, ss, ss, 270, 180, Arc2D.OPEN);
            } else if (b == p2) {
                shape = new Arc2D.Double(b.getX() - ss / 2, b.getY() - ss / 2, ss, ss, 180, 180, Arc2D.OPEN);
            } else if (b == p3) {
                shape = new Arc2D.Double(b.getX() - ss / 2, b.getY() - ss / 2, ss, ss, 90, 180, Arc2D.OPEN);
            } else {
                shape = new Arc2D.Double(b.getX() - ss / 2, b.getY() - ss / 2, ss, ss, 0, 180, Arc2D.OPEN);
            }
            this.pointShape.add(shape);
        }
    }
Font font = new Font("Arial", Font.BOLD, 24);
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        for (Point2D b : point) {
//            shape = new Ellipse2D.Double(b.getX() - ss / 2, b.getY() - ss / 2, ss, ss);
//            pointShape.add(shape);
//        }
        int k=pointShape.size();
        for (Shape sh : pointShape) {
            g2.setColor(Color.WHITE);
            g2.fill(sh);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.draw(sh);
            g2.setFont(font);
            
            String str=Integer.toString(k); 
           // g2.drawString(str, (20)+17, (20)+34);
           
        }
    }

    
}
