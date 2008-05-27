package org.dwfa.derby.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import junit.framework.TestCase;

import org.dwfa.util.id.Type5UuidFactory;

public class DerbyBackedUuidSctidFixedMapTest extends TestCase {

	private File testRoot = new File("target", "test");
	private File testMap = new File(testRoot, "sample.map");
	private DerbyBackedUuidSctidFixedMap mapDb;
	private int mapSize = 50000;
	
	
	private void generateTestMap() throws IOException, NoSuchAlgorithmException {
		testMap.getParentFile().mkdirs();
		BufferedWriter bw = new BufferedWriter(new FileWriter(testMap));
		for (int i = 0; i < mapSize; i++) {
			bw.append(Type5UuidFactory.get("first" + Integer.toString(i)).toString());
			if (i % 10 == 0) {
				bw.append("\t");
				bw.append(Type5UuidFactory.get("second" + Integer.toString(i)).toString());
			}
			bw.append("\n");
			bw.append(Long.toString(i));
			bw.append("\n");
		}
		bw.close();
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		generateTestMap();
		mapDb = DerbyBackedUuidSctidFixedMap.read(testMap);
		assertTrue(mapSize < mapDb.size());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void test() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		Long sctId = mapDb.get(Type5UuidFactory.get("first" + Integer.toString(0)));
		assertEquals(sctId.longValue(), 0L);
		sctId = mapDb.get(Type5UuidFactory.get("second" + Integer.toString(0)));
		assertEquals(sctId.longValue(), 0L);
		sctId = mapDb.get(Type5UuidFactory.get("first" + Integer.toString(1)));
		assertEquals(sctId.longValue(), 1L);
	}
	
	
}
