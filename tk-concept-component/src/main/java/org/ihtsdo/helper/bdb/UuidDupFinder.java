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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Class UuidDupFinder processes every concept in the database to determine
 * if the same uuid is used to identify more than one concept or component. This
 * class implements
 * <code>ProcessUnfetchedConceptDataBI</code> and can be "run" using the
 * terminology store method iterateConceptDataInParallel.
 *
 * @see
 * TerminologyStoreDI#iterateConceptDataInParallel(org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI)
 *
 */
public class UuidDupFinder implements ProcessUnfetchedConceptDataBI {

    private AtomicInteger count = new AtomicInteger(0);
    private AtomicInteger dots = new AtomicInteger(0);
    ConcurrentSkipListSet<UUID> allPrimUuids = new ConcurrentSkipListSet<UUID>();
    ConcurrentSkipListSet<UUID> dupUuids = new ConcurrentSkipListSet<UUID>();
    File dupsFile = new File("dups.oos");
    private final NidBitSetBI nidset;

    //~--- constant enums ------------------------------------------------------
    /**
     * The Enum PASS represent which pass of the data the processor is on.
     */
    private enum PASS {

        /**
         * The first pass.
         */
        PASS_ONE, /**
         * The second pass.
         */
        PASS_TWO
    }

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new uuid dup finder.
     *
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public UuidDupFinder() throws IOException, ClassNotFoundException {
        nidset = Ts.get().getAllConceptNids();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Adds the primordial uuid of the specified
     * <code>component</code> to a list of duplicate uuids, if it is duplicate,
     * otherwise to a list of primordial uuids.
     *
     * @param component the component to check for duplicate uuids
     * @throws IOException signals that an I/O exception has occurred
     */
    private void addToUuidList(ComponentChronicleBI component) throws IOException {
        if (component != null) {
            UUID primUuid = component.getPrimUuid();

            if (allPrimUuids.contains(primUuid)) {
                dupUuids.add(primUuid);
            } else {
                allPrimUuids.add(primUuid);
            }

            for (ComponentChronicleBI annotation : component.getAnnotations()) {
                addToUuidList(annotation);
            }
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
     * Process the components of the specified
     * <code>concept</code> to determine if any of the uuids are duplicates.
     *
     * @param concept the concept to check for duplicates
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processConcept(ConceptChronicleBI concept) throws IOException {

        // add prim uuids to list
        // concept attributes
        addToUuidList(concept.getConceptAttributes());

        // descriptions
        for (DescriptionChronicleBI desc : concept.getDescriptions()) {
            addToUuidList(desc);
        }

        // relationships
        for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
            addToUuidList(rel);
        }

        // media
        for (MediaChronicleBI media : concept.getMedia()) {
            addToUuidList(media);
        }

        if (!concept.isAnnotationStyleRefex()) {
            for (RefexChronicleBI refex : concept.getRefsetMembers()) {
                addToUuidList(refex);
            }
        }
    }

    /**
     * Processes each concept to determine if any of the uuids used also
     * identify another concept or component.
     *
     * @param cNid the nid of the concept to process
     * @param fetcher the fetcher for getting the concept associated with *
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
     * Writes a file, dups.oos, listing the duplicate uuids.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    public void writeDupFile() throws IOException {
        count.set(0);

        FileOutputStream fos = new FileOutputStream(dupsFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        try {
            oos.writeObject(dupUuids);
        } finally {
            oos.close();
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets a list of any duplicate uuids found.
     *
     * @return a list of duplicate uuids
     */
    public ConcurrentSkipListSet<UUID> getDupUuids() {
        return dupUuids;
    }

    /**
     *
     * @return the set of nids to process
     * @throws IOException
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidset;
    }
}
