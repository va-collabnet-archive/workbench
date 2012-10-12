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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

// TODO: Auto-generated Javadoc
//TODO-javadoc: skipped private methods/variables
/**
 * The Class NullComponentFinde processes every concept in the database to
 * determine if each component has valid identifiers. This class implements
 * <code>ProcessUnfetchedConceptDataBI</code> and can
 * be "run" using the terminology store method iterateConceptDataInParallel.
 *
 * @see
 * TerminologyStoreDI#iterateConceptDataInParallel(org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI)
 *
 */
public class NullComponentFinder implements ProcessUnfetchedConceptDataBI {

    /**
     * The count.
     */
    private AtomicInteger count = new AtomicInteger(0);
    /**
     * The dots.
     */
    private AtomicInteger dots = new AtomicInteger(0);
    /**
     * The list of nids to be processed.
     */
    ConcurrentSkipListSet<Integer> allPrimNids = new ConcurrentSkipListSet<Integer>();
    /**
     * The the list to hold any nids found to be null components.
     */
    ConcurrentSkipListSet<Integer> nullComponent = new ConcurrentSkipListSet<Integer>();
    /**
     * The output file: nullComponent.oos.
     */
    File nullComponentFile = new File("nullComponent.oos");
    /**
     * The nidset.
     */
    private final NidBitSetBI nidset;

    //~--- constant enums ------------------------------------------------------
    /**
     * The Enum PASS.
     */
    private enum PASS {

        /**
         * The pass one.
         */
        PASS_ONE, /**
         * The pass two.
         */
        PASS_TWO
    }

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new null component finder.
     *
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public NullComponentFinder() throws IOException, ClassNotFoundException {
        nidset = Ts.get().getAllConceptNids();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Verify component.
     *
     * @param component the component
     * @throws IOException signals that an I/O exception has occurred
     */
    private void verifyComponent(ComponentChronicleBI component) throws IOException {
        if (component != null) {
            if (component instanceof ConceptAttributeChronicleBI) {
                ConceptAttributeChronicleBI attr = (ConceptAttributeChronicleBI) component;
                for (ConceptAttributeVersionBI loopVersion : attr.getVersions()) {
                    verifyNids(loopVersion.getAllNidsForVersion(), component);
                }
            } else if (component instanceof DescriptionChronicleBI) {
                DescriptionChronicleBI desc = (DescriptionChronicleBI) component;
                for (DescriptionVersionBI loopVersion : desc.getVersions()) {
                    verifyNids(loopVersion.getAllNidsForVersion(), component);
                }
            } else if (component instanceof RelationshipChronicleBI) {
                RelationshipChronicleBI rel = (RelationshipChronicleBI) component;
                for (RelationshipVersionBI loopVersion : rel.getVersions()) {
                    verifyNids(loopVersion.getAllNidsForVersion(), component);
                }
            } else if (component instanceof MediaChronicleBI) {
                MediaChronicleBI media = (MediaChronicleBI) component;
                for (MediaVersionBI loopVersion : media.getVersions()) {
                    verifyNids(loopVersion.getAllNidsForVersion(), component);
                }
            } else if (component instanceof RefexChronicleBI) {
                RefexChronicleBI refex = (RefexChronicleBI) component;
                for (Object loopVersion : refex.getVersions()) {
                    RefexVersionBI loopRefversion = (RefexVersionBI) loopVersion;
                    verifyNids(loopRefversion.getAllNidsForVersion(), component);
                }
            }

            for (ComponentChronicleBI annotation : component.getAnnotations()) {
                verifyComponent(annotation);
            }
        }
    }

    /**
     * Verify nids.
     *
     * @param nids the nids
     * @param component the component
     */
    private void verifyNids(Set<Integer> nids, ComponentChronicleBI component) {
        for (Integer nid : nids) {
            ComponentChronicleBI<?> referencedComponent = null;
            try {
                referencedComponent = Ts.get().getComponent(nid);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (referencedComponent == null) {
                try {
                    System.out.println("No component for nid: " + nid
                            + " " + Ts.get().getUuidsForNid(nid)
                            + ". Used in component:" + component);
                    nullComponent.add(nid);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
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
     * Process concept.
     *
     * @param concept the concept
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processConcept(ConceptChronicleBI concept) throws IOException {

        // add prim uuids to list
        // concept attributtes
        verifyComponent(concept.getConceptAttributes());

        // descriptions
        for (DescriptionChronicleBI desc : concept.getDescriptions()) {
            verifyComponent(desc);
        }

        // relationships
        for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
            verifyComponent(rel);
        }

        // media
        for (MediaChronicleBI media : concept.getMedia()) {
            verifyComponent(media);
        }

        if (!concept.isAnnotationStyleRefex()) {
            for (RefexChronicleBI refex : concept.getRefsetMembers()) {
                verifyComponent(refex);
            }
        }
    }

    /**
     * Processes each concept to determine if the associated nids are valid.
     * @param cNid the nid of the concept to process
     * @param fetcher the fetcher for getting the concept associated
     * with the <code>cNid</code> from the database
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
     * Writes a file, nullComponent.oos, listing the null components.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    public void writeNullComponentFile() throws IOException { //TODO: implement for null components
        count.set(0);

        FileOutputStream fos = new FileOutputStream(nullComponentFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        try {
            oos.writeObject(nullComponent);
        } finally {
            oos.close();
        }
    }

    //~--- get methods ---------------------------------------------------------    
    /**
     * Gets a list of any null component nids found.
     *
     * @return a list of null component nids
     */
    public ConcurrentSkipListSet<Integer> getNullComponent() {
        return nullComponent;
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
