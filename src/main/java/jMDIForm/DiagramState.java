package jMDIForm;




import figure.Line;
import figure.LineStraight;
import figure.NV;
import figure.O;
import figure.R;
import figure.S1;
import figure.V;
import figure.d;
import figure.figures;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class DiagramState {
    private final ArrayList<figures> figures;
    private final ArrayList<Line> lines;
    private final int zoom;

    public DiagramState(ArrayList<figures> figures, ArrayList<Line> lines, int zoom) {
        this.figures = deepCopyFigures(figures);
        this.lines = deepCopyLines(lines);
        this.zoom = zoom;
    }

    // Глубокое копирование списка фигур
    private ArrayList<figures> deepCopyFigures(ArrayList<figures> original) {
        if (original == null) return new ArrayList<>();

        ArrayList<figures> copy = new ArrayList<>();
        for (figures fig : original) {
            copy.add(copyFigure(fig));
        }
        return copy;
    }

    // Копирование одной фигуры
    private figures copyFigure(figures fig) {
        // Здесь создаём новый экземпляр каждого типа фигуры
        if (fig instanceof S1 s1) {
            return new S1(s1.getXX(), s1.getYY(), (int) s1.getS(), s1.getIdS(), s1.getId(), s1.getNameF(), s1.getDescriptionF());
        } else if (fig instanceof V v) {
            return new V(v.getXX(), v.getYY(), (int) v.getS(), v.getIdV(), v.getId(), v.getNameF(), v.getDescriptionF());
        } else if (fig instanceof R r) {
            return new R(r.getXX(), r.getYY(), (int) r.getS(), r.getIdR(), r.getId(), r.getNameF(), r.getDescriptionF());
        } else if (fig instanceof NV nv) {
            return new NV(nv.getXX(), nv.getYY(), (int) nv.getS(), nv.getIdNV(), nv.getId(), nv.getNameF(), nv.getDescriptionF());
        } else if (fig instanceof d dObj) {
            return new d(dObj.getXX(), dObj.getYY(), (int) dObj.getS(), dObj.getIdIF(), dObj.getId(), dObj.getNameF(), dObj.getDescriptionF());
        } else if (fig instanceof O o) {
            return new O(o.getXX(), o.getYY(), (int) o.getS(), o.getIdO(), o.getId(), o.getNameF(), o.getDescriptionF());
        } else {
            throw new UnsupportedOperationException("Unknown figure type: " + fig.getClass().getName());
        }
    }

    // Глубокое копирование списка линий
    private ArrayList<Line> deepCopyLines(ArrayList<Line> original) {
        if (original == null) return new ArrayList<>();

        ArrayList<Line> copy = new ArrayList<>();
        for (Line line : original) {
            copy.add(copyLine(line));
        }
        return copy;
    }

    // Копирование одной линии
    private Line copyLine(Line line) {
        if (line instanceof LineStraight ls) {
            return new LineStraight(
                new Point2D.Double(ls.getC1().getX(), ls.getC1().getY()),
                new Point2D.Double(ls.getC2().getX(), ls.getC2().getY()),
                ls.getID1(),
                ls.getID11(),
                ls.getID2(),
                ls.getID22()
            );
        } else {
            throw new UnsupportedOperationException("Unknown line type: " + line.getClass().getName());
        }
    }

    public ArrayList<figures> getFigures() {
        return figures;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public int getZoom() {
        return zoom;
    }
}