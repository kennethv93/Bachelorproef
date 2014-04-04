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
	static boolean printTable = true;
	static int d1 = 11;
	static int d2 = 3;

	//	static String[] datasets = {"4", "6", "6a", "6b", "6c", "8", "8a", "8b", "8c","10", "10a", "10b", "10c"};//,"12","14","14a","14b","14c",
	//	"16", "16a", "16b", "16c", "18", "20", "22","24","26","28","30","32"};
	static String[] datasets = {"32"};
	
	public static void main(String[] args) {
		ArrayList<ArrayList<int[]>> solution = null;
		try {
			for(String s : datasets) { 
				
				File file = new File("C:\\Users\\Kenneth\\Desktop\\resultaten\\"+s+".txt");
				if(file.exists()) file.delete();
				if(!file.exists()) file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				
				//Kies voor welke windowsizes er gerund moet worden
					// voor alle windowsizes: TUPWindows.parseIntDataset(s)*2-2
				for(int w = 2; w<=5; w++) {
					
//					try{ 
//						solution = TUPWindows.getTableSolDecomp(s, d1, d2, w,false);
//						TUPWindows.writeSolution(bw, TUPWindows.parseIntDataset(s), solution, w,d1,d2,false);
//					} catch (GRBException e) {
//						TUPWindows.writeSolution(bw,TUPWindows.parseIntDataset(s),w,false);
//					}
					if(w!=TUPWindows.parseIntDataset(s)*2-2) {
						try {
							solution = TUPWindows.getTableSolDecomp(s,d1,d2, w,true);
							TUPWindows.writeSolution(bw,TUPWindows.parseIntDataset(s),solution,w,d1,d2,printTable);
							TUPWindows.relaxed = false;
						} catch (GRBException | NullPointerException e) {
							TUPWindows.writeSolution(bw,TUPWindows.parseIntDataset(s),w,printTable);
							TUPWindows.relaxed = false;
						}
					}
					
				}
				bw.close();
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("FINISHED RUNNING.");
	}

}
