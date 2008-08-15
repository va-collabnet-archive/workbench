package org.dwfa.vodb.process;
import java.text.ParseException;

import junit.framework.TestCase;


public class TestProcessSources extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetDate() {
		try {
			String dateStr = "20080831T000000Z";
			ProcessDates.getDate(dateStr);
		} catch (ParseException e) {
			fail(e.toString());
		}
	}

}
