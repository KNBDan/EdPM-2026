package logic.description.rtranslator;

import EPM.mdi;
import static EPM.mdi.prefsMdi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.prefs.Preferences;
import logic.description.rtranslator.CreateRCode;

public class RTranslatorClass {

    ArrayList<String> rows = new ArrayList<>(); //готовые строки
    String xesFileName = "";
    String startN = "";
    int unicNvNumber = 1; //уникальный id для технических nv
    boolean isPlotActive = false; //строить графики
    boolean isXESActive = false; //Выгрузка в ХЕС
    boolean isActiveO = false; //Учитывать О
    int idNumber = 66;
    int preCycleId = 0; //ЕСЛИ НЕ ПОТРЕБ УДАЛИТЬ!
    int startId = 66;
    int idStep = 66;
    int rCount = 0;
    int numSpace = 0; //Отступы для if  и счетчик if соотв
    boolean ifDetector = false;
    private static Preferences localPrefsMdis = prefsMdi; 
    private String preCode;
    private final ArrayList<String> vFunctionDefs = new ArrayList<>();
    private final ArrayList<String> vFunctionNames = new ArrayList<>();
    private final Map<String, String> sCodeByName = new HashMap<>();
    private final Map<String, String> nvSourceByName = new HashMap<>();
    private final Map<String, String> deferredRLines = new HashMap<>();
    private final Map<String, Set<String>> deferredRDeps = new HashMap<>();
    private final ArrayList<String> deferredROrder = new ArrayList<>();
    
    public RTranslatorClass(String preCode) {
        this.preCode = preCode;
        boolean help = true; String hep = "f";
        startN = localPrefsMdis.get("NValue", hep);
        isPlotActive = (localPrefsMdis.getBoolean("graphState",help));
        isXESActive = (localPrefsMdis.getBoolean("xesState",help));
        idNumber = Integer.valueOf(localPrefsMdis.get("startId",hep));
        idStep = Integer.valueOf(localPrefsMdis.get( "stepId",hep));
        isActiveO = (localPrefsMdis.getBoolean("oActiveState",help));
        this.xesFileName = localPrefsMdis.get("xesName", hep);
        preCycleId = idNumber; //первое значение предциклового id //ЕСЛИ НЕ ПОТРЕБ УДАЛИТЬ!
        startId = idNumber; //стартовый id
        if (!isPlotActive){ //если не строим графики, то не выгружаем хес
            isXESActive = false;
        }
    }
    public String getStringRCode(){
        String vFuncsBlock = "";
        if (!vFunctionDefs.isEmpty()) {
            vFuncsBlock = "# --- ==== [ Микросервисы ] ==== ---\n"
                + String.join("\n\n", vFunctionDefs) + "\n";
        }
        return CreateRCode.generateCodeRFromString(preCode + "\n" + vFuncsBlock,rows); //Сохраняем в файл
    }
    public void addString(String text) {
        prePassCache(text);
        rows.add("N <- "+startN);
        for (String strg : text.split("\n")) { //перепор каждой строки
            if (strg.length() == 0){
                continue;
            }
            strg = strg.replace("    ", "");
            char shape = strg.charAt(0); //выбираем первый символ строки для определения типа фигуры
            String forAdding = "";
            switch (shape) { //переделываем псевдокод в код R
                case ('i'): //первая i = 100 например
                    forAdding = space() + strg.replace("=","<-");
                    break;
                case ('F'): //первая FP = 100 например
                    forAdding = space() + strg.replace("=","<-");
                    break;    
                case ('N'): //NV
                    cacheNvSource(strg);
                    continue;
                case ('d'): //if
                    ifDetector = true; //нашли IF в коде
                    forAdding = generateIfStartCodeR(strg);
                    break;
                case ('e'): // end if
                    numSpace-=1;
                    forAdding = generateIfEndCodeR(strg);
                    if (numSpace==0){ //новый предцикловый id для новых циклов от нулевого отступа
                        preCycleId = idNumber-idStep;
                    }
                    break;
                case ('R'): //R
                    forAdding = generateRCodeR(strg);
                    if(forAdding.equals("empty")){
                        continue;
                    }
                    break;
                case ('V'): //V
                    forAdding = strg;
                    break;
                case ('S'): //S
                    cacheSCode(strg);
                    continue;
            }
            if (forAdding.isEmpty()) {
                continue;
            }
            rows.add(forAdding); //Добавляем полученную строку/строкИ в массив с готовыми строками (если нужно хранить именно строку стоит 
            //перед добавлением полученную строку разделить по \n
        }
        appendDeferredRLines();
        for (String strg : rows) {
            System.out.println(strg);
        }
        if (isXESActive && rCount>0){
            rows.add(generateWriteCode()); //Если выгружаем хес добавляем соотв строку
        }
//        return CreateRCode.generateCodeRFromString(preCode,rows); //Сохраняем в файл
//        (rFilePath+"/"+rFileName+".R") Указание сохранения файла R
    }
    private void prePassCache(String text) {
        for (String strg : text.split("\n")) {
            if (strg.length() == 0) {
                continue;
            }
            strg = strg.replace("    ", "");
            char shape = strg.charAt(0);
            if (shape == 'S') {
                cacheSCode(strg);
            } else if (shape == 'N') {
                cacheNvSource(strg);
            }
        }
    }

