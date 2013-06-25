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
package org.ihtsdo.helper.transform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collection;
import java.util.UUID;
import org.ihtsdo.helper.transform.SctIdGenerator.TYPE;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.binding.snomed.TermAux;

/**
 * Creates a mapping of uuids to SCT IDs. One file is created for each type:
 * concept, description, and relationship. The file is made up of sct to uuids
 * mapping that take the following format: A mapping consists of two lines,
 * where the first line contains the sctId and the effective date, and the
 * second contains the uuids associated with the sctId. All lines are tab
 * delimited.
 *
 * <p>This class implements
 * <code>ProcessUnfetchedConceptDataBI</code> and can be "run" using the
 * terminology store method iterateConceptDataInParallel.
 *
 * @see
 * TerminologyStoreDI#iterateConceptDataInParallel(org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI)
 *
 */
public class UuidToSctIdMapper implements ProcessUnfetchedConceptDataBI {

    /**
     * The set of nids representing the concepts associated with the uuids to
     * map.
     */
    NidBitSetBI conceptsToProcess;
    
    private String namespace;
    
    private Writer conceptsWriter;
    
    private Writer descriptionsWriter;
    
    private Writer relationshipsWriter;
    
    private Writer refexWriter;
    
    private String tab = "\t";
   
    private String endOfLine = "\r\n";

    /**
     * Instantiates a new uuid to sct id mapper.
     *
     * @param conceptsToProcess the set of nids associated with the concepts
     * specifying which uuids to map
     * @param namespace the String representing the namespace associated with
     * the SCT IDs
     * @param targetDirectory the target directory for the mapping files
     * @throws IOException signals that an I/O exception has occurred
     */
    public UuidToSctIdMapper(NidBitSetBI conceptsToProcess, String namespace, File targetDirectory) throws IOException {
        this.conceptsToProcess = conceptsToProcess;
        this.namespace = namespace;
        File directory = new File(targetDirectory.getParentFile(), "sct-uuid-maps");
        directory.mkdirs();
        File conceptFile = new File(directory, namespace + "-" + TYPE.CONCEPT + "-" + "sct-map-rw.txt");
        FileOutputStream conceptOs = new FileOutputStream(conceptFile);
        conceptsWriter = new BufferedWriter(new OutputStreamWriter(conceptOs, "UTF8"));
        File descriptionFile = new File(directory, namespace + "-" + TYPE.DESCRIPTION + "-" + "sct-map-rw.txt");
        FileOutputStream descriptionOs = new FileOutputStream(descriptionFile);
        descriptionsWriter = new BufferedWriter(new OutputStreamWriter(descriptionOs, "UTF8"));
        File relationshipFile = new File(directory, namespace + "-" + TYPE.RELATIONSHIP + "-" + "sct-map-rw.txt");
        FileOutputStream relationshipOs = new FileOutputStream(relationshipFile);
        relationshipsWriter = new BufferedWriter(new OutputStreamWriter(relationshipOs, "UTF8"));
        File refexFile = new File(directory, namespace + "-" + TYPE.SUBSET + "-" + "sct-map-rw.txt");
        FileOutputStream refexOs = new FileOutputStream(refexFile);
        refexWriter = new BufferedWriter(new OutputStreamWriter(refexOs, "UTF8"));
    }

    /**
     * Closes the mapping file writers.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    public void close() throws IOException {
        if (conceptsWriter != null) {
            conceptsWriter.close();
        }

        if (descriptionsWriter != null) {
            descriptionsWriter.close();
        }

        if (relationshipsWriter != null) {
            relationshipsWriter.close();
        }

        if (refexWriter != null) {
            refexWriter.close();
        }
    }

    /**
     * Processes each concept, according to the processes method, to determine if the uuids are in the namespace
     * specified for mapping. Generates mapping files and maintains a map in
     * memory of the specified uuids to SCT IDs.
     *
     * @param conceptNid the nid of the concept to process
     * @param conceptFetcher the fetcher for getting the concept associated with
     * * the <code>conceptNid</code> from the database
     * @throws Exception indicates an exception has occurred
     */
    @Override
    public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
        process(conceptFetcher.fetch());
    }

    /**
     *
     * @return the set of nids associated with the concepts indicating which
     * uuids should be mapped
     * @throws IOException signals that an I/O exception has occurred
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return conceptsToProcess;
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
     * Processes each concept to determine if the uuids are in the namespace
     * specified for mapping. Generates mapping files and maintains a map in
     * memory of the specified uuids to SCT IDs.
     *
     * @param conceptChronicle the conceptChronicle containing the uuids to map
     * @throws Exception indicates an exception has occurred
     */
    private void process(ConceptChronicleBI conceptChronicle) throws Exception {
        Collection<IdBI> ids = null;
        ConceptAttributeChronicleBI conceptAttr = conceptChronicle.getConceptAttributes();
        if (conceptAttr != null) {
            UUID conceptAttrUuid = conceptAttr.getPrimUuid();
            ids = (Collection<IdBI>) conceptAttr.getAdditionalIds();
            if (ids != null) {
                for (IdBI id : ids) {
                    if (id.getAuthorityNid() == TermAux.SCT_ID_AUTHORITY.getLenient().getNid()) {
                        String sctId = id.getDenotation().toString();
                        if (sctId.length() > 10 && namespace.equals(getNamespace(sctId))) {
                            conceptsWriter.write(conceptAttrUuid.toString() + endOfLine); //TODO additional UUIDs?
                            conceptsWriter.write(sctId + endOfLine); //TODO effective date?
                        }
                    }
                }
            }
        }
        
        for (DescriptionChronicleBI description : conceptChronicle.getDescriptions()) {
            ids = (Collection<IdBI>) description.getAdditionalIds();
            if (ids != null) {
                for (IdBI id : ids) {
                    if (id.getAuthorityNid() == TermAux.SCT_ID_AUTHORITY.getLenient().getNid()) {
                        String sctId = id.getDenotation().toString();
                        if (sctId.length() > 10 && namespace.equals(getNamespace(sctId))) {
                            descriptionsWriter.write(description.getPrimUuid().toString() + endOfLine); //TODO additional UUIDs?
                            descriptionsWriter.write(sctId + endOfLine); //TODO effective date?
                        }
                    }
                }
            }
        }
        for (RelationshipChronicleBI relationship : conceptChronicle.getRelationshipsOutgoing()) {
            ids = (Collection<IdBI>) relationship.getAdditionalIds();
            if (ids != null) {
                for (IdBI id : ids) {
                    if (id.getAuthorityNid() == TermAux.SCT_ID_AUTHORITY.getLenient().getNid()) {
                        String sctId = id.getDenotation().toString();
                        if (sctId.length() > 10 && namespace.equals(getNamespace(sctId))) {
                            relationshipsWriter.write(relationship.getPrimUuid().toString() + endOfLine); //TODO additional UUIDs?
                            relationshipsWriter.write(sctId + endOfLine); //TODO effective date?
                        }
                    }
                }
            }

        }
    }

    /**
     * Gets the namespace from a String representation of an SCT ID.
     *
     * @param sctId a String representing an SCT ID
     * @return a String representing the namespace associated with the SCT ID
     */
    private String getNamespace(String sctId) {
        int length = sctId.length();
        String namespace = sctId.substring(length - 10, length - 3);
        return namespace;
    }
}
