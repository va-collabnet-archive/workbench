package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UniversalIdList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int dataVersion = 1;

	private List<UniversalAceIdentification> uncommittedIds = new ArrayList<UniversalAceIdentification>();

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(uncommittedIds);
	}
	
	public String toString() {
		return "UniversalIdList: " +
				"\n uncommittedIds: " + uncommittedIds; 
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			uncommittedIds = (List<UniversalAceIdentification>) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public List<UniversalAceIdentification> getUncommittedIds() {
		return uncommittedIds;
	}

	public void setUncommittedIds(List<UniversalAceIdentification> uncommittedIds) {
		this.uncommittedIds = uncommittedIds;
	}
}
