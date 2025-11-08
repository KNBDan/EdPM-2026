package jMDIForm;

import figure.*;
import java.util.ArrayList;

public class readSaveData {
    //Код для очищения основного файла. Определяем тип, который необходимо создать и создаем его.
    public figures getElement(converter.Figure_s curFigF/*String shape, String Sx, String Sy, String Ss, String name, String code, String description, ArrayList<String> inVariable,ArrayList<String> outVariable/*, String SWork/*, String coef, String likelihood, String period*/){
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
        return currentFigure;
//        list.add(currentFigure);
        /*if ("d".equals(shape)){
            d dn =  new d(x-s/2, y-s/4, s); 
            dn.setVisible(true);
            dn.setNameF(name);
            dn.setDescriptionF(description);
            dn.setInVariable(inVariable);
            dn.setOutVariable(outVariable);
            dn.setCodeF(code);
            list.add(0,dn);
        }
        if ("V".equals(shape)){
            V dn =  new V(x-s/4, y-s/4, s); 
            dn.setVisible(true);
            dn.setNameF(name);
            dn.setDescriptionF(description);
            dn.setInVariable(inVariable);
            dn.setOutVariable(outVariable);
            dn.setCodeF(code);
            list.add(0,dn);
        }
        if ("R".equals(shape)){
            R dn =  new R(x-s/4, y-s/4, s); 
            dn.setVisible(true);
            dn.setNameF(name);
            dn.setDescriptionF(description);
            dn.setInVariable(inVariable);
            dn.setOutVariable(outVariable);
            dn.setCodeF(code);
            list.add(0,dn);
        }
        if ("NV".equals(shape)){
            NV dn =  new NV(x-s/4, y-s/4, s); 
            dn.setVisible(true);
            dn.setNameF(name);
            dn.setDescriptionF(description);
            dn.setInVariable(inVariable);
            dn.setOutVariable(outVariable);
            dn.setCodeF(code);
            list.add(0,dn);
        }
        if ("S1".equals(shape)){
            S1 dn =  new S1(x, y, s); 
            dn.setVisible(true);
            dn.setNameF(name);
            dn.setDescriptionF(description);
            dn.setInVariable(inVariable);
            dn.setOutVariable(outVariable);
            dn.setCodeF(code);
//            dn.setSwork(SWorkIndex);
//            if (SWorkIndex == 0) {
//                dn.setLikelihood(likelihood);
//            } else if (SWorkIndex == 1) {
//                dn.setPeriod(period);
//            }
            list.add(0,dn);
        }
        if ("O".equals(shape)){
            O dn =  new O(x-s/4, y-s/4, s); 
            dn.setVisible(true);
            dn.setNameF(name);
            dn.setDescriptionF(description);
            dn.setInVariable(inVariable);
            dn.setOutVariable(outVariable);
            dn.setCodeF(code);
//            dn.setCoef(coef);
            list.add(0,dn);
        }
        */
//        return list;
    }
    public ArrayList <Line> getLines(ArrayList <Line> list){
        //выгрузка элементов линий
        return list;
    }
}
