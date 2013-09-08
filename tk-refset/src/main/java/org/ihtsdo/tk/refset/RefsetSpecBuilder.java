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
package org.ihtsdo.tk.refset;

import java.io.IOException;
import java.util.ArrayList;
import org.ihtsdo.helper.query.Branch;
import org.ihtsdo.helper.query.Clause;
import org.ihtsdo.helper.query.Concept;
import org.ihtsdo.helper.query.Description;
import org.ihtsdo.helper.query.Grouping;
import org.ihtsdo.helper.query.Query;
import org.ihtsdo.helper.query.QueryBuilderBI;
import org.ihtsdo.helper.query.Relationship;
import org.ihtsdo.helper.query.Statement;
import org.ihtsdo.helper.query.StatementPart;
import org.ihtsdo.helper.query.Subsumption;
import org.ihtsdo.helper.query.TOKENS;
import org.ihtsdo.helper.query.WithConcept;
import org.ihtsdo.helper.query.WithDescription;
import org.ihtsdo.helper.query.WithRelationship;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.refset.RefsetComputer.ComputeType;
import org.ihtsdo.tk.refset.RefsetSpecQuery.GROUPING_TYPE;
import org.ihtsdo.tk.refset.RefsetSpecStatement.QUERY_TOKENS;

/**
 * Builds a RefsetQuerySpec object from a Query object.
 * @author aimeefurber
 */
public class RefsetSpecBuilder implements QueryBuilderBI{
    RefsetSpecQuery spec;
    ViewCoordinate genericVc; //use if not V1 or V2
    Grouping topGrouping;
    ComputeType computeType;

    public RefsetSpecBuilder(ViewCoordinate viewCoordinate) {
        this.genericVc = viewCoordinate;
    }
 
    @Override
    public ArrayList<NidBitSetBI> getResults(Query... queries) throws IOException, Exception{
        RefsetComputer[] computers = new RefsetComputer[queries.length];
        int count = 0;
        for(Query query : queries){
            RefsetSpecQuery refsetSpec = buildSpec(query);
            NidBitSetBI possibleConcepts = refsetSpec.getPossibleConcepts(Ts.get().getAllConceptNids(), null);
            RefsetComputer computer = new RefsetComputer(refsetSpec, genericVc,
                possibleConcepts, null,
                computeType);
            computers[count++] = computer;
        }
        MultiQueryProcessor queryProcessor = new MultiQueryProcessor(computers);
        Ts.get().iterateConceptDataInParallel(queryProcessor);
        
        ArrayList<NidBitSetBI> results = new ArrayList<>();
        for(RefsetComputer computer : computers){
            results.add(computer.getResultNids());
        }
        return results;
    }

    private RefsetSpecQuery buildSpec(Query query) throws IOException, Exception{
        
        Branch[] topBranches = query.getBranches();
        topGrouping = query.getGrouping();
        spec = new RefsetSpecQuery(getGroupingConcept(topGrouping),
                true,
                genericVc);
        
        for(Branch b : topBranches){
            if(Relationship.class.isAssignableFrom(b.getClass())){
                computeType = ComputeType.RELATIONSHP;
                RefsetSpecQuery relStatement = processRelationshipBranch(((Relationship) b));
                spec.addSubquery(getGroupingConcept(topGrouping),
                        true,
                        relStatement);
            }else if(Description.class.isAssignableFrom(b.getClass())){
                computeType = ComputeType.DESCRIPTION;
                RefsetSpecQuery descStatement = processDescriptionBranch(((Description) b));
                spec.addSubquery(getGroupingConcept(topGrouping),
                        true,
                        descStatement);
            }else if(Concept.class.isAssignableFrom(b.getClass())){
                computeType = ComputeType.CONCEPT;
                RefsetSpecQuery conceptStatement = processConceptBranch(((Concept) b));
                spec.addSubquery(getGroupingConcept(topGrouping),
                        true,
                        conceptStatement);
            }else if(Query.class.isAssignableFrom(b.getClass())){
                RefsetSpecQuery subquery = processSubquery((Query) b);
                spec.addSubquery(getGroupingConcept(topGrouping),
                    true,
                    subquery);
//                else if(computeType.equals(ComputeType.DESCRIPTION)){
//                    RefsetSpecQuery subquery = processDescriptionSubquery((Query) b);
//                    spec.addSubquery(getGroupingConcept(topGrouping),
//                        true,
//                        subquery);
//                }
                
            }else{
                throw new QueryException("Branch type not supported for branch: " + b);
            }
        }

        return spec;
    }
    
