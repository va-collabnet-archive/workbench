package org.ihtsdo.db.bdb.concept;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;
import org.ihtsdo.db.bdb.concept.component.Version;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesVersion;
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

			ConceptData conceptData = concept.getData();
			boolean primordial = conceptData.nidData.getReadOnlyBytes().length == 0
					&& conceptData.nidData.getReadWriteBytes().length == 0;

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
			// TODO DEST_REL_ORIGIN_NID_TYPE_NIDS
			byte[] destRelOriginOutput = zeroOutputArray;
			// TODO REFSETNID_MEMBERNID_FOR_CONCEPT
			byte[] refsetNidMemberNidOutput = zeroOutputArray;
			// TODO REFSETNID_MEMBERNID_COMPONENTNID_FOR_COMPONENTS
			byte[] refsetNidMemberNidComponentNid = zeroOutputArray;

			finalOutput.writeInt(1); // Format version
			finalOutput.writeInt(1); // Data version
			int nextDataLocation = OFFSETS.getHeaderSize();
			finalOutput.writeInt(nextDataLocation); // ATTRIBUTES
			nextDataLocation = nextDataLocation + attrOutput.length;
			finalOutput.writeInt(nextDataLocation); // DESCRIPTIONS
			nextDataLocation = nextDataLocation + descOutput.length;
			finalOutput.writeInt(nextDataLocation); // SOURCE_RELS
			nextDataLocation = nextDataLocation + relOutput.length;
			finalOutput.writeInt(nextDataLocation); // IMAGES
			nextDataLocation = nextDataLocation + imageOutput.length;
			finalOutput.writeInt(nextDataLocation); // REFSET_MEMBERS
			nextDataLocation = nextDataLocation + refsetOutput.length;
			finalOutput.writeInt(nextDataLocation); // DEST_REL_ORIGIN_NID_TYPE_NIDS
			nextDataLocation = nextDataLocation + destRelOriginOutput.length;
			finalOutput.writeInt(nextDataLocation); // REFSETNID_MEMBERNID_FOR_CONCEPT
			nextDataLocation = nextDataLocation
					+ refsetNidMemberNidOutput.length;
			finalOutput.writeInt(nextDataLocation); // REFSETNID_MEMBERNID_COMPONENTNID_FOR_COMPONENTS
			nextDataLocation = nextDataLocation
					+ refsetNidMemberNidComponentNid.length;
			finalOutput.makeSpace(nextDataLocation);
			finalOutput.writeFast(attrOutput); // ATTRIBUTES
			finalOutput.writeFast(descOutput); // DESCRIPTIONS
			finalOutput.writeFast(relOutput); // SOURCE_RELS
			finalOutput.writeFast(imageOutput); // IMAGES
			finalOutput.writeFast(refsetOutput); // REFSET_MEMBERS
			finalOutput.writeFast(destRelOriginOutput); // DEST_REL_ORIGIN_NID_TYPE_NIDS
			finalOutput.writeFast(refsetNidMemberNidOutput); // REFSETNID_MEMBERNID_FOR_CONCEPT
			finalOutput.writeFast(refsetNidMemberNidComponentNid); // REFSETNID_MEMBERNID_COMPONENTNID_FOR_COMPONENTS
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private byte[] getAttributeBytes(
			ConceptData conceptData,
			boolean primordial,
			OFFSETS offset,
			SoftReference<ConceptAttributes> reference,
			ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes> conceptComponentBinder)
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

	private <C extends ConceptComponent<V, C>, V extends Version<V, C>> byte[] getComponentBytes(
			ConceptData conceptData, boolean primordial, OFFSETS offset,
			SoftReference<ArrayList<C>> reference,
			ConceptComponentBinder<V, C> binder) throws InterruptedException,
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

	private byte[] getRefsetBytes(ConceptData conceptData, boolean primordial,
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

	private byte[] getPreviousData(ConceptData conceptData, OFFSETS start,
			OFFSETS end) throws InterruptedException, ExecutionException, IOException {
		byte[] output;
		TupleInput readWriteInput = conceptData.nidData
				.getReadWriteTupleInput();
		byte[] bufferBytes = readWriteInput.getBufferBytes();
		int offset = start.getOffset(bufferBytes);
		int byteCount = end.getOffset(bufferBytes) - offset;
		readWriteInput.skipFast(offset);
		output = new byte[byteCount];
		System.arraycopy(readWriteInput.getBufferBytes(), offset, output, 0,
				byteCount);
		return output;
	}
}
