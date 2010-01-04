package org.ihtsdo.db.bdb.concept;

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

	private static final ThreadLocal<ConceptBinder> binders = new ThreadLocal<ConceptBinder>() {

		@Override
		protected ConceptBinder initialValue() {
			return new ConceptBinder();
		}
	};

	public static ConceptBinder getBinder() {
		return binders.get();
	}

	@Override
	public Concept entryToObject(TupleInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void objectToEntry(Concept concept, TupleOutput finalOutput) {

		try {

			ConceptData conceptData = concept.getData();
			boolean primordial = conceptData.nidData.getReadOnlyBytes().length == 0 &&
			conceptData.nidData.getReadWriteBytes().length == 0;

			byte[] attrOutput = getAttributeBytes(conceptData, primordial,
						OFFSETS.ATTRIBUTES, conceptData.getAttributesRef(),
						ConceptAttributesBinder.getBinder());
			byte[] descOutput = getComponentBytes(conceptData, primordial,
						OFFSETS.DESCRIPTIONS, conceptData.getDescriptionsRef(),
						DescriptionBinder.getBinder());
			byte[] relOutput = getComponentBytes(conceptData, primordial,
						OFFSETS.SOURCE_RELS, conceptData.getSrcRelsRef(),
						RelationshipBinder.getBinder());
			byte[] imageOutput = getComponentBytes(conceptData, primordial,
						OFFSETS.IMAGES, conceptData.getImagesRef(),
						ImageBinder.getBinder());
			byte[] refsetOutput = getRefsetBytes(conceptData, primordial,
						OFFSETS.REFSET_MEMBERS, conceptData.getRefsetMembersRef(),
						RefsetMemberBinder.getBinder());

			finalOutput.writeInt(1); // Format version
			finalOutput.writeInt(1); // Data version
			int nextDataLocation = OFFSETS.getHeaderSize();
			finalOutput.writeFast(nextDataLocation); // ATTRIBUTES
			nextDataLocation = nextDataLocation + attrOutput.length;
			finalOutput.writeFast(nextDataLocation); // DESCRIPTIONS
			nextDataLocation = nextDataLocation + descOutput.length;
			finalOutput.writeFast(nextDataLocation); // SOURCE_RELS
			nextDataLocation = nextDataLocation + relOutput.length;
			finalOutput.writeFast(nextDataLocation); // IMAGES
			nextDataLocation = nextDataLocation + imageOutput.length;
			finalOutput.writeFast(nextDataLocation); // REFSET_MEMBERS
			nextDataLocation = nextDataLocation + refsetOutput.length;
			finalOutput.makeSpace(nextDataLocation);
			finalOutput.writeFast(attrOutput); // ATTRIBUTES
			finalOutput.writeFast(descOutput); // DESCRIPTIONS
			finalOutput.writeFast(relOutput); // SOURCE_RELS
			finalOutput.writeFast(imageOutput); // IMAGES
			finalOutput.writeFast(refsetOutput); // REFSET_MEMBERS
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private byte[] getAttributeBytes(ConceptData conceptData, 
			boolean primordial, OFFSETS offset, 
			SoftReference<ConceptAttributes> reference, 
			ConceptComponentBinder<ConceptAttributesVersion, ConceptAttributes> conceptComponentBinder)
			throws InterruptedException, ExecutionException {
		byte[] componentBytes;
		if (!primordial && reference == null) {
			componentBytes = getPreviousData(conceptData, offset,
					offset.prev);
		} else {
			TupleOutput output = new TupleOutput();
			if (reference != null) {
				ArrayList<ConceptAttributes> attrList = new ArrayList<ConceptAttributes>();
				attrList.add(reference.get());
				conceptComponentBinder.objectToEntry(attrList, output);
				componentBytes = output.toByteArray();
			} else {
				componentBytes = new byte[0];
			}
		}
		return componentBytes;
	}

	private <C extends ConceptComponent<V, C>, V extends Version<V, C>>
	byte[] getComponentBytes(ConceptData conceptData, 
		boolean primordial, OFFSETS offset, 
		SoftReference<ArrayList<C>> reference, 
		ConceptComponentBinder<V, C> binder)
		throws InterruptedException, ExecutionException {
	byte[] componentBytes;
	if (!primordial && reference == null) {
		componentBytes = getPreviousData(conceptData, offset,
				offset.prev);
	} else {
		TupleOutput output = new TupleOutput();
		if (reference != null) {
			binder.objectToEntry(reference.get(), output);
			componentBytes = output.toByteArray();
		} else {
			componentBytes = new byte[0];
		}
	}
	return componentBytes;
}
	private byte[] getRefsetBytes(ConceptData conceptData, 
			boolean primordial, OFFSETS offset, 
			SoftReference<ArrayList<RefsetMember<?, ?>>> reference, 
			RefsetMemberBinder binder)
			throws InterruptedException, ExecutionException {
		byte[] componentBytes;
		if (!primordial && reference == null) {
			componentBytes = getPreviousData(conceptData, offset,
					offset.prev);
		} else {
			TupleOutput output = new TupleOutput();
			if (reference != null) {
				binder.objectToEntry(reference.get(), output);
				componentBytes = output.toByteArray();
			} else {
				componentBytes = new byte[0];
			}
		}
		return componentBytes;
	}

	private byte[] getPreviousData(ConceptData conceptData, OFFSETS start,
			OFFSETS end) throws InterruptedException, ExecutionException {
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