    //RELATIONSHPS
    private RefsetSpecQuery processRelationshipBranch(Relationship r) throws IOException, Exception{
        boolean trueStatement = r.getTrueStatement();
        TOKENS token = r.getToken();
        Clause[] clauses = r.getClauses();
        //!!! implicit AND grouping
        ConceptChronicleBI andGrouping = Ts.get().getConcept(GROUPING_TYPE.AND.getNid());
        RefsetSpecQuery relBranch = new RefsetSpecQuery(andGrouping,
                r.getTrueStatement(),
                genericVc);
     
        if(clauses != null){
            for (Clause c : clauses) {
                RefsetSpecQuery clause = processRelationshipClause(trueStatement, token, c, r.getV1(), r.getV2());
                relBranch.addSubquery(andGrouping, true, clause);
            }
        }else{
            relBranch.addRelStatement(r.getTrueStatement(),
                    getQueryConcept(r.getToken(), null),
                    null);
            if (r.getV1() != null) {
                relBranch.setV1Is(r.getV1());
            }
            if (r.getV2() != null) {
                relBranch.setV2Is(r.getV2());
            }
        }
        
        return relBranch;
    }
        
    private RefsetSpecQuery processSubquery(Query q) throws Exception{
        Branch[] topBranches = q.getBranches();
        RefsetSpecQuery subQuerySpec = null;
        
        for(Branch b : topBranches){
            if(Relationship.class.isAssignableFrom(b.getClass())){
                RefsetSpecQuery relBranch = processRelationshipBranch(((Relationship) b));
                subQuerySpec.addSubquery(getGroupingConcept(q.getGrouping()),
                        true,
                        relBranch);
            }if(Description.class.isAssignableFrom(b.getClass())){
                RefsetSpecQuery descBranch = processDescriptionBranch(((Description) b));
                subQuerySpec.addSubquery(getGroupingConcept(q.getGrouping()),
                        true,
                        descBranch);
            }else if(Query.class.isAssignableFrom(b.getClass())){
                Query subQuery = (Query) b;
                RefsetSpecQuery subQueryBranch = processSubquery(subQuery);
                subQuerySpec.addSubquery(getGroupingConcept(subQuery.getGrouping()),
                        true,
                        subQueryBranch);
            }else{
                throw new QueryException("Branch type not supported for branch: " + b);
            }
        }
        return subQuerySpec;
    }
    
    private RefsetSpecQuery processRelationshipClause(boolean trueStatement, TOKENS token, Clause c, ViewCoordinate v1, ViewCoordinate v2) throws IOException, Exception{
        Grouping g = c.getGrouping();
        Statement[] statements = c.getStatements();      
        RefsetSpecQuery specClause = new RefsetSpecQuery(
                getGroupingConcept(g),
                trueStatement,
                genericVc);
        specClause.addRelStatement(trueStatement, getQueryConcept(token, null), null);
        if(v1 != null){
            specClause.setV1Is(v1);
        }
        if(v2 != null){
            specClause.setV2Is(v2);
        }
        
        for(Statement s : statements){
            if(WithRelationship.class.isAssignableFrom(s.getClass())){         
                RelationshipStatement rs = processRelationshipStatement((WithRelationship) s);
                specClause.addRelStatement(rs.trueStatement,
                        rs.queryToken,
                        (ConceptChronicleBI) rs.queryConstraint);
            }else if(Clause.class.isAssignableFrom(s.getClass())){  
                RefsetSpecQuery subClause = processRelationshipClause(trueStatement, token, c, v1, v2);
                specClause.addSubquery(getGroupingConcept(g), true, subClause);
            }else{
                throw new QueryException("Statement type not supported for branch: " + s);
            }
        }
        return specClause;
    }
    
