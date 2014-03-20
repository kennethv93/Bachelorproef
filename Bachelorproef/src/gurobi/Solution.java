package gurobi;

import gurobi.GRBModel;
import gurobi.GRBVar;

public class Solution {
	private GRBModel model;
	private GRBVar[][][] x;
	private GRBVar[][][] y;
	private GRBVar[][][][] z;
	
	public Solution(GRBModel model, GRBVar[][][] x, GRBVar[][][] y, GRBVar[][][][] z) {
		this.setModel(model);
		this.setX(x);
		this.setY(y);
		this.setZ(z);
	}
	
	public Solution() {
		
	}

	public GRBModel getModel() {
		return model;
	}

	public void setModel(GRBModel model) {
		this.model = model;
	}

	public GRBVar[][][] getX() {
		return x;
	}

	public void setX(GRBVar[][][] x) {
		this.x = x;
	}

	public GRBVar[][][] getY() {
		return y;
	}

	public void setY(GRBVar[][][] y) {
		this.y = y;
	}

	public GRBVar[][][][] getZ() {
		return z;
	}

	public void setZ(GRBVar[][][][] z) {
		this.z = z;
	}
}
