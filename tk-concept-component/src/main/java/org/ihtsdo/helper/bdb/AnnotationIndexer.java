/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.helper.bdb;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

/**
 *
 * @author kec
 */
public class AnnotationIndexer implements ProcessUnfetchedConceptDataBI {

    private AtomicInteger count = new AtomicInteger(0);
    private AtomicInteger dots = new AtomicInteger(0);
    private final NidBitSetBI nidset;
    private int refexNid;
    private ConceptChronicleBI refexConcept;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates annotation indexer finder.
     *
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public AnnotationIndexer(int refexNid) throws IOException, ClassNotFoundException {
        nidset = Ts.get().getAllConceptNids();
        this.refexNid = refexNid;
        refexConcept = Ts.get().getConcept(refexNid);
    }

   public AnnotationIndexer(UUID... refexUuids) throws IOException, ClassNotFoundException {
        nidset = Ts.get().getAllConceptNids();
        this.refexNid = Ts.get().getNidForUuids(refexUuids);
        refexConcept = Ts.get().getConcept(refexUuids);
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
    private void addToAnnotationIndex(Collection<? extends RefexChronicleBI<?>> annotations) throws IOException {
        
        if (annotations != null) {
            for (RefexChronicleBI annotation: annotations) {
                if (annotation.getRefexNid() == this.refexNid) {
                    refexConcept.addAnnotationIndex(annotation.getNid());
                }
                addToAnnotationIndex(annotation.getAnnotations());
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
        addToAnnotationIndex(concept.getConceptAttributes().getAnnotations());

        // descriptions
        for (DescriptionChronicleBI desc : concept.getDescriptions()) {
            addToAnnotationIndex(desc.getAnnotations());
        }

        // relationships
        for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
            addToAnnotationIndex(rel.getAnnotations());
        }

        // media
        for (MediaChronicleBI media : concept.getMedia()) {
            addToAnnotationIndex(media.getAnnotations());
        }

        if (!concept.isAnnotationStyleRefex()) {
            for (RefexChronicleBI refex : concept.getRefsetMembers()) {
                addToAnnotationIndex(refex.getAnnotations());
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

        if (count.get() % 10000 == 0) {
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
     *
     * @return the set of nids to process
     * @throws IOException
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidset;
    }
    
    public void commit() throws IOException {
        Ts.get().addUncommittedNoChecks(refexConcept);
        Ts.get().commit();
    }
}
