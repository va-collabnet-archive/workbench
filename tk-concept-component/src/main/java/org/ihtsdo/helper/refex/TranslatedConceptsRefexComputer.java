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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 *
 * @author aimeefurber
 */
public class TranslatedConceptsRefexComputer implements ProcessUnfetchedConceptDataBI{
    
    private EditCoordinate ec;
    private ViewCoordinate vc;
    private String tempKey;
    private File output;
    private String lang;
    private ConceptVersionBI transRefset;
    private TerminologyBuilderBI builder;

    public TranslatedConceptsRefexComputer(EditCoordinate ec, ViewCoordinate vc, File output,
            String lang, UUID transRefset) throws IOException {
        this.ec = ec;
        this.vc = vc;
        this.output = output;
        this.lang = lang;
        this.transRefset = Ts.get().getConceptVersion(vc,transRefset);
    }
    
    public void setup() throws IOException{
        tempKey = UUID.randomUUID().toString();
        String changsetFileName = Ts.get().getConceptForNid(ec.getAuthorNid()).toUserString() + "#transRefset#"
                + UUID.randomUUID().toString() + ".eccs";
        ChangeSetGeneratorBI generator =
                Ts.get().createDtoChangeSetGenerator(new File(output, changsetFileName), new File(output,
                "#0#" + changsetFileName), ChangeSetGenerationPolicy.MUTABLE_ONLY);
        Ts.get().addChangeSetGenerator(tempKey, generator);
        builder = Ts.get().getTerminologyBuilder(ec, vc);
    }
    
    public void cleanup() throws IOException{
        Ts.get().addUncommitted(transRefset);
        Ts.get().commit(transRefset);
        Ts.get().removeChangeSetGenerator(tempKey);
    }

    @Override
    public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
        ConceptVersionBI cv = conceptFetcher.fetch(vc);
        boolean isMember = cv.isMember(transRefset.getNid());
        boolean noActiveDescriptions = true;
        for(DescriptionVersionBI d: cv.getDescriptionsActive()){
            if(d.getLang().equalsIgnoreCase(lang)){
                noActiveDescriptions = false;
                if(!isMember){
                    addTranslationMember(cv);
                }
            }
        }
        if(isMember && noActiveDescriptions){
            retireTranslationMember(cv);
        }
    }
    
    private void addTranslationMember(ConceptVersionBI concept) throws IOException, InvalidCAB, ContradictionException{
        RefexCAB refsetbp = new RefexCAB(TK_REFEX_TYPE.CID, concept.getConceptNid(), transRefset.getNid());
        refsetbp.put(RefexCAB.RefexProperty.CNID1, RefsetAux.NORMAL_MEMBER.getLenient().getNid());
        RefexChronicleBI<?> member = builder.construct(refsetbp);
    }
    
    private void retireTranslationMember(ConceptVersionBI concept) throws IOException{
        Collection<? extends RefexVersionBI<?>> refexMembersActive = concept.getRefexMembersActive(transRefset.getNid());
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
