package legacy.misc;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JPanel;

public class CustomVerticalScrollbar extends Scrollbar {
    private JPanel panelToScroll;

    public CustomVerticalScrollbar(JPanel panelToScroll) {
        this.panelToScroll = panelToScroll;
        this.setOrientation(Scrollbar.VERTICAL);
        this.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                panelToScroll.repaint();
                panelToScroll.scrollRectToVisible(new Rectangle(panelToScroll.getWidth(), e.getValue(), 1, panelToScroll.getHeight()));
            }
        });
    }
}
