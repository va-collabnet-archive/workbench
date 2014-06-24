/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
import org.ihtsdo.tk.api.TerminologyStoreDI;


/**
 * The Class UuidDupReporter reports on the duplicate uuids found by the
 * <code>UuidDupFinder</code>. This class implements
 * <code>ProcessUnfetchedConceptDataBI</code> and the contradiction detector can
 * be "run" using the terminology store method iterateConceptDataInParallel.
 *
 * @see
 * TerminologyStoreDI#iterateConceptDataInParallel(org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI).
 *
 */
public class UuidDupReporter implements ProcessUnfetchedConceptDataBI {

    private AtomicInteger count = new AtomicInteger(0);
    private AtomicInteger dots = new AtomicInteger(0);
    ConcurrentHashMap<UUID, Collection<DupEntry>> dupMap;
    ConcurrentSkipListSet<UUID> dupUuids;
    private NidBitSetBI nidset;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new uuid dup reporter basd on the given
     * <code>duplicateUuids</code>.
     *
     * @param duplicateUuids a list duplicate uuids
     * @throws IOException signals that an I/O exception has occurred
     */
    public UuidDupReporter(ConcurrentSkipListSet<UUID> duplicateUuids) throws IOException {
        this.dupUuids = duplicateUuids;
        dupMap = new ConcurrentHashMap<>(duplicateUuids.size());

        for (UUID duped : duplicateUuids) {
            dupMap.put(duped, new CopyOnWriteArrayList<DupEntry>());
        }

        nidset = Ts.get().getAllConceptNids();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Adds the component, if it contains a duplicate uuid, to a collection of
     * concepts that have duplicate uuids.
     *
     * @param component the component to check and add
     * @throws IOException signals that an I/O exception has occurred
     */
    private void addIfDup(ComponentChronicleBI component) throws IOException {
        UUID primUuid = component.getPrimUuid();

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

    /**
     *
     * @return <code>true</code>
     */
    @Override
    public boolean continueWork() {
        return true;
    }

    /**
     * Process the components on the specified
     * <code>concept</code> to determine if the component has a duplicate uuids.
     * Adds the component to a collection of duplicates, if a duplicate uuid is
     * used.
     *
     * @param concept the concept to check for duplicates
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processConcept(ConceptChronicleBI concept) throws IOException {

        // add prim uuids to list
        // concept attributes
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

    /**
     * Processes each concept to determine which, if any, components are
     * identified with the duplicate uuids.
     *
     * @param cNid the nid of the concept to process
     * @param fetcher the fetcher for getting the concept associated with * *
     * the <code>cNid</code> from the database
     * @throws Exception indicates an exception has occurred
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
     * Prints any components associated with the duplicate uuid. Prints the
     * concept and the component to the terminal or log.
     */
    public void reportDupClasses() {
        int printCount = 0;
        Set<DupSet> dupClassSet = new HashSet<>();

        for (Collection<DupEntry> dup : dupMap.values()) {
            if (printCount < 100) {
                printCount++;
                System.out.println(dup);
            }

            Class<?>[] dupClassList = new Class<?>[dup.size()];
            int i = 0;

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
    /**
     *
     * @return the set of nids to process
     * @throws IOException signals an I/O exception occurred
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidset;
    }

    //~--- inner classes -------------------------------------------------------
    /**
     * The Class DupEntry represents a component which uses a duplicate uuid.
     */
    private static class DupEntry {

        ComponentChronicleBI dup;
        ConceptChronicleBI enclosingConcept;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new dup entry based on the component associated with
         * the duplicate uuid,
         * <code>dup</code>, and its
         * <code>enclosingConcept</code>.
         *
         * @param dup the component associated with the duplicate uuid
         * @param enclosingConcept the component's enclosing concept
         */
        public DupEntry(ComponentChronicleBI dup, ConceptChronicleBI enclosingConcept) {
            this.dup = dup;
            this.enclosingConcept = enclosingConcept;
        }

        @Override
        public String toString() {
            return "DupEntry{" + "dup=\n" + dup + ",\n\n enclosingConcept=\n" + 
                    enclosingConcept.toLongString() + "\n}\n";
        }
        
        
    }

    /**
     * The Class DupSet represents a set of
     * <code>DuplicateEntries</code>.
     */
    private static class DupSet implements Comparable<DupSet> {

        ArrayList<Class<?>> dups;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new dup set based on the given
         * <code>classes</code>.
         *
         * @param classes the <code>DupEntry</code> classes
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
            dups = new ArrayList<>(classes.length);
            dups.addAll(Arrays.asList(classes));
        }

        //~--- methods ----------------------------------------------------------
        /**
         * Compares this object with the specified object for order. Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         *
         * @param o the <code>DupSet</code> to compare against
         * @return a negative integer, zero, or a positive integer as this
         * object is less than, equal to, or greater than the specified object.
         * @see Comparable#compareTo(java.lang.Object) 
         * 
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

        /**
         * Checks if this dup set is equal to another dup set.
         * @param obj the other <code>DupSet</code> to check
         * @return <code>true</code> if the two dup sets are equal
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DupSet) {
                return dups.equals(((DupSet) obj).dups);
            }

            return false;
        }

        /**
         * Gets a hash code representing this dup set
         * @return a hash code value for this dup set
         */
        @Override
        public int hashCode() {
            return dups.hashCode();
        }

        /**
         * Generates a string representing this dup set.
         * @return a string representation of this dup set
         */
        @Override
        public String toString() {
            return dups.toString();
        }
    }
}
