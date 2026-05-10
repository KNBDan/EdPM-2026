package logic.description.rtranslator;

import EPM.mdi;
import static EPM.mdi.prefsMdi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;
import java.util.prefs.Preferences;
import logic.description.rtranslator.CreateRCode;
import objects.figure.figures;

public class RTranslatorClass {

    ArrayList<String> rows = new ArrayList<>(); 
    String xesFileName = "";
    String startN = "";
    int unicNvNumber = 1; 
    boolean isPlotActive = false; 
    boolean isXESActive = false; 
    boolean isActiveO = false; 
    int idNumber = 66;
    int preCycleId = 0; 
    int startId = 66;
    int idStep = 66;
    int rCount = 0;
    int numSpace = 0; 
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
    private final Map<String, Integer> vComplexityIdByName = new HashMap<>();
    private final Map<String, String> vCustomCodeByName = new HashMap<>();
    private final Map<String, String> vLlmPromptByName = new HashMap<>();
    private final Map<String, String> vResolvedOByName = new HashMap<>();
    private final Set<String> helperFunctionNames = new HashSet<>();
    private final ArrayList<String> helperFunctionDefs = new ArrayList<>();

    private static final int V_ID_LINEAR = 1;
    private static final int V_ID_QUADRATIC = 2;
    private static final int V_ID_NLOGN = 3;
    private static final int V_ID_EXPONENTIAL = 4;
    private static final int V_ID_CONSTANT = 5;
    private static final int V_ID_CUSTOM_FUNC = 10;
    private static final int V_ID_LLM_FUNC = 11;
    
    public RTranslatorClass(String preCode) {
        this(preCode, null);
    }

    public RTranslatorClass(String preCode, List<figures> allFigures) {
        this.preCode = preCode;
        initVMetadata(allFigures);
        boolean help = true; String hep = "f";
        startN = localPrefsMdis.get("NValue", hep);
        isPlotActive = (localPrefsMdis.getBoolean("graphState",help));
        isXESActive = (localPrefsMdis.getBoolean("xesState",help));
        idNumber = Integer.valueOf(localPrefsMdis.get("startId",hep));
        idStep = Integer.valueOf(localPrefsMdis.get( "stepId",hep));
        isActiveO = (localPrefsMdis.getBoolean("oActiveState",help));
        this.xesFileName = localPrefsMdis.get("xesName", hep);
        preCycleId = idNumber; 
        startId = idNumber; 
        if (!isPlotActive){ 
            isXESActive = false;
        }
    }
    public String getStringRCode(){
        String vFuncsBlock = "";
        if (!helperFunctionDefs.isEmpty() || !vFunctionDefs.isEmpty()) {
            StringBuilder block = new StringBuilder();
            if (!helperFunctionDefs.isEmpty()) {
                block.append(String.join("\n\n", helperFunctionDefs)).append("\n\n");
            }
            if (!vFunctionDefs.isEmpty()) {
                block.append(String.join("\n\n", vFunctionDefs)).append("\n");
            }
            vFuncsBlock = "# --- ==== [ V Functions ] ==== ---\n" + block;
        }
        return CreateRCode.generateCodeRFromString(preCode + "\n" + vFuncsBlock,rows); 
    }
    public void addString(String text) {
        prePassCache(text);
        rows.add("N <- "+startN);
        if (isPlotActive) {
            rows.add(generatePlotFileBootstrapCode());
        }
        for (String strg : text.split("\n")) { 
            if (strg.length() == 0){
                continue;
            }
            strg = strg.replace("    ", "");
            char shape = strg.charAt(0); 
            String forAdding = "";
            switch (shape) { 
                case ('i'): 
                    forAdding = space() + strg.replace("=","<-");
                    break;
                case ('F'): 
                    forAdding = space() + strg.replace("=","<-");
                    break;    
                case ('N'): 
                    cacheNvSource(strg);
                    continue;
                case ('d'): 
                    ifDetector = true; 
                    forAdding = generateIfStartCodeR(strg);
                    break;
                case ('e'): 
                    numSpace-=1;
                    forAdding = generateIfEndCodeR(strg);
                    if (numSpace==0){ 
                        preCycleId = idNumber-idStep;
                    }
                    break;
                case ('R'): 
                    forAdding = generateRCodeR(strg);
                    if(forAdding.equals("empty")){
                        continue;
                    }
                    break;
                case ('V'): 
                    forAdding = strg;
                    break;
                case ('S'): 
                    cacheSCode(strg);
                    continue;
            }
            if (forAdding.isEmpty()) {
                continue;
            }
            rows.add(forAdding); 
            
        }
        appendDeferredRLines();
        for (String strg : rows) {
            System.out.println(strg);
        }
        if (isXESActive && rCount>0){
            rows.add(generateWriteCode()); 
        }
        if (isPlotActive) {
            rows.add("try(dev.off(), silent = TRUE)");
        }


    }

