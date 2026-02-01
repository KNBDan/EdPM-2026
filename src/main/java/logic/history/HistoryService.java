package logic.history;

import objects.line.Line;
import objects.figure.figures;
import java.util.List;
import java.util.Stack;
import jMDIForm.DiagramState;

public class HistoryService {
    private final Stack<DiagramState> history = new Stack<>();
    private final int maxHistory;

    public HistoryService(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public void saveState(List<figures> figures, List<Line> lines, int zoom) {
        if (history.size() >= maxHistory) {
            history.remove(0);
        }
        history.push(new DiagramState(new java.util.ArrayList<>(figures), new java.util.ArrayList<>(lines), zoom));
    }

    public DiagramState undo() {
        if (history.isEmpty()) {
            return null;
        }
        return history.pop();
    }

    public void clear() {
        history.clear();
    }
}