    private void appendDeferredRLines() {
        if (deferredRLines.isEmpty()) {
            return;
        }
        ArrayList<String> ordered = topoSortDeferred();
        for (String rName : ordered) {
            String line = deferredRLines.get(rName);
            if (line != null && !line.isEmpty()) {
                rows.add(line);
                String plot = buildRPlotXesLines(rName);
                if (!plot.isEmpty()) {
                    rows.add(plot);
                }
            }
        }
        deferredRLines.clear();
        deferredRDeps.clear();
        deferredROrder.clear();
    }

    private ArrayList<String> topoSortDeferred() {
        Map<String, Integer> inDeg = new HashMap<>();
        Map<String, Set<String>> deps = new HashMap<>();
        for (String name : deferredRLines.keySet()) {
            Set<String> d = deferredRDeps.getOrDefault(name, new LinkedHashSet<>());
            Set<String> filtered = new LinkedHashSet<>();
            for (String dep : d) {
                if (deferredRLines.containsKey(dep)) {
                    filtered.add(dep);
                }
            }
            deps.put(name, filtered);
            inDeg.put(name, filtered.size());
        }
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> queue = new ArrayList<>();
        for (String name : deferredROrder) {
            if (inDeg.getOrDefault(name, 0) == 0) {
                queue.add(name);
            }
        }
        Set<String> queued = new HashSet<>(queue);
        while (!queue.isEmpty()) {
            String cur = queue.remove(0);
            result.add(cur);
            for (Map.Entry<String, Set<String>> e : deps.entrySet()) {
                if (e.getValue().contains(cur)) {
                    int deg = inDeg.get(e.getKey()) - 1;
                    inDeg.put(e.getKey(), deg);
                    if (deg == 0 && !queued.contains(e.getKey())) {
                        queue.add(e.getKey());
                        queued.add(e.getKey());
                    }
                }
            }
        }
        for (String name : deferredROrder) {
            if (!result.contains(name)) {
                result.add(name);
            }
        }
        return result;
    }

    private String buildRPlotXesLines(String rName) {
        if (!isPlotActive) {
            return "";
        }
        String rCodeString = "";
        rCodeString +=  "\n"+ space() +"plot(1:N, "+rName+"$R, type=\"s\", col=\"black\", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = \"Element "+rName+"\")" +
            "\n" + space() + "plot(1:N, "+rName+"$Prj_File, type=\"s\", col=\"black\", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = \"Element "+rName+"\")";
        if (isXESActive){
            rCount+=1;
            String xName = "X";
            if (rCount == 1){
                rCodeString+="\n" + space() +"X1<-XES("+rName+")";
                rCodeString+="\n" + space() +xName+"<-XES("+rName+")";
                rCodeString+="\n" + space() + "if (length(X1$W) > 0) vioplot(X1$W, col = \"lightgray\", panel.first=grid(), main = \"Element "+rName+"\")";
            }else{
                xName += rCount;
                rCodeString+="\n"+ space() +xName+"<-XES("+rName+")"
                        +"\n" + space() + "X<-rbind(X,"+xName+")";
                rCodeString+="\n" + space() + "if (length("+xName+"$W) > 0) vioplot("+xName+"$W, col = \"lightgray\", panel.first=grid(), main = \"Element "+rName+"\")";
            }
            rCodeString+= "\n" + space() + "if (length(X$W) > 0) vioplot(X$W, col = \"lightgray\", panel.first=grid(), main = \"All elements before "+rName+"\")";
        }
        return rCodeString;
    }

