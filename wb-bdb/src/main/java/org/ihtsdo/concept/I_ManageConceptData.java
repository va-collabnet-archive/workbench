package org.ihtsdo.concept;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.ihtsdo.concept.ConceptDataManager.SetModifiedWhenChangedList;
import org.ihtsdo.concept.component.ComponentList;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.image.Image;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.db.util.NidPair;

import com.sleepycat.bind.tuple.TupleInput;

public interface I_ManageConceptData {

	public int getNid();
	
	public void resetNidData();

	public int getReadWriteDataVersion() throws InterruptedException,
			ExecutionException, IOException;

	public ComponentList<Relationship> getSourceRels() throws IOException;
	public ComponentList<Relationship> getSourceRelsIfChanged() throws IOException;

	public ComponentList<Description> getDescriptions() throws IOException;
	public ComponentList<Description> getDescriptionsIfChanged() throws IOException;

	public ConceptAttributes getConceptAttributes() throws IOException;
	public ConceptAttributes getConceptAttributesIfChanged() throws IOException;

	public ComponentList<RefsetMember<?, ?>> getRefsetMembers() throws IOException;
	public ComponentList<RefsetMember<?, ?>> getRefsetMembersIfChanged() throws IOException;

	public ComponentList<Image> getImages() throws IOException;
	public ComponentList<Image> getImagesIfChanged() throws IOException;

	/**
	 * Destination rels are stored as a relid and a type id in 
	 * an array.
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<Relationship> getDestRels() throws IOException;

	public void set(ConceptAttributes attr) throws IOException;

	public void add(Description desc) throws IOException;

	public void add(Relationship rel) throws IOException;

	public void add(Image img) throws IOException;

	public void add(RefsetMember<?, ?> refsetMember) throws IOException;

	public Collection<Integer> getAllNids() throws IOException;

	public byte[] getReadOnlyBytes() throws IOException;

	public byte[] getReadWriteBytes() throws IOException;
	
	public TupleInput getReadWriteTupleInput() throws IOException;

	public void setDestRelNidTypeNidList(
			List<NidPair> destRelOriginNidTypeNidList) throws IOException;

	public void setRefsetNidMemberNidForConceptList(
			List<NidPair> refsetNidMemberNidForConceptList) throws IOException;

	public void setRefsetNidMemberNidForDescriptionsList(
			List<NidPair> refsetNidMemberNidForDescriptionsList) throws IOException;

	public void setRefsetNidMemberNidForRelsList(
			List<NidPair> refsetNidMemberNidForRelsList) throws IOException;


	public void setRefsetNidMemberNidForImagesList(
			List<NidPair> refsetNidMemberNidForImagesList) throws IOException;

	public void setRefsetNidMemberNidForRefsetMembersList(
			List<NidPair> refsetNidMemberNidForRefsetMembersList) throws IOException;

	public SetModifiedWhenChangedList getDestRelNidTypeNidList() throws IOException;
	public List<? extends NidPair> getDestRelNidTypeNidListReadOnly() throws IOException;

	public SetModifiedWhenChangedList getRefsetNidMemberNidForConceptList() throws IOException;
	public List<? extends NidPair> getRefsetNidMemberNidForConceptListReadOnly() throws IOException;

	public SetModifiedWhenChangedList getRefsetNidMemberNidForDescriptionsList() throws IOException;
	public List<? extends NidPair> getRefsetNidMemberNidForDescriptionsListReadOnly() throws IOException;

	public SetModifiedWhenChangedList getRefsetNidMemberNidForRelsList() throws IOException;
	public List<? extends NidPair> getRefsetNidMemberNidForRelsListReadOnly() throws IOException;

	public SetModifiedWhenChangedList getRefsetNidMemberNidForImagesList() throws IOException;
	public List<? extends NidPair> getRefsetNidMemberNidForImagesListReadOnly() throws IOException;

	public SetModifiedWhenChangedList getRefsetNidMemberNidForRefsetMembersList() throws IOException;
	public List<? extends NidPair> getRefsetNidMemberNidForRefsetMembersListReadOnly() throws IOException;

	public ConceptComponent<?, ?> getComponent(int nid) throws IOException;

	public List<RefsetMember<?, ?>> getExtensionsForComponent(int nid) throws IOException;

	public Set<Integer> getDescNidsReadOnly() throws IOException;

	public Set<Integer> getDescNids()  throws IOException;

	public Set<Integer> getSrcRelNidsReadOnly()  throws IOException;

	public Set<Integer> getSrcRelNids()  throws IOException;

	public Set<Integer> getImageNidsReadOnly()  throws IOException;

	public Set<Integer> getImageNids()  throws IOException;

	public Set<Integer> getMemberNidsReadOnly() throws IOException;

	public Set<Integer> getMemberNids() throws IOException;

	public RefsetMember<?, ?> getRefsetMember(int memberNid) throws IOException;

	public boolean isUncommitted();

	public void addRefsetNidMemberNidForComponent(int refsetNid, int memberNid,
			int componentNid) throws IOException;

	public void modified();
	
	public long getLastChange();
	
	public long getLastWrite();
	
	public void setLastWrite(long version);

	public boolean isUnwritten();

	public boolean isPrimordial() throws IOException;

    public boolean isLeafByDestRels(I_ConfigAceFrame aceConfig) throws IOException;

}