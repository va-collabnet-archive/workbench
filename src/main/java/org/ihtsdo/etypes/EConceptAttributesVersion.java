package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.tapi.TerminologyException;

public class EConceptAttributesVersion extends EVersion 
	implements I_ConceptualizeExternally {
	public static final long serialVersionUID = 1;

	protected boolean defined;

	public EConceptAttributesVersion(DataInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public EConceptAttributesVersion(I_ConceptAttributePart part) throws TerminologyException, IOException {
		defined = part.isDefined();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		defined = in.readBoolean();
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		out.writeBoolean(defined);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.etypes.I_ConceptualizeExternally#isDefined()
	 */
	public boolean isDefined() {
		return defined;
	}

	public void setDefined(boolean defined) {
		this.defined = defined;
	}
}