    public String generateWriteCode(){
        String rCodeString = "";
        String spr_num = Integer.toString(idNumber-idStep); //Значение S_prob  (вычитаем шаг чтобы было корректное значнеие) (всегда последнее id в S)
        if (rCount >1){
            StringBuilder xList = new StringBuilder();
            for (int i = 1; i <= rCount;i++){
                xList.append("X").append(i).append("$W,");
            }
            if (xList.length() > 0) {
                xList.setLength(xList.length() - 1);
            }
            rCodeString += "X_list <- list(" + xList + ")\n";
            rCodeString += "X_list <- X_list[sapply(X_list, length) > 0]\n";
            rCodeString += "if (length(X_list) > 0) do.call(vioplot, c(X_list, list(col = \"lightgray\", panel.first=grid())))\n";
        }
        rCodeString += "l<-unique(X$ID)";
        if (ifDetector){ //цикл был вставляем в код
            rCodeString +="\nl<-l[l<"+spr_num+"]";
        }
        rCodeString +="\ns_last<-NA" +
        "\nfor (i in 1:length(l)){" +
        "\n  s_last[i]<-sum(X$W[X$ID==l[i]])" +
        "\n}" +
        "\nif (length(s_last) > 0) vioplot(s_last, col = \"lightgray\", panel.first=grid())";
        rCodeString += "\nwrite.csv(X, file=\""+xesFileName+".csv\")";
        return rCodeString;
    }
    public String generateIfStartCodeR(String exCode) { //Констиурктор кода языка R для if start
        String rCodeString  = "";
        String condition = exCode.split("\\(")[1].split("\\)")[0];
        rCodeString = space() + "while (" + condition + "){";
        numSpace+=1;
        return rCodeString;
    }
     public String generateIfEndCodeR(String exCode) { //Констиурктор кода языка R для if end
        String rCodeString = "";
        String afterElse = exCode.split("else ")[1].split("\\(")[0].replace(" ", "");
        String rLeft = afterElse.split("=")[0];
        String rRight = afterElse.split("=")[1];
        String newID = Integer.toString(idNumber-idStep);//preCycleId + ((1 + numSpace) * idStep)//чтото на умном, код не нужен, после проверки ДЕМОНТИРОВАТЬ!!
        rCodeString = "\n" + space() +rLeft + "<-subset(" + rRight + ", select=c(R, ID_Out)"+
        "\n"+ space() + "colnames("+rLeft+") <- c('S', 'ID')"+
        "\n"+ space() + rLeft + "<- Select("+ rLeft +", "+startId+", "+newID+")";  //(вычитаем шаг чтобы было корректное значнеие)    
        rCodeString = space() + "}" + rCodeString;
        return rCodeString;
    }
    public String generateSCodeR(String exCode) { //Констиурктор кода языка R для фиугры S
        String rCodeString = "";
        String name = exCode.split(" = ")[0];//до =
        String type = exCode.split(" = ")[1].split("\\(")[0]; // после = до (
        String typeVar = exCode.split(" = ")[1].split("\\(")[1].replace(")", "");// в ()
        typeVar = typeVar.replace(',','.'); //Замена запятой на точку, так как конфликт в R. ИСПРАВИТЬ В ОСНОВНОЙ ПРОГЕ ИЛИ УЧЕСТЬ ВЕЗДЕ
        if (type.equals("prob")){ //prob S<-S_prob(N, 0.9, 1000)
            rCodeString = space() + name + "<-S_" + type + "(N, " + typeVar + ", " + idNumber + ")";
        }
        else{ //periodic S<-S_periodic(N, FP, 9, 1000)
            rCodeString = space() + name + "<-S_" + type + "(N, FP, " + typeVar + ", " + idNumber + ")";
        }
        idNumber += idStep;
        
        if (isPlotActive) { //если нужно строить графики
            rCodeString += "\n"
                    + space() + "plot(1:N, " + name + "$S, type=\"s\", col=\"black\", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,6), main = \"Элемент "+name+"\")";
        }
        return rCodeString;
    }

    public String generateNVCodeR(String exCode) { //Констиурктор кода языка R для фиугры NV
        String rCodeString = "";
        String nvName = exCode.split(" = ")[0]; //имя NV
        String rName = exCode.split(" = ")[1]; //имя R
        rCodeString = space() + nvName + "<-subset( " + rName + ", select=c(R, ID_Out))"+
                "\n"+space()+"colnames( " + nvName + " ) <- c('S', 'ID')";
        return rCodeString;
    }

