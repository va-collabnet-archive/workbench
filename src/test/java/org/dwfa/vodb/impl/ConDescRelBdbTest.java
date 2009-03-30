package org.dwfa.vodb.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.DataFormatException;

import junit.framework.TestCase;

import org.dwfa.ace.api.I_DescriptionVersioned;

public class ConDescRelBdbTest extends TestCase {
	
	public void testRoundTrip() {
		List<I_DescriptionVersioned> descriptions = null;
		try {
			ConDescRelBdb.DescriptionCompressionMap map1 = new ConDescRelBdb.DescriptionCompressionMap(descriptions);
			byte[] compressedMapBytes = map1.getBytes();
			ConDescRelBdb.DescriptionCompressionMap map2 = new ConDescRelBdb.DescriptionCompressionMap(compressedMapBytes);
			map2.call();

		} catch (UnsupportedEncodingException e) {
			fail(e.toString());
		} catch (DataFormatException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		}
	}

}
