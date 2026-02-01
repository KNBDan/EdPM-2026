package objects.line;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.JComponent;
import objects.render.Arrow;
import objects.render.SVGDrawable;

public abstract class Line extends JComponent implements SVGDrawable {
    int ss=10;
    double x, y;
    Point2D c1, c2;// координаты центра точек
    public ArrayList<Point2D> point = new ArrayList();
    public ArrayList<Shape> pointShape = new ArrayList();
    int id11, id22;//id  соединяемых объектов
    String ID1, ID2;//имя соединяемоых объектов
    public Arrow arrow;//стрелка
    
    public Line(){
    }
    public void setC2(Point2D c2) {
        this.c2 = c2;
    }
    public void setC1(Point2D c1) {
        this.c1 = c1;
    }
    public void setCC(Point2D c1, Point2D c2) {
        this.c2 = c2;
        this.c1=c1;
    }
    public Point2D getC1(){
        return c1;
    }
    public Point2D getC2(){
        return c2;
    }
    public int getID11(){
        return id11;
    }
    public int getID22(){
        return id22;
    }
    public String getID1(){
        return ID1;
    }
    public String getID2(){
        return ID2;
    }
    public void setID1(String ID1){
        this.ID1=ID1;
    }
    public void setID2(String ID2){
        this.ID2=ID2;
    }
    public void setID22(int id22){
        this.id22=id22;
    }

    @Override
    public abstract String toSVG();
}
