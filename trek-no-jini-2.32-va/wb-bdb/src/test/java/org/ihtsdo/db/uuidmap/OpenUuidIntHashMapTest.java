package org.ihtsdo.db.uuidmap;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpenUuidIntHashMapTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAndPut() {
		int testSize = 100000;
		int repeats = 10;
		concurrentHashMapTest(testSize, repeats);
		openUuidIntHashMapTest(testSize, repeats);
		concurrentHashMapTest(testSize, repeats);
		openUuidIntHashMapTest(testSize, repeats);
	}


	private void openUuidIntHashMapTest(int testSize, int repeats) {
		UuidArrayList list;
		List<Long> testResults = new ArrayList<Long>();
		for (int r = 0; r < repeats; r++) {
		long start = System.currentTimeMillis();
                        UuidToIntHashMap nativeMap = new UuidToIntHashMap((int) (testSize/0.81), 0.0, 0.8);
			list = new UuidArrayList(testSize);
			for (int i = 0; i < testSize; i++) {
				UUID key = UUID.randomUUID();
				nativeMap.put(UuidUtil.convert(key), i);
				list.add(UuidUtil.convert(key));
			}
			for (int i = 0; i < testSize; i++) {
				int value = nativeMap.get(list.get(i));
				assertTrue(value == i);
			}
			testResults.add(System.currentTimeMillis() - start);
		}
		System.out.println("Test openUuidIntHashMapTest results: " + testResults);
		long sum = 0;
		for (Long result: testResults) {
			sum = sum + result;
		}
		long average = sum / testResults.size();
		System.out.println("Test openUuidIntHashMapTest average: " + average);
	}

	private void concurrentHashMapTest(int testSize, int repeats) {
		List<Long> testResults = new ArrayList<Long>();
		for (int r = 0; r < repeats; r++) {
                       long start = System.currentTimeMillis();
			ConcurrentHashMap<UUID, Integer> ids = new ConcurrentHashMap<UUID, Integer>(
					testSize);
			UuidArrayList list = new UuidArrayList(testSize);
			for (int i = 0; i < testSize; i++) {
				UUID key = UUID.randomUUID();
				ids.put(key, i);
				list.add(UuidUtil.convert(key));
			}
			for (int i = 0; i < testSize; i++) {
				int value = ids.get(UuidUtil.convert(list.get(i)));
				assertTrue(value == i);
			}
			testResults.add(System.currentTimeMillis() - start);
		}
		System.out.println("Test ConcurrentHashMap results: " + testResults);
		long sum = 0;
		for (Long result: testResults) {
			sum = sum + result;
		}
		long average = sum / testResults.size();
		System.out.println("Test ConcurrentHashMap average: " + average);
		
		
	}
}
