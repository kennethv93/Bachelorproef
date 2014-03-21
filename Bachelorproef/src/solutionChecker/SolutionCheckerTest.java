//package solutionChecker;
//
//import gurobi.TUPWindows;
//
//import java.util.ArrayList;
//
//import org.junit.Before;
//
//public class SolutionCheckerTest {
//
//	private ArrayList<ArrayList<int[]>> sol;
//
//	@Before
//	public void setUp() throws Exception {
//		sol = TUPWindows.getTableSolDecomp("8", 0, 0, 14, true);
//	}
//
//	@Test
//	public void test() throws IOException {
//		assertTrue(SolutionChecker.check(sol,0,0) == 0);
//	}
//	
//	@Test
//	public void CheckC4test() throws IOException {
//		sol.get(0).get(4)[0] = 1;
//		assertTrue(SolutionChecker.check(sol,0,0) == 0);
//	}
//	
//	@Test
//	public void CheckC4test2() throws IOException {
//		sol.get(0).get(3)[0] = 1;
//		assertFalse(SolutionChecker.check(sol,0,0) == 0);
//	}
//	
//	@Test
//	public void CheckC5test() throws IOException {
//		sol.get(0).get(2)[1] = 2;
//		assertFalse(SolutionChecker.check(sol,0,0) == 0);
//	}
//
//	@Test
//	public void CheckC5test2() throws IOException {
//		sol.get(0).get(3)[1] = 2;
//		assertTrue(SolutionChecker.check(sol,0,0) == 0);
//	}
//}
