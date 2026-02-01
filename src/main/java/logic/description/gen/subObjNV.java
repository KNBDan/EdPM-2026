package logic.description.gen;

import java.util.ArrayList;

public class subObjNV {
    private ArrayList<String> arrayLinkedR = new ArrayList<String>();
    private String nameNV;
    
    public subObjNV(String name){ //стандартный генератор с прикреплением имени
        nameNV = name;
    }
    public subObjNV(String name, String rName){ //стандартный генератор с прикреплением имени и первой фигуры
        nameNV = name;
        arrayLinkedR.add(rName);
    }
    public void AddToRList(String rName){
        arrayLinkedR.add(rName);
    }
    
    public ArrayList<String> getArrayLinkedR(){
        return arrayLinkedR;
    }
    public void setArrayLinkedR(ArrayList<String> array){
        arrayLinkedR = array;
    }
    public String getName(){
        return nameNV;
    }
    public void setName(String name){
        nameNV = name;
    }
}
