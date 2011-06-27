
/**
 * QadbServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.2  Built on : Sep 06, 2010 (09:42:01 CEST)
 */

    package org.ihtsdo.qadb.ws;

    /**
     *  QadbServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class QadbServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public QadbServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public QadbServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getCase method
            * override this method for handling normal response from getCase operation
            */
           public void receiveResultgetCase(
                    org.ihtsdo.qadb.ws.data.GetCaseResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCase operation
           */
            public void receiveErrorgetCase(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getQACasesReportLinesByPage method
            * override this method for handling normal response from getQACasesReportLinesByPage operation
            */
           public void receiveResultgetQACasesReportLinesByPage(
                    org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getQACasesReportLinesByPage operation
           */
            public void receiveErrorgetQACasesReportLinesByPage(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllDispositionStatus method
            * override this method for handling normal response from getAllDispositionStatus operation
            */
           public void receiveResultgetAllDispositionStatus(
                    org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllDispositionStatus operation
           */
            public void receiveErrorgetAllDispositionStatus(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllRules method
            * override this method for handling normal response from getAllRules operation
            */
           public void receiveResultgetAllRules(
                    org.ihtsdo.qadb.ws.data.AllRulesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllRules operation
           */
            public void receiveErrorgetAllRules(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRule method
            * override this method for handling normal response from getRule operation
            */
           public void receiveResultgetRule(
                    org.ihtsdo.qadb.ws.data.RuleResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRule operation
           */
            public void receiveErrorgetRule(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllPathsForDatabase method
            * override this method for handling normal response from getAllPathsForDatabase operation
            */
           public void receiveResultgetAllPathsForDatabase(
                    org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllPathsForDatabase operation
           */
            public void receiveErrorgetAllPathsForDatabase(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getExecution method
            * override this method for handling normal response from getExecution operation
            */
           public void receiveResultgetExecution(
                    org.ihtsdo.qadb.ws.data.ExecutionResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getExecution operation
           */
            public void receiveErrorgetExecution(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllDatabases method
            * override this method for handling normal response from getAllDatabases operation
            */
           public void receiveResultgetAllDatabases(
                    org.ihtsdo.qadb.ws.data.AllDatabasesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllDatabases operation
           */
            public void receiveErrorgetAllDatabases(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllSeverities method
            * override this method for handling normal response from getAllSeverities operation
            */
           public void receiveResultgetAllSeverities(
                    org.ihtsdo.qadb.ws.data.AllSeveritiesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllSeverities operation
           */
            public void receiveErrorgetAllSeverities(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllPaths method
            * override this method for handling normal response from getAllPaths operation
            */
           public void receiveResultgetAllPaths(
                    org.ihtsdo.qadb.ws.data.AllPathsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllPaths operation
           */
            public void receiveErrorgetAllPaths(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getComponent method
            * override this method for handling normal response from getComponent operation
            */
           public void receiveResultgetComponent(
                    org.ihtsdo.qadb.ws.data.ComponentResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getComponent operation
           */
            public void receiveErrorgetComponent(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRulesReportLinesByPage method
            * override this method for handling normal response from getRulesReportLinesByPage operation
            */
           public void receiveResultgetRulesReportLinesByPage(
                    org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRulesReportLinesByPage operation
           */
            public void receiveErrorgetRulesReportLinesByPage(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllComponents method
            * override this method for handling normal response from getAllComponents operation
            */
           public void receiveResultgetAllComponents(
                    org.ihtsdo.qadb.ws.data.AllComponentsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllComponents operation
           */
            public void receiveErrorgetAllComponents(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for persistQACase method
            * override this method for handling normal response from persistQACase operation
            */
           public void receiveResultpersistQACase(
                    ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from persistQACase operation
           */
            public void receiveErrorpersistQACase(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getQADatabase method
            * override this method for handling normal response from getQADatabase operation
            */
           public void receiveResultgetQADatabase(
                    org.ihtsdo.qadb.ws.data.QADatabaseResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getQADatabase operation
           */
            public void receiveErrorgetQADatabase(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getFinding method
            * override this method for handling normal response from getFinding operation
            */
           public void receiveResultgetFinding(
                    org.ihtsdo.qadb.ws.data.FindingResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getFinding operation
           */
            public void receiveErrorgetFinding(java.lang.Exception e) {
            }
                


    }
    