
/**
 * QadbServiceSkeletonInterface.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.2  Built on : Sep 06, 2010 (09:42:01 CEST)
 */
    package org.ihtsdo.qadb.ws;
    /**
     *  QadbServiceSkeletonInterface java skeleton interface for the axisService
     */
    public interface QadbServiceSkeletonInterface {
     
         
        /**
         * Auto generated method signature
         * 
                                    * @param caseRequest
         */

        
                public org.ihtsdo.qadb.ws.data.GetCaseResponse getCase
                (
                  org.ihtsdo.qadb.ws.data.CaseRequest caseRequest
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param qACasesReportLinesByPageRequest
         */

        
                public org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse getQACasesReportLinesByPage
                (
                  org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageRequest qACasesReportLinesByPageRequest
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
         */

        
                public org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse getAllDispositionStatus
                (
                  
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
         */

        
                public org.ihtsdo.qadb.ws.data.AllRulesResponse getAllRules
                (
                  
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param ruleRequest
         */

        
                public org.ihtsdo.qadb.ws.data.RuleResponse getRule
                (
                  org.ihtsdo.qadb.ws.data.RuleRequest ruleRequest
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param allPathsForDatabaseRequest
         */

        
                public org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse getAllPathsForDatabase
                (
                  org.ihtsdo.qadb.ws.data.AllPathsForDatabaseRequest allPathsForDatabaseRequest
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param executionRequest
         */

        
                public org.ihtsdo.qadb.ws.data.ExecutionResponse getExecution
                (
                  org.ihtsdo.qadb.ws.data.ExecutionRequest executionRequest
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
         */

        
                public org.ihtsdo.qadb.ws.data.AllDatabasesResponse getAllDatabases
                (
                  
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
         */

        
                public org.ihtsdo.qadb.ws.data.AllSeveritiesResponse getAllSeverities
                (
                  
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
         */

        
                public org.ihtsdo.qadb.ws.data.AllPathsResponse getAllPaths
                (
                  
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param componentRequest
         */

        
                public org.ihtsdo.qadb.ws.data.ComponentResponse getComponent
                (
                  org.ihtsdo.qadb.ws.data.ComponentRequest componentRequest
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param rulesReportLinesByPageRequest
         */

        
                public org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse getRulesReportLinesByPage
                (
                  org.ihtsdo.qadb.ws.data.RulesReportLinesByPageRequest rulesReportLinesByPageRequest
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
         */

        
                public org.ihtsdo.qadb.ws.data.AllComponentsResponse getAllComponents
                (
                  
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param persistQACaseRequest
         */

        
                public void persistQACase
                (
                  org.ihtsdo.qadb.ws.data.PersistQACaseRequest persistQACaseRequest
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param qADatabaseRequest
         */

        
                public org.ihtsdo.qadb.ws.data.QADatabaseResponse getQADatabase
                (
                  org.ihtsdo.qadb.ws.data.QADatabaseRequest qADatabaseRequest
                 )
            ;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param findingRequest
         */

        
                public org.ihtsdo.qadb.ws.data.FindingResponse getFinding
                (
                  org.ihtsdo.qadb.ws.data.FindingRequest findingRequest
                 )
            ;
        
         }
    