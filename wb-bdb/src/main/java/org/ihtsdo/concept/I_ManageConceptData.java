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
import org.ihtsdo.tk.api.NidSetBI;

public interface I_ManageConceptData {

    int getNid();

    void resetNidData();

    int getReadWriteDataVersion() throws InterruptedException,
            ExecutionException, IOException;

    AddSrcRelSet getSourceRels() throws IOException;

    Collection<Relationship> getSourceRelsIfChanged() throws IOException;

    AddDescriptionSet getDescriptions() throws IOException;

    Collection<Description> getDescriptionsIfChanged() throws IOException;

    ConceptAttributes getConceptAttributes() throws IOException;

    ConceptAttributes getConceptAttributesIfChanged() throws IOException;

    RefsetMember<?, ?> getRefsetMemberForComponent(int componentNid) throws IOException;

    AddMemberSet getRefsetMembers() throws IOException;

    Collection<RefsetMember<?, ?>> getRefsetMembersIfChanged() throws IOException;

    AddImageSet getImages() throws IOException;

    Collection<Image> getImagesIfChanged() throws IOException;

    /**
     * Destination rels are stored as a relid and a type id in
     * an array.
     *
     * @return
     * @throws IOException
     */
    List<Relationship> getDestRels() throws IOException;

    List<Relationship> getDestRels(NidSetBI allowedTypes) throws IOException;

    void set(ConceptAttributes attr) throws IOException;

    void add(Description desc) throws IOException;

    void add(Relationship rel) throws IOException;

    void add(Image img) throws IOException;

    void add(RefsetMember<?, ?> refsetMember) throws IOException;

    Collection<Integer> getAllNids() throws IOException;

    byte[] getReadOnlyBytes() throws IOException;

    byte[] getReadWriteBytes() throws IOException;

    TupleInput getReadWriteTupleInput() throws IOException;

    ComponentChroncileBI<?> getComponent(int nid) throws IOException;

    Set<Integer> getDescNidsReadOnly() throws IOException;

    Set<Integer> getDescNids() throws IOException;

    Set<Integer> getSrcRelNidsReadOnly() throws IOException;

    Set<Integer> getSrcRelNids() throws IOException;

    Set<Integer> getImageNidsReadOnly() throws IOException;

    Set<Integer> getImageNids() throws IOException;

    Set<Integer> getMemberNidsReadOnly() throws IOException;

    Set<Integer> getMemberNids() throws IOException;

    RefsetMember<?, ?> getRefsetMember(int memberNid) throws IOException;

    boolean isUncommitted();

    void modified();

    long getLastChange();

    long getLastWrite();

    void setLastWrite(long version);

    boolean isUnwritten();

    boolean isPrimordial() throws IOException;

    boolean isLeafByDestRels(I_ConfigAceFrame aceConfig) throws IOException;

    boolean isAnnotationStyleRefset() throws IOException;

    void setAnnotationStyleRefset(boolean annotationStyleRefset);

    /**
     * For single-concept commit.
     * @param time
     */
    NidSetBI setCommitTime(long time);

    /**
     * For single-concept cancel.
     */
    void cancel() throws IOException;

    void diet();
}