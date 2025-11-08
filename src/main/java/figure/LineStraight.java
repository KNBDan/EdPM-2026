package figure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class LineStraight extends Line{
    public LineStraight(Point2D c1, Point2D c2, String ID1, int id11, String ID2, int id22) {
        this.c1=c1;//координаты цетров соединяемых точек
        this.c2=c2;
        this.ID2=ID2;
        this.id11=id11;
        this.id22=id22;
        this.ID1=ID1;
        this.arrow = new Arrow(c1, c2);
    }
    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        g2.setColor(Color.BLACK);
        GeneralPath gp = new GeneralPath();
        gp.moveTo(c1.getX(), c1.getY());//startpoint
        gp.lineTo(c2.getX(), c2.getY());//nextpoint
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.draw(gp);
        arrow.x1 = c1.getX();
        arrow.y1 = c1.getY();
        arrow.x2 = c2.getX();
        arrow.y2 = c2.getY();
        arrow.paintComponent(g);
        }
    
    public String toSVG() {
        return "<line x1=\"" + c1.getX() + "\" y1=\"" + c1.getY() +
               "\" x2=\"" + c2.getX() + "\" y2=\"" + c2.getY() +
               "\" stroke=\"black\" stroke-width=\"2\"/>\n";
}
}
