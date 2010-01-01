package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.tapi.TerminologyException;

public class ERefsetCidFloatVersion extends EVersion {

	private UUID c1Uuid;
	private float floatValue;

	public ERefsetCidFloatVersion(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	public ERefsetCidFloatVersion(
			I_ThinExtByRefPartMeasurement part) throws TerminologyException, IOException {
		c1Uuid = nidToUuid(part.getUnitsOfMeasureId());
		floatValue = (float) part.getMeasurementValue();
		pathUuid = nidToUuid(part.getPathId());
		statusUuid = nidToUuid(part.getStatusId());
		time = part.getTime();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		c1Uuid = new UUID(in.readLong(), in.readLong());
		floatValue = in.readFloat();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(c1Uuid.getMostSignificantBits());
		out.writeLong(c1Uuid.getLeastSignificantBits());
		out.writeFloat(floatValue);
	}

	public float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}

	public UUID getC1Uuid() {
		return c1Uuid;
	}

	public void setC1Uuid(UUID c1Uuid) {
		this.c1Uuid = c1Uuid;
	}

}
