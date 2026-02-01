package logic.pagerank;

import objects.line.Line;
import objects.figure.figures;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageRankService {

    public static class PageRankResult {
        private final double[][] matrix;
        private final String[] names;

        public PageRankResult(double[][] matrix, String[] names) {
            this.matrix = matrix;
            this.names = names;
        }

        public double[][] getMatrix() {
            return matrix;
        }

        public String[] getNames() {
            return names;
        }
    }

    public PageRankResult pageRank(List<figures> all, List<Line> lines, int iteration, double delta, double d) {
        ArrayList<figures> ordered = new ArrayList<>(all);
        ordered.sort(Comparator.comparingInt(figures::getId).thenComparing(figures::getNameF));

        double[][] matrix = new double[ordered.size()][ordered.size()];
        int size = matrix.length;
        for (int column = 0; column < size; column++) {
            for (int row = 0; row < size; row++) {
                matrix[column][row] = 0;
            }
        }

        Map<String, Integer> nameToIndex = new HashMap<>();
        for (int i = 0; i < ordered.size(); i++) {
            nameToIndex.put(ordered.get(i).getNameF(), i);
        }

        for (Line line : lines) {
            Integer start = nameToIndex.get(line.getID1());
            Integer end = nameToIndex.get(line.getID2());
            if (start != null && end != null) {
                matrix[start][end] = 1;
            }
        }

        ArrayList<figures> fig = new ArrayList<>(ordered);
        String[] names = new String[size];
        for (int i = 0; i < size; i++) {
            names[i] = fig.get(i).getNameF();
        }
        boolean flag;
        do {
            flag = false;
            for (int nv = 0; nv < size; nv++) {
                if (nv >= fig.size() || !fig.get(nv).getNameF().startsWith("NV")) continue;
                for (int v = 0; v < size; v++) {
                    if (v >= fig.size() || matrix[nv][v] != 1.0 || !fig.get(v).getNameF().startsWith("V")) continue;
                    for (int r = 0; r < size; r++) {
                        if (r >= fig.size() || matrix[v][r] != 1.0 || !fig.get(r).getNameF().startsWith("R")) continue;
                        matrix = MergeMatrix(matrix, r, nv, v);

                        String[] newNames = new String[size - 2];
                        int counter = 0;

                        for (int i = 0; i < size; i++) {
                            if (i == v || i == r) continue;
                            if (i == nv) {
                                newNames[counter] = names[nv] + "->" + names[v] + "->" + names[r];
                            } else {
                                newNames[counter] = names[i];
                            }
                            counter++;
                        }
                        names = newNames;

                        if (v > nv) {
                            fig.remove(v);
                            fig.remove(nv);
                        } else {
                            fig.remove(nv);
                            fig.remove(v);
                        }
                        size -= 2;
                        flag = true;
                        break;
                    }
                    if (flag) break;
                }
                if (flag) break;
            }
        } while (flag);

        for (int row = 0; row < size; row++) {
            int counter = 0;
            for (int column = 0; column < size; column++) {
                if (matrix[column][row] == 1)
                    counter++;
            }
            for (int column = 0; column < size; column++) {
                if (matrix[column][row] == 1)
                    matrix[column][row] = 1.0 / counter;
            }
        }

        for (int j = 0; j < size; j++) {
            int columnSum = 0;
            for (int i = 0; i < size; i++) {
                columnSum += matrix[i][j];
            }
            if (columnSum == 0) {
                for (int i = 0; i < size; i++) {
                    matrix[i][j] = 1.0 / size;
                }
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = d * matrix[i][j] + (1 - d) / size;
            }
        }

        double[][] pageRank;
        if (iteration > 0)
            pageRank = MultiplyMatrixIteration(matrix, iteration);
        else
            pageRank = MultiplyMatrixUntilDelta(matrix, delta);

        return new PageRankResult(pageRank, names);
    }

    protected double[][] MergeMatrix(double[][] matrix, int row1, int row2, int row3) {
        int n = matrix.length;
        double[][] newMatrix = new double[n - 2][n - 2];

        for (int i = 0; i < n; i++) {
            if (i != row1) {
                matrix[i][row1] = Math.max(matrix[i][row1], Math.max(matrix[i][row2], matrix[i][row3]));
                matrix[i][row2] = Math.max(matrix[i][row1], Math.max(matrix[i][row2], matrix[i][row3]));
                matrix[i][row3] = Math.max(matrix[i][row1], Math.max(matrix[i][row2], matrix[i][row3]));
            }
        }

        for (int j = 0; j < n; j++) {
            if (j != row1 && j != row2 && j != row3) {
                matrix[row1][j] = Math.max(matrix[row1][j], Math.max(matrix[row2][j], matrix[row3][j]));
                matrix[row2][j] = Math.max(matrix[row1][j], Math.max(matrix[row2][j], matrix[row3][j]));
                matrix[row3][j] = Math.max(matrix[row1][j], Math.max(matrix[row2][j], matrix[row3][j]));
            }
        }

        int newRow = 0;
        for (int i = 0; i < n; i++) {
            if (i == row2 || i == row3)
                continue;
            int newCol = 0;
            for (int j = 0; j < n; j++) {
                if (j == row2 || j == row3)
                    continue;
                newMatrix[newRow][newCol] = matrix[i][j];
                newCol++;
            }
            newRow++;
        }

        return newMatrix;
    }

    protected double[][] MultiplyMatrixIteration(double[][] matrix, int iteration) {
        int size = matrix.length;
        double[] vector = new double[size];
        for (int i = 0; i < size; i++)
            vector[i] = 1;
        double[] newVector = new double[size];
        double[][] pageRank = new double[iteration][size];

        for (int iter = 0; iter < iteration; iter++) {
            for (int i = 0; i < size; i++) {
                newVector[i] = 0;
                for (int j = 0; j < size; j++) {
                    newVector[i] += matrix[i][j] * vector[j];
                }
            }
            vector = newVector.clone();
            pageRank[iter] = vector.clone();
        }
        return pageRank;
    }

    protected double[][] MultiplyMatrixUntilDelta(double[][] matrix, double delta) {
        int size = matrix.length;
        double[] newVector = new double[size];
        double maxDifference;
        double[] vector = new double[size];
        for (int i = 0; i < size; i++)
            vector[i] = 1;

        List<double[]> history = new ArrayList<>();

        do {
            maxDifference = 0;

            for (int i = 0; i < size; i++) {
                newVector[i] = 0;
                for (int j = 0; j < size; j++) {
                    newVector[i] += matrix[i][j] * vector[j];
                }
                maxDifference = Math.max(maxDifference, Math.abs(newVector[i] - vector[i]));
            }

            vector = newVector.clone();
            history.add(vector.clone());

        } while (maxDifference >= delta);

        double[][] pageRank = new double[history.size()][size];
        for (int i = 0; i < history.size(); i++) {
            pageRank[i] = history.get(i);
        }

        return pageRank;
    }

    protected double Determinant(double[][] matrix) {
        double determinant = 0;
        if (matrix.length == 1)
            return matrix[0][0];
        else {
            for (int j = 0; j < matrix.length; j++) {
                double[][] minor_matrix = MatrixMinor(matrix, 0, j);
                determinant += matrix[0][j] * Math.pow(-1, j) * Determinant(minor_matrix);
            }
            return determinant;
        }
    }

    protected double[][] MatrixMinor(double[][] matrix, int rowToDelete, int columnToDelete) {
        int size = matrix.length;
        double[][] minorMatrix = new double[size - 1][size - 1];

        int newRow = 0;
        for (int i = 0; i < size; i++) {
            if (i == rowToDelete) continue;

            int newCol = 0;
            for (int j = 0; j < size; j++) {
                if (j == columnToDelete) continue;

                minorMatrix[newRow][newCol] = matrix[i][j];
                newCol++;
            }
            newRow++;
        }
        return minorMatrix;
    }

    protected double[][] Transposition(double[][] matrix) {
        double[][] newMatrix = matrix;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                newMatrix[i][j] = matrix[j][i];
            }
        }

        return newMatrix;
    }

    protected double[][] InverseMatrix(double matrix[][], double determinant) {
        double[][] inverseMatrix = new double[matrix.length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                inverseMatrix[i][j] = Determinant(MatrixMinor(matrix, j, i)) / determinant;
            }
        }
        return inverseMatrix;
    }
}
