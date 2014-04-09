package simulatedAnnealing;

public class HATest {

	
	public static void main(String[] args) {

		int not = Integer.MAX_VALUE;
		double[][] kostMatrix = {{4,not,not,not},
								{not,not,not,not},
								{4,not,not,not},
								{not,not,not,not},
								{4,not,not,not},
								{4,not,not,not}};
		
		HungarianAlgorithm ha = new HungarianAlgorithm(kostMatrix);
		int[] kak = ha.execute();
		for(int k: kak) {
			System.out.println(k);
		}

	}

}
