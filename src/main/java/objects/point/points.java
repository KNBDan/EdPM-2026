package objects.point;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.JComponent;

public class points extends JComponent {

    Shape shape;
    int ss = 12;
    double x, y, w, h;
    Point2D p1, p2, p3, p4;// координаты точки (её центр)
    Rectangle2D r;
    ArrayList<Point2D> point = new ArrayList();
    ArrayList<Shape> pointShape = new ArrayList();

    public points() {
    }

    public void setRec(Rectangle2D r) {
        this.r = r;
    }
    public ArrayList<Shape> getShape(){
       return pointShape;       
    }
//
    public Shape getShape(int i) {
        return pointShape.get(i);
    }

    public ArrayList<Point2D> getPoint() {
        return point;
    }

}
