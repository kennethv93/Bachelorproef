package Gurobi;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestTUPWindows {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHasNextWindow() {
		assertFalse(TUPWindows.hasNextWindow(6, 6, 1));
		assertFalse(TUPWindows.hasNextWindow(10, 8, 2));
		assertTrue(TUPWindows.hasNextWindow(21, 5, 3));
		assertTrue(TUPWindows.hasNextWindow(21, 5, 4));
		assertFalse(TUPWindows.hasNextWindow(21, 5, 5));
	}

}
