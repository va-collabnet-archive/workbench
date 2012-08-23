package org.ihtsdo.concept;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleInput;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.ConceptComponentBinder;
import org.ihtsdo.concept.component.Revision;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.description.DescriptionBinder;
import org.ihtsdo.concept.component.identifier.IdentifierVersion;
import org.ihtsdo.concept.component.image.Image;
import org.ihtsdo.concept.component.image.ImageBinder;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.RelationshipBinder;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.I_GetNidData;
import org.ihtsdo.db.bdb.NidDataFromBdb;
import org.ihtsdo.db.bdb.NidDataInMemory;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.NidList;
import org.ihtsdo.tk.api.NidListBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupChronicleBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.ihtsdo.db.change.ChangeNotifier;

public class ConceptDataSimpleReference extends ConceptDataManager {
   private AtomicReference<ConceptAttributes>              attributes =
      new AtomicReference<ConceptAttributes>();
   private AtomicReference<AddSrcRelSet>                   srcRels    = new AtomicReference<AddSrcRelSet>();
   private AtomicReference<ConcurrentSkipListSet<Integer>> srcRelNids =
      new AtomicReference<ConcurrentSkipListSet<Integer>>();
   ReentrantLock                                                               sourceRelLock     =
      new ReentrantLock();
   ReentrantLock                                                               refsetMembersLock =
      new ReentrantLock();
   private AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>> refsetMembersMap       =
      new AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>>();
   private AtomicReference<AddMemberSet>                                   refsetMembers      =
      new AtomicReference<AddMemberSet>();
   private AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>> refsetComponentMap =
      new AtomicReference<ConcurrentHashMap<Integer, RefsetMember<?, ?>>>();
   private AtomicReference<ConcurrentSkipListSet<Integer>> memberNids =
      new AtomicReference<ConcurrentSkipListSet<Integer>>();
   private ReentrantLock                                   memberMapLock  = new ReentrantLock();
   private AtomicReference<AddImageSet>                    images         =
      new AtomicReference<AddImageSet>();
   ReentrantLock                                               imageLock = new ReentrantLock();
   private AtomicReference<ConcurrentSkipListSet<Integer>> imageNids      =
      new AtomicReference<ConcurrentSkipListSet<Integer>>();
   private AtomicReference<AddDescriptionSet>              descriptions  =
      new AtomicReference<AddDescriptionSet>();
   ReentrantLock                                               descLock = new ReentrantLock();
   private AtomicReference<ConcurrentSkipListSet<Integer>> descNids      =
      new AtomicReference<ConcurrentSkipListSet<Integer>>();
   ReentrantLock       attrLock = new ReentrantLock();
   private Boolean annotationIndex;
   private Boolean annotationStyleRefset;

   //~--- constructors --------------------------------------------------------

   public ConceptDataSimpleReference(Concept enclosingConcept) throws IOException {
      super(new NidDataFromBdb(enclosingConcept.getNid()));
      assert enclosingConcept != null : "enclosing concept cannot be null.";
      this.enclosingConcept = enclosingConcept;
   }

   public ConceptDataSimpleReference(Concept enclosingConcept, byte[] roBytes, byte[] mutableBytes)
           throws IOException {
      super(new NidDataInMemory(roBytes, mutableBytes));
      assert enclosingConcept != null : "enclosing concept cannot be null.";
      this.enclosingConcept = enclosingConcept;
   }

   //~--- methods -------------------------------------------------------------

   /*
    * (non-Javadoc)
    *
    * @see
    * org.ihtsdo.db.bdb.concept.I_ManageConceptData#add(org.ihtsdo.db.bdb.concept
    * .component.refset.RefsetMember)
    */
   @Override
   public void add(RefsetMember<?, ?> refsetMember) throws IOException {
      getRefsetMembers().addDirect(refsetMember);
      getMemberNids().add(refsetMember.nid);
      addToMemberMap(refsetMember);
      modified();
   }

   private void addConceptNidsAffectedByCommit(Collection<? extends ConceptComponent<?, ?>> componentList,
           Collection<Integer> affectedConceptNids)
           throws IOException {
      if (componentList != null) {
         for (ConceptComponent<?, ?> cc : componentList) {
            addConceptNidsAffectedByCommit(cc, affectedConceptNids);
         }
      }
   }

