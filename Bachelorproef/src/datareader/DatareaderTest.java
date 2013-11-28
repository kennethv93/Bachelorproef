package datareader;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class DatareaderTest {
	
	private Datareader dr;

	@Before
	public void setUp() throws Exception {
		dr = new Datareader();
	}

	@Test
	public void test() throws IOException {
		
		// vul hier nTeams in ---
		int nTeams = 4;      //|
		//------------------
		
		dr.getData("C:\\Users\\Kenneth\\Desktop\\dataset.txt");
		int[][] dist = dr.getDist();
		int[][] opp = dr.getOpp();
		for(int i = 0; i<nTeams; i++) {
			for(int j = 0; j<nTeams; j++) {
				System.out.print(dist[i][j]+" ");
			}
			System.out.println();
		}
		
		for(int i = 0; i<2*nTeams-2; i++) {
			for(int j = 0; j<nTeams; j++) {
				System.out.print(opp[i][j]+" ");
			}
			System.out.println();
		}
	}

}
