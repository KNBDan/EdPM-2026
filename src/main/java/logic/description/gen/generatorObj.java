package logic.description.gen;

import EPM.mdi;
import logic.serialization.model.ConvertedObject;
import logic.serialization.model.Figure_s;
import logic.serialization.model.Line_s;
import objects.figure.figures;
import java.util.ArrayList;
import java.util.List;

public class generatorObj {
    private ConvertedObject curObj; //Объект с массивами
    private ArrayList<Figure_s> curFigures = new ArrayList<Figure_s>(); // Массив фигур
    private ArrayList<Line_s> curLines = new ArrayList<Line_s>(); // Массив линий
    ArrayList<subObjR> arrayRs = new ArrayList<subObjR>(); // Связи V->R 
    ArrayList<subObjNV> arrayNVs = new ArrayList<subObjNV>(); // Связи R->NV
    ArrayList<subObjV> arrayVs = new ArrayList<subObjV>();// Связи O,R,NV,S -> V
    ArrayList<subObjIF> arrayIFs = new ArrayList<subObjIF>();// Связи R -> IF ->R,V
    
    public generatorObj(ConvertedObject co){ //стандартный генератор из объекта сохранения*
        curObj = co;
        curFigures = curObj.getCurrentFigures();
        curLines = curObj.getCurrentLine();
    }
    
    //Функции для генерации строк каждого элемента
    public String allSgenerator(){
        String sString = ""; 
        for (Figure_s fig: curFigures){
            if (fig.getShape().equals("S1")){
                sString += fig.getName() +" = Prob()\n";
            }
        }
        return sString;
    }
    public String sStringGenerator(Figure_s fig){
        String subString = "";
        subString += fig.getName() + " = ";
        if (fig.getSwork().equals("0")){
            subString += "prob("+fig.getLikelihood()+")";
        }else{
            subString += "periodic("+fig.getPeriod()+")";
        }
        return subString;
    }
    public String nVStringGenerator(subObjNV objNV){
        String subString = objNV.getName() + " = ";
        subString += generateStringFromSubArray(objNV.getArrayLinkedR());
        return subString;
    }
    public String rStringGenerator(subObjR objR){
        String subString = objR.getName() + " = ";
        boolean isEmpty = true; //для проверки наличия записи в R, иначе не будем добавлять в общую запись
        for (String connectedV: objR.getArrayLinkedV()){
            subObjV objV = findVObjByName(connectedV);
            if (objV == null){
                return "FATALERROR";
            }
            if (objV.isEmpty()){
                return "FATALERROR";
            }
            isEmpty = false;
            subString+=generateVFunction(objV);
        }          
        if (isEmpty){
            return "FATALERROR";
        }
        return subString;
    }
    public String ifStringgenerator(subObjIF objIF){
        String globalResult = "";
        boolean withNvType = false;
        curSpace += 1;
        if (objIF.getLinkedOutFalseV() == null){ //Связь через NV иначе напрямую с V
           withNvType = true;
        }
        String beforeName = ""; //Имя фигуры,первой выходящей из if
        String figVtoR = ""; //Имя V входяхую в пред IF фигуру R
        String subString = "do " + objIF.getName() + "("+ objIF.getCompareElementName()+objIF.getComapreSign()+objIF.getCompareNumber()  +") then\n"+"    ".repeat(curSpace)+"i = i + 1"+ "\n";
        String iskluchenia = objIF.getName(); //имя фигуры входящей в V
        if (withNvType){ //через NV
           beforeName = objIF.getLinkedOutFalseNV();
           figVtoR = findEndOfLine(beforeName, "").getFirst();
           iskluchenia = beforeName;
        }else{ //через V
           beforeName = objIF.getLinkedOutFalseV();
           figVtoR = beforeName;
        }
        ArrayList<String> chain = findRChainBetween(figVtoR, objIF.getLinkedInR());
        if (!chain.isEmpty()) {
            for (String rName : chain) {
                subObjR r = findRObjByName(rName);
                if (r != null) {
                    subString += "    ".repeat(curSpace) + rStringGenerator(r) + "\n";
                }
            }
            curSpace -= 1;
            subString+="    ".repeat(curSpace)+"else "+ objIF.getLinkedOutTrueR()+" = "+objIF.getLinkedInR()+" (i)";
            if (!withNvType){
               subString += "/" +objIF.getLinkedInR() +" (i-1);";
            }
            globalResult+=subString+ "\n";
            return globalResult;
        }
        ArrayList<String> beforest =findStartOfLine(figVtoR,iskluchenia); // всё что входит в фигуру V
        exFiguresString = "";
        createExFigureCode(beforest);
        subString += exFiguresString;
        for (subObjR objR: arrayRs){
            if (objR.getArrayLinkedV().getFirst().equals(figVtoR)){ //ищем связанный с V r для уникального обозначения
                //клонирование V
                subObjV originV = findVObjByName(figVtoR); //оригинальная V, которую нельзя менять
                //Клонирование V
                subObjV objV = new subObjV(figVtoR,originV.getType() , originV.getArrayLinkedSR(), originV.getLinkedNv(), originV.getLinkedO(), originV.getOValue()); //новая фантомная V только для вывода в IF
                if (withNvType){
                    subString +="    ".repeat(curSpace) + beforeName + " = " + objR.getName()+ "\n";
                }
                else{
                    objV.AddToSRList(objR.getName()); //добавляем в список S наш R. Далее необходимо делать это в другом(склонированном) V
                }
                //убрал лишние проверки
                subString+= "    ".repeat(curSpace)+objR.getName() + " = " + generateVFunction(objV)+ "\n";
            }
        }
        curSpace -= 1;
        subString+="    ".repeat(curSpace)+"else "+ objIF.getLinkedOutTrueR()+" = "+objIF.getLinkedInR()+" (i)";
        if (!withNvType){
           subString += "/" +objIF.getLinkedInR() +" (i-1);";
        }
        globalResult+=subString+ "\n";
        return globalResult;
    }      
    
