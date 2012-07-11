package org.ihtsdo.tk.api;

import java.io.IOException;
import org.ihtsdo.tk.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.MediaCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RelationshipCAB;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;

import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

public interface TerminologyBuilderBI {

    /**
     *  
     * @param res
     * @return A <code>RefexChronicleBI</code> if the <code>refexBlueprint</code> 
     * regardless of if the RefexChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RefexChronicleBI<?> construct(RefexCAB refexBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>RefexChronicleBI</code> if the <code>refexBlueprint</code> 
     * regardless of if the RefexChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RefexChronicleBI<?> constructIfNotCurrent(RefexCAB refexBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  
     * @param res
     * @return A <code>RelationshipChronicleBI</code> if the <code>relBlueprint</code> 
     * regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RelationshipChronicleBI construct(RelationshipCAB relBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>RelationshipChronicleBI</code> if the 
     * <code>relBlueprint</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RelationshipChronicleBI constructIfNotCurrent(RelationshipCAB relBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  
     * @param res
     * @return A <code>DescriptionChronicleBI</code> if the <code>descriptionBlueprint</code> 
     * regardless of if the DescriptionChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    DescriptionChronicleBI construct(DescriptionCAB descriptionBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>DescriptionChronicleBI</code> if the
     * <code>descriptionBlueprint</code> regardless of if the DescriptionChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    DescriptionChronicleBI constructIfNotCurrent(DescriptionCAB descriptionBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  
     * @param res
     * @return A <code>MediaChronicleBI</code> if the <code>mediaBlueprint</code> 
     * regardless of if the MediaChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    MediaChronicleBI construct(MediaCAB mediaBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>MediaChronicleBI</code> if the <code>mediaBlueprint</code> 
     *         regardless of if the MediaChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    MediaChronicleBI constructIfNotCurrent(MediaCAB mediaBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  
     * @param res
     * @return A <code>ConceptChronicleBI</code> if the <code>conceptBlueprint</code> 
     *          regardless of if the ConceptChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    ConceptChronicleBI construct(ConceptCB conceptBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  This method incurs an extra cost to determine if a current version 
     *  already meets the specification. 
     * @param res
     * @return A <code>ConceptChronicleBI</code> if the <code>conceptBlueprint</code> 
     *          regardless of if the ConceptChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    ConceptChronicleBI constructIfNotCurrent(ConceptCB conceptBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  
     * @param res
     * @return A <code>ConceptAttributeChronicleBI</code> if the <code>conceptAttributeBlueprint</code> regardless of if the ConceptAttributeChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    ConceptAttributeChronicleBI construct(ConceptAttributeAB conceptAttributeBlueprint) throws IOException, InvalidCAB, ContradictionException;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>ConceptAttributeChronicleBI</code> if the <code>conceptAttributeBlueprint</code> regardless of if the ConceptAttributeChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    ConceptAttributeChronicleBI constructIfNotCurrent(ConceptAttributeAB conceptAttributeBlueprint) throws IOException, InvalidCAB, ContradictionException;
    
    EditCoordinate getEditCoordinate();
}
