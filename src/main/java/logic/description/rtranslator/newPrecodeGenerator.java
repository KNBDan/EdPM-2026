package logic.description.rtranslator;

import objects.figure.V;
import objects.figure.figures;
import java.util.ArrayList;

public class newPrecodeGenerator {
    String vPreCode = "";
    public newPrecodeGenerator(ArrayList<figures> all){
        getAllV(all);
    }
    private void getAllV(ArrayList<figures> all){
        for (figures fig: all){
            if (fig.getClass().equals(V.class)){
                if (fig.getVSelected().equals("Индивидуальная функция сложности")){
                    vPreCode += "\n"+fig.getCodeF();
                }
            }
        }
    }
    public String getPrecodeString(){
        return vPreCode;
    }
}
