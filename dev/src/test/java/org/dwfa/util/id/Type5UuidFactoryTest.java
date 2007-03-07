package org.dwfa.util.id;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import junit.framework.TestCase;

public class Type5UuidFactoryTest extends TestCase {

	public void testGet() {
		try {
			UUID nameSpace_DNS = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
			UUID testId = UUID.fromString("886313e1-3b8a-5372-9b90-0c9aee199e5d");
			UUID generatedId = Type5UuidFactory.get(nameSpace_DNS, "python.org");
			
			assertEquals(testId, generatedId);
		} catch (NoSuchAlgorithmException e) {
			fail(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			fail(e.getMessage());
		}
		
	}
	/*
	public void testFromSNOMED() {
		
		uuid.uuid5(uuid.NAMESPACE_DNS, 'python.org')
		+    UUID('886313e1-3b8a-5372-9b90-0c9aee199e5d');
		
		fail("Not yet implemented");
	}
	*/
}
