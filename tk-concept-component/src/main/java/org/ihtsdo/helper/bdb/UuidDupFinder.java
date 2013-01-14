/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
 *
 * @author kec
 */
public class UuidDupFinder implements ProcessUnfetchedConceptDataBI {

    private AtomicInteger count = new AtomicInteger(0);
    private AtomicInteger dots = new AtomicInteger(0);
    ConcurrentSkipListSet<UUID> allPrimUuids = new ConcurrentSkipListSet<UUID>();
    ConcurrentSkipListSet<UUID> dupUuids = new ConcurrentSkipListSet<UUID>();
    File dupsFile = new File("target/dups_UuidDupFinder.oos");
    private final NidBitSetBI nidset;

    //~--- constant enums ------------------------------------------------------
    private enum PASS {

        PASS_ONE, PASS_TWO
    }

    //~--- constructors --------------------------------------------------------
    public UuidDupFinder() throws IOException, ClassNotFoundException {
        nidset = Ts.get().getAllConceptNids();
    }

    //~--- methods -------------------------------------------------------------
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

    @Override
    public boolean continueWork() {
        return true;
    }

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
    public ConcurrentSkipListSet<UUID> getDupUuids() {
        return dupUuids;
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return nidset;
    }
}
