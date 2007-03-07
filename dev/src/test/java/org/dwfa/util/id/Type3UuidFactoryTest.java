package org.dwfa.util.id;

import static org.dwfa.util.id.Type3UuidFactory.SNOMED_ISA_REL;
import static org.dwfa.util.id.Type3UuidFactory.SNOMED_ISA_REL_UUID;
import static org.dwfa.util.id.Type3UuidFactory.SNOMED_ROOT_CONCEPTID;
import static org.dwfa.util.id.Type3UuidFactory.SNOMED_ROOT_DESCID;
import static org.dwfa.util.id.Type3UuidFactory.SNOMED_ROOT_DESC_UUID;
import static org.dwfa.util.id.Type3UuidFactory.SNOMED_ROOT_UUID;

import java.util.UUID;

import junit.framework.TestCase;

public class Type3UuidFactoryTest extends TestCase {

	private enum TestEnum {
		TEST1(UUID.fromString("a3223f79-b208-3be7-938e-4d884c691eee")), TEST2(
				UUID.fromString("024e8661-e896-39e9-87ee-047048eadf84"));

		private UUID generatedId;

		private TestEnum(UUID generatedId) {
			this.generatedId = generatedId;
		}

		public UUID getGeneratedId() {
			return generatedId;
		}

	}

	public void testFromSNOMEDString() {
		UUID uid = Type3UuidFactory.fromSNOMED(new Long(SNOMED_ROOT_CONCEPTID)
				.toString());
		if (uid.equals(SNOMED_ROOT_UUID) == false) {
			fail("UUIDs not equal");
		}
		uid = Type3UuidFactory.fromSNOMED(new Long(SNOMED_ROOT_DESCID)
				.toString());
		if (uid.equals(SNOMED_ROOT_DESC_UUID) == false) {
			fail("UUIDs not equal");
		}
		uid = Type3UuidFactory.fromSNOMED(new Long(SNOMED_ISA_REL).toString());
		if (uid.equals(SNOMED_ISA_REL_UUID) == false) {
			fail("UUIDs not equal");
		}
	}

	public void testFromSNOMEDLong() {
		UUID uid = Type3UuidFactory.fromSNOMED(new Long(SNOMED_ROOT_CONCEPTID));
		if (uid.equals(SNOMED_ROOT_UUID) == false) {
			fail("UUIDs not equal");
		}
	}

	public void testFromSNOMEDLong1() {
		UUID uid = Type3UuidFactory.fromSNOMED(SNOMED_ROOT_CONCEPTID);
		if (uid.equals(SNOMED_ROOT_UUID) == false) {
			fail("UUIDs not equal");
		}
	}

	public void testFromEnum() {
		for (TestEnum e : TestEnum.values()) {
			if (e.getGeneratedId().equals(Type3UuidFactory.fromEnum(e)) == false) {
				fail("UUIDs not equal");
			}
		}
	}

}
