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

public abstract class EComponent extends EVersion implements Externalizable {

	public static final long serialVersionUID = 1;
		
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

	protected List<EIdentifierVersion> idComponents;

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
			idComponents = new ArrayList<EIdentifierVersion>(idVersionCount);
			for (int i = 0; i < idVersionCount; i++) {
				switch (IDENTIFIER_PART_TYPES.readType(in)) {
				case LONG:
					idComponents.add(new EIdentifierVersionLong(in));
					break;
				case STRING:
					idComponents.add(new EIdentifierVersionString(in));
					break;
				case UUID:
					idComponents.add(new EIdentifierVersionUuid(in));
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
		if (idComponents == null) {
			out.writeShort(0);
		} else {
			out.writeShort(idComponents.size());
			for (EIdentifierVersion idv : idComponents) {
				idv.writeExternal(out);
			}
		}
	}

	public void convert(I_Identify id) throws TerminologyException, IOException {
		boolean primordialWritten = false;
		int partCount = id.getMutableIdParts().size() - 1;
		if (partCount > 0) {
			idComponents = new ArrayList<EIdentifierVersion>(partCount);
			for (I_IdPart idp : id.getMutableIdParts()) {
				Object denotation = idp.getDenotation();
				switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
				case LONG:
					idComponents.add(new EIdentifierVersionLong(idp));
					break;
				case STRING:
					idComponents.add(new EIdentifierVersionString(idp));
					break;
				case UUID:
					if (primordialWritten) {
						idComponents.add(new EIdentifierVersionUuid(idp));
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
	
	public int getIdComponentCount() {
		if (idComponents == null) {
			return 1;
		}
		return idComponents.size() + 1;
	}

	public List<EIdentifierVersion> getEIdentifiers() {
		List<EIdentifierVersion> ids;
		if (idComponents != null) {
			ids = new ArrayList<EIdentifierVersion>(idComponents.size() + 1);
			ids.addAll(idComponents);
		} else {
			ids = new ArrayList<EIdentifierVersion>(1);
		}
		ids.add(new EIdentifierVersionUuid(this));
		return ids;
	}
	
	public List<UUID> getUuids() {
		List<UUID> uuids = new ArrayList<UUID>();
		uuids.add(primordialComponentUuid);
		if (idComponents != null) {
			for (EIdentifierVersion idv: idComponents) {
				if (EIdentifierVersionUuid.class.isAssignableFrom(idv.getClass())) {
					uuids.add((UUID) idv.getDenotation());
				}
			}
		}
		return uuids;
	}
	
	public int getVersionCount() {
		List<? extends EVersion> extraVersions = getExtraVersionsList();
		if (extraVersions == null) {
			return 1;
		}
		return extraVersions.size() + 1;
	}
	
	public abstract List<? extends EVersion> getExtraVersionsList();
}
