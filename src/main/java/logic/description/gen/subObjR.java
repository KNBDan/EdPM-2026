package logic.description.gen;

import java.util.ArrayList;

public class subObjR {
    private ArrayList<String> arrayLinkedV = new ArrayList<String>();
    private String nameR;
    
    public subObjR(String name){ //стандартный генератор с прикреплением имени
        nameR = name;
    }
    public subObjR(String name, String vName){ //стандартный генератор с прикреплением имени и первой фигуры
        nameR = name;
        arrayLinkedV.add(vName);
    }
    public void AddToVList(String vName){
        arrayLinkedV.add(vName);
    }
    
    public ArrayList<String> getArrayLinkedV(){
        return arrayLinkedV;
    }
    public void setArrayLinkedV(ArrayList<String> array){
        arrayLinkedV = array;
    }
    public String getName(){
        return nameR;
    }
    public void setName(String name){
        nameR = name;
    }
}
