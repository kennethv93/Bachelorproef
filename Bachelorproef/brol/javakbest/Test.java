package com.google.code.javakbest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author David Miguel Antunes <davidmiguel [ at ] antunes.net>
 */
public class Test {

    public static void main(String[] args) throws FileNotFoundException, IOException {
    	
    	for(int i=0;i<10;++i) {
    		System.out.println(i);
    	}
    	
//    	double[][] costMat =
//    		{
//    				{90, 75, 75, 80},
//    				{35, 85, 55, 65},
//    				{125, 95, 90, 105},
//    				{45, 110, 95, 115}
//    		};
//
//        System.out.println("Cost matrix:");
//        for (int l = 0; l < costMat.length; l++) {
//            for (int c = 0; c < costMat[0].length; c++) {
//                System.out.format("%04f\t", costMat[l][c]);
//            }
//            System.out.println("");
//        }
//
//        long start = System.currentTimeMillis();
//        List<int[]> rowsols = Murty.solve(costMat, 6);
//        System.out.println("Took " + (System.currentTimeMillis() - start) + " millis");
//
//        System.out.println("");
//
//        int sol = 0;
//        for (int[] solution : rowsols) {
//            String s = "";
//            double cost = 0;
//            for (int i = 0; i < solution.length; i++) {
//                s += (solution[i] + "\t");
//                cost += costMat[i][solution[i]];
//            }
//            System.out.println("Solution " + (sol++) + " (" + cost + "):");
//            System.out.println(s);
//        }
//    }
//
//    private static void testDataset(String path) throws FileNotFoundException, IOException {
//
//        for (int i = 0; i < 10; i++) {
//            String file = path + "mat" + String.format("%03d", i) + ".m";
//            String s;
//            BufferedReader br = new BufferedReader(new FileReader(file));
//            String line;
//            s = br.readLine();
//            int nLines = 1;
//            int nCols = s.split("\\t").length;
//            while ((line = br.readLine()) != null) {
//                s += line;
//                nLines++;
//            }
//
//            String[] cells = s.split("\\t");
//
//            double[][] costMat = new double[nLines][nCols];
//
//            for (int j = 0; j < cells.length; j++) {
//                costMat[j / nCols][j % nCols] = Double.parseDouble(cells[j]);
//            }
//
//            String msg = "";
//            msg += "== cost matrix ==" + "\n";
//
//            for (int l = 0; l < costMat.length; l++) {
//                for (int c = 0; c < costMat[0].length; c++) {
//                    msg += costMat[l][c] + "\t";
//                }
//                msg += "\n";
//            }
//
////        System.out.println(msg);
//
//            List<int[]> rowsols = Murty.solve(costMat, 5);
//
//            for (int[] rowsol : rowsols) {
//                System.out.print(i + ": ");
//                for (int j = 0; j < rowsol.length; j++) {
//                    int col = rowsol[j];
//                    System.out.print((col + 1) + " ");
//                }
//                System.out.println("");
//            }
//
////            System.out.format("Cost=%f", cost);
//
//        }
    }
}
