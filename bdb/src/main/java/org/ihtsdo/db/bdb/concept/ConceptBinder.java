package org.ihtsdo.db.bdb.concept;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;
import org.ihtsdo.db.bdb.concept.component.Revision;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesRevision;
import org.ihtsdo.db.bdb.concept.component.description.DescriptionBinder;
import org.ihtsdo.db.bdb.concept.component.image.ImageBinder;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.db.bdb.concept.component.relationship.RelationshipBinder;

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

			I_ManageConceptData conceptData = concept.getData();
			boolean primordial = conceptData.getReadOnlyBytes().length == 0
					&& conceptData.getReadWriteBytes().length == 0;

			byte[] attrOutput = getAttributeBytes(conceptData, primordial,
					OFFSETS.ATTRIBUTES, conceptData.getAttributesRef(),
					new ConceptAttributesBinder());
			byte[] descOutput = getComponentBytes(conceptData, primordial,
					OFFSETS.DESCRIPTIONS, conceptData.getDescriptionsRef(),
					new DescriptionBinder());
			byte[] relOutput = getComponentBytes(conceptData, primordial,
					OFFSETS.SOURCE_RELS, conceptData.getSrcRelsRef(),
					new RelationshipBinder());
			byte[] imageOutput = getComponentBytes(conceptData, primordial,
					OFFSETS.IMAGES, conceptData.getImagesRef(), new ImageBinder());
			byte[] refsetOutput = getRefsetBytes(conceptData, primordial,
					OFFSETS.REFSET_MEMBERS, conceptData.getRefsetMembersRef(),
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

			finalOutput.writeInt(1); // FORMAT_VERSION
			finalOutput.writeInt(1); // DATA_VERSION
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
			
			finalOutput.writeInt(nextDataLocation); // IMAGES
			nextDataLocation = nextDataLocation + imageOutput.length;
			
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
			finalOutput.writeFast(imageOutput);    // IMAGES
		
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static IntSetBinder intSetBinder = new IntSetBinder();
	private byte[] getNidSetBytes(I_ManageConceptData conceptData,
			boolean primordial, IntSet nidsReadOnly, IntSet nids) {
		IntSet nidsToWrite = new IntSet();
		nidsToWrite.addAll(nids.getSetValues());
		nidsToWrite.removeAll(nidsReadOnly.getSetValues());
		TupleOutput output = new TupleOutput();
		intSetBinder.objectToEntry(nidsToWrite, output);
		return output.toByteArray();
	}

	private byte[] getAttributeBytes(
			I_ManageConceptData conceptData,
			boolean primordial,
			OFFSETS offset,
			SoftReference<ConceptAttributes> reference,
			ConceptComponentBinder<ConceptAttributesRevision, ConceptAttributes> conceptComponentBinder)
			throws InterruptedException, ExecutionException, IOException {
		byte[] componentBytes;
		if (!primordial && reference == null) {
			componentBytes = getPreviousData(conceptData, offset, offset.prev);
		} else {
			TupleOutput output = new TupleOutput();
			if (reference != null) {
				ArrayList<ConceptAttributes> attrList = new ArrayList<ConceptAttributes>();
				attrList.add(reference.get());
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
			SoftReference<ArrayList<C>> softReference,
			ConceptComponentBinder<V, C> binder) throws InterruptedException,
			ExecutionException, IOException {
		byte[] componentBytes;
		if (!primordial && softReference == null) {
			componentBytes = getPreviousData(conceptData, offset, offset.prev);
		} else {
			TupleOutput output = new TupleOutput();
			if (softReference != null) {
				binder.objectToEntry(softReference.get(), output);
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
			SoftReference<ArrayList<RefsetMember<?, ?>>> reference,
			RefsetMemberBinder binder) throws InterruptedException,
			ExecutionException, IOException {
		byte[] componentBytes;
		if (!primordial && reference == null) {
			componentBytes = getPreviousData(conceptData, offset, offset.prev);
		} else {
			TupleOutput output = new TupleOutput();
			if (reference != null) {
				binder.objectToEntry(reference.get(), output);
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

	private byte[] getPreviousData(I_ManageConceptData conceptData, OFFSETS start,
			OFFSETS end) throws InterruptedException, ExecutionException, IOException {
		byte[] output;
		TupleInput readWriteInput = conceptData.getReadWriteTupleInput();
		byte[] bufferBytes = readWriteInput.getBufferBytes();
		if (bufferBytes.length > OFFSETS.getHeaderSize()) {
			int offset = start.getOffset(bufferBytes);
			int byteCount = end.getOffset(bufferBytes) - offset;
			readWriteInput.skipFast(offset);
			output = new byte[byteCount];
			System.arraycopy(readWriteInput.getBufferBytes(), offset, output, 0,
					byteCount);
		} else {
			output = zeroOutputArray;
		}
		return output;
	}
}