    private RelationshipStatement processRelationshipStatement(WithRelationship rc) throws IOException, Exception{
        StatementPart s = rc.getStatementPart();
        ConceptChronicleBI concept = Ts.get().getConcept(s.getConcept().getLenient().getConceptNid());
        RelationshipStatement rs = new RelationshipStatement(s.getTrueStatement(),
                getQueryConcept(s.getToken(), s.getSubsumption()),
                concept,
                genericVc);
        return rs;
    }
    
    //DESCRIPTIONS
        private RefsetSpecQuery processDescriptionBranch(Description d) throws IOException, Exception{
        boolean trueStatement = d.getTrueStatement();
        TOKENS token = d.getToken();
        Clause[] clauses = d.getClauses();
        //!!! implicit AND grouping
        ConceptChronicleBI andGrouping = Ts.get().getConcept(GROUPING_TYPE.AND.getNid());
        RefsetSpecQuery descBranch = new RefsetSpecQuery(andGrouping,
                d.getTrueStatement(),
                genericVc);
     
        if(clauses != null){
            for (Clause c : clauses) {
                RefsetSpecQuery clause = processDescriptionClause(trueStatement, token, c, d.getV1(), d.getV2());
                descBranch.addSubquery(andGrouping, true, clause);
            }
        }else{
            descBranch.addDescStatement(d.getTrueStatement(),
                    getQueryConcept(d.getToken(), null),
                    null);
            if (d.getV1() != null) {
                descBranch.setV1Is(d.getV1());
            }
            if (d.getV2() != null) {
                descBranch.setV2Is(d.getV2());
            }
        }
        
        return descBranch;
    }
        
//    private RefsetSpecQuery processDescriptionSubquery(Query q) throws Exception{
//        Branch[] topBranches = q.getBranches();
//        RefsetSpecQuery subQuerySpec = null;
//        
//        for(Branch b : topBranches){
//            if(Description.class.isAssignableFrom(b.getClass())){
//                RefsetSpecQuery descBranch = processDescriptionBranch(((Description) b));
//                subQuerySpec.addSubquery(getGroupingConcept(q.getGrouping()),
//                        true,
//                        descBranch);
//            }else if(Query.class.isAssignableFrom(b.getClass())){
//                Query subQuery = (Query) b;
//                RefsetSpecQuery subQueryBranch = processDescriptionSubquery(subQuery);
//                subQuerySpec.addSubquery(getGroupingConcept(subQuery.getGrouping()),
//                        true,
//                        subQueryBranch);
//            }else{
//                throw new QueryException("Branch type not supported for branch: " + b);
//            }
//        }
//        return subQuerySpec;
//    }
    
    private RefsetSpecQuery processDescriptionClause(boolean trueStatement, TOKENS token,
            Clause c, ViewCoordinate v1, ViewCoordinate v2) throws IOException, Exception{
        Grouping g = c.getGrouping();
        Statement[] statements = c.getStatements();      
        RefsetSpecQuery specClause = new RefsetSpecQuery(
                getGroupingConcept(g),
                trueStatement,
                genericVc);
        specClause.addDescStatement(trueStatement, getQueryConcept(token, null), null);
        if(v1 != null){
            specClause.setV1Is(v1);
        }
        if(v2 != null){
            specClause.setV2Is(v2);
        }
        
        for(Statement s : statements){
            if(WithDescription.class.isAssignableFrom(s.getClass())){         
                DescStatement ds = processDescriptionStatement((WithDescription) s);
                specClause.addDescStatement(ds.trueStatement,
                        ds.queryToken,
                        (ConceptChronicleBI) ds.queryConstraint);
            }else if(Clause.class.isAssignableFrom(s.getClass())){     
                RefsetSpecQuery subClause = processDescriptionClause(trueStatement, token, c, v1, v2);
                specClause.addSubquery(getGroupingConcept(g), true, subClause);
            }else{
                throw new QueryException("Statement type not supported for branch: " + s);
            }
        }
        return specClause;
    }
    
