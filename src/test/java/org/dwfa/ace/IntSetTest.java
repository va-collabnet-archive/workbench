package org.dwfa.ace;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.vodb.types.IntSet;

import junit.framework.TestCase;

public class IntSetTest extends TestCase {

	public IntSetTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void test() {
		I_IntSet set = new IntSet();
		set.add(1);
		set.add(3);
		set.add(2);
		set.add(5);
		
		assertTrue(set.contains(1));
		assertTrue(set.contains(2));
		assertTrue(set.contains(3));
		assertFalse(set.contains(4));
		assertTrue(set.contains(5));
		set.clear();
		set.addAll(new int[] {1,3,2,5});
		assertTrue(set.contains(1));
		assertTrue(set.contains(2));
		assertTrue(set.contains(3));
		assertFalse(set.contains(4));
		assertTrue(set.contains(5));

	}
}
