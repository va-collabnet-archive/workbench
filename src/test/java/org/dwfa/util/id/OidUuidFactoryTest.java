package org.dwfa.util.id;

import java.util.UUID;

import junit.framework.TestCase;

public class OidUuidFactoryTest extends TestCase {
	
	/*Tests derived from
	 * http://www.itu.int/ITU-T/studygroups/com17/oid/X.667-E.pdf
	EXAMPLE Ð The following is an example of the string 
	          representation of a UUID as a URN:
		urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6
		
		NOTE Ð An alternative URN format (see [6]) is available, 
		but is not recommended for URNs generated using UUIDs. This
		alternative format uses the single integer value of the UUID 
		specified in 6.3, and represents the above example as
		
		"urn:oid:2.25.329800735698586629295641978511506172918".	
	*/
	String testOid = "2.25.329800735698586629295641978511506172918";
	UUID testUuid = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");

	public void testGetUUID() {
		UUID uid = OidUuidFactory.get(testOid);
		assertEquals(testUuid, uid);
	}

	public void testGetString() {
		String oid = OidUuidFactory.get(testUuid);
		assertEquals(testOid, oid);
	}

}
