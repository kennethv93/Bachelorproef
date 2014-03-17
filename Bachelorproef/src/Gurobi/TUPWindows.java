package Gurobi;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import datareader.Datareader;
import gurobi.*;

public class TUPWindows {

	static boolean relaxation = true; // relaxatie
	static boolean printSol = false;
	static boolean printVars = false;
	static boolean printToConsole = true;
	static int lb = 0;
	 
	/*
	 * ZET CONSTRAINTS AAN/UIT
	 */
	static boolean c2 = true;
	static boolean c3 = false;
	static boolean c4 = true; // Contraint 3, werkt niet bij windows!
	static boolean c5 = true;
	static boolean c6 = true;
	static boolean c7 = false;
	static boolean c10 = true;
	static boolean c11 = false;
	static boolean c12 = false;
	static boolean c13 = true;
	static boolean c14 = false;
	static boolean c15 = true;
	static boolean c16 = true;
	static boolean c17 = true;
	static boolean c22 = true;
	static boolean c23 = true;
	
	static String[] datasets = {"4", "6", "6a", "6b", "6c", "8", "8a", "8b", "8c", "10", "10a", "10b", "10c","12","14","14a","14b","14c", 
			"16", "16a", "16b", "16c", "18", "20", "22","24","26","28","30","32"};

