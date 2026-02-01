package logic.serialization.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Figure_s {

    @JsonProperty("x_pos")
    private String x_pos;
    @JsonProperty("y_pos")
    private String y_pos;
    @JsonProperty("shape")
    private String shape;
    @JsonProperty("size")
    private String size;
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name; // Имя фигуры
    @JsonProperty("description")
    private String description; // Подробное описание объекта
    @JsonProperty("nameNvElement")
    private ArrayList<String> nameNvElement = new ArrayList(); // Лист со входными переменными
    @JsonProperty("varNvElement")
    private ArrayList<String> varNvElement = new ArrayList(); // Лист с выходными переменными
    @JsonProperty("code")
    private String code; // Код фигуры
    
    @JsonProperty("likelihood")
    private String likelihood;//вероятность для S
    @JsonProperty("period")
    private String period;//период для S
    @JsonProperty("coef")
    private String coef;//коэффициент эффективности для О
    @JsonProperty("SWorkIndex")
    private String SWorkIndex;//отметка выбора вида работы для S
    @JsonProperty("vSelected")
    private String vSelected;
    @JsonProperty("llmPrompt")
    private String llmPrompt;
    @JsonProperty("ifSelected")
    private int ifSelected; //0-i, 1-nv
    @JsonProperty("ifNvElement")
    private String ifNvElement; //выбранная переменная из NV для сравнения
    @JsonProperty("signIfSelected")
    private int signIfSelected; //0-< 1-<= 2-= 3->= 4-> 
    @JsonProperty("compareNumber")
    private int compareNumber; // Число с которым сравниваем в IF


//    @JsonProperty("likelihood")
//    отметка выбора сложности для V

    public void setX_pos(String x_pos) {
        this.x_pos = x_pos;
    }

    public void setY_pos(String y_pos) {
        this.y_pos = y_pos;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getX_pos() {
        return x_pos;
    }

    public String getY_pos() {
        return y_pos;
    }

    public String getShape() {
        return shape;
    }

    public String getSize() {
        return size;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setNameNvElement(ArrayList<String> inVar) {
        this.nameNvElement = inVar;
    }

    public ArrayList<String> getNameNvElement() {
        return nameNvElement;
    }

    public void setVarNvElement(ArrayList<String> outVar) {
        this.varNvElement = outVar;
    }

    public ArrayList<String> getVarNvElement() {
        return varNvElement;
    }
    
    public void setCoef(String coef) {
        this.coef = coef;
    }

    public String getCoef() {
        return coef;
    }
    
    public void setLikelihood(String likelihood) {
        this.likelihood = likelihood;
    }

    public String getLikelihood() {
        return likelihood;
    }
    
    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPeriod() {
        return period;
    }
    
    public void setSwork(String SWorkIndex) {
        this.SWorkIndex = SWorkIndex;
    }

    public String getSwork() {
        return SWorkIndex;
    }
    public void setVSelected(String vSelected) {
        this.vSelected = vSelected;
    }

    public String getVSelected() {
        return vSelected;
    }
    public void setLlmPrompt(String llmPrompt) {
        this.llmPrompt = llmPrompt;
    }
    public String getLlmPrompt() {
        return llmPrompt;
    }
    public void setSignIfSelected(int var){
        signIfSelected = var;
    }
    public void setIfNvElement(String var){
        ifNvElement = var;
    }
    public void setIfSelected(int var){
        ifSelected = var;
    }  
    public int getSignIfSelected(){
        return signIfSelected;
    }
    public String getIfNvElement(){
        return ifNvElement;
    }
    public int getIfSelected(){
        return ifSelected;
    }
    public int getCompareNumber(){
        return compareNumber;
    }
    public void setCompareNumber(int var){
        compareNumber = var;
    }
}
