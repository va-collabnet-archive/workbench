/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.id.IdBI;

/**
 * Writes out a "Concept List" which is a tab delimited list of concepts
 * containing
 * the concepts identifier and all non-English descriptions. The identifier will be the
 * concept's
 * SNOMED CT ID if it has one, or its UUID if it does not.
 * 
 * @author aimeefurber
 * 
 */
public class FilteredDescriptionListWriter extends GenericFileWriter<ConceptVersionBI> {

    private Integer snomedIntId;

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.file.GenericFileWriter#serialize(java.lang.Object)
     */
    @Override
    protected String serialize(ConceptVersionBI concept) throws IOException, TerminologyException {

        try {
            if (snomedIntId == null) {
                snomedIntId = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());
            }
            Object conceptId = null;
            List<IdBI> additionalIDList = (List<IdBI>) concept.getAdditionalIds();
            if (additionalIDList != null) {
            for(IdBI id : concept.getAdditionalIds()){
                if(id.getAuthorityNid() == snomedIntId){
                    conceptId = id.getDenotation();
                }
            }
            }
            if (conceptId == null) {
                conceptId = concept.getPrimUuid();
            }
            
            DescriptionVersionBI fsn = concept.getDescriptionFullySpecified();
            Collection<? extends DescriptionVersionBI> preferred = concept.getDescriptionsPreferredActive();
            Collection<DescriptionVersionBI> preferredNonEN = new ArrayList<DescriptionVersionBI>();
            Collection<? extends DescriptionVersionBI> synonyms = concept.getDescriptionsActive();
            Collection<DescriptionVersionBI> synonymsNonEN =  new ArrayList<DescriptionVersionBI>();
            synonyms.remove(fsn);
            synonyms.removeAll(preferred);
            
            StringBuilder sb = new StringBuilder();
            sb.append(conceptId + "\t");
            if(!fsn.getLang().equals("en")){
                sb.append(fsn.getText() + "\t");
            }
            for (DescriptionVersionBI p : preferred) {
                if(!p.getLang().equals("en")) {
                    sb.append(p.getText() + "\t");
                }
            }
            for (DescriptionVersionBI s : synonyms) {
                if (!s.getLang().equals("en")) {
                    sb.append(s.getText() + "\t");
                }
            }
            return sb.toString();
        } catch (ContradictionException ex) {
            throw new TerminologyException(ex);
        }
    }
}
