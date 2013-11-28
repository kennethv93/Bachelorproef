package solutionChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;


public class SolutionChecker {
	
	public static boolean check(HashMap<Integer,ArrayList<int[]>> sol, int[][] dist, int d1, int d2) {
		
		boolean kaka = true;
		int nbUmp = sol.keySet().size();
		int p1 = nbUmp - d1;
		int p2 = (int) (Math.floor(nbUmp/2) - d2);
		
		// maak array met teams
		int nbTeams = nbUmp*2;
		ArrayList<Integer> teams = new ArrayList<Integer>();
		for(int i=1; i<=nbTeams; i++) {
			teams.add(i);
		}
		
		// controleer constraint 3 en 4 voor iedere umpire
		Iterator<Entry<Integer, ArrayList<int[]>>> it = sol.entrySet().iterator();
		while(it.hasNext()) {
			ArrayList<Integer> visitedTeams = new ArrayList<Integer>();
			Entry<Integer,ArrayList<int[]>> current = (Entry<Integer, ArrayList<int[]>>) it.next();
			for(int[] game: current.getValue()) {
				visitedTeams.add(game[0]);
			}
			
			ArrayList<Integer> arrayTeams = new ArrayList<Integer>();
			for(int[] game: current.getValue()) {
				arrayTeams.add(game[0]);
				arrayTeams.add(game[1]);
			}
			
			// constraint 4
			if(!checkC4(visitedTeams,p1)) {
				kaka = false;
				break;
			}
			
			// constraint 5
			if(!checkC5(arrayTeams,p2)) {
				kaka = false;
				break;
			}
			
			// constraint 3
			Collections.sort(visitedTeams);
			if(!visitedTeams.containsAll(teams)) {
				kaka = false;
				break;
			}
					
		}
		return kaka;		
	}
	
	/**
	 * Controleer of in de gegeven teams aan constraint 4 voldaan is.
	 * 
	 * @param	teams
	 * @param	p1
	 */
	public static boolean checkC4(ArrayList<Integer> teams, int p1) {
		
		boolean b = true;
		for(int i=0; i<=teams.size()-p1;i++) {
			List<Integer> sublist = teams.subList(i+1, i+p1);
			if(sublist.contains(teams.get(i))) {
				b = false;
				break;
			}	
		}
		return b;
	}
	
	/**
	 * Controleer of in de gegeven teams aan constraint 5 voldaan is.
	 * 
	 * @param	teams
	 * @param	p2
	 */
	public static boolean checkC5(ArrayList<Integer> teams, int p2) {
		boolean b = true;
		for(int i=0; i<=teams.size()-1-(2*p2); i++) {
			if(i%2==0) {
				List<Integer> sublist = teams.subList(i+2, (i+2)+2*(p2-1));
				if(sublist.contains(teams.get(i))) {
					b = false;
					break;
				}
			} else {
				List<Integer> sublist = teams.subList(i+1, (i+1)+2*(p2-1));
				if(sublist.contains(teams.get(i))) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

}
