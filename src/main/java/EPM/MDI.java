package EPM;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jdi.Method;
import logic.serialization.model.ConvertedObject;
import objects.figure.figures;
import objects.figure.NV;
import objects.figure.R;
import objects.figure.V;
import objects.figure.S1;
import objects.figure.O;
import objects.figure.d;
import jMDIForm.GridPanel;
import jMDIForm.PageRankFrame;
import jMDIForm.jMDIFrame;
import java.awt.Color;
import javax.swing.JColorChooser;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import javax.accessibility.AccessibleValue;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.prefs.Preferences; //для хранения n и i
import jMDIForm.ChoiceMethodPageRankDialog;
import java.nio.file.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class mdi extends javax.swing.JFrame {     
    private static final int FRAME_OFFSET = 20;
    private int c=0; // номер нового создаваемого mdi для вычисления смещения
    private PopupMenu jMDIFrame;

    
    public static Preferences prefsMdi = Preferences.userNodeForPackage(mdi.class);
    
    //Переменные для хранения цветов
    private Color originalGridColor;
    private Color originalBackgroundColor;
    private Color originalS1BackgroundColor;
    private Color originalS1TextColor;
    private Color originalVBackgroundColor;
    private Color originalVTextColor;
    private Color originalNVBackgroundColor;
    private Color originalNVTextColor;
    private Color originalRBackgroundColor;
    private Color originalRTextColor;
    private Color originalOBackgroundColor;
    private Color originalOTextColor;
    private Color originalIFBackgroundColor;
    private Color originalIFTextColor;
    private boolean originalGridVisibility;

    
    

    public mdi() {
         try { 
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); 
        } catch (Exception ex) { 
            ex.printStackTrace(); 
        }
        initComponents();
        
        
        // ---- Добавляем JScrollPane программно ----
        JScrollPane scrollPane = new JScrollPane(jDesktopPane);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    
        // Удаляем старый jDesktopPane из контейнера и заменяем его на scrollPane
        getContentPane().remove(jDesktopPane);
        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
        // ------------------------------------------
        
        Dimension sSize = Toolkit.getDefaultToolkit ().getScreenSize ();
        setSize (sSize);
        
        // Загружаем цвет фона из Preferences
        GridPanel.backgroundColor = new Color(GridPanel.prefs.getInt("BackgroundColor", Color.WHITE.getRGB()));
        jBackgroundColor.setBackground(GridPanel.backgroundColor);        
        
        if (!GridPanel.prefs.getBoolean("isVisible", true)) {
            //GridPanel.color = new Color (240, 240, 240);
            jChangeColorGrid.setEnabled(false);
        }
        
        
        JInternalFrame newFrame = new JInternalFrame();
        jDesktopPane.add(newFrame);
        addInternalFrameListeners(newFrame);
        updateDesktopPaneSize();
        enabledChanger();
        
    }
    
    private void addInternalFrameListeners(JInternalFrame frame) {
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                updateDesktopPaneSize();
            }
        
            @Override
            public void componentResized(ComponentEvent e) {
                updateDesktopPaneSize();
            }
        });
    }
    
    
    
    private void addNewFrame(jMDIFrame frame) {
        jDesktopPane.add(frame);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                updateDesktopSize();
            }
            @Override
            public void componentResized(ComponentEvent e) {
                updateDesktopSize();
            }
        });
        updateDesktopSize();
    scrollToFrame(frame); // Опционально
    }
    
    
    private void scrollToFrame(JInternalFrame frame) {
    Rectangle frameBounds = frame.getBounds();
    jDesktopPane.scrollRectToVisible(frameBounds);
    }
    
    private void updateDesktopSize() {
    Rectangle totalBounds = new Rectangle();
    
    // Собираем границы всех окон
    for (JInternalFrame frame : jDesktopPane.getAllFrames()) {
        totalBounds = totalBounds.union(frame.getBounds());
    }
    
    // Добавляем отступы (50px справа и снизу)
    int padding = 50;
    int newWidth = Math.max(
        jDesktopPane.getParent().getWidth(),
        totalBounds.width + totalBounds.x + padding
    );
    int newHeight = Math.max(
        jDesktopPane.getParent().getHeight(),
        totalBounds.height + totalBounds.y + padding
    );
    
    jDesktopPane.setPreferredSize(new Dimension(newWidth, newHeight));
    jDesktopPane.revalidate(); // Важно!
    }
    
    
    //часть для вырезки, вставки, копирования и удаления
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        OpenChooser = new javax.swing.JFileChooser();
        SaveChooser = new javax.swing.JFileChooser();
        Info = new javax.swing.JOptionPane();
        jDialogSettings = new javax.swing.JDialog();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jSTextColor = new javax.swing.JPanel();
        jVTextColor = new javax.swing.JPanel();
        jNVTextColor = new javax.swing.JPanel();
        jRTextColor = new javax.swing.JPanel();
        jIFTextColor = new javax.swing.JPanel();
        jOTextColor = new javax.swing.JPanel();
        jChangeTextColorS = new javax.swing.JButton();
        jChangeTextColorV = new javax.swing.JButton();
        jChangeTextColorNV = new javax.swing.JButton();
        jChangeTextColorR = new javax.swing.JButton();
        jChangeTextColorIF = new javax.swing.JButton();
        jChangeTextColorO = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jSColor = new javax.swing.JPanel();
        jVColor = new javax.swing.JPanel();
        jNVColor = new javax.swing.JPanel();
        jRColor = new javax.swing.JPanel();
        jIFColor = new javax.swing.JPanel();
        jOColor = new javax.swing.JPanel();
        jChangeColorS = new javax.swing.JButton();
        jChangeColorV = new javax.swing.JButton();
        jChangeColorNV = new javax.swing.JButton();
        jChangeColorR = new javax.swing.JButton();
        jChangeColorIF = new javax.swing.JButton();
        jChangeColorO = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jGridColor = new javax.swing.JPanel();
        jChangeColorGrid = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanelBackground = new javax.swing.JPanel();
        jBackgroundColor = new javax.swing.JPanel();
        jChangeColorBackground = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jColorChooser1 = new javax.swing.JColorChooser();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        SetConstantDialog = new javax.swing.JDialog();
        OkIBut = new javax.swing.JButton();
        CancelIBut = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        IValueField = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        NValueField = new javax.swing.JFormattedTextField();
        idNumLabel = new javax.swing.JLabel();
        startIdNum = new javax.swing.JFormattedTextField();
        jLabel5 = new javax.swing.JLabel();
        stepId = new javax.swing.JFormattedTextField();
        jLabel8 = new javax.swing.JLabel();
        valueFP = new javax.swing.JFormattedTextField();
        boxIsPlot = new javax.swing.JCheckBox();
        boxIsXES = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        xesNameFile = new javax.swing.JTextField();
        boxLookAtO = new javax.swing.JCheckBox();
        rFileSaveChooser = new javax.swing.JFileChooser();
        CyclomaticComplexityDialog = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        CyclomaticComplexityValue = new javax.swing.JLabel();
        jDesktopPane = new javax.swing.JDesktopPane();
        jToolBar1 = new javax.swing.JToolBar();
        jButtonNew = new javax.swing.JButton();
        jButtonOpen = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButtonS = new javax.swing.JButton();
        jButtonV = new javax.swing.JButton();
        jButtonNV = new javax.swing.JButton();
        jButtonR = new javax.swing.JButton();
        jButtonIF = new javax.swing.JButton();
        jButtonO = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        New = new javax.swing.JMenuItem();
        Open = new javax.swing.JMenuItem();
        Save = new javax.swing.JMenuItem();
        Saveas = new javax.swing.JMenuItem();
        jExport = new javax.swing.JMenu();
        ExportPNG = new javax.swing.JMenuItem();
        quit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuUndo = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItemS = new javax.swing.JMenuItem();
        jMenuItemV = new javax.swing.JMenuItem();
        jMenuItemNV = new javax.swing.JMenuItem();
        jMenuItemR = new javax.swing.JMenuItem();
        jMenuItemIF = new javax.swing.JMenuItem();
        jMenuItemO = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItemClear = new javax.swing.JMenuItem();
        Tools = new javax.swing.JMenu();
        jMenuLegend = new javax.swing.JMenuItem();
        Code_Generation = new javax.swing.JMenuItem();
        ProductivityAnalyzesMenu = new javax.swing.JMenu();
        ProductivityAnalyzesStaticMenuItem = new javax.swing.JMenuItem();
        ProductivityAnalyzesDynamicMenuItem = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuOptions = new javax.swing.JMenu();
        jMenuItemSetI = new javax.swing.JMenuItem();
        jMenuItemColorSettings = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        Cascade = new javax.swing.JMenuItem();
        Tile = new javax.swing.JMenuItem();
        jMenu6 = new javax.swing.JMenu();
        About = new javax.swing.JMenuItem();

        Info.setMessage("That's our programm. First versoin.");
        Info.setToolTipText("");
        Info.setFont(new java.awt.Font("Times New Roman", 0, 13)); // NOI18N

        jDialogSettings.setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        jDialogSettings.setResizable(false);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Shape line"));
        jPanel2.setPreferredSize(new java.awt.Dimension(170, 170));

        jSTextColor.setBackground(S1.TextColor);
        jSTextColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jSTextColor.setMaximumSize(new java.awt.Dimension(10, 10));
        jSTextColor.setPreferredSize(new java.awt.Dimension(25, 25));
        jSTextColor.setRequestFocusEnabled(false);

        javax.swing.GroupLayout jSTextColorLayout = new javax.swing.GroupLayout(jSTextColor);
        jSTextColor.setLayout(jSTextColorLayout);
        jSTextColorLayout.setHorizontalGroup(
            jSTextColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jSTextColorLayout.setVerticalGroup(
            jSTextColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

        jVTextColor.setBackground(V.TextColor);
        jVTextColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jVTextColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jVTextColorLayout = new javax.swing.GroupLayout(jVTextColor);
        jVTextColor.setLayout(jVTextColorLayout);
        jVTextColorLayout.setHorizontalGroup(
            jVTextColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jVTextColorLayout.setVerticalGroup(
            jVTextColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

        jNVTextColor.setBackground(NV.TextColor);
        jNVTextColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jNVTextColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jNVTextColorLayout = new javax.swing.GroupLayout(jNVTextColor);
        jNVTextColor.setLayout(jNVTextColorLayout);
        jNVTextColorLayout.setHorizontalGroup(
            jNVTextColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jNVTextColorLayout.setVerticalGroup(
            jNVTextColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

        jRTextColor.setBackground(R.TextColor);
        jRTextColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jRTextColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jRTextColorLayout = new javax.swing.GroupLayout(jRTextColor);
        jRTextColor.setLayout(jRTextColorLayout);
        jRTextColorLayout.setHorizontalGroup(
            jRTextColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jRTextColorLayout.setVerticalGroup(
            jRTextColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

        jIFTextColor.setBackground(d.TextColor);
        jIFTextColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jIFTextColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jIFTextColorLayout = new javax.swing.GroupLayout(jIFTextColor);
        jIFTextColor.setLayout(jIFTextColorLayout);
        jIFTextColorLayout.setHorizontalGroup(
            jIFTextColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jIFTextColorLayout.setVerticalGroup(
            jIFTextColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

        jOTextColor.setBackground(O.TextColor);
        jOTextColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jOTextColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jOTextColorLayout = new javax.swing.GroupLayout(jOTextColor);
        jOTextColor.setLayout(jOTextColorLayout);
        jOTextColorLayout.setHorizontalGroup(
            jOTextColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jOTextColorLayout.setVerticalGroup(
            jOTextColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

        jChangeTextColorS.setText("S");
        jChangeTextColorS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeTextColorSActionPerformed(evt);
            }
        });

        jChangeTextColorV.setText("V");
        jChangeTextColorV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeTextColorVActionPerformed(evt);
            }
        });

        jChangeTextColorNV.setText("NV");
        jChangeTextColorNV.setPreferredSize(new java.awt.Dimension(23, 23));
        jChangeTextColorNV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeTextColorNVActionPerformed(evt);
            }
        });

        jChangeTextColorR.setText("R");
        jChangeTextColorR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeTextColorRActionPerformed(evt);
            }
        });

        jChangeTextColorIF.setText("IF");
        jChangeTextColorIF.setPreferredSize(new java.awt.Dimension(23, 23));
        jChangeTextColorIF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeTextColorIFActionPerformed(evt);
            }
        });

        jChangeTextColorO.setText("O");
        jChangeTextColorO.setMaximumSize(new java.awt.Dimension(72, 23));
        jChangeTextColorO.setMinimumSize(new java.awt.Dimension(72, 23));
        jChangeTextColorO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeTextColorOActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jOTextColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jChangeTextColorO, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jNVTextColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jChangeTextColorNV, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jRTextColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jChangeTextColorR, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jIFTextColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jChangeTextColorIF, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jSTextColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jChangeTextColorS, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jVTextColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jChangeTextColorV, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSTextColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangeTextColorS))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jVTextColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangeTextColorV))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jNVTextColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangeTextColorNV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRTextColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangeTextColorR))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jIFTextColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangeTextColorIF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jOTextColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangeTextColorO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Shape filling"));

        jSColor.setBackground(S1.BackgroundColor);
        jSColor.setBackground(S1.BackgroundColor);
        jSColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jSColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jSColorLayout = new javax.swing.GroupLayout(jSColor);
        jSColor.setLayout(jSColorLayout);
        jSColorLayout.setHorizontalGroup(
            jSColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jSColorLayout.setVerticalGroup(
            jSColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        jVColor.setBackground(V.BackgroundColor);
        jVColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jVColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jVColorLayout = new javax.swing.GroupLayout(jVColor);
        jVColor.setLayout(jVColorLayout);
        jVColorLayout.setHorizontalGroup(
            jVColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jVColorLayout.setVerticalGroup(
            jVColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        jNVColor.setBackground(NV.BackgroundColor);
        jNVColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jNVColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jNVColorLayout = new javax.swing.GroupLayout(jNVColor);
        jNVColor.setLayout(jNVColorLayout);
        jNVColorLayout.setHorizontalGroup(
            jNVColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jNVColorLayout.setVerticalGroup(
            jNVColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        jRColor.setBackground(R.BackgroundColor);
        jRColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jRColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jRColorLayout = new javax.swing.GroupLayout(jRColor);
        jRColor.setLayout(jRColorLayout);
        jRColorLayout.setHorizontalGroup(
            jRColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jRColorLayout.setVerticalGroup(
            jRColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        jIFColor.setBackground(d.BackgroundColor);
        jIFColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jIFColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jIFColorLayout = new javax.swing.GroupLayout(jIFColor);
        jIFColor.setLayout(jIFColorLayout);
        jIFColorLayout.setHorizontalGroup(
            jIFColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jIFColorLayout.setVerticalGroup(
            jIFColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        jOColor.setBackground(O.BackgroundColor);
        jOColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jOColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jOColorLayout = new javax.swing.GroupLayout(jOColor);
        jOColor.setLayout(jOColorLayout);
        jOColorLayout.setHorizontalGroup(
            jOColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jOColorLayout.setVerticalGroup(
            jOColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        jChangeColorS.setText("S");
        jChangeColorS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeColorSActionPerformed(evt);
            }
        });

        jChangeColorV.setText("V");
        jChangeColorV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeColorVActionPerformed(evt);
            }
        });

        jChangeColorNV.setText("NV");
        jChangeColorNV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeColorNVActionPerformed(evt);
            }
        });

        jChangeColorR.setText("R");
        jChangeColorR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeColorRActionPerformed(evt);
            }
        });

        jChangeColorIF.setText("IF");
        jChangeColorIF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeColorIFActionPerformed(evt);
            }
        });

        jChangeColorO.setText("O");
        jChangeColorO.setMaximumSize(new java.awt.Dimension(72, 23));
        jChangeColorO.setMinimumSize(new java.awt.Dimension(72, 23));
        jChangeColorO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeColorOActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jOColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jChangeColorO, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jSColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jChangeColorS, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jVColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jChangeColorV, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jRColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jChangeColorR, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jNVColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jChangeColorNV, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jIFColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jChangeColorIF, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSColor, 24, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangeColorS))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jVColor, 24, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangeColorV))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jNVColor, 24, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangeColorNV))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRColor, 24, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangeColorR))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jIFColor, 24, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangeColorIF))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jOColor, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChangeColorO, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Grid"));

        jGridColor.setBackground(GridPanel.color);
        jGridColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jGridColor.setEnabled(false);
        jGridColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jGridColorLayout = new javax.swing.GroupLayout(jGridColor);
        jGridColor.setLayout(jGridColorLayout);
        jGridColorLayout.setHorizontalGroup(
            jGridColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jGridColorLayout.setVerticalGroup(
            jGridColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

        jChangeColorGrid.setText("Color");
        jChangeColorGrid.setEnabled(false);
        jChangeColorGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeColorGridActionPerformed(evt);
            }
        });

        jCheckBox1.setText("show grid");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addGap(39, 39, 39)
                .addComponent(jGridColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jChangeColorGrid, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jChangeColorGrid)
                    .addComponent(jGridColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton2.setText("Ok");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Cancel");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jPanelBackground.setBorder(javax.swing.BorderFactory.createTitledBorder("Background"));

        jBackgroundColor.setBackground(GridPanel.color);
        jBackgroundColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jBackgroundColor.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jBackgroundColorLayout = new javax.swing.GroupLayout(jBackgroundColor);
        jBackgroundColor.setLayout(jBackgroundColorLayout);
        jBackgroundColorLayout.setHorizontalGroup(
            jBackgroundColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );
        jBackgroundColorLayout.setVerticalGroup(
            jBackgroundColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

        jChangeColorBackground.setText("Color");
        jChangeColorBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeColorBackgroundActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelBackgroundLayout = new javax.swing.GroupLayout(jPanelBackground);
        jPanelBackground.setLayout(jPanelBackgroundLayout);
        jPanelBackgroundLayout.setHorizontalGroup(
            jPanelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBackgroundLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jBackgroundColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jChangeColorBackground, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelBackgroundLayout.setVerticalGroup(
            jPanelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBackgroundLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jChangeColorBackground)
                    .addComponent(jBackgroundColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addGap(18, 18, 18)
                        .addComponent(jButton3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelBackground, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanelBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelBackground.getAccessibleContext().setAccessibleName("BackgroundColor");

        javax.swing.GroupLayout jDialogSettingsLayout = new javax.swing.GroupLayout(jDialogSettings.getContentPane());
        jDialogSettings.getContentPane().setLayout(jDialogSettingsLayout);
        jDialogSettingsLayout.setHorizontalGroup(
            jDialogSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jDialogSettingsLayout.setVerticalGroup(
            jDialogSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogSettingsLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton10.setText("jButton10");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        SetConstantDialog.setMinimumSize(new java.awt.Dimension(300, 380));
        SetConstantDialog.setModal(true);
        SetConstantDialog.setResizable(false);

        OkIBut.setText("Ok");
        OkIBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OkIButActionPerformed(evt);
            }
        });

        CancelIBut.setText("Cancel");
        CancelIBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelIButActionPerformed(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Model variables"));
        jPanel5.setToolTipText("");
        jPanel5.setName(""); // NOI18N

        jLabel1.setText("Calculation steps' start value (i):");

        IValueField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        IValueField.setText("1");
        IValueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IValueFieldActionPerformed(evt);
            }
        });

        jLabel3.setText("Calculation steps' end value (n):");

        NValueField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        NValueField.setText("1000");
        NValueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NValueFieldActionPerformed(evt);
            }
        });

        idNumLabel.setText("First ID value (element S1):");

        startIdNum.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        startIdNum.setText("1");
        startIdNum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startIdNumActionPerformed(evt);
            }
        });

        jLabel5.setText("A gap between ID groups: ");

        stepId.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        stepId.setText("1000");
        stepId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepIdActionPerformed(evt);
            }
        });

        jLabel8.setText("Start step for a periodic S (FP):");

        valueFP.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        valueFP.setText("1");
        valueFP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueFPActionPerformed(evt);
            }
        });

        boxIsPlot.setText("auto-generate diagram's code ");
        boxIsPlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxIsPlotActionPerformed(evt);
            }
        });
        boxIsPlot.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                boxIsPlotPropertyChange(evt);
            }
        });

        boxIsXES.setText("auto-generate code for XES output");
        boxIsXES.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxIsXESActionPerformed(evt);
            }
        });
        boxIsXES.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                boxIsXESPropertyChange(evt);
            }
        });

        jLabel7.setText("Name of XES file:");
        jLabel7.setEnabled(false);

        xesNameFile.setText("result");
        xesNameFile.setEnabled(false);

        boxLookAtO.setText("the process of O elements");
        boxLookAtO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxLookAtOActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(boxIsPlot)
                    .addComponent(boxIsXES)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(xesNameFile, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(idNumLabel)
                            .addComponent(jLabel5)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(valueFP, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(startIdNum, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(NValueField, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(IValueField, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(stepId, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(boxLookAtO))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(IValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(NValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startIdNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(idNumLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(stepId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(valueFP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(boxIsPlot, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(boxIsXES, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(xesNameFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(boxLookAtO, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout SetConstantDialogLayout = new javax.swing.GroupLayout(SetConstantDialog.getContentPane());
        SetConstantDialog.getContentPane().setLayout(SetConstantDialogLayout);
        SetConstantDialogLayout.setHorizontalGroup(
            SetConstantDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SetConstantDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(SetConstantDialogLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(OkIBut)
                .addGap(18, 18, 18)
                .addComponent(CancelIBut)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        SetConstantDialogLayout.setVerticalGroup(
            SetConstantDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SetConstantDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(SetConstantDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(CancelIBut)
                    .addComponent(OkIBut))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.getAccessibleContext().setAccessibleName("");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Cyclomatic complexity = ");

        CyclomaticComplexityValue.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        CyclomaticComplexityValue.setText("0");

        javax.swing.GroupLayout CyclomaticComplexityDialogLayout = new javax.swing.GroupLayout(CyclomaticComplexityDialog.getContentPane());
        CyclomaticComplexityDialog.getContentPane().setLayout(CyclomaticComplexityDialogLayout);
        CyclomaticComplexityDialogLayout.setHorizontalGroup(
            CyclomaticComplexityDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(CyclomaticComplexityDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CyclomaticComplexityValue)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        CyclomaticComplexityDialogLayout.setVerticalGroup(
            CyclomaticComplexityDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CyclomaticComplexityDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                .addComponent(CyclomaticComplexityValue))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setFont(new java.awt.Font("Times New Roman", 0, 10)); // NOI18N
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        getContentPane().setLayout(new java.awt.BorderLayout(1, 1));

        jDesktopPane.setBackground(java.awt.SystemColor.windowBorder);
        jDesktopPane.setAutoscrolls(true);
        jDesktopPane.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        jDesktopPane.setMinimumSize(new java.awt.Dimension(800, 400));

        javax.swing.GroupLayout jDesktopPaneLayout = new javax.swing.GroupLayout(jDesktopPane);
        jDesktopPane.setLayout(jDesktopPaneLayout);
        jDesktopPaneLayout.setHorizontalGroup(
            jDesktopPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        jDesktopPaneLayout.setVerticalGroup(
            jDesktopPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );

        getContentPane().add(jDesktopPane, java.awt.BorderLayout.CENTER);

        jToolBar1.setRollover(true);

        jButtonNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/new3.gif"))); // NOI18N
        jButtonNew.setText("New");
        jButtonNew.setFocusable(false);
        jButtonNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonNew);

        jButtonOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/open3.gif"))); // NOI18N
        jButtonOpen.setText("Open");
        jButtonOpen.setFocusable(false);
        jButtonOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonOpen);

        jButtonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/save3.gif"))); // NOI18N
        jButtonSave.setText("Save");
        jButtonSave.setEnabled(false);
        jButtonSave.setFocusable(false);
        jButtonSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonSave);
        jToolBar1.add(jSeparator1);

        jButtonS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/S2.gif"))); // NOI18N
        jButtonS.setText("S");
        jButtonS.setEnabled(false);
        jButtonS.setFocusable(false);
        jButtonS.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonS.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonS);

        jButtonV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/V2.gif"))); // NOI18N
        jButtonV.setText("V");
        jButtonV.setEnabled(false);
        jButtonV.setFocusable(false);
        jButtonV.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonV.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonVActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonV);

        jButtonNV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/NV2.gif"))); // NOI18N
        jButtonNV.setText("NV");
        jButtonNV.setEnabled(false);
        jButtonNV.setFocusable(false);
        jButtonNV.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonNV.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonNV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNVActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonNV);

        jButtonR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/R2.gif"))); // NOI18N
        jButtonR.setText("R");
        jButtonR.setEnabled(false);
        jButtonR.setFocusable(false);
        jButtonR.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonR.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonR);

        jButtonIF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/IF2.gif"))); // NOI18N
        jButtonIF.setText("IF");
        jButtonIF.setEnabled(false);
        jButtonIF.setFocusable(false);
        jButtonIF.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonIF.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonIF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonIFActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonIF);

        jButtonO.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/O2.gif"))); // NOI18N
        jButtonO.setText("O");
        jButtonO.setEnabled(false);
        jButtonO.setFocusable(false);
        jButtonO.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonO.setMaximumSize(new java.awt.Dimension(42, 60));
        jButtonO.setMinimumSize(new java.awt.Dimension(42, 60));
        jButtonO.setPreferredSize(new java.awt.Dimension(42, 60));
        jButtonO.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonO);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        jMenuBar1.setAlignmentY(0.5F);
        jMenuBar1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jMenu1.setText("File");
        jMenu1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        New.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.META_DOWN_MASK));
        New.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        New.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/new2.gif"))); // NOI18N
        New.setText("New");
        New.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewActionPerformed(evt);
            }
        });
        jMenu1.add(New);

        Open.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_DOWN_MASK));
        Open.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        Open.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/open2.gif"))); // NOI18N
        Open.setText("Open");
        Open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenActionPerformed(evt);
            }
        });
        jMenu1.add(Open);

        Save.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.META_DOWN_MASK));
        Save.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        Save.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/save2.gif"))); // NOI18N
        Save.setText("Save");
        Save.setEnabled(false);
        Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveActionPerformed(evt);
            }
        });
        jMenu1.add(Save);

        Saveas.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        Saveas.setText("Save as ...");
        Saveas.setEnabled(false);
        Saveas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveasActionPerformed(evt);
            }
        });
        jMenu1.add(Saveas);

        jExport.setText("Export");
        jExport.setEnabled(false);

        ExportPNG.setText("Export to PNG");
        ExportPNG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExportPNGActionPerformed(evt);
            }
        });
        jExport.add(ExportPNG);

        jMenu1.add(jExport);

        quit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_DOWN_MASK));
        quit.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        quit.setText("Exit");
        quit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitActionPerformed(evt);
            }
        });
        jMenu1.add(quit);

        jMenuBar1.add(jMenu1);

        jMenuEdit.setText("Edit");
        jMenuEdit.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jMenuUndo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.META_DOWN_MASK));
        jMenuUndo.setText("Undo");
        jMenuUndo.setEnabled(false);
        jMenuUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuUndoActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuUndo);
        jMenuEdit.add(jSeparator2);

        jMenuItemS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/S1.gif"))); // NOI18N
        jMenuItemS.setText("Insert S");
        jMenuItemS.setEnabled(false);
        jMenuItemS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemS);

        jMenuItemV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/V1.gif"))); // NOI18N
        jMenuItemV.setText("Insert V");
        jMenuItemV.setEnabled(false);
        jMenuItemV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonVActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemV);

        jMenuItemNV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/NV1.gif"))); // NOI18N
        jMenuItemNV.setText("Insert NV");
        jMenuItemNV.setEnabled(false);
        jMenuItemNV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNVActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemNV);

        jMenuItemR.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/R1.gif"))); // NOI18N
        jMenuItemR.setText("Insert R");
        jMenuItemR.setEnabled(false);
        jMenuItemR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemR);

        jMenuItemIF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/IF1.gif"))); // NOI18N
        jMenuItemIF.setText("Insert IF");
        jMenuItemIF.setEnabled(false);
        jMenuItemIF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonIFActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemIF);

        jMenuItemO.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/O1.gif"))); // NOI18N
        jMenuItemO.setText("Insert O");
        jMenuItemO.setEnabled(false);
        jMenuItemO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemO);
        jMenuEdit.add(jSeparator3);

        jMenuItemClear.setText("Clear all");
        jMenuItemClear.setEnabled(false);
        jMenuItemClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemClear);

        jMenuBar1.add(jMenuEdit);

        Tools.setText("Tools");
        Tools.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                ToolsMenuSelected(evt);
            }
        });

        jMenuLegend.setText("Show legend");
        jMenuLegend.setEnabled(false);
        Tools.add(jMenuLegend);

        Code_Generation.setText("Model generation ...");
        Code_Generation.setEnabled(false);
        Code_Generation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Code_GenerationActionPerformed(evt);
            }
        });
        Tools.add(Code_Generation);

        ProductivityAnalyzesMenu.setText("Productivity analyzes");
        ProductivityAnalyzesMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                ProductivityAnalyzesMenuMenuSelected(evt);
            }
        });

        ProductivityAnalyzesStaticMenuItem.setText("Static");
        ProductivityAnalyzesStaticMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProductivityAnalyzesStaticMenuItemActionPerformed(evt);
            }
        });
        ProductivityAnalyzesMenu.add(ProductivityAnalyzesStaticMenuItem);

        ProductivityAnalyzesDynamicMenuItem.setText("Dynamic");
        ProductivityAnalyzesDynamicMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProductivityAnalyzesDynamicMenuItemActionPerformed(evt);
            }
        });
        ProductivityAnalyzesMenu.add(ProductivityAnalyzesDynamicMenuItem);

        Tools.add(ProductivityAnalyzesMenu);

        jMenuItem2.setText("Cyclomatic complexity");
        jMenuItem2.setEnabled(false);
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        Tools.add(jMenuItem2);

        jMenuBar1.add(Tools);

        jMenuOptions.setText("Options");

        jMenuItemSetI.setText("Simulation settings ...");
        jMenuItemSetI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSetIActionPerformed(evt);
            }
        });
        jMenuOptions.add(jMenuItemSetI);

        jMenuItemColorSettings.setText("Color settings ...");
        jMenuItemColorSettings.setActionCommand("Color settings ...");
        jMenuItemColorSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemColorSettingsActionPerformed(evt);
            }
        });
        jMenuOptions.add(jMenuItemColorSettings);

        jMenuBar1.add(jMenuOptions);

        jMenu5.setText("Window");
        jMenu5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        Cascade.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        Cascade.setText("Cascade");
        Cascade.setEnabled(false);
        Cascade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CascadeActionPerformed(evt);
            }
        });
        jMenu5.add(Cascade);

        Tile.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        Tile.setText("Tile");
        Tile.setEnabled(false);
        Tile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TileActionPerformed(evt);
            }
        });
        jMenu5.add(Tile);

        jMenuBar1.add(jMenu5);

        jMenu6.setText("Help");
        jMenu6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        About.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        About.setText("About");
        About.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AboutActionPerformed(evt);
            }
        });
        jMenu6.add(About);

        jMenuBar1.add(jMenu6);

        setJMenuBar(jMenuBar1);

        setBounds(0, 0, 800, 492);
    }// </editor-fold>//GEN-END:initComponents

    
    
    
    
    private void NewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewActionPerformed
        String title = "New Model " + c; 
        Boolean resizable = true; 
        Boolean closable = true; 
        Boolean maximizable = false; 
        Boolean iconifiable = true; 
        
        jMDIFrame iFrame =  new jMDIFrame(title, resizable, closable,  maximizable, iconifiable,null); 
        iFrame.setPreferredSize(new Dimension(800, 700));
        iFrame.setSize(iFrame.getPreferredSize());
        
        iFrame.setResizable(true);
        iFrame.pack();
        iFrame.setLocation(20*c, 20*c);
        
        //jDesktopPane.add(iFrame);
        addNewFrame(iFrame);
        iFrame.clearHistory();

        iFrame.toFront();
        iFrame.setVisible(true);
        
         Cascade.setEnabled(true);
         Tile.setEnabled(true);
         
        c++;
    }//GEN-LAST:event_NewActionPerformed

    private void OpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenActionPerformed
        OpenChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        //OpenChooser.setCurrentDirectory(OpenChooser.getCurrentDirectory());
        OpenChooser.setCurrentDirectory(new File("."));
        OpenChooser.setFileFilter(new FileNameExtensionFilter("Event-driven Process Methodology", "json", "epm"));
        int option = OpenChooser.showOpenDialog(this);
        //.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
        if(option == JFileChooser.APPROVE_OPTION) {
            
            String title = OpenChooser.getSelectedFile().getName(); 
            String file = OpenChooser.getSelectedFile().getAbsolutePath();
            
            Boolean resizable = true; 
            Boolean closable = true; 
            Boolean maximizable = false; 
            Boolean iconifiable = true; 
        
            jMDIFrame iFrame =  new jMDIFrame(file, resizable, closable,  maximizable, iconifiable,null); 
            iFrame.setPreferredSize(new Dimension(800, 700));
            iFrame.setSize(iFrame.getPreferredSize());
        
            iFrame.setResizable(true);
            iFrame.pack();
            iFrame.setLocation(20*c, 20*c);
        
            //jDesktopPane.add(iFrame);
            addNewFrame(iFrame);

            iFrame.toFront();
            iFrame.setVisible(true);
        
            Cascade.setEnabled(true);
            Tile.setEnabled(true);
         
            c++;
            
            iFrame.LoadFromJSON(file);
        }
    }//GEN-LAST:event_OpenActionPerformed

    private void SaveasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveasActionPerformed

        // 1. Вытаскиваем имя файла из активного окна
        JInternalFrame iFrame = jDesktopPane.getSelectedFrame();        
        Field field = null;
        try {
            field = iFrame.getClass().getDeclaredField("fileName");
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }
        field.setAccessible(true);
        
        Object fn = "";
        try {
            fn = field.get(iFrame);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }

        SaveChooser.setDialogTitle("Specify a file to save");// ("+fn+")");
        SaveChooser.setCurrentDirectory(new File("."));
        SaveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        SaveChooser.setFileFilter(new FileNameExtensionFilter("Event-driven Process Methodology", "json"));
        SaveChooser.approveSelection();
        int option = SaveChooser.showSaveDialog(iFrame);

        //.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
        
        Field field2 = null;
        
        if(option != JFileChooser.CANCEL_OPTION) {
            
            File file1 = SaveChooser.getSelectedFile(); 
            String file = null;
            
            try {
                file = file1.getCanonicalPath()+".json";
            } catch (IOException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                field2 = iFrame.getClass().getDeclaredField("fileName");
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            }
            field2.setAccessible(true);
        
            try {
                field2.set(iFrame, file);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            } 
            
            // 3. Вызываем метод сохранения из активного окна            
            try {
                java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("SaveInJSON", String.class);
                method.setAccessible(true);
                method.invoke(iFrame, file);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            iFrame.setTitle(file);
            
            
            // 2. Устанавливаем флаг изменений
            try {
                field2 = iFrame.getClass().getDeclaredField("change_idx");
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            }
            field2.setAccessible(true);
        
            try {
                field2.set(iFrame, false);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            }                
            // 4. Вызываем метод обновления состояния кнопок
            try {
              java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("ButtonActivated");
              method.setAccessible(true);
              method.invoke(iFrame);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            
                        // 2. Устанавливаем флаг изменений
            try {
                field2 = iFrame.getClass().getDeclaredField("change_idx");
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            }
            field2.setAccessible(true);
        
            try {
                field2.set(iFrame, true);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            } 

            // 4. Вызываем метод обновления состояния кнопок
            try {
              java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("ButtonActivated");
              method.setAccessible(true);
              method.invoke(iFrame);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_SaveasActionPerformed

    private void TileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TileActionPerformed
        JInternalFrame[] allFrames = jDesktopPane.getAllFrames();
        if (allFrames.length == 0) return; // Если нет окон, ничего не делаем

        // Получаем размеры рабочей области jDesktopPane (без учета границ)
        int desktopWidth = jDesktopPane.getWidth();
        int desktopHeight = jDesktopPane.getHeight();

        // Вычисляем количество строк и столбцов для равномерного распределения окон
        int cols = (int) Math.ceil(Math.sqrt(allFrames.length));
        int rows = (int) Math.ceil((double) allFrames.length / cols);

        // Вычисляем ширину и высоту каждого окна
        int frameWidth = Math.max(100, desktopWidth / cols);  // Минимальная ширина = 100
        int frameHeight = Math.max(100, desktopHeight / rows); // Минимальная высота = 100

        int x = 0, y = 0;
        int currentCol = 0, currentRow = 0;

        for (JInternalFrame frame : allFrames) {
            // Устанавливаем размер и позицию окна
            frame.setSize(frameWidth, frameHeight);
            frame.setLocation(x, y);

            // Переходим к следующему столбцу
            x += frameWidth;
            currentCol++;

            // Если дошли до конца строки, переходим на новую строку
            if (currentCol >= cols) {
                x = 0;
                y += frameHeight;
                currentCol = 0;
                currentRow++;
            }
        }
    }//GEN-LAST:event_TileActionPerformed

    private void CascadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CascadeActionPerformed
        int x = 0;
        int y = 0;
        JInternalFrame allFrames[] = jDesktopPane.getAllFrames();
    
        // Получаем размеры рабочей области jDesktopPane
        int desktopWidth = jDesktopPane.getWidth();
        int desktopHeight = jDesktopPane.getHeight();
    
        // Рассчитываем максимально возможные смещения, чтобы окна не выходили за границы
        int maxFrameWidth = desktopWidth - (allFrames.length * FRAME_OFFSET);
        int maxFrameHeight = desktopHeight - (allFrames.length * FRAME_OFFSET);
    
        // Устанавливаем размеры фреймов так, чтобы они не выходили за границы
        for (int i = allFrames.length - 1; i >= 0; i--) {
            // Ограничиваем размеры фрейма
            int frameWidth = Math.min(maxFrameWidth, allFrames[i].getWidth());
            int frameHeight = Math.min(maxFrameHeight, allFrames[i].getHeight());
        
            // Ограничиваем позиционирование, чтобы фрейм не выходил за границы
            x = Math.min(x, desktopWidth - frameWidth);
            y = Math.min(y, desktopHeight - frameHeight);
        
            allFrames[i].setSize(frameWidth, frameHeight);
            allFrames[i].setLocation(x, y);
        
            x = x + FRAME_OFFSET;
            y = y + FRAME_OFFSET;
        }  
    }//GEN-LAST:event_CascadeActionPerformed

    private void AboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AboutActionPerformed
        Info.showInternalMessageDialog(jDesktopPane, " Event-driven Process Methodology (EdPM) Modeller"
                + "\n Version: in dev process"
                + "\n Copyright (C) 2025", "About", JOptionPane.INFORMATION_MESSAGE,null);

    }//GEN-LAST:event_AboutActionPerformed

    private void quitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitActionPerformed
    System.exit(0);
    }//GEN-LAST:event_quitActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        Dimension frameSize = getSize();
        jDesktopPane.setSize(frameSize);
        
    }//GEN-LAST:event_formComponentResized

    private void SaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveActionPerformed

        // 1. Вытаскиваем имя файла из активного окна
        JInternalFrame iFrame = jDesktopPane.getSelectedFrame();        
        Field field = null;
        try {
            field = iFrame.getClass().getDeclaredField("fileName");
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }
        field.setAccessible(true);
        
        Object fn = "";
        try {
            fn = field.get(iFrame);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }
        // 1. Конец
        // 2. Устанавливаем флаг изменений
        Field field2 = null;
        try {
            field2 = iFrame.getClass().getDeclaredField("change_idx");
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }
        field2.setAccessible(true);
        
        try {
            field2.set(iFrame, false);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        } 
        // 2. 
        // 3. Вызываем метод сохранения из активного окна
        if (fn != "") {
            try {
                java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("SaveInJSON", String.class);
                method.setAccessible(true);
                method.invoke(iFrame, fn);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            // сохраняем под новым именем т.к. имя еще не задано
            this.SaveasActionPerformed(evt);
        }
        // 3.
        
        // 4. Вызываем метод обновления состояния кнопок
        try {
            java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("ButtonActivated");
            method.setAccessible(true);
            method.invoke(iFrame);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_SaveActionPerformed

    //Вставляем объект S
    private void jButtonSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSActionPerformed
        JInternalFrame iFrame = jDesktopPane.getSelectedFrame();        
        try {
            java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("SActionPerformed", java.awt.event.ActionEvent.class);
            method.setAccessible(true);
            method.invoke(iFrame, evt);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonSActionPerformed

    //Вставляем объект R
    private void jButtonRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRActionPerformed
        JInternalFrame iFrame = jDesktopPane.getSelectedFrame();        
        try {
            java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("RActionPerformed", java.awt.event.ActionEvent.class);
            method.setAccessible(true);
            method.invoke(iFrame, evt);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }//GEN-LAST:event_jButtonRActionPerformed

    private void jButtonIFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonIFActionPerformed
        // TODO add your handling code here:
        JInternalFrame iFrame = jDesktopPane.getSelectedFrame();        
        try {
            java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("DActionPerformed", java.awt.event.ActionEvent.class);
            method.setAccessible(true);
            method.invoke(iFrame, evt);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }//GEN-LAST:event_jButtonIFActionPerformed

    private void jButtonVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonVActionPerformed
        JInternalFrame iFrame = jDesktopPane.getSelectedFrame();        
        try {
            java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("VActionPerformed", java.awt.event.ActionEvent.class);
            method.setAccessible(true);
            method.invoke(iFrame, evt);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }                
    }//GEN-LAST:event_jButtonVActionPerformed

    private void jMenuItemClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClearActionPerformed
         JInternalFrame iFrame = jDesktopPane.getSelectedFrame();        
        try {
            java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("clearActionPerformed", java.awt.event.ActionEvent.class);
            method.setAccessible(true);
            method.invoke(iFrame, evt);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }                
        
    }//GEN-LAST:event_jMenuItemClearActionPerformed

    private void jButtonNVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNVActionPerformed
        JInternalFrame iFrame = jDesktopPane.getSelectedFrame();        
        try {
            java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("NVActionPerformed", java.awt.event.ActionEvent.class);
            method.setAccessible(true);
            method.invoke(iFrame, evt);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }                
         
        
    }//GEN-LAST:event_jButtonNVActionPerformed
