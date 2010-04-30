package org.ihtsdo.db.bdb;

import static org.junit.Assert.assertTrue;

import org.ihtsdo.db.bdb.sap.TimeStatusPosition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TimeStatusPositionTest {
	
	long[] times;
	int[] statusNids;
	int[] pathNids;

	@Before
	public void setUp() throws Exception {
		//                       0  1   2  3                  4                  5                  6                  7                  8
		times = new long[]     { 1, 0, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, 0,                 0, 
				4, 5, 256, 1024, 2048, 4096, -4096, -2048, -1024, -256, -5, -4 };
		statusNids = new int[] { 1, 0, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, 0,                 Integer.MAX_VALUE,
				4, 5, 256, 1024, 2048, 4096, -4096, -2048, -1024, -256, -5, -4};
		pathNids = new int[]   { -1, 1, 0, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 
				5, 256, 1024, 2048, 4096, -4096, -2048, -1024, -256, -5, -4, 4};
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTimeStatusPositionToUuid() {
		for (int i = 0; i < times.length; i++) {
/*			System.out.println();
			System.out.println(i);*/
			long[] uuid = TimeStatusPosition.timeStatusPositionToUuid(times[i], statusNids[i], pathNids[i]);
			//System.out.println(times[i]  + " " +  statusNids[i]  + " " +  pathNids[i]);
			assertTrue(times[i] == TimeStatusPosition.getTime(uuid));
/*			System.out.println(TimeStatusPosition.getTime(uuid) + " " + 
					TimeStatusPosition.getStatusNid(uuid) + " " + 
					TimeStatusPosition.getPathNid(uuid));*/
			assertTrue(pathNids[i] == TimeStatusPosition.getPathNid(uuid));
/*			System.out.println(TimeStatusPosition.getTime(uuid) + " " + 
					TimeStatusPosition.getStatusNid(uuid) + " " + 
					TimeStatusPosition.getPathNid(uuid));*/
			assertTrue(statusNids[i] == TimeStatusPosition.getStatusNid(uuid));
/*			System.out.println(TimeStatusPosition.getTime(uuid) + " " + 
					TimeStatusPosition.getStatusNid(uuid) + " " + 
					TimeStatusPosition.getPathNid(uuid));*/
		}
		
	}

	@Test
	public void testGetTime() {
		for (int i = 0; i < times.length; i++) {
			testGetTime(times[i], statusNids[i], pathNids[i]);
		}
	}

	private void testGetTime(long time, int statusNid, int pathNid) {
		long[] uuid = TimeStatusPosition.timeStatusPositionToUuid(time, statusNid, pathNid);
		assertTrue(time == TimeStatusPosition.getTime(uuid));
	}

	@Test
	public void testGetStatusNid() {
		for (int i = 0; i < times.length; i++) {
			testGetStatusNid(times[i], statusNids[i], pathNids[i]);
		}
	}

	private void testGetStatusNid(long time, int statusNid, int pathNid) {
		long[] uuid = TimeStatusPosition.timeStatusPositionToUuid(time, statusNid, pathNid);
		assertTrue(statusNid == TimeStatusPosition.getStatusNid(uuid));
	}

	@Test
	public void testGetPathNid() {
		for (int i = 0; i < times.length; i++) {
			testGetPositionNid(times[i], statusNids[i], pathNids[i]);
		}
	}

	private void testGetPositionNid(long time, int statusNid, int pathNid) {
		long[] uuid = TimeStatusPosition.timeStatusPositionToUuid(time, statusNid, pathNid);
		assertTrue(pathNid == TimeStatusPosition.getPathNid(uuid));
	}

}
