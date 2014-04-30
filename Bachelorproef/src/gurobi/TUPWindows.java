package gurobi;
import gurobi.exception.ConstraintException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import datareader.Datareader;

public class TUPWindows {

	static boolean variableRelaxation = false;
	static boolean printSol = true;
	static boolean printVars = false;
	static boolean printToConsole = true;
	static double timeLimit = 3600; // in seconden
	static int penalty;
	
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
	static double totalexectime;
	static boolean relaxed = false;
	static DecimalFormat df = new DecimalFormat("#0.000");
	static boolean optimized = false;
	
	/**
	 * Oplossen via decompositie
	 */
	public static ArrayList<ArrayList<int[]>> getTableSolDecomp(String dataset, int n1, int n2, 
			int windowsize, boolean overlapConstraints, int pen,boolean localBranching, int k, int LBTimeLimit) throws IOException, GRBException {
		penalty = pen;
		withOverlapConstraints = overlapConstraints;
		totalexectime = 0;
		
		// Data inlezen
		Datareader dr = new Datareader();
		dr.getData("C:\\Users\\Kenneth\\Dropbox\\School\\bachelorproef\\datasets\\umps"+dataset+".txt");
		dist = dr.getDist();
		opp = dr.getOpp();
		
		double[][][] sol = null;
		ArrayList<ArrayList<int[]>> soltable = new ArrayList<ArrayList<int[]>>();
		ArrayList<Integer> bounds = getBounds(parseIntDataset(dataset)*2-2,windowsize);
		for(int i = 0; i<bounds.size()-1;i++) {
			sol = execute(dataset,n1,n2,bounds.get(i),bounds.get(i+1),sol);
			printSolution(getSolution(n,sol,bounds.get(i),bounds.get(i+1)));
			soltable = concatSolutions(soltable,getSolution(n,sol,bounds.get(i),bounds.get(i+1)));
		}
		
		// OPLOSSING INLEZEN
//		ArrayList<ArrayList<int[]>> soltable = Solutionreader.getSolTable("C:\\Users\\Kenneth\\Dropbox\\School\\"
//				+ "bachelorproef\\readysols\\sol"+dataset+".txt");
		
		//LOCAL BRANCHING
		int cost = getCost(n, soltable);
		ArrayList<ArrayList<int[]>> bestsoltable = soltable;
		ArrayList<ArrayList<int[]>> lbsoltable;
		if(localBranching) {
			while(true) {
				c4 = true;
				lbsoltable = LocalBranching.execute(dist, opp, bestsoltable, n1, n2,k,LBTimeLimit);
				if(getCost(n,lbsoltable) < cost) {
					bestsoltable = lbsoltable;
					cost = getCost(n,lbsoltable);
					optimized = true;
				} else {
					break;
				}
			}
		}
		c4 = false;
		return bestsoltable;
	}

	/**
	 * Initialiseer het model
	 */
	public static Solution initialize(GRBEnv env, int[][] dist, double n, double amountTeams, 
			double amountSlots, int begin, int end, double[][][] prevSol) throws GRBException {
		
		// Model
		GRBModel model = new GRBModel(env);
		model.set(GRB.StringAttr.ModelName, "TUP");
		
		// soort variabele
		char type = (variableRelaxation) ? GRB.CONTINUOUS : GRB.BINARY;
		
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
		
		// Update model om x en z te integreren
		model.update();
		
		// OBJECTIVE FUNCTION:
		GRBLinExpr expr = new GRBLinExpr();
		for(int i=0; i<amountTeams; ++i) {
			for(int j=0; j<amountTeams; ++j) {
				for(int u=0; u<n; ++u) {
					for(int s=0; s<amountSlots-1; ++s) {
						expr.addTerm(dist[i][j],z[i][j][s][u]);
		}}}}
		
		// CONSTRAINT 3	
		if(begin != 0) {
			for(int u=0; u<n; ++u) {				
				for(int i=0; i<amountTeams; ++i) {
					int teller = 0;
					for(int s=0; s<=begin;++s) {
						if(prevSol[i][s][u] == 1) teller++;
					}
					for(int s=begin+1;s<=end;++s) {
						if(teller == 0) {
							expr.addTerm(-Math.sqrt(begin)*penalty, x[i][s][u]);
						} else {
							//expr.addTerm(teller*penalty, x[i][s][u]);
						}
					}
				}
			}
		}
		
		// SET OBJECTIVE
		model.setObjective(expr, GRB.MINIMIZE);
		return new Solution(model,x,z);
	}
	
