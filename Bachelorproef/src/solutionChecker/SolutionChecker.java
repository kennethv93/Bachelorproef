package solutionChecker;

import gurobi.exception.ConstraintException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class SolutionChecker {
	
	public static void check(ArrayList<ArrayList<int[]>> sol, int d1, int d2) throws ConstraintException {
		
		int nbUmp = sol.size();
		int p1 = nbUmp - d1;
		int p2 = (int) (Math.floor(nbUmp/2) - d2);
		
		// maak array met teams
		int nbTeams = nbUmp*2;
		ArrayList<Integer> teams = new ArrayList<Integer>();
		for(int i=1; i<=nbTeams; i++) {
			teams.add(i);
		}
		
		// controleer constraint 3 en 4 voor iedere umpire
		Iterator<ArrayList<int[]>> it = sol.iterator();
		while(it.hasNext()) {
			ArrayList<Integer> visitedTeams = new ArrayList<Integer>();
			ArrayList<int[]> current = it.next();
			for(int[] game: current) {
				visitedTeams.add(game[0]);
			}
			
			ArrayList<Integer> arrayTeams = new ArrayList<Integer>();
			for(int[] game: current) {
				arrayTeams.add(game[0]);
				arrayTeams.add(game[1]);
			}
			
			// constraint 4
			checkC4(sol.indexOf(current),visitedTeams,p1);
			
			// constraint 5
			checkC5(sol.indexOf(current),arrayTeams,p2);
			
			// constraint 3
			Collections.sort(visitedTeams);
			checkC3(sol.indexOf(current),visitedTeams,teams);

					
		}
	}
	
	/**
	 * Controleer of in de gegeven teams aan de constraint 3 voldaan is.
	 * 
	 * @param visitedTeams
	 * @param allTeams
	 * @return
	 * @throws ConstraintException 
	 */
	public static void checkC3(int umpire, ArrayList<Integer> visitedTeams, ArrayList<Integer> allTeams) throws ConstraintException {
		Collections.sort(visitedTeams);
		if(!visitedTeams.containsAll(allTeams)) throw new ConstraintException(umpire,3);
	}
	
	
	/**
	 * Controleer of in de gegeven teams aan constraint 4 voldaan is.
	 * 
	 * @param	teams
	 * @param	p1
	 * @throws ConstraintException 
	 */
	public static void checkC4(int umpire, ArrayList<Integer> teams, int p1) throws ConstraintException {
		for(int i=0; i<=teams.size()-p1;i++) {
			List<Integer> sublist = teams.subList(i+1, i+p1);
			if(sublist.contains(teams.get(i))) {
				throw new ConstraintException(umpire,i,4);
			}	
		}
	}
	
	/**
	 * Controleer of in de gegeven teams aan constraint 5 voldaan is.
	 * 
	 * @param	teams
	 * @param	p2
	 * @throws ConstraintException 
	 */
	public static void checkC5(int umpire, ArrayList<Integer> teams, int p2) throws ConstraintException {
		for(int i=0; i<=teams.size()-1-(2*p2); i++) {
			if(i%2==0) {
				List<Integer> sublist = teams.subList(i+2, (i+2)+2*(p2-1));
				if(sublist.contains(teams.get(i))) {
					throw new ConstraintException(umpire,i,5);
				}
			} else {
				List<Integer> sublist = teams.subList(i+1, (i+1)+2*(p2-1));
				if(sublist.contains(teams.get(i))) {
					throw new ConstraintException(umpire,i,5);
				}
			}
		}
	}

}
