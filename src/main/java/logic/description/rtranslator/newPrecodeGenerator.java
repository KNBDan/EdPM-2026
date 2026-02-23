package logic.description.rtranslator;

import java.util.ArrayList;
import objects.figure.V;
import objects.figure.figures;

public class newPrecodeGenerator {
    String vPreCode = "";

    public newPrecodeGenerator(ArrayList<figures> all){
        getAllV(all);
    }

    private void getAllV(ArrayList<figures> all){
        for (figures fig: all){
            if (!fig.getClass().equals(V.class)) {
                continue;
            }
            String selected = fig.getVSelected();
            if (selected == null || !selected.toLowerCase().contains("custom")) {
                continue;
            }
            String code = fig.getCodeF();
            if (code != null && !code.trim().isEmpty()) {
                vPreCode += "\n" + code;
            }
        }
    }

    public String getPrecodeString(){
        return vPreCode;
    }
}