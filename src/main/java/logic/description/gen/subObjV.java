package logic.description.gen;

import java.util.ArrayList;

public class subObjV {
    private String linkedNv = "";
    private String linkedO = "";
    private ArrayList<String> arrayLinkedSR = new ArrayList<String>();
    private String oValue;
    private String nameV;
    private String type;

    public subObjV(String name, String type) {
        nameV = name;
        setNumberType(type);
    }

    public subObjV(String name, String type, ArrayList<String> oldSR, String oldNv, String oldO, String oldOValue) {
        nameV = name;
        this.type = type;
        linkedO = oldO;
        for (String el : oldSR) {
            arrayLinkedSR.add(el);
        }
        linkedNv = oldNv;
        oValue = oldOValue;
    }

    public void AddToSRList(String vName) {
        arrayLinkedSR.add(vName);
    }

    public boolean isEmpty() {
        if (linkedNv == "" & linkedO == "" & arrayLinkedSR.size() == 0) {
            return true;
        }
        return false;
    }

    public String getLinkedNv() {
        return linkedNv;
    }

    public void setLinkedNv(String link) {
        linkedNv = link;
    }

    public String getLinkedO() {
        return linkedO;
    }

    public void setLinkedO(String link) {
        linkedO = link;
    }

    public ArrayList<String> getArrayLinkedSR() {
        return arrayLinkedSR;
    }

    public void setArrayLinkedSR(ArrayList<String> array) {
        arrayLinkedSR = array;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        setNumberType(type);
    }

    public String getName() {
        return nameV;
    }

    public void setName(String name) {
        nameV = name;
    }

    public void setOValue(String number) {
        oValue = number;
    }

    public String getOValue() {
        return oValue;
    }

    private void setNumberType(String textType) {
        if (textType == null || textType.isEmpty()) {
            type = "1";
            return;
        }

        String trimmed = textType.trim();
        if (trimmed.matches("\\d+")) {
            type = trimmed;
            return;
        }

        String norm = trimmed.toLowerCase();
        if (norm.contains("llm")) {
            type = "11";
            return;
        }
        if (norm.contains("custom") || norm.contains("индив")) {
            type = "10";
            return;
        }
        if (norm.contains("exp")) {
            type = "4";
            return;
        }
        if (norm.contains("n * log") || norm.contains("n*log") || norm.contains("xlog") || norm.contains("log")) {
            type = "3";
            return;
        }
        if (norm.contains("o ( 1 )") || norm.contains("o(1)") || norm.equals("1")) {
            type = "5";
            return;
        }
        if (norm.contains("o ( n )") || norm.contains("o(n)")) {
            type = "1";
            return;
        }

        type = "1";
    }
}
