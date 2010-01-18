/**
 * 
 */
package org.ihtsdo.db.bdb.concept;

import com.sleepycat.bind.tuple.TupleInput;

public enum OFFSETS {
	FORMAT_VERSION(4, null),
	DATA_VERSION(4, FORMAT_VERSION),
	ATTRIBUTES(4, DATA_VERSION),
	DESCRIPTIONS(4, ATTRIBUTES),
	SOURCE_RELS(4, DESCRIPTIONS),
	REFSET_MEMBERS(4, SOURCE_RELS), 
	DEST_REL_NID_TYPE_NIDS(4, REFSET_MEMBERS), 
	REFSETNID_MEMBERNID_FOR_CONCEPT(4, DEST_REL_NID_TYPE_NIDS), 
	REFSETNID_MEMBERNID_FOR_DESCRIPTIONS(4, REFSETNID_MEMBERNID_FOR_CONCEPT), 
	REFSETNID_MEMBERNID_FOR_RELATIONSHIPS(4, REFSETNID_MEMBERNID_FOR_DESCRIPTIONS),
	REFSETNID_MEMBERNID_FOR_IMAGES(4, REFSETNID_MEMBERNID_FOR_RELATIONSHIPS),
	REFSETNID_MEMBERNID_FOR_REFSETMEMBERS(4, REFSETNID_MEMBERNID_FOR_IMAGES),
	DESC_NIDS(4, REFSETNID_MEMBERNID_FOR_REFSETMEMBERS), 
	SRC_REL_NIDS(4, DESC_NIDS), 
	IMAGE_NIDS(4, SRC_REL_NIDS), 
	IMAGES(4, IMAGE_NIDS),
	HEADER_SIZE(0, IMAGES);
	
	public static int CURRENT_FORMAT_VERSION = 1;
	public static int CURRENT_DATA_VERSION = 1;

	
	protected int offset;
	protected int bytes;
	protected OFFSETS prev;
	
	OFFSETS(int bytes, OFFSETS prev) {
		this.bytes = bytes;
		if (prev == null) {
			offset = 0;
		} else {
			offset = prev.offset + prev.bytes;
		}
	}
	
	int getOffset(byte[] data) {
		TupleInput offsetInput = new TupleInput(data);
		offsetInput.skipFast(offset);
		return offsetInput.readInt();
	}

	public int getOffset() {
		return offset;
	}

	public int getBytes() {
		return bytes;
	}
	
	public static int getHeaderSize() {
		return HEADER_SIZE.getOffset();
	}
}