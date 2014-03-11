package datareader;


public class UmpireTable {
	
	public static int[][] getNthWindowOpp(int N, int windowsize, int[][] opp) {
		int begin = (N-1)*(windowsize-1);
		int end = begin + windowsize - 1;
		int[][] newOpp = new int[windowsize][opp[0].length];
		for(int i = 0; i<= end-begin; i++) {
			newOpp[i] = opp[begin+i];
		}
		return newOpp;
	}
	
//	public static int[] getNthWindowSlots(int N, int windows, int[][] opp) {
//		
//	}
	
	public static int getAmountWindows(int windowsize, int amountSlots) {
		return (int) (1+Math.ceil((amountSlots-windowsize)/(double) (windowsize - 1)));
	}

}
