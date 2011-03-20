package org.ihtsdo.tk.api;

import java.io.IOException;
import org.ihtsdo.tk.api.blueprint.DescCUB;
import org.ihtsdo.tk.api.blueprint.InvalidCUB;
import org.ihtsdo.tk.api.blueprint.MediaCUB;
import org.ihtsdo.tk.api.blueprint.RefexCUB;
import org.ihtsdo.tk.api.blueprint.RelCUB;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;

import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

public interface TerminologyConstructorBI {

    /**
     *  
     * @param res
     * @return A <code>RefexChronicleBI</code> if the <code>spec</code> regardless of if the Refex was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RefexChronicleBI<?> construct(RefexCUB spec) throws IOException, InvalidCUB;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>RefexChronicleBI</code> if the <code>spec</code> regardless of if the Refex was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RefexChronicleBI<?> constructIfNotCurrent(RefexCUB spec) throws IOException, InvalidCUB;

    /**
     *  
     * @param res
     * @return A <code>RelationshipChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RelationshipChronicleBI construct(RelCUB spec) throws IOException, InvalidCUB;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>RelationshipChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    RelationshipChronicleBI constructIfNotCurrent(RelCUB spec) throws IOException, InvalidCUB;

    /**
     *  
     * @param res
     * @return A <code>DescriptionChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    DescriptionChronicleBI construct(DescCUB spec) throws IOException, InvalidCUB;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>DescriptionChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    DescriptionChronicleBI constructIfNotCurrent(DescCUB spec) throws IOException, InvalidCUB;

    /**
     *  
     * @param res
     * @return A <code>MediaChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    MediaChronicleBI construct(MediaCUB spec) throws IOException, InvalidCUB;

    /**
     *  This method incurs an extra cost to determine if a current version already meets the specification. 
     * @param res
     * @return A <code>MediaChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
     * @throws IOException
     * @throws InvalidAmendmentSpec
     */
    MediaChronicleBI constructIfNotCurrent(MediaCUB spec) throws IOException, InvalidCUB;
}
