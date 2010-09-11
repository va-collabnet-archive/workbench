package org.ihtsdo.db.bdb;


import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.dwfa.util.io.FileIO;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NidCNidMapBdbTest {
	private static final int TEST_SIZE = 50000;
	String dbTarget;

	@Before
	public void setUp() throws Exception {
		dbTarget = "target/" + UUID.randomUUID();
		Bdb.setup(dbTarget);
	}

	@Test
	public void addRetrieveTest() {
	    int firstDiff = Integer.MAX_VALUE;
		for (int i = 0; i < TEST_SIZE; i++) {
			int nid = Bdb.getUuidsToNidMap().uuidToNid(UUID.randomUUID());
			if (firstDiff == Integer.MAX_VALUE) {
			    firstDiff = i - nid;
			}
			Assert.assertEquals(nid, i - firstDiff);
			try {
				Bdb.getNidCNidMap().setCNidForNid(Integer.MIN_VALUE + i, nid);
			} catch (IOException e) {
				Assert.fail(e.getLocalizedMessage());
			}
		}
		for (int i = 0; i < TEST_SIZE; i++) {
			Assert.assertEquals(Integer.MIN_VALUE + i, 
					Bdb.getNidCNidMap().getCNid(i - firstDiff));
		}
		try {
			Bdb.close();
		} catch (InterruptedException e) {
			Assert.fail(e.getLocalizedMessage());
		} catch (ExecutionException e) {
			Assert.fail(e.getLocalizedMessage());
		}
		Bdb.setup(dbTarget);
		for (int i = 0; i < TEST_SIZE; i++) {
			Assert.assertEquals(Integer.MIN_VALUE + i, 
					Bdb.getNidCNidMap().getCNid(i - firstDiff));
		}
	}
	
	@After
	public void tearDown() throws Exception {
		Bdb.close();
		FileIO.recursiveDelete(new File(dbTarget));
	}

}
