package org.dwfa.ace.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyRuntimeException;

/**
 * Helper class to obtain sets of statuses (ie. all active or inactive statuses)
 * by taking out the complexity of traversing the hierarchy (which in itself requires allowed statuses!)   
 */
public class StatusHelper {

    private final LineageHelper lineageHelper = new PresetLineageHelper();
    
    private final HashSet<Integer> activeStatusIds = new HashSet<Integer>();
    
    private final HashSet<Integer> inactiveStatusIds = new HashSet<Integer>();
    
    public StatusHelper() {}

    public Set<Integer> getActiveStatuses() {
        
        if (activeStatusIds.isEmpty()) {
            try {
                I_GetConceptData activeConcept = 
                    LocalVersionedTerminology.get().getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());            
                for (I_GetConceptData status : lineageHelper.getAllDescendants(activeConcept)) {
                    activeStatusIds.add(status.getConceptId());
                }
                activeStatusIds.add(activeConcept.getConceptId());
                
            } catch (Exception e) {
                throw new TerminologyRuntimeException("Unable to determine active statuses from hierarchy", e);
            }
        }
        
        return Collections.unmodifiableSet(activeStatusIds);
    }
    
    public Set<Integer> getInactiveStatuses() {
        
        if (inactiveStatusIds.isEmpty()) {
            try {
                I_GetConceptData activeConcept = 
                    LocalVersionedTerminology.get().getConcept(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());            
                for (I_GetConceptData status : lineageHelper.getAllDescendants(activeConcept)) {
                    inactiveStatusIds.add(status.getConceptId());
                }
                inactiveStatusIds.add(activeConcept.getConceptId());
                
            } catch (Exception e) {
                throw new TerminologyRuntimeException("Unable to determine inactive statuses from hierarchy", e);
            }
        }
        
        return Collections.unmodifiableSet(inactiveStatusIds);
    }    
    
    /**
     * Define a helper which does not use an individual (users) profile/preferences
     * to traverse the hierarchy    
     */
    private class PresetLineageHelper extends LineageHelper {
        public PresetLineageHelper() {
            super();
            try {
                // Use ALL view positions
                setViewPositions(new HashSet<I_Position>());
                
                // Use just CURRENT status on relationships
                I_IntSet allowedStatuses = termFactory.newIntSet();
                allowedStatuses.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
                setAllowedStatuses(allowedStatuses);
                
            } catch (Exception e) {
                throw new TerminologyRuntimeException(e);
            } 
        }
    }
}
