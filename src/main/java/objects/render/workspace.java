package objects.render;
//package figure;
//
//import java.awt.Color;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Polygon;
//import java.awt.RenderingHints;
//import javax.swing.JPanel;
////import /*weblaf*/;
//
//public class workspace extends JPanel{
//    private int w=100;
//    private int h=100;
//    private int x;
//    private int y;
//    private String fig;
//    public workspace(){
//    }
//    public void draw ( int width, int height, int xNew, int yNew, String s){
//        fig= s;
//        w=width;
//        h=height;
//        x=xNew;
//        y=yNew;   
//    }
//
//    public void paintComponent(Graphics g){
//            Graphics2D g2 = (Graphics2D) g;
//            g2.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
//            g2.setColor(Color.DARK_GRAY);
//            switch(fig){
//                case "S" -> g2.drawRoundRect(w/2-x/2, h/2-y/2, x, y, 50, 100);
//                case "V" -> g2.drawRect(getWidth()/2-x/2, getHeight()/2-y/2, x, y);
//                case "R" -> {
//                    Polygon p = new Polygon();
//                    p.addPoint(getWidth()/2-7*x/10, getHeight()/2+y/4);
//                    p.addPoint(getWidth()/2-3*x/10, getHeight()/2-y/4);
//                    p.addPoint(getWidth()/2+7*x/10, getHeight()/2-y/4);
//                    p.addPoint(getWidth()/2+3*x/10, getHeight()/2+y/4);
//                    p.addPoint(getWidth()/2-7*x/10, getHeight()/2+y/4);
//                    g2.drawPolygon(p);
//                }
//                case "NV" -> {
//                    Polygon p2 = new Polygon();
//                    p2.addPoint(getWidth()/2-x, getHeight()/2);
//                    p2.addPoint(getWidth()/2, getHeight()/2-y/2);
//                    p2.addPoint(getWidth()/2+x, getHeight()/2);
//                    p2.addPoint(getWidth()/2, getHeight()/2+y/2);
//                    p2.addPoint(getWidth()/2-x, getHeight()/2);
//                    g2.drawPolygon(p2);
//                }
//        }
//    }
//}
