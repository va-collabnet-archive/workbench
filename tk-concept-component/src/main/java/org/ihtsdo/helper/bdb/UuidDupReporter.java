/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.helper.bdb;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: Auto-generated Javadoc
/**
 * The Class UuidDupReporter.
 *
 * @author kec
 */
public class UuidDupReporter implements ProcessUnfetchedConceptDataBI {
   
   /** The count. */
   private AtomicInteger                         count = new AtomicInteger(0);
   
   /** The dots. */
   private AtomicInteger                         dots  = new AtomicInteger(0);
   
   /** The dup map. */
   ConcurrentHashMap<UUID, Collection<DupEntry>> dupMap;
   
   /** The dup uuids. */
   ConcurrentSkipListSet<UUID>                   dupUuids;
   
   /** The nidset. */
   private NidBitSetBI                           nidset;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new uuid dup reporter.
    *
    * @param dupUuids the dup uuids
    * @throws IOException signals that an I/O exception has occurred
    */
   public UuidDupReporter(ConcurrentSkipListSet<UUID> dupUuids) throws IOException {
      this.dupUuids = dupUuids;
      dupMap        = new ConcurrentHashMap<UUID, Collection<DupEntry>>(dupUuids.size());

      for (UUID duped : dupUuids) {
         dupMap.put(duped, new CopyOnWriteArrayList<DupEntry>());
      }

      nidset = Ts.get().getAllConceptNids();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the if dup.
    *
    * @param component the component
    * @throws IOException signals that an I/O exception has occurred
    */
   private void addIfDup(ComponentChronicleBI component) throws IOException {
      UUID primUuid = component.getPrimUuid();

      if (primUuid.equals(UUID.fromString("80126d25-fc16-5a9f-b182-68a01d64504b"))) {
         System.out.print("");
      }

      if (dupUuids.contains(primUuid)) {
         Collection<DupEntry> dupCollection = dupMap.get(primUuid);

         dupCollection.add(new DupEntry(component, component.getEnclosingConcept()));

         if (dupCollection.size() == 2) {
            System.out.print("");
         }
      }

      for (ComponentChronicleBI annotation : component.getAnnotations()) {
         addIfDup(annotation);
      }
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.api.ContinuationTrackerBI#continueWork()
    */
   @Override
   public boolean continueWork() {
      return true;
   }

   /**
    * Process concept.
    *
    * @param concept the concept
    * @throws IOException signals that an I/O exception has occurred
    */
   private void processConcept(ConceptChronicleBI concept) throws IOException {

      // add prim uuids to list
      // concept attributtes
      addIfDup(concept.getConceptAttributes());

      // descriptions
      for (DescriptionChronicleBI desc : concept.getDescriptions()) {
         addIfDup(desc);
      }

      // relationships
      for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
         addIfDup(rel);
      }

      // media
      for (MediaChronicleBI media : concept.getMedia()) {
         addIfDup(media);
      }

      if (!concept.isAnnotationStyleRefex()) {
         for (RefexChronicleBI refex : concept.getRefsetMembers()) {
            addIfDup(refex);
         }
      }
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#processUnfetchedConceptData(int, org.ihtsdo.tk.api.ConceptFetcherBI)
    */
   @Override
   public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
      count.incrementAndGet();

      if (count.get() % 1000 == 0) {
         System.out.print(".");
         System.out.flush();
         dots.incrementAndGet();

         if (dots.get() > 80) {
            dots.set(0);
            System.out.println();
            System.out.print(count.get() + ": ");
         }
      }

      processConcept(fetcher.fetch());
   }

   /**
    * Report dup classes.
    */
   public void reportDupClasses() {
      int         printCount  = 0;
      Set<DupSet> dupClassSet = new HashSet<DupSet>();

      for (Collection<DupEntry> dup : dupMap.values()) {
         if (printCount < 100) {
            printCount++;
            System.out.println(dup);
         }

         Class<?>[] dupClassList = new Class<?>[dup.size()];
         int        i            = 0;

         for (DupEntry cc : dup) {
            dupClassList[i++] = cc.dup.getClass();
         }

         DupSet dupSet = new DupSet(dupClassList);

         dupClassSet.add(dupSet);
      }

      for (DupSet ds : dupClassSet) {
         System.out.println(ds);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#getNidSet()
    */
   @Override
   public NidBitSetBI getNidSet() throws IOException {
      return nidset;
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class DupEntry.
    */
   private static class DupEntry {
      
      /** The dup. */
      ComponentChronicleBI dup;
      
      /** The enclosing concept. */
      ConceptChronicleBI   enclosingConcept;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new dup entry.
       *
       * @param dup the dup
       * @param enclosingConcept the enclosing concept
       */
      public DupEntry(ComponentChronicleBI dup, ConceptChronicleBI enclosingConcept) {
         this.dup              = dup;
         this.enclosingConcept = enclosingConcept;
      }
   }


   /**
    * The Class DupSet.
    */
   private static class DupSet implements Comparable<DupSet> {
      
      /** The dups. */
      ArrayList<Class<?>> dups;

      //~--- constructors -----------------------------------------------------

      /**
       * Instantiates a new dup set.
       *
       * @param classes the classes
       */
      public DupSet(Class<?>... classes) {
         Arrays.sort(classes, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
               if ((o1 == null) || (o2 == null)) {
                  System.out.println("Uh oh...");
               }

               return o1.toString().compareTo(o2.toString());
            }
         });
         dups = new ArrayList<Class<?>>(classes.length);

         for (Class<?> c : classes) {
            dups.add(c);
         }
      }

      //~--- methods ----------------------------------------------------------

      /* (non-Javadoc)
       * @see java.lang.Comparable#compareTo(java.lang.Object)
       */
      @Override
      public int compareTo(DupSet o) {
         if (dups.size() != o.dups.size()) {
            return dups.size() - o.dups.size();
         }

         for (int i = 0; i < dups.size(); i++) {
            int compare = dups.get(i).toString().compareTo(o.dups.get(i).toString());

            if (compare != 0) {
               return compare;
            }
         }

         return 0;
      }

      /* (non-Javadoc)
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(Object obj) {
         if (obj instanceof DupSet) {
            return dups.equals(((DupSet) obj).dups);
         }

         return false;
      }

      /* (non-Javadoc)
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode() {
         return dups.hashCode();
      }

      /* (non-Javadoc)
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString() {
         return dups.toString();
      }
   }
}
