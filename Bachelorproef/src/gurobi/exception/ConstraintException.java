package gurobi.exception;

public class ConstraintException extends Exception{

	private int umpire;
	private int slot;
	private int constraint;
	
	public ConstraintException(int u, int s, int c) {
		this.umpire = u;
		this.slot = s;
		this.constraint = c;
	}
	
	public ConstraintException(int u, int c) {
		this.umpire = u;
		this.slot = -1;
		this.constraint = c;
	}
	
	public String toString() {
		switch(constraint) {
		case 4:
			return "Constraint 4 not ok for umpire "+(umpire+1)+" at slot "+(slot+1)+".";
		case 5:
			return "Constraint 5 not ok for umpire "+(umpire+1)+" at slot "+(slot+1)+".";
		case 3:
			return "Constraint 3 not ok for umpire "+(umpire+1)+".";
		}
		return null;
	}
	
	private static final long serialVersionUID = 1L;

}
