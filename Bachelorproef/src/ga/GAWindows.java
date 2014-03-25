package ga;
import gurobi.*;
import gurobi.exception.ConstraintException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;

import datareader.Datareader;

public class GAWindows {

	static boolean relaxation = false;
	static boolean printSol = true;
	static boolean printVars = false;
	static boolean printToConsole = true;
	static int lb = 0;
	 
	// CONSTRAINTS
	static boolean c2 = true;		static boolean c3 = false;		static boolean c4 = false;
	static boolean c5 = true;		static boolean c6 = true;		static boolean c7 = false;
	static boolean c10 = true;		static boolean c11 = false;		static boolean c12 = false;
	static boolean c13 = true;		static boolean c14 = false;		static boolean c15 = true;
	static boolean c16 = true;		static boolean c17 = true;		static boolean c22 = true;
	static boolean c23 = true;
	
	// OVERLAP CONSTRAINT
	static boolean withOverlapConstraints;
	
	// Variabelen
	static int n;
	static int[][] dist;
	static int[][] opp;
	static double cost;
	static double totalexectime;
	static DecimalFormat df = new DecimalFormat("#0.000");
	
	static String[] datasets = {"4", "6", "6a", "6b", "6c", "8", "8a", "8b", "8c", "10", "10a", "10b", "10c","12","14","14a","14b","14c", 
			"16", "16a", "16b", "16c", "18", "20", "22","24","26","28","30","32"};

	public static void main(String[] args) throws IOException, GRBException {		
		String dataset = (String) JOptionPane.showInputDialog(null, 
							"Choose a dataset", 
							"Traveling Umpire Problem", 
							JOptionPane.PLAIN_MESSAGE,
							null,
							datasets,
							"4");
		//System.out.println("\t\t\t\t\t\t\t\t\t"+lb);
		
		String windowsizechoice = JOptionPane.showInputDialog("Give window size, there are "+(parseIntDataset(dataset)*2-2)+" rounds.");
		int windowsize = Integer.parseInt(windowsizechoice);
		
		ArrayList<ArrayList<int[]>> soltable = getTableSolDecomp(dataset, 0, 0, windowsize,true);

		// PRINT
		printSolution(soltable);
		System.out.println("Total cost: "+cost);
		System.out.println("Total execution time "+df.format(totalexectime)+"s");
	}
	
	public static ArrayList<ArrayList<int[]>> getTableSolDecomp(String dataset, int n1, int n2, int windowsize, boolean cuts) throws IOException, GRBException {
		cost = 0;
		withOverlapConstraints = cuts;
		//int window = 0;
		double[][][] sol = null;
		ArrayList<ArrayList<int[]>> soltable = new ArrayList<ArrayList<int[]>>();
		//hasNextWindow(parseIntDataset(dataset)*2-2,windowsize,window)
		ArrayList<Integer> bounds = getBounds(parseIntDataset(dataset)*2-2,windowsize);
		for(int i = 0; i<bounds.size()-1;i++) {
			//window++;
			sol = execute(dataset,0,0,bounds.get(i),bounds.get(i+1),sol);
			//printSolution(n,sol,window,windowsize);
			soltable = concatSolutions(soltable,getSolution(n,sol,bounds.get(i),bounds.get(i+1)));
		}
		return soltable;
	}
	
	public static ArrayList<ArrayList<int[]>> getTableSolDecompIncreasingWS(String dataset, int n1, int n2, boolean cuts) throws IOException, GRBException {
		cost = 0;
		withOverlapConstraints = cuts;
		double[][][] sol = null;
		ArrayList<ArrayList<int[]>> soltable = new ArrayList<ArrayList<int[]>>();
		ArrayList<Integer> bounds = getBoundsIncreasingWS(parseIntDataset(dataset)*2-2);
		for(int i = 0; i<bounds.size()-1;i++){
			sol = execute(dataset,0,0,bounds.get(i),bounds.get(i+1),sol);
			//printSolution(n,sol,window,windowsize);
			soltable = concatSolutions(soltable,getSolution(n,sol,bounds.get(i),bounds.get(i+1)));
		}
		return soltable;
	}

	public static Solution initialize(GRBEnv env, int[][] dist, double n, double amountTeams, 
			double amountSlots, int begin, int end) throws GRBException {
		// Model
		GRBModel model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "TUP");
		