   private void addConceptNidsAffectedByCommit(ConceptComponent<?, ?> cc,
           Collection<Integer> affectedConceptNids)
           throws IOException {
      if (cc != null) {
         if (cc.isUncommitted()) {
            if (cc instanceof RelationshipChronicleBI) {
               RelationshipChronicleBI r = (RelationshipChronicleBI) cc;

               affectedConceptNids.add(r.getSourceNid());
               affectedConceptNids.add(r.getTargetNid());
               ChangeNotifier.touchRelTarget(r.getTargetNid());
               ChangeNotifier.touchRelOrigin(r.getSourceNid());
            } else if (cc instanceof RefexChronicleBI) {
               RefexChronicleBI r = (RefexChronicleBI) cc;

               affectedConceptNids.add(Ts.get().getConceptNidForNid(r.getReferencedComponentNid()));
               affectedConceptNids.add(r.getRefexNid());
               ChangeNotifier.touchRefexRC(r.getReferencedComponentNid());
            } else {
               affectedConceptNids.add(getNid());
            }
         }
      }
   }

   @Override
   protected void addToMemberMap(RefsetMember<?, ?> refsetMember) {
      memberMapLock.lock();

      try {
         if (refsetMembersMap.get() != null) {
            refsetMembersMap.get().put(refsetMember.nid, refsetMember);
         }

         if (refsetComponentMap.get() != null) {
            refsetComponentMap.get().put(refsetMember.getComponentNid(), refsetMember);
         }
      } finally {
         memberMapLock.unlock();
      }
   }

   private void addUncommittedNids(Collection<? extends ConceptComponent<?, ?>> componentList,
                                   NidListBI uncommittedNids) {
      if (componentList != null) {
         for (ConceptComponent<?, ?> cc : componentList) {
            addUncommittedNids(cc, uncommittedNids);
         }
      }
   }