    public String generateRCodeR(String exCode) { //R code generation for R figure
        String rCodeString = "";
        String rName = exCode.split(" = ")[0]; //R name
        String vName = exCode.split(" = ")[1].split("\\(")[0]; //V name
        String[] allInProp = exCode.split(" = ")[1].split("\\(")[1].split("\\)")[0].split("\\,");
        String type = allInProp[0]; //complexity
        String[] srFig = allInProp[1].split(" \\+ ");
        String[] nvFig = allInProp[2].split(" \\+ "); //all NV inputs (not used directly)
        String[] oFig = allInProp[3].split(" \\+ "); //all O inputs (not used directly)
        String oNum = "1"; //default O value

        boolean hasS = !srFig[0].equals("NULL");
        boolean hasNV = !nvFig[0].equals("NULL");
        if (!hasS && !hasNV){
            return "empty";
        }
        String rInputName = findFirstRInput(srFig);
        SrBlockResult srResult;
        if (hasS) {
            srResult = srBlockGenForFunction(srFig, rInputName, vName);
        } else {
            srResult = srBlockGenForFunction(nvFig, rInputName, vName);
        }
        String srElement = srResult.srElement;
        if (isActiveO && !(oFig[0].equals(" NULL"))){ //use O if enabled
            oNum =  exCode.split("\\(")[2].split("\\)")[0]; //read O value from input
        }

        String vFuncName = vName + "_func";
        ArrayList<String> externalRInputs = getExternalRInputs(nvFig, rInputName);
        ensureVFunctionDefined(vFuncName, vName, type, srResult, oNum, rInputName, srFig, nvFig, externalRInputs);

        String callArgs = buildVFuncCallArgs(rInputName, externalRInputs);
        rCodeString = space() + rName + "<- " + vFuncName + "(" + callArgs + ")";
        
        if (numSpace == 0) {
            deferTopLevelR(rName, rCodeString, rInputName, externalRInputs);
            return "";
        }

        if (isPlotActive){ //plots for R
            rCodeString +=  "\n"+ space() +"plot(1:N, "+rName+"$R, type=\"s\", col=\"black\", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = \"Element "+rName+"\")" +
            "\n" + space() + "plot(1:N, "+rName+"$Prj_File, type=\"s\", col=\"black\", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = \"Element "+rName+"\")";
            if (isXESActive){ //plots for X
                rCount+=1;
                String xName = "X";
                if (rCount == 1){
                    rCodeString+="\n" + space() +"X1<-XES("+rName+")";  
                    rCodeString+="\n" + space() +xName+"<-XES("+rName+")";
                    rCodeString+="\n" + space() + "if (length(X1$W) > 0) vioplot(X1$W, col = \"lightgray\", panel.first=grid(), main = \"Element "+rName+"\")";
                }else{
                    xName += rCount;
                    rCodeString+="\n"+ space() +xName+"<-XES("+rName+")"
                            +"\n" + space() + "X<-rbind(X,"+xName+")";
                    rCodeString+="\n" + space() + "if (length("+xName+"$W) > 0) vioplot("+xName+"$W, col = \"lightgray\", panel.first=grid(), main = \"Element "+rName+"\")";
                }
                rCodeString+= "\n" + space() + "if (length(X$W) > 0) vioplot(X$W, col = \"lightgray\", panel.first=grid(), main = \"All elements before "+rName+"\")";
            }
        }
        
        return rCodeString;
    }