	/**
	 * MAIN METHOD
	 */
	public static void main(String[] args) throws IOException {
		System.out.println(Math.pow(Math.E, Math.PI)-Math.PI);
		Object[] options = {"Execute 1 dataset", "Execute all datasets", "Cancel"};

		int choice = JOptionPane.showOptionDialog(null,  "Choose an option.",  "Traveling Umpire Problem", 
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		
		String dataset = null;
		switch (choice) {
			case 0: dataset = (String) JOptionPane.showInputDialog(null, 
							"Choose a dataset", 
							"Traveling Umpire Problem", 
							JOptionPane.PLAIN_MESSAGE,
							null,
							datasets,
							"4");
					System.out.println("\t\t\t\t\t\t\t\t\t"+lb);
					break;
			case 1:	executeAll(0,0);
					break;
		}
		
		String windowsizechoice = JOptionPane.showInputDialog("Give window size, there are "+(parseIntDataset(dataset)*2-2)+" rounds.");
		int windowsize = Integer.parseInt(windowsizechoice);
		
		if(dataset != null) execute(dataset,0,0,1,windowsize,null);
	}
	
	/**
	 * SOLVE WINDOWS
	 */
	public static void solveWindows(String dataset, double d1, double d2, int amountSlots, int windowSize) throws IOException {
		int currentWindow = 0;
		ArrayList<ArrayList<int[]>> solution = new ArrayList<ArrayList<int[]>>();
		ArrayList<ArrayList<int[]>> windowSolution = new ArrayList<ArrayList<int[]>>();
		while(hasNextWindow(amountSlots, windowSize, currentWindow)) {
			currentWindow++;
			windowSolution = execute(dataset, d1, d2, currentWindow, windowSize,solution);
			
		}
	}
	
	/**
	 * EXECUTE METHODE
	 */
	public static ArrayList<ArrayList<int[]>> execute(String dataset, double d1, double d2, int window, int windowsize,ArrayList<ArrayList<int[]>> prevSol) throws IOException {
		
		System.out.println("Dataset: "+dataset);
		ArrayList<ArrayList<int[]>> solution = null;
		
		try {		
			// Data inlezen
			Datareader dr = new Datareader();
			dr.getData("C:\\Users\\Kenneth\\Dropbox\\School\\bachelorproef\\datasets\\umps"+dataset+".txt");
			int[][] dist = dr.getDist();
			int[][] opp = dr.getOpp();
			
			double n = dist.length/2;
			double amountTeams = opp[0].length;
			double amountSlots = opp.length;
			
			// Windows
			int N = window;
			int begin = (N-1)*(windowsize-1);
			int end = begin + windowsize - 1;
			if(end>=amountSlots) end = (int) (amountSlots -1);
						
			// Parameters
			double n1 = n-d1;
			double n2 = Math.floor(n/2)-d2;
			
			// Model
			GRBEnv env = new GRBEnv();
			if(!printToConsole) env.set(GRB.IntParam.OutputFlag, 0);
			GRBModel model = new GRBModel(env);
			model.set(GRB.StringAttr.ModelName, "TUP");
			
			// soort variabele
			char type = (relaxation) ? GRB.CONTINUOUS : GRB.BINARY;
			
			// Maak variabele x
			GRBVar[][][] x = new GRBVar[(int) amountTeams][(int) amountSlots][(int) n];
			for(int i=0; i<amountTeams;++i){
				for(int u=0; u<n; ++u) {
					for(int s=begin; s<end+1; ++s) {
						x[i][s][u] = 
								model.addVar(0, 1, 0,type, "x"+(i+1)+(s+1)+(u+1));
			}}}

			// Maak variabele z
			GRBVar[][][][] z = new GRBVar[(int) amountTeams][(int) amountTeams][(int) amountSlots-1][(int) n];
			for(int i=0; i<amountTeams;++i){
				for(int j=0; j<amountTeams; ++j) {
					for(int u=0; u<n; ++u) {
						for(int s=begin; s<end; ++s) {
							z[i][j][s][u] = 
									model.addVar(0, 1, dist[i][j],type, "z"+(i+1)+(j+1)+(s+1)+(u+1));
			}}}}
			
			// Maak variabele y
//			GRBVar[][][] y = new GRBVar[(int) amountTeams][(int) amountSlots][(int) n];
//			for(int i=0; i<amountTeams;++i){
//				for(int u=0; u<n; ++u) {
//					for(int s=begin; s<end+1; ++s) {
//						y[i][s][u] = 
//								model.addVar(0, 1, 0,GRB.BINARY, "y"+(i+1)+(s+1)+(u+1));
//			}}}
			
			// Update model om x en z te integreren
			model.update();
			
			// Stel objective in:
			GRBLinExpr expr = new GRBLinExpr();
			for(int i=0; i<amountTeams; ++i) {
				for(int j=0; j<amountTeams; ++j) {
					for(int u=0; u<n; ++u) {
						for(int s=begin; s<end; ++s) {
							expr.addTerm(dist[i][j],z[i][j][s][u]);
			}}}}
			model.setObjective(expr, GRB.MINIMIZE);
			 
			//model.set(GRB.IntAttr.ModelSense, 1);
									
			/**
			 * Constraints
			 */
			
			// Constraint 2
			if(c2) {
				for(int i=0; i<amountTeams;++i){
					for(int s=begin; s<end+1; ++s) {
						if(opp[s][i] > 0 ) {
							GRBLinExpr d1tot = new GRBLinExpr();
							for(int u=0; u<n; ++u) {
									d1tot.addTerm(1.0,x[i][s][u]);
							}
							model.addConstr(d1tot, GRB.EQUAL, 1, "C2_x"+i+s);
			}}}}
			
			// Constraint 3
			if(c3) {
				for(int s=begin; s<end+1;++s){
					for(int u=0; u<n; ++u) {
						GRBLinExpr d2tot = new GRBLinExpr();
						for(int i=0; i<amountTeams; ++i) {
							if(opp[s][i] > 0) {
								d2tot.addTerm(1.0,x[i][s][u]);
						}}
						model.addConstr(d2tot, GRB.EQUAL, 1, "C3_x"+s+u);
			}}}
			
			// Constraint 4
			if(c4) {
				for(int i=0; i<amountTeams;++i){
					for(int u=0; u<n; ++u) {
						GRBLinExpr d3tot = new GRBLinExpr();
						for(int s=begin; s<end+1; ++s) {
							if(opp[s][i] > 0) {
								d3tot.addTerm(1.0,x[i][s][u]);
						}}
						model.addConstr(d3tot, GRB.GREATER_EQUAL, 1, "C4_x"+i+u);
			}}}
			
			// Constraint 5
			if(c5) {
				for(int i=0; i<amountTeams;++i){
					for(int u=0; u<n; ++u) {
						for(int s=begin; s<end-n1; ++s) {
							GRBLinExpr d4tot = new GRBLinExpr();
							for(int c=s; c<=s+n1-1;++c) {
								d4tot.addTerm(1.0,x[i][c][u]);
							}
							model.addConstr(d4tot, GRB.LESS_EQUAL, 1, "C5_x"+i+s+u);
			}}}}
			
			// Constraint 6
			if(c6) {
				for(int i=0; i<amountTeams;++i){
					for(int u=0; u<n; ++u) {
						for(int s=begin; s<end-n2; ++s) {
							GRBLinExpr d5tot = new GRBLinExpr();
							for(int c=s; c<=s+n2-1;++c) {
								d5tot.addTerm(1.0,x[i][c][u]);
								for(int j=0; j<amountTeams; ++j) {
									if(opp[c][j] == i+1) {
										d5tot.addTerm(1.0,x[j][c][u]);
							}}}
							model.addConstr(d5tot, GRB.LESS_EQUAL, 1, "C6_x"+i+s+u);
			}}}}
			
			// Constraint 7
			if(c7) {
				for(int i=0; i<amountTeams; ++i) {
					for(int j=0; j<amountTeams; ++j) {
						for(int u=0; u<n; ++u) {
							for(int s=begin; s<end; ++s) {
								GRBLinExpr d6tot = new GRBLinExpr();
								d6tot.addTerm(1.0, x[i][s][u]);
								d6tot.addTerm(1.0, x[j][s+1][u]);
								d6tot.addTerm(-1.0, z[i][j][s][u]);
								model.addConstr(d6tot, GRB.LESS_EQUAL, 1, "C7"+i+j+u+s);
			}}}}}
			
			// Constraint 10
			if(c10) {
				for(int i=0; i<amountTeams; i++) {
					for(int u=0; u<n; u++) {
						for(int s=begin; s<end+1; s++) {
							if(opp[s][i] < 0) {
								GRBLinExpr d10tot = new GRBLinExpr();
								d10tot.addTerm(1.0, x[i][s][u]);
								model.addConstr(d10tot, GRB.EQUAL, 0, "C10_x"+i+s+u);
			}}}}}
			
			// Constraint 11
			if(c11) {
				for(int i=0; i<amountTeams; i++) {
					for(int j=0; j<amountTeams; j++) {
						for(int u=0; u<n; u++) {
							for(int s=begin; s<end; s++) {
								GRBLinExpr d11tot = new GRBLinExpr();
								d11tot.addTerm(1.0, z[i][j][s][u]);
								d11tot.addTerm(-1.0, x[i][s][u]);
								model.addConstr(d11tot, GRB.LESS_EQUAL, 0, "C11_x"+i+j+u+s);
			}}}}}
			
			// Constraint 12
			if(c12) {
				for(int i=0; i<amountTeams; i++) {
					for(int j=0; j<amountTeams; j++) {
						for(int u=0; u<n; u++) {
							for(int s=begin; s<end; s++) {
								GRBLinExpr d12tot = new GRBLinExpr();
								d12tot.addTerm(1.0, z[i][j][s][u]);
								d12tot.addTerm(-1.0, x[j][s+1][u]);
								model.addConstr(d12tot, GRB.LESS_EQUAL, 0, "C12_x"+i+j+u+s);
			}}}}}
			
			// Constraint 13
			if(c13) {
				for(int i=0; i<amountTeams; i++) {
					for(int u=0; u<n; u++) {
						for(int s=begin; s<end-1; s++) {
							GRBLinExpr d13tot = new GRBLinExpr();
							for(int j=0; j<amountTeams; j++) {
								d13tot.addTerm(1.0, z[j][i][s][u]);
							}
							for(int j=0; j<amountTeams; j++) {
								d13tot.addTerm(-1.0, z[i][j][s+1][u]);
							}
							model.addConstr(d13tot, GRB.EQUAL, 0, "C13_x"+i+u+s);
			}}}}
			
			// Constraint 14
			if(c14) {
				for(int u=0; u<n; u++) {
					for(int s=begin; s<end; s++) {
						GRBLinExpr d14tot = new GRBLinExpr();
						for(int i=0; i<amountTeams; i++) {
							for(int j=0; j<amountTeams; j++) {
								d14tot.addTerm(1.0, z[i][j][s][u]);
						}}
						model.addConstr(d14tot, GRB.EQUAL, 1, "C14_x"+u+s);
			}}}		
			
			/*
			 * Constraints stronger formulation
			 */
			
			// Constraint 15
			if(c15) {
				//Random rand = new Random();
				//int k = rand.nextInt((int) ((4*n-2-1)+1));
				int k = begin;
	
				ArrayList<int[]> venues = new ArrayList<int[]>();
				int umpire = 0;
				for(int i=0; i<amountTeams; i++) {
					if(opp[k][i] > 0) {
						int[] venue = {i,umpire};
						umpire++;
						venues.add(venue);
					}
				}
				
				for(int[] v: venues) {
					GRBLinExpr d15tot = new GRBLinExpr();
					d15tot.addTerm(1.0, x[v[0]][k][v[1]]);
					model.addConstr(d15tot, GRB.EQUAL, 1, "C15"+v[0]+v[1]);
				}
			}
			
			// Constraint 16
			if(c16) {
				for(int i=0; i<amountTeams; i++) {
					for(int u=0; u<n; u++) {
						GRBLinExpr d16tot = new GRBLinExpr();
						for(int j=0; j<amountTeams; j++) {
							d16tot.addTerm(1.0, z[i][j][begin][u]);
						}
						model.addConstr(x[i][begin][u], GRB.EQUAL, d16tot, "C16"+i+u);
			}}}
			
			// Constraint 17
			if(c17) {
				for(int i=0; i<amountTeams; i++) {
					for(int u=0; u<n; u++) {
						for(int s=begin+1; s<end+1; s++) {
							GRBLinExpr d16tot = new GRBLinExpr();
							for(int j=0; j<amountTeams; j++) {
								d16tot.addTerm(1.0, z[j][i][s-1][u]);
							}
							model.addConstr(x[i][s][u], GRB.EQUAL, d16tot, "C16"+i+u);
			}}}}
			
			// Constraint 22
			if(c22) {
				if(d1 < n-1) {
					for(int i=0; i<amountTeams; i++) {
						for(int u=0; u<n; u++) {
							for(int s=begin; s<end; s++) {
								GRBLinExpr d22tot = new GRBLinExpr();
								d22tot.addTerm(1.0, z[i][i][s][u]);
								model.addConstr(d22tot, GRB.EQUAL, 0, "C22"+i+u+s);
			}}}}}
			
			// Constraint 23
			if(c23) {
				for(int i=0; i<amountTeams; i++) {
					for(int j=0; j<amountTeams; j++) {
						if(i!=j) {
							for(int u=0; u<n; u++) {
								for(int s=begin; s<end; s++) {
									if((d2<Math.floor(n/2)-1) && ((opp[s][i] == opp[s+1][j]) || (opp[s][i] == j+1) || (opp[s+1][j] == i+1))) {
										GRBLinExpr d23tot = new GRBLinExpr();
										d23tot.addTerm(1.0, z[i][j][s][u]);
										model.addConstr(d23tot, GRB.EQUAL, 0, "C23"+i+j+u+s);
			}}}}}}}
			
			// Constraints ivm met de vorige oplossingen
			//addCuts(model,x,y,prevSol,(int) amountTeams,n1,n2, window,begin,end);
			
			// Solve
			model.optimize();
			solution = getSolution(model,x,z,opp,begin,end);
			if(printSol) printSolution(solution);
			printSolution(model,x,z);

			model.dispose();
			env.dispose();
				
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
								e.getMessage());
		}
		return solution;
	}

