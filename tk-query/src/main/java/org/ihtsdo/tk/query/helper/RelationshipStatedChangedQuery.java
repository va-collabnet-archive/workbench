/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.tk.query.helper;

import org.ihtsdo.tk.query.parts.Clause;
import org.ihtsdo.tk.query.parts.Query;
import org.ihtsdo.tk.query.parts.Relationship;
import org.ihtsdo.tk.query.parts.Subsumption;
import org.ihtsdo.tk.query.parts.WithRelationship;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 *The class <code>RelationshipStatedChangedQuery</code> represents a query asking
 * for stated relationships which have been added or changed between two positions.
 */
public class RelationshipStatedChangedQuery {
    private Query query;
    
    /**
     * Constructs a stated relationship changed query using the interval represented
     * by the two given <code>ViewCoordinates</code>.
     * @param v1 the <code>ViewCoordinate</code> representing the starting position
     * @param v2 the <code>ViewCoordinate</code> representing the ending position
     */
    public RelationshipStatedChangedQuery(ViewCoordinate v1, ViewCoordinate v2) {
        v1 = v1.getViewCoordinateWithAllStatusValues();
        v2 = v2.getViewCoordinateWithAllStatusValues();
        query = Query.or(Relationship.added(true, v1, v2, Clause.and(WithRelationship.characteristic(true, Subsumption.IS, SnomedMetadataRf2.STATED_RELATIONSHIP_RF2))),
                 Relationship.changed(true, v1, v2, Clause.and(WithRelationship.characteristic(true, Subsumption.IS, SnomedMetadataRf2.STATED_RELATIONSHIP_RF2))));
    }
    
    /**
     * Gets a <code>Query></code> object represented by this class.
     * @return a <code>Query></code> object represented by this class
     */
    public Query getQuery() {
        return query;
    }
}
