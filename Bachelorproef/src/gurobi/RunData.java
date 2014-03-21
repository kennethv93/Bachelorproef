package gurobi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RunData {

	static String[] datasets = {"4", "6", "6a", "6b", "6c", "8", "8a", "8b", "8c", "10", "10a", "10b", "10c","12","14","14a","14b","14c", 
		"16", "16a", "16b", "16c", "18", "20", "22","24","26","28","30","32"};
	
	public static void main(String[] args) {
		try {
			for(String s : datasets) { 
				File file = new File("C:\\Users\\Kenneth\\Desktop\\resultaten\\"+s+".txt");
				
				if(!file.exists()) file.createNewFile();
				
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(kak);
				bw.close();
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
