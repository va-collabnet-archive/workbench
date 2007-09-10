package org.dwfa.cement;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;


public enum PrimordialId {
	UNASSIGNED_ID(0, UUID.fromString("1c423bfd-147a-11db-ac5d-0800200c9a66")),
	AUTHORITY_ID(1, UUID.randomUUID()),
	ACE_AUXILIARY_ID(2, UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")),
	CURRENT_ID(3, UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66")),
	ACE_AUX_ENCODING_ID(4, UUID.fromString("2faa9262-8fb2-11db-b606-0800200c9a66")),
	FULLY_SPECIFIED_DESCRIPTION_TYPE_ID(5, UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66")),
	XHTML_DEF_ID(6, UUID.fromString("5e1fe941-8faf-11db-b606-0800200c9a66")),
	IS_A_REL_ID(7, UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66")),
	DEFINING_CHARACTERISTIC_ID(8, UUID.fromString("a4c6bf72-8fb6-11db-b606-0800200c9a66")),
	NOT_REFINABLE_ID(9, UUID.fromString("e4cde443-8fb6-11db-b606-0800200c9a66")),
   PREFERED_TERM_ID(10, UUID.fromString("d8e3b37d-7c11-33ef-b1d0-8769e2264d44"));

	private int sequenceRelativeId;
	private Collection<UUID> uids;
	private PrimordialId(int sequenceRelativeId, UUID uid) {
		this(sequenceRelativeId, Arrays.asList(new UUID[] {uid}));
	}
	private PrimordialId(int sequenceRelativeId, Collection<UUID> uids) {
		this.sequenceRelativeId = sequenceRelativeId;
		this.uids = uids;
	}

	public int getNativeId(int sequenceStart) {
		return sequenceStart + sequenceRelativeId;
	}
	public long getNativeId(long sequenceStart) {
		return sequenceStart + sequenceRelativeId;
	}

	public Collection<UUID> getUids() {
		return uids;
	}
}
