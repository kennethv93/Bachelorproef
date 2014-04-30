package gurobi;

import java.util.ArrayList;

public class LocalBranching {
		
	public static ArrayList<ArrayList<int[]>> execute(int[][] dist, int[][] opp, ArrayList<ArrayList<int[]>> soltable,
			int d1, int d2, int k, int LBTimeLimit) throws GRBException {
		System.out.println("EXECUTING LOCAL BRANCHING");
		
		int begin = 0;
		int end = soltable.get(0).size()-1;
		int n = soltable.size();
		int amountTeams = 2*n;
		int amountSlots = 4*n-2;
		
		// Parameters
		double n1 = n-d1-1;
		double n2 = Math.floor(n/2)-d2-1;
		
		int[][][] x1 = new int[(int) amountTeams][(int) amountSlots][(int) n];
		for(int u=0; u<n; ++u) {
			for(int s=0; s<amountSlots; ++s) {
				int venue = soltable.get(u).get(s)[0];
				x1[venue-1][s][u] = 1;
		}}
		
		// Environment
		GRBEnv env = new GRBEnv();
		if(LBTimeLimit != -1) env.set(GRB.DoubleParam.TimeLimit, LBTimeLimit);

		Solution sol = TUPWindows.initialize(env,dist,n,amountTeams,amountSlots,begin,end,null);
		GRBModel model = sol.getModel();

		TUPWindows.addConstraints(sol,opp,n,amountTeams,amountSlots,begin,end,d1,d2,n1,n2);	
		
		// NEIGHBORHOOD CONSTRAINT
		GRBVar[][][] x = sol.getX();
		GRBLinExpr expr = new GRBLinExpr();
		for(int i=0; i<amountTeams;++i){
			for(int s=0; s<amountSlots; ++s) {
				for(int u=0; u<n; ++u) {
					if(x1[i][s][u] == 1) {
						expr.addConstant(1);
						expr.addTerm(-1, x[i][s][u]);
					} else {
						expr.addTerm(1, x[i][s][u]);
					}
		}}}
		
		model.addConstr(expr, GRB.LESS_EQUAL, k, "neighborhood");
		
		// Solve
		model.optimize();	
		double[][][] XCurrSol = TUPWindows.getXSol(sol,amountTeams,amountSlots,n);
		ArrayList<ArrayList<int[]>> newsoltable = TUPWindows.getSolution(n,XCurrSol,begin,end);
		return newsoltable;
	}
}