    private SrBlockResult srBlockGenForFunction(String[] allObject, String rInputName, String vName) {
        StringBuilder localPreCode = new StringBuilder();
        ArrayList<String> inFigures = new ArrayList<String>();
        
        for (String el : allObject) {
            inFigures.add(el);
        }
        
        String readyElement = "";
        
        if (inFigures.size() == 1) { //only one input (no ADD)
            String curFig = inFigures.remove(0);
            if (curFig.charAt(0) == ('R')) {
                String nvName = "NV_in_" + vName;
                String rVar = (rInputName != null && curFig.equals(rInputName)) ? "R_in" : curFig;
                localPreCode.append(generateNVFromRVar(nvName, rVar)).append("\n");
                readyElement = nvName;
                return new SrBlockResult(readyElement, localPreCode.toString());
            }
            return new SrBlockResult(curFig, localPreCode.toString());
        }
        
        while (inFigures.size() != 1) {
            readyElement += "Add(";

            String curFig = inFigures.remove(0);
            if (curFig.charAt(0) == ('R')) { //if input is R, convert to NV
                String nvName = "NV_in_" + vName + "_" + inFigures.size();
                String rVar = (rInputName != null && curFig.equals(rInputName)) ? "R_in" : curFig;
                localPreCode.append(generateNVFromRVar(nvName, rVar)).append("\n");
                curFig = nvName;
            }
            readyElement += curFig + ",";
        }
        //add last figure
        String curFig = inFigures.remove(0);
        if (curFig.charAt(0) == ('R')) {
               String nvName = "NV_in_" + vName + "_" + inFigures.size();
               String rVar = (rInputName != null && curFig.equals(rInputName)) ? "R_in" : curFig;
               localPreCode.append(generateNVFromRVar(nvName, rVar)).append("\n");
               curFig = nvName;
           }
        readyElement += curFig + ")".repeat(allObject.length - 1);

        return new SrBlockResult(readyElement, localPreCode.toString());
    }

    private String generateNVFromRVar(String nvName, String rVar){
        return nvName + "<-subset( " + rVar + ", select=c(R, ID_Out))\n"
            + "colnames( " + nvName + " ) <- c('S', 'ID')\n"
            + nvName + "$ID <- lapply(" + nvName + "$ID, function(x) { if (is.list(x)) x else list(x) })";
    }

    private void ensureVFunctionDefined(
        String vFuncName,
        String vName,
        String type,
        SrBlockResult srResult,
        String oNum,
        String rInputName,
        String[] srFig,
        String[] nvFig,
        ArrayList<String> externalRInputs
    ) {
        if (vFunctionNames.contains(vFuncName)) {
            return;
        }
        vFunctionNames.add(vFuncName);
        StringBuilder func = new StringBuilder();
        func.append(vFuncName).append(" <- function(");
        String funcArgs = buildVFuncParams(rInputName, externalRInputs);
        func.append(funcArgs);
        func.append("){").append("\n");
        appendSCode(func, srFig);
        appendNvCode(func, nvFig, rInputName, externalRInputs);
        if (srResult.preCode != null && !srResult.preCode.isEmpty()) {
            for (String line : srResult.preCode.split("\n")) {
                if (!line.isEmpty()) {
                    func.append("  ").append(line).append("\n");
                }
            }
        }
        func.append("  return(V(")
            .append(type).append(", ")
            .append(srResult.srElement).append(", \"")
            .append(vName).append("\", ")
            .append(oNum).append("))\n");
        func.append("}");
        vFunctionDefs.add(func.toString());
    }

    private void deferTopLevelR(String rName, String rLine, String rInputName, ArrayList<String> externalRInputs) {
        if (!deferredRLines.containsKey(rName)) {
            deferredROrder.add(rName);
        }
        deferredRLines.put(rName, rLine);
        Set<String> deps = new LinkedHashSet<>();
        if (rInputName != null) {
            deps.add(rInputName);
        }
        if (externalRInputs != null) {
            deps.addAll(externalRInputs);
        }
        deferredRDeps.put(rName, deps);
    }

    private String findFirstRInput(String[] srFig) {
        for (String fig : srFig) {
            String trimmed = fig.trim();
            if (trimmed.startsWith("R")) {
                return trimmed;
            }
        }
        return null;
    }

    private static class SrBlockResult {
        private final String srElement;
        private final String preCode;

        private SrBlockResult(String srElement, String preCode) {
            this.srElement = srElement;
            this.preCode = preCode;
        }
    }

    private ArrayList<String> parseRList(String rExpr) {
        ArrayList<String> res = new ArrayList<>();
        if (rExpr == null) {
            return res;
        }
        String[] parts = rExpr.split("\\+");
        for (String part : parts) {
            String name = part.trim();
            if (!name.isEmpty()) {
                res.add(name);
            }
        }
        return res;
    }

    private void cacheSCode(String exCode) {
        String name = exCode.split(" = ")[0];
        String code = generateSCodeR(exCode);
        sCodeByName.put(name, stripIndent(code));
    }

    private void cacheNvSource(String exCode) {
        String nvName = exCode.split(" = ")[0];
        String rExpr = exCode.split(" = ")[1];
        nvSourceByName.put(nvName, rExpr);
    }