    public String generateString(){
        linkHandler();
        String base = "1";
        String globalResult = "i = " + (mdi.prefsMdi.get("IValue", base))+"\n";
        globalResult += "FP = " + (mdi.prefsMdi.get("FPValue", base))+"\n";
        for (Figure_s curFig :curFigures){  
            if (curFig.getShape().equals("S1")){
                globalResult+=sStringGenerator(curFig)+"\n";
            }
        }
        for (subObjR objR:arrayRs){ //обработка всех R связей
            String subString =  rStringGenerator(objR);
            if (subString.equals("FATALERROR")){
                continue;
            }
            globalResult+=subString+"\n";
        }
        for (subObjNV objNV:arrayNVs){ //Обработка всех NV объектов и их связей
            String subString = nVStringGenerator(objNV);
            globalResult+=subString+"\n";
        }
        
        
        for (subObjIF objIF:arrayIFs){ //Обработка всех IF объектов и их связей
            if (objIF.isCorrect() == false){ //Проверяем на наличие всех элементов в объекте if
                continue;
            } 
            globalResult+=ifStringgenerator(objIF);
        }
        return globalResult;
    }
    String exFiguresString="";
    int curSpace = 0;
    private void createExFigureCode( ArrayList<String> startLineNames){
        for (String figName: startLineNames){
            Figure_s exFindedFig = findFigByName(figName);
            if (exFindedFig == null){
                return ;
            }
            String cShape = exFindedFig.getShape();
            switch(cShape){
                case("S1"):
                    exFiguresString+="    ".repeat(curSpace) + sStringGenerator(exFindedFig) + "\n";
                    continue;
                case("R"):
                    subObjR r = findRObjByName(figName);
                    if (r != null){
                        exFiguresString+="    ".repeat(curSpace)+ rStringGenerator(r) + "\n";
                    }
                    break;
                case("NV"):
                    subObjNV nv = findNVObjByName(figName);
                    if (nv != null){
                        exFiguresString+="    ".repeat(curSpace)+ nVStringGenerator(nv) + "\n";
                    }
                    break;
                case("d"):
                    exFiguresString+="    ".repeat(curSpace) + ifStringgenerator(findIFObjByName(figName))+ "\n";
                    return;
            }
            ArrayList<String> startOfLine = findStartOfLine(figName,"");
            for (String el: startOfLine){ //пытаемся убрать лишние повторы в текстовом описании
                if (findIFObjByName(el)!=null){
                    startOfLine.clear();
                    startOfLine.add(el);
                    break;
                }
            }
            createExFigureCode(startOfLine);        
        }
        return;
    }
    private ArrayList<String> findStartOfLine(String endOfLine,String iskluchenia){
        ArrayList<String> allLinks = new ArrayList<String>();
        for (Line_s line: curLines){
            if (line.GetID2().equals(endOfLine)){
                if (line.GetID1().equals(iskluchenia)){
                    continue;
                }
                allLinks.add(line.GetID1());
            }
        }
        return allLinks;
    }
    private ArrayList<String> findEndOfLine(String startOfLine,String iskluchenia){
        ArrayList<String> allEndLinks = new ArrayList<String>();
        for (Line_s line: curLines){
            if (line.GetID1().equals(startOfLine)){
                if (line.GetID2().equals(iskluchenia)){
                    continue;
                }
                allEndLinks.add(line.GetID2());
            }
        }
        return allEndLinks;
    }

