package legacy.point;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class pointDotted extends points{
    
    public pointDotted(Rectangle2D r) {
        this.r=r;
        p1 = new Point2D.Double(r.getCenterX()-r.getWidth()/2, r.getCenterY());
        p2 = new Point2D.Double(r.getCenterX(), r.getCenterY()-r.getHeight()/2);
        p3 = new Point2D.Double(r.getCenterX()+r.getWidth()/2, r.getCenterY());
        p4 = new Point2D.Double(r.getCenterX(), r.getCenterY()+r.getHeight()/2);
        point.add(p1);
        point.add(p2);
        point.add(p3);
        point.add(p4);
    }
    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        g2.setColor(Color.DARK_GRAY);
        for (Point2D b : point) {
            shape = new Ellipse2D.Double(b.getX()-ss/2, b.getY()-ss/2, ss, ss);
            pointShape.add(shape);
            }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        for (Shape sh : pointShape) {
            g2.setColor(Color.WHITE);
            g2.fill(sh);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.draw(sh);
            }
        }
}
