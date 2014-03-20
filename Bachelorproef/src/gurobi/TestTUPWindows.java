package gurobi;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class TestTUPWindows {

	@Test
	public void testHasNextWindow() {
		assertTrue(TUPWindows.hasNextWindow(6, 2, 4));
		assertFalse(TUPWindows.hasNextWindow(10, 8, 2));
		assertTrue(TUPWindows.hasNextWindow(21, 5, 3));
		assertTrue(TUPWindows.hasNextWindow(21, 5, 4));
		assertFalse(TUPWindows.hasNextWindow(21, 5, 5));
	}
	
	@Test
	public void testConcatSolutions() {
		int[] u11 = {1,2}; int[] u12 = {3,4}; int[] u13 = {5,6};
		int[] u21 = {7,8}; int[] u22 = {9,10}; int[] u23 = {11,12};
		
		ArrayList<ArrayList<int[]>> kak1 = new ArrayList<ArrayList<int[]>>();
		ArrayList<int[]> ul11 = new ArrayList<int[]>();
		ArrayList<int[]> ul12 = new ArrayList<int[]>();
		ul11.add(u11); ul11.add(u12); kak1.add(ul11);
		ul12.add(u21); ul12.add(u22); kak1.add(ul12);
		
		ArrayList<ArrayList<int[]>> kak2 = new ArrayList<ArrayList<int[]>>();
		ArrayList<int[]> ul21 = new ArrayList<int[]>();
		ArrayList<int[]> ul22 = new ArrayList<int[]>();
		ul21.add(u22); ul21.add(u23); kak2.add(ul21);
		ul22.add(u12); ul22.add(u13); kak2.add(ul22);
		
		ArrayList<ArrayList<int[]>> sol = new ArrayList<ArrayList<int[]>>();
		ArrayList<int[]> usol1 = new ArrayList<int[]>();
		ArrayList<int[]> usol2 = new ArrayList<int[]>();
		usol1.add(u11); usol1.add(u12); usol1.add(u13); sol.add(usol1);
		usol2.add(u21); usol2.add(u22); usol2.add(u23); sol.add(usol2);
		
		assertEquals(TUPWindows.concatSolutions(kak1,kak2),sol);	
	}
	
	@Test
	public void testPrintSolution() {
		int[] u11 = {1,2}; int[] u12 = {3,4}; int[] u13 = {5,6};
		int[] u21 = {7,8}; int[] u22 = {9,10}; int[] u23 = {11,12};
		
		ArrayList<ArrayList<int[]>> kak1 = new ArrayList<ArrayList<int[]>>();
		ArrayList<int[]> ul11 = new ArrayList<int[]>();
		ArrayList<int[]> ul12 = new ArrayList<int[]>();
		ul11.add(u11); ul11.add(u12); kak1.add(ul11);
		ul12.add(u21); ul12.add(u22); kak1.add(ul12);
		TUPWindows.printSolution(kak1);
	}

}