    private DescStatement processDescriptionStatement(WithDescription dc) throws IOException, Exception{
        StatementPart s = dc.getStatementPart();
        ConceptChronicleBI concept = Ts.get().getConcept(s.getConcept().getLenient().getConceptNid());
        DescStatement rs = new DescStatement(s.getTrueStatement(),
                getQueryConcept(s.getToken(), s.getSubsumption()),
                concept,
                genericVc);
        return rs;
    }
    
    //CONCEPTS
    private RefsetSpecQuery processConceptBranch(Concept con) throws IOException, Exception{
        boolean trueStatement = con.getTrueStatement();
        TOKENS token = con.getToken();
        Clause[] clauses = con.getClauses();
        //!!! implicit AND grouping
        ConceptChronicleBI andGrouping = Ts.get().getConcept(GROUPING_TYPE.AND.getNid());
        RefsetSpecQuery conceptBranch = new RefsetSpecQuery(andGrouping,
                con.getTrueStatement(),
                genericVc);
     
        if(clauses != null){
            for (Clause c : clauses) {
                RefsetSpecQuery clause = processConceptClause(trueStatement, token, c, con.getV1(), con.getV2());
                conceptBranch.addSubquery(andGrouping, true, clause);
            }
        }else{
            conceptBranch.addConceptStatement(con.getTrueStatement(),
                    getQueryConcept(con.getToken(), null),
                    null);
            if (con.getV1() != null) {
                conceptBranch.setV1Is(con.getV1());
            }
            if (con.getV2() != null) {
                conceptBranch.setV2Is(con.getV2());
            }
        }
        
        return conceptBranch;
    }
    
    private RefsetSpecQuery processConceptClause(boolean trueStatement, TOKENS token,
            Clause c, ViewCoordinate v1, ViewCoordinate v2) throws IOException, Exception{
        Grouping g = c.getGrouping();
        Statement[] statements = c.getStatements();      
        RefsetSpecQuery specClause = new RefsetSpecQuery(
                getGroupingConcept(g),
                trueStatement,
                genericVc);
        specClause.addConceptStatement(trueStatement, getQueryConcept(token, null), null);
        if(v1 != null){
            specClause.setV1Is(v1);
        }
        if(v2 != null){
            specClause.setV2Is(v2);
        }
        
        for(Statement s : statements){
            if(WithConcept.class.isAssignableFrom(s.getClass())){         
                ConceptStatement cs = processConceptStatement((WithConcept) s);
                specClause.addConceptStatement(cs.trueStatement,
                        cs.queryToken,
                        (ConceptChronicleBI) cs.queryConstraint);
            }else if(Clause.class.isAssignableFrom(s.getClass())){     
                RefsetSpecQuery subClause = processConceptClause(trueStatement, token, c, v1, v2);
                specClause.addSubquery(getGroupingConcept(g), true, subClause);
            }else{
                throw new QueryException("Statement type not supported for branch: " + s);
            }
        }
        return specClause;
    }
    
    private ConceptStatement processConceptStatement(WithConcept cc) throws IOException, Exception{
        StatementPart s = cc.getStatementPart();
        ConceptChronicleBI concept = Ts.get().getConcept(s.getConcept().getLenient().getConceptNid());
        ConceptStatement rs = new ConceptStatement(s.getTrueStatement(),
                getQueryConcept(s.getToken(), s.getSubsumption()),
                concept,
                genericVc);
        return rs;
    }
    
