package org.ihtsdo.etypes;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;

public class EVersion implements Externalizable, I_VersionExternally {
	
	private static I_TermFactory tf = LocalVersionedTerminology.get();
	
	protected static UUID nidToUuid(int nid) throws TerminologyException, IOException {
		return tf.getId(nid).getUUIDs().iterator().next();
	}

	protected static int uuidToNid(Collection<UUID> collection) throws TerminologyException, IOException {
		return tf.uuidToNative(collection);
	}

	protected static I_Identify nidToIdentifier(int nid) throws TerminologyException, IOException {
		return tf.getId(nid);
	}

	protected static List<I_ThinExtByRefVersioned> getRefsetMembers(int nid) throws TerminologyException, IOException {
		return tf.getRefsetExtensionMembers(nid);
	}


	protected UUID pathUuid;
	protected UUID statusUuid;
	protected long time;
	
	public EVersion(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}
	
	public EVersion() {
		super();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		pathUuid = new UUID(in.readLong(), in.readLong());
		statusUuid = new UUID(in.readLong(), in.readLong());
		time = in.readLong();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(pathUuid.getMostSignificantBits());
		out.writeLong(pathUuid.getLeastSignificantBits());
		out.writeLong(statusUuid.getMostSignificantBits());
		out.writeLong(statusUuid.getLeastSignificantBits());
		out.writeLong(time);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.etypes.I_VersionExternal#getPathUuid()
	 */
	public UUID getPathUuid() {
		return pathUuid;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.etypes.I_VersionExternal#getStatusUuid()
	 */
	public UUID getStatusUuid() {
		return statusUuid;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.etypes.I_VersionExternal#getTime()
	 */
	public long getTime() {
		return time;
	}
}
