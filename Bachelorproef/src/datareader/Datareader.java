package datareader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class Datareader {
	
	private int[][] dist;
	private int[][] opp;
	
	public Datareader() {
				
	}
	
	public int[][] getDist() {
		return dist;
	}
	
	public int[][] getOpp() {
		return opp;
	}
	
	/**
	 * Haal de data uit de gegeven file
	 * 
	 * @param	file
	 * 			De directory van de file
	 * 			bvb. C:\\Users\\Kenneth\\Desktop\\dataset.txt
	 * @throws IOException 
	 */
	public void getData(String file) throws IOException {
		
		try {		
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine().trim();
			int n = Integer.parseInt(line.substring(7,line.length()-1));
			dist = new int[n][n];
			
			// Zoek de distance matrix
			while(line.equals("") || !line.substring(0,4).equals("dist")) {
				line = br.readLine();
			}
			
			// Lees de matrix in
			for(int i = 0; i<n; i++) {
				line = br.readLine().trim();
				line = line.substring(1,line.length()-1).trim();
				String[] numbers = line.split("\\s+");
				
				for(int j = 0; j<n; j++) {
					dist[i][j] = Integer.parseInt(numbers[j]);
				}
			}
			
			// Zoek de opponents matrix
			while(line.equals("") || !line.substring(0,2).equals("op")) {
				line = br.readLine();
			}
			
			// Lees de matrix in
			int m = (4*n/2)-2;
			opp = new int[m][n];
			
			for(int i = 0; i<m; i++) {
				line = br.readLine().trim();
				line = line.substring(1,line.length()-1).trim();
				String[] numbers = line.split("\\s+");
				
				for(int j = 0; j<n; j++) {
					opp[i][j] = Integer.parseInt(numbers[j]);
				}
			}
				
		} catch(FileNotFoundException e) {
			System.out.println("Bestand niet gevonden.");
		}
	}
}