//Начало работы Иванова А.А. настройка цвета
    //меню настроек
    private void jMenuItemColorSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemColorSettingsActionPerformed
        
        
            // Сохраняем текущие цвета перед открытием диалога
        originalGridColor = GridPanel.color;
        originalBackgroundColor = GridPanel.backgroundColor;
        originalS1BackgroundColor = S1.BackgroundColor;
        originalS1TextColor = S1.TextColor;
        originalVBackgroundColor = V.BackgroundColor;
        originalVTextColor = V.TextColor;
        originalNVBackgroundColor = NV.BackgroundColor;
        originalNVTextColor = NV.TextColor;
        originalRBackgroundColor = R.BackgroundColor;
        originalRTextColor = R.TextColor;
        originalOBackgroundColor = O.BackgroundColor;
        originalOTextColor = O.TextColor;
        originalIFBackgroundColor = d.BackgroundColor;
        originalIFTextColor = d.TextColor;
        originalGridVisibility = GridPanel.isVisible;

        // Устанавливаем текущие значения в UI
        jGridColor.setBackground(GridPanel.color);
        jBackgroundColor.setBackground(GridPanel.backgroundColor);
        jSColor.setBackground(S1.BackgroundColor);
        jSTextColor.setBackground(S1.TextColor);
        jVColor.setBackground(V.BackgroundColor);
        jVTextColor.setBackground(V.TextColor);
        jNVColor.setBackground(NV.BackgroundColor);
        jNVTextColor.setBackground(NV.TextColor);
        jRColor.setBackground(R.BackgroundColor);
        jRTextColor.setBackground(R.TextColor);
        jOColor.setBackground(O.BackgroundColor);
        jOTextColor.setBackground(O.TextColor);
        jIFColor.setBackground(d.BackgroundColor);
        jIFTextColor.setBackground(d.TextColor);
        jCheckBox1.setSelected(GridPanel.isVisible);

        
        // Автоматически подгоняем размер окна под содержимое
        jDialogSettings.pack();
        
        // Центрируем относительно экрана
        jDialogSettings.setLocationRelativeTo(null);
        
        
        
        jDialogSettings.setTitle("Color settings");
        
        
        jDialogSettings.setLocation((jDesktopPane.getWidth()-jDialogSettings.getWidth())/2, (jDesktopPane.getHeight()-jDialogSettings.getHeight())/2);
        
        if (GridPanel.isVisible) {
          jCheckBox1.setSelected(true);   
        } else {
          jCheckBox1.setSelected(false); 
        }
        
        jCheckBox1.setSelected(GridPanel.isVisible); // Восстанавливаем из настроек или переменной
        jGridColor.setEnabled(jCheckBox1.isSelected());
        jChangeColorGrid.setEnabled(jCheckBox1.isSelected());
        
        jDialogSettings.setVisible(true);
    }//GEN-LAST:event_jMenuItemColorSettingsActionPerformed

    Color gridColor = GridPanel.color;                              
    private void jButtonOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOActionPerformed
        JInternalFrame iFrame = jDesktopPane.getSelectedFrame();        
        try {
            java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("OActionPerformed", java.awt.event.ActionEvent.class);
            method.setAccessible(true);
            method.invoke(iFrame, evt);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }//GEN-LAST:event_jButtonOActionPerformed

    private void jMenuItemSetIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSetIActionPerformed
        String base = "5";
        boolean bs = true;
        stepId.setText((mdi.prefsMdi.get("stepId", base)));
        startIdNum.setText((mdi.prefsMdi.get("startId", base)));
        IValueField.setText((mdi.prefsMdi.get("IValue", base)));
        NValueField.setText( (mdi.prefsMdi.get("NValue", base)));
        boxIsPlot.setSelected((mdi.prefsMdi.getBoolean("graphState", bs)));
        boxIsXES.setSelected((mdi.prefsMdi.getBoolean("xesState", bs)));
        boxLookAtO.setSelected((mdi.prefsMdi.getBoolean("oActiveState", bs)));
        xesNameFile.setText((mdi.prefsMdi.get("xesName", base)));
        valueFP.setText((mdi.prefsMdi.get("FPValue", base)));
                
        enabledChanger();

        SetConstantDialog.setModal(true);
        SetConstantDialog.setTitle("Simulation settings");
        SetConstantDialog.setLocationRelativeTo(null);
        SetConstantDialog.pack();
        SetConstantDialog.setVisible(true);        
    }//GEN-LAST:event_jMenuItemSetIActionPerformed

    private void Code_GenerationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Code_GenerationActionPerformed
        JInternalFrame iFrame = jDesktopPane.getSelectedFrame();        
        try {
            java.lang.reflect.Method method = iFrame.getClass().getDeclaredMethod("GenerateDescription");
            method.setAccessible(true);
            method.invoke(iFrame);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }//GEN-LAST:event_Code_GenerationActionPerformed

    private void OkIButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OkIButActionPerformed
        SetConstantDialog.setVisible(false);
        mdi.prefsMdi.put("IValue",  IValueField.getText());
        mdi.prefsMdi.put("NValue",  NValueField.getText());
        mdi.prefsMdi.put("FPValue",  valueFP.getText());
        mdi.prefsMdi.putBoolean("graphState",  boxIsPlot.isSelected());
        mdi.prefsMdi.putBoolean("xesState",  boxIsXES.isSelected());
        mdi.prefsMdi.putBoolean("oActiveState", boxLookAtO.isSelected());
        mdi.prefsMdi.put("startId",  startIdNum.getText());
        mdi.prefsMdi.put("stepId",  stepId.getText());
        mdi.prefsMdi.put("xesName", xesNameFile.getText());
        
    }//GEN-LAST:event_OkIButActionPerformed

    private void CancelIButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelIButActionPerformed
        SetConstantDialog.setVisible(false);        
    }//GEN-LAST:event_CancelIButActionPerformed
