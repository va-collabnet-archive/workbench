/**
 * 
 */
package org.ihtsdo.db.bdb.concept;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.I_GetNidData;
import org.ihtsdo.db.bdb.NidDataFromBdb;
import org.ihtsdo.db.bdb.NidDataInMemory;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.ConceptComponentBinder;
import org.ihtsdo.db.bdb.concept.component.DataVersionBinder;
import org.ihtsdo.db.bdb.concept.component.RelNidTypeNidBinder;
import org.ihtsdo.db.bdb.concept.component.Version;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.db.bdb.concept.component.description.Description;
import org.ihtsdo.db.bdb.concept.component.description.DescriptionBinder;
import org.ihtsdo.db.bdb.concept.component.image.Image;
import org.ihtsdo.db.bdb.concept.component.image.ImageBinder;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.db.bdb.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.concept.component.relationship.RelationshipBinder;

import cern.colt.map.OpenIntIntHashMap;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.je.DatabaseEntry;

/**
 * File format:<br>
 * 
 * @author kec
 * 
 */
public class ConceptData {

	private Concept enclosingConcept;
	protected I_GetNidData nidData;

	protected SoftReference<ConceptAttributes> attributesRef;
	protected SoftReference<ArrayList<Relationship>> srcRelsRef;
	protected SoftReference<ArrayList<Description>> descriptionsRef;
	protected SoftReference<ArrayList<Image>> imagesRef;
	protected SoftReference<ArrayList<RefsetMember<?, ?>>> refsetMembersRef;
	private ArrayList<Object> strongReferences;

	ConceptData(Concept enclosingConcept) throws IOException {
		assert enclosingConcept != null : "enclosing concept cannot be null.";
		this.enclosingConcept = enclosingConcept;
		if (enclosingConcept.isEditable()) {
			strongReferences = new ArrayList<Object>();
		}
		nidData = new NidDataFromBdb(enclosingConcept.getNid(), Bdb.getConceptDb().getReadOnly(), Bdb
				.getConceptDb().getReadWrite());
	}

	ConceptData(Concept enclosingConcept, DatabaseEntry data) throws IOException {
		assert enclosingConcept != null : "enclosing concept cannot be null.";
		this.enclosingConcept = enclosingConcept;
		if (enclosingConcept.isEditable()) {
			strongReferences = new ArrayList<Object>();
		}
		nidData = new NidDataInMemory(new byte[] {}, data.getData());
	}

	public int getNid() {
		return enclosingConcept.getNid();
	}

	public int getReadWriteDataVersion() throws InterruptedException,
			ExecutionException, IOException {
		DataVersionBinder binder = DataVersionBinder.getBinder();
		return binder.entryToObject(nidData.getReadWriteTupleInput());
	}

