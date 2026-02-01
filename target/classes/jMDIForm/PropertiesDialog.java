package jMDIForm;

import objects.figure.figures;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.w3c.dom.NameList;

public class PropertiesDialog extends javax.swing.JDialog {

    private DefaultListModel<String> listModelName = new DefaultListModel<>(); //для отображения списка Имен переменных NV
    private DefaultListModel<String> listModelNumber = new DefaultListModel<>(); //для отображения списка Занчений переменныъх NV
    int delIndIn = -1; //Индекс для удаления
    int delIndOut = -1; //Индекс для удаления
    Point2D p; //текущая точка
    String typeAdd; //in или out;
    figures figOnWork; //текущая фигура
    String curShape;//текущий тип фигуры
    public ArrayList<String> nameNvEl = new ArrayList(); // Лист со входными переменными для сохранения
    public ArrayList<String> varNvEl = new ArrayList(); // Лист с выходными переменными для сохранения
    private ArrayList<figures> curFig;
    int SWorkIndex;//выбранный тип работы 0-поток пуассона, 1- периодическая
    int VProperties; // ????????? ????? ? V ?????? 0-??????????, 1-???????, 2-1???, 3-????????, 4-xlog
    private static final String V_LLM_COMPLEXITY_NAME = "LLM Generate code"; // выбранный пункт в V фигуре 0-экспонента, 1-элеиент, 2-1шаг, 3-логарифм, 4-xlog
    boolean isOkPressed=false;
    
    
    public PropertiesDialog(java.awt.Frame parent, boolean modal,  figures fig, ArrayList<figures> allFigures) {
        super(parent, modal);
        initComponents();
        
        descriptionTextField.setLineWrap(true);    // Включить перенос строк
        descriptionTextField.setWrapStyleWord(true); // Перенос по словам (а не по символам)
        descriptionTextField.setAlignmentY(JTextArea.TOP_ALIGNMENT); // Выравнивание по верху
        descriptionTextField.setAlignmentX(JTextArea.LEFT_ALIGNMENT); // Выравнивание по левому краю
        
        SwingUtilities.invokeLater(() -> {
             descriptionTextField.requestFocusInWindow();
        });
        
        curFig = allFigures;
        nameListOfEl.setModel(listModelName); //привязываем лист имен переменных NV и лист модел      
        varListOfEl.setModel(listModelNumber); //привязываем лист Занчений переменныъх NV и лист модел
        figOnWork = fig; //Связываем оригинальную и вспомогательную фигуру-копию
        curShape = fig.getClass().toString().replace("class objects.figure.", "");
        if (curShape.equals("S1")){
            curShape = "S";
        }
        if(curShape.equals("d")){
            curShape = "IF";
        }

        nameNvEl = getArray(figOnWork.getNameNvElement());
        varNvEl = getArray(figOnWork.getVarNvElement());
        // Заполняем поля от фигуры
        this.setTitle(fig.getNameF()); // Устанавливаем заголовок как имя объекта
        shapeName.setText(curShape); //устанавливаем тип фигуры
        figuresNimberField.setText(fig.getNameF().replace(curShape,"")); //устанавливаем номер фигуры
       
        descriptionTextField.setText(fig.getDescriptionF());   //устанавливаем описание фигуры
        //установка s параметров
        Swork.setSelectedIndex(fig.getSwork());
        SWorkIndex = Swork.getSelectedIndex();
        
        mainBodyTabbedPanel.addTab("Basic properties",mainPanel); // добавление нужных окон делаем через этот метод
        switch (curShape) {//устанавливаем спец свойства
            case "S":
                mainBodyTabbedPanel.addTab("Properties of S",SpropertiesPanel); // добавление нужных окон делаем через этот метод
                if (SWorkIndex == 0) {
                    probabilityLabel.setEnabled(true);
                    Slikelihood.setEnabled(true);
                    Slikelihood.setText(figOnWork.getLikelihood());//устанавливаем значение вероятнсти
                } else if (SWorkIndex == 1) {
                    periodLabel.setEnabled(true);
                    Speriod.setEnabled(true);
                    Speriod.setText(figOnWork.getPeriod());//устанавливаем значение периода
                }
                break;
            case "V":
                String vSelected = figOnWork.getVSelected();
                if (vSelected == null || vSelected.isEmpty()) {
                    vSelected = expButton.getText();
                }
                if (vSelected.equals("Экспонента (exp(x))")) {
                    vSelected = expButton.getText();
                }
                if (vSelected.equals("Llm code")) {
                    vSelected = V_LLM_COMPLEXITY_NAME;
                }
                figOnWork.setVSelected(vSelected);
                for (Enumeration<AbstractButton> buttons = properties.getElements(); buttons.hasMoreElements();) {
                    AbstractButton button = buttons.nextElement();
                    if (button.getText().equals(vSelected)) {
                        button.setSelected(true);
                        break;
                    }
                }
                mainBodyTabbedPanel.addTab("Propertires of V",VpropertiesPanel); // добавление нужных окон делаем через этот метод
                if (fig.getVSelected().equals("Custom complexity function")){
                    addCodeWindowInProp();
                    String transform = fig.getCodeF();
                    String[] rows = transform.split("\n");
                    transform = "";
                    for (int i = 1; i<rows.length-2;i++){
                        transform += rows[i]+'\n';
                    }
                    firstStringCodeField.setText(rows[0]);
                    lastStringCodeField.setText(rows[rows.length-2]);
                    codeTextField.setText(transform); // Установка текста кода фигуры
                }
                if (fig.getVSelected().equals(V_LLM_COMPLEXITY_NAME)){
                    addLlmCodeWindowInProp();
                    llmCodeTextField.setText(fig.getLlmPrompt());
                }
                break;
            case "O":
                mainBodyTabbedPanel.addTab("Propertires of O",OpropertiesPanel); // добавление нужных окон делаем через этот метод
                Ocoef.setText(fig.getCoef());//утсанавливаем значение коэффициента эффективности
                break;
            case "IF":
                ArrayList<String> newAr = new ArrayList<String>(); //Обновление выпадающего списка с названиями переменных NV для IF
                for (figures s : curFig){
                    if (s.getNameNvElement().size()!=0){
                        for (String el:s.getNameNvElement()){
                            newAr.add(el) ;
                        }
                    }
                }
                nvComboBox.setModel(new DefaultComboBoxModel<>(newAr.toArray(new String[0])));
                
                mainBodyTabbedPanel.addTab("Propertires of IF", IfPropertiesPanel);
                if (fig.getIfSelected() == 0){
                    selectI.setSelected(true);
                }else{
                    selectNV.setSelected(true);
                }
                compareNumberField.setText(String.valueOf(fig.getCompareNumber()));
                signComboBox.setSelectedIndex(fig.getSignIfSelected());
                
                nvComboBox.setSelectedItem(figOnWork.getIfNvElement());
                break;
            case "NV":
                mainBodyTabbedPanel.addTab("Propertires of NV",nvPropertiesPanel);
                break;
        }
        
        
        // Заполняем списки переменными фигуры
        for (String inVar : nameNvEl){ 
            listModelName.addElement(inVar);
        }
        for (String outVar : varNvEl){
            listModelNumber.addElement(outVar);
        }   
        addNvButton.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none"); //что это?
        
        pack(); 
    }
   
    
    
