package jMDIForm;

import objects.figure.*;

public class readSaveData {
    //Код для очищения основного файла. Определяем тип, который необходимо создать и создаем его.
    public figures getElement(logic.serialization.model.Figure_s curFigF/*String shape, String Sx, String Sy, String Ss, String name, String code, String description, ArrayList<String> inVariable,ArrayList<String> outVariable/*, String SWork/*, String coef, String likelihood, String period*/){
        int x = Integer.parseInt(curFigF.getX_pos());
        int y = Integer.parseInt(curFigF.getY_pos());
        int s = Integer.parseInt(curFigF.getSize());

        figures currentFigure = new figures() {

        };   
        switch(curFigF.getShape()){
            case("d"):
                currentFigure = new d(x-s/2, y-s/4, s,currentFigure.getId(),currentFigure.getIdIF(), currentFigure.getNameF(), currentFigure.getDescriptionF());
                currentFigure.setIfNvElement(curFigF.getIfNvElement());
                currentFigure.setIfSelected(curFigF.getIfSelected());
                currentFigure.setSignIfSelected(curFigF.getSignIfSelected());
                currentFigure.setCompareNumber(curFigF.getCompareNumber());
                
                break;
            case("V"):
                currentFigure = new V(x-s/4, y-s/4, s, currentFigure.getId(), currentFigure.getIdV(), currentFigure.getNameF(), currentFigure.getDescriptionF());
                currentFigure.setVSelected(curFigF.getVSelected());
                break;
            case("R"):
                currentFigure = new R(x-s/4, y-s/4, s, currentFigure.getId(), currentFigure.getIdR(), currentFigure.getNameF(), currentFigure.getDescriptionF());
                break;
            case("S1"):
                currentFigure = new S1(x, y, s,currentFigure.getId(), currentFigure.getIdS(), currentFigure.getNameF(), currentFigure.getDescriptionF());
                currentFigure.setSwork( Integer.parseInt(curFigF.getSwork()));
                if (currentFigure.getSwork() == 0){
                    currentFigure.setLikelihood(curFigF.getLikelihood());
                }
                else{
                    currentFigure.setPeriod(curFigF.getPeriod());
                }
               
                break;    
            case("O"):
                currentFigure = new O(x-s/4, y-s/4, s, currentFigure.getId(), currentFigure.getIdO(), currentFigure.getNameF(), currentFigure.getDescriptionF());
                currentFigure.setCoef(curFigF.getCoef());
                break;
            case("NV"):
                currentFigure = new NV(x-s/4, y-s/4, s, currentFigure.getId(), currentFigure.getIdNV(), currentFigure.getNameF(), currentFigure.getDescriptionF());
                break;
               
        }
        currentFigure.setVisible(true);
        currentFigure.setNameF(curFigF.getName());
        currentFigure.setDescriptionF(curFigF.getDescription());
        currentFigure.setNameNvElement(curFigF.getNameNvElement());
        currentFigure.setVarNvElement(curFigF.getVarNvElement());
        currentFigure.setCodeF(curFigF.getCode());
        currentFigure.setLlmPrompt(curFigF.getLlmPrompt());
        return currentFigure;
    }
    
}
