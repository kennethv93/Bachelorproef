package Gurobi;
import java.io.IOException;
import java.util.ArrayList;

import datareader.Datareader;

import gurobi.*;

public class TUP {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		try {		
			// Problem Data
//			int n = 4;
//			int amSlots = 4*n-2;
//			int amTeams = 2*n;
//			double slots[] = new double[amSlots];
//			for(int i=0; i<amSlots; ++i) {
//				slots[i] = i;
//			}
//			double teams[] = new double[amTeams];
//			for(int i=0; i<amSlots; ++i) {
//				teams[i] = i;
//			}
//			Datareader dr = new Datareader();
//			dr.getData("D:\\Dropbox\\School\\bachelorproef\\datasets\\umps4.txt");
//			int[][] dist = dr.getDist();
//			int[][] opp = dr.getOpp();
//			double n = dist.length/2;
//			double amountTeams = dist[0].length;
//			double amountSlots = 2*amountTeams-2;
//			
			// Data voor n = 2
//			double n = 2;
//			double[] slots = {0,1,2,3,4,5};
//			double[] teams = {0,1,2,3};
//			double[] umps = {0,1};
//			double[][] opp = {
//						{3,4,-1,-2},
//						{2,-1,4,-3},
//						{4,-3,2,-1},
//						{-3,-4,1,2},
//						{-2,1,-4,3},
//						{-4,3,-2,1}
//						};
//			
//			double[][] dist = {
//					{0,745,665,929},
//					{745,0,80,337},
//					{665,80,0,380},
//					{929,337,380,0},
//					};
			
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
				for(int s=0; s<amountSlots; ++s) {
					for(int c=0; c<n; ++c) {
						x[i][s][c] = 
								model.addVar(0, 1, 1,GRB.BINARY, "x"+(i+1)+(s+1)+(c+1));
					}
				}
			}
			
			GRBVar[][][][] z = new GRBVar[(int) amountTeams][(int) amountTeams][(int) amountSlots][(int) n];
			for(int i=0; i<amountTeams;++i){
				for(int j=0; j<amountTeams; ++j) {
					for(int s=0; s<amountSlots; ++s) {
						for(int c=0; c<n; ++c) {
							z[i][j][s][c] = 
									model.addVar(0, 1, dist[i][j],GRB.BINARY, "z"+i+j+s+c);
						}
					}
				}
			}
			
			// Update model to integrate new variables
			model.update();
			
			// Set objective:
			GRBLinExpr expr = new GRBLinExpr();
			for(int i=0; i<amountTeams; ++i) {
				for(int j=0; j<amountTeams; ++j) {
					for(int c=0; c<n; ++c) {
						for(int s=0; s<amountSlots; ++s) {
							expr.addTerm(dist[i][j],z[i][j][s][c]);
						}
					}
				}
			}
			model.setObjective(expr, GRB.MINIMIZE);
									
			// Constraints
			// B1 (works)
			for(int i=0; i<amountTeams;++i){
				for(int s=0; s<amountSlots; ++s) {
					if(opp[s][i] > 0 ) {
						GRBLinExpr d1tot = new GRBLinExpr();
						for(int c=0; c<n; ++c) {
								d1tot.addTerm(1.0,x[i][s][c]);
						}
						model.addConstr(d1tot, GRB.EQUAL, 1, "B1_x"+i+s);
					}
				}
			}
			
			// B2 (works)
			for(int s=0; s<amountSlots;++s){
				for(int c=0; c<n; ++c) {
					GRBLinExpr d2tot = new GRBLinExpr();
					for(int i=0; i<amountTeams; ++i) {
						if(opp[s][i] > 0) {
							d2tot.addTerm(1.0,x[i][s][c]);
						}
					}
					model.addConstr(d2tot, GRB.EQUAL, 1, "B2_x"+s+c);
				}
			}
			
			// B3 (works)
			for(int i=0; i<amountTeams;++i){
				for(int c=0; c<n; ++c) {
					GRBLinExpr d3tot = new GRBLinExpr();
					for(int s=0; s<amountSlots; ++s) {
						if(opp[s][i] > 0) {
							d3tot.addTerm(1.0,x[i][s][c]);
						}
					}
					model.addConstr(d3tot, GRB.GREATER_EQUAL, 1, "B3_x"+i+c);
				}
			}
			
			// B4
			for(int i=0; i<amountTeams;++i){
				for(int c=0; c<n; ++c) {
					for(int s=0; s<amountSlots-n1-1; ++s) {
						GRBLinExpr d4tot = new GRBLinExpr();
						for(int s1=0; s1<n1;++s1) {
							d4tot.addTerm(1.0,x[i][s+s1][c]);
						}
						model.addConstr(d4tot, GRB.LESS_EQUAL, 1, "B4_x"+i+s+c);
					}
				}
			}
			
			// B5
			for(int i=0; i<amountTeams;++i){
				for(int c=0; c<n; ++c) {
					for(int s=0; s<amountSlots-n2-1; ++s) {
						GRBLinExpr d5tot = new GRBLinExpr();
						for(int s2=0; s2<n2;++s2) {
							d5tot.addTerm(1.0,x[i][s+s2][c]);
							for(int k=0; k<amountTeams; ++k) {
								if(opp[s+s2][k] == i) {
									d5tot.addTerm(1.0,x[k][s+s2][c]);
								}
							}
						}
						model.addConstr(d5tot, GRB.LESS_EQUAL, 1, "B5_x"+i+s+c);
					}
				}
			}
			
			// B6
			for(int i=0; i<amountTeams; ++i) {
				for(int j=0; j<amountTeams; ++j) {
					for(int c=0; c<n; ++c) {
						for(int s=0; s<amountSlots-1; ++s) {
							GRBLinExpr d6tot = new GRBLinExpr();
							d6tot.addTerm(1.0, x[i][s][c]);
							d6tot.addTerm(1.0, x[j][s+1][c]);
							d6tot.addTerm(-1.0, z[i][j][s][c]);
							model.addConstr(d6tot, GRB.LESS_EQUAL, 1, "xisc6"+i+j+c+s);
						}
					}
				}
			}
			
			// Solve
			model.optimize();
			printSolution(model,x,z);

			
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
								e.getMessage());
			
		}
	}
	
	private static void printSolution(GRBModel model, GRBVar[][][] x,
            GRBVar[][][][] z) throws GRBException {
		if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
			System.out.println("\nCost: " + model.get(GRB.DoubleAttr.ObjVal));
//			System.out.println("\nX:");
//			for (int i = 0; i < x.length; ++i) {
//				for(int s = 0; s<x[0].length; ++s) {
//					for(int c = 0; c<x[0][0].length; ++c) {
//						//if (x[i][s][c].get(GRB.DoubleAttr.X) > 0.0001) {
//							System.out.println(x[i][s][c].get(GRB.StringAttr.VarName) + " " +
//									x[i][s][c].get(GRB.DoubleAttr.X));
//						//}
//					}
//				}
//			}
//			System.out.println("\nZ:");
//			for (int i = 0; i < z.length; ++i) {
//				for(int j = 0; j< z[0].length; ++j) {
//					for(int s= 0; s<z[0][0].length; ++s){
//						for(int c=0; c<z[0][0][0].length; ++c) {
//							System.out.println(z[i][j][s][c].get(GRB.StringAttr.VarName) + " " +
//									z[i][j][s][c].get(GRB.DoubleAttr.X));
//						}
//					}
//				}
//			}
		} else {
			System.out.println("No solution");
		}
		}		
	}

