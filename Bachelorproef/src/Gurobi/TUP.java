package Gurobi;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import datareader.Datareader;

import gurobi.*;

public class TUP {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		/////////////////
		//Kies dataset //
		/////////////////
//		String dataset = "4"; // Opl: 5176
//		String dataset = "6"; // Opl: 14077
//		String dataset = "6a"; // Opl: 15457
//		String dataset = "6b"; // Opl: 16716
//		String dataset = "6c"; // Opl: 14396
//		String dataset = "8"; // Opl: 34311
//		String dataset = "8a"; // Opl: 31490  
//		String dataset = "8b"; // Opl: 32731 
//		String dataset = "8c"; // Opl: 29879 
//		String dataset = "10"; // Opl: 48942
//		String dataset = "10a"; // Opl: 46551
//		String dataset = "10b"; // Opl: 45609
//		String dataset = "10c"; // Opl: 43149

		execute(dataset);
		
		// Test allemaal
//		String[] datasets = {"4", "6", "6a", "6b", "6c", "8", "8a", "8b", "8c", "10", "10a", "10b", "10c","12","14","14a","14b","14c"};
//		for(String s: datasets) {
//			execute(s);
//			for(int i=0; i<100; i++) {
//				System.out.print("-");
//			}
//			System.out.println();
//		}
	}
	
	public static void execute(String dataset) throws IOException {
		try {		
					
			// Data inlezen
			Datareader dr = new Datareader();
			dr.getData("D:\\Dropbox\\School\\bachelorproef\\datasets\\umps"+dataset+".txt");
			int[][] dist = dr.getDist();
			int[][] opp = dr.getOpp();
			double n = dist.length/2;
			double amountTeams = dist[0].length;
			double amountSlots = 2*amountTeams-2;
			System.out.println("number of teams = "+amountTeams);
			
			char type = GRB.CONTINUOUS;
						
			// Parameters
			double d1 = 0;
			double d2 = 0;
			double n1 = n-d1-1;
			double n2 = Math.floor(n/2)-d2-1;
			
			// Model
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);
			model.set(GRB.StringAttr.ModelName, "TUP");
			
			// Create variables
			GRBVar[][][] x = new GRBVar[(int) amountTeams][(int) amountSlots][(int) n];
			for(int i=0; i<amountTeams;++i){
				for(int u=0; u<n; ++u) {
					for(int s=0; s<amountSlots; ++s) {
						x[i][s][u] = 
								model.addVar(0, 1, 0,type, "x"+(i+1)+(s+1)+(u+1));
					}
				}
			}
			
			GRBVar[][][][] z = new GRBVar[(int) amountTeams][(int) amountTeams][(int) amountSlots-1][(int) n];
			for(int i=0; i<amountTeams;++i){
				for(int j=0; j<amountTeams; ++j) {
					for(int u=0; u<n; ++u) {
						for(int s=0; s<amountSlots-1; ++s) {
							z[i][j][s][u] = 
									model.addVar(0, 1, dist[i][j],type, "z"+(i+1)+(j+1)+(s+1)+(u+1));
						}
					}
				}
			}
			
			// Update model to integrate new variables
			model.update();
			
			// Set objective:
//			GRBLinExpr expr = new GRBLinExpr();
//			for(int i=0; i<amountTeams; ++i) {
//				for(int j=0; j<amountTeams; ++j) {
//					for(int u=0; u<n; ++u) {
//						for(int s=0; s<amountSlots-1; ++s) {
//							expr.addTerm(dist[i][j],z[i][j][s][u]);
//						}
//					}
//				}
//			}
//			model.setObjective(expr, GRB.MINIMIZE);
			 
			model.set(GRB.IntAttr.ModelSense, 1);
									
			/*
			 * Constraints
			 */
			
			// Constraint 2 (works)
			for(int i=0; i<amountTeams;++i){
				for(int s=0; s<amountSlots; ++s) {
					if(opp[s][i] > 0 ) {
						GRBLinExpr d1tot = new GRBLinExpr();
						for(int u=0; u<n; ++u) {
								d1tot.addTerm(1.0,x[i][s][u]);
						}
						model.addConstr(d1tot, GRB.EQUAL, 1, "C2_x"+i+s);
					}
				}
			}
			
			// Constraint 3 (works)
			for(int s=0; s<amountSlots;++s){
				for(int u=0; u<n; ++u) {
					GRBLinExpr d2tot = new GRBLinExpr();
					for(int i=0; i<amountTeams; ++i) {
						if(opp[s][i] > 0) {
							d2tot.addTerm(1.0,x[i][s][u]);
						}
					}
					model.addConstr(d2tot, GRB.EQUAL, 1, "C3_x"+s+u);
				}
			}
			
			// Constraint 4 (works)
			for(int i=0; i<amountTeams;++i){
				for(int u=0; u<n; ++u) {
					GRBLinExpr d3tot = new GRBLinExpr();
					for(int s=0; s<amountSlots; ++s) {
						if(opp[s][i] > 0) {
							d3tot.addTerm(1.0,x[i][s][u]);
						}
					}
					model.addConstr(d3tot, GRB.GREATER_EQUAL, 1, "C4_x"+i+u);
				}
			}
			
			// Constraint 5 (works)
			for(int i=0; i<amountTeams;++i){
				for(int u=0; u<n; ++u) {
					for(int s=0; s<amountSlots-n1; ++s) {
						GRBLinExpr d4tot = new GRBLinExpr();
						for(int c=s; c<=s+n1;++c) {
							//System.out.println("i: "+i+"  c: "+c+" u: "+u);
							d4tot.addTerm(1.0,x[i][c][u]);
						}
						//System.out.println("-----------");
						model.addConstr(d4tot, GRB.LESS_EQUAL, 1, "C5_x"+i+s+u);
					}
				}
			}
			
			// Constraint 6 (works)
			for(int i=0; i<amountTeams;++i){
				for(int u=0; u<n; ++u) {
					for(int s=0; s<amountSlots-n2; ++s) {
						GRBLinExpr d5tot = new GRBLinExpr();
						for(int c=s; c<=s+n2;++c) {
							d5tot.addTerm(1.0,x[i][c][u]);
							for(int j=0; j<amountTeams; ++j) {
								//System.out.println("i: "+i+" u: "+u+" s: "+s+" c: "+c+" j: "+j);
								if(opp[c][j] == i+1) {
									//System.out.println("team "+(j+1)+" speelt op slot "+(c+1)+" tegen "+i);
									d5tot.addTerm(1.0,x[j][c][u]);
								}
							}
						}
						//System.out.println("-----------");
						model.addConstr(d5tot, GRB.LESS_EQUAL, 1, "C6_x"+i+s+u);
					}
				}
			}
			
			// Constraint 7 (works)
			for(int i=0; i<amountTeams; ++i) {
				for(int j=0; j<amountTeams; ++j) {
					for(int u=0; u<n; ++u) {
						for(int s=0; s<amountSlots-1; ++s) {
							GRBLinExpr d6tot = new GRBLinExpr();
							d6tot.addTerm(1.0, x[i][s][u]);
							d6tot.addTerm(1.0, x[j][s+1][u]);
							d6tot.addTerm(-1.0, z[i][j][s][u]);
							model.addConstr(d6tot, GRB.LESS_EQUAL, 1, "C7"+i+j+u+s);
						}
					}
				}
			}
			
			// Constraint 10
			for(int i=0; i<amountTeams; i++) {
				for(int u=0; u<n; u++) {
					for(int s=0; s<amountSlots; s++) {
						if(opp[s][i] < 0) {
							GRBLinExpr d10tot = new GRBLinExpr();
							d10tot.addTerm(1.0, x[i][s][u]);
							model.addConstr(d10tot, GRB.EQUAL, 0, "C10_x"+i+s+u);
						}
					}
				}
			}
			
			// Constraint 11
			for(int i=0; i<amountTeams; i++) {
				for(int j=0; j<amountTeams; j++) {
					for(int u=0; u<n; u++) {
						for(int s=0; s<amountSlots-1; s++) {
							GRBLinExpr d11tot = new GRBLinExpr();
							d11tot.addTerm(1.0, z[i][j][s][u]);
							d11tot.addTerm(-1.0, x[i][s][u]);
							model.addConstr(d11tot, GRB.LESS_EQUAL, 0, "C11_x"+i+j+u+s);
						}
					}
				}
			}
			
			// Constraint 12
			for(int i=0; i<amountTeams; i++) {
				for(int j=0; j<amountTeams; j++) {
					for(int u=0; u<n; u++) {
						for(int s=0; s<amountSlots-1; s++) {
							GRBLinExpr d12tot = new GRBLinExpr();
							d12tot.addTerm(1.0, z[i][j][s][u]);
							d12tot.addTerm(-1.0, x[j][s+1][u]);
							model.addConstr(d12tot, GRB.LESS_EQUAL, 0, "C12_x"+i+j+u+s);
						}
					}
				}
			}
			
			// Constraint 13
			for(int i=0; i<amountTeams; i++) {
				for(int u=0; u<n; u++) {
					for(int s=0; s<amountSlots-2; s++) {
						GRBLinExpr d13tot = new GRBLinExpr();
						for(int j=0; j<amountTeams; j++) {
							d13tot.addTerm(1.0, z[j][i][s][u]);
						}
						for(int j=0; j<amountTeams; j++) {
							d13tot.addTerm(-1.0, z[i][j][s+1][u]);
						}
						model.addConstr(d13tot, GRB.EQUAL, 0, "C13_x"+i+u+s);
					}
				}
			}
			
			// Constraint 14
			for(int u=0; u<n; u++) {
				for(int s=0; s<amountSlots-1; s++) {
					GRBLinExpr d14tot = new GRBLinExpr();
					for(int i=0; i<amountTeams; i++) {
						for(int j=0; j<amountTeams; j++) {
							d14tot.addTerm(1.0, z[i][j][s][u]);
						}
					}
					model.addConstr(d14tot, GRB.EQUAL, 1, "C14_x"+u+s);
				}
			}
			
			/*
			 * Constraints stronger formulation
			 */
			
			// Constraint 15
			Random rand = new Random();
			//int k = rand.nextInt((int) ((4*n-2-1)+1));
			int k = 0;

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
			
//			// Constraint 16
//			for(int i=0; i<amountTeams; i++) {
//				for(int u=0; u<n; u++) {
//					GRBLinExpr d16tot = new GRBLinExpr();
//					for(int j=0; j<amountTeams; j++) {
//						d16tot.addTerm(1.0, z[i][j][0][u]);
//					}
//					model.addConstr(x[i][0][u], GRB.EQUAL, d16tot, "C16"+i+u);
//				}
//			}
//			
//			// Constraint 17
//			for(int i=0; i<amountTeams; i++) {
//				for(int u=0; u<n; u++) {
//					for(int s=1; s<amountSlots; s++) {
//						GRBLinExpr d16tot = new GRBLinExpr();
//						for(int j=0; j<amountTeams; j++) {
//							d16tot.addTerm(1.0, z[j][i][s-1][u]);
//						}
//						model.addConstr(x[i][s][u], GRB.EQUAL, d16tot, "C16"+i+u);
//					}
//				}
//			}
//			
//			// Constraint 22
//			if(d1 < n-1) {
//				for(int i=0; i<amountTeams; i++) {
//					for(int u=0; u<n; u++) {
//						for(int s=0; s<amountSlots - 1; s++) {
//							GRBLinExpr d22tot = new GRBLinExpr();
//							d22tot.addTerm(1.0, z[i][i][s][u]);
//							model.addConstr(d22tot, GRB.EQUAL, 0, "C22"+i+u+s);
//						}
//					}
//				}
//			}
//			
//			// Constraint 23
//			for(int i=0; i<amountTeams; i++) {
//				for(int j=0; j<amountTeams; j++) {
//					if(i==j) break;
//					for(int u=0; u<n; u++) {
//						for(int s=0; s<amountSlots-1; s++) {
//							if((d2<n2) && (opp[s][i] == opp[s+1][j] || opp[s][i] == j+1 || opp[s+1][j] == i+1)) {
//								GRBLinExpr d23tot = new GRBLinExpr();
//								d23tot.addTerm(1.0, z[i][j][s][u]);
//								model.addConstr(d23tot, GRB.EQUAL, 0, "C23"+i+j+u+s);
//							}
//						}
//					}
//				}
//			}
			
			// Solve
			model.optimize();
			printSolution(model,x,z);

			model.dispose();
			env.dispose();
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
								e.getMessage());
			
		}
	}
	
	private static void printSolution(GRBModel model, GRBVar[][][] x,
            GRBVar[][][][] z) throws GRBException {
		if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
			System.out.println("\nCost: " + model.get(GRB.DoubleAttr.ObjVal));
			
			boolean print = false;
			if(print) {
				// Print waardes voor de x'en
				System.out.println("\nX:");
				for (int i = 0; i < x.length; ++i) {
					for(int s = 0; s<x[0].length; ++s) {
						for(int u = 0; u<x[0][0].length; ++u) {
							if (x[i][s][u].get(GRB.DoubleAttr.X) > 0.0001) {
								System.out.println(x[i][s][u].get(GRB.StringAttr.VarName) + " " +
										x[i][s][u].get(GRB.DoubleAttr.X));
							}
						}
					}
				}
				
				// Print waardes voor alle z
				System.out.println("\nZ:");
				for (int i = 0; i < z.length; ++i) {
					for(int j = 0; j< z[0].length; ++j) {
						for(int u=0; u<z[0][0][0].length; ++u) {
							for(int s= 0; s<z[0][0].length-1; ++s){
								if(z[i][j][s][u].get(GRB.DoubleAttr.X) > 0.0001) {
									System.out.println(z[i][j][s][u].get(GRB.StringAttr.VarName) + " " +
											z[i][j][s][u].get(GRB.DoubleAttr.X));
								}
							}
						}
					}
				}
			}
		} else {
			System.out.println("No solution");
		}
		}	
	}

