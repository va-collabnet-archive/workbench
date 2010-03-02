package org.ihtsdo.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.ConceptComponentBinder;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.concept.component.attributes.ConceptAttributesRevision;
import org.ihtsdo.concept.component.description.DescriptionBinder;
import org.ihtsdo.concept.component.image.ImageBinder;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.concept.component.relationship.RelationshipBinder;
import org.ihtsdo.db.bdb.Bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptBinder extends TupleBinding<Concept> {

	public static final byte[] zeroOutputArray;
	static {
		TupleOutput zeroOutput = new TupleOutput();
		zeroOutput.writeInt(0);
		zeroOutputArray = zeroOutput.toByteArray();
	}

	@Override
	public Concept entryToObject(TupleInput input) {
		/*
		 * We don't retrieve the entire concept. Instead we just retrieve lists
		 * of concept components on demand. See getList in ConceptData.
		 */
		throw new UnsupportedOperationException();
	}

	@Override
	public void objectToEntry(Concept concept, TupleOutput finalOutput) {

		try {
			long dataVersion = concept.getDataVersion();
			if (Bdb.watchList.containsKey(concept.getNid())) {
				AceLog.getAppLog().info("Starting to write: " + concept.toLongString());
			}
			I_ManageConceptData conceptData = concept.getData();
			boolean primordial = conceptData.getReadOnlyBytes().length == 0
					&& conceptData.getReadWriteBytes().length == 0;

			byte[] attrOutput = getAttributeBytes(conceptData, primordial,
					OFFSETS.ATTRIBUTES, conceptData.getConceptAttributesIfChanged(),
					new ConceptAttributesBinder());
			byte[] descOutput = getComponentBytes(conceptData, primordial,
					OFFSETS.DESCRIPTIONS, conceptData.getDescriptionsIfChanged(),
					new DescriptionBinder());
			byte[] relOutput = getComponentBytes(conceptData, primordial,
					OFFSETS.SOURCE_RELS, conceptData.getSourceRelsIfChanged(),
					new RelationshipBinder());
			byte[] imageOutput = getComponentBytes(conceptData, primordial,
					OFFSETS.IMAGES, conceptData.getImagesIfChanged(), 
					new ImageBinder());
			byte[] refsetOutput = getRefsetBytes(conceptData, primordial,
					OFFSETS.REFSET_MEMBERS, conceptData.getRefsetMembersIfChanged(),
					new RefsetMemberBinder());
			IntListPairsBinder pairsBinder = new IntListPairsBinder();
			
			byte[] destRelOutput = pairsBinder.getBytes(
					conceptData.getDestRelNidTypeNidListReadOnly(),
					conceptData.getDestRelNidTypeNidList());
			
			byte[] refsetNidMemberNidForConceptOutput = pairsBinder.getBytes(
					conceptData.getRefsetNidMemberNidForConceptListReadOnly(),
					conceptData.getRefsetNidMemberNidForConceptList());

			byte[] refsetNidMemberNidForDescOutput = pairsBinder.getBytes(
					conceptData.getRefsetNidMemberNidForDescriptionsListReadOnly(),
					conceptData.getRefsetNidMemberNidForDescriptionsList());

			byte[] refsetNidMemberNidForRelsOutput = pairsBinder.getBytes(
					conceptData.getRefsetNidMemberNidForRelsListReadOnly(),
					conceptData.getRefsetNidMemberNidForRelsList());
			
			byte[] refsetNidMemberNidForImagesOutput = pairsBinder.getBytes(
					conceptData.getRefsetNidMemberNidForImagesListReadOnly(),
					conceptData.getRefsetNidMemberNidForImagesList());

			byte[] refsetNidMemberNidForRefsetMembersOutput = pairsBinder.getBytes(
					conceptData.getRefsetNidMemberNidForRefsetMembersListReadOnly(),
					conceptData.getRefsetNidMemberNidForRefsetMembersList());

			byte[] descNidOutput = getNidSetBytes(conceptData, primordial,
					conceptData.getDescNidsReadOnly(),
					conceptData.getDescNids());
			byte[] srcRelNidOutput = getNidSetBytes(conceptData, primordial,
					conceptData.getSrcRelNidsReadOnly(),
					conceptData.getSrcRelNids());
			byte[] imageNidOutput = getNidSetBytes(conceptData, primordial,
					conceptData.getImageNidsReadOnly(),
					conceptData.getImageNids());

			byte[] memberNidOutput = getNidSetBytes(conceptData, primordial,
					conceptData.getMemberNidsReadOnly(),
					conceptData.getMemberNids());
			
			finalOutput.writeInt(1); // FORMAT_VERSION
			finalOutput.writeLong(dataVersion); // DATA_VERSION
			int nextDataLocation = OFFSETS.getHeaderSize();
			finalOutput.writeInt(nextDataLocation); // ATTRIBUTES
			nextDataLocation = nextDataLocation + attrOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // DESCRIPTIONS
			nextDataLocation = nextDataLocation + descOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // SOURCE_RELS
			nextDataLocation = nextDataLocation + relOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // REFSET_MEMBERS
			nextDataLocation = nextDataLocation + refsetOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // DEST_REL_NID_TYPE_NIDS
			nextDataLocation = nextDataLocation + destRelOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // REFSETNID_MEMBERNID_FOR_CONCEPT
			nextDataLocation = nextDataLocation
					+ refsetNidMemberNidForConceptOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // REFSETNID_MEMBERNID_FOR_DESCRIPTIONS
			nextDataLocation = nextDataLocation
					+ refsetNidMemberNidForDescOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // REFSETNID_MEMBERNID_FOR_RELATIONSHIPS
			nextDataLocation = nextDataLocation
				+ refsetNidMemberNidForRelsOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // REFSETNID_MEMBERNID_FOR_IMAGES
			nextDataLocation = nextDataLocation
					+ refsetNidMemberNidForImagesOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // REFSETNID_MEMBERNID_FOR_REFSETMEMBERS
			nextDataLocation = nextDataLocation
					+ refsetNidMemberNidForRefsetMembersOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // DESC_NIDS
			nextDataLocation = nextDataLocation
					+ descNidOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // SRC_REL_NIDS
			nextDataLocation = nextDataLocation
					+ srcRelNidOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // IMAGE_NIDS
			nextDataLocation = nextDataLocation
					+ imageNidOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // MEMBER_NIDS
			nextDataLocation = nextDataLocation
					+ memberNidOutput.length;
			
			finalOutput.writeInt(nextDataLocation); // IMAGES
			nextDataLocation = nextDataLocation + imageOutput.length;

			finalOutput.writeInt(nextDataLocation); // DATA_SIZE

			finalOutput.makeSpace(nextDataLocation);
			finalOutput.writeFast(attrOutput);   // ATTRIBUTES
			finalOutput.writeFast(descOutput);   // DESCRIPTIONS
			finalOutput.writeFast(relOutput);    // SOURCE_RELS
			finalOutput.writeFast(refsetOutput); // REFSET_MEMBERS
			finalOutput.writeFast(destRelOutput);// DEST_REL_ORIGIN_NID_TYPE_NIDS
			finalOutput.writeFast(refsetNidMemberNidForConceptOutput); // REFSETNID_MEMBERNID_FOR_CONCEPT
			finalOutput.writeFast(refsetNidMemberNidForDescOutput);    // REFSETNID_MEMBERNID_FOR_DESCRIPTIONS
			finalOutput.writeFast(refsetNidMemberNidForRelsOutput);    // REFSETNID_MEMBERNID_FOR_RELATIONSHIPS
			finalOutput.writeFast(refsetNidMemberNidForImagesOutput);    // REFSETNID_MEMBERNID_FOR_IMAGES
			finalOutput.writeFast(refsetNidMemberNidForRefsetMembersOutput); // REFSETNID_MEMBERNID_FOR_REFSETMEMBERS
			finalOutput.writeFast(descNidOutput);  // DESC_NIDS
			finalOutput.writeFast(srcRelNidOutput);// SRC_REL_NIDS
			finalOutput.writeFast(imageNidOutput); // IMAGE_NIDS
			finalOutput.writeFast(memberNidOutput); // MEMBER_NIDS
			finalOutput.writeFast(imageOutput);    // IMAGES
		
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static IntSetBinder intSetBinder = new IntSetBinder();
	private byte[] getNidSetBytes(I_ManageConceptData conceptData,
			boolean primordial, Set<Integer> nidsReadOnly, Set<Integer> nids) {
		HashSet<Integer> nidsToWrite = new HashSet<Integer>(nids);
		nidsToWrite.removeAll(nidsReadOnly);
		TupleOutput output = new TupleOutput();
		intSetBinder.objectToEntry(nidsToWrite, output);
		return output.toByteArray();
	}

	private static byte[] getAttributeBytes(
			I_ManageConceptData conceptData,
			boolean primordial,
			OFFSETS offset,
			ConceptAttributes attributes,
			ConceptComponentBinder<ConceptAttributesRevision, 
			ConceptAttributes> conceptComponentBinder)
			throws InterruptedException, ExecutionException, IOException {
		assert offset != null && offset.prev != null: "offset is malformed: " + offset;
		byte[] componentBytes;
		if (!primordial && attributes == null) {
			componentBytes = getPreviousData(conceptData, offset, OFFSETS.values()[offset.ordinal() + 1]);
		} else {
			TupleOutput output = new TupleOutput();
			if (attributes != null && attributes.getTime() != Long.MIN_VALUE) {
				List<ConceptAttributes> attrList = new ArrayList<ConceptAttributes>();
				attrList.add(attributes);
				conceptComponentBinder.objectToEntry(attrList, output);
				componentBytes = output.toByteArray();
			} else {
				componentBytes = zeroOutputArray;
			}
		}
		return componentBytes;
	}

	private <C extends ConceptComponent<V, C>, V extends Revision<V, C>> byte[] getComponentBytes(
			I_ManageConceptData conceptData, boolean primordial, OFFSETS offset,
			List<C> componentList,
			ConceptComponentBinder<V, C> binder) throws InterruptedException,
			ExecutionException, IOException {
		byte[] componentBytes;
		if (!primordial && componentList == null) {
			componentBytes = getPreviousData(conceptData, offset, OFFSETS.values()[offset.ordinal() + 1]);
		} else {
			TupleOutput output = new TupleOutput();
			if (componentList != null) {
				binder.objectToEntry(componentList, output);
				componentBytes = output.toByteArray();
			} else {
				componentBytes = zeroOutputArray;
			}
		}
		if (componentBytes.length == 0) {
			componentBytes = zeroOutputArray;
		}
		return componentBytes;
	}

	private byte[] getRefsetBytes(I_ManageConceptData conceptData, boolean primordial,
			OFFSETS offset,
			List<RefsetMember<?, ?>> members,
			RefsetMemberBinder binder) throws InterruptedException,
			ExecutionException, IOException {
		byte[] componentBytes;
		if (!primordial && members == null) {
			componentBytes = getPreviousData(conceptData, offset, OFFSETS.values()[offset.ordinal() + 1]);
		} else {
			TupleOutput output = new TupleOutput();
			if (members != null) {
				binder.objectToEntry(members, output);
				componentBytes = output.toByteArray();
			} else {
				componentBytes = zeroOutputArray;
			}
		}
		if (componentBytes.length == 0) {
			componentBytes = zeroOutputArray;
		}
		return componentBytes;
	}

	private static byte[] getPreviousData(I_ManageConceptData conceptData, 
			OFFSETS start,
			OFFSETS end) throws InterruptedException, ExecutionException, IOException {
		assert start != null: "start is null. end: " + end;
		assert end != null: "end is null. start: " + start;
		byte[] output;
		TupleInput readWriteInput = conceptData.getReadWriteTupleInput();
		byte[] bufferBytes = readWriteInput.getBufferBytes();
		if (bufferBytes.length > OFFSETS.getHeaderSize()) {
			int offset = start.getOffset(bufferBytes);
			int endOffset = end.getOffset(bufferBytes);
			int byteCount = endOffset - offset;
			readWriteInput.skipFast(offset);
			assert byteCount >= 0: " neg byteCount: " + byteCount + 
				" start offset: " + offset + " end offset: " + endOffset +
				" start: " + start + " end: " + end;
			output = new byte[byteCount];
			System.arraycopy(readWriteInput.getBufferBytes(), offset, output, 0,
					byteCount);
		} else {
			output = zeroOutputArray;
		}
		return output;
	}
}
