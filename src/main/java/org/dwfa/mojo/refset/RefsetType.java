/**
 * 
 */
package org.dwfa.mojo.refset;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.mojo.refset.writers.BooleanRefsetWriter;
import org.dwfa.mojo.refset.writers.ConceptIntegerRefsetWriter;
import org.dwfa.mojo.refset.writers.ConceptRefsetWriter;
import org.dwfa.mojo.refset.writers.IntegerRefsetWriter;
import org.dwfa.mojo.refset.writers.MemberRefsetWriter;
import org.dwfa.mojo.refset.writers.StringRefsetWriter;

enum RefsetType {
	CONCEPT(ConceptRefsetWriter.class),
	INTEGER(IntegerRefsetWriter.class),
	STRING(StringRefsetWriter.class),
	BOOLEAN(BooleanRefsetWriter.class),
	CONCEPT_INTEGER(ConceptIntegerRefsetWriter.class);

	private Class<? extends MemberRefsetWriter> refsetWriterClass;
	private MemberRefsetWriter refsetWriter = null;
	
	RefsetType(Class<? extends MemberRefsetWriter> refsetWriterClass) {
		this.refsetWriterClass = refsetWriterClass;
	}
	
	public static RefsetType getType(I_ThinExtByRefTuple thinExtByRefTuple) {
		I_ThinExtByRefPart part = thinExtByRefTuple.getPart();
		if (part instanceof I_ThinExtByRefPartBoolean) {
			return BOOLEAN;
		} else if (part instanceof I_ThinExtByRefPartConcept) {
			return CONCEPT;
		} else if (part instanceof I_ThinExtByRefPartConceptInt) {
			return CONCEPT_INTEGER;
		} else if (part instanceof I_ThinExtByRefPartInteger) {
			return INTEGER;
		} else if (part instanceof I_ThinExtByRefPartString) {
			return STRING;
		}
		
		throw new EnumConstantNotPresentException(RefsetType.class, "No enum for the class " + thinExtByRefTuple.getClass() + " exists");
	}

	public MemberRefsetWriter getRefsetWriter() throws InstantiationException, IllegalAccessException {
		if (refsetWriter == null) {
			refsetWriter = refsetWriterClass.newInstance();
		}
	
		return refsetWriter;
	}
	
	
}