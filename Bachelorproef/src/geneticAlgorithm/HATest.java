package geneticAlgorithm;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class HATest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testNextSol() {
		
		double[] cursol = {0, 0, 0, 0};
		int i = 0;
		while(HungarianAlgorithm.hasNextSol(cursol, 4, 4)) {
			cursol = HungarianAlgorithm.nextSol(cursol, 4, 4);
			for(int j=0; j<cursol.length; j++) {
				System.out.print(cursol[j]+" ");
			}
			System.out.println("");
			i++;
		}
	}

}
