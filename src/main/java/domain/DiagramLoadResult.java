package domain;

import objects.line.Line;
import objects.figure.figures;
import java.util.ArrayList;

public class DiagramLoadResult {
    private final ArrayList<figures> figures;
    private final ArrayList<Line> lines;
    private final int zoom;
    private final int idS;
    private final int idNV;
    private final int idV;
    private final int idR;
    private final int idO;
    private final int idIF;

    public DiagramLoadResult(
            ArrayList<figures> figures,
            ArrayList<Line> lines,
            int zoom,
            int idS,
            int idNV,
            int idV,
            int idR,
            int idO,
            int idIF
    ) {
        this.figures = figures;
        this.lines = lines;
        this.zoom = zoom;
        this.idS = idS;
        this.idNV = idNV;
        this.idV = idV;
        this.idR = idR;
        this.idO = idO;
        this.idIF = idIF;
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

    public int getIdS() {
        return idS;
    }

    public int getIdNV() {
        return idNV;
    }

    public int getIdV() {
        return idV;
    }

    public int getIdR() {
        return idR;
    }

    public int getIdO() {
        return idO;
    }

    public int getIdIF() {
        return idIF;
    }
}
