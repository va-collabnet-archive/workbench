package org.ihtsdo.concept;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.image.Image;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.tk.api.ComponentChroncileBI;

import com.sleepycat.bind.tuple.TupleInput;
import org.ihtsdo.concept.ConceptDataManager.AddDescriptionSet;
import org.ihtsdo.concept.ConceptDataManager.AddImageSet;
import org.ihtsdo.concept.ConceptDataManager.AddMemberSet;
import org.ihtsdo.concept.ConceptDataManager.AddSrcRelSet;

public interface I_ManageConceptData {

	public int getNid();
	
	public void resetNidData();

	public int getReadWriteDataVersion() throws InterruptedException,
			ExecutionException, IOException;

	public AddSrcRelSet getSourceRels() throws IOException;
	public Collection<Relationship> getSourceRelsIfChanged() throws IOException;

	public AddDescriptionSet getDescriptions() throws IOException;
	public Collection<Description> getDescriptionsIfChanged() throws IOException;

	public ConceptAttributes getConceptAttributes() throws IOException;
	public ConceptAttributes getConceptAttributesIfChanged() throws IOException;

	public RefsetMember<?, ?> getRefsetMemberForComponent(int componentNid) throws IOException;
	public AddMemberSet getRefsetMembers() throws IOException;
	public Collection<RefsetMember<?, ?>> getRefsetMembersIfChanged() throws IOException;

	public AddImageSet getImages() throws IOException;
	public Collection<Image> getImagesIfChanged() throws IOException;

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

	public ComponentChroncileBI<?> getComponent(int nid) throws IOException;

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

	public void modified();
	
	public long getLastChange();
	
	public long getLastWrite();
	
	public void setLastWrite(long version);

	public boolean isUnwritten();

	public boolean isPrimordial() throws IOException;

    public boolean isLeafByDestRels(I_ConfigAceFrame aceConfig) throws IOException;

}