    private ArrayList<String> getArray(ArrayList<String> mas){
        ArrayList<String> newAr = new ArrayList<String>();
        for (String el : mas){
            newAr.add(el);
        }
        return newAr;
    }
    private PropertiesDialog(JFrame jFrame, boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        properties = new javax.swing.ButtonGroup();
        ifProp = new javax.swing.ButtonGroup();
        SpropertiesPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        Swork = new javax.swing.JComboBox<>();
        probabilityLabel = new javax.swing.JLabel();
        Slikelihood = new javax.swing.JFormattedTextField();
        periodLabel = new javax.swing.JLabel();
        Speriod = new javax.swing.JFormattedTextField();
        mainPanel = new javax.swing.JPanel();
        nameLabel = new java.awt.Label();
        label6 = new java.awt.Label();
        figuresNimberField = new javax.swing.JFormattedTextField();
        shapeName = new javax.swing.JLabel();
        nameLabel1 = new java.awt.Label();
        jScrollPane2 = new javax.swing.JScrollPane();
        descriptionTextField = new javax.swing.JTextArea();
        codePanel = new javax.swing.JScrollPane();
        codeTPanel = new javax.swing.JPanel();
        firstStringCodeField = new javax.swing.JLabel();
        lastStringCodeField = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        codeTextField = new javax.swing.JTextPane();
        endString = new javax.swing.JLabel();
        VpropertiesPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        expButton = new javax.swing.JRadioButton();
        xButton = new javax.swing.JRadioButton();
        stepButton = new javax.swing.JRadioButton();
        logButton = new javax.swing.JRadioButton();
        xlogButton = new javax.swing.JRadioButton();
        individCodeButton = new javax.swing.JRadioButton();
        llmGeneratedCodeButton = new javax.swing.JRadioButton();
        OpropertiesPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        Ocoef = new javax.swing.JFormattedTextField();
        IfPropertiesPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        selectI = new javax.swing.JRadioButton();
        signComboBox = new javax.swing.JComboBox<>();
        compareNumberField = new javax.swing.JFormattedTextField();
        jLabel7 = new javax.swing.JLabel();
        selectNV = new javax.swing.JRadioButton();
        nvComboBox = new javax.swing.JComboBox<>();
        nvPropertiesPanel = new javax.swing.JPanel();
        nameOfEl = new javax.swing.JScrollPane();
        nameListOfEl = new javax.swing.JList<>();
        varOfEl = new javax.swing.JScrollPane();
        varListOfEl = new javax.swing.JList<>();
        addNvButton = new javax.swing.JButton();
        deleteNvButton = new javax.swing.JButton();
        editNvButton = new javax.swing.JButton();
        changeNvelement = new javax.swing.JDialog();
        labelStandart = new javax.swing.JLabel();
        varField = new javax.swing.JFormattedTextField();
        nameField = new javax.swing.JTextField();
        nameOfElDefault = new javax.swing.JLabel();
        SaveNv = new javax.swing.JButton();
        BackNv = new javax.swing.JButton();
        labelStandart1 = new javax.swing.JLabel();
        llmCodePanel = new javax.swing.JPanel();
        llmCodeScrollPanel = new javax.swing.JScrollPane();
        llmCodeTextField = new javax.swing.JTextPane();
        mainBodyTabbedPanel = new javax.swing.JTabbedPane();
        cancelPropBut = new javax.swing.JButton();
        savePropBut = new javax.swing.JButton();

        SpropertiesPanel.setEnabled(false);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("The operating principle"));

