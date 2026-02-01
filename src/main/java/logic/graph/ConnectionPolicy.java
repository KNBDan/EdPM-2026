package logic.graph;

import objects.line.Line;
import objects.figure.NV;
import objects.figure.O;
import objects.figure.R;
import objects.figure.S1;
import objects.figure.V;
import objects.figure.d;
import objects.figure.figures;
import java.util.List;

public class ConnectionPolicy {

    public boolean canConnect(figures first, figures second, List<Line> lines, List<figures> all) {
        if (first == null || second == null || lines == null || lines.isEmpty() || all == null) {
            return false;
        }
        int cc = 0;// счетчик повторных соединений
        for (Line ln : lines) {
            if ((lines.get(0).getID1() == ln.getID1()) && (lines.get(0).getID2() == ln.getID2())) {
                cc += 1;
            }
        }

        return (((first.getClass().equals(NV.class) && second.getClass().equals(V.class) && findLinkedFigToFig("NV", second.getNameF(), lines, all))
                || // связь NV -> V
                (first.getClass().equals(S1.class) && second.getClass().equals(V.class) && findLinkedFigToFig("S1", second.getNameF(), lines, all))
                || // связь S -> V
                (first.getClass().equals(V.class) && second.getClass().equals(R.class) && findLinkedFigToFig("V", second.getNameF(), lines, all))
                || // связь V -> R
                (first.getClass().equals(R.class) && second.getClass().equals(NV.class))
                || // связь R -> NV
                (first.getClass().equals(R.class) && second.getClass().equals(V.class) && findLinkedFigToFig("R", second.getNameF(), lines, all))
                || // связь R -> V
                (first.getClass().equals(R.class) && second.getClass().equals(d.class))
                || // связь R -> IF
                (first.getClass().equals(O.class) && second.getClass().equals(V.class) && findLinkedFigToFig("O", second.getNameF(), lines, all))
                ||// связь O -> V
                (first.getClass().equals(d.class))) // связь IF
                && (cc == 1)// проверяем повторные соединения
        );
    }

    private boolean findLinkedFigToFig(String startShape, String endName, List<Line> lines, List<figures> all) {
        boolean isFirst = true;
        for (Line ln : lines) {
            if (ln.getID2().equals(endName)) {
                for (figures fig : all) {
                    if (ln.getID1().equals(fig.getNameF())) {
                        if (fig.getClass().toString().replace("class objects.figure.", "").equals(startShape)) {
                            if (isFirst) {
                                isFirst = false;
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("3");
        return true;
    }
}
