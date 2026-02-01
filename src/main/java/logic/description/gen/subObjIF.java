package logic.description.gen;

import java.util.ArrayList;

public class subObjIF {
    private String linkedInR; //в теории только по одной фигуре на вход и выход да/нет, так что просто сттроки
    private String linkedOutTrueR;
    private String linkedOutFalseNV;
    private String linkedOutFalseV;
    private String compareElementName ;
    private String compareNumber;
    private String compareSign;
   
    private String nameIF;
    
    public subObjIF(String name){ //стандартный генератор с прикреплением имени
        nameIF = name;
    }
    public boolean isCorrect(){ // если нет одного из элементов if не имеет смысла
        if ((linkedOutFalseV == null && linkedOutFalseNV == null) || linkedOutTrueR == null || linkedInR == null){
            return false;
        }
        return true;
    }
    public String getLinkedOutTrueR(){
        return linkedOutTrueR;
    }
    public void setLinkedOutTrueR(String rLink){
        linkedOutTrueR = rLink;
    }
     public void setLinkedOutFalseNV(String nvLink){
        linkedOutFalseNV = nvLink;
    }
    public String getLinkedOutFalseNV(){
        return linkedOutFalseNV;
    }
    public void setLinkedOutFalseV(String vLink){
        linkedOutFalseV = vLink;
    }
    public String getLinkedOutFalseV(){
        return linkedOutFalseV;
    }
    public void setLinkedInR(String rLink){
        linkedInR = rLink;
    }
    public String getLinkedInR(){
        return linkedInR;
    }
    public String getName(){
        return nameIF;
    }
    public void setName(String name){
        nameIF = name;
        
    }public String getComapreSign(){
        return compareSign;
    }
    public void setComapreSign( int sign){
        switch(sign){
            case(0):
                compareSign = "<";
                break;
            case(1):
                compareSign = "<=";
                break;
            case(2):
                compareSign = "=";
                break;
            case(3):
                compareSign = ">=";
                break;
            case(4):
                compareSign = ">";
                break;
        }
        
    }public String getCompareNumber(){
        return compareNumber;
    }
    public void setCompareNumber(int num){
        compareNumber = Integer.toString(num);
    }
    public String getCompareElementName(){
        return compareElementName;
    }
    public void setCompareElementName(String cName){
        compareElementName = cName;
    }
}
