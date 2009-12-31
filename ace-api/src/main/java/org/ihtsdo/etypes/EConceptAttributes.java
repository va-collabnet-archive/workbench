package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.tapi.TerminologyException;

public class EConceptAttributes extends EComponent {

	private boolean defined;

	private List<EConceptAttributesVersion> versions;

	public EConceptAttributes(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public EConceptAttributes(I_ConceptAttributeVersioned conceptAttributes) throws TerminologyException, IOException {
		super();
		int partCount = conceptAttributes.getMutableParts().size();
		I_ConceptAttributePart part = conceptAttributes.getMutableParts().get(0);
		defined = part.isDefined();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			versions = new ArrayList<EConceptAttributesVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				versions.add(new EConceptAttributesVersion(conceptAttributes.getMutableParts().get(i)));
			}
		} 
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		defined = in.readBoolean();
		int versionCount = in.readInt();
		versions = new ArrayList<EConceptAttributesVersion>();
		if (versionCount > 0) {
			for (int i = 0; i < versionCount; i++) {
				versions.add(new EConceptAttributesVersion(in));
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeBoolean(defined);
		if (versions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(versions.size());
			for (EConceptAttributesVersion cav: versions) {
				cav.writeExternal(out);
			}
		}
	}

	public boolean isDefined() {
		return defined;
	}

	public void setDefined(boolean defined) {
		this.defined = defined;
	}
}