
/**
 * QadbServiceMessageReceiverInOut.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.2  Built on : Sep 06, 2010 (09:42:01 CEST)
 */
        package org.ihtsdo.qadb.ws;

        /**
        *  QadbServiceMessageReceiverInOut message receiver
        */

        public class QadbServiceMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutMessageReceiver{


        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        QadbServiceSkeletonInterface skel = (QadbServiceSkeletonInterface)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String methodName;
        if((op.getName() != null) && ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)){

        

            if("getCase".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.GetCaseResponse getCaseResponse7 = null;
	                        org.ihtsdo.qadb.ws.data.CaseRequest wrappedParam =
                                                             (org.ihtsdo.qadb.ws.data.CaseRequest)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    org.ihtsdo.qadb.ws.data.CaseRequest.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               getCaseResponse7 =
                                                   
                                                   
                                                         skel.getCase(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), getCaseResponse7, false);
                                    } else 

            if("getQACasesReportLinesByPage".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse qACasesReportLinesByPageResponse9 = null;
	                        org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageRequest wrappedParam =
                                                             (org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageRequest)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageRequest.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               qACasesReportLinesByPageResponse9 =
                                                   
                                                   
                                                         skel.getQACasesReportLinesByPage(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), qACasesReportLinesByPageResponse9, false);
                                    } else 

            if("getAllDispositionStatus".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse allDispositionStatusResponse11 = null;
	                        allDispositionStatusResponse11 =
                                                     
                                                 skel.getAllDispositionStatus()
                                                ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), allDispositionStatusResponse11, false);
                                    } else 

            if("getAllRules".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.AllRulesResponse allRulesResponse13 = null;
	                        allRulesResponse13 =
                                                     
                                                 skel.getAllRules()
                                                ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), allRulesResponse13, false);
                                    } else 

            if("getRule".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.RuleResponse ruleResponse15 = null;
	                        org.ihtsdo.qadb.ws.data.RuleRequest wrappedParam =
                                                             (org.ihtsdo.qadb.ws.data.RuleRequest)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    org.ihtsdo.qadb.ws.data.RuleRequest.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               ruleResponse15 =
                                                   
                                                   
                                                         skel.getRule(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), ruleResponse15, false);
                                    } else 

            if("getAllPathsForDatabase".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse allPathsForDatabaseResponse17 = null;
	                        org.ihtsdo.qadb.ws.data.AllPathsForDatabaseRequest wrappedParam =
                                                             (org.ihtsdo.qadb.ws.data.AllPathsForDatabaseRequest)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    org.ihtsdo.qadb.ws.data.AllPathsForDatabaseRequest.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               allPathsForDatabaseResponse17 =
                                                   
                                                   
                                                         skel.getAllPathsForDatabase(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), allPathsForDatabaseResponse17, false);
                                    } else 

            if("getExecution".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.ExecutionResponse executionResponse19 = null;
	                        org.ihtsdo.qadb.ws.data.ExecutionRequest wrappedParam =
                                                             (org.ihtsdo.qadb.ws.data.ExecutionRequest)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    org.ihtsdo.qadb.ws.data.ExecutionRequest.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               executionResponse19 =
                                                   
                                                   
                                                         skel.getExecution(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), executionResponse19, false);
                                    } else 

            if("getAllDatabases".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.AllDatabasesResponse allDatabasesResponse21 = null;
	                        allDatabasesResponse21 =
                                                     
                                                 skel.getAllDatabases()
                                                ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), allDatabasesResponse21, false);
                                    } else 

            if("getAllSeverities".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.AllSeveritiesResponse allSeveritiesResponse23 = null;
	                        allSeveritiesResponse23 =
                                                     
                                                 skel.getAllSeverities()
                                                ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), allSeveritiesResponse23, false);
                                    } else 

            if("getAllPaths".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.AllPathsResponse allPathsResponse25 = null;
	                        allPathsResponse25 =
                                                     
                                                 skel.getAllPaths()
                                                ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), allPathsResponse25, false);
                                    } else 

            if("getComponent".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.ComponentResponse componentResponse27 = null;
	                        org.ihtsdo.qadb.ws.data.ComponentRequest wrappedParam =
                                                             (org.ihtsdo.qadb.ws.data.ComponentRequest)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    org.ihtsdo.qadb.ws.data.ComponentRequest.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               componentResponse27 =
                                                   
                                                   
                                                         skel.getComponent(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), componentResponse27, false);
                                    } else 

            if("getRulesReportLinesByPage".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse rulesReportLinesByPageResponse29 = null;
	                        org.ihtsdo.qadb.ws.data.RulesReportLinesByPageRequest wrappedParam =
                                                             (org.ihtsdo.qadb.ws.data.RulesReportLinesByPageRequest)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    org.ihtsdo.qadb.ws.data.RulesReportLinesByPageRequest.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               rulesReportLinesByPageResponse29 =
                                                   
                                                   
                                                         skel.getRulesReportLinesByPage(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), rulesReportLinesByPageResponse29, false);
                                    } else 

            if("getAllComponents".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.AllComponentsResponse allComponentsResponse31 = null;
	                        allComponentsResponse31 =
                                                     
                                                 skel.getAllComponents()
                                                ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), allComponentsResponse31, false);
                                    } else 

            if("getQADatabase".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.QADatabaseResponse qADatabaseResponse33 = null;
	                        org.ihtsdo.qadb.ws.data.QADatabaseRequest wrappedParam =
                                                             (org.ihtsdo.qadb.ws.data.QADatabaseRequest)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    org.ihtsdo.qadb.ws.data.QADatabaseRequest.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               qADatabaseResponse33 =
                                                   
                                                   
                                                         skel.getQADatabase(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), qADatabaseResponse33, false);
                                    } else 

            if("getFinding".equals(methodName)){
                
                org.ihtsdo.qadb.ws.data.FindingResponse findingResponse35 = null;
	                        org.ihtsdo.qadb.ws.data.FindingRequest wrappedParam =
                                                             (org.ihtsdo.qadb.ws.data.FindingRequest)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    org.ihtsdo.qadb.ws.data.FindingRequest.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               findingResponse35 =
                                                   
                                                   
                                                         skel.getFinding(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), findingResponse35, false);
                                    
            } else {
              throw new java.lang.RuntimeException("method not found");
            }
        

        newMsgContext.setEnvelope(envelope);
        }
        }
        catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }
        
        //
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.CaseRequest param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.CaseRequest.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.GetCaseResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.GetCaseResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageRequest param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageRequest.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.AllRulesResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.AllRulesResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.RuleRequest param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.RuleRequest.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.RuleResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.RuleResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.AllPathsForDatabaseRequest param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.AllPathsForDatabaseRequest.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.ExecutionRequest param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.ExecutionRequest.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.ExecutionResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.ExecutionResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.AllDatabasesResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.AllDatabasesResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.AllSeveritiesResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.AllSeveritiesResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.AllPathsResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.AllPathsResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.ComponentRequest param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.ComponentRequest.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.ComponentResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.ComponentResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.RulesReportLinesByPageRequest param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.RulesReportLinesByPageRequest.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.AllComponentsResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.AllComponentsResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.QADatabaseRequest param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.QADatabaseRequest.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.QADatabaseResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.QADatabaseResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.FindingRequest param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.FindingRequest.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(org.ihtsdo.qadb.ws.data.FindingResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(org.ihtsdo.qadb.ws.data.FindingResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.GetCaseResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.GetCaseResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.GetCaseResponse wrapgetCase(){
                                org.ihtsdo.qadb.ws.data.GetCaseResponse wrappedElement = new org.ihtsdo.qadb.ws.data.GetCaseResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse wrapgetQACasesReportLinesByPage(){
                                org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse wrappedElement = new org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse wrapgetAllDispositionStatus(){
                                org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse wrappedElement = new org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.AllRulesResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.AllRulesResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.AllRulesResponse wrapgetAllRules(){
                                org.ihtsdo.qadb.ws.data.AllRulesResponse wrappedElement = new org.ihtsdo.qadb.ws.data.AllRulesResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.RuleResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.RuleResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.RuleResponse wrapgetRule(){
                                org.ihtsdo.qadb.ws.data.RuleResponse wrappedElement = new org.ihtsdo.qadb.ws.data.RuleResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse wrapgetAllPathsForDatabase(){
                                org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse wrappedElement = new org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.ExecutionResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.ExecutionResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.ExecutionResponse wrapgetExecution(){
                                org.ihtsdo.qadb.ws.data.ExecutionResponse wrappedElement = new org.ihtsdo.qadb.ws.data.ExecutionResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.AllDatabasesResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.AllDatabasesResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.AllDatabasesResponse wrapgetAllDatabases(){
                                org.ihtsdo.qadb.ws.data.AllDatabasesResponse wrappedElement = new org.ihtsdo.qadb.ws.data.AllDatabasesResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.AllSeveritiesResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.AllSeveritiesResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.AllSeveritiesResponse wrapgetAllSeverities(){
                                org.ihtsdo.qadb.ws.data.AllSeveritiesResponse wrappedElement = new org.ihtsdo.qadb.ws.data.AllSeveritiesResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.AllPathsResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.AllPathsResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.AllPathsResponse wrapgetAllPaths(){
                                org.ihtsdo.qadb.ws.data.AllPathsResponse wrappedElement = new org.ihtsdo.qadb.ws.data.AllPathsResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.ComponentResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.ComponentResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.ComponentResponse wrapgetComponent(){
                                org.ihtsdo.qadb.ws.data.ComponentResponse wrappedElement = new org.ihtsdo.qadb.ws.data.ComponentResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse wrapgetRulesReportLinesByPage(){
                                org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse wrappedElement = new org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.AllComponentsResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.AllComponentsResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.AllComponentsResponse wrapgetAllComponents(){
                                org.ihtsdo.qadb.ws.data.AllComponentsResponse wrappedElement = new org.ihtsdo.qadb.ws.data.AllComponentsResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.QADatabaseResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.QADatabaseResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.QADatabaseResponse wrapgetQADatabase(){
                                org.ihtsdo.qadb.ws.data.QADatabaseResponse wrappedElement = new org.ihtsdo.qadb.ws.data.QADatabaseResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.ihtsdo.qadb.ws.data.FindingResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(org.ihtsdo.qadb.ws.data.FindingResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private org.ihtsdo.qadb.ws.data.FindingResponse wrapgetFinding(){
                                org.ihtsdo.qadb.ws.data.FindingResponse wrappedElement = new org.ihtsdo.qadb.ws.data.FindingResponse();
                                return wrappedElement;
                         }
                    


        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{

        try {
        
                if (org.ihtsdo.qadb.ws.data.CaseRequest.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.CaseRequest.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.GetCaseResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.GetCaseResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageRequest.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageRequest.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.QACasesReportLinesByPageResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.AllDispositionStatusResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.AllRulesResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.AllRulesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.RuleRequest.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.RuleRequest.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.RuleResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.RuleResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.AllPathsForDatabaseRequest.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.AllPathsForDatabaseRequest.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.AllPathsForDatabaseResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.ExecutionRequest.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.ExecutionRequest.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.ExecutionResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.ExecutionResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.AllDatabasesResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.AllDatabasesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.AllSeveritiesResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.AllSeveritiesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.AllPathsResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.AllPathsResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.ComponentRequest.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.ComponentRequest.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.ComponentResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.ComponentResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.RulesReportLinesByPageRequest.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.RulesReportLinesByPageRequest.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.RulesReportLinesByPageResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.AllComponentsResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.AllComponentsResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.QADatabaseRequest.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.QADatabaseRequest.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.QADatabaseResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.QADatabaseResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.FindingRequest.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.FindingRequest.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (org.ihtsdo.qadb.ws.data.FindingResponse.class.equals(type)){
                
                           return org.ihtsdo.qadb.ws.data.FindingResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
        }



    

        /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
        private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
        org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
        returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
        return returnMap;
        }

        private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
        org.apache.axis2.AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
        } else {
            f = new org.apache.axis2.AxisFault(e.getMessage());
        }

        return f;
    }

        }//end of class
    