	private static void addCuts(GRBModel model, GRBVar[][][] x, GRBVar[][][] y, ArrayList<ArrayList<int[]>> prevSol, int amountTeams, double n1, double n2, int window,int begin, int end) throws GRBException {
		if(prevSol == null) return;
		int start = (int) ((begin+1-n1 < 0) ? 0 : begin+1-n1);
		for(int s = start; s<end+1-n1; s++) {
			for(ArrayList<int[]> u : prevSol) {
				for(int i = 0; i<amountTeams; i++) {
					GRBLinExpr my = new GRBLinExpr();
					my.addTerm(n1, y[i][s][prevSol.indexOf(u)]);
					model.addConstr(getSumOfX(model, x, i, prevSol.indexOf(u), start,(int) n1), GRB.LESS_EQUAL, my, "cut1"+i+s+u);
				}
			}
		}
	}
	
	private static GRBLinExpr getSumOfX(GRBModel model, GRBVar[][][] x, int i, int u, int start, int n1) {
		GRBLinExpr sum = new GRBLinExpr();
		for(int s = start; s<=start+n1; s++) {
			sum.addTerm(1.0, x[i][s][u]);
		}
		return sum;
	}

	/**
	 * Voer alle datasets uit.
	 */
	public static void executeAll(double d1, double d2) throws IOException {
		for(String s: datasets) {
			execute(s,d1,d2,1,parseIntDataset(s)*2-2,null);
			for(int i=0; i<40; i++) {
				System.out.print("-");
			}
			System.out.println();
		}
	}
	