    private ArrayList<String> findRChainBetween(String startV, String targetR){
        ArrayList<String> result = new ArrayList<String>();
        if (startV == null || targetR == null) {
            return result;
        }
        String currentV = startV;
        ArrayList<String> visited = new ArrayList<String>();
        while (currentV != null && !currentV.isEmpty()) {
            if (visited.contains(currentV)) {
                break;
            }
            visited.add(currentV);
            String nextR = null;
            for (Line_s line: curLines){
                if (line.GetID1().equals(currentV)) {
                    Figure_s endFig = findFigByName(line.GetID2());
                    if (endFig != null && endFig.getShape().equals("R")) {
                        nextR = endFig.getName();
                        break;
                    }
                }
            }
            if (nextR == null) {
                break;
            }
            result.add(nextR);
            if (nextR.equals(targetR)) {
                return result;
            }
            String nextV = null;
            for (Line_s line: curLines){
                if (line.GetID1().equals(nextR)) {
                    Figure_s endFig = findFigByName(line.GetID2());
                    if (endFig != null && endFig.getShape().equals("V")) {
                        nextV = endFig.getName();
                        break;
                    }
                }
            }
            currentV = nextV;
        }
        result.clear();
        return result;
    }

    
    //Сосздание суммы любой фигуры, типа R + R1 ,  O + O1 для удобства
    private String generateStringFromSubArray(ArrayList<String> array){
        String summ = "";
        boolean isFirst = true;
        for (String el :array){
            if (isFirst){
                summ += el;
                isFirst = false;
                continue;
            }
            summ += " + " + el;
        }
        return summ;
    }

    //Поиск фигуры по имени
    private Figure_s findFigByName(String name){
        for (Figure_s figure: curFigures){
            if (figure.getName().equals(name)){
                return figure;
            }
        }
        return null;
    }
    
    //Создает объекты со связями
    private void linkHandler(){
        for (Line_s link: curLines){ //Перебор всех линий
            Figure_s endOfLink = findFigByName(link.GetID2());
            Figure_s startOfLink = findFigByName(link.GetID1());
            if (endOfLink.getShape().equals("d") & startOfLink.getShape().equals("R")){ // если R -> IF
                if (findIFObjByName(endOfLink.getName()) == null){ //Если не найден элемент{
                    subObjIF newEl = new subObjIF(endOfLink.getName()); //Создание нового объекта
                    newEl.setComapreSign(endOfLink.getSignIfSelected()); //Указание знака
                    if (endOfLink.getIfSelected() == 0){ //Указание на то с чем сравниваем
                        newEl.setCompareElementName("i");
                    }else{
                        newEl.setCompareElementName(endOfLink.getIfNvElement());
                    }
                    newEl.setCompareNumber(endOfLink.getCompareNumber()); //Указываем число сравнения
                    newEl.setLinkedInR(startOfLink.getName()); //Добавление нужного элемента
                    arrayIFs.add(newEl);
                    continue;
                }
                findIFObjByName(endOfLink.getName()).setLinkedInR(startOfLink.getName());  //Добавление нужного элемента
            }
            if (endOfLink.getShape().equals("R") & startOfLink.getShape().equals("d")){ // если IF -> R
                if (findIFObjByName(startOfLink.getName()) == null){ //если не найден элемент{
                    subObjIF newEl = new subObjIF(startOfLink.getName());
                    newEl.setComapreSign(startOfLink.getSignIfSelected());
                    if (startOfLink.getIfSelected() == 0){
                        newEl.setCompareElementName("i");
                    }else{
                        newEl.setCompareElementName(startOfLink.getIfNvElement());
                    }
                    newEl.setCompareNumber(startOfLink.getCompareNumber());
                    newEl.setLinkedOutTrueR(endOfLink.getName()); //Добавление нужного элемента
                    arrayIFs.add(newEl);
                    continue;
                }
                findIFObjByName(startOfLink.getName()).setLinkedOutTrueR(endOfLink.getName()); //Добавление нужного элемента
            }
            if (endOfLink.getShape().equals("V") & startOfLink.getShape().equals("d")){ // если IF -> V
                if (findIFObjByName(startOfLink.getName()) == null){ //если не найден элемент{
                    subObjIF newEl = new subObjIF(startOfLink.getName());
                    newEl.setComapreSign(startOfLink.getSignIfSelected());
                    if (startOfLink.getIfSelected() == 0){
                        newEl.setCompareElementName("i");
                    }else{
                        newEl.setCompareElementName(startOfLink.getIfNvElement());
                    }
                    newEl.setCompareNumber(startOfLink.getCompareNumber());
                    newEl.setLinkedOutFalseV(endOfLink.getName()); //Добавление нужного элемента
                    arrayIFs.add(newEl);
                    continue;
                }
                findIFObjByName(startOfLink.getName()).setLinkedOutFalseV(endOfLink.getName()); //Добавление нужного элемента
            }
            if (endOfLink.getShape().equals("NV") & startOfLink.getShape().equals("d")){ // если IF -> NV 
                if (findIFObjByName(startOfLink.getName()) == null){ //если не найден элемент{
                    subObjIF newEl = new subObjIF(startOfLink.getName());
                    newEl.setComapreSign(startOfLink.getSignIfSelected());
                    if (startOfLink.getIfSelected() == 0){
                        newEl.setCompareElementName("i");
                    }else{
                        newEl.setCompareElementName(startOfLink.getIfNvElement());
                    }
                    newEl.setCompareNumber(startOfLink.getCompareNumber());
                    newEl.setLinkedOutFalseNV(endOfLink.getName()); //Добавление нужного элемента
                    arrayIFs.add(newEl);
                    continue;
                }
                findIFObjByName(startOfLink.getName()).setLinkedOutFalseNV(endOfLink.getName()); //Добавление нужного элемента
            }
            if (endOfLink.getShape().equals("R") & startOfLink.getShape().equals("V")){ //если связь V->R
                if (findRObjByName(endOfLink.getName()) == null){ //если не найден элемент
                    subObjR newEl = new subObjR(endOfLink.getName(),startOfLink.getName());
                    arrayRs.add(newEl);
                    continue;
                }
                findRObjByName(endOfLink.getName()).AddToVList(startOfLink.getName());
                continue;
            }
            if (endOfLink.getShape().equals("NV") & startOfLink.getShape().equals("R")){ //если связь R->NV
                if (findNVObjByName(endOfLink.getName()) == null){ //если не найден элемент
                   subObjNV newEl = new subObjNV(endOfLink.getName(),startOfLink.getName());
                   arrayNVs.add(newEl);
                   continue;
                }
                findNVObjByName(endOfLink.getName()).AddToRList(startOfLink.getName());
                continue;
            }
            if (endOfLink.getShape().equals("V")){ //если связь R->V NV->V O->V S->V
                subObjV cur = findVObjByName(endOfLink.getName());
                if (cur == null){ //если элемент не найден
                    cur = new subObjV(endOfLink.getName(),endOfLink.getVSelected());
                    arrayVs.add(cur);
                }
                String startShape = startOfLink.getShape();
                switch(startShape){
                    case "S1": //добавляем имена фигур в соотв массивы по shape фигур
                        cur.AddToSRList(startOfLink.getName());
                        break;
                    case "O":
                        cur.setLinkedO(startOfLink.getName());
                        cur.setOValue(startOfLink.getCoef());
                        break;
                    case "NV":
                        cur.setLinkedNv(startOfLink.getName());
                        break;
                    case "R": 
                        cur.AddToSRList(startOfLink.getName());
                        break;
                }
            }                  
        }
    }
    