//Конец работы Иванова А.А.

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // Восстанавливаем исходные цвета
        GridPanel.color = originalGridColor;
        GridPanel.backgroundColor = originalBackgroundColor;
        S1.BackgroundColor = originalS1BackgroundColor;
        S1.TextColor = originalS1TextColor;
        V.BackgroundColor = originalVBackgroundColor;
        V.TextColor = originalVTextColor;
        NV.BackgroundColor = originalNVBackgroundColor;
        NV.TextColor = originalNVTextColor;
        R.BackgroundColor = originalRBackgroundColor;
        R.TextColor = originalRTextColor;
        O.BackgroundColor = originalOBackgroundColor;
        O.TextColor = originalOTextColor;
        d.BackgroundColor = originalIFBackgroundColor;
        d.TextColor = originalIFTextColor;
        GridPanel.isVisible = originalGridVisibility;

        // Закрываем диалог
        jDialogSettings.setVisible(false);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        jDialogSettings.setVisible(false);


        if (GridPanel.isVisible == true) {
            GridPanel.prefs.putInt("color", GridPanel.color.getRGB());
        }
        
        GridPanel.prefs.putBoolean("isVisible", GridPanel.isVisible);
        GridPanel.prefs.putInt("BackgroundColor",  GridPanel.backgroundColor.getRGB());

        figures.prefs.putInt("IFBackgroundColor", d.BackgroundColor.getRGB());
        
        figures.prefs.putInt("IFTextColor", d.TextColor.getRGB());
        figures.prefs.putInt("S1BackgroundColor", S1.BackgroundColor.getRGB());
        figures.prefs.putInt("S1TextColor", S1.TextColor.getRGB());
        figures.prefs.putInt("VBackgroundColor", V.BackgroundColor.getRGB());
        figures.prefs.putInt("VTextColor", V.TextColor.getRGB());
        figures.prefs.putInt("NVBackgroundColor", NV.BackgroundColor.getRGB());
        figures.prefs.putInt("NVTextColor", NV.TextColor.getRGB());
        figures.prefs.putInt("RBackgroundColor", R.BackgroundColor.getRGB());
        figures.prefs.putInt("RTextColor", R.TextColor.getRGB());
        figures.prefs.putInt("OBackgroundColor", O.BackgroundColor.getRGB());
        figures.prefs.putInt("OTextColor", O.TextColor.getRGB());
        
        
        
        jDesktopPane.repaint(); // Обновить отображение
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed

        
        GridPanel.backgroundColor = new Color(GridPanel.prefs.getInt("BackgroundColor", Color.GRAY.getRGB()));
        
        boolean isSelected = jCheckBox1.isSelected();

        // Активируем/деактивируем элементы
        jGridColor.setEnabled(isSelected);
        jChangeColorGrid.setEnabled(isSelected);

        // Сохраняйте состояние в Preferences, если нужно
        GridPanel.prefs.putBoolean("gridVisible", isSelected);
        
        //if (isSelected) {
            //GridPanel.isVisible = true;
            //GridPanel.prefs.putBoolean("isVisible", true);
     
            
            //GridPanel.color = new Color(GridPanel.prefs.getInt("color", Color.GRAY.getRGB()));
            //jChangeColorGrid.setEnabled(true);
        //} else {
            //GridPanel.isVisible = false;
            //GridPanel.prefs.putBoolean("isVisible", false);
          //  gridColor = GridPanel.color;
            //GridPanel.color = jDesktopPane.getSelectedFrame().getBackground();
            //jChangeColorGrid.setEnabled(false);
        //}
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    
    
    
    private void jChangeColorGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeColorGridActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Pick color", Color.BLACK);
        if (selectedColor != null) {
            GridPanel.color = selectedColor;
            jGridColor.setBackground(selectedColor);
        }    
    }//GEN-LAST:event_jChangeColorGridActionPerformed

    private void jChangeColorOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeColorOActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Выберите цвет", Color.BLACK);
        if (selectedColor != null) {
            O.BackgroundColor = selectedColor;
            jOColor.setBackground(selectedColor);
        //figures.prefs.putInt("S1BackgroundColor", selectedColor.getRGB());
        }
    }//GEN-LAST:event_jChangeColorOActionPerformed

    private void jChangeColorIFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeColorIFActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Pick color", Color.BLACK);
        if (selectedColor != null) {
            d.BackgroundColor = selectedColor;
            jIFColor.setBackground(selectedColor);
        }    
    }//GEN-LAST:event_jChangeColorIFActionPerformed

    private void jChangeColorRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeColorRActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Pick color", Color.BLACK);
        if (selectedColor != null) {
            R.BackgroundColor = selectedColor;
            jRColor.setBackground(selectedColor);
        }    
    }//GEN-LAST:event_jChangeColorRActionPerformed

    private void jChangeColorNVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeColorNVActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Pick color", Color.BLACK);
        if (selectedColor != null) {
            NV.BackgroundColor = selectedColor;
            jNVColor.setBackground(selectedColor);
        }    
    }//GEN-LAST:event_jChangeColorNVActionPerformed

    private void jChangeColorVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeColorVActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Pick color", Color.BLACK);
        if (selectedColor != null) {
            V.BackgroundColor = selectedColor;
            jVColor.setBackground(selectedColor);
        }    
    }//GEN-LAST:event_jChangeColorVActionPerformed

    private void jChangeColorSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeColorSActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Выберите цвет", Color.BLACK);
        if (selectedColor != null) {
            S1.BackgroundColor = selectedColor;
            jSColor.setBackground(selectedColor);
            //figures.prefs.putInt("S1BackgroundColor", selectedColor.getRGB());
        }

        // TODO add your handling code here:
    }//GEN-LAST:event_jChangeColorSActionPerformed

    private void jChangeTextColorOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeTextColorOActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Выберите цвет", Color.BLACK);
        if (selectedColor != null) {
            O.TextColor = selectedColor;
            jOTextColor.setBackground(selectedColor);
        }    
        //figures.prefs.putInt("IFTextColor", selectedColor.getRGB());
        // jDesktopPane.repaint();
    }//GEN-LAST:event_jChangeTextColorOActionPerformed

    private void jChangeTextColorIFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeTextColorIFActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Выберите цвет", Color.BLACK);
        if (selectedColor != null) {
            d.TextColor = selectedColor;
            jIFTextColor.setBackground(selectedColor);
        }    
        //figures.prefs.putInt("IFTextColor", selectedColor.getRGB());
        // jDesktopPane.repaint();
    }//GEN-LAST:event_jChangeTextColorIFActionPerformed

    private void jChangeTextColorRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeTextColorRActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Выберите цвет", Color.BLACK);
        if (selectedColor != null) {
            R.TextColor = selectedColor;
            jRTextColor.setBackground(selectedColor);
        }    
        //figures.prefs.putInt("RTextColor", selectedColor.getRGB());
        //jDesktopPane.repaint();
    }//GEN-LAST:event_jChangeTextColorRActionPerformed

    private void jChangeTextColorNVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeTextColorNVActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Выберите цвет", Color.BLACK);
        if (selectedColor != null) {
            NV.TextColor = selectedColor;
            jNVTextColor.setBackground(selectedColor);
        }    
        //figures.prefs.putInt("NVTextColor", selectedColor.getRGB());
        //jDesktopPane.repaint();
    }//GEN-LAST:event_jChangeTextColorNVActionPerformed

    private void jChangeTextColorVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeTextColorVActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Выберите цвет", Color.BLACK);
        if (selectedColor != null) {
            V.TextColor = selectedColor;
            jVTextColor.setBackground(selectedColor);
        }    
        //figures.prefs.putInt("VTextColor", selectedColor.getRGB());
        //jDesktopPane.repaint();
    }//GEN-LAST:event_jChangeTextColorVActionPerformed

    private void jChangeTextColorSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeTextColorSActionPerformed
        Color selectedColor = jColorChooser1.showDialog(this, "Выберите цвет", Color.BLACK);
        if (selectedColor != null) {
            S1.TextColor = selectedColor;
            jSTextColor.setBackground(selectedColor);
        }    
        //figures.prefs.putInt("S1TextColor", selectedColor.getRGB());
        //jDesktopPane.repaint();
    }//GEN-LAST:event_jChangeTextColorSActionPerformed

    private void ProductivityAnalyzesStaticMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ProductivityAnalyzesStaticMenuItemActionPerformed
        try {
            var dialog = new ChoiceMethodPageRankDialog(this, true, true, (jMDIFrame)(jDesktopPane.getSelectedFrame()));
        } catch (Exception ex) {
                ex.printStackTrace();
        }
             
    }//GEN-LAST:event_ProductivityAnalyzesStaticMenuItemActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        String text = ((jMDIFrame)(jDesktopPane.getSelectedFrame())).СyclomaticСomplexity() + "";
        CyclomaticComplexityValue.setText(text);
        CyclomaticComplexityDialog.setSize(250, 90);
        CyclomaticComplexityDialog.setLocation(this.getWidth()/2-CyclomaticComplexityDialog.getWidth()/2, this.getHeight()/2-CyclomaticComplexityDialog.getHeight()/2);
        CyclomaticComplexityDialog.show();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void ProductivityAnalyzesDynamicMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ProductivityAnalyzesDynamicMenuItemActionPerformed
        try {
            var dialog = new ChoiceMethodPageRankDialog(this, true, false, null);
        } catch (Exception ex) {
                ex.printStackTrace();
        }        
    }//GEN-LAST:event_ProductivityAnalyzesDynamicMenuItemActionPerformed

    private void ToolsMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_ToolsMenuSelected
        jMenuItem2.setEnabled(jDesktopPane.getSelectedFrame() != null);
    }//GEN-LAST:event_ToolsMenuSelected

    private void ProductivityAnalyzesMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_ProductivityAnalyzesMenuMenuSelected
        ProductivityAnalyzesStaticMenuItem.setEnabled(jDesktopPane.getSelectedFrame() != null);
    }//GEN-LAST:event_ProductivityAnalyzesMenuMenuSelected

    private void ExportPNGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExportPNGActionPerformed


        // Выбираем место сохранения файла
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save as PNG");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
    
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
        
            // Добавляем расширение .png, если пользователь не указал
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }
            JInternalFrame iFrame = jDesktopPane.getSelectedFrame();
            java.lang.reflect.Method method;
            try {
                method = iFrame.getClass().getDeclaredMethod("exportToPNG", String.class);
                method.setAccessible(true);     
                method.invoke(iFrame, file.getAbsolutePath());            
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(mdi.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }//GEN-LAST:event_ExportPNGActionPerformed

    private void jChangeColorBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeColorBackgroundActionPerformed
        
        
        Color selectedColor = jColorChooser1.showDialog(this, "Pick color", Color.BLACK);
        if (selectedColor != null) {
            GridPanel.backgroundColor = selectedColor;
            jBackgroundColor.setBackground(selectedColor);
        }
    }//GEN-LAST:event_jChangeColorBackgroundActionPerformed

    private void jMenuUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuUndoActionPerformed
        JInternalFrame selectedFrame = jDesktopPane.getSelectedFrame();

        if (selectedFrame != null && selectedFrame instanceof jMDIFrame) {
            jMDIFrame mdiFrame = (jMDIFrame) selectedFrame;
            mdiFrame.undo(); //  Вызов метода undo()
        }    
        
    }//GEN-LAST:event_jMenuUndoActionPerformed

    private void boxLookAtOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxLookAtOActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_boxLookAtOActionPerformed

    private void boxIsXESPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_boxIsXESPropertyChange
        enabledChanger();
    }//GEN-LAST:event_boxIsXESPropertyChange

    private void boxIsXESActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxIsXESActionPerformed
        enabledChanger();
    }//GEN-LAST:event_boxIsXESActionPerformed

    private void boxIsPlotPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_boxIsPlotPropertyChange
        enabledChanger();
    }//GEN-LAST:event_boxIsPlotPropertyChange

    private void boxIsPlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxIsPlotActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_boxIsPlotActionPerformed

    private void valueFPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueFPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_valueFPActionPerformed

    private void stepIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stepIdActionPerformed

    private void startIdNumActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startIdNumActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_startIdNumActionPerformed

    private void NValueFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NValueFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NValueFieldActionPerformed

    private void IValueFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IValueFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_IValueFieldActionPerformed


         
    public void enabledChanger(){ 
        boolean isEnabled = boxIsXES.isSelected();
        xesNameFile.setEnabled(isEnabled);
        jLabel7.setEnabled(isEnabled);
    }
    public static void main(String args[]) throws Exception{
        java.awt.EventQueue.invokeLater(new RunnableImpl());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem About;
    private javax.swing.JButton CancelIBut;
    private javax.swing.JMenuItem Cascade;
    public static javax.swing.JMenuItem Code_Generation;
    private javax.swing.JDialog CyclomaticComplexityDialog;
    private javax.swing.JLabel CyclomaticComplexityValue;
    public static javax.swing.JMenuItem ExportPNG;
    private javax.swing.JFormattedTextField IValueField;
    private javax.swing.JOptionPane Info;
    private javax.swing.JFormattedTextField NValueField;
    private javax.swing.JMenuItem New;
    private javax.swing.JButton OkIBut;
    private javax.swing.JMenuItem Open;
    private javax.swing.JFileChooser OpenChooser;
    private javax.swing.JMenuItem ProductivityAnalyzesDynamicMenuItem;
    private javax.swing.JMenu ProductivityAnalyzesMenu;
    private javax.swing.JMenuItem ProductivityAnalyzesStaticMenuItem;
    public static javax.swing.JMenuItem Save;
    private javax.swing.JFileChooser SaveChooser;
    public static javax.swing.JMenuItem Saveas;
    private javax.swing.JDialog SetConstantDialog;
    private javax.swing.JMenuItem Tile;
    private javax.swing.JMenu Tools;
    private javax.swing.JCheckBox boxIsPlot;
    private javax.swing.JCheckBox boxIsXES;
    private javax.swing.JCheckBox boxLookAtO;
    private javax.swing.JLabel idNumLabel;
    private javax.swing.JPanel jBackgroundColor;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    public static javax.swing.JButton jButtonIF;
    public static javax.swing.JButton jButtonNV;
    private javax.swing.JButton jButtonNew;
    public static javax.swing.JButton jButtonO;
    private javax.swing.JButton jButtonOpen;
    public static javax.swing.JButton jButtonR;
    public static javax.swing.JButton jButtonS;
    public static javax.swing.JButton jButtonSave;
    public static javax.swing.JButton jButtonV;
    private javax.swing.JButton jChangeColorBackground;
    private javax.swing.JButton jChangeColorGrid;
    private javax.swing.JButton jChangeColorIF;
    private javax.swing.JButton jChangeColorNV;
    private javax.swing.JButton jChangeColorO;
    private javax.swing.JButton jChangeColorR;
    private javax.swing.JButton jChangeColorS;
    private javax.swing.JButton jChangeColorV;
    private javax.swing.JButton jChangeTextColorIF;
    private javax.swing.JButton jChangeTextColorNV;
    private javax.swing.JButton jChangeTextColorO;
    private javax.swing.JButton jChangeTextColorR;
    private javax.swing.JButton jChangeTextColorS;
    private javax.swing.JButton jChangeTextColorV;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JColorChooser jColorChooser1;
    private javax.swing.JDesktopPane jDesktopPane;
    private javax.swing.JDialog jDialogSettings;
    public static javax.swing.JMenu jExport;
    private javax.swing.JPanel jGridColor;
    private javax.swing.JPanel jIFColor;
    private javax.swing.JPanel jIFTextColor;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenuItem jMenuItem2;
    public static javax.swing.JMenuItem jMenuItemClear;
    public static javax.swing.JMenuItem jMenuItemColorSettings;
    public static javax.swing.JMenuItem jMenuItemIF;
    public static javax.swing.JMenuItem jMenuItemNV;
    public static javax.swing.JMenuItem jMenuItemO;
    public static javax.swing.JMenuItem jMenuItemR;
    public static javax.swing.JMenuItem jMenuItemS;
    public static javax.swing.JMenuItem jMenuItemSetI;
    public static javax.swing.JMenuItem jMenuItemV;
    public static javax.swing.JMenuItem jMenuLegend;
    private javax.swing.JMenu jMenuOptions;
    public static javax.swing.JMenuItem jMenuUndo;
    private javax.swing.JPanel jNVColor;
    private javax.swing.JPanel jNVTextColor;
    private javax.swing.JPanel jOColor;
    private javax.swing.JPanel jOTextColor;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanelBackground;
    private javax.swing.JPanel jRColor;
    private javax.swing.JPanel jRTextColor;
    private javax.swing.JPanel jSColor;
    private javax.swing.JPanel jSTextColor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel jVColor;
    private javax.swing.JPanel jVTextColor;
    private javax.swing.JMenuItem quit;
    private javax.swing.JFileChooser rFileSaveChooser;
    private javax.swing.JFormattedTextField startIdNum;
    private javax.swing.JFormattedTextField stepId;
    private javax.swing.JFormattedTextField valueFP;
    private javax.swing.JTextField xesNameFile;
    // End of variables declaration//GEN-END:variables
    
    private static class RunnableImpl implements Runnable {

        public RunnableImpl() {
        }
        
//        DatabaseHandler dbHandler = new DatabaseHandler();

        @Override
        public void run() {
            
            new mdi().setVisible(true);
        }
    }
    
    
    
    private void updateDesktopPaneSize() {
        if (jDesktopPane.getAllFrames().length == 0) {
            // Если нет окон, сбрасываем предпочтительный размер
            jDesktopPane.setPreferredSize(null);
        } else {
            // Вычисляем общие границы всех фреймов
            Rectangle totalBounds = new Rectangle();
            for (JInternalFrame frame : jDesktopPane.getAllFrames()) {
                totalBounds = totalBounds.union(frame.getBounds());
            }
            // Добавляем отступы
            int padding = 50;
            int newWidth = totalBounds.width + totalBounds.x + padding;
            int newHeight = totalBounds.height + totalBounds.y + padding;
            jDesktopPane.setPreferredSize(new Dimension(newWidth, newHeight));
        }
        jDesktopPane.revalidate();
    }

    
    
}


