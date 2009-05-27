package org.dwfa.ace.task.refset.spec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class AddTextQueryToRefsetSpec extends AbstractAddRefsetSpecTask {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			//
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	protected int getRefsetPartTypeId() throws IOException, TerminologyException {
		int typeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION
				.localize().getNid();
		return typeId;
	}

	protected I_ThinExtByRefPart createAndPopulatePart(
			I_TermFactory tf, I_Path p, I_ConfigAceFrame configFrame) throws IOException,
			TerminologyException {
		I_ThinExtByRefPartConceptConceptString specPart = tf.newConceptConceptStringExtensionPart();
		specPart.setC1id(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.localize().getNid());
		specPart.setC2id(RefsetAuxiliary.Concept.CONCEPT_IS.localize().getNid());
		specPart.setStr("+corneal +abrasion");
		specPart.setPathId(p.getConceptId());
		specPart.setStatusId(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
		specPart.setVersion(Integer.MAX_VALUE);
		return specPart;
	}

}