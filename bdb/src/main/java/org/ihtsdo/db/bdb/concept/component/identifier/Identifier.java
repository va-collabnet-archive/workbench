package org.ihtsdo.db.bdb.concept.component.identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Identifier extends ConceptComponent<IdentifierMutablePart>
		implements I_IdVersioned {

	protected enum VARIABLE_PART_TYPES {
		LONG(1), STRING(2), UUID(3);

		private int partTypeId;

		VARIABLE_PART_TYPES(int partTypeId) {
			this.partTypeId = partTypeId;
		}

		public void writeType(TupleOutput output) {
			output.writeByte(partTypeId);
		}

		public static VARIABLE_PART_TYPES readType(TupleInput input) {
			switch (input.readByte()) {
			case 1:
				return LONG;
			case 2:
				return STRING;
			case 3:
				return UUID;
			}
			throw new UnsupportedOperationException();
		}
	};

	protected Identifier(int nid, int listSize, boolean editable) {
		super(nid, listSize, editable);
	}

	@Override
	public void readComponentFromBdb(TupleInput input, int conceptNid, int listSize) {
		// nid, list size, and conceptNid are read already by the binder...
		for (int i = 0; i < listSize; i++) {
			switch (VARIABLE_PART_TYPES.readType(input)) {
			case LONG:
				mutableParts.add(new IdentifierMutablePartLong(input));
				break;
			case STRING:
				mutableParts.add(new IdentifierMutablePartString(input));
				break;
			case UUID:
				mutableParts.add(new IdentifierMutablePartUuid(input));
				break;
				default:
					throw new UnsupportedOperationException();
			}
		}
	}

	@Override
	public void writeComponentToBdb(TupleOutput output,
			int maxReadOnlyStatusAtPositionNid) {
		List<IdentifierMutablePart> partsToWrite = new ArrayList<IdentifierMutablePart>();
		for (IdentifierMutablePart p: mutableParts) {
			if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
				partsToWrite.add(p);
			}
		}
		// Start writing
		output.writeInt(nid);
		output.writeShort(partsToWrite.size());
		for (IdentifierMutablePart p: partsToWrite) {
			p.getType().writeType(output);
			p.writePartToBdb(output);
		}
	}

	@Override
	public boolean addVersion(I_IdPart srcId) {
		return mutableParts.add((IdentifierMutablePart) srcId);
	}

	@Override
	public List<I_IdTuple> getTuples() {
		List<I_IdTuple> returnValues = new ArrayList<I_IdTuple>();
		for (IdentifierMutablePart idv : mutableParts) {
			returnValues.add(new IdentifierVersion(this, idv));
		}
		return returnValues;
	}

	@Override
	public List<UUID> getUIDs() {
		List<UUID> returnValues = new ArrayList<UUID>();
		for (IdentifierMutablePart idv : mutableParts) {
			if (IdentifierMutablePartUuid.class.isAssignableFrom(idv
					.getClass())) {
				IdentifierMutablePartUuid uuidPart = (IdentifierMutablePartUuid) idv;
				returnValues.add(uuidPart.getUuid());
			}
		}
		return returnValues;
	}

	@Override
	public UniversalAceIdentification getUniversal() throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNativeId(int nativeId) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasVersion(I_IdPart newPart) {
		return super.hasVersion((IdentifierMutablePart) newPart);
	}

}
