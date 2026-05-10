package logic.serialization.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerationSettings {
    private int nValue = 1000;
    private int iValue = 1;
    private int fpValue = 1;
    private int startId = 1;
    private int stepId = 1;
    private boolean graphState = true;
    private boolean xesState = true;
    private boolean oActiveState = true;
    private String xesName = "result";

    public static GenerationSettings defaults() {
        return new GenerationSettings();
    }

    public int getNValue() {
        return nValue;
    }

    public void setNValue(int nValue) {
        this.nValue = nValue;
    }

    public int getIValue() {
        return iValue;
    }

    public void setIValue(int iValue) {
        this.iValue = iValue;
    }

    public int getFpValue() {
        return fpValue;
    }

    public void setFpValue(int fpValue) {
        this.fpValue = fpValue;
    }

    public int getStartId() {
        return startId;
    }

    public void setStartId(int startId) {
        this.startId = startId;
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public boolean isGraphState() {
        return graphState;
    }

    public void setGraphState(boolean graphState) {
        this.graphState = graphState;
    }

    public boolean isXesState() {
        return xesState;
    }

    public void setXesState(boolean xesState) {
        this.xesState = xesState;
    }

    public boolean isOActiveState() {
        return oActiveState;
    }

    public void setOActiveState(boolean oActiveState) {
        this.oActiveState = oActiveState;
    }

    public String getXesName() {
        return xesName;
    }

    public void setXesName(String xesName) {
        this.xesName = xesName;
    }
}

