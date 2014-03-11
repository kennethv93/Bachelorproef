package simulatedAnnealing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import datareader.Datareader;

public class Algoritme {

	private static int[][] dist;
	private static int[][] opp;
	
	public static void main(String[] args) throws IOException {

		ArrayList<ArrayList<int[]>> games = getGames();
		for(ArrayList<int[]> tgames: games) {
			for(int[] kak: tgames) {
				System.out.println((games.indexOf(tgames)+1)+" "+kak[0]+" "+kak[1]);
			}
		}
		
	}
	
	public void algorithm() throws IOException {
		Datareader dr = new Datareader();
		dr.getData("C:\\Users\\Kenneth\\Desktop\\dataset.txt");
		dist = dr.getDist();
		opp = dr.getOpp();
		
		ArrayList<ArrayList<int[]>> games = getGames();
				
	}
	
	public static HashMap<Integer,ArrayList<int[]>> getInitialSolution(int d1, int d2, ArrayList<ArrayList<int[]>> games) {
		
		int slot = 1;
		int minkost = Integer.MAX_VALUE;
		int optgame = 0;
		int n = dist[0].length;
		int p1 = n-d1;
		int p2 = ((int) Math.floor(n/2))-d2;
		HashMap<Integer,ArrayList<int[]>> solution = new HashMap<Integer,ArrayList<int[]>>();
		
		// initieel, elke umpire wordt toegewezen
		for(int i=0; i<n; i++) {
			solution.put(i, new ArrayList<int[]>());
			solution.get(i).add(games.get(0).get(i));
		}
		
		for(int i=0; i<n; i++) {
			
			for(int[] game: games.get(slot)) {
				
				// constraints
				int beginInterval = slot-p1+1;
				if(beginInterval < 0)
					beginInterval = 0;
				
				// constraint 4
				boolean k = false;
				for(int j=beginInterval; j<slot; j++) {
					if(solution.get(i).get(j)[0] == game[0])
							k = true;
				}
				if(k == true)
					break;
				
				// constraint 5
				int hometeam = game[0];
				int awayteam = game[1];
				int histHometeam = 0;
				int histAwayteam = 0;
				int beginInterval2 = slot-p2+1;
				if(beginInterval < 0) {
					beginInterval = 0;
				}
				
				for(int j=beginInterval2; j<slot; j++) {
					if(solution.get(i).get(j)[0] == hometeam || solution.get(i).get(j)[1] == hometeam) 
						histHometeam++;
					if(solution.get(i).get(j)[0] == awayteam || solution.get(i).get(j)[1] == awayteam) 
						histAwayteam++;
				}
				if(histHometeam > 1 || histAwayteam > 1)
					break;
				
				// als alle contraints voldaan
				if(dist[game[0]][game[1]] < minkost) {
					minkost = dist[game[0]][game[1]];
					optgame = games.indexOf(game);
				}
			}
			if(minkost == Integer.MAX_VALUE)
				//BACKTRACKEN
			solution.get(i).add(games.get(slot).get(optgame));
			games.get(slot).remove(optgame);
			
		
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
}
