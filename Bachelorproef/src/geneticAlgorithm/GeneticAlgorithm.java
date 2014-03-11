package geneticAlgorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import datareader.Datareader;

public class GeneticAlgorithm {

	private static int[][] dist;
	private static int[][] opp;
	
	public static void main(String[] args) throws IOException {

		ArrayList<ArrayList<int[]>> games = getGames();
		for(ArrayList<int[]> tgames: games) {
			for(int[] kak: tgames) {
				System.out.println((games.indexOf(tgames)+1)+" "+kak[0]+" "+kak[1]);
			}
		}
		algorithm();
		
	}
	
	public static void algorithm() throws IOException {
		Datareader dr = new Datareader();
		dr.getData("C:\\Users\\Kenneth\\Desktop\\dataset.txt");
		dist = dr.getDist();
		opp = dr.getOpp();
		
		ArrayList<ArrayList<int[]>> games = getGames();
		HashMap<Integer,ArrayList<int[]>> initSol = getInitialSolution(0,0,games);
	}
	
	public static HashMap<Integer,ArrayList<int[]>> getInitialSolution(int d1, int d2, ArrayList<ArrayList<int[]>> games) {

		int minkost = Integer.MAX_VALUE;
		int optgame = 0;
		int amountSlots = games.size();
		int n = dist[0].length/2;
		int p1 = n-d1;
		int p2 = ((int) Math.floor(n/2))-d2;
		HashMap<Integer,ArrayList<int[]>> solution = new HashMap<Integer,ArrayList<int[]>>();
		
		// initieel, elke umpire wordt toegewezen+
		for(int i=0; i<n; i++) {
			solution.put(i, new ArrayList<int[]>());
			solution.get(i).add(games.get(0).get(i));
		}
		
		int slot = 1;
		double[][] costMatrix = new double[n][n];
		int[] loc = new int[n];
		int[] newloc = new int[n];
		while(slot<amountSlots) {
			ArrayList<int[]> gamesSlot = games.get(slot);
			for(int i=0;i<n;i++) {
				int currentLocation = solution.get(i).get(slot-1)[0];
				loc[i] = currentLocation;
				for(int j=0;j<n;j++) {
					int newLocation = gamesSlot.get(j)[0];
					newloc[j] = newLocation;
					costMatrix[i][j] = dist[currentLocation-1][newLocation-1];
				}
			}
			
			int[] bestSolution = HungarianAlgorithm.hgAlgorithm(costMatrix, "min");
			int[] sbSolution = HungarianAlgorithm.get2ndSolution();
			
			for(int i=0; i<bestSolution.length; i++) {
				System.out.print(bestSolution[i]+" ");
			}
			
			int[] game = new int[2];
			for(int i=0;i<n;i++) {
				game[0] = newloc[bestSolution[i]];
				game[1] = getOppGivenSlot(game[0],slot);
				solution.get(i).add(game);
			}
			slot++;
		}

		
		return null;
	}
	
	/**
	 * Zet de opponentsmatrix om in een datastructuur met de matchen per tijdsslot
	 * @throws IOException 
	 */
	public static ArrayList<ArrayList<int[]>> getGames() throws IOException {
		
		// duplicaat van het stuk in de algorithmmethode, moet verdwijnen
		Datareader dr = new Datareader();
		dr.getData("C:\\Users\\Kenneth\\Desktop\\dataset.txt");
		dist = dr.getDist();
		opp = dr.getOpp();
		//-----------------------------------------------------------------
		
		int amountTeams = dist[0].length;
		int amountSlots = 2*amountTeams-2;
		ArrayList<ArrayList<int[]>> games = new ArrayList<ArrayList<int[]>>();
		for(int i = 0; i<amountSlots; i++) {
			games.add(new ArrayList<int[]>());
		}
		
		for(int i = 0; i<amountSlots; i++) {
			for(int j = 0; j<amountTeams; j++) {
				if(opp[i][j] > 0) {
					int[] game = {j+1, opp[i][j]};
					if(!ArrayListContains(games.get(i),game)) {
						games.get(i).add(game);
					}
				} else {
					int[] game = {opp[i][j]*(-1), j+1};
					if(!ArrayListContains(games.get(i),game)) {
						games.get(i).add(game);
					}
				}
			}
		}
		
		return games;
	}
	
	/**
	 * Gegeven een ArrayList met integerarrays en een integerarray, controleer of de gegeven integerarray
	 * in de ArrayList zit.
	 */
	public static boolean ArrayListContains(ArrayList<int[]> l1, int[] l2) {
		for(int[] game: l1) {
			if(Arrays.equals(game, l2))
				return true;
		}
		return false;
	}
	
	public static int getOppGivenSlot(int team, int slot) {
		return Math.abs(opp[slot][team-1]);
	}
	
}
