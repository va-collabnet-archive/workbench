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
package org.ihtsdo.helper.query;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;


/**
 * The class <code>Relationship</code> represents query branches specific to relationships. 
 * If no clauses are specified, the difference report returns any that meet the Relationship clause.
 */
public class Relationship implements Branch{
    private Clause[] clauses;
    private Boolean trueStatement;
    private TOKENS token;
    private ViewCoordinate v1;
    private ViewCoordinate v2;
    
    private Relationship() {
        
    }

    /**
     * Creates a query branch asking for relationships that have been added.
     * @param trueStatement set to <code>false</code> if using negation, <code>true</code> otherwise
     * @param v1 the <code>ViewCoordinate<code> representing the first position
     * @param v2 the <code>ViewCoordinate<code> representing the second position
     * @return returns a branch representing the specified query
     */
    public static Relationship added(Boolean trueStatement, 
            ViewCoordinate v1, ViewCoordinate v2) {
        Relationship r = new Relationship();
        r.v1 = v1;
        r.v2 = v2;
        r.trueStatement = trueStatement;
        r.token = TOKENS.ADDED_RELATIONSHIP;
        return r;
    }
    
    /**
     * Creates a query branch asking for relationships that have been added.
     * @param trueStatement set to <code>false</code> if using negation, <code>true</code> otherwise
     * @param v1 the <code>ViewCoordinate<code> representing the first position
     * @param v2 the <code>ViewCoordinate<code> representing the second position
     * @param clauses one or more clauses to further filter the query
     * @return returns a branch representing the specified query
     */
    public static Relationship added(Boolean trueStatement, 
            ViewCoordinate v1, ViewCoordinate v2,
            Clause... clauses) {
        Relationship r = new Relationship();
        r.v1 = v1;
        r.v2 = v2;
        r.trueStatement = trueStatement;
        r.token = TOKENS.ADDED_RELATIONSHIP;
        r.clauses = clauses;
        return r;
    }

    /**
     * Created a query branch asking for relationships that have changed. E.g.
     * the relationship status changed from Active to Inactive.
     * @param trueStatement set to <code>false</code> if using negation, <code>true</code> otherwise
     * @param v1 the <code>ViewCoordinate<code> representing the first position
     * @param v2 the <code>ViewCoordinate<code> representing the second position
     * @return returns a branch representing the specified query
     */
    public static Relationship changedStatus(Boolean trueStatement,
            ViewCoordinate v1, ViewCoordinate v2) {
        Relationship r = new Relationship();
        r.v1 = v1;
        r.v2 = v2;
        r.trueStatement = trueStatement;
        r.token = TOKENS.CHANGED_RELATIONSHIP_STATUS;
        return r;
    }
    
    /**
     * Created a query branch asking for relationships that have changed. E.g.
     * the relationship status changed from Active to Inactive.
     * @param trueStatement set to <code>false</code> if using negation, <code>true</code> otherwise
     * @param v1 the <code>ViewCoordinate<code> representing the first position
     * @param v2 the <code>ViewCoordinate<code> representing the second position
     * @param clauses one or more clauses to further filter the query
     * @return returns a branch representing the specified query
     */
    public static Relationship changedStatus(Boolean trueStatement,
            ViewCoordinate v1, ViewCoordinate v2,
            Clause... clauses) {
        Relationship r = new Relationship();
        r.v1 = v1;
        r.v2 = v2;
        r.trueStatement = trueStatement;
        r.token = TOKENS.CHANGED_RELATIONSHIP_STATUS;
        r.clauses = clauses;
        return r;
    }
    
    
    /**
     * Created a query branch asking for relationships that have changed role groups.
     * @param trueStatement set to <code>false</code> if using negation, <code>true</code> otherwise
     * @param v1 the <code>ViewCoordinate<code> representing the first position
     * @param v2 the <code>ViewCoordinate<code> representing the second position
     * @return returns a branch representing the specified query
     */
    public static Relationship changedGroup(Boolean trueStatement,
            ViewCoordinate v1, ViewCoordinate v2) {
        Relationship r = new Relationship();
        r.v1 = v1;
        r.v2 = v2;
        r.trueStatement = trueStatement;
        r.token = TOKENS.CHANGED_REL_GROUP;
        return r;
    }
    
    /**
     * Created a query branch asking for relationships that have changed role groups.
     * @param trueStatement set to <code>false</code> if using negation, <code>true</code> otherwise
     * @param v1 the <code>ViewCoordinate<code> representing the first position
     * @param v2 the <code>ViewCoordinate<code> representing the second position
     * @param clauses one or more clauses to further filter the query
     * @return returns a branch representing the specified query
     */
    public static Relationship changedGroup(Boolean trueStatement,
            ViewCoordinate v1, ViewCoordinate v2,
            Clause... clauses) {
        Relationship r = new Relationship();
        r.v1 = v1;
        r.v2 = v2;
        r.trueStatement = trueStatement;
        r.token = TOKENS.CHANGED_REL_GROUP;
        r.clauses = clauses;
        return r;
    }

    /**
     * The clauses further specifying the query.
     * @return and array of <code>Clauses</code> further specifying the query
     */
    public Clause[] getClauses() {
        return clauses;
    }

    /**
     * Returns <code>true</code> if not using negation, otherwise <code>false</code>.
     * @return <code>true</code> if not using negation, otherwise <code>false</code>
     */
    public Boolean getTrueStatement() {
        return trueStatement;
    }

    /**
     * The token representing the type of concept query.
     * @return a <code>TOKEN</code> representing the type of concept query
     */
    public TOKENS getToken() {
        return token;
    }

    /**
     * Gets the <code>ViewCoordinate</code> of the first position.
     * @return the <code>ViewCoordinate</code> of the first position
     */
    public ViewCoordinate getV1() {
        return v1;
    }

    /**
     * Gets the <code>ViewCoordinate</code> of the second position.
     * @return the <code>ViewCoordinate</code> of the second position
     */
    public ViewCoordinate getV2() {
        return v2;
    }
    
    @Override
    public String toString(){
       StringBuilder sb = new StringBuilder();
       sb.append(trueStatement.toString()).append("\n    ");
       sb.append(v1.getPositionSet().toString()).append("\n    ");
       sb.append(v2.getPositionSet().toString()).append("\n    ");
       sb.append(token.toString()).append("\n    ");
       if(clauses != null){
           for(Clause c : clauses){
            sb.append(c.toString());
            sb.append("\n    ");
           }
       }
       return sb.toString();
    }
    
}
