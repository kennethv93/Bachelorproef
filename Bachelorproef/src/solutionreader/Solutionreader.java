package solutionreader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Solutionreader {
		
	/**
	 * Haal de table uit de gegeven file
	 * 
	 * @param	file
	 * 			De directory van de file
	 * 			bvb. C:\\Users\\Kenneth\\Desktop\\dataset.txt
	 * @throws IOException 
	 */
	public static ArrayList<ArrayList<int[]>> getSolTable(String file) throws IOException {
		ArrayList<ArrayList<int[]>> soltable = new ArrayList<>();
		try {		
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine().trim();
			while(!line.equals("-")) {
				ArrayList<int[]> umpire = new ArrayList<>();
				String[] matches = line.split(" ");
				for(String match : matches) {
					int[] imatch = new int[2];
					String[] teams = match.split(",");
					imatch[0] = Integer.parseInt(teams[0].substring(1));
					imatch[1] = Integer.parseInt(teams[1].substring(0, teams[1].length()-1));
					umpire.add(imatch);
				}
				soltable.add(umpire);
				line = br.readLine().trim();
			}
			br.close();
		} catch(FileNotFoundException e) {
			System.out.println("Bestand niet gevonden.");
		}
		return soltable;
	}
}
