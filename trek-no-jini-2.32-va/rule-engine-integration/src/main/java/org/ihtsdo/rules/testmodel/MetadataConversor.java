/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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

package org.ihtsdo.rules.testmodel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ValidationException;

/**
 *
 * @author alo
 */
public class MetadataConversor {
    
    private Map<Integer,Integer> rf2Torf1;
    private Map<Integer,Integer> rf1Torf2;

    public MetadataConversor() {
        try {
            rf1Torf2 = new HashMap<Integer,Integer>();
            rf2Torf1 = new HashMap<Integer,Integer>();
            
            rf2Torf1.put(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.HISTORICAL_RELATIONSSHIP_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.HISTORICAL_CHARACTERISTIC_TYPE_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.HISTORICAL_CHARACTERISTIC_TYPE_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.HISTORICAL_RELATIONSSHIP_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.QUALIFIER_CHARACTERISTICS_TYPE_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.QUALIFIER_CHARACTERISTICS_TYPE_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getNid(),
                    SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.ACCEPTABLE_DESCRIPTION_TYPE_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.ACCEPTABLE_DESCRIPTION_TYPE_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid());
            
            rf2Torf1.put(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid(), 
                    SnomedMetadataRf1.PREFERRED_ACCEPTABILITY_RF1.getLenient().getNid());
            rf1Torf2.put(SnomedMetadataRf1.PREFERRED_ACCEPTABILITY_RF1.getLenient().getNid(),
                    SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
            
        } catch (ValidationException ex) {
            Logger.getLogger(MetadataConversor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MetadataConversor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Integer getRf2Value(Integer rf1Value) {
        if (rf1Torf2.containsKey(rf1Value)) {
            return rf1Torf2.get(rf1Value);
        } else {
            return rf1Value;
        }
    }
    
    public Integer getRf1Value(Integer rf2Value) {
        if (rf2Torf1.containsKey(rf2Value)) {
            return rf2Torf1.get(rf2Value);
        } else {
            return rf2Value;
        }
    }
    

}
