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

// TODO: Auto-generated Javadoc
/**
 * Creates a mapping of sctIds to uuids. One file is created for each type:
 * concept, description, and relationship. The file takes the following format. A mapping consists of two lines
 * the first line contains the sctId and the effective date; the second contains the uuids associated with the sctId.
 * All lines are tab delimited.
 * @author akf
 */
public class UuidToSctIdMapper implements ProcessUnfetchedConceptDataBI {

    /** The concepts to process. */
    NidBitSetBI conceptsToProcess;
    
    /** The namespace. */
    String namespace;
    
    /** The concepts writer. */
    Writer conceptsWriter;
    
    /** The descriptions writer. */
    Writer descriptionsWriter;
    
    /** The relationships writer. */
    Writer relationshipsWriter;
    
    /** The refex writer. */
    Writer refexWriter;
    
    /** The tab. */
    String tab = "\t";
    
    /** The end of line. */
    String endOfLine = "\r\n";
    
    /**
     * Instantiates a new uuid to sct id mapper.
     *
     * @param conceptsToProcess the concepts to process
     * @param namespace the namespace
     * @param sourceDirectory the source directory
     * @throws IOException signals that an I/O exception has occurred.
     */
    public UuidToSctIdMapper(NidBitSetBI conceptsToProcess, String namespace, File sourceDirectory) throws IOException {
        this.conceptsToProcess = conceptsToProcess;
        this.namespace = namespace;
        File directory = new File(sourceDirectory.getParentFile(), "sct-uuid-maps");
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
     * Close.
     *
     * @throws IOException signals that an I/O exception has occurred.
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#processUnfetchedConceptData(int, org.ihtsdo.tk.api.ConceptFetcherBI)
     */
    @Override
    public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
        process(conceptFetcher.fetch());
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#getNidSet()
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return conceptsToProcess;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ContinuationTrackerBI#continueWork()
     */
    @Override
    public boolean continueWork() {
        return true;
    }
    
    /**
     * Process.
     *
     * @param c the c
     * @throws Exception the exception
     */
    public void process(ConceptChronicleBI c) throws Exception {
        ConceptAttributeChronicleBI conceptAttr = c.getConceptAttributes();
        UUID conceptAttrUuid = conceptAttr.getPrimUuid();
        Collection<IdBI> ids = (Collection<IdBI>) conceptAttr.getAdditionalIds();
        if (ids != null) {
            for (IdBI id : ids) {
                if (id.getAuthorityNid() == TermAux.SCT_ID_AUTHORITY.getLenient().getNid()) {
                    String sctId = id.getDenotation().toString();
                    if(sctId.length() > 10 && namespace.equals(getNamespace(sctId))){
                        conceptsWriter.write(conceptAttrUuid.toString() + endOfLine); //TODO additional UUIDs?
                        conceptsWriter.write(sctId + endOfLine); //TODO effective date?
                    }
                }
            }
        }
        for(DescriptionChronicleBI description : c.getDescriptions()){
            ids = (Collection<IdBI>) description.getAdditionalIds();
            if (ids != null) {
                for (IdBI id : ids) {
                    if (id.getAuthorityNid() == TermAux.SCT_ID_AUTHORITY.getLenient().getNid()) {
                        String sctId = id.getDenotation().toString();
                        if(sctId.length() > 10 && namespace.equals(getNamespace(sctId))){
                            descriptionsWriter.write(conceptAttrUuid.toString() + endOfLine); //TODO additional UUIDs?
                            descriptionsWriter.write(sctId + endOfLine); //TODO effective date?
                        }
                    }
                }
            }
        }
        for(RelationshipChronicleBI relationship : c.getRelationshipsSource()){
            ids = (Collection<IdBI>) relationship.getAdditionalIds();
            if (ids != null) {
                for (IdBI id : ids) {
                    if (id.getAuthorityNid() == TermAux.SCT_ID_AUTHORITY.getLenient().getNid()) {
                        String sctId = id.getDenotation().toString();
                        if(sctId.length() > 10 && namespace.equals(getNamespace(sctId))){
                            relationshipsWriter.write(conceptAttrUuid.toString() + endOfLine); //TODO additional UUIDs?
                            relationshipsWriter.write(sctId + endOfLine); //TODO effective date?
                        }
                    }
                }
            }
            
        }
    }
    
    /**
     * Gets the namespace.
     *
     * @param sctId the sct id
     * @return the namespace
     */
    private String getNamespace(String sctId){
        int length = sctId.length();
        String namespace = sctId.substring(length - 10, length - 3);
        return namespace;
    }
    
}
