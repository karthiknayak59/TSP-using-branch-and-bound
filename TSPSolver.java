import java.io.File;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
​
​
/**
 * Method to represent the node at any given point in the state space graph
 */
class Node {
    double cost;
    int vertex;
    double matrix[][];
    int level;
​
    public Node() {
    }
​
    public Node(double cost, int vertex, double[][] matrix, int level) {
        this.cost = cost;
        this.vertex = vertex;
        this.matrix = matrix;
        this.level = level;
    }
​
    public int getLevel() {
        return level;
    }
​
    public void setLevel(int level) {
        this.level = level;
    }
​
    public double getCost() {
        return cost;
    }
​
    public void setCost(double cost) {
        this.cost = cost;
    }
​
    public int getVertex() {
        return vertex;
    }
​
    public void setVertex(int vertex) {
        this.vertex = vertex;
    }
​
    public double[][] getMatrix() {
        return matrix;
    }
​
    public void setMatrix(double[][] matrix) {
        this.matrix = matrix;
    }
}
​
/**
 * Class definition for TSP - AI problem
 */
public class TSPSolver {
​
    /**
     * Method to extract the matrix from the file
     * @param folderName
     * @param fileName
     * @return
     * @throws Exception
     */
    private static double[][] getMatrixFromFile(String folderName, String fileName) throws Exception {
        double costMatrix[][] = null;
        try {
            Scanner input = new Scanner(new File(folderName + File.separator + fileName));
            int size = Integer.parseInt(input.nextLine().trim());
            costMatrix = new double[size][size];
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    if (input.hasNext()) {
                        double value = input.nextDouble();
                        costMatrix[i][j] = (value == 0.0 ? Double.MAX_VALUE : value);
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return costMatrix;
    }
​
    /**
     * Method to read all input files and solve the problem
     * @param folderName
     */
    private static void readAllInputFilesAndSolve(String folderName) {
        TravelingSalesmanProblem tsp = new TravelingSalesmanProblem();
        File folder = new File(folderName);
​
        for (final File fileEntry : folder.listFiles()) {
            tsp.bestCost = Double.MAX_VALUE;
            try {
                System.out.print(fileEntry.getName()+ " ");
                tsp.setCostMatrix(getMatrixFromFile(folderName, fileEntry.getName()));
                tsp.solve();
                System.out.println(" " + tsp.bestCost);
            } catch (OutOfMemoryError | Exception e) {
                System.out.println(tsp.bestCost);
            }
        }
    }
​
    public static void main(String[] args) {
        readAllInputFilesAndSolve(args[0]);
    }
}
​
/**
 * Class defining method to compute the minimum cost for the TSP problem
 */
class TravelingSalesmanProblem {
​
    double[][] costMatrix;
    double row[];
    double column[];
    double bestCost = Double.MAX_VALUE;
​
    public double[][] getCostMatrix() {
        return costMatrix;
    }
​
    public void setCostMatrix(double[][] costMatrix) {
        this.costMatrix = costMatrix;
    }
​
    /**
     * Method to reduce the rows of the cost matrix
     * @param matrix
     * @param row
     */
    public void rowReduction(double matrix[][], double row[]) {
        int N = matrix.length;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (matrix[i][j] < row[i])
                    row[i] = matrix[i][j];
            }
        }
​
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (matrix[i][j] != Double.MAX_VALUE && row[i] != Double.MAX_VALUE)
                    matrix[i][j] -= row[i];
            }
        }
    }
​
    /**
     * Method to reduce the column of the cost matrix
     * @param matrix
     * @param column
     */
    public void columnReduction(double matrix[][], double column[]) {
        Arrays.fill(column, Double.MAX_VALUE);
        int N = matrix.length;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (matrix[i][j] < column[j])
                    column[j] = matrix[i][j];
            }
        }
​
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (matrix[i][j] != Double.MAX_VALUE && column[j] != Double.MAX_VALUE)
                    matrix[i][j] -= column[j];
            }
        }
    }
​
    /**
     * Method to calculate cost of the child node
     * @param matrix
     * @return
     */
    public double calculateCost(double matrix[][]) {
        int N = matrix.length;
        double cost = 0;
​
        row = new double[N];
        Arrays.fill(row, Double.MAX_VALUE);
        rowReduction(matrix, row);
​
        column = new double[N];
        Arrays.fill(column, Double.MAX_VALUE);
        columnReduction(matrix, column);
​
        for (int i = 0; i < N; i++) {
​
            if (row[i] != Double.MAX_VALUE) {
                cost = cost + row[i];
            }
​
            if (column[i] != Double.MAX_VALUE) {
                cost = cost + column[i];
            }
        }
        row = null;
        column = null;
        return cost;
    }
​
​
    /**
     * Method to create a child node
     * @param parentMatrix
     * @param level
     * @param i
     * @param j
     * @param pathCost
     * @return
     */
    public Node createNode(double[][] parentMatrix, int level, int i, int j) {
        Node node = new Node();
        int N = parentMatrix.length;
        node.matrix = new double[N][N];
        node.matrix = Arrays.stream(parentMatrix).map(double[]::clone).toArray(double[][]::new);
​
        for (int k = 0; level != 0 && k < parentMatrix.length; k++) {
            node.matrix[i][k] = Double.MAX_VALUE;
            node.matrix[k][j] = Double.MAX_VALUE;
        }
​
        node.matrix[j][0] = Double.MAX_VALUE;
        node.vertex = j;
        node.level = level;
​
        return node;
    }
​
    /**
     * Method to solve the TSP problem
     * @return
     * @throws Exception
     */
    public double solve() throws Exception {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(node -> node.cost));
​
        int nodesGenerated = 1;
        int nodesPruned = 0;
        int N = costMatrix.length;
        Node root = createNode(costMatrix, 0, -1, 0);
        root.cost = calculateCost(root.matrix);
        queue.add(root);
​
        long startTimer = System.currentTimeMillis();
​
        while (!queue.isEmpty()) {
​
            Node minimum = queue.remove();
            int currentCity = minimum.vertex;
​
            try {
​
                if (minimum.level == N - 1 && bestCost > minimum.cost) {
                    bestCost = minimum.cost;
                }
​
                if (minimum.cost > bestCost) {
                    minimum = null;
                } else {
                    for (int destination = 0; destination < N; destination++) {
                        if (minimum.matrix[currentCity][destination] != Double.MAX_VALUE) {
                            Node child = createNode(minimum.matrix, minimum.level + 1, currentCity, destination);
                            child.cost = minimum.cost + minimum.matrix[currentCity][destination] +
                                    calculateCost(child.matrix);
​
                            queue.add(child);
                        }
                    }
                }
            } catch (OutOfMemoryError e) {
                if (minimum != null)
                    bestCost = minimum.cost;
                throw e;
            }
​
            if (System.currentTimeMillis() - startTimer > 900000) {
                if (minimum != null) {
                    return minimum.cost;
                }
            }
        }
        return bestCost;
    }
}
