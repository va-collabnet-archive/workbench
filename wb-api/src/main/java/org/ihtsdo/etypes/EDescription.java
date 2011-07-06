package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

public class EDescription extends TkDescription {
    public static final long serialVersionUID = 1;


    public EDescription(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EDescription(I_DescriptionVersioned<?> desc) throws TerminologyException, IOException {
        EConcept.convertId(Terms.get().getId(desc.getNid()), this);
        int partCount = desc.getMutableParts().size();
        I_DescriptionPart part = desc.getMutableParts().get(0);
        conceptUuid = Terms.get().nidToUuid(desc.getConceptNid());
        initialCaseSignificant = part.isInitialCaseSignificant();
        lang = part.getLang();
        text = part.getText();
        typeUuid = Terms.get().nidToUuid(part.getTypeNid());
        pathUuid = Terms.get().nidToUuid(part.getPathNid());
        statusUuid = Terms.get().nidToUuid(part.getStatusNid());
        time = part.getTime();
        if (partCount > 1) {
            revisions = new ArrayList<TkDescriptionRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                revisions.add(new EDescriptionRevision(desc.getMutableParts().get(i)));
            }
        }
        
        List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid());
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

    public EDescription() {
        super();
    }
}
