package logic.description.gen;

import java.util.ArrayList;

public class subObjV {
    private String linkedNv = "";
    private String linkedO = "";
    private ArrayList<String> arrayLinkedSR = new ArrayList<String>();
    private String oValue;
    private String nameV;
    private String type;
    
    public subObjV(String name,String type){ //СЃС‚Р°РЅРґР°СЂС‚РЅС‹Р№ РіРµРЅРµСЂР°С‚РѕСЂ СЃ РїСЂРёРєСЂРµРїР»РµРЅРёРµРј РёРјРµРЅРё
        nameV = name;
        setNumberType(type);
    }
    //РіРµРЅРµСЂР°С‚РѕСЂ РґР»СЏ РєР»РѕРЅРёСЂРѕРІР°РЅРёСЏ РѕР±СЉРµРєС‚Р°
    public subObjV(String name,String type,  ArrayList<String> oldSR,  String oldNv, String oldO, String oldOValue){ //СЃС‚Р°РЅРґР°СЂС‚РЅС‹Р№ РіРµРЅРµСЂР°С‚РѕСЂ СЃ РїСЂРёРєСЂРµРїР»РµРЅРёРµРј РёРјРµРЅРё
        nameV = name;
        this.type = type;
        linkedO = oldO;
        for (String el: oldSR){
            arrayLinkedSR.add(el);
        }
        linkedNv = oldNv;
        oValue = oldOValue;
    }

    public void AddToSRList(String vName){
        arrayLinkedSR.add(vName);
    }
    public boolean isEmpty(){
        if (linkedNv == "" & linkedO == "" & arrayLinkedSR.size() == 0){
            return true;
        }
        return false;
    }
    public String getLinkedNv(){
        return linkedNv;
    }
    public void setLinkedNv(String link){
        linkedNv = link;
    }
    public String getLinkedO(){
        return linkedO;
    }
    public void setLinkedO(String link){
        linkedO = link;
    }
    public ArrayList<String> getArrayLinkedSR(){
        return arrayLinkedSR;
    }
    public void setArrayLinkedSR(ArrayList<String> array){
        arrayLinkedSR = array;
    }
    public String getType(){
        return type;
    }
    public void setType(String type){
        setNumberType(type);
    }
    public String getName(){
        return nameV;
    }
    public void setName(String name){
        nameV = name;
    }
    public void setOValue(String number){
        oValue=number;
    }
    public String getOValue(){
        return oValue;
    }

    private void setNumberType(String textType){
        if (textType == null || textType.isEmpty()) {
            type = "1";
            return;
        }
        String norm = textType.toLowerCase();
        if (norm.contains("exp") || norm.contains("СЌРєСЃРї")) { // СЌРєСЃРїРѕРЅРµРЅС‚Р°
            type = "4";
            return;
        }
        if (norm.contains("n * log") || norm.contains("n*log") || norm.contains("xlog")) { // n*log(n)
            type = "3";
            return;
        }
        if (norm.contains("log") || norm.contains("Р»РѕРі")) { // Р»РѕРіР°СЂРёС„Рј
            type = "3";
            return;
        }
        if (norm.contains("1") || norm.contains("С€Р°Рі")) { // 1 С€Р°Рі
            type = "1";
            return;
        }
        if (norm.contains("o ( n )") || norm.contains("o(n)") || norm.contains("СЌР»РµРј")) { // Р»РёРЅРµР№РЅР°СЏ
            type = "1";
            return;
        }
        if (norm.contains("custom") || norm.contains("РёРЅРґРёРІ") || norm.contains("llm")) { // РїРѕР»СЊР·РѕРІР°С‚РµР»СЊСЃРєР°СЏ/LLM
            type = "1";
            return;
        }
        type = "1";
    }
}
