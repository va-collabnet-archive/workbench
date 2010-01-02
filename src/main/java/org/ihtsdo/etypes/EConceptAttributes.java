package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.tapi.TerminologyException;

public class EConceptAttributes extends EComponent 
	implements I_ConceptualizeExternally {
	public static final long serialVersionUID = 1;

	private boolean defined;

	private List<EConceptAttributesVersion> extraVersions;

	public EConceptAttributes(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public EConceptAttributes(I_ConceptAttributeVersioned conceptAttributes) throws TerminologyException, IOException {
		super();
		convert(nidToIdentifier(conceptAttributes.getNid()));
		int partCount = conceptAttributes.getMutableParts().size();
		I_ConceptAttributePart part = conceptAttributes.getMutableParts().get(0);
		defined = part.isDefined();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
		if (partCount > 1) {
			extraVersions = new ArrayList<EConceptAttributesVersion>(partCount -1);
			for (int i = 1; i < partCount; i++) {
				extraVersions.add(new EConceptAttributesVersion(conceptAttributes.getMutableParts().get(i)));
			}
		} 
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		defined = in.readBoolean();
		int versionCount = in.readInt();
		extraVersions = new ArrayList<EConceptAttributesVersion>();
		if (versionCount > 0) {
			for (int i = 0; i < versionCount; i++) {
				extraVersions.add(new EConceptAttributesVersion(in));
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeBoolean(defined);
		if (extraVersions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(extraVersions.size());
			for (EConceptAttributesVersion cav: extraVersions) {
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
	
	public List<EConceptAttributesVersion> getExtraVersionsList() {
		return extraVersions;
	}
}