package org.ihtsdo.tk.api.blueprint;

import java.io.IOException;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;

import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

public interface TerminologyAmendmentBI {

    /**
     *  
     * @param res
     * @return A <code>RefexChronicleBI</code> if the <code>spec</code> regardless of if the Refex was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RefexChronicleBI<?> amend(RefexCUB spec) throws IOException, InvalidCUB;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>RefexChronicleBI</code> if the <code>spec</code> regardless of if the Refex was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RefexChronicleBI<?> amendIfNotCurrent(RefexCUB spec) throws IOException, InvalidCUB;

    /**
     *  
     * @param res
     * @return A <code>RelationshipChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RelationshipChronicleBI amend(RelCUB spec) throws IOException, InvalidCUB;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>RelationshipChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RelationshipChronicleBI amendIfNotCurrent(RelCUB spec) throws IOException, InvalidCUB;

    /**
     *  
     * @param res
     * @return A <code>DescriptionChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    DescriptionChronicleBI amend(DescCUB spec) throws IOException, InvalidCUB;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>DescriptionChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    DescriptionChronicleBI amendIfNotCurrent(DescCUB spec) throws IOException, InvalidCUB;

    /**
     *  
     * @param res
     * @return A <code>MediaChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    MediaChronicleBI amend(MediaCUB spec) throws IOException, InvalidCUB;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>MediaChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    MediaChronicleBI amendIfNotCurrent(MediaCUB spec) throws IOException, InvalidCUB;
}
