package org.ihtsdo.etypes;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.tapi.TerminologyException;

public class EComponent extends EVersion implements Externalizable {

	private static final int dataVersion = 1;

	public enum IDENTIFIER_PART_TYPES {
		LONG(1), STRING(2), UUID(3);

		private int partTypeId;

		IDENTIFIER_PART_TYPES(int partTypeId) {
			this.partTypeId = partTypeId;
		}

		public void writeType(ObjectOutput output) throws IOException {
			output.writeByte(partTypeId);
		}

		public static IDENTIFIER_PART_TYPES getType(Class<?> c) {
			if (Long.class.isAssignableFrom(c)) {
				return LONG;
			} else if (String.class.isAssignableFrom(c)) {
				return STRING;
			} else if (UUID.class.isAssignableFrom(c)) {
				return UUID;
			} 
			throw new UnsupportedOperationException();
		}

		public static IDENTIFIER_PART_TYPES readType(ObjectInput input)
				throws IOException {
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

	protected UUID primordialComponentUuid;

	protected List<EIdentifierVersion> idVersions;

	public EComponent() {
		super();
	}

	public EComponent(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super();
		readExternal(in);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		int readDataVersion = in.readInt();
		if (readDataVersion != dataVersion) {
			throw new IOException("Unsupported dataVersion: " + readDataVersion);
		}
		primordialComponentUuid = new UUID(in.readLong(), in.readLong());
		short idVersionCount = in.readShort();
		if (idVersionCount > 0) {
			idVersions = new ArrayList<EIdentifierVersion>(idVersionCount);
			for (int i = 0; i < idVersionCount; i++) {
				switch (IDENTIFIER_PART_TYPES.readType(in)) {
				case LONG:
					idVersions.add(new EIdentifierVersionLong(in));
					break;
				case STRING:
					idVersions.add(new EIdentifierVersionString(in));
					break;
				case UUID:
					idVersions.add(new EIdentifierVersionUuid(in));
					break;
				default:
					throw new UnsupportedOperationException();
				}
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(dataVersion);
		out.writeLong(primordialComponentUuid.getMostSignificantBits());
		out.writeLong(primordialComponentUuid.getLeastSignificantBits());
		if (idVersions == null) {
			out.writeShort(0);
		} else {
			out.writeShort(idVersions.size());
			for (EIdentifierVersion idv : idVersions) {
				idv.writeExternal(out);
			}
		}
	}

	public void convert(I_Identify id) throws TerminologyException, IOException {
		boolean primordialWritten = false;
		int partCount = id.getMutableIdParts().size() - 1;
		if (partCount > 0) {
			idVersions = new ArrayList<EIdentifierVersion>(partCount);
			for (I_IdPart idp : id.getMutableIdParts()) {
				Object denotation = idp.getDenotation();
				switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
				case LONG:
					idVersions.add(new EIdentifierVersionLong(idp));
					break;
				case STRING:
					idVersions.add(new EIdentifierVersionString(idp));
					break;
				case UUID:
					if (primordialWritten) {
						idVersions.add(new EIdentifierVersionUuid(idp));
					} else {
						primordialComponentUuid = (UUID) idp.getDenotation();
						primordialWritten = true;
					}
					break;
				default:
					throw new UnsupportedOperationException();
				}

			}
		} else {
			primordialComponentUuid = (UUID) id.getUUIDs().get(0);
		}
	}

	public List<EIdentifierVersion> getEIdentifiers() {
		List<EIdentifierVersion> ids;
		if (idVersions != null) {
			ids = new ArrayList<EIdentifierVersion>(idVersions.size() + 1);
			ids.addAll(idVersions);
		} else {
			ids = new ArrayList<EIdentifierVersion>(1);
		}
		ids.add(new EIdentifierVersionUuid(this));
		return ids;
	}
}
