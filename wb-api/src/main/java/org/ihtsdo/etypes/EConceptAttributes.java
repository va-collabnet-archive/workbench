package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

public class EConceptAttributes extends TkConceptAttributes {
	public static final long serialVersionUID = 1;

	public EConceptAttributes() {
		super();
	}

	public EConceptAttributes(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
		super(in, dataVersion);
	}

	public EConceptAttributes(I_ConceptAttributeVersioned<?> conceptAttributes) throws TerminologyException, IOException {
		super();
		EConcept.convertId(Terms.get().getId(conceptAttributes.getNid()), this);
		int partCount = conceptAttributes.getMutableParts().size();
		I_ConceptAttributePart part = conceptAttributes.getMutableParts().get(0);
		defined = part.isDefined();
		pathUuid = Terms.get().nidToUuid(part.getPathId());
		statusUuid = Terms.get().nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			revisions = new ArrayList<TkConceptAttributesRevision>(partCount - 1);
			for (int i = 1; i < partCount; i++) {
				revisions.add(new EConceptAttributesRevision(conceptAttributes.getMutableParts().get(i)));
			}
		}


		List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(conceptAttributes.getNid());
		if (extensions != null) {
			this.annotations = new ArrayList<TkRefsetAbstractMember<?>>(extensions.size());
			for (I_ExtendByRef m : extensions) {
				if (Terms.get().getConcept(m.getRefsetId()).isAnnotationStyleRefex()) {
					TkRefsetAbstractMember<?> member = EConcept.convertRefsetMember(m);
					if (member != null) {
						this.annotations.add(member);
					} else {
						AceLog.getAppLog().severe("Could not convert refset member: " + m + ")");
					}
				}
			}
		}
	}

}