	/**
	 * Print de oplossingen van de variabelen die niet 0 zijn.
	 */
	private static void printSolution(GRBModel model, GRBVar[][][] x,
            GRBVar[][][][] z) throws GRBException {
		if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
			System.out.println("Cost: " + model.get(GRB.DoubleAttr.ObjVal));
			lb += model.get(GRB.DoubleAttr.ObjVal);
			DecimalFormat df = new DecimalFormat("#0.00000");
			System.out.println("Execution time: "+ df.format(model.get(GRB.DoubleAttr.Runtime))+" seconds");
			
			if(printVars) {
				// Print waardes voor alle x
				System.out.println("\nX:");
				for (int i = 0; i < x.length; ++i) {
					for(int s = 0; s<x[0].length; ++s) {
						for(int u = 0; u<x[0][0].length; ++u) {
							if (x[i][s][u].get(GRB.DoubleAttr.X) > 0.0001) {
								System.out.println(x[i][s][u].get(GRB.StringAttr.VarName) + " " +
										x[i][s][u].get(GRB.DoubleAttr.X));
				}}}}
				
				// Print waardes voor alle z
				System.out.println("\nZ:");
				for (int i = 0; i < z.length; ++i) {
					for(int j = 0; j< z[0].length; ++j) {
						for(int u=0; u<z[0][0][0].length; ++u) {
							for(int s= 0; s<z[0][0].length-1; ++s){
								if(z[i][j][s][u].get(GRB.DoubleAttr.X) > 0.0001) {
									System.out.println(z[i][j][s][u].get(GRB.StringAttr.VarName) + " " +
											z[i][j][s][u].get(GRB.DoubleAttr.X));
				}}}}}
			}
		} else {
			System.out.println("No solution");
		}
	}
	
	/**
	 * Print de oplossing in tabelvorm.
	 */
	private static void printSolution(ArrayList<ArrayList<int[]>> sol) {
		System.out.println();
		
		for(ArrayList<int[]> list: sol) {
			for(int[] i: list) {
				System.out.print("("+i[0]+","+i[1]+") ");
			}
			System.out.println();
		}
	}
	
	/**
	 * Haal de oplossing uit het model.
	 */
	private static ArrayList<ArrayList<int[]>> getSolution(GRBModel model, GRBVar[][][] x,
            GRBVar[][][][] z, int[][] opp, int begin, int end) throws GRBException {
		ArrayList<ArrayList<int[]>> solution = new ArrayList<ArrayList<int[]>>();
		
		for(int u = 0; u<x[0][0].length; ++u) {
			ArrayList<int[]> ump = new ArrayList<int[]>();
			for(int s = begin; s<end+1; ++s) {
				ump.add(new int[2]);
			}
			solution.add(ump);
		}
		
		for (int i = 0; i < x.length; ++i) {
			for(int s = begin; s<end+1; ++s) {
				for(int u = 0; u<x[0][0].length; ++u) {
					if (x[i][s][u].get(GRB.DoubleAttr.X) == 1) {
						int[] match = new int[2];
						match[0] = i+1;
						match[1] = opp[s][i];
						solution.get(u).get(s-begin)[0] = i+1;
						solution.get(u).get(s-begin)[1] = opp[s][i];
		}}}}
		
		return solution;	
	}
	
	/**
	 * Execute-methode waarbij maar met 1 window gewerkt wordt.
	 */
