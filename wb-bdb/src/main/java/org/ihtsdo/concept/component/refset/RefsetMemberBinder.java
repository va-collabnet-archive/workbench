package org.ihtsdo.concept.component.refset;

//~--- non-JDK imports --------------------------------------------------------

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import org.dwfa.ace.log.AceLog;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_BindConceptComponents;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class RefsetMemberBinder extends TupleBinding<Collection<RefsetMember<?, ?>>>
        implements I_BindConceptComponents {
   public static AtomicInteger encountered                   = new AtomicInteger();
   public static AtomicInteger written                       = new AtomicInteger();
   private static int          maxReadOnlyStatusAtPositionId = Bdb.getSapDb().getReadOnlyMax();

   //~--- fields --------------------------------------------------------------

   RefsetMemberFactory                    factory = new RefsetMemberFactory();
   private Concept                        enclosingConcept;
   private Collection<RefsetMember<?, ?>> refsetMemberList;

   //~--- constructors --------------------------------------------------------

   public RefsetMemberBinder(Concept concept) {
      this.enclosingConcept = concept;
   }

   //~--- methods -------------------------------------------------------------

   @SuppressWarnings("unchecked")
   @Override
   public Collection<RefsetMember<?, ?>> entryToObject(TupleInput input) {
      assert enclosingConcept != null;

      int                                  listSize = input.readInt();
      Collection<RefsetMember<?, ?>>       newRefsetMemberList;
      HashMap<Integer, RefsetMember<?, ?>> nidToRefsetMemberMap = null;

      if (refsetMemberList != null) {
         newRefsetMemberList  = refsetMemberList;
         nidToRefsetMemberMap = new HashMap<Integer, RefsetMember<?, ?>>(listSize);

         for (RefsetMember<?, ?> component : refsetMemberList) {
            nidToRefsetMemberMap.put(component.nid, component);
         }
      } else {
         newRefsetMemberList = new ArrayList<RefsetMember<?, ?>>(listSize);
      }

      for (int index = 0; index < listSize; index++) {
         int typeNid = input.readInt();

         // Can be removed in the future, here strictly for read/write conformance testing.
         try {
            REFSET_TYPES.nidToType(typeNid);
         } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(new Exception("For concept: "
                    + enclosingConcept.toString(), e1));
            AceLog.getAppLog().info("List prior to exception: " + newRefsetMemberList);

            return newRefsetMemberList;
         }

         input.mark(8);

         int nid = input.readInt();

         input.reset();

         Object component = Concept.componentsCRHM.get(nid);

         if ((component == null) || (component instanceof RefsetMember)) {
            RefsetMember<?, ?> refsetMember = (RefsetMember<?, ?>) component;

            if ((refsetMember != null) && (refsetMember.getTime() == Long.MIN_VALUE)) {
               refsetMember = null;
               Concept.componentsCRHM.remove(nid);
            }

            if ((nidToRefsetMemberMap != null) && nidToRefsetMemberMap.containsKey(nid)) {
               if (refsetMember == null) {
                  refsetMember = nidToRefsetMemberMap.get(nid);

                  RefsetMember<?, ?> oldMember = (RefsetMember<?,
                                                    ?>) Concept.componentsCRHM.putIfAbsent(nid, refsetMember);

                  if (oldMember != null) {
                     refsetMember = oldMember;

                     if (nidToRefsetMemberMap != null) {
                        nidToRefsetMemberMap.put(nid, refsetMember);
                     }
                  }
               }

               refsetMember.readComponentFromBdb(input);
            } else {
               try {
                  if (refsetMember == null) {
                     refsetMember = factory.create(nid, typeNid, enclosingConcept.getNid(), input);

                     if (refsetMember.getTime() != Long.MIN_VALUE &&
                             refsetMember.getRefexNid() == enclosingConcept.getNid()) {
                        RefsetMember<?, ?> oldMember = (RefsetMember<?,
                                                          ?>) Concept.componentsCRHM.putIfAbsent(nid,
                                                             refsetMember);

                        if (oldMember != null) {
                           refsetMember = oldMember;

                           if (nidToRefsetMemberMap != null) {
                              nidToRefsetMemberMap.put(nid, refsetMember);
                           }
                        }
                     }
                  } else {
                     refsetMember.merge(factory.create(nid, typeNid, enclosingConcept.getNid(), input), 
                             new HashSet<ConceptChronicleBI>());
                  }
               } catch (IOException e) {
                  throw new RuntimeException(e);
               }

               if (refsetMember.getTime() != Long.MIN_VALUE &&
                             refsetMember.getRefexNid() == enclosingConcept.getNid()) {
                  newRefsetMemberList.add(refsetMember);
               }
            }
         } else {
            StringBuilder sb = new StringBuilder();

            sb.append("Refset member has nid: ").append(nid);
            sb.append(" But another component has same nid:\n").append(component);

            try {
               sb.append("Refset member: \n"
                         + factory.create(nid, typeNid, enclosingConcept.getNid(), input));
            } catch (IOException ex) {
               AceLog.getAppLog().log(Level.WARNING, ex.getMessage(), ex);
            }

            AceLog.getAppLog().alertAndLogException(new Exception(sb.toString()));
         }
      }

      return newRefsetMemberList;
   }

   @Override
   public void objectToEntry(Collection<RefsetMember<?, ?>> list, TupleOutput output) {
      List<RefsetMember<?, ?>> refsetMembersToWrite = new ArrayList<RefsetMember<?, ?>>(list.size());

      for (RefsetMember<?, ?> refsetMember : list) {
         encountered.incrementAndGet();
         assert refsetMember.primordialSapNid != Integer.MAX_VALUE;

         if ((refsetMember.primordialSapNid > maxReadOnlyStatusAtPositionId)
                 && (refsetMember.getTime() != Long.MIN_VALUE &&
                             refsetMember.getRefexNid() == enclosingConcept.getNid())) {
            refsetMembersToWrite.add(refsetMember);
         } else {
            if (refsetMember.revisions != null) {
               for (RefsetRevision<?, ?> r : refsetMember.revisions) {
                  if ((r.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionId)
                          && (r.getTime() != Long.MIN_VALUE &&
                             refsetMember.getRefexNid() == enclosingConcept.getNid())) {
                     refsetMembersToWrite.add(refsetMember);

                     break;
                  }
               }
            }
         }
      }

      output.writeInt(refsetMembersToWrite.size());    // List size

      for (RefsetMember<?, ?> refsetMember : refsetMembersToWrite) {
         written.incrementAndGet();
         output.writeInt(refsetMember.getTypeNid());
         try{
             refsetMember.writeComponentToBdb(output, maxReadOnlyStatusAtPositionId);
         }catch (IOException ex) {
                    throw new RuntimeException(ex);
         }
         
      }
   }

   @Override
   public void setupBinder(Concept enclosingConcept) {
      this.enclosingConcept = enclosingConcept;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Concept getEnclosingConcept() {
      return enclosingConcept;
   }

   //~--- set methods ---------------------------------------------------------

   public void setEnclosingConcept(Concept enclosingConcept) {
      this.enclosingConcept = enclosingConcept;
   }

   public void setTermComponentList(Collection<RefsetMember<?, ?>> componentList) {
      this.refsetMemberList = componentList;
   }
}