    private ConceptChronicleBI getQueryConcept(TOKENS t, Subsumption s) throws IOException{
        Integer tokenNid = null;
        if(t.equals(TOKENS.ADDED_RELATIONSHIP)){
            tokenNid = QUERY_TOKENS.ADDED_RELATIONSHIP.nid;
        }else if(t.equals(TOKENS.CHANGED_RELATIONSHIP_STATUS)){
            tokenNid = QUERY_TOKENS.CHANGED_RELATIONSHIP_STATUS.nid;
        }if(t.equals(TOKENS.ADDED_DESCRIPTION)){
            tokenNid = QUERY_TOKENS.ADDED_DESCRIPTION.nid;
        }else if(t.equals(TOKENS.CHANGED_DESCRIPTION_STATUS)){
            tokenNid = QUERY_TOKENS.CHANGED_DESCRIPTION_STATUS.nid;
        }else if(t.equals(TOKENS.CHANGED_DEFINED)){
            tokenNid = QUERY_TOKENS.CHANGED_CONCEPT_DEFINED.nid;
        }else if(s != null && s.equals(Subsumption.IS)){
            if(t.equals(TOKENS.REL_CHARACTERISTIC)){
                tokenNid = QUERY_TOKENS.REL_CHARACTERISTIC_IS.nid;
            }else if(t.equals(TOKENS.REL_STATUS)){
                tokenNid = QUERY_TOKENS.REL_STATUS_IS.nid;
            }else if(t.equals(TOKENS.DESC_STATUS)){
                tokenNid = QUERY_TOKENS.DESC_STATUS_IS.nid;
            }
        }else if(s != null && s.equals(Subsumption.IS_CHILD_OF)){
            if(t.equals(TOKENS.REL_CHARACTERISTIC)){
                tokenNid = QUERY_TOKENS.REL_CHARACTERISTIC_IS_CHILD_OF.nid;
            }else if(t.equals(TOKENS.REL_STATUS)){
                tokenNid = QUERY_TOKENS.REL_STATUS_IS_CHILD_OF.nid;
            }else if(t.equals(TOKENS.DESC_STATUS)){
                tokenNid = QUERY_TOKENS.DESC_STATUS_IS_CHILD_OF.nid;
            }
        }else if(s != null && s.equals(Subsumption.IS_DESCENDANT_OF)){
            if(t.equals(TOKENS.REL_CHARACTERISTIC)){
                tokenNid = QUERY_TOKENS.REL_CHARACTERISTIC_IS_DESCENDENT_OF.nid;
            }else if(t.equals(TOKENS.REL_STATUS)){
                tokenNid = QUERY_TOKENS.REL_STATUS_IS_DESCENDENT_OF.nid;
            }else if(t.equals(TOKENS.DESC_STATUS)){
                tokenNid = QUERY_TOKENS.DESC_STATUS_IS_DESCENDENT_OF.nid;
            }
        }else if(s != null && s.equals(Subsumption.IS_KIND_OF)){
            if(t.equals(TOKENS.REL_CHARACTERISTIC)){
                tokenNid = QUERY_TOKENS.REL_CHARACTERISTIC_IS_KIND_OF.nid;
            }else if(t.equals(TOKENS.REL_STATUS)){
                tokenNid = QUERY_TOKENS.REL_STATUS_IS_KIND_OF.nid;
            }else if(t.equals(TOKENS.DESC_STATUS)){
                tokenNid = QUERY_TOKENS.DESC_STATUS_IS_KIND_OF.nid;
            }
        }
        return Ts.get().getConcept(tokenNid);
    }
    
    private ConceptChronicleBI getGroupingConcept(Grouping g) throws IOException{
        Integer groupingNid = null;
        if(g.equals(Grouping.AND)){
            groupingNid = GROUPING_TYPE.AND.getNid();
        }else if(g.equals(Grouping.OR)){
            groupingNid = GROUPING_TYPE.OR.getNid();
        }
        return Ts.get().getConcept(groupingNid);
    }
}
