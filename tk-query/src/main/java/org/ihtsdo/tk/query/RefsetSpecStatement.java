/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.query;

import java.io.IOException;
import java.util.Collection;
import org.dwfa.cement.RefsetAuxiliary.Concept;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.query.RefsetSpecQuery.GROUPING_TYPE;

/**
 * Represents partial information contained in a refset spec. An example of a
 * statement is : "NOT: Concept is : Paracetamol"
 *
 * @author Chrissy Hill
 *
 */
public abstract class RefsetSpecStatement extends RefsetSpecComponent {

    protected enum QUERY_TOKENS {

        CONCEPT_IS(Concept.CONCEPT_IS),
        CONCEPT_IS_CHILD_OF(Concept.CONCEPT_IS_CHILD_OF),
        CONCEPT_IS_KIND_OF(Concept.CONCEPT_IS_KIND_OF),
        CONCEPT_IS_DESCENDENT_OF(Concept.CONCEPT_IS_DESCENDENT_OF),
        CONCEPT_IS_MEMBER_OF(Concept.CONCEPT_IS_MEMBER_OF),
        CONCEPT_STATUS_IS(Concept.CONCEPT_STATUS_IS),
        CONCEPT_STATUS_IS_CHILD_OF(Concept.CONCEPT_STATUS_IS_CHILD_OF),
        CONCEPT_STATUS_IS_KIND_OF(Concept.CONCEPT_STATUS_IS_KIND_OF),
        CONCEPT_STATUS_IS_DESCENDENT_OF(Concept.CONCEPT_STATUS_IS_DESCENDENT_OF),
        DESC_IS(Concept.DESC_IS),
        DESC_IS_MEMBER_OF(Concept.DESC_IS_MEMBER_OF),
        DESC_STATUS_IS(Concept.DESC_STATUS_IS),
        DESC_STATUS_IS_CHILD_OF(Concept.DESC_STATUS_IS_CHILD_OF),
        DESC_STATUS_IS_KIND_OF(Concept.DESC_STATUS_IS_KIND_OF),
        DESC_STATUS_IS_DESCENDENT_OF(Concept.DESC_STATUS_IS_DESCENDENT_OF),
        DESC_TYPE_IS(Concept.DESC_TYPE_IS),
        DESC_TYPE_IS_CHILD_OF(Concept.DESC_TYPE_IS_CHILD_OF),
        DESC_TYPE_IS_KIND_OF(Concept.DESC_TYPE_IS_KIND_OF),
        DESC_TYPE_IS_DESCENDENT_OF(Concept.DESC_TYPE_IS_DESCENDENT_OF),
        DESC_REGEX_MATCH(Concept.DESC_REGEX_MATCH),
        DESC_LUCENE_MATCH(Concept.DESC_LUCENE_MATCH),
        REL_IS(Concept.REL_IS),
        REL_RESTRICTION_IS(Concept.REL_IS_MEMBER_OF),
        REL_IS_MEMBER_OF(Concept.REL_IS_MEMBER_OF),
        REL_STATUS_IS(Concept.REL_STATUS_IS),
        REL_STATUS_IS_KIND_OF(Concept.REL_STATUS_IS_KIND_OF),
        REL_STATUS_IS_CHILD_OF(Concept.REL_STATUS_IS_CHILD_OF),
        REL_STATUS_IS_DESCENDENT_OF(Concept.REL_STATUS_IS_DESCENDENT_OF),
        REL_TYPE_IS(Concept.REL_TYPE_IS),
        REL_TYPE_IS_KIND_OF(Concept.REL_TYPE_IS_KIND_OF),
        REL_TYPE_IS_CHILD_OF(Concept.REL_TYPE_IS_CHILD_OF),
        REL_TYPE_IS_DESCENDENT_OF(Concept.REL_TYPE_IS_DESCENDENT_OF),
        REL_LOGICAL_QUANTIFIER_IS(Concept.REL_LOGICAL_QUANTIFIER_IS),
        REL_LOGICAL_QUANTIFIER_IS_KIND_OF(Concept.REL_LOGICAL_QUANTIFIER_IS_KIND_OF),
        REL_LOGICAL_QUANTIFIER_IS_CHILD_OF(Concept.REL_LOGICAL_QUANTIFIER_IS_CHILD_OF),
        REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF(Concept.REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF),
        REL_CHARACTERISTIC_IS(Concept.REL_CHARACTERISTIC_IS),
        REL_CHARACTERISTIC_IS_KIND_OF(Concept.REL_CHARACTERISTIC_IS_KIND_OF),
        REL_CHARACTERISTIC_IS_CHILD_OF(Concept.REL_CHARACTERISTIC_IS_CHILD_OF),
        REL_CHARACTERISTIC_IS_DESCENDENT_OF(Concept.REL_CHARACTERISTIC_IS_DESCENDENT_OF),
        REL_REFINABILITY_IS(Concept.REL_REFINABILITY_IS),
        REL_REFINABILITY_IS_KIND_OF(Concept.REL_REFINABILITY_IS_KIND_OF),
        REL_REFINABILITY_IS_CHILD_OF(Concept.REL_REFINABILITY_IS_CHILD_OF),
        REL_REFINABILITY_IS_DESCENDENT_OF(Concept.REL_REFINABILITY_IS_DESCENDENT_OF),
        REL_DESTINATION_IS(Concept.REL_DESTINATION_IS),
        REL_DESTINATION_IS_KIND_OF(Concept.REL_DESTINATION_IS_KIND_OF),
        REL_DESTINATION_IS_CHILD_OF(Concept.REL_DESTINATION_IS_CHILD_OF),
        REL_DESTINATION_IS_DESCENDENT_OF(Concept.REL_DESTINATION_IS_DESCENDENT_OF),
        V1_IS(Concept.DIFFERENCE_V1_IS),
        V2_IS(Concept.DIFFERENCE_V2_IS),
        ADDED_CONCEPT(Concept.ADDED_CONCEPT),
        ADDED_DESCRIPTION(Concept.ADDED_DESCRIPTION),
        ADDED_RELATIONSHIP(Concept.ADDED_RELATIONSHIP),
        CHANGED_CONCEPT_STATUS(Concept.CHANGED_CONCEPT_STATUS),
        CHANGED_CONCEPT_DEFINED(Concept.CHANGED_DEFINED),
        CHANGED_DESCRIPTION_CASE(Concept.CHANGED_DESCRIPTION_CASE),
        CHANGED_DESCRIPTION_LANGUAGE(Concept.CHANGED_DESCRIPTION_LANGUAGE),
        CHANGED_DESCRIPTION_STATUS(Concept.CHANGED_DESCRIPTION_STATUS),
        CHANGED_DESCRIPTION_TERM(Concept.CHANGED_DESCRIPTION_TERM),
        CHANGED_DESCRIPTION_TYPE(Concept.CHANGED_DESCRIPTION_TYPE),
        CHANGED_RELATIONSHIP_CHARACTERISTIC(Concept.CHANGED_RELATIONSHIP_CHARACTERISTIC),
        CHANGED_RELATIONSHIP_GROUP(Concept.CHANGED_RELATIONSHIP_GROUP),
        CHANGED_RELATIONSHIP_REFINABILITY(Concept.CHANGED_RELATIONSHIP_REFINABILITY),
        CHANGED_RELATIONSHIP_STATUS(Concept.CHANGED_RELATIONSHIP_STATUS),
        CHANGED_RELATIONSHIP_TYPE(Concept.CHANGED_RELATIONSHIP_TYPE);
        protected int nid;