    private String generatePlotFileBootstrapCode() {
        return "script_path <- NULL\n"
            + "try({\n"
            + "  ofile <- sys.frame(1)$ofile\n"
            + "  if (!is.null(ofile) && length(ofile) > 0) script_path <- ofile\n"
            + "}, silent = TRUE)\n"
            + "if (is.null(script_path)) {\n"
            + "  full_args <- commandArgs(trailingOnly = FALSE)\n"
            + "  file_arg <- grep('^--file=', full_args, value = TRUE)\n"
            + "  if (length(file_arg) > 0) script_path <- sub('^--file=', '', file_arg[1])\n"
            + "}\n"
            + "output_dir <- if (!is.null(script_path)) dirname(normalizePath(script_path, winslash='/', mustWork=FALSE)) else getwd()\n"
            + "plots_pdf_file <- file.path(output_dir, 'linear_plots.pdf')\n"
            + "if (capabilities('cairo')) {\n"
            + "  cairo_pdf(plots_pdf_file, width = 12, height = 8, family = 'Arial', onefile = TRUE)\n"
            + "} else {\n"
            + "  pdf(plots_pdf_file, width = 12, height = 8, onefile = TRUE)\n"
            + "}";
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
        String spr_num = Integer.toString(idNumber-idStep); 
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
        if (ifDetector){ 
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
    public String generateIfStartCodeR(String exCode) { 
        String rCodeString  = "";
        String condition = exCode.split("\\(")[1].split("\\)")[0];
        rCodeString = space() + "while (" + condition + "){";
        numSpace+=1;
        return rCodeString;
    }
     public String generateIfEndCodeR(String exCode) { 
        String rCodeString = "";
        String afterElse = exCode.split("else ")[1].split("\\(")[0].replace(" ", "");
        String rLeft = afterElse.split("=")[0];
        String rRight = afterElse.split("=")[1];
        String newID = Integer.toString(idNumber-idStep);
        rCodeString = "\n" + space() +rLeft + "<-subset(" + rRight + ", select=c(R, ID_Out))"+
        "\n"+ space() + "colnames("+rLeft+") <- c('S', 'ID')"+
        "\n"+ space() + rLeft + "<- Select("+ rLeft +", "+startId+", "+newID+")";  
        rCodeString = space() + "}" + rCodeString;
        return rCodeString;
    }
    public String generateSCodeR(String exCode) { 
        String rCodeString = "";
        String name = exCode.split(" = ")[0];
        String type = exCode.split(" = ")[1].split("\\(")[0]; 
        String typeVar = exCode.split(" = ")[1].split("\\(")[1].replace(")", "");
        typeVar = typeVar.replace(',','.'); 
        if (type.equals("prob")){ 
            rCodeString = space() + name + "<-S_" + type + "(N, " + typeVar + ", " + idNumber + ")";
        }
        else{ 
            rCodeString = space() + name + "<-S_" + type + "(N, FP, " + typeVar + ", " + idNumber + ")";
        }
        idNumber += idStep;
        
        if (isPlotActive) { 
            rCodeString += "\n"
                    + space() + "plot(1:N, " + name + "$S, type=\"s\", col=\"black\", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,6), main = \"Элемент "+name+"\")";
        }
        return rCodeString;
    }

    public String generateNVCodeR(String exCode) { 
        String rCodeString = "";
        String nvName = exCode.split(" = ")[0]; 
        String rName = exCode.split(" = ")[1]; 
        rCodeString = space() + nvName + "<-subset( " + rName + ", select=c(R, ID_Out))"+
                "\n"+space()+"colnames( " + nvName + " ) <- c('S', 'ID')";
        return rCodeString;
    }

    public String generateRCodeR(String exCode) { 
        String rCodeString = "";
        String rName = exCode.split(" = ")[0]; 
        String vName = exCode.split(" = ")[1].split("\\(")[0]; 
        String[] allInProp = exCode.split(" = ")[1].split("\\(")[1].split("\\)")[0].split("\\,");
        String type = allInProp[0]; 
        String[] srFig = allInProp[1].split(" \\+ ");
        String[] nvFig = allInProp[2].split(" \\+ "); 
        String[] oFig = allInProp[3].split(" \\+ "); 
        String oNum = "1"; 

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
        if (isActiveO && !(oFig[0].equals(" NULL"))){ 
            oNum =  exCode.split("\\(")[2].split("\\)")[0]; 
        }

        String vFuncName = vName + "_func";
        ArrayList<String> externalRInputs = getExternalRInputs(nvFig, rInputName);
        int complexityId = resolveComplexityId(vName, type);
        ensureVFunctionDefined(vFuncName, vName, complexityId, srResult, oNum, rInputName, srFig, nvFig, externalRInputs);

        String callArgs = buildVFuncCallArgs(rInputName, externalRInputs);
        rCodeString = space() + rName + "<- " + vFuncName + "(" + callArgs + ")";
        
        if (numSpace == 0) {
            deferTopLevelR(rName, rCodeString, rInputName, externalRInputs);
            return "";
        }

        if (isPlotActive){ 
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
        
        if (inFigures.size() == 1) { 
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
            if (curFig.charAt(0) == ('R')) { 
                String nvName = "NV_in_" + vName + "_" + inFigures.size();
                String rVar = (rInputName != null && curFig.equals(rInputName)) ? "R_in" : curFig;
                localPreCode.append(generateNVFromRVar(nvName, rVar)).append("\n");
                curFig = nvName;
            }
            readyElement += curFig + ",";
        }
        
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
            + "colnames( " + nvName + " ) <- c('S', 'ID')";
    }

    private void ensureVFunctionDefined(
        String vFuncName,
        String vName,
        int complexityId,
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
        appendNvCode(func, nvFig, rInputName, externalRInputs, srResult.srElement);
        if (srResult.preCode != null && !srResult.preCode.isEmpty()) {
            for (String line : srResult.preCode.split("\n")) {
                if (!line.isEmpty()) {
                    func.append("  ").append(line).append("\n");
                }
            }
        }
        String customOExpr = buildCustomOExpr(vName, complexityId, srResult.srElement, oNum);
        int runtimeM = normalizeRuntimeM(complexityId);
        func.append("  return(V(")
            .append(runtimeM).append(", ")
            .append(srResult.srElement).append(", \"")
            .append(vName).append("\", ")
            .append(customOExpr).append("))\n");
        func.append("}");
        vFunctionDefs.add(func.toString());
    }

    private int resolveComplexityId(String vName, String legacyType) {
        if (vComplexityIdByName.containsKey(vName)) {
            int id = vComplexityIdByName.get(vName);
            validateComplexity(vName, id);
            return id;
        }
        try {
            int id = Integer.parseInt(legacyType.trim());
            validateComplexity(vName, id);
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Unsupported complexity for " + vName + ": " + legacyType);
        }
    }

    private void validateComplexity(String vName, int id) {
        if (id >= V_ID_LINEAR && id <= V_ID_EXPONENTIAL) {
            return;
        }
        if (id == V_ID_CONSTANT) {
            throw new IllegalArgumentException("Complexity O(1) is not implemented in base R for " + vName);
        }
        if (id == V_ID_CUSTOM_FUNC) {
            String code = vCustomCodeByName.get(vName);
            if (code == null || code.trim().isEmpty()) {
                throw new IllegalArgumentException("Custom complexity selected but code is empty for " + vName);
            }
            return;
        }
        if (id == V_ID_LLM_FUNC) {
            String prompt = vLlmPromptByName.get(vName);
            if (prompt == null || prompt.trim().isEmpty()) {
                throw new IllegalArgumentException("LLM complexity selected but prompt is empty for " + vName);
            }
            return;
        }
        throw new IllegalArgumentException("Complexity id " + id + " is not supported for " + vName);
    }

    private int normalizeRuntimeM(int complexityId) {
        if (complexityId >= V_ID_LINEAR && complexityId <= V_ID_EXPONENTIAL) {
            return complexityId;
        }
        return V_ID_LINEAR;
    }

    private String buildCustomOExpr(String vName, int complexityId, String srElement, String defaultOExpr) {
        if (complexityId == V_ID_CUSTOM_FUNC) {
            String userCode = vCustomCodeByName.getOrDefault(vName, "");
            String userFn = extractFunctionName(userCode);
            if (userFn == null || userFn.isEmpty()) {
                throw new IllegalArgumentException("Unable to detect custom function name for " + vName);
            }
            String helperName = vName + "_custom_complexity";
            ensureCustomHelper(helperName, userFn);
            return helperName + "(N, " + srElement + ", " + defaultOExpr + ")";
        }
        if (complexityId == V_ID_LLM_FUNC) {
            String resolved = vResolvedOByName.get(vName);
            if (resolved != null && !resolved.trim().isEmpty()) {
                return resolved.trim();
            }
            String prompt = vLlmPromptByName.getOrDefault(vName, "").replace("\"", "\\\"");
            String helperName = vName + "_llm_complexity";
            ensureLlmHelper(helperName, prompt);
            return helperName + "(N, " + srElement + ", " + defaultOExpr + ")";
        }
        return defaultOExpr;
    }

    private void ensureCustomHelper(String helperName, String userFn) {
        if (helperFunctionNames.contains(helperName)) {
            return;
        }
        helperFunctionNames.add(helperName);
        StringBuilder sb = new StringBuilder();
        sb.append(helperName).append(" <- function(N, S_in, O_default){\n")
          .append("  val <- tryCatch(").append(userFn).append("(N, S_in, O_default), error = function(e) NA)\n")
          .append("  if (is.na(val[1])) val <- tryCatch(").append(userFn).append("(N, S_in), error = function(e) NA)\n")
          .append("  if (is.na(val[1])) val <- tryCatch(").append(userFn).append("(N), error = function(e) NA)\n")
          .append("  if (!is.numeric(val) || length(val) == 0 || is.na(val[1])) stop('Custom complexity must return numeric scalar')\n")
          .append("  as.numeric(val[1])\n")
          .append("}");
        helperFunctionDefs.add(sb.toString());
    }

    private void ensureLlmHelper(String helperName, String prompt) {
        if (helperFunctionNames.contains(helperName)) {
            return;
        }
        helperFunctionNames.add(helperName);
        StringBuilder sb = new StringBuilder();
        sb.append(helperName).append(" <- function(N, S_in, O_default){\n")
          .append("  prompt <- \"").append(prompt).append("\"\n")
          .append("  # Wrapper placeholder: integrate real LLM call later.\n")
          .append("  # Must return numeric scalar complexity coefficient.\n")
          .append("  if (is.null(prompt) || nchar(prompt) == 0) stop('LLM prompt is empty')\n")
          .append("  as.numeric(O_default)\n")
          .append("}");
        helperFunctionDefs.add(sb.toString());
    }

    private String extractFunctionName(String code) {
        if (code == null) {
            return null;
        }
        String[] lines = code.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.contains("<-") && trimmed.contains("function(")) {
                return trimmed.split("<-", 2)[0].trim();
            }
        }
        return null;
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

    private void appendNvCode(
        StringBuilder func,
        String[] nvFig,
        String rInputName,
        ArrayList<String> externalRInputs,
        String srElement
    ) {
        Set<String> added = new HashSet<>();
        for (String fig : nvFig) {
            String nvName = fig.trim();
            if (nvName.equals("NULL") || nvName.isEmpty()) {
                continue;
            }
            if (!isNvUsedInSrElement(nvName, srElement)) {
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

    private boolean isNvUsedInSrElement(String nvName, String srElement) {
        if (srElement == null || srElement.isEmpty()) {
            return true;
        }
        String tokenRegex = "(^|[^A-Za-z0-9_])" + Pattern.quote(nvName) + "([^A-Za-z0-9_]|$)";
        return srElement.matches(".*" + tokenRegex + ".*");
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

    private void initVMetadata(List<figures> allFigures) {
        if (allFigures == null) {
            return;
        }
        for (figures fig : allFigures) {
            if (fig == null || fig.getNameF() == null || !fig.getNameF().startsWith("V")) {
                continue;
            }
            String vName = fig.getNameF();
            int id = mapComplexityIdFromSelection(fig.getVSelected());
            vComplexityIdByName.put(vName, id);
            if (id == V_ID_CUSTOM_FUNC) {
                vCustomCodeByName.put(vName, fig.getCodeF());
            }
            if (id == V_ID_LLM_FUNC) {
                vLlmPromptByName.put(vName, fig.getLlmPrompt());
                String coef = fig.getCoef();
                if (coef != null && !coef.trim().isEmpty()) {
                    vResolvedOByName.put(vName, coef.trim());
                }
            }
        }
    }

    private int mapComplexityIdFromSelection(String selected) {
        if (selected == null) {
            return V_ID_LINEAR;
        }
        String norm = selected.trim().toLowerCase();
        if (norm.contains("custom")) {
            return V_ID_CUSTOM_FUNC;
        }
        if (norm.contains("llm")) {
            return V_ID_LLM_FUNC;
        }
        if (norm.contains("exp")) {
            return V_ID_EXPONENTIAL;
        }
        if (norm.contains("n * log") || norm.contains("n*log")) {
            return V_ID_NLOGN;
        }
        if (norm.contains("log")) {
            return V_ID_NLOGN;
        }
        if (norm.contains("o ( 1 )") || norm.contains("o(1)")) {
            return V_ID_CONSTANT;
        }
        if (norm.contains("n^2")) {
            return V_ID_QUADRATIC;
        }
        if (norm.contains("o ( n )") || norm.contains("o(n)")) {
            return V_ID_LINEAR;
        }
        return V_ID_LINEAR;
    }

    private String space(){
        return "    ".repeat(numSpace);
    }
}