	public ArrayList<Relationship> getSourceRels() throws IOException {
		ArrayList<Relationship> rels;
		if (srcRelsRef != null) {
			rels = srcRelsRef.get();
			if (rels != null) {
				return rels;
			}
		}
		try {
			rels = getList(new RelationshipBinder(), 
					OFFSETS.SOURCE_RELS, 
					enclosingConcept);
			if (enclosingConcept.isEditable() && rels != null) {
				strongReferences.add(rels);
				srcRelsRef = new SoftReference<ArrayList<Relationship>>(rels);
			}
			return rels;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	public List<Description> getDescriptions() throws IOException {
		ArrayList<Description> descList = null;
		if (descriptionsRef != null) {
			descList = descriptionsRef.get();
			if (descList != null) {
				return descList;
			}
		}
		try {
			descList = getList(new DescriptionBinder(),
					OFFSETS.DESCRIPTIONS,
					enclosingConcept);
			if (enclosingConcept.isEditable()) {
				strongReferences.add(descList);
				descriptionsRef = new SoftReference<ArrayList<Description>>(
						descList);
			}
			return descList;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	private <C extends ConceptComponent<V, C>, 
	         V extends Version<V, C>> ArrayList<C> 
		getList(ConceptComponentBinder<V, C> binder, 
				OFFSETS offset, Concept enclosingConcept)
			throws InterruptedException, ExecutionException, IOException {
		binder.setupBinder(enclosingConcept);
		ArrayList<C> componentList;
		TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
		if (readOnlyInput.available() > 0) {
			checkFormatAndVersion(readOnlyInput);
			readOnlyInput.mark(128);
			readOnlyInput.skipFast(offset.offset);
			int listStart = readOnlyInput.readInt();
			readOnlyInput.reset();
			readOnlyInput.skipFast(listStart);			
			componentList = binder.entryToObject(readOnlyInput);
		} else {
			componentList = new ArrayList<C>();
		}
		assert componentList != null;
		binder.setTermComponentList(componentList);
		TupleInput readWriteInput = nidData.getReadWriteTupleInput();
		if (readWriteInput.available() > 0) {
			checkFormatAndVersion(readWriteInput);
			readWriteInput.mark(128);
			readWriteInput.skipFast(offset.offset);
			int listStart = readWriteInput.readInt();
			readWriteInput.reset();
			readWriteInput.skipFast(listStart);			
			componentList = binder.entryToObject(readWriteInput);
		}
		return componentList;
	}

	private void checkFormatAndVersion(TupleInput input)
			throws UnsupportedEncodingException {
		input.mark(128);
		int formatVersion = input.readInt();
		int dataVersion = input.readInt();
		if (formatVersion != OFFSETS.CURRENT_FORMAT_VERSION) {
			throw new UnsupportedEncodingException("No support for format version: " + formatVersion);
		}
		if (dataVersion != OFFSETS.CURRENT_DATA_VERSION) {
			throw new UnsupportedEncodingException("No support for data version: " + dataVersion);
		}
		input.reset();
	}

	private ArrayList<RefsetMember<?, ?>> getList(
			RefsetMemberBinder binder, OFFSETS offset, Concept enclosingConcept)
			throws InterruptedException, ExecutionException, IOException {
		binder.setupBinder(enclosingConcept);
		ArrayList<RefsetMember<?, ?>> componentList;
		TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
		if (readOnlyInput.available() > 0) {
			checkFormatAndVersion(readOnlyInput);
			readOnlyInput.mark(128);
			readOnlyInput.skipFast(offset.offset);
			int listStart = readOnlyInput.readInt();
			readOnlyInput.reset();
			readOnlyInput.skipFast(listStart);			
			componentList = binder.entryToObject(readOnlyInput);
		} else {
			componentList = new ArrayList<RefsetMember<?, ?>>();
		}
		assert componentList != null;
		binder.setTermComponentList(componentList);
		TupleInput readWriteInput = nidData.getReadWriteTupleInput();
		if (readWriteInput.available() > 0) {
			readWriteInput.mark(128);
			checkFormatAndVersion(readWriteInput);
			readWriteInput.reset();
			readWriteInput.skipFast(offset.offset);
			int listStart = readWriteInput.readInt();
			readWriteInput.reset();
			readWriteInput.skipFast(listStart);			
			componentList = binder.entryToObject(readWriteInput);
		}
		return componentList;
	}

	public ConceptAttributes getConceptAttributes() throws IOException {
		ConceptAttributes attr;
		if (attributesRef != null) {
			attr = attributesRef.get();
			if (attr != null) {
				return attr;
			}
		}
		try {
			ArrayList<ConceptAttributes> components = getList(
					new ConceptAttributesBinder(), 
					OFFSETS.ATTRIBUTES, 
					enclosingConcept);
			if (components != null && components.size() == 1) {
				if (enclosingConcept.isEditable()) {
					strongReferences.add(components.get(0));
				}
				return components.get(0);
			}
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
		return null;
	}

	/**
	 * Destination rels are stored as a relid and a type id in an array.
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<Relationship> getDestRels() throws IOException {
		try {
			TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();
			readOnlyInput
					.skipFast(OFFSETS.DEST_REL_ORIGIN_NID_TYPE_NIDS.offset);
			OpenIntIntHashMap roRelNidToTypeNidMap = RelNidTypeNidBinder
					.getBinder().entryToObject(readOnlyInput);

			TupleInput readWriteInput = nidData.getReadWriteTupleInput();
			readWriteInput
					.skipFast(OFFSETS.DEST_REL_ORIGIN_NID_TYPE_NIDS.offset);
			OpenIntIntHashMap rwRelNidToTypeNidMap = RelNidTypeNidBinder
					.getBinder().entryToObject(readWriteInput);
			if (rwRelNidToTypeNidMap.size() > roRelNidToTypeNidMap.size()) {
				for (int relNid : roRelNidToTypeNidMap.keys().elements()) {
					rwRelNidToTypeNidMap.put(relNid, roRelNidToTypeNidMap
							.get(relNid));
				}
			} else {
				for (int relNid : rwRelNidToTypeNidMap.keys().elements()) {
					roRelNidToTypeNidMap.put(relNid, rwRelNidToTypeNidMap
							.get(relNid));
				}
				rwRelNidToTypeNidMap = roRelNidToTypeNidMap;
			}
			List<Relationship> destRels = new ArrayList<Relationship>();
			for (int relNid : rwRelNidToTypeNidMap.keys().elements()) {
				Concept c = Bdb.getConceptForComponent(relNid);
				destRels.add(c.getRelationship(relNid));
			}
			return destRels;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	public List<RefsetMember<?, ?>> getRefsetMembers() throws IOException {
		ArrayList<RefsetMember<?, ?>> refsetMemberList;
		if (refsetMembersRef != null) {
			refsetMemberList = refsetMembersRef.get();
			if (refsetMemberList != null) {
				return refsetMemberList;
			}
		}
		try {
			refsetMemberList = getList(new RefsetMemberBinder(),
					OFFSETS.REFSET_MEMBERS, 
					enclosingConcept);
			if (enclosingConcept.isEditable() && refsetMemberList != null) {
				strongReferences.add(refsetMemberList);
				refsetMembersRef = new SoftReference<ArrayList<RefsetMember<?, ?>>>(
						refsetMemberList);
			}
			return refsetMemberList;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	public List<Image> getImages() throws IOException {
		ArrayList<Image> imgList;
		if (imagesRef != null) {
			imgList = imagesRef.get();
			if (imgList != null) {
				return imgList;
			}
		}
		try {
			imgList = getList(new ImageBinder(), OFFSETS.IMAGES, 
					enclosingConcept);
			if (enclosingConcept.isEditable() && imgList != null) {
				strongReferences.add(imgList);
				imagesRef = new SoftReference<ArrayList<Image>>(imgList);
			}
			return imgList;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	protected SoftReference<ConceptAttributes> getAttributesRef() {
		return attributesRef;
	}

	protected SoftReference<ArrayList<Relationship>> getSrcRelsRef() {
		return srcRelsRef;
	}

	protected SoftReference<ArrayList<Description>> getDescriptionsRef() {
		return descriptionsRef;
	}

	protected SoftReference<ArrayList<Image>> getImagesRef() {
		return imagesRef;
	}

	protected SoftReference<ArrayList<RefsetMember<?, ?>>> getRefsetMembersRef() {
		return refsetMembersRef;
	}

	protected I_GetNidData getNidData() {
		return nidData;
	}

	public void set(ConceptAttributes attr) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		if (attributesRef != null) {
			throw new IOException(
					"Attributes is already set. Please modify the exisiting attributes object.");
		}
		attributesRef = new SoftReference<ConceptAttributes>(attr);
		strongReferences.add(attr);
	}

	public void add(Description desc) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		getDescriptions().add(desc);
	}

	public void add(Relationship rel) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		getSourceRels().add(rel);
	}

	public void add(Image img) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		getImages().add(img);
	}

	public void add(RefsetMember<?, ?> refsetMember) throws IOException {
		if (enclosingConcept.isEditable() == false) {
			throw new IOException("Attempting to add to an uneditable concept");
		}
		getRefsetMembers().add(refsetMember);
	}

	public int[] getAllNids() throws IOException {
		ArrayIntList allContainedNids = new ArrayIntList();
		allContainedNids.add(enclosingConcept.getNid());
		for (Description d: getDescriptions()) {
			allContainedNids.add(d.nid);
		}
		for (Relationship r: getSourceRels()) {
			allContainedNids.add(r.nid);
		}
		for (Image i: getImages()) {
			allContainedNids.add(i.nid);
		}
		for (RefsetMember<?, ?> r: getRefsetMembers()) {
			allContainedNids.add(r.nid);
		}
		return allContainedNids.toArray();
	}
}