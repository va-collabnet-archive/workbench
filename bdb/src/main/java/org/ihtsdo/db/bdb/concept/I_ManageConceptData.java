package org.ihtsdo.db.bdb.concept;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.concept.component.description.Description;
import org.ihtsdo.db.bdb.concept.component.image.Image;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.concept.component.relationship.Relationship;

import com.sleepycat.bind.tuple.TupleInput;

public interface I_ManageConceptData {

	public int getNid();

	public int getReadWriteDataVersion() throws InterruptedException,
			ExecutionException, IOException;

	public ArrayList<Relationship> getSourceRels() throws IOException;

	public List<Description> getDescriptions() throws IOException;

	public ConceptAttributes getConceptAttributes() throws IOException;

	/**
	 * Destination rels are stored as a relid and a type id in an array.
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<Relationship> getDestRels() throws IOException;

	public List<RefsetMember<?, ?>> getRefsetMembers() throws IOException;

	public List<Image> getImages() throws IOException;

	public void set(ConceptAttributes attr) throws IOException;

	public void add(Description desc) throws IOException;

	public void add(Relationship rel) throws IOException;

	public void add(Image img) throws IOException;

	public void add(RefsetMember<?, ?> refsetMember) throws IOException;

	public int[] getAllNids() throws IOException;

	public byte[] getReadOnlyBytes() throws InterruptedException, ExecutionException, IOException;

	public byte[] getReadWriteBytes() throws InterruptedException, ExecutionException;
	
	public SoftReference<ConceptAttributes> getAttributesRef();

	public SoftReference<ArrayList<Relationship>> getSrcRelsRef();

	public SoftReference<ArrayList<Description>> getDescriptionsRef();

	public SoftReference<ArrayList<Image>> getImagesRef();

	public SoftReference<ArrayList<RefsetMember<?, ?>>> getRefsetMembersRef();

	public TupleInput getReadWriteTupleInput() throws InterruptedException, ExecutionException;

	public void setDestRelNidTypeNidList(
			ArrayIntList destRelNidTypeNidList) throws IOException;

	public void setRefsetNidMemberNidForConceptList(
			ArrayIntList refsetNidMemberNidForConceptList) throws IOException;

	public void setRefsetNidMemberNidForDescriptionsList(
			ArrayIntList refsetNidMemberNidForDescriptionsList) throws IOException;

	public void setRefsetNidMemberNidForRelsList(
			ArrayIntList refsetNidMemberNidForRelsList) throws IOException;

	public ArrayIntList getDestRelNidTypeNidList() throws IOException;
	public ArrayIntList getDestRelNidTypeNidListReadOnly() throws IOException;

	public ArrayIntList getRefsetNidMemberNidForConceptList() throws IOException;
	public ArrayIntList getRefsetNidMemberNidForConceptListReadOnly() throws IOException;

	public ArrayIntList getRefsetNidMemberNidForDescriptionsList() throws IOException;
	public ArrayIntList getRefsetNidMemberNidForDescriptionsListReadOnly() throws IOException;

	public ArrayIntList getRefsetNidMemberNidForRelsList() throws IOException;
	public ArrayIntList getRefsetNidMemberNidForRelsListReadOnly() throws IOException;


}