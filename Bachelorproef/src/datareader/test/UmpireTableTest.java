package datareader.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import datareader.Datareader;
import datareader.UmpireTable;

public class UmpireTableTest {
	
	private Datareader dr = new Datareader();

	@Test
	public void testGetNthWindowOpp43() throws IOException {
		int[][] sol = {
				{4,-3,2,-1},
				{-3,-4,1,2},
				{-2,1,-4,3}
			};
		
		dr.getData("C:\\Users\\Kenneth\\Dropbox\\School\\bachelorproef\\datasets\\umps4.txt");
		
		int[][] newOpp = UmpireTable.getNthWindowOpp(2, 3, dr.getOpp());
		assertTrue(Arrays.deepEquals(newOpp, sol));
	}
	
	@Test
	public void testGetNthWindowOpp105() throws IOException {
		int[][] sol = {
				{-2 , 1  , 8  , -7 , -10,  -9 , 4  , -3 , 6 ,  5},
				{-4 , 8  , 10 ,  1  , -6,  5  , -9 , -2 , 7 ,  -3 },
				{-6 , 10 ,  -9,  8  , 7  , 1 ,  -5 , -4 , 3 ,  -2},
				{7 ,  -6  ,-8 , 10 ,  9 ,  2  , -1,  3 ,  -5, -4}
			};
		dr.getData("C:\\Users\\Kenneth\\Dropbox\\School\\bachelorproef\\datasets\\umps10.txt");
		
		int[][] newOpp = UmpireTable.getNthWindowOpp(4, 4, dr.getOpp());
		assertTrue(Arrays.deepEquals(newOpp, sol));
	}
	
	@Test
	public void testGetAmountWindows() {
		assertTrue(UmpireTable.getAmountWindows(3,18) == 9);
		assertTrue(UmpireTable.getAmountWindows(4,18) == 6);
		assertTrue(UmpireTable.getAmountWindows(5,18) == 5);
	}
	
}
