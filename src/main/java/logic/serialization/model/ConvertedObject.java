package logic.serialization.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
//Совмещает в себе все классы, которые нужно сохранить в json
public class ConvertedObject {
    private ArrayList<Line_s> currentLine;
    private ArrayList<Figure_s> currentFigures;
    private int zoom;
    private int idS;
    private int idNV;
    private int idV;
    private int idR;
    private int idO;
    private int idIF;

    public ConvertedObject() {
    }
    
    public ConvertedObject(ArrayList<Line_s> currentLine, ArrayList<Figure_s> currentFigures, int zoom, 
                          int idS, int idNV, int idV, int idR, int idO, int idIF) {
        this.currentLine = currentLine;
        this.currentFigures = currentFigures;
        this.zoom = zoom;
        this.idS = idS;
        this.idNV = idNV;
        this.idV = idV;
        this.idR = idR;
        this.idO = idO;
        this.idIF = idIF;
    }
    
    public void setCurrentLine(ArrayList<Line_s> linesList){
        this.currentLine = linesList;
    }
    public void setCurrentFigures(ArrayList<Figure_s> figuresList){
        this.currentFigures = figuresList;
    }
    public ArrayList<Line_s> getCurrentLine(){
        return currentLine;
    }
    public ArrayList<Figure_s> getCurrentFigures(){
        return currentFigures;
    }
    
    
    public int getZoom() {
        return zoom;
    }

    public void setZoom(int CurrentZoom) {
        zoom = CurrentZoom;
    }

    public int getIdS() {
        return idS; 
    }

    public int getIdV() {
        return idV; 
    }
    
    public int getIdNV() {
        return idNV; 
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