	/**
	 * EXECUTE METHODE
	 */
	public static double[][][] execute(String dataset,double p1,double p2,int begin,int end,double[][][] prevSol) throws IOException {
		
		System.out.println("Dataset: "+dataset);
		double[][][] XCurrSol = null;
		try {		
			
			n = dist.length/2;
			double amountTeams = opp[0].length;
			double amountSlots = opp.length;
			
			// Windows
			if(end>=amountSlots) end = (int) (amountSlots -1);
			System.out.println("begin: "+begin);
			System.out.println("end: "+end);
						
			// Parameters
			double n1 = p1-1;
			double n2 = p2-1;
			
			// Environment
			GRBEnv env = new GRBEnv();
			env.set(GRB.DoubleParam.TimeLimit, timeLimit);
			if(!printToConsole) env.set(GRB.IntParam.OutputFlag, 0);
			
			Solution sol = initialize(env,dist,n,amountTeams,amountSlots,begin,end,prevSol);
			GRBModel model = sol.getModel();
			 
			//model.set(GRB.IntAttr.ModelSense, 1);
			addConstraints(sol,opp,n,amountTeams,amountSlots,begin,end,p1,p2,n1,n2);
			
			// Cuts
			if(begin != 0) {
				addOverlapConstraint(prevSol, sol, amountTeams,n,n1,n2, begin,end);
				if(withOverlapConstraints) {
					addOverlapConstraints45(prevSol, sol, amountTeams,n,n1,n2, begin,end);
				}
			}
						
			// Solve
			model.optimize();
//			if( model.get(GRB.IntAttr.Status) == GRB.Status.INFEASIBLE) {
//				model.feasRelax(GRB.FEASRELAX_LINEAR, false, false, true);
//				relaxed = true;
//				model.optimize();
//			}
			XCurrSol = getXSol(sol,amountTeams,amountSlots,n);
			if(printSol) printVars(sol);
			
			// Concat previous solutions
			concatPreviousSolution(prevSol,XCurrSol, n,(int) amountTeams,(int) amountSlots,begin);
			
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
								e.getMessage());
		}
		return XCurrSol;
	}

	/**
	 *  Concateneer de vorige oplossingen aan de nieuwe oplossing voor het huidige window.
	 */
	public static void concatPreviousSolution(double[][][] prevSol, double[][][] currsol, int n, int amountTeams, int amountSlots, int begin) throws GRBException {
		if(prevSol == null) return;
		for(int s=0; s<begin; ++s) {
			for(int i=0; i<amountTeams;++i) {
				for(int u=0; u<n; ++u) {
					currsol[i][s][u] = prevSol[i][s][u];
				}
			}
		}
	}

	/**
	 * Voeg alle constraints toe.
	 */
	public static void addConstraints(Solution sol,int[][] opp, double n, double amountTeams, double amountSlots, int begin, int end1, 
			double d1, double d2, double n1, double n2) throws GRBException {
		
		GRBModel model = sol.getModel();
		GRBVar[][][] x = sol.getX();
		GRBVar[][][][] z = sol.getZ();
		
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
		if(c5) {
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
		if(c6) {
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
		if(c15 && begin == 0) {
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

	/**
	 * Voeg constraint toe zodat de assignments voor het eerste slot van het huidige window
	 * gelijk zijn aan de assignments van het laatste slot van het vorige window.
	 */
	public static void addOverlapConstraint(double[][][] prevX, Solution sol, double amountTeams, double n,
			double n1, double n2, int begin, int end) throws GRBException {
		if(sol == null) return;
		GRBModel model = sol.getModel();
		GRBVar[][][] x = sol.getX();
		
		// ADD OVERLAP CONSTRAINT
		for(int u=0; u<n; u++) {
			for(int i=0; i<amountTeams; i++) {
				model.addConstr(x[i][begin][u], GRB.EQUAL, prevX[i][begin][u], "overlap"+i+begin+u);
			}
		}
	}
	
	/**
	 * Voeg constraints toe om constraints 4 en 5 in de overlap te forceren.
	 */
	public static void addOverlapConstraints45(double[][][] XPrevSol, Solution sol, double amountTeams, double n,
			double n1, double n2, int begin, int end) throws GRBException {
		if(sol == null) return;
		GRBModel model = sol.getModel();
		GRBVar[][][] x = sol.getX();
		
		// Constraint 5
		int start5 = (int) ((begin+1-n1 < 0) ? 0 : begin+1-n1); 
		int end5 = (int) begin-1;
		for(int i=0; i<amountTeams;++i){
			for(int u=0; u<n; ++u) {
				for(int s=start5; s<=end5; ++s) {
					GRBLinExpr d4tot = new GRBLinExpr();
					int n15 = (int) ((s+n1 > end) ? end : s+n1); 
					for(int c=s; c<=n15;++c) {
						if(opp[c][i] > 0) {
							if(c<begin) {
								d4tot.addConstant(XPrevSol[i][c][u]);
							} else {
								d4tot.addTerm(1.0,x[i][c][u]);
							}	
						}
					}
					model.addConstr(d4tot, GRB.LESS_EQUAL, 1, "C5_x"+i+s+u);
		}}}
		
		// Constraint 6
		int start6 = (int) ((begin+1-n2 < 0) ? 0 : begin+1-n2);
		int end6 = (int) begin -1;
		for(int i=0; i<amountTeams;++i){
			for(int u=0; u<n; ++u) {
				for(int s=start6; s<=end6; ++s) {
					GRBLinExpr d5tot = new GRBLinExpr();
					int n26 = (int) (((s+n2) > end) ? end : s+n2);
					for(int c=s; c<=n26;++c) {
						if(c<begin) {
							d5tot.addConstant(XPrevSol[i][c][u]);
						} else {
							d5tot.addTerm(1.0,x[i][c][u]);
						}
						for(int j=0; j<amountTeams; ++j) {
							if(opp[c][j] == i+1) {
								if(c<begin) {
									d5tot.addConstant(XPrevSol[j][c][u]);
								} else {
									d5tot.addTerm(1.0,x[j][c][u]);
								}
					}}}
					model.addConstr(d5tot, GRB.LESS_EQUAL, 1, "C6_x"+i+s+u);
		}}}
	}

	public static ArrayList<ArrayList<int[]>> getSolution(double n, double[][][] x, int begin, int end) throws GRBException {
		
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
	
	public static int getCost(double n, ArrayList<ArrayList<int[]>> sol) {
		int cost = 0;
		for(ArrayList<int[]> umpire : sol) {
			for(int s=1; s<umpire.size(); s++) {
				cost+=dist[umpire.get(s)[0]-1][umpire.get(s-1)[0]-1];
			}
		}
		return cost;
	}

	public static Solution printVars(Solution sol) throws GRBException {
		GRBModel model = sol.getModel();
		GRBVar[][][] x = sol.getX();
		GRBVar[][][][] z = sol.getZ();
		if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
			//cost += model.get(GRB.DoubleAttr.ObjVal);
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
	


	
	public static void printSolution(ArrayList<ArrayList<int[]>> soltable) {
		for(ArrayList<int[]> list: soltable) {
			for(int[] i: list) {
				System.out.print("("+i[0]+","+i[1]+") ");
			}
			System.out.println();
		}
	}

	public static int parseIntDataset(String dataset) {
		if(dataset.equals("JPL")) return 16;
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
	
	/*
	 * WEGSCHRIJVEN NAAR FILES
	 */
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
	
	public static void writeSolution(BufferedWriter bw, double n,ArrayList<ArrayList<int[]>> soltable , int windowsize, int d1,int d2, boolean printTable) throws GRBException, IOException {
		bw.write("AMOUNT OF TEAMS: "+n+", WINDOW SIZE: "+windowsize+", d1 = "+d1+", d2 = "+d2+", time limit/window = "+timeLimit+"s"+
					", penalty: "+penalty); bw.newLine();
		if(withOverlapConstraints) {
			bw.write("\tWITH OVERLAP CONSTRAINTS:");
		} else {
			bw.write("\tWITHOUT OVERLAP CONSTRAINTS:");
			relaxed = true;
		}
		bw.newLine();
		if(optimized) {
			bw.write("\tOPTIMIZED WITH LOCAL BRANCHING");
			bw.newLine();
		}
		if(!relaxed) {
			//bw.write("\t\tCost: "+cost); bw.newLine();
			bw.write("\t\tCost: "+getCost(n,soltable));
		} else {
			//bw.write("\t\tLOWER BOUND: "+getCost(n,soltable)); bw.newLine();
		}
		bw.write("\t\tExecution time: "+df.format(totalexectime)+"s"); bw.newLine();
		if(printTable) {
						try {
							solutionChecker.SolutionChecker.check(soltable, d1, d2);
							bw.write("\t\t\tFEASIBLE SOLUTION"); bw.newLine();
							writeTable(bw,soltable);
						} catch (ConstraintException c) {
							bw.write("\t\t\t"+c.toString().toUpperCase()); bw.newLine();
							//writeTable(bw,soltable);
						}
						bw.newLine();
		}
		bw.write("-----------------------------------------------------------------------"); bw.newLine();
		
	}
	
	public static void writeTable(BufferedWriter bw, ArrayList<ArrayList<int[]>> soltable) throws IOException {
		for(ArrayList<int[]> list: soltable) {
			bw.write("\t\t\t");
			for(int[] i: list) {
				bw.write("("+i[0]+","+i[1]+") ");
			}
			bw.newLine();
		}
	}
}