    private subObjIF findIFObjByName(String name){ //Поисковик в массиве IF с объектами по имени
        for (subObjIF curAr: arrayIFs){
            if (curAr.getName().equals(name)){
                return curAr;
            }
        }
        return null;
    }
    private subObjR findRObjByName(String name){ //Поисковик в массиве R с объектами по имени
        for (subObjR curAr: arrayRs){
            if (curAr.getName().equals(name)){
                return curAr;
            }
        }
        return null;
    }
    private subObjV findVObjByName(String name){ //Поисковик в массиве V с объектами по имени
        for (subObjV curAr: arrayVs){
            if (curAr.getName().equals(name)){
                return curAr;
            }
        }
        return null;
    }
    private subObjNV findNVObjByName(String name){ //Поисковик в массиве NV с объектами по имени
        for (subObjNV curAr: arrayNVs){
            if (curAr.getName().equals(name)){
                return curAr;
            }
        }
        return null;
    }
    
    private String generateVFunction(subObjV objV){    
        String subString="";
        subString += objV.getName()+"("; //Имя V и скобка
        subString += objV.getType()+","; //Сложность V
        String help = generateStringFromSubArray(objV.getArrayLinkedSR()); //пишем все s
        if (help == ""){ //если строка с s фигурами пуста
            help = "NULL";
        }
        subString+=help;
        
        help = objV.getLinkedNv(); //пишем nv
        if (help == ""){ //если строка с nv r фигурами пуста
            help = "NULL";
        }
        subString+=", " + help;
        
        help = objV.getLinkedO(); //пишем  o
        if (help == ""){ //если строка с o фигурами пуста
            help = "NULL";
        }
        subString+=", " + help;
        if (help!= "NULL"){
            subString += "("+objV.getOValue()+")";
        }
        subString+=") ";
        return subString;
    }
}