		// soort variabele
		char type = (relaxation) ? GRB.CONTINUOUS : GRB.BINARY;
		
		// Maak variabele x
		GRBVar[][][] x = new GRBVar[(int) amountTeams][(int) amountSlots][(int) n];
		for(int i=0; i<amountTeams;++i){
			for(int u=0; u<n; ++u) {
				for(int s=0; s<amountSlots; ++s) {
					x[i][s][u] = 
							model.addVar(0, 1, 0,type, "x"+(i)+(s)+(u));
		}}}

		// Maak variabele z
		GRBVar[][][][] z = new GRBVar[(int) amountTeams][(int) amountTeams][(int) amountSlots-1][(int) n];
		for(int i=0; i<amountTeams;++i){
			for(int j=0; j<amountTeams; ++j) {
				for(int u=0; u<n; ++u) {
					for(int s=0; s<amountSlots-1; ++s) {
						z[i][j][s][u] = 
								model.addVar(0, 1, dist[i][j],type, "z"+(i+1)+(j+1)+(s+1)+(u+1));
		}}}}
		
		// Update model om x,y en z te integreren
		model.update();
		
		// Stel objective in:
		GRBLinExpr expr = new GRBLinExpr();
		for(int i=0; i<amountTeams; ++i) {
			for(int j=0; j<amountTeams; ++j) {
				for(int u=0; u<n; ++u) {
					for(int s=0; s<amountSlots-1; ++s) {
						expr.addTerm(dist[i][j],z[i][j][s][u]);
		}}}}
		model.setObjective(expr, GRB.MINIMIZE);
		return new Solution(model,x,z);
	}
	
	public static double[][][] execute(String dataset,double d1,double d2,int begin,int end,double[][][] prevSol) throws IOException {
		
		System.out.println("Dataset: "+dataset);
		double[][][] XCurrSol = null;
		try {		
			// Data inlezen
			Datareader dr = new Datareader();
			dr.getData("C:\\Users\\Kenneth\\Dropbox\\School\\bachelorproef\\datasets\\umps"+dataset+".txt");
			dist = dr.getDist();
			opp = dr.getOpp();
			
			n = dist.length/2;
			double amountTeams = opp[0].length;
			double amountSlots = opp.length;
			
			// Windows
//			int begin = getBegin(window,windowsize);
//			int end = getEnd(window,windowsize);
			if(end>=amountSlots) end = (int) (amountSlots -1);
			System.out.println("begin: "+begin);
			System.out.println("end: "+end);
						
			// Parameters
			double n1 = n-d1-1;
			double n2 = Math.floor(n/2)-d2-1;
			
			// Environment
			GRBEnv env = new GRBEnv();
			env.set(GRB.DoubleParam.TimeLimit, 10800);
			if(!printToConsole) env.set(GRB.IntParam.OutputFlag, 0);
			
			Solution sol = initialize(env,dist,n,amountTeams,amountSlots,begin,end);
			GRBModel model = sol.getModel();
			 
			//model.set(GRB.IntAttr.ModelSense, 1);
			addConstraints(sol,opp,n,amountTeams,amountSlots,begin,end,d1,d2,n1,n2);
			
			// Cuts
//			if(begin != 0) {
//				//addOverlapConstraint(prevSol, sol, amountTeams,n,n1,n2, begin,end);
//				if(withOverlapConstraints) {
//					addInfeasibility(prevSol, sol, amountTeams,n,n1,n2, begin,end);
//					//System.out.println("Cuts added");
//				}
//			}
						
			// Solve
			//System.out.println("Start solving.");
			model.optimize();
			if(printSol) printVars(sol);
			XCurrSol = getXSol(sol,amountTeams,amountSlots,n);
			// Concat previous solutions
			concatPreviousSolution(prevSol,XCurrSol, n,(int) amountTeams,(int) amountSlots,begin);
			
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
								e.getMessage());
		}
		return XCurrSol;
	}

	private static void concatPreviousSolution(double[][][] prevSol, double[][][] currsol, int n, int amountTeams, int amountSlots, int begin) throws GRBException {
		if(prevSol == null) return ;
		for(int s=0; s<begin; ++s) {
			for(int i=0; i<amountTeams;++i) {
				for(int u=0; u<n; ++u) {
					currsol[i][s][u] = prevSol[i][s][u];
				}
			}
		}
	}

	private static void addConstraints(Solution sol,int[][] opp, double n, double amountTeams, double amountSlots, int begin, int end1, 
			double d1, double d2, double n1, double n2) throws GRBException {
		
		GRBModel model = sol.getModel();
		GRBVar[][][] x = sol.getX();
		GRBVar[][][][] z = sol.getZ();
		//int end1 = (int) ((end > 4*n-3) ? 4*n-3 : end);
		
		// Constraint 2
		if(c2) {
			for(int i=0; i<amountTeams;++i){
				for(int s=begin; s<end1+1; ++s) {
					if(opp[s][i] > 0 ) {
						GRBLinExpr d1tot = new GRBLinExpr();
						for(int u=0; u<n; ++u) {
								d1tot.addTerm(1.0,x[i][s][u]);
						}
						model.addConstr(d1tot, GRB.EQUAL, 1, "C2_x"+i+s);
		}}}}
		
		// Constraint 3
		if(c3) {
			for(int s=begin; s<end1+1;++s){
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
					for(int s=begin; s<end1+1; ++s) {
						if(opp[s][i] > 0) {
							d3tot.addTerm(1.0,x[i][s][u]);
					}}
					model.addConstr(d3tot, GRB.GREATER_EQUAL, 1, "C4_x"+i+u);
		}}}
		
		// Constraint 5
		if(true) {
			for(int i=0; i<amountTeams;++i){
				for(int u=0; u<n; ++u) {
					int end5 = (int) ((end1-n1 < begin) ? begin : end1-n1);
					for(int s=begin; s<=end5; ++s) {
						GRBLinExpr d4tot = new GRBLinExpr();
						int n15 = (int) ((end1-n1 < begin) ? end1-begin : n1);
						for(int c=s; c<=s+n15;++c) {
							if(opp[c][i] > 0) {
								d4tot.addTerm(1.0,x[i][c][u]);
							}
						}
						model.addConstr(d4tot, GRB.LESS_EQUAL, 1, "C5_x"+i+s+u);
		}}}}
		
		// Constraint 6
		if(true) {
			for(int i=0; i<amountTeams;++i){
				for(int u=0; u<n; ++u) {
					int end6 = (int) ((end1-n2 < begin) ? begin : end1-n2);
					for(int s=begin; s<=end6; ++s) {
						GRBLinExpr d5tot = new GRBLinExpr();
						int n26 = (int) ((end1-n2 < begin) ? end1-begin : n2);
						for(int c=s; c<=s+n26;++c) {
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
						for(int s=begin; s<end1; ++s) {
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
					for(int s=begin; s<end1+1; s++) {
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
						for(int s=begin; s<end1; s++) {
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
						for(int s=begin; s<end1; s++) {
							GRBLinExpr d12tot = new GRBLinExpr();
							d12tot.addTerm(1.0, z[i][j][s][u]);
							d12tot.addTerm(-1.0, x[j][s+1][u]);
							model.addConstr(d12tot, GRB.LESS_EQUAL, 0, "C12_x"+i+j+u+s);
		}}}}}
		
		// Constraint 13
		if(c13) {
			for(int i=0; i<amountTeams; i++) {
				for(int u=0; u<n; u++) {
					for(int s=begin; s<end1-1; s++) {
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
				for(int s=begin; s<end1; s++) {
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
			
			Random rand = new Random();
			k = rand.nextInt((int) (4*n-2));
			 // make umpire list
			ArrayList<Integer> umpires = new ArrayList<Integer>();
			for(int u=0; u<n;u++) {
				umpires.add(u);
			}
			venues = new ArrayList<int[]>();
			//umpire = 0;
			for(int i=0; i<amountTeams; i++) {
				if(opp[k][i] > 0) {
					int u = rand.nextInt(umpires.size());
					int[] venue = {i,umpires.get(u)};
					umpires.remove(u);
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
					for(int s=begin+1; s<end1+1; s++) {
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
						for(int s=begin; s<end1; s++) {
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
							for(int s=begin; s<end1; s++) {
								if((d2<Math.floor(n/2)-1) && ((opp[s][i] == opp[s+1][j]) || (opp[s][i] == j+1) || (opp[s+1][j] == i+1))) {
									GRBLinExpr d23tot = new GRBLinExpr();
									d23tot.addTerm(1.0, z[i][j][s][u]);
									model.addConstr(d23tot, GRB.EQUAL, 0, "C23"+i+j+u+s);
		}}}}}}}
	}

//	private static void addOverlapConstraint(double[][][] prevX, Solution sol, double amountTeams, double n,
//			double n1, double n2, int begin, int end) throws GRBException {
//		if(sol == null) return;
//		GRBModel model = sol.getModel();
//		GRBVar[][][] x = sol.getX();
//		
//		// ADD OVERLAP CONSTRAINT
//		for(int u=0; u<n; u++) {
//			for(int i=0; i<amountTeams; i++) {
//				model.addConstr(x[i][begin][u], GRB.EQUAL, prevX[i][begin][u], "overlap"+i+begin+u);
//			}
//		}
//	}
	
	private static void addInfeasibility(double[][][] XPrevSol, Solution sol, double amountTeams, double n,
			double n1, double n2, int begin, int end) throws GRBException {
		if(sol == null) return;
		GRBModel model = sol.getModel();
		GRBVar[][][] x = sol.getX();
		
		Random rand = new Random();
		rand.nextInt((int) amountTeams);
		
		Random rand2 = new Random();
		rand.nextInt((int) ((4*n-2-1)+1));
		
		Random rand3 = new Random();
		rand.nextInt((int) (n+1));
		
		GRBLinExpr feas = new GRBLinExpr();
		
	}

	public static void executeAll(double d1, double d2) throws IOException {
		for(String s: datasets) {
			execute(s,d1,d2,1,parseIntDataset(s)*2-2,null);
			for(int i=0; i<40; i++) {
				System.out.print("-");
			}
			System.out.println();
		}
	}

	private static ArrayList<ArrayList<int[]>> getSolution(double n, double[][][] x, int begin, int end) throws GRBException {
		
//		int begin = getBegin(window,windowsize);
//		int end = getEnd(window,windowsize);
		
		ArrayList<ArrayList<int[]>> solution = new ArrayList<ArrayList<int[]>>();
		int end1 = (int) ((end > 4*n-3) ? 4*n-3 : end);
		for(int u = 0; u<x[0][0].length; ++u) {
			ArrayList<int[]> ump = new ArrayList<int[]>();
			for(int s = begin; s<end1+1; ++s) {
				ump.add(new int[2]);
			}
			solution.add(ump);
		}
		
		for (int i = 0; i < x.length; ++i) {
			for(int s = begin; s<end1+1; ++s) {
				for(int u = 0; u<x[0][0].length; ++u) {
					if (x[i][s][u] > 0) {
						int[] match = new int[2];
						match[0] = i+1;
						match[1] = opp[s][i];
						solution.get(u).get(s-begin)[0] = i+1;
						solution.get(u).get(s-begin)[1] = opp[s][i];
		}}}}
		
		return solution;	
	}

	private static Solution printVars(Solution sol) throws GRBException {
		GRBModel model = sol.getModel();
		GRBVar[][][] x = sol.getX();
		GRBVar[][][][] z = sol.getZ();
		if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
			cost += model.get(GRB.DoubleAttr.ObjVal);
			totalexectime+=model.get(GRB.DoubleAttr.Runtime);
			
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
		return sol;
	}

//	@SuppressWarnings("unused")
//	private static void printSolution(double n, Solution solution,int window, int windowsize) throws GRBException {
//		System.out.println();
//		ArrayList<ArrayList<int[]>> sol = getSolution(n, solution,window,windowsize);
//		for(ArrayList<int[]> list: sol) {
//			for(int[] i: list) {
//				System.out.print("("+i[0]+","+i[1]+") ");
//			}
//			System.out.println();
//		}
//	}
	
	public static void writeSolution(BufferedWriter bw, double n, int windowsize, boolean withCuts) throws IOException {
		bw.write("AMOUNT OF TEAMS: "+n+" AND WINDOW SIZE: "+windowsize); bw.newLine();
		if(withCuts) {
			bw.write("\tWITH CUTS:");
		} else {
			bw.write("\tWITHOUT CUTS:");
		}
		bw.write("infeasible"); bw.newLine();
		bw.write("-----------------------------------------------------------------------"); bw.newLine();
	}
	
	public static void writeSolution(BufferedWriter bw, double n,ArrayList<ArrayList<int[]>> soltable , int windowsize, boolean printTable) throws GRBException, IOException {
		bw.write("AMOUNT OF TEAMS: "+n+" AND WINDOW SIZE: "+windowsize); bw.newLine();
		if(withOverlapConstraints) {
			bw.write("\tWITH CUTS:");
		} else {
			bw.write("\tWITHOUT CUTS:");
		}
		bw.newLine();
		bw.write("\t\tCost: "+cost); bw.newLine();
		bw.write("\t\tExecution time: "+df.format(totalexectime)+"s"); bw.newLine();
		if(printTable) {
						try {
							solutionChecker.SolutionChecker.check(soltable, 0, 0);
							writeTable(bw,soltable);
						} catch (ConstraintException c) {
							bw.write("\t\t\t"+c.toString().toUpperCase()); bw.newLine();
							writeTable(bw,soltable);
						}
						bw.newLine();
		}
		bw.write("-----------------------------------------------------------------------"); bw.newLine();
		
	}
	
	private static void writeTable(BufferedWriter bw, ArrayList<ArrayList<int[]>> soltable) throws IOException {
		for(ArrayList<int[]> list: soltable) {
			bw.write("\t\t\t");
			for(int[] i: list) {
				bw.write("("+i[0]+","+i[1]+") ");
			}
			bw.newLine();
		}
	}
	
	public static void printSolution(ArrayList<ArrayList<int[]>> soltable) {
		for(ArrayList<int[]> list: soltable) {
			for(int[] i: list) {
				System.out.print("("+i[0]+","+i[1]+") ");
			}
			System.out.println();
		}
	}

	public static int parseIntDataset(String dataset) {
		try {
			return Integer.parseInt(dataset);
		} catch(NumberFormatException e) {
			return Integer.parseInt(dataset.substring(0, dataset.length()-1));
		}
	}

	public static ArrayList<ArrayList<int[]>> concatSolutions(ArrayList<ArrayList<int[]>> s1, ArrayList<ArrayList<int[]>> s2) {
		if(s1.isEmpty()) return s2;
		if(s2.isEmpty()) return s1;
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

	public static ArrayList<int[]> getUmpireByGivenMatch(int[] match, ArrayList<ArrayList<int[]>> table) {
		ArrayList<int[]> solution = null;
		for(ArrayList<int[]> u : table) {
			if(u.get(0) == match) {
				solution = u;
				table.remove(u);
				break;
			}
		}
		return solution;
	}

	public static double[][][] getXSol(Solution prevsol, double amountTeams,double amountSlots,int n) throws GRBException {
		double[][][] xprevsol = new double[(int) amountTeams][(int) amountSlots][n];
		for(int i=0; i<amountTeams;++i){
			for(int u=0; u<n; ++u) {
				for(int s=0; s<amountSlots; ++s) {
					xprevsol[i][s][u] = prevsol.getX()[i][s][u].get(GRB.DoubleAttr.X);
		}}}
		return xprevsol;
	}

	public static ArrayList<Integer> getBounds(double amountSlots, int windowSize) {
		ArrayList<Integer> bounds = new ArrayList<Integer>();
		bounds.add(0);
		while(true) {
			if(bounds.get(bounds.size()-1)+windowSize-1 >= amountSlots-1) {
				bounds.add((int) (amountSlots-1));
				break;
			}
			bounds.add(bounds.get(bounds.size()-1)+windowSize-1);
		}
		return bounds;
	}
	
	public static ArrayList<Integer> getBoundsIncreasingWS(double amountSlots) {
		ArrayList<Integer> bounds = new ArrayList<Integer>();
		bounds.add(0);
		int acc = (int) (amountSlots/2);
		while(true) {
			if(bounds.get(bounds.size()-1)+acc >= amountSlots-1) {
				bounds.add((int) (amountSlots-1));
				break;
			}
			bounds.add(bounds.get(bounds.size()-1)+acc);
			acc = (acc/2 < 2) ? 2 : acc/2;
		}
		return bounds;
	}
	
}

