
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.2  Built on : Sep 06, 2010 (09:42:47 CEST)
 */

            package org.ihtsdo.qadb.ws.data;
            /**
            *  ExtensionMapper class
            */
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "Case".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.Case.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "dispositionStatusCount_type0".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.DispositionStatusCount_type0.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "Component".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.Component.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "Finding".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.Finding.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "Severity".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.Severity.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "IntStrKeyValue".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.IntStrKeyValue.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "Rule".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.Rule.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "DispositionStatus".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.DispositionStatus.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "WsQACasesReportLine".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.WsQACasesReportLine.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "statusCount_type0".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.StatusCount_type0.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "IntBoolKeyValue".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.IntBoolKeyValue.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "QACoordinate".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.QACoordinate.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "Execution".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.Execution.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "RulesReportLine".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.RulesReportLine.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.ihtsdo.org/qadb/qadb-service/".equals(namespaceURI) &&
                  "Database".equals(typeName)){
                   
                            return  org.ihtsdo.qadb.ws.data.Database.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    