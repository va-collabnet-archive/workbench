/*
 * Copyright 2014 International Health Terminology Standards Development Organisation.
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

package org.ihtsdo.helper.refex;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.TermAux;

/**
 * Checks to make sure that the members of a given language refset are all in the correct language.
 * Will check the given language and the English language refsets.
 * @author aimeefurber
 */
public class LanguageRefsetChecker implements ProcessUnfetchedConceptDataBI{
    
    private ViewCoordinate vc;
    private ConceptChronicleBI languageRefset;
    private UUID languageRefsetUuid;
    private String lang;

    public LanguageRefsetChecker(ViewCoordinate vc, UUID languageRefsetUuid, String lang) throws IOException {
        this.vc = vc;
        this.languageRefsetUuid = languageRefsetUuid;
        this.lang = lang;
    }
    
    public void setup() throws IOException{
        languageRefset = Ts.get().getConcept(languageRefsetUuid);
        System.out.println("Checking language refset members. If incorrect members are found, " + "\n"+
                "the printed list can be imported into the list view.");
    }
    

    @Override
    public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
        ConceptVersionBI cv = conceptFetcher.fetch(vc);
        if (cv != null)
        {
            for(DescriptionVersionBI d: cv.getDescriptionsActive()){
                Collection<? extends RefexVersionBI<?>> langAnnotations = d.getActiveAnnotations(vc, languageRefset.getNid());
                Collection<? extends RefexVersionBI<?>> usAnnotations = d.getActiveAnnotations(vc, RefsetAux.EN_US_REFEX.getLenient().getNid());
                Collection<? extends RefexVersionBI<?>> gbAnnotations = d.getActiveAnnotations(vc, RefsetAux.EN_GB_REFEX.getLenient().getNid());
                
    //             !d.getLang().equalsIgnoreCase("en") && (usAnnotations.size() > 0) ||
    //                    !d.getLang().equalsIgnoreCase("en") && (gbAnnotations.size() > 0
                
                if((d.getLang().equalsIgnoreCase("en") && (langAnnotations.size() > 0))||
                    (d.getLang().equalsIgnoreCase(lang) && (usAnnotations.size() > 0) )||
                    (d.getLang().equalsIgnoreCase(lang) && (gbAnnotations.size() > 0))){
                    Collection<? extends IdBI> additionalIds = cv.getChronicle().getAdditionalIds();
                    if (additionalIds != null)
                    {
                        for(IdBI id : additionalIds){
                            if (id.getAuthorityNid() == TermAux.SCT_ID_AUTHORITY.getLenient().getNid()) {
                                    String sctId = id.getDenotation().toString();
                                    System.out.println(sctId + "\t" + d.getText());
                            }
                        }
                    }
                    else
                    {
                        //who knows.... this "tool" is really poorly designed.
                    }
                }
            }
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

}