//	public static ArrayList<ArrayList<int[]>> execute(String dataset, double d1, double d2) throws IOException {
//		return execute(dataset,d1,d2,1,parseIntDataset(dataset)*2-2);
//	}
	
	/**
	 * Haal het aantal teams uit de String dataset
	 * bvb. 10c -> 10
	 */
	public static int parseIntDataset(String dataset) {
		try {
			return Integer.parseInt(dataset);
		} catch(NumberFormatException e) {
			return Integer.parseInt(dataset.substring(0, dataset.length()-1));
		}
	}
	
	// TODO CALCULATE LOWER BOUNDS FOR ALL DATASETS
	
	/**
	 * Concatenate 2 given solutions.
	 */
	public static ArrayList<ArrayList<int[]>> concatSolutions(ArrayList<ArrayList<int[]>> s1, ArrayList<ArrayList<int[]>> s2) {
		ArrayList<ArrayList<int[]>> newSol = new ArrayList<ArrayList<int[]>>();
		ArrayList<int[]> addAL;
		for(int i = 0; i<s1.size(); i++) {
			newSol.add(s1.get(i));
			addAL = s2.get(i);
			addAL.remove(0);
			newSol.get(i).addAll(addAL);
		}
		return newSol;
	}
	
	/**
	 * Calculate the lower bounds.
	 */
//	public static void calculateLowerBounds(String dataset, double d1, double d2, int windowsize) throws IOException {
//		int window = 0;
//		while(hasNextWindow(2*parseIntDataset(dataset)-2,windowsize,window)) {
//			window++;
//			execute(dataset,d1,d2,window,windowsize);
//		}
//	}
	
	/**
	 * Check if the table has a next window.
	 */
	public static boolean hasNextWindow(int amountSlots, int windowSize, int currentWindow) {
		if(currentWindow == 0) return true;
		if(windowSize > amountSlots) return false;
		if(windowSize == amountSlots) return false;
		if(currentWindow == 1) return true;
		
		int slotsLeft = amountSlots;
		int window = currentWindow - 1;
		slotsLeft = slotsLeft - windowSize;
		while(window != 0) {
			slotsLeft -= (windowSize - 1);
			window--;
		}
		if(slotsLeft <= 0) return false;
		return true;
	}
}

