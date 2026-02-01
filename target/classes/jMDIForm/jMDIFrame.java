package jMDIForm;

import java.awt.Toolkit;//для буфера обмена
import java.awt.datatransfer.Clipboard;//для буфера обмена
import java.awt.datatransfer.StringSelection;//для буфера обмена

import com.fasterxml.jackson.core.JsonProcessingException;
import EPM.mdi;
import logic.serialization.model.ConvertedObject;
import objects.figure.*;
import objects.line.*;
import objects.point.*;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import domain.DiagramLoadResult;
import logic.description.DescriptionService;
import logic.graph.ConnectionPolicy;
import logic.history.HistoryService;
import logic.pagerank.PageRankService;
import logic.serialization.DiagramSerializer;

public class jMDIFrame extends JInternalFrame {

//    DatabaseHandler dbHandler = new DatabaseHandler();
    public int s;
    public final int k = 100;
    public ArrayList<figures> all = new ArrayList();//массив хранящий фугуры по порядку расположения !
//    public ArrayList<points> points = new ArrayList();//массив хранящий точки по порядку расположения
    public ArrayList<points> points = new ArrayList();//массив хранящий точки по порядку расположения
    public ArrayList<Line> lines = new ArrayList();//массив линий 
    public ArrayList<Shape> pointShape = new ArrayList();//массив форм точек на 1 фигуре//обновляется для каждой отдельной фигуры
    public int x;//координаты мыши
    public int y;
    Point2D p;// текущая точка

    private final int MAX_HISTORY = 20; // Ограничение глубины истории
    private final HistoryService historyService = new HistoryService(MAX_HISTORY);
    
    int dx = 0;//смещенные координаты курсора относительно фигуры при захвате объекта
    int dy = 0;
    int zoomStep = 20;
    public static int oldX, oldY;//coordinates before moving
    int newX, newY;//новые координаты после преобразований
    int zoom = 100;// коэффициент масштаба
    Shape ss;
    boolean touch; //Флаг показывает есть или нет выбранный объект
    boolean pointed = false;//есть ли точки
    boolean checkLine = false;//для проведения соединений объектов
    boolean lined = false;//есть линии или нет
    boolean atLeftBorder = false; //флаг того что фигура уперлась в левую границу
    boolean atTopBorder = false; //флаг того что фигура уперлась в верхнюю границу
    boolean moveState = false;

    int id11, id22;// индексы расположения точек, соединенных линиями
    String ID1, ID2;
    int countp = 0;

    boolean change_idx = false; //Индикатор который показывает были или нет изменения в схеме
    boolean draw_idx = true; //Показывает можно рисовать или нет
    public boolean cleaned = false;//индикатор проведена была очистка или нет

    public String fileName = ""; // Имя файла в котором храниться схема
    public int dificult = 0; //Сложность проекта указывается в настройках
    //long thisTimeFirstClick;//начальный замер времени
    GridPanel grid;
    
    
    int id=0;
    int idS=0;
    int idV=0;
    int idO=0;
    int idNV=0;
    int idR=0;
    int idIF=0;
    
    Point2D.Double p1,p2;

    private final PageRankService pageRankService = new PageRankService();
    private final ConnectionPolicy connectionPolicy = new ConnectionPolicy();
    private final DiagramSerializer diagramSerializer = new DiagramSerializer();
    private final DescriptionService descriptionService = new DescriptionService();
            
    
    private static final int RESIZE_ZONE_SIZE = 10; 

