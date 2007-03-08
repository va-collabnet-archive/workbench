package org.dwfa.vodb.bind;

import java.util.Date;

import junit.framework.TestCase;

public class ThinVersionHelperTest extends TestCase {

	public void testConvert() {
		Date now = new Date();
		long thickTime = now.getTime();
		thickTime = thickTime/1000;
		thickTime = thickTime * 1000;
		
		int thinTime = ThinVersionHelper.convert(thickTime);
		long convertBack = ThinVersionHelper.convert(thinTime);
		assertEquals(thickTime, convertBack);
	}
}