    private void appendSCode(StringBuilder func, String[] srFig) {
        Set<String> added = new HashSet<>();
        for (String fig : srFig) {
            String name = fig.trim();
            if (!name.startsWith("S")) {
                continue;
            }
            if (added.contains(name)) {
                continue;
            }
            String code = sCodeByName.get(name);
            if (code == null || code.isEmpty()) {
                continue;
            }
            for (String line : code.split("\n")) {
                if (!line.isEmpty()) {
                    func.append("  ").append(line).append("\n");
                }
            }
            added.add(name);
        }
    }

    private void appendNvCode(StringBuilder func, String[] nvFig, String rInputName, ArrayList<String> externalRInputs) {
        Set<String> added = new HashSet<>();
        for (String fig : nvFig) {
            String nvName = fig.trim();
            if (nvName.equals("NULL") || nvName.isEmpty()) {
                continue;
            }
            if (added.contains(nvName)) {
                continue;
            }
            String rExpr = nvSourceByName.get(nvName);
            if (rExpr == null || rExpr.isEmpty()) {
                continue;
            }
            ArrayList<String> rNames = parseRList(rExpr);
            ArrayList<String> nvParts = new ArrayList<>();
            int idx = 1;
            for (String rName : rNames) {
                String rVar;
                if (rInputName != null && rName.equals(rInputName)) {
                    rVar = "R_in";
                } else if (externalRInputs != null && externalRInputs.contains(rName)) {
                    rVar = buildExternalParamName(rName);
                } else {
                    rVar = rName;
                }
                String partName = nvName + "_p" + idx;
                idx += 1;
                String nvCode = generateNVFromRVar(partName, rVar);
                for (String line : nvCode.split("\n")) {
                    if (!line.isEmpty()) {
                        func.append("  ").append(line).append("\n");
                    }
                }
                nvParts.add(partName);
            }
            if (nvParts.size() == 1) {
                func.append("  ").append(nvName).append(" <- ").append(nvParts.get(0)).append("\n");
            } else if (nvParts.size() > 1) {
                String addExpr = "Add(";
                for (int i = 0; i < nvParts.size(); i++) {
                    if (i > 0) {
                        addExpr += ",";
                    }
                    addExpr += nvParts.get(i);
                }
                addExpr += ")";
                func.append("  ").append(nvName).append(" <- ").append(addExpr).append("\n");
            }
            added.add(nvName);
        }
    }

    private String stripIndent(String code) {
        StringBuilder cleaned = new StringBuilder();
        for (String line : code.split("\n")) {
            cleaned.append(line.stripLeading()).append("\n");
        }
        return cleaned.toString().trim();
    }

    private ArrayList<String> getExternalRInputs(String[] nvFig, String rInputName) {
        ArrayList<String> result = new ArrayList<>();
        if (nvFig == null) {
            return result;
        }
        for (String fig : nvFig) {
            String nvName = fig.trim();
            if (nvName.equals("NULL") || nvName.isEmpty()) {
                continue;
            }
            String rExpr = nvSourceByName.get(nvName);
            if (rExpr == null || rExpr.isEmpty()) {
                continue;
            }
            ArrayList<String> rNames = parseRList(rExpr);
            for (String rName : rNames) {
                if (rInputName != null && rName.equals(rInputName)) {
                    continue;
                }
                if (!result.contains(rName)) {
                    result.add(rName);
                }
            }
        }
        return result;
    }

    private String buildExternalParamName(String rName) {
        return "R_in_" + rName;
    }

    private String buildVFuncParams(String rInputName, ArrayList<String> externalRInputs) {
        StringBuilder args = new StringBuilder();
        if (rInputName != null) {
            args.append("R_in");
        }
        if (externalRInputs != null) {
            for (String rName : externalRInputs) {
                if (args.length() > 0) {
                    args.append(", ");
                }
                args.append(buildExternalParamName(rName));
            }
        }
        return args.toString();
    }

    private String buildVFuncCallArgs(String rInputName, ArrayList<String> externalRInputs) {
        StringBuilder args = new StringBuilder();
        if (rInputName != null) {
            args.append(rInputName);
        }
        if (externalRInputs != null) {
            for (String rName : externalRInputs) {
                if (args.length() > 0) {
                    args.append(", ");
                }
                args.append(rName);
            }
        }
        return args.toString();
    }

    private String space(){
        return "    ".repeat(numSpace);
    }
}
