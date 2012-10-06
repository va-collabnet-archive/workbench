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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: Auto-generated Javadoc
/**
 * The Class UuidDupFinder.
 *
 * @author kec
 */
public class UuidDupFinder implements ProcessUnfetchedConceptDataBI {

    /** The count. */
    private AtomicInteger count = new AtomicInteger(0);
    
    /** The dots. */
    private AtomicInteger dots = new AtomicInteger(0);
    
    /** The all prim uuids. */
    ConcurrentSkipListSet<UUID> allPrimUuids = new ConcurrentSkipListSet<UUID>();
    
    /** The dup uuids. */
    ConcurrentSkipListSet<UUID> dupUuids = new ConcurrentSkipListSet<UUID>();
    
    /** The dups file. */
    File dupsFile = new File("dups.oos");
    
    /** The nidset. */
    private final NidBitSetBI nidset;

    //~--- constant enums ------------------------------------------------------
    /**
     * The Enum PASS.
     */
    private enum PASS {

        /** The pass one. */
        PASS_ONE, /** The pass two. */
 PASS_TWO
    }

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new uuid dup finder.
     *
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException the class not found exception
     */
    public UuidDupFinder() throws IOException, ClassNotFoundException {
        nidset = Ts.get().getAllConceptNids();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Adds the to uuid list.
     *
     * @param component the component
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
     * Write dup file.
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
     * Gets the dup uuids.
     *
     * @return the dup uuids
     */
    public ConcurrentSkipListSet<UUID> getDupUuids() {
        return dupUuids;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#getNidSet()
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidset;
    }
}