        private QUERY_TOKENS(I_ConceptualizeUniversally concept) {
            try {
                this.nid = concept.localize().getNid();
            } catch (TerminologyException e) {
                throw new RuntimeException(this.toString(), e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };
    protected QUERY_TOKENS tokenEnum = null;
    /**
     * Whether to use the NOT qualifier.
     */
    protected boolean trueStatement;
    /**
     * The type of query - e.g. "Concept is", "Concept is member of" etc.
     */
    protected ConceptChronicleBI queryToken;
    /**
     * The component to which the query type is applied. e.g. if query type is
     * "concept is" and query destination is "paracetamol", then the statement
     * would be "concept is":"paracetamol". Need to keep as object to handle
     * strings for descriptions
     */
    protected Object queryConstraint;
    protected TerminologyStoreDI termFactory;
    protected NidSetBI allowedTypes;

    /**
     * Constructor for refset spec statement.
     *
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The destination concept (e.g. "paracetamol")
     * @throws Exception
     * @throws IOException
     * @throws TerminologyException
     */
    public RefsetSpecStatement(boolean trueStatement, ConceptChronicleBI groupingToken, ConceptChronicleBI constraint,
            ViewCoordinate viewCoordinate) throws TerminologyException, IOException, Exception {
        super(viewCoordinate);
        this.trueStatement = trueStatement;
        this.queryToken = groupingToken;
        this.queryConstraint = constraint;
        termFactory = Ts.get();

    }

    /**
     * Constructor for refset spec statement.
     *
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The string value for regex or lucene search.
     * @throws Exception
     * @throws IOException
     * @throws TerminologyException
     */
    public RefsetSpecStatement(boolean trueStatement, ConceptChronicleBI groupingToken, String constraint,
            ViewCoordinate viewCoordinate) throws TerminologyException, IOException, Exception {
        super(viewCoordinate);
        this.trueStatement = trueStatement;
        this.queryToken = groupingToken;
        this.queryConstraint = constraint;
        termFactory = Ts.get();
    }

    public boolean isNegated() {
        return !trueStatement;
    }

    @Override
    public boolean execute(int componentNid, Object component,
            GROUPING_TYPE version, ViewCoordinate v1_is,
            ViewCoordinate v2_is) throws IOException, ContradictionException {

        boolean statementResult = getStatementResult(componentNid, component,
                version, v1_is, v2_is);

        if (trueStatement) {
            // if the statement has a negation associated with it then we need
            // to negate the results
            return statementResult;
        } else {
            return !statementResult;
        }
    }

    public abstract boolean getStatementResult(int componentNid, Object component, GROUPING_TYPE version, ViewCoordinate v1Is, ViewCoordinate v2Is) throws IOException, ContradictionException;

    protected boolean isComponentStatus(ConceptChronicleBI requiredStatus, ComponentVersionBI component) {

        if (component.getStatusNid() == requiredStatus.getConceptNid()) {
            return true;
        }

        return false;
    }

    protected boolean componentStatusIs(ComponentVersionBI tuple) {

        return isComponentStatus((ConceptChronicleBI) queryConstraint, tuple);
    }

    protected boolean componentStatusIs(ConceptChronicleBI requiredStatus, ComponentVersionBI tuple) {

        return isComponentStatus(requiredStatus, tuple);
    }

    protected boolean componentStatusIsKindOf(ComponentVersionBI tuple) throws TerminologyException, IOException {

        try {
            if (isComponentStatus((ConceptChronicleBI) queryConstraint, tuple)) {
                return true;
            }

            int parentNid = ((ConceptChronicleBI) queryConstraint).getNid();
            return termFactory.isKindOf(tuple.getStatusNid(), parentNid, viewCoordinate);
        } catch (Exception e) {
            throw new TerminologyException(e);
        }
    }

    protected boolean componentIsMemberOf(ComponentVersionBI component) throws IOException {
        int refexNid = ((ConceptChronicleBI) queryConstraint).getConceptNid();
        Collection<? extends RefexChronicleBI<?>> annotations = component.getRefexMembersActive(viewCoordinate, refexNid);
        if (!annotations.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Negates the statement by inverting the current associated negation.
     */
    public void negateStatement() {
        trueStatement = !trueStatement;
    }

    public QUERY_TOKENS getTokenEnum() {
        return tokenEnum;
    }

    public void setTokenEnum(QUERY_TOKENS tokenEnum) {
        this.tokenEnum = tokenEnum;
    }

    public String toHtmlFragment() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(trueStatement);
        buff.append(" ");
        buff.append(getTokenEnum());
        buff.append(" ");
        buff.append(queryConstraint);
        return buff.toString();
    }

    protected ViewCoordinate getViewCoordinate(GROUPING_TYPE version, ViewCoordinate v1_is,
            ViewCoordinate v2_is) throws IOException {
        if (version == GROUPING_TYPE.V1) {
            return v1_is;
        }
        if (version == GROUPING_TYPE.V2) {
            return v2_is;
        }
        throw new IOException("Version error:" + version);
    }

}
