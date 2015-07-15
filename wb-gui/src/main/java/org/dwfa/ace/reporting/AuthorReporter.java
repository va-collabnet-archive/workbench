/*
 * Copyright 2015 International Health Terminology Standards Development Organisation.
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
package org.dwfa.ace.reporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.dwfa.ace.log.AceLog;
import static org.dwfa.ace.log.AceLog.getAppLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 *
 * @author aimeefurber
 */
public class AuthorReporter implements ProcessUnfetchedConceptDataBI {

    int authorNid;
    File resultsDir;
    ViewCoordinate vc;
    ConcurrentSkipListSet<String> resultsText = new ConcurrentSkipListSet<>();
    ConcurrentSkipListSet<String> resultsUuid = new ConcurrentSkipListSet<>(); //concept then component uuid, tab delimited
    ConcurrentSkipListSet<String> listFormat = new ConcurrentSkipListSet<>(); //file of concepts that can be imported into the list view

    public AuthorReporter(int authorNid, File resultsDir, ViewCoordinate vc) {
        this.authorNid = authorNid;
        this.resultsDir = resultsDir;
        this.vc = vc;
    }

    @Override
    public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
        boolean edited = false;
        ConceptChronicleBI concept = conceptFetcher.fetch();

        for (ConceptAttributeVersionBI version : concept.getConceptAttributes().getVersions()) {
            if (version.getAuthorNid() == authorNid) {
                resultsText.add(version.toString() + "\t\n");
                resultsUuid.add(Ts.get().getUuidPrimordialForNid(version.getConceptNid()) + "\t" + version.getPrimUuid().toString() + "\t\n");
                for (RefexChronicleBI refexChronicle : version.getAnnotations()) {
                    for (Object o : refexChronicle.getVersions()) {
                        RefexVersionBI refexVersion = (RefexVersionBI) o;
                        if (refexVersion.getAuthorNid() == authorNid) {
                            edited = true;
                            resultsText.add(refexVersion.toString());
                            resultsUuid.add(Ts.get().getUuidPrimordialForNid(refexVersion.getConceptNid()) + "\t" + refexVersion.getPrimUuid().toString());
                        }
                    }
                }
            }
        }

        for (DescriptionChronicleBI chronicle : concept.getDescriptions()) {
            for (DescriptionVersionBI version : chronicle.getVersions()) {
                if (version.getAuthorNid() == authorNid) {
                    edited = true;
                    resultsText.add(version.toString());
                    resultsUuid.add(Ts.get().getUuidPrimordialForNid(version.getConceptNid()) + "\t" + version.getPrimUuid().toString());
                }
                for (RefexChronicleBI refexChronicle : version.getAnnotations()) {
                    for (Object o : refexChronicle.getVersions()) {
                        RefexVersionBI refexVersion = (RefexVersionBI) o;
                        if (refexVersion.getAuthorNid() == authorNid) {
                            edited = true;
                            resultsText.add(refexVersion.toString());
                            resultsUuid.add(Ts.get().getUuidPrimordialForNid(refexVersion.getConceptNid()) + "\t" + refexVersion.getPrimUuid().toString());
                        }
                    }
                }
            }
        }
        for (RelationshipChronicleBI chronicle : concept.getRelationshipsOutgoing()) {
            for (RelationshipVersionBI version : chronicle.getVersions()) {
                if (version.getAuthorNid() == authorNid) {
                    edited = true;
                    resultsText.add(version.toString());
                    resultsUuid.add(Ts.get().getUuidPrimordialForNid(version.getConceptNid()) + "\t" + version.getPrimUuid().toString());
                }
                for (RefexChronicleBI refexChronicle : version.getAnnotations()) {
                    for (Object o : refexChronicle.getVersions()) {
                        RefexVersionBI refexVersion = (RefexVersionBI) o;
                        if (refexVersion.getAuthorNid() == authorNid) {
                            edited = true;
                            resultsText.add(refexVersion.toString());
                            resultsUuid.add(Ts.get().getUuidPrimordialForNid(refexVersion.getConceptNid()) + "\t" + refexVersion.getPrimUuid().toString());
                        }
                    }
                }
            }
        }
        for (RefexChronicleBI refexChronicle : concept.getRefexes()) {
            for (Object o : refexChronicle.getVersions()) {
                RefexVersionBI refexVersion = (RefexVersionBI) o;
                if (refexVersion.getAuthorNid() == authorNid) {
                    edited = true;
                    resultsText.add(refexVersion.toString());
                    resultsUuid.add(Ts.get().getUuidPrimordialForNid(refexVersion.getConceptNid()) + "\t" + refexVersion.getPrimUuid().toString());
                }
            }
        }
        for (RefexChronicleBI refexChronicle : concept.getRefsetMembers()) {
            for (Object o : refexChronicle.getVersions()) {
                RefexVersionBI refexVersion = (RefexVersionBI) o;
                if (refexVersion.getAuthorNid() == authorNid) {
                    edited = true;
                    resultsText.add(refexVersion.toString());
                    resultsUuid.add(Ts.get().getUuidPrimordialForNid(refexVersion.getConceptNid()) + "\t" + refexVersion.getPrimUuid().toString());
                }
            }
        }
        if(edited){
            int snomedIntId = Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());
            Object conceptId = null;
            Collection<? extends IdBI> additionalIds = concept.getAdditionalIds();
            if (additionalIds != null) {
                for (IdBI id : concept.getAdditionalIds()) {
                    if (id.getAuthorityNid() == snomedIntId) {
                        conceptId = id.getDenotation();
                    }
                }
            }
            //No SCT ID, so use UUID
            if (additionalIds == null || conceptId == null) {
                conceptId = concept.getPrimUuid();
            }
            listFormat.add(concept.getVersion(vc).getDescriptionPreferred().getText() + "\t" + conceptId.toString());
        }
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return Ts.get().getAllConceptNids();
    }

    @Override
    public boolean continueWork() {
        return true;
    }

    public void report() throws IOException, FileNotFoundException, ContradictionException{
        String author = Ts.get().getConceptVersion(vc, authorNid).getDescriptionFullySpecified().getText();
        author = author.replace(" ", "-");
        
        ArrayList<String> uuidResults = new ArrayList<>();
        ArrayList<String> textResults = new ArrayList<>();
        
        uuidResults.addAll(resultsUuid);
        textResults.addAll(resultsText);
        
        Collections.sort(uuidResults);
        Collections.sort(textResults);
        
        FileOutputStream textStream = new FileOutputStream(new File(resultsDir, author + "-textResults.txt"));
        BufferedWriter textWriter = new BufferedWriter(new OutputStreamWriter(textStream, "UTF8"));
        for(String result : textResults){
            textWriter.write(result + "\t\n");
        }
        textWriter.close();
        
        FileOutputStream uuidStream = new FileOutputStream(new File(resultsDir, author + "-uuidResults.txt"));
        BufferedWriter uuidWriter = new BufferedWriter(new OutputStreamWriter(uuidStream, "UTF8"));
        for(String result : uuidResults){
            uuidWriter.write(result + "\t\n");
        }
        uuidWriter.close();
        
        FileOutputStream listStream = new FileOutputStream(new File(resultsDir, author + "-listFormat.txt"));
        BufferedWriter listWriter = new BufferedWriter(new OutputStreamWriter(listStream, "UTF8"));
        for(String result : listFormat){
            String[] parts = result.split("\t");
            listWriter.write(parts[1] + "\t" + parts[0] + "\t\n");
        }
        listWriter.close();
        AceLog.getAppLog().info("Done writing reports.");
    }
}
