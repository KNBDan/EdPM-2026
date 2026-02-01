//КОД для теоритического окна, не использщуется
package jMDIForm;
import objects.figure.figures;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FigurePropertiesDialog extends JDialog {
    private JTextField xField = new JTextField(10);
    private JTextField yField = new JTextField(10);
    private JTextField sizeField = new JTextField(10);

    private figures selectedFigure;

    public FigurePropertiesDialog(jMDIFrame parent, figures selectedFigure) {
        //super(parent, "Настройки фигуры", true);

        this.selectedFigure = selectedFigure;

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("X:"));
        panel.add(xField);
        panel.add(new JLabel("Y:"));
        panel.add(yField);
        panel.add(new JLabel("Размер:"));
        panel.add(sizeField);

        xField.setText(String.valueOf(selectedFigure.getXX()));
        yField.setText(String.valueOf(selectedFigure.getYY()));
        sizeField.setText(String.valueOf(selectedFigure.getS()));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFigureProperties();
                dispose();
            }
        });

        panel.add(okButton);

        add(panel);
        pack();
        setLocationRelativeTo(parent);
    }

    private void updateFigureProperties() {
        try {
            int newX = Integer.parseInt(xField.getText());
            int newY = Integer.parseInt(yField.getText());
            int newSize = Integer.parseInt(sizeField.getText());

            selectedFigure.setXX(newX);
            selectedFigure.setYY(newY);
            selectedFigure.setS(newSize);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, введите корректные значения.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}