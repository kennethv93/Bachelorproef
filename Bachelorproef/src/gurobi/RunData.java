package gurobi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RunData {
	
	/**
	 * PARAMETERS
	 */
	static boolean localBranching = false;
	static boolean printTable = true;
//	static int d1 = 0;
//	static int d2 = 0;
	static int[] penalties = {60};
//	static String[] kak = {"26"};//,"14a","14a","14a","14b","14b","14b","14c","14c","14c"};
//	static int[] d1 = {8};//,7,6,5,7,6,5,7,6,5};
//	static int[] d2 = {1};//,3,3,3,3,3,3,3,3,3};
	
	// LB
	static int k = 20;
	static int LBTimeLimit = 6000;

	//static String[] datasets = {"14","14a","14b","14c"};
	//	"16", "16a", "16b", "16c", "18", "20", "22","24","26","28","30","32"};
	//static String[] datasets = {"30"};
	static String[] kak = {"32"};
	static int[] d1 = {11};
	static int[] d2 = {3};
	
	public static void main(String[] args) {
		ArrayList<ArrayList<int[]>> solution = null;
		try {
			for(int z=0;z<=0;z++) { 
				for(int i=0; i<penalties.length;i++) {
					File file;
					if(localBranching) {
						file = new File("C:\\Users\\Kenneth\\Desktop\\ResultsForPaper\\"+kak[z]+"_"+d1[z]+"_"+d2[z]+"LB.txt");
					} else {
						file = new File("C:\\Users\\Kenneth\\Desktop\\ResultsForPaper\\"+kak[z]+"-"+d1[z]+","+d2[z]+" pen "+penalties[i]+".txt");
					}
					if(file.exists()) file.delete();
					if(!file.exists()) file.createNewFile();
					FileWriter fw = new FileWriter(file.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					
					//Kies voor welke windowsizes er gerund moet worden
						// voor alle windowsizes: TUPWindows.parseIntDataset(s)*2-2
						for(int w = 6; w<=6; w++) {
							System.out.println(w);
							if(w!=TUPWindows.parseIntDataset(kak[z])*2-2) {
									int n1 = (TUPWindows.parseIntDataset(kak[z])/2)-d1[z];
									int n2 = (int) ((Math.floor(TUPWindows.parseIntDataset(kak[z])/4))-d2[z]);
									System.out.println(n1); System.out.println(n2);
									try {
										solution = TUPWindows.getTableSolDecomp(kak[z],n1,n2, w,true,penalties[i],localBranching,k,LBTimeLimit);
										TUPWindows.writeSolution(bw,TUPWindows.parseIntDataset(kak[z]),solution,w,n1,n2,printTable);
										TUPWindows.relaxed = false;
										TUPWindows.optimized = false;
									} catch (GRBException | NullPointerException e) {
										TUPWindows.writeSolution(bw,TUPWindows.parseIntDataset(kak[z]),w,printTable);
										TUPWindows.relaxed = false;
										TUPWindows.optimized = false;
									}
								
							}
							
						}
					bw.close();
				}
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("FINISHED RUNNING.");
	}

}