    public jMDIFrame(String title, Boolean resizable, Boolean closable, Boolean maximizable, Boolean iconifiable, String file) {
        super(title, resizable, closable, maximizable, iconifiable);
        initComponents();
        
       

        // Добавляем GridPanel на jPanel1
        grid = new GridPanel(GridPanel.GetBaseCellSize());
        jPanel1.add(grid);
        
        
        
         // Отключаем фокус для панели
         jPanel1.setFocusable(false);
         jPanel1.setRequestFocusEnabled(false);
    
        // Включаем двойную буферизацию
        jPanel1.setDoubleBuffered(true);
        
        setResizable(true);  
        setMinimumSize(new Dimension(200, 200));
        
        
        // Добавляем обработчики мыши для изменения размера
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Преобразуем координаты мыши относительно фрейма
                Point framePoint = SwingUtilities.convertPoint(
                    e.getComponent(), 
                    e.getPoint(), 
                    jMDIFrame.this
                );
                
                // Проверяем, находится ли курсор внутри фрейма
                if (contains(framePoint)) {
                    updateResizeCursor(framePoint.x, framePoint.y);
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (getCursor().getType() == Cursor.SE_RESIZE_CURSOR) {
                    // Логика изменения размера фрейма
                    setSize(Math.max(getMinimumSize().width, e.getX()),
                           Math.max(getMinimumSize().height, e.getY()));
                }
            }
        });

    }
    
    private void updateResizeCursor(int x, int y) {
        int width = getWidth();
        int height = getHeight();
        boolean inRightBorder = (x >= width - 2*RESIZE_ZONE_SIZE) && (x < width- RESIZE_ZONE_SIZE);
        boolean inBottomBorder = (y >= height - 2*RESIZE_ZONE_SIZE) && (y < height- RESIZE_ZONE_SIZE);

        if (inRightBorder && inBottomBorder) {
            setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
        } else {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    
  
    
  
    
    

    @SuppressWarnings("unchecked")


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rcMenu = new javax.swing.JPopupMenu();
        jMenuUndo = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItemDelette = new javax.swing.JMenuItem();
        jMenuItemClear = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuLegend = new javax.swing.JMenuItem();
        jMenuItemGenDesc = new javax.swing.JMenuItem();
        canvas1 = new java.awt.Canvas();
        SaveChooser = new javax.swing.JFileChooser();
        descrShowDialog = new javax.swing.JDialog();
        jPanel3 = new javax.swing.JPanel();
        closeDescrBut = new javax.swing.JButton();
        toRCodeBut = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        textDescription = new javax.swing.JTextArea();
        copyDescrBut = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        scrollPaneR = new javax.swing.JScrollPane();
        textDescriptionRCode = new javax.swing.JTextArea();
        copyDescrButRCode = new javax.swing.JButton();
        rCodeActivatorBut = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        zminus = new javax.swing.JButton();
        jSize = new javax.swing.JTextField();
        zplus = new javax.swing.JButton();
        jInternalFrame1 = new javax.swing.JInternalFrame();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        zminus1 = new javax.swing.JButton();
        jSize1 = new javax.swing.JTextField();
        zplus1 = new javax.swing.JButton();

        jMenuUndo.setText("Undo");
        jMenuUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuUndoActionPerformed(evt);
            }
        });
        rcMenu.add(jMenuUndo);

        jMenuItem1.setText("Duplicate");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        rcMenu.add(jMenuItem1);

        jMenuItemDelette.setText("Delete");
        jMenuItemDelette.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeletteActionPerformed(evt);
            }
        });
        rcMenu.add(jMenuItemDelette);

        jMenuItemClear.setText("Clear all");
        jMenuItemClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });
        rcMenu.add(jMenuItemClear);
        rcMenu.add(jSeparator1);

        jMenuLegend.setText("Show legend");
        jMenuLegend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuLegendActionPerformed(evt);
            }
        });
        rcMenu.add(jMenuLegend);

        jMenuItemGenDesc.setText("Model's Code Generation ...");
        jMenuItemGenDesc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGenDescActionPerformed(evt);
            }
        });
        rcMenu.add(jMenuItemGenDesc);

        descrShowDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        descrShowDialog.setTitle("Model generation");
        descrShowDialog.setResizable(false);

        closeDescrBut.setText("Close");
        closeDescrBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeDescrButActionPerformed(evt);
            }
        });

        toRCodeBut.setText("R Code —>");
        toRCodeBut.setActionCommand("R Code —>");
        toRCodeBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toRCodeButActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("PseudoCode"));

        textDescription.setEditable(false);
        textDescription.setColumns(20);
        textDescription.setRows(5);
        scrollPane.setViewportView(textDescription);

        copyDescrBut.setText("Copy");
        copyDescrBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyDescrButActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(109, 109, 109)
                        .addComponent(copyDescrBut)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(copyDescrBut)
                .addGap(0, 11, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Target Code"));

        textDescriptionRCode.setEditable(false);
        textDescriptionRCode.setColumns(20);
        textDescriptionRCode.setRows(5);
        scrollPaneR.setViewportView(textDescriptionRCode);

        copyDescrButRCode.setText("Copy");
        copyDescrButRCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyDescrButRCodeActionPerformed(evt);
            }
        });

        rCodeActivatorBut.setText("Save");
        rCodeActivatorBut.setActionCommand("Save");
        rCodeActivatorBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rCodeActivatorButActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneR, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(70, 70, 70)
                .addComponent(copyDescrButRCode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(rCodeActivatorBut)
                .addGap(61, 61, 61))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(scrollPaneR, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(copyDescrButRCode)
                    .addComponent(rCodeActivatorBut))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(closeDescrBut))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(toRCodeBut, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(toRCodeBut))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeDescrBut)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        toRCodeBut.getAccessibleContext().setAccessibleName("R code —>");

        javax.swing.GroupLayout descrShowDialogLayout = new javax.swing.GroupLayout(descrShowDialog.getContentPane());
        descrShowDialog.getContentPane().setLayout(descrShowDialogLayout);
        descrShowDialogLayout.setHorizontalGroup(
            descrShowDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(descrShowDialogLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        descrShowDialogLayout.setVerticalGroup(
            descrShowDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        setMaximizable(true);
        setResizable(true);
        setAutoscrolls(true);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameActivated(evt);
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jScrollPane1MouseWheelMoved(evt);
            }
        });
        jScrollPane1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                jScrollPane1CaretPositionChanged(evt);
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setComponentPopupMenu(rcMenu);
        jPanel1.setPreferredSize(new java.awt.Dimension(600, 400));
        jPanel1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                moveobj(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jPanel1MouseMoved(evt);
            }
        });
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jPanel1MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jPanel1MouseReleased(evt);
            }
        });
        jPanel1.setLayout(new javax.swing.OverlayLayout(jPanel1));
        jScrollPane1.setViewportView(jPanel1);

        zminus.setText("-");
        zminus.setEnabled(true);
        zminus.setPreferredSize(new java.awt.Dimension(27, 23));
        zminus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zminusActionPerformed(evt);
            }
        });

        jSize.setEditable(false);
        jSize.setBackground(getBackground());
        jSize.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSize.setText("100%");
        jSize.setToolTipText("");
        jSize.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSize.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jSize.setFocusable(false);
        jSize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jSizeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jSizeMouseExited(evt);
            }
        });

        zplus.setText("+");
        zplus.setEnabled(true);
        zplus.setPreferredSize(new java.awt.Dimension(27, 23));
        zplus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zplusActionPerformed(evt);
            }
        });

        jInternalFrame1.setMaximizable(true);
        jInternalFrame1.setResizable(true);
        jInternalFrame1.setAutoscrolls(true);
        jInternalFrame1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jInternalFrame1.addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
                jInternalFrame1formInternalFrameActivated(evt);
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                jInternalFrame1formInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jScrollPane2.setAutoscrolls(true);
        jScrollPane2.setComponentPopupMenu(rcMenu);
        jScrollPane2.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jScrollPane2MouseWheelMoved(evt);
            }
        });
        jScrollPane2.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                jScrollPane2CaretPositionChanged(evt);
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
            }
        });

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setPreferredSize(new java.awt.Dimension(600, 400));
        jPanel2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jPanel2moveobj(evt);
            }
        });
        jPanel2.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jPanel2Resizing(evt);
            }
        });
        jPanel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jPanel2MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jPanel2MouseReleased(evt);
            }
        });
        jPanel2.setLayout(new javax.swing.OverlayLayout(jPanel2));
        jScrollPane2.setViewportView(jPanel2);

        zminus1.setText("-");
        zminus1.setEnabled(true);
        zminus1.setPreferredSize(new java.awt.Dimension(27, 23));
        zminus1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zminus1ActionPerformed(evt);
            }
        });

        jSize1.setEditable(false);
        jSize1.setBackground(getBackground());
        jSize1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jSize1.setText("100%");
        jSize1.setToolTipText("");
        jSize1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jSize1.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jSize1.setFocusable(false);
        jSize1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jSize1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jSize1MouseExited(evt);
            }
        });

        zplus1.setText("+");
        zplus1.setEnabled(true);
        zplus1.setPreferredSize(new java.awt.Dimension(27, 23));
        zplus1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zplus1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
        jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
        jInternalFrame1Layout.setHorizontalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jInternalFrame1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jInternalFrame1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(zminus1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSize1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zplus1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(14, 14, 14))
        );
        jInternalFrame1Layout.setVerticalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInternalFrame1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGap(4, 4, 4)
                .addGroup(jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(zplus1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zminus1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSize1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(zminus, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSize, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zplus, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addGap(14, 14, 14))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jInternalFrame1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(zplus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zminus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSize, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jInternalFrame1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Получения центра видимой области панели по координате X
    public int GetCenterX() {
        // Получаем размеры панели
        int panelWidth = jPanel1.getWidth();

        // Рассчитываем размеры видимой области панели
        int visibleWidth = jScrollPane1.getViewport().getViewRect().width;

        // Максимальное значение скроллера
        int maxScrollX = jScrollPane1.getHorizontalScrollBar().getMaximum();

        // Получаем текущее положение скроллеров
        int scrollX = jScrollPane1.getHorizontalScrollBar().getValue();

        // Рассчитываем координаты центра видимой области панели относительно текущего положения скроллеров
        int centerX = panelWidth / maxScrollX * scrollX + visibleWidth / 2 - s / 4;

        return centerX;
    }

    //Получения центра видимой области панели по координате Y
    public int GetCenterY() {
        // Получаем размеры панели
        int panelHeight = jPanel1.getHeight();

        // Рассчитываем размеры видимой области панели
        int visibleHeight = jScrollPane1.getViewport().getViewRect().height;

        // Максимальное значение скроллера
        int maxScrollY = jScrollPane1.getVerticalScrollBar().getMaximum();

        // Получаем текущее положение скроллеров
        int scrollY = jScrollPane1.getVerticalScrollBar().getValue();

        // Рассчитываем координаты центра видимой области панели относительно текущего положения скроллеров
        int centerY = panelHeight / maxScrollY * scrollY + visibleHeight / 2 - s / 4;

        return centerY;
    }

//Вставляем элемент S
    private void SActionPerformed(java.awt.event.ActionEvent evt) {
        saveState();
        s = k;
        CleanNumbers(cleaned);

        // Рассчитываем координаты центра фигуры
        int x = GetCenterX();
        int y = GetCenterY();

        // Создаем экземпляр фигуры и устанавливаем его координаты
        id++;
        idS++;
        S1 Sn = new S1(x, y, (int) (s * zoom / 100), idS, id, "S" + idS, "");

        // Добавляем фигуру на панель
        jPanel1.removeAll();
        all.add(0, Sn);
        this.drawObjects();

        // Устанавливаем флаг изменения
        change_idx = true;

        cleaned = false;
        ButtonActivated();
    }

    //Вставляем элемент V
    private void VActionPerformed(java.awt.event.ActionEvent evt) {
        saveState();
        s = k;
        CleanNumbers(cleaned);
        // Рассчитываем координаты верхнего левого угла фигуры
        int x = GetCenterX();
        int y = GetCenterY();
        
        id++;
        idV++;

        V Vn = new V(x, y, (int) (s * zoom / 100), idV, id, "V" + idV, "");
        Vn.setSize(jPanel1.getWidth(), jPanel1.getHeight());
        Vn.setVisible(true);
        Vn.setOpaque(false); // Сделаем фигуру прозрачной

        jPanel1.removeAll();
        all.add(0, Vn);
        this.drawObjects();

        change_idx = true;
        cleaned = false;
        ButtonActivated();
    }

    //Вставляем элемент R
    private void RActionPerformed(java.awt.event.ActionEvent evt) {
        saveState();
        s = k;
        CleanNumbers(cleaned);
        // Рассчитываем координаты верхнего левого угла фигуры
        int x = GetCenterX();
        int y = GetCenterY();
        
        id++;
        idR++;

        R Rn = new R(x, y, (int) (s * zoom / 100), idR, id, "R" + idR, "");
        Rn.setSize(jPanel1.getWidth(), jPanel1.getHeight());
        Rn.setVisible(true);

        jPanel1.removeAll();
        all.add(0, Rn);
        this.drawObjects();

        change_idx = true;
        cleaned = false;
        ButtonActivated();
    }

    //Вставляем элемент NV
    private void NVActionPerformed(java.awt.event.ActionEvent evt) {
        saveState();
        s = k;
        CleanNumbers(cleaned);
        int x = GetCenterX();
        int y = GetCenterY();
        
        id++;
        idNV++;

        NV NVn = new NV(x, y, (int) (s * zoom / 100), idNV, id, "NV" + idNV, "");
        NVn.setSize(jPanel1.getWidth(), jPanel1.getHeight());
        NVn.setVisible(true);

        jPanel1.removeAll();
        all.add(0, NVn);
        this.drawObjects();

        change_idx = true;
        cleaned = false;
        ButtonActivated();
    }

    //Вставляем элемент условие (IF)
    private void DActionPerformed(java.awt.event.ActionEvent evt) {
        saveState();
        s = k;
        CleanNumbers(cleaned);
        int x = GetCenterX();
        int y = GetCenterY();
        
        id++;
        idIF++;

        //d dn = new d(x, y, s);
        d dn = new d(x, y, (int) (s * zoom / 100), idIF, id, "IF" + idIF, "");
        dn.setSize(jPanel1.getWidth(), jPanel1.getHeight());
        dn.setVisible(true);

        jPanel1.removeAll();
        all.add(0, dn);
        this.drawObjects();

        change_idx = true;
        cleaned = false;
        ButtonActivated();
    }

    private void OActionPerformed(java.awt.event.ActionEvent evt) {
        saveState();
        s = k;
        CleanNumbers(cleaned);
        int x = GetCenterX();
        int y = GetCenterY();
        id++;
        idO++;

        //d dn = new d(x, y, s);
        O on = new O(x, y, (int) (s * zoom / 100), idO, id, "O" + idO, "");
        on.setSize(jPanel1.getWidth(), jPanel1.getHeight());
        on.setVisible(true);

        jPanel1.removeAll();
        all.add(0, on);
        this.drawObjects();

        change_idx = true;
        cleaned = false;
        ButtonActivated();
    }

    private void drawObjects() {//перерисовка всех объектов
        
        //jPanel1.removeAll();
        for (figures b : all) {
            jPanel1.add(b);
        }
        for (Line line : lines) {
            jPanel1.add(line);
        }
        jPanel1.add(grid); // Добавляем сетку перед добавлением фигур
        jPanel1.revalidate();
        jPanel1.repaint();
    }

    private void addPoints(int zoom) {//добавление точек на все фигуры + их отображение
        points.clear();
        for (figures ff : all) {//заполнение массива точками для каждой фигуры с соответствием их идексам
            if (ff.getRec() != null) {
                
                
                
                pointStraight pn = new pointStraight(ff.getRec());
                
                
                //ff.setXX(ff.getXX()*zoom/100);
                //ff.setYY(ff.getYY()*zoom/100);
                //ff.setAbsoluteX(oldX);
                //ff.setAbsoluteY(oldY);
              
                //pn.setSize(jPanel1.getWidth(), jPanel1.getHeight());

                //pn.setVisible(true);
                
                
                
                pn.setLocation(ff.getXX()*zoom/100,ff.getYY()*zoom/100);
                
                
            
                points.add(pn);                
                //points.add(all.indexOf(ff), pn);//по итогу полностью заполнится всеми точками на текущий момент

               
            }
        }
        
        
     
        
        //drawObjects();
    }

    private void oneShapePoints(int i) {//????????? ?????? ? ??????????? ??????? ??????
        pointShape.clear();
        if (points.isEmpty() || i < 0 || i >= points.size()) {
            pointed = false;
            return;
        }
        pointShape = points.get(i).getShape();
        pointed = true;
    }

    private void jPanel1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MousePressed
        
        oldX = evt.getX();
        oldY = evt.getY();
        p = evt.getPoint();

        id11 = 0;//индекс по точкам
        id22 = 0;
        int countf = 0;//счет нерасмотренных фигур
        countp = 0;//счет нерассмотренных точек//на них не попал курсор
        //addPoints();
        
       

        for (figures currentFigure : all) {
            ss = currentFigure.getShape();
            

            
            if (ss.contains(p) == true) {

                //если двойной клик открываем окно со свойствами фгуры
                if (evt.getClickCount() == 2) {
                    String oldName = currentFigure.getNameF();
                    PropertiesDialog pDialog = new PropertiesDialog(null, true, currentFigure,all);
                    
                    // Автоматически подгоняем размер окна под содержимое
                    pDialog.pack();
        
                    // Центрируем относительно экрана
                    pDialog.setLocationRelativeTo(null);
                    
                    pDialog.setVisible(true);
                    
                    if (pDialog.isOkPressed) {
                        change_idx = true;
                        ButtonActivated();
                    }
                    
                    //изменение названия фигуры в линиях для корректной привязки линий к фигурам
                    for (Line line: lines){
                        if (line.getID1().equals(oldName)){
                            line.setID1(currentFigure.getNameF());
                        }
                        if (line.getID2().equals(oldName)){
                            line.setID2(currentFigure.getNameF());
                        }
                    }
                    evt.consume();
                }

                //Далее код для переноса фигуры, не относ к двойному клику
                evt.consume();
                all.remove(currentFigure);
                all.add(0, currentFigure);
                touch = true; // Фигура выбрана
                //addPoints();

                //установить новые координты для x и y, прибавить к координате значение положения скроллера
                jPanel1.removeAll();
                for (JComponent c : all) {
                    jPanel1.add(c);
                }
                
                
                
                //oneShapePoints(0);
                addPoints(zoom);
                pointed = true;
                
                //if (!points.isEmpty()) {
                //    jPanel1.add(points.get(0)); // добавляем точки поверх фигуры
                    //pointed = true;
                //}
                
                
                


                
                for (Shape l : pointShape) {
                    if (l.contains(p)) {
                        //выделение всех точек и соединение
                        id11 = pointShape.indexOf(l);
                        jPanel1.removeAll();
                        for (points a : points) {
                            jPanel1.add(a);
                            if (lined) {
                                for (Line ln : lines) {
                                    jPanel1.add(ln);
                                }
                            }
                            jPanel1.add(all.get(points.indexOf(a)));
                        }
                        LineStraight ls = new LineStraight((Point2D) points.get(0).getPoint().get(id11), (Point2D) evt.getPoint(), currentFigure.getNameF(), id11, currentFigure.getNameF(), id11);
                        ls.setSize(jPanel1.getWidth(), jPanel1.getHeight());
                        ls.setVisible(true);
                        jPanel1.add(ls);
                        lines.add(0, ls);
                        checkLine = true;
                        //jPanel1.add(grid);
                        //jPanel1.revalidate();
                        //jPanel1.repaint();
                    } else {
                        countp++;
                        
                    }
                }
                touch = true;

                //}
                // Нажате правой кнопки
                if (evt.isPopupTrigger()) {
                    // create a popup menu
                    rcMenu.show(this, oldX, oldY);
                }

                jPanel1.add(grid); // Добавляем сетку перед добавлением фигур
                jPanel1.revalidate();
                jPanel1.repaint();
                //updateSch();

                ButtonActivated();

                break;
            } else {//не попадает на фигуру
                countf++;
             

            }
        }

        if ((countp == pointShape.size())) { //Не попали в точку соединения
            jPanel1.removeAll();

            if (lined) {
                for (Line ln : lines) {
                    jPanel1.add(ln);
                }
            }

            if (countf != all.size()) {
                jPanel1.add(points.get(0));
                pointed = true;
            } else {
                pointed = false;
            }

            for (JComponent c : all) {
                jPanel1.add(c);
            }

            jPanel1.add(grid);
            jPanel1.revalidate();
            jPanel1.repaint();

        }

        countp = 0;

        
        //при нажатии не попали в фигуру
        if (countf == all.size()) {
            oneShapePoints(0); //Удаляем соединительные точки у фигуры
            pointed = false;
            touch = false;
            jPanel1.removeAll();
            updateSch(); //Обновляем схему
        }
        
        

        //touch = ss.contains(p) == true;


    }//GEN-LAST:event_jPanel1MousePressed

    private void chcord(figures b) {
        b.setXX(newX);
        b.setYY(newY);
    }

    //Перемещение мышки с нажатой кнопкой
    private void moveobj(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveobj

        if (all.isEmpty() || points.isEmpty()) {
            return;
        }
        figures b = all.get(0);
        ss = b.getShape();

                        // Если фигура уже на границе, не меняем скроллеры
        atLeftBorder = (b.getXX() <= 20*zoom/100);// && newX == (int) 20*zoom/100);
        atTopBorder = (b.getYY() <= 20*zoom/100);// && newY == (int) 20*zoom/100);        

        //if (pointed == true && checkLine == false) {
        //    // Убираем точки текущего объекта при перетаскивании
            jPanel1.remove(points.get(0));
        //    jPanel1.add(new GridPanel((int) (GridPanel.GetBaseCellSize() * zoom / 100))); // Добавляем сетку перед добавлением фигур
        //    jPanel1.revalidate();
        //    jPanel1.repaint();
        //    pointed = false;
        //}

        if (touch == true && checkLine == false) {
            
            //Сохраняем в буфере положение с которого началось перетаскивание
            if (!moveState) { 
                saveState(); 
                moveState = true;
            }
            
            //if (pointed) {
            //    jPanel1.remove(points.get(0));
            //}

            
            // Логика перетаскивания фигуры
            this.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            
            
       
            
            
            
            dx = -oldX + b.getXX();
            dy = -oldY + b.getYY();
            
            
            
             jPanel1.setComponentZOrder(b, 0);
            

            newX = Math.max((int) 20*zoom/100, evt.getX() + dx); // Ограничение по левой границе
            newY = Math.max((int) 20*zoom/100, evt.getY() + dy); // Ограничение по верхней границе
            

            

            // Обновляем координаты фигуры
            b.setXX(newX);
            b.setYY(newY);
            b.setAbsoluteX((int) (b.getXX() / (double) zoom * 100));
            b.setAbsoluteY((int) (b.getYY() / (double) zoom * 100));
            
            addPoints(zoom); // Создаем новые точки
            jPanel1.add(points.get(0)); // Добавляем новые точки
            pointed = true;
            
            
            
            // Логика перетаскивания линии, если связана
            if (lined){// && !atLeftBorder && !atTopBorder) {
                for (Line ln : lines) {
                    // for (figures f : all) {
                    
                   
                    
                    
                    
                    
                    double dxx, dyy;
                    double pn1, pn2;
                    if (b.getNameF().equals(ln.getID1())) {
                        
                                               
                       dxx = ln.getC1().getX() - oldX;
                       dyy = ln.getC1().getY() - oldY;
                       
                       pn1 = dxx + evt.getX();

                       if ((ln.getC1().getX() <= b.getRec().getX()+1) && (ln.getC1().getX() >= b.getRec().getX()-1) ) {pn1 = Math.max(20*zoom/100, pn1);       }
                       if ((ln.getC1().getX() <= b.getRec().getWidth() + b.getRec().getX()+1) && (ln.getC1().getX() >= b.getRec().getWidth() + b.getRec().getX()-1))  { pn1 = Math.max(b.getRec().getWidth()+20*zoom/100, pn1);   }
                       if ((ln.getC1().getX() <= (b.getRec().getWidth()/2) + b.getRec().getX()+1) && (ln.getC1().getX() >= (b.getRec().getWidth()/2) + b.getRec().getX()-1)) { pn1 = Math.max((b.getRec().getWidth()/2)+20*zoom/100, pn1); }

                       pn2 = dyy + evt.getY();
                       if ((ln.getC1().getY() <= b.getRec().getY()+1) && (ln.getC1().getY() >= b.getRec().getY()-1)) { pn2 = Math.max(20*zoom/100, pn2);       }
                       if ((ln.getC1().getY() <= b.getRec().getHeight() + b.getRec().getY()+1) && (ln.getC1().getY() >= b.getRec().getHeight() + b.getRec().getY()-1))  { pn2 = Math.max(b.getRec().getHeight()+20*zoom/100, pn2);   }
                       if ((ln.getC1().getY() <= b.getRec().getHeight()/2 + b.getRec().getY()+1) && (ln.getC1().getY() >= b.getRec().getHeight()/2 + b.getRec().getY()-1)) { pn2 = Math.max(b.getRec().getHeight()/2+20*zoom/100, pn2); }
                        
                        p1 = new Point2D.Double(pn1,pn2);
                        
                        ln.setC1(p1);
                        ln.arrow.x1 = p1.x;
                        ln.arrow.y1 = p1.y;
                        ln.arrow.repaint();

                    }
                    
                    if (b.getNameF().equals(ln.getID2())) {
                        dxx = ln.getC2().getX() - oldX;
                        dyy = ln.getC2().getY() - oldY;
             
                        
                       pn1 = dxx + evt.getX();
                       if ((ln.getC2().getX() <= b.getRec().getX()+1) && (ln.getC2().getX() >= b.getRec().getX()-1)) { pn1 = Math.max(20*zoom/100, pn1);       }
                       if ((ln.getC2().getX() <= b.getRec().getWidth() + b.getRec().getX()+1) && (ln.getC2().getX() >= b.getRec().getWidth() + b.getRec().getX()-1))  { pn1 = Math.max(b.getRec().getWidth()+20*zoom/100, pn1);   }
                       if ((ln.getC2().getX() <= b.getRec().getWidth()/2 + b.getRec().getX()+1) && (ln.getC2().getX() >= b.getRec().getWidth()/2 + b.getRec().getX()-1)) { pn1 = Math.max(b.getRec().getWidth()/2+20*zoom/100, pn1); }

                       pn2 = dyy + evt.getY();
                       if ((ln.getC2().getY() <= b.getRec().getY()+1) && (ln.getC2().getY() >= b.getRec().getY()-1)) { pn2 = Math.max( 20*zoom/100, pn2);       }
                       if ((ln.getC2().getY() <= b.getRec().getHeight() + b.getRec().getY()+1) && (ln.getC2().getY() >= b.getRec().getHeight() + b.getRec().getY()-1))  { pn2 = Math.max(b.getRec().getHeight()+20*zoom/100, pn2);   }
                       if ((ln.getC2().getY() <= b.getRec().getHeight()/2 + b.getRec().getY()+1) && (ln.getC2().getY() >= b.getRec().getHeight()/2 + b.getRec().getY()-1)) { pn2 = Math.max(b.getRec().getHeight()/2+20*zoom/100, pn2); }
                        
                        p2 = new Point2D.Double(pn1,pn2);                        
                        
                        
                        //Point2D.Double p2 = new Point2D.Double(dxx + evt.getX(), dyy + evt.getY());
                        ln.setC2(p2);
                        ln.arrow.x2 = p2.x;
                        ln.arrow.y2 = p2.y;
                        ln.arrow.repaint();
                    }
                    
                    
                    
                }
            }
            
            
            
           
            
            //jPanel1.revalidate();
            //jPanel1.repaint();
            
            if (!atLeftBorder ) { 
                
                oldX = evt.getX(); 
                
            } //else {evt.translatePoint(oldX, 0);}
            if (!atTopBorder) { 
                
                oldY = evt.getY(); 
                
            }      //else {evt.translatePoint(0, oldY);}      
            //oldX = evt.getX();
            //oldY = evt.getY();
            
           
            
            oneShapePoints(0);
            ButtonActivated();
            //updateSch();
            
                checkPanelBounds();
            
        }
        //Проверка, если ведем стрелку
        //Перерисовка стрелки
        if (checkLine == true) {
            Line l = lines.get(0);

            // 1. Проверяем попадание в площадку для соединения целевой фигуры и если попадаем то меняем курсор на крестик
            boolean targetPoint = false; //Флаг для проверки попадания на точки фигуры
            for (points ps : points) {
                ArrayList<Shape> pointShape1 = new ArrayList();
                pointShape1 = points.get(points.indexOf(ps)).getShape();
                
                for (Shape l1 : pointShape1) {
                    if (l1.contains(evt.getPoint())) {
                        this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                        targetPoint = true;
                    } else {
                        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    }
                    if (targetPoint) {
                        break;
                    }
                }
                if (targetPoint) {
                    break;
                }
            }
            // 1. Конец

            //this.setCursor(new Cursor(Cursor.HAND_CURSOR));
            l.setC2((Point2D) evt.getPoint());//обновление второй точки
            l.arrow.x2 = l.getC2().getX();
            l.arrow.y2 = l.getC2().getY();
            l.arrow.repaint();
            jPanel1.add(grid);
            jPanel1.revalidate();
            jPanel1.repaint();
        }

        change_idx = true;
        ButtonActivated();
        jPanel1.revalidate(); // Обновляем компоновку
        jPanel1.repaint();    // Перерисовываем панель

    }//GEN-LAST:event_moveobj

    public ConvertedObject CreatorConvertObject() {
        return diagramSerializer.createConvertedObject(all, lines, zoom, idS, idNV, idV, idR, idO, idIF);
    }

    //Сохранение файла
    public void SaveInJSON(String fn) {
        try {
            //создание объекта со всеми фигурами и связями
            ConvertedObject co = CreatorConvertObject();
            diagramSerializer.saveToJson(fn, co);
            
            JOptionPane.showMessageDialog(this, "The file has been successfully saved!", "Message", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (JsonProcessingException ex) {
            Logger.getLogger(jMDIFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error saving the file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (HeadlessException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving the file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    // *** Загружаем модель из файла JSON ***
    public void LoadFromJSON(String saveName) {
        try {
            DiagramLoadResult result = diagramSerializer.loadFromJson(saveName);

            zoom = result.getZoom();
            idS = result.getIdS();
            idNV = result.getIdNV();
            idV = result.getIdV();
            idR = result.getIdR();
            idO = result.getIdO();
            idIF = result.getIdIF();

            jSize.setText(String.format("%d", zoom) + '%');

            all.clear();
            lines.clear();

            all.addAll(result.getFigures());
            lines.addAll(result.getLines());

            lined = !lines.isEmpty();
            fileName = saveName;

            grid.SetCellSize((int) (GridPanel.GetBaseCellSize() * zoom / 100));

            this.requestFocusInWindow();
            updatePanelSize();
            updateSch();

            historyService.clear();
            saveState();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    
    // Метод для обновления размеров панели с учетом масштаба
    private void updatePanelSize() {
        // Находим максимальные координаты фигур
        int maxX = 0;
        int maxY = 0;
        for (figures fig : all) {
            maxX = (int) Math.max(maxX, fig.getXX() + fig.getS());
            maxY = (int) Math.max(maxY, fig.getYY() + fig.getS());
        }
    
        // Добавляем отступы
        int padding = 100;
        int newWidth = Math.max(maxX + padding, 800);//jPanel1.getWidth());
        int newHeight = Math.max(maxY + padding, 600);//jPanel1.getHeight());
    
        // Устанавливаем новые размеры
        jPanel1.setPreferredSize(new Dimension(newWidth, newHeight));
        //jPanel1.revalidate();
        
        //// Обновляем скроллбары
        //int currentHValue = jScrollPane1.getHorizontalScrollBar().getValue();
        //int currentVValue = jScrollPane1.getVerticalScrollBar().getValue();
        
        //// Восстанавливаем позицию скролла с учетом нового масштаба
        //jScrollPane1.getHorizontalScrollBar().setValue((int)(currentHValue * (zoom / (zoom + 20.0))));
        //jScrollPane1.getVerticalScrollBar().setValue((int)(currentVValue * (zoom / (zoom + 20.0))));
        
        
        
    }
    
    private void formInternalFrameActivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameActivated
        // TODO add your handling code here:
        ButtonActivated();
    }

    //Рисование сетки / миллимитровки
    private void paintGrid(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Определяем размер ячейки сетки
        int cellSize = grid.GetCellSize();

        // Определяем размеры панели
        int panelWidth = jPanel1.getWidth();
        int panelHeight = jPanel1.getHeight();

        // Определяем начальные координаты для рисования сетки
        int startX = 0;
        int startY = 0;

        // Отрисовываем вертикальные линии сетки
        for (int x = startX; x <= panelWidth; x += cellSize) {
            g2d.drawLine(x, 0, x, panelHeight);
        }

        // Отрисовываем горизонтальные линии сетки
        for (int y = startY; y <= panelHeight; y += cellSize) {
            g2d.drawLine(0, y, panelWidth, y);
        }
    }//GEN-LAST:event_formInternalFrameActivated

    private void formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosing
        if (change_idx) {
            Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            
            int dialogResult = JOptionPane.showConfirmDialog(this, "Project wasn't saved. Would you like to save your project?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon(image));
            if (dialogResult == JOptionPane.YES_OPTION) {
                if (!("".equals(fileName))) {
                    SaveInJSON(fileName);
                } else {
                    JFileChooser SaveChooser = new javax.swing.JFileChooser();
                    SaveChooser.setDialogTitle("Specify a file to save");
                    SaveChooser.setCurrentDirectory(new File("."));
                    SaveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    SaveChooser.setFileFilter(new FileNameExtensionFilter("Event-driven Process Methodology", "epm"));
                    SaveChooser.approveSelection();
                    int option = SaveChooser.showSaveDialog(this);

                    Field field2 = null;

                    if (option != JFileChooser.CANCEL_OPTION) {

                        File file1 = SaveChooser.getSelectedFile();
                        String file = null;

                        try {
                            file = file1.getCanonicalPath();
                        } catch (IOException ex) {
                            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        fileName = file;
                        SaveInJSON(fileName);
                    } else {
                         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Блокируем закрытие
                        return;         // Прерываем выполнение метода                   
                    }

                }
            } else if (dialogResult == JOptionPane.CANCEL_OPTION) {
                        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Блокируем закрытие
                        return;         // Прерываем выполнение метода
                    }
            }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        change_idx = false;
        draw_idx = false;
        ButtonActivated();
    }//GEN-LAST:event_formInternalFrameClosing

    //Отпускание нажатой кнопки мыши
    private void jPanel1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseReleased

        // Когда закончили перетаскивать объект и отпкстили мышку снова делаем курсор-стрелочку (по умолчанию)
        if (touch == true) { //&& checkLine == false) {
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            touch = true; 
            pointed = true;
            moveState = false;
        }
        

        countp = 0;
        if (checkLine) {//при нажатии уже начали отрисовку линий
            for (points ps : points) {
                oneShapePoints(points.indexOf(ps));
                for (Shape l : pointShape) {
                    if (l.contains(evt.getPoint())) { //курсор находится на точке соединения 
//                        id22 = pointShape.indexOf(l);
//                        id2 = all.get(points.indexOf(ps)).getId();
                        //jPanel1.removeAll();
                        lines.get(0).setC2((Point2D) ps.getPoint().get(pointShape.indexOf(l)));
                        lines.get(0).setID2(all.get(points.indexOf(ps)).getNameF());
                        figures first = null, second = null;
//                        Class first = null, second = null;
                        for (figures currentFigure : all) {
                            if (currentFigure.getNameF().equals(lines.get(0).getID1())) {
                                first = currentFigure; //.getClass();
                            }
                            if (currentFigure.getNameF().equals(lines.get(0).getID2())) {
                                second = currentFigure; //.getClass();
                            }
                        }
                        if (connectionPolicy.canConnect(first, second, lines, all)) {
                            if (!lined) {
                                jPanel1.add(lines.get(0));
                            }
                            this.drawObjects();
                            lined = true;
                            checkLine = false;
                            //break;
                        } else {
                            lines.remove(0); //Если соединение не из обобренного списка то линию не создаем (бахаем заготовку)
                        }

                    }
                    if (!checkLine) {
                        break; //Если сделали соединение то заканчиваем поиск точек для соединения                  
                    }
                }
                if (!checkLine) {
                    break; //Если сделали соединение то заканчиваем поиск точек для соединения          
                }
            }
            if (lines.size()!=0){
                if (lines.get(0).getID1().equals(lines.get(0).getID2())) {
                    lines.remove(0); //Если не попали в конечный элемент то линию не создаем, а бахаем заготовку
                }
            }
            //Перерисовываем
            jPanel1.removeAll();
            if (lined) {
                for (Line ln : lines) {
                    jPanel1.add(ln);
                }
            }
            for (JComponent c : all) {
                jPanel1.add(c);
            }
            jPanel1.add(grid);
            //jPanel1.revalidate();
            //jPanel1.repaint();
            checkLine = false; //устанавливаем флаг окончания рисования
            countp = 0;
        }
        
        
        updateSch();
        //checkPanelBounds();
        //jPanel1.revalidate();
        //jPanel1.repaint();

    }//GEN-LAST:event_jPanel1MouseReleased

    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        // TODO add your handling code here:
        // очищаем все
        //mdi.jMenuIt
        //emClear.clearActionPerformed(evt);
        //jPanel1.clear

        //нужно сделать вызов из jPanel чтобы не переписывать несколько раз
        //jPanel1.removeAll();
        //jPanel1.repaint();
        //all.clear();
        //Удаление всего, отрисовка сетки
        jPanel1.removeAll();
        jPanel1.add(grid);
        jPanel1.repaint();
        all.clear();
        lines.clear();

        //zoom = 100;
        //ButtonActivated();
//        
        // jPanel1.removeAll();
        // all.clear();
        // lines.clear();
        points.clear();
        pointShape.clear();

        cleaned = true;
        
        change_idx = true;
        ButtonActivated();

        // grid.SetCellSize((int) ((GridPanel.GetBaseCellSize() * zoom) / 100));
        // int HorizontalScrollBarScale = (int) (jScrollPane1.getHorizontalScrollBar().getMaximum() * zoom / 100);
        // int VerticalScrollBarScale = (int) (jScrollPane1.getVerticalScrollBar().getMaximum() * zoom / 100);
        // jScrollPane1.getHorizontalScrollBar().setMaximum(HorizontalScrollBarScale);
        // jScrollPane1.getVerticalScrollBar().setMaximum(VerticalScrollBarScale);
        // jSize.setText(String.format("%d", zoom) + '%');
        // jPanel1.add(grid);
        // jPanel1.revalidate();
        // jPanel1.repaint();
        // 

    }//GEN-LAST:event_clearActionPerformed

    private void CleanNumbers(boolean c) {
        s = k;
        if (c == true) {
            S1 Sn = new S1(x, y, (int) (s * zoom / 100), idS, id, "S" + idS, "");
            Sn.idChange();
            Sn = null;

        }
    }

    private void jScrollPane1MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jScrollPane1MouseWheelMoved

    }//GEN-LAST:event_jScrollPane1MouseWheelMoved

    private void jScrollPane1CaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jScrollPane1CaretPositionChanged
        // TODO add your handling code here:

    }//GEN-LAST:event_jScrollPane1CaretPositionChanged

    
    //обновление рисунка схемы
    private void updateSch() {
        jPanel1.removeAll(); // Очищаем панель

        // Добавляем линии
        for (Line ln : lines) {
            jPanel1.add(ln);
        }

                
        // Добавляем точки
        if (!points.isEmpty() && pointed) {
            jPanel1.add(points.get(0));
        }


        
        // Добавляем фигуры
        for (JComponent c : all) {
            jPanel1.add(c);
        }
        
                
        // Добавляем сетку
        jPanel1.add(grid);

        jPanel1.revalidate(); // Обновляем компоновку
        
 
        jPanel1.repaint();    // Перерисовываем панель
        
   
        
    }
    
    
    
    //При изменении масштаба учитывать, что должны меняться и координаты линии
    private void zminusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zminusActionPerformed
        saveState();
        if (zoom > 30) {
            zoom -= zoomStep;
        } else {
            return;
        }







    for (figures b : all) {
        b.setS((int) (k * zoom / 100));
        b.setXX((int) (b.getAbsoluteX() * zoom / 100));
        b.setYY((int) (b.getAbsoluteY() * zoom / 100));
        //Rectangle2D rec = new Rectangle2D.Double(b.getAbsoluteX()* zoom / 100, b.getAbsoluteY()* zoom / 100, b.getAbsoluteX()* zoom / 100, b.getAbsoluteY()* zoom / 100);
        //b.setRec(rec);
    }
    
    

    
    //oldX = oldX* zoom / 100;
    //oldY = oldY* zoom / 100;
    
    
    // Добавляем новые точки
        //addPoints(zoom);
            
        //pointed = false;
    // Удаляем точки (если есть)
    //if (!points.isEmpty()) {
    //    jPanel1.remove(points.get(0));
    //}
        

    //oneShapePoints(0); 
    //if (!points.isEmpty()) {
    //    jPanel1.add(points.get(0));
    //}

    // Обновляем линии
    for (Line currentLine : lines) {
        Point2D p1 = new Point2D.Double(
            currentLine.getC1().getX() * zoom / (zoom + zoomStep),
            currentLine.getC1().getY() * zoom / (zoom + zoomStep)
        );
        Point2D p2 = new Point2D.Double(
            currentLine.getC2().getX() * zoom / (zoom + zoomStep),
            currentLine.getC2().getY() * zoom / (zoom + zoomStep)
        );
        currentLine.setC1(p1);
        currentLine.setC2(p2);
        currentLine.arrow.x1 = p1.getX();
        currentLine.arrow.y1 = p1.getY();
        currentLine.arrow.x2 = p2.getX();
        currentLine.arrow.y2 = p2.getY();
    }
    
    
    

    // Обновляем сетку
    grid.SetCellSize((int) ((GridPanel.GetBaseCellSize() * zoom) / 100));

    // Обновляем скроллбары
    int currentHValue = jScrollPane1.getHorizontalScrollBar().getValue();
    int currentVValue = jScrollPane1.getVerticalScrollBar().getValue();
    
    // Рассчитываем новые максимальные значения с учетом текущего масштаба
    int maxWidth = (int) (jPanel1.getPreferredSize().width * zoom / 100);
    int maxHeight = (int) (jPanel1.getPreferredSize().height * zoom / 100);
    
    // Устанавливаем новые размеры панели
    jPanel1.setPreferredSize(new Dimension(maxWidth, maxHeight));
    
    // Устанавливаем новые максимальные значения для скроллбаров
    jScrollPane1.getHorizontalScrollBar().setMaximum(maxWidth);
    jScrollPane1.getVerticalScrollBar().setMaximum(maxHeight);
    
    // Восстанавливаем позицию скролла с учетом нового масштаба
    jScrollPane1.getHorizontalScrollBar().setValue((int)(currentHValue * (zoom / (zoom + 20.0))));
    jScrollPane1.getVerticalScrollBar().setValue((int)(currentVValue * (zoom / (zoom + 20.0))));

    // Обновляем отображение масштаба
    jSize.setText(String.format("%d", zoom) + '%');
    


    // Перерисовываем
    //jPanel1.revalidate();
    //jPanel1.repaint();
    updateSch();
    jScrollPane1.revalidate();

    change_idx = true;
    ButtonActivated();

    }//GEN-LAST:event_zminusActionPerformed


    private void zplusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zplusActionPerformed
        
        saveState();
        if (zoom < 180) {
            zoom +=  zoomStep;
        } else {
            return;
        }
     


    // Удаляем точки (если есть)
    //if (!points.isEmpty()) {
    //    jPanel1.remove(points.get(0));
    //}
    


    for (figures b : all) {
        b.setS((int) (k * zoom / 100));
        b.setXX((int) (b.getAbsoluteX() * zoom / 100));
        b.setYY((int) (b.getAbsoluteY() * zoom / 100));
    }
    
            // Добавляем новые точки
    //addPoints(zoom);
    //pointed = false;
        // Удаляем точки (если есть)
    //if (!points.isEmpty()) {
    //    jPanel1.remove(points.get(0));
    //}
    

    //oneShapePoints(0); 
    //if (!points.isEmpty()) {
    //    jPanel1.add(points.get(0));
    //}

    // Обновляем линии
    for (Line currentLine : lines) {
        Point2D p1 = new Point2D.Double(
            currentLine.getC1().getX() * zoom / (zoom - zoomStep),
            currentLine.getC1().getY() * zoom / (zoom - zoomStep)
        );
        Point2D p2 = new Point2D.Double(
            currentLine.getC2().getX() * zoom / (zoom - zoomStep),
            currentLine.getC2().getY() * zoom / (zoom - zoomStep)
        );
        currentLine.setC1(p1);
        currentLine.setC2(p2);
        
        currentLine.arrow.x1 = p1.getX();
        currentLine.arrow.y1 = p1.getY();
        currentLine.arrow.x2 = p2.getX();
        currentLine.arrow.y2 = p2.getY();
    }

    // Обновляем сетку
    grid.SetCellSize((int) ((GridPanel.GetBaseCellSize() * zoom) / 100));

    // Обновляем скроллбары
    int currentHValue = jScrollPane1.getHorizontalScrollBar().getValue();
    int currentVValue = jScrollPane1.getVerticalScrollBar().getValue();
    
    // Рассчитываем новые максимальные значения с учетом текущего масштаба
    int maxWidth = (int) (jPanel1.getPreferredSize().width * zoom / 100);
    int maxHeight = (int) (jPanel1.getPreferredSize().height * zoom / 100);
    
    // Устанавливаем новые размеры панели
    jPanel1.setPreferredSize(new Dimension(maxWidth, maxHeight));
    
    // Устанавливаем новые максимальные значения для скроллбаров
    jScrollPane1.getHorizontalScrollBar().setMaximum(maxWidth);
    jScrollPane1.getVerticalScrollBar().setMaximum(maxHeight);
    
    // Восстанавливаем позицию скролла с учетом нового масштаба
    jScrollPane1.getHorizontalScrollBar().setValue((int)(currentHValue * (zoom / (zoom - 20.0))));
    jScrollPane1.getVerticalScrollBar().setValue((int)(currentVValue * (zoom / (zoom - 20.0))));

    // Обновляем отображение масштаба
    jSize.setText(String.format("%d", zoom) + '%');
    


    // Перерисовываем
    //jPanel1.revalidate();
    //jPanel1.repaint();

    updateSch();
    jScrollPane1.revalidate();

    change_idx = true;
    ButtonActivated();
    
    
    }//GEN-LAST:event_zplusActionPerformed

    static BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    // Create a new blank cursor.
    Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            cursorImg, new Point(0, 0), "blank cursor");
    private void jSizeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSizeMouseEntered
        setCursor(blankCursor);
    }//GEN-LAST:event_jSizeMouseEntered

    private void jSizeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSizeMouseExited
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_jSizeMouseExited

    private void DeletteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeletteActionPerformed
//        удаление выделеного объекта и связей с ним              
       ArrayList<Integer> array = new ArrayList();
        for (Line ln : lines) {
            if ((ln.getID1() == all.get(0).getNameF()) || (ln.getID2() == all.get(0).getNameF())) {
                array.add(lines.indexOf(ln));
            }
        }
        
        Collections.reverse(array);
        for (int a:array){
            lines.remove(a);
        }
        all.remove(0);
        array.clear();
        // Перерисовываем панель
        jPanel1.removeAll();
        this.drawObjects();
    }//GEN-LAST:event_DeletteActionPerformed

    private void jMenuItemDeletteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeletteActionPerformed
//        удаление выделеного объекта и связей с ним              
        saveState();
        ArrayList<Integer> array = new ArrayList();
        for (Line ln : lines) {
            if ((ln.getID1().equals(all.get(0).getNameF())) || (ln.getID2().equals(all.get(0).getNameF()))) {
                array.add(lines.indexOf(ln));
            }
        }
        
        Collections.reverse(array);
        for (int a:array){
            lines.remove(a);
        }
        all.remove(0);
        array.clear();
        // Перерисовываем панель
        jPanel1.removeAll();
        this.drawObjects();
        
        change_idx = true;
        ButtonActivated();
        

    }//GEN-LAST:event_jMenuItemDeletteActionPerformed

    private void jPanel2moveobj(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2moveobj
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel2moveobj

    private void jPanel2Resizing(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jPanel2Resizing
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel2Resizing

    private void jPanel2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2MousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel2MousePressed

    private void jPanel2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2MouseReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel2MouseReleased

    private void jScrollPane2MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jScrollPane2MouseWheelMoved
        // TODO add your handling code here:
    }//GEN-LAST:event_jScrollPane2MouseWheelMoved

    private void jScrollPane2CaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jScrollPane2CaretPositionChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jScrollPane2CaretPositionChanged

    private void zminus1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zminus1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_zminus1ActionPerformed

    private void jSize1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSize1MouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_jSize1MouseEntered

    private void jSize1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSize1MouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_jSize1MouseExited

    private void zplus1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zplus1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_zplus1ActionPerformed

    private void jInternalFrame1formInternalFrameActivated(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_jInternalFrame1formInternalFrameActivated
        // TODO add your handling code here:
    }//GEN-LAST:event_jInternalFrame1formInternalFrameActivated

    private void jInternalFrame1formInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_jInternalFrame1formInternalFrameClosing
        // TODO add your handling code here:
    }//GEN-LAST:event_jInternalFrame1formInternalFrameClosing

    private void jMenuItemGenDescActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGenDescActionPerformed
        GenerateDescription();
    }//GEN-LAST:event_jMenuItemGenDescActionPerformed

    private void jPanel1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseMoved

        
              


        // 1. --- Если при выделении объекта попадаем в площадку для соединения то меняем курсор на крестик ---
        for (Shape l : pointShape) {
            if (l.contains(evt.getPoint())) {
                this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                break;
            } else {
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
        // 1. --- конец ---
        
    }//GEN-LAST:event_jPanel1MouseMoved

    public static void copyToClipboard(String text) { //сохранение в буфер обмена
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    //Дублирование элемента по правой кнопке
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed

       saveState();
       // Защита от пустого списка
        if (all.isEmpty() || (touch == false)) {
            JOptionPane.showMessageDialog(this, "Nothing to duplicate", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        change_idx = true;
        ButtonActivated(); 
        
        figures original = all.get(0);

        if (original instanceof S1) {
            SActionPerformed(evt);
        } else if (original instanceof V) {
            VActionPerformed(evt);
        } else if (original instanceof R) {
            RActionPerformed(evt);
        } else if (original instanceof NV) {
            NVActionPerformed(evt);
        } else if (original instanceof d) {
            DActionPerformed(evt);
        } else if (original instanceof O) {
            OActionPerformed(evt);
        }
        throw new IllegalArgumentException("Unrecognized block type: " + original.getClass().getSimpleName());
        

    

    }//GEN-LAST:event_jMenuItem1ActionPerformed

    // Метод для отображения информации о фигурах
    private void jMenuLegendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuLegendActionPerformed
        // Создаем диалоговое окно
        JDialog infoDialog = new JDialog();
        infoDialog.setTitle("Legend");
        infoDialog.setSize(500, 400);
        infoDialog.setLayout(new BorderLayout());
    
        // Создаем модель таблицы
        String[] columnNames = {"Name", "Description"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
    
        // Заполняем таблицу данными
        for (figures fig : all) {
            String name = fig.getNameF();
            String description = fig.getDescriptionF();
        
            model.addRow(new Object[]{name, description});
        }
    
        // Создаем таблицу
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
    
        // Добавляем таблицу в скролл-панель
        JScrollPane scrollPane1 = new JScrollPane(table);
        infoDialog.add(scrollPane1, BorderLayout.CENTER);
    
        // Кнопка закрытия
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> infoDialog.dispose());
    
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        infoDialog.add(buttonPanel, BorderLayout.SOUTH);
    
        // Позиционируем окно относительно главного окна
        infoDialog.setLocationRelativeTo(this);
        infoDialog.setVisible(true);       
    }//GEN-LAST:event_jMenuLegendActionPerformed

    private void jMenuUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuUndoActionPerformed
       undo(); 
    }//GEN-LAST:event_jMenuUndoActionPerformed

    private void copyDescrButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyDescrButActionPerformed
        copyToClipboard(textDescription.getText());
    }//GEN-LAST:event_copyDescrButActionPerformed

    private void toRCodeButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toRCodeButActionPerformed
        //?????????????????? R ????????
        String rCode = descriptionService.generateRCode(all, textDescription.getText());
        textDescriptionRCode.setText(rCode); //?-?????????<???????? R ??????

        rCodeActivatorBut.setEnabled(true); //?????'?????????????? ???????????? ?????:??????????????
        copyDescrButRCode.setEnabled(true);
    }//GEN-LAST:event_toRCodeButActionPerformed//GEN-LAST:event_toRCodeButActionPerformed

    private void copyDescrButRCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyDescrButRCodeActionPerformed
        copyToClipboard(textDescriptionRCode.getText());
    }//GEN-LAST:event_copyDescrButRCodeActionPerformed

    private void rCodeActivatorButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rCodeActivatorButActionPerformed
        SaveInRFile();
    }//GEN-LAST:event_rCodeActivatorButActionPerformed

    private void closeDescrButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeDescrButActionPerformed
        descrShowDialog.dispose();
    }//GEN-LAST:event_closeDescrButActionPerformed

    

    
    
    
    private void GenerateDescription() {
        //?"???????????????? ??????????????????????
        String selfMaidCode = descriptionService.generateDescription(CreatorConvertObject());
        textDescription.setText(selfMaidCode); //???????????<???????? ??????????????????
        textDescriptionRCode.setText(""); //???+?????>?????? ?????? R
        rCodeActivatorBut.setEnabled(false); //?????????'?????????????? ???????????? ?????:??????????????
        copyDescrButRCode.setEnabled(false);
        
        descrShowDialog.setDefaultCloseOperation(descrShowDialog.DISPOSE_ON_CLOSE);
        descrShowDialog.pack();
        descrShowDialog.setModal(true);
        descrShowDialog.setLocationRelativeTo(this);
        // ?????'???????'???????????? ?????????????????? ???????????? ???????? ?????? ??????????????????
        descrShowDialog.pack();
        
        // ???????'???????????? ???'?????????'???>?????? ????????????
        descrShowDialog.setLocationRelativeTo(null);
        descrShowDialog.setVisible(true);       
    }
    public void SaveInRFile() {     
        SaveChooser.setDialogTitle("Saving R File");// ("+fn+")");
        SaveChooser.setCurrentDirectory(new File("."));
        SaveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        SaveChooser.setFileFilter(new FileNameExtensionFilter("Event-driven Process Methodology", "R"));
        SaveChooser.approveSelection();
        int option = SaveChooser.showSaveDialog(this);
        if(option != JFileChooser.CANCEL_OPTION) {
            File file1 = SaveChooser.getSelectedFile();
            String file = null;
            try {
                file = file1.getCanonicalPath()+".R";
            } catch (IOException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(file);
            descriptionService.saveRCodeToFile(textDescriptionRCode.getText(),file);
        }
    }
    

    // Активация / деактивация кнопки Save 
    public void ButtonActivated() {
        if (change_idx) {
            mdi.Save.setEnabled(true);
            mdi.jExport.setEnabled(true);
            mdi.jButtonSave.setEnabled(true);
            mdi.Saveas.setEnabled(true);
            mdi.Code_Generation.setEnabled(true);
        } else {
            mdi.Save.setEnabled(false);
            mdi.jExport.setEnabled(false);
            mdi.jButtonSave.setEnabled(false);
            mdi.Saveas.setEnabled(false);
            mdi.Code_Generation.setEnabled(false);            
        }

        if (draw_idx) {
            mdi.jButtonS.setEnabled(true);
            mdi.jButtonV.setEnabled(true);
            mdi.jButtonNV.setEnabled(true);
            mdi.jButtonR.setEnabled(true);
            mdi.jButtonIF.setEnabled(true);
            mdi.jButtonO.setEnabled(true);
            mdi.jMenuItemS.setEnabled(true);
            mdi.jMenuItemV.setEnabled(true);
            mdi.jMenuItemR.setEnabled(true);
            mdi.jMenuItemNV.setEnabled(true);
            mdi.jMenuItemIF.setEnabled(true);
            mdi.jMenuItemO.setEnabled(true);
            mdi.jMenuItemClear.setEnabled(true);
            mdi.jMenuUndo.setEnabled(true);
            mdi.jMenuLegend.setEnabled(true);

        } else {
            mdi.jButtonS.setEnabled(false);
            mdi.jButtonV.setEnabled(false);
            mdi.jButtonNV.setEnabled(false);
            mdi.jButtonR.setEnabled(false);
            mdi.jButtonIF.setEnabled(false);
            mdi.jButtonO.setEnabled(false);
            mdi.jMenuItemS.setEnabled(false);
            mdi.jMenuItemV.setEnabled(false);
            mdi.jMenuItemR.setEnabled(false);
            mdi.jMenuItemNV.setEnabled(false);
            mdi.jMenuItemIF.setEnabled(false);
            mdi.jMenuItemO.setEnabled(false);
            mdi.jMenuItemClear.setEnabled(false);
            mdi.jMenuUndo.setEnabled(false);
            mdi.jMenuLegend.setEnabled(false);

        }
    }
    
    String[] names;
    public String[] GetNames() {
        String[] toReturn = new String[names.length];
        for (int i = 0; i < names.length; i++)
            toReturn[i] = names[names.length-1-i];
        return toReturn;
    }
    
    public double[][] PageRank(int iteration, double delta, double d) {
        PageRankService.PageRankResult result = pageRankService.pageRank(all, lines, iteration, delta, d);
        names = result.getNames();
        return result.getMatrix();
    }

    public int СyclomaticСomplexity() {
        int E = lines.size();
        int N = all.size();
        int P = Component();
        int M = E - N + 2*P;
        return M;
    }
    
    boolean[] used;    
    protected int Component() {
        used = new boolean[all.size()];
        int component = 0;
        for (int i = 0; i < all.size(); i++) {
            if (!used[i]) {
                DFS(i);
                component++;
            }
        }
        return component;
    }
    
    
    protected void DFS(int index) {
        used[index] = true;
        String nodeName = all.get(index).getNameF();

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getID1().equals(nodeName)) {
                for (int j = 0; j < all.size(); j++) {
                    if (all.get(j).getNameF().equals(lines.get(i).getID2()) && !used[j]) {
                        DFS(j);
                    }
                }
            } else if (lines.get(i).getID2().equals(nodeName)) { // Добавляем обратное направление
                for (int j = 0; j < all.size(); j++) {
                    if (all.get(j).getNameF().equals(lines.get(i).getID1()) && !used[j]) {
                        DFS(j);
                    }
                }
            }
        }
    }
    
    
    // Обработка скроллеров при перетаскивании элементов съемы
    private void checkPanelBounds() {
        // Получаем текущие размеры панели
        int currentWidth = jPanel1.getWidth();
        int currentHeight = jPanel1.getHeight();

        // Определяем минимальные и максимальные координаты фигур
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = 0;
        int maxY = 0;

        for (figures figure : all) {
            int figureX = figure.getXX();
            int figureY = figure.getYY();
            int figureSize = (int) figure.getS();

            minX = Math.min(minX, figureX);
            minY = Math.min(minY, figureY);
            maxX = Math.max(maxX, figureX + figureSize);
            maxY = Math.max(maxY, figureY + figureSize);
        }

        // Добавляем отступ для удобства
        int padding = 50;
        int newWidth = currentWidth;
        int newHeight = currentHeight;

        if (!atLeftBorder) {
        // Проверяем границы по оси X
        if (maxX + padding > currentWidth) {
            newWidth = maxX + padding; // Увеличиваем ширину, если фигура выходит за правую границу
        //} else if (minX - padding < 0) {
            // Если фигура выходит за левую границу, можно сдвинуть содержимое или расширить панель
            // В данном случае просто расширяем панель
          //  newWidth = currentWidth + (padding - minX);
        } else if (maxX + padding < currentWidth - padding) {
            // Уменьшаем ширину, если фигуры не используют правую часть панели
            newWidth = Math.max(maxX + padding, currentWidth / 2); // Не уменьшаем меньше половины текущей ширины
        }
        }
        
        
        if (!atTopBorder) {
        // Проверяем границы по оси Y
        if (maxY + padding > currentHeight) {
            newHeight = maxY + padding; // Увеличиваем высоту, если фигура выходит за нижнюю границу
        //} else if (minY - padding < 0) {
            // Если фигура выходит за верхнюю границу, можно сдвинуть содержимое или расширить панель
          //  newHeight = currentHeight + (padding - minY);
        } else if (maxY + padding < currentHeight - padding) {
            // Уменьшаем высоту, если фигуры не используют нижнюю часть панели
            newHeight = Math.max(maxY + padding, currentHeight / 2); // Не уменьшаем меньше половины текущей высоты
        }
        }

        // Устанавливаем новые размеры, если они изменились
        if (newWidth != currentWidth || newHeight != currentHeight) {
            jPanel1.setPreferredSize(new Dimension(newWidth, newHeight));
            jPanel1.revalidate();
        }
    }
    
    
    
    public void exportToPNG(String filename) {
        try {
            // Проверка расширения файла
            if (!filename.toLowerCase().endsWith(".png")) {
                filename += ".png";
            }

            // Создаем буфер для изображения с размерами jPanel1
            BufferedImage image = new BufferedImage(
                jPanel1.getWidth(), 
                jPanel1.getHeight(), 
                BufferedImage.TYPE_INT_RGB
            );

            // Получаем графический контекст изображения
            Graphics2D g2d = image.createGraphics();

            // Отрисовываем содержимое jPanel1 в изображение
            jPanel1.paint(g2d);
            g2d.dispose();

            // Проверяем существование файла
            File outputFile = new File(filename);
            if (outputFile.exists()) {
                int response = JOptionPane.showConfirmDialog(
                    this,
                    "File already exists. Overwrite?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
                );
                if (response != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Сохраняем изображение в файл
            ImageIO.write(image, "png", outputFile);
            JOptionPane.showMessageDialog(this, "PNG export completed successfully!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                this, 
                "Error saving the file: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    public void saveState() {
        historyService.saveState(all, lines, zoom);
    }
    
    public void undo() {
        DiagramState state = historyService.undo();
        if (state != null) {
            all = state.getFigures();
            lines = state.getLines();
            zoom = state.getZoom();

            // Рефреш UI
            jSize.setText(String.format("%d", zoom) + '%');
            grid.SetCellSize((int) ((GridPanel.GetBaseCellSize() * zoom) / 100));

            addPoints(zoom);
            updatePanelSize(); // Обновление размера панели
            updateSch();

            change_idx = true;
            ButtonActivated();
        }
    }

    public void clearHistory() {
        historyService.clear();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser SaveChooser;
    private java.awt.Canvas canvas1;
    private javax.swing.JButton closeDescrBut;
    private javax.swing.JButton copyDescrBut;
    private javax.swing.JButton copyDescrButRCode;
    private javax.swing.JDialog descrShowDialog;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItemClear;
    private javax.swing.JMenuItem jMenuItemDelette;
    private javax.swing.JMenuItem jMenuItemGenDesc;
    private javax.swing.JMenuItem jMenuLegend;
    private javax.swing.JMenuItem jMenuUndo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTextField jSize;
    private javax.swing.JTextField jSize1;
    private javax.swing.JButton rCodeActivatorBut;
    private javax.swing.JPopupMenu rcMenu;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JScrollPane scrollPaneR;
    private javax.swing.JTextArea textDescription;
    private javax.swing.JTextArea textDescriptionRCode;
    private javax.swing.JButton toRCodeBut;
    private javax.swing.JButton zminus;
    private javax.swing.JButton zminus1;
    private javax.swing.JButton zplus;
    private javax.swing.JButton zplus1;
    // End of variables declaration//GEN-END:variables

}
