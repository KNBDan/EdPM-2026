package logic.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import logic.serialization.model.ConvertedObject;
import logic.serialization.model.Figure_s;
import logic.serialization.model.Line_s;
import objects.line.Line;
import objects.line.LineStraight;
import objects.figure.figures;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import jMDIForm.readSaveData;
import domain.DiagramLoadResult;

public class DiagramSerializer {

    public ConvertedObject createConvertedObject(
            List<figures> all,
            List<Line> lines,
            int zoom,
            int idS,
            int idNV,
            int idV,
            int idR,
            int idO,
            int idIF
    ) {
        ArrayList<Figure_s> figuresList = new ArrayList<>();

        for (figures f : all) {
            Figure_s fig = new Figure_s();
            String gType = f.getClass().toString();
            gType = gType.replace("class objects.figure.", "");
            fig.setX_pos(Integer.toString(f.getAbsoluteX()));
            fig.setY_pos(Integer.toString(f.getAbsoluteY()));
            fig.setShape(gType);
            fig.setSize(Integer.toString(f.getSises()));
            fig.setId(Integer.toString(f.getId()));
            fig.setName(f.getNameF());
            fig.setDescription(f.getDescriptionF());
            fig.setCode(f.getCodeF());
            fig.setNameNvElement(f.getNameNvElement());
            fig.setVarNvElement(f.getVarNvElement());
            fig.setSwork(String.valueOf((int) f.getSwork()));
            fig.setLikelihood(f.getLikelihood());
            fig.setPeriod(f.getPeriod());
            fig.setCoef(f.getCoef());
            fig.setVSelected(f.getVSelected());
            fig.setLlmPrompt(f.getLlmPrompt());
            fig.setIfSelected(f.getIfSelected());
            fig.setIfNvElement(f.getIfNvElement());
            fig.setSignIfSelected(f.getSignIfSelected());
            fig.setCompareNumber(f.getCompareNumber());
            figuresList.add(fig);
        }

        ArrayList<Line_s> linesList = new ArrayList<>();
        for (Line currentLine : lines) {
            Line_s ln = new Line_s();
            ln.SetID1(currentLine.getID1());
            ln.SetID2(currentLine.getID2());
            ln.SetId11(currentLine.getID11());
            ln.SetId22(currentLine.getID22());

            Point2D c1 = new Point2D.Double(
                currentLine.getC1().getX() * 100.0 / zoom,
                currentLine.getC1().getY() * 100.0 / zoom
            );
            Point2D c2 = new Point2D.Double(
                currentLine.getC2().getX() * 100.0 / zoom,
                currentLine.getC2().getY() * 100.0 / zoom
            );

            ln.SetC1(c1);
            ln.SetC2(c2);

            linesList.add(ln);
        }

        return new ConvertedObject(
                linesList,
                figuresList,
                zoom,
                idS, idNV, idV, idR, idO, idIF
        );
    }

    public void saveToJson(String fn, ConvertedObject converted) throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.writeValue(Path.of(fn).toFile(), converted);
    }

    public DiagramLoadResult loadFromJson(String saveName) throws IOException {
        ObjectMapper om = new ObjectMapper();
        Path filePath = Path.of(saveName);
        String jsonString = Files.readString(filePath);
        ConvertedObject cv = om.readValue(jsonString, ConvertedObject.class);

        int savedZoom = cv.getZoom();
        int zoom = savedZoom;
        if (zoom == 0) {zoom = 100;}

        int idS = cv.getIdS();
        int idNV = cv.getIdNV();
        int idV = cv.getIdV();
        int idR = cv.getIdR();
        int idO = cv.getIdO();
        int idIF = cv.getIdIF();

        ArrayList<figures> all = new ArrayList<>();
        ArrayList<Line> lines = new ArrayList<>();

        List<Figure_s> figuresList = cv.getCurrentFigures();
        if (figuresList == null) {
            figuresList = List.of();
        }
        readSaveData rs = new readSaveData();
        for (Figure_s fig : figuresList) {
            figures loadedFigure = rs.getElement(fig);

            loadedFigure.setAbsoluteX(Integer.parseInt(fig.getX_pos()));
            loadedFigure.setAbsoluteY(Integer.parseInt(fig.getY_pos()));

            loadedFigure.setXX((int) Math.round(loadedFigure.getAbsoluteX() * zoom / 100.0));
            loadedFigure.setYY((int) Math.round(loadedFigure.getAbsoluteY() * zoom / 100.0));

            loadedFigure.setS((int) Math.round(Integer.parseInt(fig.getSize())));

            all.add(loadedFigure);
        }

        List<Line_s> lineList = cv.getCurrentLine();
        if (lineList == null) {
            lineList = List.of();
        }
        for (Line_s line_from_file : lineList) {
            Point2D scaledC1 = new Point2D.Double(
                line_from_file.GetC1().getX() * zoom / 100.0,
                line_from_file.GetC1().getY() * zoom / 100.0
            );

            Point2D scaledC2 = new Point2D.Double(
                line_from_file.GetC2().getX() * zoom / 100.0,
                line_from_file.GetC2().getY() * zoom / 100.0
            );

            LineStraight ls = new LineStraight(
                scaledC1, scaledC2,
                line_from_file.GetID1(),
                line_from_file.GetId11(),
                line_from_file.GetID2(),
                line_from_file.GetId22()
            );
            lines.add(0, ls);
        }

        return new DiagramLoadResult(all, lines, zoom, idS, idNV, idV, idR, idO, idIF);
    }
}
