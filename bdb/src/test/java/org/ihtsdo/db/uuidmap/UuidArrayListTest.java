package org.ihtsdo.db.uuidmap;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UuidArrayListTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReverse() {
		int testSize = 1000;
		UuidArrayList testList = new UuidArrayList();
		for (int i = 0; i < testSize; i++) {
			testList.add(UuidUtil.convert(UUID.randomUUID()));
		}
		testList.sort();
		for (int i = 1; i < testSize; i++) {
			UUID before = UuidUtil.convert(testList.get(i - 1));
			UUID after = UuidUtil.convert(testList.get(i));
			assertTrue(before.toString().compareTo(after.toString()) < 0);
		}
		testList.reverse();
		for (int i = 1; i < testSize; i++) {
			UUID before = UuidUtil.convert(testList.get(i - 1));
			UUID after = UuidUtil.convert(testList.get(i));
			assertTrue(before.toString().compareTo(after.toString()) > 0);
		}
		testList.reverse();
		for (int i = 1; i < testSize; i++) {
			UUID before = UuidUtil.convert(testList.get(i - 1));
			UUID after = UuidUtil.convert(testList.get(i));
			assertTrue(before.toString().compareTo(after.toString()) < 0);
		}
	}

	@Test
	public void testAdd() {
		UuidArrayList testList = new UuidArrayList();
		assertTrue(testList.size == 0);
		UUID testUuid = UUID.randomUUID();
		testList.add(UuidUtil.convert(testUuid));
		assertTrue(testList.size == 1);
		long[] testArray = testList.get(0);
		assertTrue(testUuid.equals(UuidUtil.convert(testArray)));
	}

	@Test
	public void testSize() {
		int testSize = 1000;
		UuidArrayList testList = new UuidArrayList();
		assertTrue(testList.size() == 0);
		testList = new UuidArrayList(1000);
		assertTrue(testList.size() == 0);
		for (int i = 0; i < testSize; i++) {
			testList.add(UuidUtil.convert(UUID.randomUUID()));
		}
		assertTrue(testList.size() == testSize);
		for (int i = 0; i < testSize; i++) {
			testList.remove(0);
		}
		assertTrue(testList.size() == 0);
 	}


	@Test
	public void testSort() {
		int testSize = 1000;
		UuidArrayList testList = new UuidArrayList();
		for (int i = 0; i < testSize; i++) {
			testList.add(UuidUtil.convert(UUID.randomUUID()));
		}
		testList.sort();
		for (int i = 1; i < testSize; i++) {
			UUID before = UuidUtil.convert(testList.get(i - 1));
			UUID after = UuidUtil.convert(testList.get(i));
			assertTrue(before.toString().compareTo(after.toString()) < 0);
		}
	}

}
