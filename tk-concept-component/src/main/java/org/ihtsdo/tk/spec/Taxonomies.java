package org.ihtsdo.tk.spec;

import java.util.UUID;

public class Taxonomies {

	public static ConceptSpec WB_AUX = new ConceptSpec("Terminology Auxiliary concept", UUID.fromString("f4d2fabc-7e96-3b3a-a348-ae867ba74029"));
	public static ConceptSpec REFSET_AUX = new ConceptSpec("Refset Auxiliary Concept", UUID.fromString("1c698388-c309-3dfa-96f0-86248753fac5"));
	public static ConceptSpec QUEUE_TYPE = new ConceptSpec("Queue Type", UUID.fromString("fb78d89e-6953-3456-8903-8ee9d25539bc"));
	public static ConceptSpec SNOMED = new ConceptSpec("SNOMED CT Concept", UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
}
