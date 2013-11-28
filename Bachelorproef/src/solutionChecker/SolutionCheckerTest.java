package solutionChecker;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import datareader.Datareader;

public class SolutionCheckerTest {

	private HashMap<Integer, ArrayList<int[]>> solution;

	@Before
	public void setUp() throws Exception {
		solution = new HashMap<Integer,ArrayList<int[]>>();
		ArrayList<int[]> u1 = new ArrayList<int[]>(); 
		int[] l1 = {1,3}; int[] l2 = {3,4}; int[] l3 = {1,4}; int[] l4 = {3,1}; int[] l5 = {4,3}; int[] l6 = {2,3};
		u1.add(l1); u1.add(l2); u1.add(l3); u1.add(l4); u1.add(l5); u1.add(l6);
		ArrayList<int[]> u2 = new ArrayList<int[]>(); 
		int[] j1 = {2,4}; int[] j2 = {1,2}; int[] j3 = {3,2}; int[] j4 = {4,2}; int[] j5 = {2,1}; int[] j6 = {4,1};
		u2.add(j1); u2.add(j2); u2.add(j3); u2.add(j4); u2.add(j5); u2.add(j6); 
		solution.put(0, u1); solution.put(1, u2);
	}

	@Test
	public void test() throws IOException {
		
		Datareader dr = new Datareader();
		dr.getData("C:\\Users\\Kenneth\\Desktop\\dataset.txt");
		
		assertTrue(SolutionChecker.check(solution,dr.getDist(),0,0));
	}

}
