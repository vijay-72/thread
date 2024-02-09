import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultiplication {

    public static void main(String[] args) {
        // Define matrix dimensions
        int rows = 1000;
        int cols = 1000;

        // Generate random matrices
        int[][] matrixA = generateRandomMatrix(rows, cols);
        int[][] matrixB = generateRandomMatrix(rows, cols);

        // Serial multiplication
        long startTimeSerial = System.nanoTime();
        int[][] resultSerial = multiplyMatricesSerial(matrixA, matrixB);
        long endTimeSerial = System.nanoTime();
        long serialTimeTaken = endTimeSerial - startTimeSerial;

        // Parallel multiplication
        long startTimeParallel = System.nanoTime();
        int[][] resultParallel = multiplyMatricesParallel(matrixA, matrixB);
        long endTimeParallel = System.nanoTime();
        long parallelTimeTaken = endTimeParallel - startTimeParallel;

        // Calculate speedup factor
        double speedupFactor = (double) serialTimeTaken / parallelTimeTaken;

        // Check if results are equal
        boolean resultsEqual = areMatricesEqual(resultSerial, resultParallel);

        // Print results
        System.out.println("Serial multiplication time: " + serialTimeTaken + " nanoseconds");
        System.out.println("Parallel multiplication time: " + parallelTimeTaken + " nanoseconds");
        System.out.println("Speedup factor: " + speedupFactor);
        System.out.println("Results are equal: " + resultsEqual);
    }

    // Method to generate a random matrix of given dimensions
    private static int[][] generateRandomMatrix(int rows, int cols) {
        int[][] matrix = new int[rows][cols];
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextInt(10); // Random integers from 0 to 9
            }
        }
        return matrix;
    }

    // Serial matrix multiplication
    private static int[][] multiplyMatricesSerial(int[][] matrixA, int[][] matrixB) {
        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int colsB = matrixB[0].length;

        int[][] result = new int[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }

        return result;
    }

    // Parallel matrix multiplication
    private static int[][] multiplyMatricesParallel(int[][] matrixA, int[][] matrixB) {
        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int colsB = matrixB[0].length;

        int[][] result = new int[rowsA][colsB];

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < rowsA; i++) {
            executor.execute(new RowMultiplier(matrixA, matrixB, result, i));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    // Method to check if two matrices are equal
    private static boolean areMatricesEqual(int[][] matrixA, int[][] matrixB) {
        int rows = matrixA.length;
        int cols = matrixA[0].length;

        if (matrixB.length != rows || matrixB[0].length != cols) {
            return false;
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    // Runnable class to perform row-wise matrix multiplication
    private static class RowMultiplier implements Runnable {
        private final int[][] matrixA;
        private final int[][] matrixB;
        private final int[][] result;
        private final int row;

        public RowMultiplier(int[][] matrixA, int[][] matrixB, int[][] result, int row) {
            this.matrixA = matrixA;
            this.matrixB = matrixB;
            this.result = result;
            this.row = row;
        }

        @Override
        public void run() {
            int colsA = matrixA[0].length;
            int colsB = matrixB[0].length;

            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[row][j] += matrixA[row][k] * matrixB[k][j];
                }
            }
        }
    }
}