   private void addUncommittedNids(ConceptComponent<?, ?> cc, NidListBI uncommittedNids) {
      if (cc != null) {
         if (cc.getTime() == Long.MAX_VALUE) {
            uncommittedNids.add(cc.nid);
         } else {
            if (cc.revisions != null) {
               for (Revision<?, ?> r : cc.revisions) {
                  if (r.getTime() == Long.MAX_VALUE) {
                     uncommittedNids.add(cc.nid);

                     break;
                  }
               }
            }
         }

         if (cc.annotations != null) {
            for (ConceptComponent<?, ?> annotation : cc.annotations) {
               if (annotation.annotations != null) {
                  for (ConceptComponent<?, ?> aa : annotation.annotations) {
                     addUncommittedNids(aa, uncommittedNids);
                  }
               }

               if (annotation.getTime() == Long.MAX_VALUE) {
                  uncommittedNids.add(annotation.nid);

                  break;
               } else if (annotation.revisions != null) {
                  for (Revision<?, ?> r : annotation.revisions) {
                     if (r.getTime() == Long.MAX_VALUE) {
                        uncommittedNids.add(annotation.nid);

                        break;
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void cancel() throws IOException {
      cancel(attributes.get());
      cancel(srcRels.get());
      cancel(descriptions.get());
      cancel(images.get());
      cancel(refsetMembers.get());
   }

   private void cancel(Collection<? extends ConceptComponent<?, ?>> componentList) throws IOException {
      ArrayList<ConceptComponent<?, ?>> toRemove = new ArrayList<ConceptComponent<?, ?>>();

      if (componentList != null) {
         for (ConceptComponent<?, ?> cc : componentList) {
            if (cancel(cc)) {
               toRemove.add(cc);
               removeRefsetReferences(cc);
            }
         }

         for (ConceptComponent<?, ?> cc : toRemove) {
             componentList.remove(cc);
             cc.primordialSapNid = -1;
         }
      }
   }

   private boolean cancel(ConceptComponent<?, ?> cc) throws IOException {
      if (cc == null) {
         return true;
      }

      // component
      if (cc.getTime() == Long.MAX_VALUE) {
         cc.cancel();
         removeRefsetReferences(cc);
         cc.primordialSapNid = -1;

         return true;
      }

      cc.cancel();

      return false;
   }

   @Override
   public void diet() {
      if (!isUnwritten()) {
         refsetMembersMap.set(null);
         refsetComponentMap.set(null);
         refsetMembers.set(null);
      }
   }

   @SuppressWarnings("unchecked")
   private void handleCanceledComponents() {
      if (lastExtinctRemoval < BdbCommitManager.getLastCancel()) {
         if ((refsetMembers != null) && (refsetMembers.get() != null) && (refsetMembers.get().size() > 0)) {
            List<RefsetMember<?, ?>> removed = (List<RefsetMember<?,
                                                  ?>>) removeCanceledFromList(refsetMembers.get());

            if ((refsetMembersMap.get() != null) || (refsetComponentMap.get() != null)) {
               Map<Integer, ?> memberMap    = refsetMembersMap.get();
               Map<Integer, ?> componentMap = refsetComponentMap.get();

               for (RefsetMember<?, ?> cc : removed) {
                  if (memberMap != null) {
                     memberMap.remove(cc.getNid());
                  }

                  if (componentMap != null) {
                     componentMap.remove(cc.getComponentNid());
                  }
               }
            }
         }

         if ((descriptions != null) && (descriptions.get() != null) && (descriptions.get().size() > 0)) {
            AddDescriptionSet descList = descriptions.get();

            removeCanceledFromList(descList);
         }

         if ((images != null) && (images.get() != null) && (images.get().size() > 0)) {
            removeCanceledFromList(images.get());
         }

         if ((srcRels != null) && (srcRels.get() != null) && (srcRels.get().size() > 0)) {
            removeCanceledFromList(srcRels.get());
         }

         lastExtinctRemoval = Bdb.gVersion.incrementAndGet();
      }
   }

   @Override
   public boolean readyToWrite() {
      if (attributes.get() != null) {
         attributes.get().readyToWriteComponent();
      }

      if (srcRels.get() != null) {
         for (Relationship r : srcRels.get()) {
            assert r.readyToWriteComponent();
         }
      }

      if (descriptions.get() != null) {
         for (Description component : descriptions.get()) {
            assert component.readyToWriteComponent();
         }
      }

      if (images.get() != null) {
         for (Image component : images.get()) {
            assert component.readyToWriteComponent();
         }
      }

      if (refsetMembers.get() != null) {
         for (RefsetMember component : refsetMembers.get()) {
            assert component.readyToWriteComponent();
         }
      }

      if (descNids.get() != null) {
         for (Integer component : descNids.get()) {
            assert component != null;
            assert component != Integer.MAX_VALUE;
         }
      }

      if (srcRelNids.get() != null) {
         for (Integer component : srcRelNids.get()) {
            assert component != null;
            assert component != Integer.MAX_VALUE;
         }
      }

      if (imageNids.get() != null) {
         for (Integer component : imageNids.get()) {
            assert component != null;
            assert component != Integer.MAX_VALUE;
         }
      }

      if (memberNids.get() != null) {
         for (Integer component : memberNids.get()) {
            assert component != null;
            assert component != Integer.MAX_VALUE;
         }
      }

      return true;
   }

   private List<? extends ConceptComponent<?,
           ?>> removeCanceledFromList(Collection<? extends ConceptComponent<?, ?>> ccList) {
      List<ConceptComponent<?, ?>> toRemove = new ArrayList<ConceptComponent<?, ?>>();

      if (ccList != null) {
         synchronized (ccList) {
            for (ConceptComponent<?, ?> cc : ccList) {
               if (cc.getTime() == Long.MIN_VALUE) {
                  toRemove.add(cc);
                  cc.clearVersions();
                  Concept.componentsCRHM.remove(cc.getNid());
               } else {
                  if (cc.revisions != null) {
                     List<Revision<?, ?>> revisionToRemove = new ArrayList<Revision<?, ?>>();

                     for (Revision<?, ?> r : cc.revisions) {
                        if (r.getTime() == Long.MIN_VALUE) {
                           cc.clearVersions();
                           revisionToRemove.add(r);
                        }
                     }

                     for (Revision<?, ?> r : revisionToRemove) {
                        cc.revisions.remove(r);
                     }
                  }
               }
            }

            ccList.removeAll(toRemove);
         }
      }

      return toRemove;
   }

   private void removeRefsetReferences(ConceptComponent<?, ?> cc) throws IOException {
      for (RefexChronicleBI<?> rc : cc.getRefsetMembers()) {
         Concept      refsetCon = Concept.get(rc.getRefexNid());
         RefsetMember rm        = (RefsetMember) rc;

         rm.primordialSapNid = -1;
         BdbCommitManager.writeImmediate(refsetCon);
         BdbCommitManager.addUncommittedNoChecks(refsetCon);
      }
   }

   private void setupMemberMap(Collection<RefsetMember<?, ?>> refsetMemberList) {
      memberMapLock.lock();

      try {
         if ((refsetMembersMap.get() == null) || (refsetComponentMap.get() == null)) {
            ConcurrentHashMap<Integer, RefsetMember<?, ?>> memberMap = new ConcurrentHashMap<Integer,
                                                                          RefsetMember<?,
                                                                             ?>>(refsetMemberList.size(),
                                                                                0.75f, 2);
            ConcurrentHashMap<Integer, RefsetMember<?, ?>> componentMap = new ConcurrentHashMap<Integer,
                                                                             RefsetMember<?,
                                                                                ?>>(refsetMemberList.size(),
                                                                                   0.75f, 2);

            for (RefsetMember<?, ?> m : refsetMemberList) {
               memberMap.put(m.nid, m);
               componentMap.put(m.getComponentNid(), m);
            }

            refsetMembersMap.set(memberMap);
            refsetComponentMap.set(componentMap);
         }
      } finally {
         memberMapLock.unlock();
      }
   }

   //~--- get methods ---------------------------------------------------------

   private RefexChronicleBI<?> getAnnotation(int nid) throws IOException {
      RefexChronicleBI<?> cc = null;

      // recursive search through all annotations...
      if (getConceptAttributes() != null) {
         cc = getAnnotation(getConceptAttributes().annotations, nid);

         if (cc != null) {
            return cc;
         }
      }

      if (getDescriptions() != null) {
         for (Description d : getDescriptions()) {
            cc = getAnnotation(d.annotations, nid);

            if (cc != null) {
               return cc;
            }
         }
      }

      if (getSourceRels() != null) {
         for (Relationship r : getSourceRels()) {
            cc = getAnnotation(r.annotations, nid);

            if (cc != null) {
               return cc;
            }
         }
      }

      if (getImages() != null) {
         for (Image i : getImages()) {
            cc = getAnnotation(i.annotations, nid);

            if (cc != null) {
               return cc;
            }
         }
      }

      if (getRefsetMembers() != null) {
         for (RefsetMember r : getRefsetMembers()) {
            cc = getAnnotation(r.annotations, nid);

            if (cc != null) {
               return cc;
            }
         }
      }

      return null;
   }

   private RefexChronicleBI<?> getAnnotation(Collection<? extends RefexChronicleBI<?>> annotations, int nid)
           throws IOException {
      if (annotations == null) {
         return null;
      }

      for (RefexChronicleBI<?> annotation : annotations) {
         if (annotation.getNid() == nid) {
            return annotation;
         }

         RefexChronicleBI<?> cc = getAnnotation(annotation.getAnnotations(), nid);

         if (cc != null) {
            return cc;
         }
      }

      return null;
   }

   @Override
   public ComponentChronicleBI<?> getComponent(int nid) throws IOException {
      if ((getConceptAttributes() != null) && (getConceptAttributes().nid == nid)) {
         return getConceptAttributes();
      }

      if (getDescNids().contains(nid)) {
         for (Description d : getDescriptions()) {
            if (d.getNid() == nid) {
               return d;
            }
         }
      }

      if (getSrcRelNids().contains(nid)) {
         for (Relationship r : getSourceRels()) {
            if (r.getNid() == nid) {
               return r;
            }
         }
      }

      if (getImageNids().contains(nid)) {
         for (Image i : getImages()) {
            if (i.getNid() == nid) {
               return i;
            }
         }
      }

      if (getMemberNids().contains(nid)) {
         return getRefsetMember(nid);
      }

      return getAnnotation(nid);
   }

   @Override
   public ConceptAttributes getConceptAttributes() throws IOException {
      if (attributes.get() == null) {
         attrLock.lock();

         try {
            if (attributes.get() == null) {
               ArrayList<ConceptAttributes> components = getList(new ConceptAttributesBinder(),
                                                            OFFSETS.ATTRIBUTES, enclosingConcept);

               if ((components != null) && (components.size() > 0)) {
                  attributes.compareAndSet(null, components.get(0));
               }
            }
         } finally {
            attrLock.unlock();
         }
      }

      return attributes.get();
   }

   @Override
   public ConceptAttributes getConceptAttributesIfChanged() throws IOException {
      return attributes.get();
   }

   @Override
   public Collection<Integer> getConceptNidsAffectedByCommit() throws IOException {
      Collection<Integer> uncommittedNids = new HashSet<Integer>();

      addConceptNidsAffectedByCommit(attributes.get(), uncommittedNids);
      addConceptNidsAffectedByCommit(srcRels.get(), uncommittedNids);
      addConceptNidsAffectedByCommit(descriptions.get(), uncommittedNids);
      addConceptNidsAffectedByCommit(images.get(), uncommittedNids);
      addConceptNidsAffectedByCommit(refsetMembers.get(), uncommittedNids);

      return uncommittedNids;
   }

   @Override
   public Set<Integer> getDescNids() throws IOException {
      if (descNids.get() == null) {
         ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<Integer>(getDescNidsReadOnly());

         temp.addAll(getMutableIntSet(OFFSETS.DESC_NIDS));
         descNids.compareAndSet(null, temp);
      }

      return descNids.get();
   }

   @Override
   public Set<Integer> getDescNidsReadOnly() throws IOException {
      return getReadOnlyIntSet(OFFSETS.DESC_NIDS);
   }

   @Override
   public AddDescriptionSet getDescriptions() throws IOException {
      if (descriptions.get() == null) {
         descLock.lock();

         try {
            if (descriptions.get() == null) {
               descriptions.compareAndSet(null,
                                          new AddDescriptionSet(getList(new DescriptionBinder(),
                                             OFFSETS.DESCRIPTIONS, enclosingConcept)));
            }
         } finally {
            descLock.unlock();
         }
      }

      handleCanceledComponents();

      return descriptions.get();
   }

   @Override
   public Collection<Description> getDescriptionsIfChanged() throws IOException {
      return descriptions.get();
   }

   @Override
   public Set<Integer> getImageNids() throws IOException {
      if (imageNids.get() == null) {
         ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<Integer>(getImageNidsReadOnly());

         temp.addAll(getMutableIntSet(OFFSETS.IMAGE_NIDS));
         imageNids.compareAndSet(null, temp);
      }

      return imageNids.get();
   }

   @Override
   public Set<Integer> getImageNidsReadOnly() throws IOException {
      return getReadOnlyIntSet(OFFSETS.IMAGE_NIDS);
   }

   @Override
   public AddImageSet getImages() throws IOException {
      if (images.get() == null) {
         imageLock.lock();

         try {
            if (images.get() == null) {
               images.compareAndSet(null,
                                    new AddImageSet(getList(new ImageBinder(), OFFSETS.IMAGES,
                                       enclosingConcept)));
            }
         } finally {
            imageLock.unlock();
         }
      }

      handleCanceledComponents();

      return images.get();
   }

   @Override
   public Collection<Image> getImagesIfChanged() throws IOException {
      return images.get();
   }

   private <C extends ConceptComponent<V, C>,
            V extends Revision<V, C>> ArrayList<C> getList(ConceptComponentBinder<V, C> binder,
               OFFSETS offset, Concept enclosingConcept)
           throws IOException {
      binder.setupBinder(enclosingConcept);

      ArrayList<C> componentList;
      TupleInput   readOnlyInput = nidData.getReadOnlyTupleInput();

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

      TupleInput readWriteInput = nidData.getMutableTupleInput();

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

   private Collection<RefsetMember<?, ?>> getList(RefsetMemberBinder binder, OFFSETS offset,
           Concept enclosingConcept)
           throws IOException {
      binder.setupBinder(enclosingConcept);

      Collection<RefsetMember<?, ?>> componentList;
      TupleInput                     readOnlyInput = nidData.getReadOnlyTupleInput();

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

      TupleInput readWriteInput = nidData.getMutableTupleInput();

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

   @Override
   public Set<Integer> getMemberNids() throws IOException {
      if (memberNids.get() == null) {
         ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<Integer>(getMemberNidsReadOnly());

         temp.addAll(getMutableIntSet(OFFSETS.MEMBER_NIDS));
         memberNids.compareAndSet(null, temp);
      }

      return memberNids.get();
   }

   @Override
   public Set<Integer> getMemberNidsReadOnly() throws IOException {
      return getReadOnlyIntSet(OFFSETS.MEMBER_NIDS);
   }

   protected ConcurrentSkipListSet<Integer> getMutableIntSet(OFFSETS offset) throws IOException {
      TupleInput mutableInput = nidData.getMutableTupleInput();

      if (mutableInput.available() < OFFSETS.getHeaderSize()) {
         return new ConcurrentSkipListSet<Integer>();
      }

      mutableInput.mark(OFFSETS.getHeaderSize());
      mutableInput.skipFast(offset.offset);

      int dataOffset = mutableInput.readInt();

      mutableInput.reset();
      mutableInput.skipFast(dataOffset);

      IntSetBinder binder = new IntSetBinder();

      return binder.entryToObject(mutableInput);
   }

   public I_GetNidData getNidData() {
      return nidData;
   }

   protected ConcurrentSkipListSet<Integer> getReadOnlyIntSet(OFFSETS offset) throws IOException {
      TupleInput readOnlyInput = nidData.getReadOnlyTupleInput();

      if (readOnlyInput.available() < OFFSETS.getHeaderSize()) {
         return new ConcurrentSkipListSet<Integer>();
      }

      readOnlyInput.mark(OFFSETS.getHeaderSize());
      readOnlyInput.skipFast(offset.offset);

      int dataOffset = readOnlyInput.readInt();

      readOnlyInput.reset();
      readOnlyInput.skipFast(dataOffset);

      IntSetBinder binder = new IntSetBinder();

      return binder.entryToObject(readOnlyInput);
   }

   @Override
   public RefsetMember<?, ?> getRefsetMember(int memberNid) throws IOException {
      if (isAnnotationStyleRefex()) {
         if (getMemberNids().contains(memberNid)) {
            if (Bdb.getNidCNidMap().getCNid(memberNid) == getNid()) {
               return (RefsetMember<?, ?>) getAnnotation(memberNid);
            } else {
               return (RefsetMember<?, ?>) Bdb.getComponent(memberNid);
            }
         }

         return null;
      }

      Collection<RefsetMember<?, ?>> refsetMemberList = getRefsetMembers();

      if (refsetMembersMap.get() != null) {
         return refsetMembersMap.get().get(memberNid);
      }

      if (refsetMemberList.size() < useMemberMapThreshold) {
         for (RefsetMember<?, ?> member : refsetMemberList) {
            if (member.nid == memberNid) {
               return member;
            }
         }

         return null;
      }

      if (refsetMembersMap.get() == null) {
         setupMemberMap(refsetMemberList);
      }

      return refsetMembersMap.get().get(memberNid);
   }

   @Override
   public RefsetMember<?, ?> getRefsetMemberForComponent(int componentNid) throws IOException {
      Collection<RefsetMember<?, ?>> refsetMemberList = getRefsetMembers();

      if (refsetMemberList.size() < useMemberMapThreshold) {
         for (RefsetMember<?, ?> member : refsetMemberList) {
            if (member.getComponentNid() == componentNid) {
               return member;
            }
         }

         return null;
      }

      if (refsetComponentMap.get() == null) {
         setupMemberMap(refsetMemberList);
      }

      return refsetComponentMap.get().get(componentNid);
   }

   @Override
   public AddMemberSet getRefsetMembers() throws IOException {
      if (isAnnotationStyleRefex() && isAnnotationIndex()) {
         ArrayList<RefsetMember<?, ?>> members = new ArrayList<RefsetMember<?, ?>>(getMemberNids().size());

         for (int memberNid : getMemberNids()) {
            RefsetMember<?, ?> member = (RefsetMember<?, ?>) Bdb.getComponent(memberNid);

            if (member != null) {
               members.add(member);
            }
         }

         return new AddMemberSet(members);
      }

      if (refsetMembers.get() == null) {
         refsetMembersLock.lock();

         try {
            if (refsetMembers.get() == null) {
               refsetMembers.compareAndSet(null,
                                           new AddMemberSet(getList(new RefsetMemberBinder(enclosingConcept),
                                              OFFSETS.REFSET_MEMBERS, enclosingConcept)));
            }
         } finally {
            refsetMembersLock.unlock();
         }
      }

      handleCanceledComponents();

      return refsetMembers.get();
   }

   @Override
   public Collection<RefsetMember<?, ?>> getRefsetMembersIfChanged() throws IOException {
      return refsetMembers.get();
   }

   @Override
   public AddSrcRelSet getSourceRels() throws IOException {
      if (srcRels.get() == null) {
         sourceRelLock.lock();

         try {
            if (srcRels.get() == null) {
               srcRels.compareAndSet(null,
                                     new AddSrcRelSet(getList(new RelationshipBinder(), OFFSETS.SOURCE_RELS,
                                        enclosingConcept), true));
            }
         } finally {
            sourceRelLock.unlock();
         }
      }

      handleCanceledComponents();

      return srcRels.get();
   }

   @Override
   public Collection<Relationship> getSourceRelsIfChanged() throws IOException {
      return srcRels.get();
   }

   @Override
   public Set<Integer> getSrcRelNids() throws IOException {
      if (srcRelNids.get() == null) {
         ConcurrentSkipListSet<Integer> temp = new ConcurrentSkipListSet<Integer>(getSrcRelNidsReadOnly());

         temp.addAll(getMutableIntSet(OFFSETS.SRC_REL_NIDS));
         srcRelNids.compareAndSet(null, temp);
      }

      return srcRelNids.get();
   }

   @Override
   public Set<Integer> getSrcRelNidsReadOnly() throws IOException {
      return getReadOnlyIntSet(OFFSETS.SRC_REL_NIDS);
   }

   @Override
   public NidListBI getUncommittedNids() {
      NidListBI uncommittedNids = new NidList();

      addUncommittedNids(attributes.get(), uncommittedNids);
      addUncommittedNids(srcRels.get(), uncommittedNids);
      addUncommittedNids(descriptions.get(), uncommittedNids);
      addUncommittedNids(images.get(), uncommittedNids);
      addUncommittedNids(refsetMembers.get(), uncommittedNids);

      return uncommittedNids;
   }

   @Override
   public boolean hasComponent(int nid) throws IOException {
      if (getNid() == nid) {
         return true;
      }

      if (getDescNids().contains(nid)) {
         return true;
      }

      if (getSrcRelNids().contains(nid)) {
         return true;
      }

      if (getImageNids().contains(nid)) {
         return true;
      }

      if (getMemberNids().contains(nid)) {
         return true;
      }

      return false;
   }

   private boolean hasUncommittedAnnotation(ConceptComponent<?, ?> cc) {
      if ((cc != null) && (cc.annotations != null)) {
         for (RefexChronicleBI<?> rmc : cc.annotations) {
            if (rmc.isUncommitted()) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean hasUncommittedComponents() {
      if (hasUncommittedVersion(attributes.get())) {
         return true;
      }

      if (hasUncommittedVersion(srcRels.get())) {
         return true;
      }

      if (hasUncommittedVersion(descriptions.get())) {
         return true;
      }

      if (hasUncommittedVersion(images.get())) {
         return true;
      }

      if (hasUncommittedVersion(refsetMembers.get())) {
         return true;
      }

      return false;
   }

   private boolean hasUncommittedId(ConceptComponent<?, ?> cc) {
      if ((cc != null) && (cc.getAdditionalIdentifierParts() != null)) {
         for (IdentifierVersion idv : cc.getAdditionalIdentifierParts()) {
            if (idv.getTime() == Long.MAX_VALUE) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean hasUncommittedVersion(Collection<? extends ConceptComponent<?, ?>> componentList) {
      if (componentList != null) {
         for (ConceptComponent<?, ?> cc : componentList) {
            if (hasUncommittedVersion(cc)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean hasUncommittedVersion(ConceptComponent<?, ?> cc) {
      if (cc != null) {
         if (cc.getTime() == Long.MAX_VALUE) {
            return true;
         }

         if (cc.revisions != null) {
            for (Revision<?, ?> r : cc.revisions) {
               if (r.getTime() == Long.MAX_VALUE) {
                  return true;
               }
            }
         }

         if (hasUncommittedId(cc)) {
            return true;
         }

         if (hasUncommittedAnnotation(cc)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isAnnotationIndex() throws IOException {
      if (annotationIndex == null) {
         annotationIndex = getIsAnnotationStyleIndex();
      }

      return annotationIndex;
   }

   @Override
   public boolean isAnnotationStyleRefex() throws IOException {
      if (annotationStyleRefset == null) {
         annotationStyleRefset = getIsAnnotationStyleRefset();
      }

      return annotationStyleRefset;
   }

   @Override
   public boolean isAnnotationStyleSet() throws IOException {
      return isAnnotationStyleRefex();
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void set(ConceptAttributes attr) throws IOException {
      if (attributes.get() != null) {
         throw new IOException("Attributes is already set. Please modify the exisiting attributes object.");
      }

      if (!attributes.compareAndSet(null, attr)) {
         throw new IOException("Attributes is already set. Please modify the exisiting attributes object.");
      }

      enclosingConcept.modified();
   }

   @Override
   public void setAnnotationIndex(boolean annotationIndex) throws IOException {
      modified();
      this.annotationIndex = annotationIndex;
   }

   @Override
   public void setAnnotationStyleRefset(boolean annotationStyleRefset) {
      modified();
      this.annotationStyleRefset = annotationStyleRefset;
   }

   @Override
   public NidSetBI setCommitTime(long time) {
      NidSet sapNids = new NidSet();

      setCommitTime(attributes.get(), time, sapNids);
      setCommitTime(srcRels.get(), time, sapNids);
      setCommitTime(descriptions.get(), time, sapNids);
      setCommitTime(images.get(), time, sapNids);
      setCommitTime(refsetMembers.get(), time, sapNids);

      return sapNids;
   }

   private void setCommitTime(Collection<? extends ConceptComponent<?, ?>> componentList, long time,
                              NidSetBI sapNids) {
      if (componentList != null) {
         for (ConceptComponent<?, ?> cc : componentList) {
            setCommitTime(cc, time, sapNids);
         }
      }
   }

   private void setCommitTime(ConceptComponent<?, ?> cc, long time, NidSetBI sapNids) {

      // component
      if(cc != null){
        if (cc.getTime() == Long.MAX_VALUE) {
            cc.setTime(time);
            sapNids.add(cc.primordialSapNid);
        }

        if (cc.revisions != null) {
            for (Revision<?, ?> r : cc.revisions) {
                if (r.getTime() == Long.MAX_VALUE) {
                r.setTime(time);
                sapNids.add(r.sapNid);
                }
            }
        }

        // id
        if (cc.getAdditionalIdentifierParts() != null) {
            for (IdentifierVersion idv : cc.getAdditionalIdentifierParts()) {
                if (idv.getTime() == Long.MAX_VALUE) {
                idv.setTime(time);
                sapNids.add(idv.getStampNid());
                }
            }
        }

        // annotation
        if (cc.annotations != null) {
            for (RefexChronicleBI<?> rc : cc.annotations) {
                RefsetMember<?, ?> rm = (RefsetMember<?, ?>) rc;

                if (rm.getTime() == Long.MAX_VALUE) {
                rm.setTime(time);
                sapNids.add(rm.getStampNid());
                }

                if (rm.revisions != null) {
                for (RefsetRevision<?, ?> rr : rm.revisions) {
                    if (rr.getTime() == Long.MAX_VALUE) {
                        rr.setTime(time);
                        sapNids.add(rr.getStampNid());
                    }
                }
                }
            }
        }
      }
   }
}
