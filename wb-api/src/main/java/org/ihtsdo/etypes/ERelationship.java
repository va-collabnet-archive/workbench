package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

public class ERelationship extends TkRelationship {

    public static final long serialVersionUID = 1;

    public ERelationship(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public ERelationship(I_RelVersioned<?> rel) throws TerminologyException, IOException {
        if (I_Identify.class.isAssignableFrom(rel.getClass())) {
            EConcept.convertId((I_Identify) rel, this);
        } else {
            EConcept.convertId(Terms.get().getId(rel.getNid()), this);
        }
        int partCount = rel.getMutableParts().size();
        I_RelPart part = rel.getMutableParts().get(0);
        c1Uuid = Terms.get().nidToUuid(rel.getC1Id());
        c2Uuid = Terms.get().nidToUuid(rel.getC2Id());
        characteristicUuid = Terms.get().nidToUuid(part.getCharacteristicId());
        refinabilityUuid = Terms.get().nidToUuid(part.getRefinabilityId());
        relGroup = part.getGroup();
        typeUuid = Terms.get().nidToUuid(part.getTypeNid());
        pathUuid = Terms.get().nidToUuid(part.getPathNid());
        statusUuid = Terms.get().nidToUuid(part.getStatusNid());
        time = part.getTime();
        if (partCount > 1) {
            revisions = new ArrayList<TkRelationshipRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                revisions.add(new ERelationshipRevision(rel.getMutableParts().get(i)));
            }
        }
        
        List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(rel.getNid());
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

    public ERelationship() {
        super();
    }

}