        Swork.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Poisson process", "Periodic process" }));
        Swork.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SworkActionPerformed(evt);
            }
        });

        probabilityLabel.setText("Probability:");
        probabilityLabel.setEnabled(false);

        Slikelihood.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        Slikelihood.setToolTipText("0,1 - 10");
        Slikelihood.setEnabled(false);
        Slikelihood.setMaximumSize(new java.awt.Dimension(133, 23));
        Slikelihood.setMinimumSize(new java.awt.Dimension(70, 23));
        Slikelihood.setName(""); // NOI18N
        Slikelihood.setPreferredSize(new java.awt.Dimension(70, 23));
        Slikelihood.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SlikelihoodActionPerformed(evt);
            }
        });
        Slikelihood.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SlikelihoodKeyTyped(evt);
            }
        });

        periodLabel.setText("Period:");
        periodLabel.setEnabled(false);

        Speriod.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        Speriod.setToolTipText("1-1000");
        Speriod.setEnabled(false);
        Speriod.setMaximumSize(new java.awt.Dimension(133, 23));
        Speriod.setMinimumSize(new java.awt.Dimension(70, 23));
        Speriod.setPreferredSize(new java.awt.Dimension(70, 23));
        Speriod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SperiodActionPerformed(evt);
            }
        });
        Speriod.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SperiodKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Swork, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(probabilityLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Slikelihood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(periodLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Speriod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Swork, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(probabilityLabel)
                    .addComponent(Slikelihood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(periodLabel)
                    .addComponent(Speriod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout SpropertiesPanelLayout = new javax.swing.GroupLayout(SpropertiesPanel);
        SpropertiesPanel.setLayout(SpropertiesPanelLayout);
        SpropertiesPanelLayout.setHorizontalGroup(
            SpropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SpropertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        SpropertiesPanelLayout.setVerticalGroup(
            SpropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SpropertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nameLabel.setText("Name of");

        label6.setText("Description:");

        figuresNimberField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        figuresNimberField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                figuresNimberFieldActionPerformed(evt);
            }
        });

        shapeName.setText("jLabel8");

        nameLabel1.setText(":");

        descriptionTextField.setColumns(20);
        descriptionTextField.setRows(5);
        jScrollPane2.setViewportView(descriptionTextField);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(nameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(1, 1, 1)
                                .addComponent(shapeName)
                                .addGap(1, 1, 1)
                                .addComponent(nameLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(20, 20, 20)
                        .addComponent(figuresNimberField, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(figuresNimberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(shapeName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nameLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nameLabel.getAccessibleContext().setAccessibleName("Name of ");

        firstStringCodeField.setText("V1_def (Function name) <- function(S, V){");

        lastStringCodeField.setText(" (Function name)<-Df ");

        codeTextField.setText(" # O( log N ) analysis\n N<-length(S$S)\n Df<-data.frame(I=1:N,  \n                J=vector(mode = \"numeric\", length = length(N)),  \n                Prj_Flow=S$S,  \n                Prj_File=vector(mode = \"numeric\", length = length(N)),  \n                V_W=vector(mode = \"numeric\", length = length(N)),  \n                V=V,  \n                R=vector(mode = \"numeric\", length = length(N)),  \n                ID_File=vector(mode = \"numeric\", length = length(N)),  \n                ID_Out=vector(mode = \"numeric\", length = length(N)))\n j<-1\n L<-0\n\n for (i in 1:N){\n   Df$Prj_File[i]<-sum(Df$Prj_Flow[i:j])\n   Df$ID_File[i]<-list(unique(list_c(S$ID[i:j])))\n   Df$J[i]<-j\n   if (Df$V_W[i]==0) {\n     nk<-Df$Prj_File[i]\n     L<-1 \n     k<-min(i+L-1,N)\n     if (k>=i){\n       Df$V_W[i:k]<-L\n       Df$R[k] <- nk\n       Df$ID_Out[k]<-Df$ID_File[i]\n     }\n     j<-i+1\n   }\n }\n\n P1<-Df$R\n P2<-Df$ID_Out\n Df$R[1]<-0\n Df$R[2:N]<-P1[1:(N-1)]\n Df$ID_Out[1]<-0\n Df$ID_Out[2:N]<-P2[1:(N-1)]");
        jScrollPane1.setViewportView(codeTextField);

        endString.setText(" }");

        javax.swing.GroupLayout codeTPanelLayout = new javax.swing.GroupLayout(codeTPanel);
        codeTPanel.setLayout(codeTPanelLayout);
        codeTPanelLayout.setHorizontalGroup(
            codeTPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(codeTPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(codeTPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lastStringCodeField)
                    .addComponent(endString)
                    .addComponent(firstStringCodeField)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        codeTPanelLayout.setVerticalGroup(
            codeTPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(codeTPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(firstStringCodeField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lastStringCodeField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(endString, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        codePanel.setViewportView(codeTPanel);

        VpropertiesPanel.setEnabled(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("The algorithmic complexity of the algorithm"));

        properties.add(expButton);
        expButton.setSelected(true);
        expButton.setText("O ( Exp( N ) )");
        expButton.setActionCommand("O(exp(x))");
        expButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expButtonActionPerformed(evt);
            }
        });

        properties.add(xButton);
        xButton.setText("O ( N )");
        xButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xButtonActionPerformed(evt);
            }
        });

        properties.add(stepButton);
        stepButton.setText("O ( 1 )");

        properties.add(logButton);
        logButton.setText("O ( Log( N ) )");

        properties.add(xlogButton);
        xlogButton.setText("O ( N * Log( N ) )");

        properties.add(individCodeButton);
        individCodeButton.setText("Custom complexity function");
        individCodeButton.setAutoscrolls(true);
        individCodeButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                individCodeButtonItemStateChanged(evt);
            }
        });

        properties.add(llmGeneratedCodeButton);
        llmGeneratedCodeButton.setText(V_LLM_COMPLEXITY_NAME);
        llmGeneratedCodeButton.setAutoscrolls(true);
        llmGeneratedCodeButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                llmGeneratedCodeButtonItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(expButton)
                    .addComponent(xButton)
                    .addComponent(stepButton)
                    .addComponent(logButton)
                    .addComponent(xlogButton)
                    .addComponent(individCodeButton)
                    .addComponent(llmGeneratedCodeButton))
                .addGap(65, 65, 65))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(expButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stepButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xlogButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(individCodeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(llmGeneratedCodeButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout VpropertiesPanelLayout = new javax.swing.GroupLayout(VpropertiesPanel);
        VpropertiesPanel.setLayout(VpropertiesPanelLayout);
        VpropertiesPanelLayout.setHorizontalGroup(
            VpropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VpropertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        VpropertiesPanelLayout.setVerticalGroup(
            VpropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VpropertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        OpropertiesPanel.setVerifyInputWhenFocusTarget(false);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Performance correction coefficient:");

        Ocoef.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.#"))));
        Ocoef.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        Ocoef.setText("0");
        Ocoef.setToolTipText("0,1-10");
        Ocoef.setActionCommand("<Not Set>");
        Ocoef.setDoubleBuffered(true);
        Ocoef.setMaximumSize(new java.awt.Dimension(133, 23));
        Ocoef.setMinimumSize(new java.awt.Dimension(70, 23));
        Ocoef.setPreferredSize(new java.awt.Dimension(70, 23));
        Ocoef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OcoefActionPerformed(evt);
            }
        });
        Ocoef.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                OcoefKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout OpropertiesPanelLayout = new javax.swing.GroupLayout(OpropertiesPanel);
        OpropertiesPanel.setLayout(OpropertiesPanelLayout);
        OpropertiesPanelLayout.setHorizontalGroup(
            OpropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OpropertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Ocoef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        OpropertiesPanelLayout.setVerticalGroup(
            OpropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OpropertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(OpropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(Ocoef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Condition"));

        ifProp.add(selectI);
        selectI.setSelected(true);
        selectI.setText("step (i)");

        signComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "<", "<=", "=", ">=", ">" }));
        signComboBox.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        signComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signComboBoxActionPerformed(evt);
            }
        });

        compareNumberField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        compareNumberField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compareNumberFieldActionPerformed(evt);
            }
        });

        jLabel7.setText("Value:");

        ifProp.add(selectNV);
        selectNV.setText("NV");
        selectNV.setToolTipText("");
        selectNV.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                selectNVStateChanged(evt);
            }
        });
        selectNV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNVActionPerformed(evt);
            }
        });

        nvComboBox.setEnabled(false);
        nvComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                nvComboBoxItemStateChanged(evt);
            }
        });
        nvComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nvComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectI)
                    .addComponent(selectNV))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(signComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(compareNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(nvComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectI)
                    .addComponent(signComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(compareNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectNV)
                    .addComponent(nvComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout IfPropertiesPanelLayout = new javax.swing.GroupLayout(IfPropertiesPanel);
        IfPropertiesPanel.setLayout(IfPropertiesPanelLayout);
        IfPropertiesPanelLayout.setHorizontalGroup(
            IfPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(IfPropertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        IfPropertiesPanelLayout.setVerticalGroup(
            IfPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(IfPropertiesPanelLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nameOfEl.addHierarchyListener(new java.awt.event.HierarchyListener() {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
                nameOfElHierarchyChanged(evt);
            }
        });

        nameListOfEl.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        nameListOfEl.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        nameListOfEl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                nameListOfElMousePressed(evt);
            }
        });
        nameListOfEl.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                nameListOfElPropertyChange(evt);
            }
        });
        nameListOfEl.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                nameListOfElValueChanged(evt);
            }
        });
        nameOfEl.setViewportView(nameListOfEl);

        varListOfEl.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        varListOfEl.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        varListOfEl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                varListOfElMousePressed(evt);
            }
        });
        varListOfEl.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                varListOfElValueChanged(evt);
            }
        });
        varOfEl.setViewportView(varListOfEl);

        addNvButton.setText("Add");
        addNvButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNvButtonActionPerformed(evt);
            }
        });

        deleteNvButton.setText("Delete");
        deleteNvButton.setEnabled(false);
        deleteNvButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteNvButtonActionPerformed(evt);
            }
        });

        editNvButton.setText("Edit");
        editNvButton.setEnabled(false);
        editNvButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editNvButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout nvPropertiesPanelLayout = new javax.swing.GroupLayout(nvPropertiesPanel);
        nvPropertiesPanel.setLayout(nvPropertiesPanelLayout);
        nvPropertiesPanelLayout.setHorizontalGroup(
            nvPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nvPropertiesPanelLayout.createSequentialGroup()
                .addGroup(nvPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(nvPropertiesPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(nameOfEl, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(nvPropertiesPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addNvButton)
                        .addGap(18, 18, 18)
                        .addComponent(deleteNvButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGroup(nvPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(nvPropertiesPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(editNvButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(nvPropertiesPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(varOfEl, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        nvPropertiesPanelLayout.setVerticalGroup(
            nvPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nvPropertiesPanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(nvPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(varOfEl, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                    .addComponent(nameOfEl))
                .addGap(18, 18, 18)
                .addGroup(nvPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addNvButton)
                    .addComponent(deleteNvButton)
                    .addComponent(editNvButton))
                .addContainerGap())
        );

        labelStandart.setText("Name of variable");

        varField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        varField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                varFieldActionPerformed(evt);
            }
        });

        nameField.setText("jTextField1");

        nameOfElDefault.setText("var_nv1231");

        SaveNv.setText("Ok");
        SaveNv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveNvActionPerformed(evt);
            }
        });

        BackNv.setText("Cancel");

        labelStandart1.setText("Value:");

        javax.swing.GroupLayout changeNvelementLayout = new javax.swing.GroupLayout(changeNvelement.getContentPane());
        changeNvelement.getContentPane().setLayout(changeNvelementLayout);
        changeNvelementLayout.setHorizontalGroup(
            changeNvelementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(changeNvelementLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(changeNvelementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(changeNvelementLayout.createSequentialGroup()
                        .addGroup(changeNvelementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelStandart1)
                            .addGroup(changeNvelementLayout.createSequentialGroup()
                                .addComponent(labelStandart)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nameOfElDefault)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(changeNvelementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(varField)
                            .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(changeNvelementLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(SaveNv)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(BackNv)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        changeNvelementLayout.setVerticalGroup(
            changeNvelementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(changeNvelementLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(changeNvelementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelStandart)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameOfElDefault))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(changeNvelementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(varField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelStandart1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(changeNvelementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SaveNv)
                    .addComponent(BackNv))
                .addContainerGap())
        );

        llmCodeTextField.setText("Eneter llm request here");
        llmCodeScrollPanel.setViewportView(llmCodeTextField);

        javax.swing.GroupLayout llmCodePanelLayout = new javax.swing.GroupLayout(llmCodePanel);
        llmCodePanel.setLayout(llmCodePanelLayout);
        llmCodePanelLayout.setHorizontalGroup(
            llmCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(llmCodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(llmCodeScrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        llmCodePanelLayout.setVerticalGroup(
            llmCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(llmCodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(llmCodeScrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(83, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        cancelPropBut.setText("Cancel");
        cancelPropBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelPropButActionPerformed(evt);
            }
        });

        savePropBut.setText("Ok");
        savePropBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePropButActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(savePropBut)
                        .addGap(18, 18, 18)
                        .addComponent(cancelPropBut)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(mainBodyTabbedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 379, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainBodyTabbedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelPropBut)
                    .addComponent(savePropBut))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
//        figOnWork.setNameF(nameTextField.getText());
//        figOnWork.setDescriptionF(descriptionTextField.getText());
//        figOnWork.setCodeF(codeTextField.getText()); //Раньше при закрытии окна сохранялись параметры фигуры, теперь при нажатии на кнопку
// РАСКОМЕНТИРОВАТЬ ЕСЛИ НУЖНО СОХРАНЕНЕ АВТОМАТИЧЕСКОЕ ПОСЛЕ ЗАКРЫТИЯ ОКНА!
    }//GEN-LAST:event_formWindowClosing

    

    private void savePropButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePropButActionPerformed
        //Сохранение параметров фигуры на кнопку
        String oldName = figOnWork.getNameF();
        figOnWork.setNameF(curShape+figuresNimberField.getText());

        figOnWork.setDescriptionF(descriptionTextField.getText());
        switch (curShape){
            case "S":
                figOnWork.setLikelihood(Slikelihood.getText());
                figOnWork.setPeriod(Speriod.getText());
                figOnWork.setSwork(Swork.getSelectedIndex());
                break;
            case "V":
                for (Enumeration<AbstractButton> buttons = properties.getElements(); buttons.hasMoreElements();) {
                    AbstractButton button = buttons.nextElement();
                    if (button.isSelected()) {
                        figOnWork.setVSelected(button.getText());
                    }
                }
                if (individCodeButton.isSelected()) {
                    figOnWork.setCodeF(firstStringCodeField.getText()+"\n"+codeTextField.getText()+"\n"+lastStringCodeField.getText()+"\n}");
                }
                if (llmGeneratedCodeButton.isSelected()) {
                    figOnWork.setLlmPrompt(llmCodeTextField.getText());
                } else {
                    figOnWork.setLlmPrompt("");
                }
                break;
            case "O":
                figOnWork.setCoef(Ocoef.getText());
                break;
            case "IF": //сохраняем поля IF
                if (selectI.isSelected() == true){ //если выбрана i
                    figOnWork.setIfSelected(0);
                }else{ //если выбрана n
                    figOnWork.setIfSelected(1);
                    figOnWork.setIfNvElement(nvComboBox.getSelectedItem().toString());
                }
                figOnWork.setSignIfSelected(signComboBox.getSelectedIndex()); //Выбранный знак сравнения
                figOnWork.setCompareNumber(Integer.valueOf(compareNumberField.getText())); //Выбранное число для сравнения
                
                break;
            case "NV":
                if (!oldName.equals(figOnWork.getNameF())){ //меняем имя у всех переменных нв при смене его имени
                    ArrayList<String> newAr = new ArrayList<String>();
                    for (String el: nameNvEl){
                        newAr.add(el.replace("var_"+oldName+"_","var_"+curShape+figuresNimberField.getText()+"_"));
                    }
                    figOnWork.setNameNvElement(newAr);
                }else{
                    figOnWork.setNameNvElement(nameNvEl);
                }
                figOnWork.setVarNvElement(varNvEl);
                break;
        }
       
        isOkPressed = true;
        this.dispose();
    }//GEN-LAST:event_savePropButActionPerformed

    private void cancelPropButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelPropButActionPerformed
        isOkPressed = false;
        this.dispose();
    }//GEN-LAST:event_cancelPropButActionPerformed

    private void SlikelihoodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SlikelihoodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SlikelihoodActionPerformed

    private void SlikelihoodKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SlikelihoodKeyTyped
        if (!Character.isDigit(evt.getKeyChar())) {
            evt.consume();
        }
    }//GEN-LAST:event_SlikelihoodKeyTyped

    private void SperiodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SperiodActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SperiodActionPerformed

    private void SperiodKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SperiodKeyTyped
        if (!Character.isDigit(evt.getKeyChar())) {
            evt.consume();
        }
    }//GEN-LAST:event_SperiodKeyTyped

    private void xButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_xButtonActionPerformed

    private void OcoefKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_OcoefKeyTyped
        if (!Character.isDigit(evt.getKeyChar())) {
            evt.consume();
        }
    }//GEN-LAST:event_OcoefKeyTyped

    private void SworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SworkActionPerformed
        SWorkIndex = Swork.getSelectedIndex();
        if (SWorkIndex == 0) {
            periodLabel.setEnabled(false);
            Speriod.setEnabled(false);
            probabilityLabel.setEnabled(true);
            Slikelihood.setEnabled(true);
            Slikelihood.setText("0,5");
            Speriod.setText("");
        } else if (SWorkIndex == 1) {
            probabilityLabel.setEnabled(false);
            Slikelihood.setEnabled(false);
            periodLabel.setEnabled(true);
            Speriod.setEnabled(true);
            Slikelihood.setText("");
            Speriod.setText("5");
        }
    }//GEN-LAST:event_SworkActionPerformed

    private void nameListOfElPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_nameListOfElPropertyChange
        
    }//GEN-LAST:event_nameListOfElPropertyChange
    
    int currentIndex; //Выбранный элемент в элементах NV
    private void nameListOfElValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_nameListOfElValueChanged
        currentIndex = nameListOfEl.getSelectedIndex();
        varListOfEl.setSelectedIndex(currentIndex);
        deleteNvButton.setText("Delete "+nameListOfEl.getSelectedValue());
        editNvButton.setText("Edit "+nameListOfEl.getSelectedValue());
    }//GEN-LAST:event_nameListOfElValueChanged

    private void varListOfElValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_varListOfElValueChanged
        currentIndex = varListOfEl.getSelectedIndex();
        nameListOfEl.setSelectedIndex(currentIndex);
        deleteNvButton.setText("Delete "+nameListOfEl.getSelectedValue());
        editNvButton.setText("Edit "+nameListOfEl.getSelectedValue());
    }//GEN-LAST:event_varListOfElValueChanged
      
    private void addNvButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNvButtonActionPerformed
        String initialValue = "var_"+figOnWork.getNameF()+"_";
        //Показываем окно ввода и деактивируем остальные окна
        String varName = initialValue + (String) JOptionPane.showInputDialog(null, "Type the variable name:", "Adding an input variable", JOptionPane.PLAIN_MESSAGE, null, null, String.valueOf(nameNvEl.size()+1));
        // Проверяем, было ли что-то введено
        if (!(varName.isEmpty() || varName.length()>=20)) {
            if (nameNvEl.contains(varName)){
                JOptionPane.showMessageDialog(null, "Variable name is already used!");
            }
            else{
                listModelName.addElement(varName); //добавялем элемент с введённым названием в список вход
                nameNvEl.add(varName); //добавялем элемент с введённым названием в фигуру
                listModelNumber.addElement(String.valueOf("0"));
                varNvEl.add(String.valueOf("0")); //добавялем элемент с введённым названием в фигуру
            }
        } else {
            JOptionPane.showMessageDialog(null, "This variable length is not supported.","Error",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_addNvButtonActionPerformed

    private void deleteNvButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteNvButtonActionPerformed
        nameNvEl.remove(delIndIn); //Удаляем элемент с выбранным индексом в фигуре
        listModelName.remove(delIndIn); //Удаляем элемент с выбранным индексом в списке
        varNvEl.remove(delIndIn); //Удаляем элемент с выбранным индексом в фигуре
        listModelNumber.remove(delIndIn); //Удаляем элемент с выбранным индексом в списке
        delIndIn = -1; //обнуляем индекс
        deleteNvButton.setEnabled(false); //Выключкаем кнопку удаления
        editNvButton.setEnabled(false); //Выключкаем кнопку удаления
        deleteNvButton.setText("Delete");
        editNvButton.setText("Edit");
    }//GEN-LAST:event_deleteNvButtonActionPerformed

    private void nameListOfElMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nameListOfElMousePressed
        p = evt.getPoint();
        if (nameListOfEl.contains((Point) p) == true){
            delIndIn = nameListOfEl.locationToIndex((Point) p); //проверяем индекс на котором произошло нажатие (можно изменить на клик)
            if (delIndIn != -1){
                deleteNvButton.setEnabled(true);
                editNvButton.setEnabled(true);
            }
            else{
                deleteNvButton.setEnabled(false);
                editNvButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_nameListOfElMousePressed
    
    private void nameOfElHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_nameOfElHierarchyChanged
    }//GEN-LAST:event_nameOfElHierarchyChanged

    private void varListOfElMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_varListOfElMousePressed
        p = evt.getPoint();
        if (varListOfEl.contains((Point) p) == true){
            delIndIn = varListOfEl.locationToIndex((Point) p); //проверяем индекс на котором произошло нажатие (можно изменить на клик)
            if (delIndIn != -1){
                deleteNvButton.setEnabled(true);
                editNvButton.setEnabled(true);
            }
            else{
                deleteNvButton.setEnabled(false);
                editNvButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_varListOfElMousePressed

    private void editNvButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editNvButtonActionPerformed
        String figName = figOnWork.getNameF();
        nameOfElDefault.setText("var_"+figName+"_");
        nameField.setText(nameListOfEl.getSelectedValue().replace("var_"+figName+"_",""));
        varField.setText(varListOfEl.getSelectedValue());
        changeNvelement.setDefaultCloseOperation(changeNvelement.DISPOSE_ON_CLOSE);
        changeNvelement.pack();
        changeNvelement.setModal(true);
        changeNvelement.setLocationRelativeTo(this);
        changeNvelement.setVisible(true);
    }//GEN-LAST:event_editNvButtonActionPerformed

    private void varFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_varFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_varFieldActionPerformed

    private void SaveNvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveNvActionPerformed
        listModelName.set(nameListOfEl.getSelectedIndex(),nameOfElDefault.getText()+nameField.getText());
        listModelNumber.set(nameListOfEl.getSelectedIndex(),varField.getText());
        nameNvEl.set(nameListOfEl.getSelectedIndex(),nameOfElDefault.getText()+nameField.getText());
        varNvEl.set(nameListOfEl.getSelectedIndex(),varField.getText());
        changeNvelement.dispose();
    }//GEN-LAST:event_SaveNvActionPerformed

    private void figuresNimberFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_figuresNimberFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_figuresNimberFieldActionPerformed

    private void OcoefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OcoefActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_OcoefActionPerformed

    private void individCodeButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_individCodeButtonItemStateChanged
        //Включили сложность свой код
        if (individCodeButton.isSelected()){
            addCodeWindowInProp();
        }
        else{
            if (mainBodyTabbedPanel.getTabCount()==3){
                mainBodyTabbedPanel.removeTabAt(2);
            }
        }
    }//GEN-LAST:event_individCodeButtonItemStateChanged

    private void expButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_expButtonActionPerformed

    private void nvComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nvComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nvComboBoxActionPerformed

    private void nvComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_nvComboBoxItemStateChanged

    }//GEN-LAST:event_nvComboBoxItemStateChanged

    private void selectNVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectNVActionPerformed

    }//GEN-LAST:event_selectNVActionPerformed

    private void selectNVStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_selectNVStateChanged
        if (selectNV.isSelected() == true){
            nvComboBox.setEnabled(true);
        } else{
            nvComboBox.setEnabled(false);
            nvComboBox.setSelectedIndex(-1);
        }
    }//GEN-LAST:event_selectNVStateChanged

    private void compareNumberFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compareNumberFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_compareNumberFieldActionPerformed

    private void signComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_signComboBoxActionPerformed

    private void llmGeneratedCodeButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_llmGeneratedCodeButtonItemStateChanged
        if (llmGeneratedCodeButton.isSelected()){
            addLlmCodeWindowInProp();
        }
        else{
            if (mainBodyTabbedPanel.getTabCount()==3){
                mainBodyTabbedPanel.removeTabAt(2);
            }
        }
    }//GEN-LAST:event_llmGeneratedCodeButtonItemStateChanged
    private void addCodeWindowInProp(){
        mainBodyTabbedPanel.addTab("Function", codeTPanel); // добавление нужных окон делаем через этот метод
    }
    private void addLlmCodeWindowInProp(){
        mainBodyTabbedPanel.addTab("LLM Prompt", llmCodePanel);
    }
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PropertiesDialog dialog = new PropertiesDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BackNv;
    private javax.swing.JPanel IfPropertiesPanel;
    private javax.swing.JFormattedTextField Ocoef;
    private javax.swing.JPanel OpropertiesPanel;
    private javax.swing.JButton SaveNv;
    private javax.swing.JFormattedTextField Slikelihood;
    private javax.swing.JFormattedTextField Speriod;
    private javax.swing.JPanel SpropertiesPanel;
    private javax.swing.JComboBox<String> Swork;
    private javax.swing.JPanel VpropertiesPanel;
    private javax.swing.JButton addNvButton;
    private javax.swing.JButton cancelPropBut;
    private javax.swing.JDialog changeNvelement;
    private javax.swing.JScrollPane codePanel;
    private javax.swing.JPanel codeTPanel;
    private javax.swing.JTextPane codeTextField;
    private javax.swing.JFormattedTextField compareNumberField;
    private javax.swing.JButton deleteNvButton;
    private javax.swing.JTextArea descriptionTextField;
    private javax.swing.JButton editNvButton;
    private javax.swing.JLabel endString;
    private javax.swing.JRadioButton expButton;
    private javax.swing.JFormattedTextField figuresNimberField;
    private javax.swing.JLabel firstStringCodeField;
    private javax.swing.ButtonGroup ifProp;
    private javax.swing.JRadioButton individCodeButton;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private java.awt.Label label6;
    private javax.swing.JLabel labelStandart;
    private javax.swing.JLabel labelStandart1;
    private javax.swing.JLabel lastStringCodeField;
    private javax.swing.JPanel llmCodePanel;
    private javax.swing.JScrollPane llmCodeScrollPanel;
    private javax.swing.JTextPane llmCodeTextField;
    private javax.swing.JRadioButton llmGeneratedCodeButton;
    private javax.swing.JRadioButton logButton;
    private javax.swing.JTabbedPane mainBodyTabbedPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField nameField;
    private java.awt.Label nameLabel;
    private java.awt.Label nameLabel1;
    private javax.swing.JList<String> nameListOfEl;
    private javax.swing.JScrollPane nameOfEl;
    private javax.swing.JLabel nameOfElDefault;
    private javax.swing.JComboBox<String> nvComboBox;
    private javax.swing.JPanel nvPropertiesPanel;
    private javax.swing.JLabel periodLabel;
    private javax.swing.JLabel probabilityLabel;
    private javax.swing.ButtonGroup properties;
    private javax.swing.JButton savePropBut;
    private javax.swing.JRadioButton selectI;
    private javax.swing.JRadioButton selectNV;
    private javax.swing.JLabel shapeName;
    private javax.swing.JComboBox<String> signComboBox;
    private javax.swing.JRadioButton stepButton;
    private javax.swing.JFormattedTextField varField;
    private javax.swing.JList<String> varListOfEl;
    private javax.swing.JScrollPane varOfEl;
    private javax.swing.JRadioButton xButton;
    private javax.swing.JRadioButton xlogButton;
    // End of variables declaration//GEN-END:variables
}
