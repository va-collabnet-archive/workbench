
/**
 * Case.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.2  Built on : Sep 06, 2010 (09:42:47 CEST)
 */
            
                package org.ihtsdo.qadb.ws.data;
            

            /**
            *  Case bean class
            */
        
        public  class Case
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = Case
                Namespace URI = http://www.ihtsdo.org/qadb/qadb-service/
                Namespace Prefix = ns1
                */
            

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://www.ihtsdo.org/qadb/qadb-service/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        

                        /**
                        * field for CaseUuid
                        */

                        
                                    protected java.lang.String localCaseUuid ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getCaseUuid(){
                               return localCaseUuid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CaseUuid
                               */
                               public void setCaseUuid(java.lang.String param){
                            
                                            this.localCaseUuid=param;
                                    

                               }
                            

                        /**
                        * field for Rule
                        */

                        
                                    protected org.ihtsdo.qadb.ws.data.Rule localRule ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRuleTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return org.ihtsdo.qadb.ws.data.Rule
                           */
                           public  org.ihtsdo.qadb.ws.data.Rule getRule(){
                               return localRule;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Rule
                               */
                               public void setRule(org.ihtsdo.qadb.ws.data.Rule param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localRuleTracker = true;
                                       } else {
                                          localRuleTracker = false;
                                              
                                       }
                                   
                                            this.localRule=param;
                                    

                               }
                            

                        /**
                        * field for Component
                        */

                        
                                    protected org.ihtsdo.qadb.ws.data.Component localComponent ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localComponentTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return org.ihtsdo.qadb.ws.data.Component
                           */
                           public  org.ihtsdo.qadb.ws.data.Component getComponent(){
                               return localComponent;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Component
                               */
                               public void setComponent(org.ihtsdo.qadb.ws.data.Component param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localComponentTracker = true;
                                       } else {
                                          localComponentTracker = false;
                                              
                                       }
                                   
                                            this.localComponent=param;
                                    

                               }
                            

                        /**
                        * field for IsActive
                        */

                        
                                    protected boolean localIsActive ;
                                

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsActive(){
                               return localIsActive;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsActive
                               */
                               public void setIsActive(boolean param){
                            
                                            this.localIsActive=param;
                                    

                               }
                            

                        /**
                        * field for DispositionStatus
                        */

                        
                                    protected org.ihtsdo.qadb.ws.data.DispositionStatus localDispositionStatus ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDispositionStatusTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return org.ihtsdo.qadb.ws.data.DispositionStatus
                           */
                           public  org.ihtsdo.qadb.ws.data.DispositionStatus getDispositionStatus(){
                               return localDispositionStatus;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DispositionStatus
                               */
                               public void setDispositionStatus(org.ihtsdo.qadb.ws.data.DispositionStatus param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDispositionStatusTracker = true;
                                       } else {
                                          localDispositionStatusTracker = false;
                                              
                                       }
                                   
                                            this.localDispositionStatus=param;
                                    

                               }
                            

                        /**
                        * field for DispositionReasonUuid
                        */

                        
                                    protected java.lang.String localDispositionReasonUuid ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDispositionReasonUuidTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDispositionReasonUuid(){
                               return localDispositionReasonUuid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DispositionReasonUuid
                               */
                               public void setDispositionReasonUuid(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDispositionReasonUuidTracker = true;
                                       } else {
                                          localDispositionReasonUuidTracker = false;
                                              
                                       }
                                   
                                            this.localDispositionReasonUuid=param;
                                    

                               }
                            

                        /**
                        * field for DispositionStatusDate
                        */

                        
                                    protected java.util.Date localDispositionStatusDate ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDispositionStatusDateTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Date
                           */
                           public  java.util.Date getDispositionStatusDate(){
                               return localDispositionStatusDate;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DispositionStatusDate
                               */
                               public void setDispositionStatusDate(java.util.Date param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDispositionStatusDateTracker = true;
                                       } else {
                                          localDispositionStatusDateTracker = false;
                                              
                                       }
                                   
                                            this.localDispositionStatusDate=param;
                                    

                               }
                            

                        /**
                        * field for DispositionStatusEditor
                        */

                        
                                    protected java.lang.String localDispositionStatusEditor ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDispositionStatusEditorTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDispositionStatusEditor(){
                               return localDispositionStatusEditor;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DispositionStatusEditor
                               */
                               public void setDispositionStatusEditor(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDispositionStatusEditorTracker = true;
                                       } else {
                                          localDispositionStatusEditorTracker = false;
                                              
                                       }
                                   
                                            this.localDispositionStatusEditor=param;
                                    

                               }
                            

                        /**
                        * field for DispositionAnnotation
                        */

                        
                                    protected java.lang.String localDispositionAnnotation ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDispositionAnnotationTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDispositionAnnotation(){
                               return localDispositionAnnotation;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DispositionAnnotation
                               */
                               public void setDispositionAnnotation(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDispositionAnnotationTracker = true;
                                       } else {
                                          localDispositionAnnotationTracker = false;
                                              
                                       }
                                   
                                            this.localDispositionAnnotation=param;
                                    

                               }
                            

                        /**
                        * field for Detail
                        */

                        
                                    protected java.lang.String localDetail ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDetailTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDetail(){
                               return localDetail;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Detail
                               */
                               public void setDetail(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDetailTracker = true;
                                       } else {
                                          localDetailTracker = false;
                                              
                                       }
                                   
                                            this.localDetail=param;
                                    

                               }
                            

                        /**
                        * field for AssignedTo
                        */

                        
                                    protected java.lang.String localAssignedTo ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignedToTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignedTo(){
                               return localAssignedTo;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignedTo
                               */
                               public void setAssignedTo(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localAssignedToTracker = true;
                                       } else {
                                          localAssignedToTracker = false;
                                              
                                       }
                                   
                                            this.localAssignedTo=param;
                                    

                               }
                            

                        /**
                        * field for EffectiveTime
                        */

                        
                                    protected java.util.Date localEffectiveTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localEffectiveTimeTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.util.Date
                           */
                           public  java.util.Date getEffectiveTime(){
                               return localEffectiveTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param EffectiveTime
                               */
                               public void setEffectiveTime(java.util.Date param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localEffectiveTimeTracker = true;
                                       } else {
                                          localEffectiveTimeTracker = false;
                                              
                                       }
                                   
                                            this.localEffectiveTime=param;
                                    

                               }
                            

                        /**
                        * field for PathUuid
                        */

                        
                                    protected java.lang.String localPathUuid ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPathUuid(){
                               return localPathUuid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PathUuid
                               */
                               public void setPathUuid(java.lang.String param){
                            
                                            this.localPathUuid=param;
                                    

                               }
                            

                        /**
                        * field for DatabaseUuid
                        */

                        
                                    protected java.lang.String localDatabaseUuid ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDatabaseUuid(){
                               return localDatabaseUuid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DatabaseUuid
                               */
                               public void setDatabaseUuid(java.lang.String param){
                            
                                            this.localDatabaseUuid=param;
                                    

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{


        
               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       Case.this.serialize(parentQName,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);
            
       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            
                


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();

                    if ((namespace != null) && (namespace.trim().length() > 0)) {
                        java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                        if (writerPrefix != null) {
                            xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                        } else {
                            if (prefix == null) {
                                prefix = generatePrefix(namespace);
                            }

                            xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                            xmlWriter.writeNamespace(prefix, namespace);
                            xmlWriter.setPrefix(prefix, namespace);
                        }
                    } else {
                        xmlWriter.writeStartElement(parentQName.getLocalPart());
                    }
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://www.ihtsdo.org/qadb/qadb-service/");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":Case",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "Case",
                           xmlWriter);
                   }

               
                   }
               
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"caseUuid", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"caseUuid");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("caseUuid");
                                    }
                                

                                          if (localCaseUuid==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("caseUuid cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localCaseUuid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                              if (localRuleTracker){
                                            if (localRule==null){
                                                 throw new org.apache.axis2.databinding.ADBException("rule cannot be null!!");
                                            }
                                           localRule.serialize(new javax.xml.namespace.QName("","rule"),
                                               factory,xmlWriter);
                                        } if (localComponentTracker){
                                            if (localComponent==null){
                                                 throw new org.apache.axis2.databinding.ADBException("component cannot be null!!");
                                            }
                                           localComponent.serialize(new javax.xml.namespace.QName("","component"),
                                               factory,xmlWriter);
                                        }
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"isActive", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"isActive");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("isActive");
                                    }
                                
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isActive cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsActive));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                              if (localDispositionStatusTracker){
                                            if (localDispositionStatus==null){
                                                 throw new org.apache.axis2.databinding.ADBException("dispositionStatus cannot be null!!");
                                            }
                                           localDispositionStatus.serialize(new javax.xml.namespace.QName("","dispositionStatus"),
                                               factory,xmlWriter);
                                        } if (localDispositionReasonUuidTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"dispositionReasonUuid", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"dispositionReasonUuid");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("dispositionReasonUuid");
                                    }
                                

                                          if (localDispositionReasonUuid==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("dispositionReasonUuid cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDispositionReasonUuid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDispositionStatusDateTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"dispositionStatusDate", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"dispositionStatusDate");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("dispositionStatusDate");
                                    }
                                

                                          if (localDispositionStatusDate==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("dispositionStatusDate cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDispositionStatusDate));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDispositionStatusEditorTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"dispositionStatusEditor", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"dispositionStatusEditor");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("dispositionStatusEditor");
                                    }
                                

                                          if (localDispositionStatusEditor==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("dispositionStatusEditor cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDispositionStatusEditor);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDispositionAnnotationTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"dispositionAnnotation", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"dispositionAnnotation");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("dispositionAnnotation");
                                    }
                                

                                          if (localDispositionAnnotation==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("dispositionAnnotation cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDispositionAnnotation);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDetailTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"detail", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"detail");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("detail");
                                    }
                                

                                          if (localDetail==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("detail cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDetail);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignedToTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"assignedTo", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"assignedTo");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("assignedTo");
                                    }
                                

                                          if (localAssignedTo==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("assignedTo cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignedTo);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localEffectiveTimeTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"effectiveTime", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"effectiveTime");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("effectiveTime");
                                    }
                                

                                          if (localEffectiveTime==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("effectiveTime cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEffectiveTime));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"pathUuid", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"pathUuid");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("pathUuid");
                                    }
                                

                                          if (localPathUuid==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("pathUuid cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPathUuid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"databaseUuid", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"databaseUuid");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("databaseUuid");
                                    }
                                

                                          if (localDatabaseUuid==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("databaseUuid cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDatabaseUuid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             
                    xmlWriter.writeEndElement();
               

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

        /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
                if (namespace.equals(""))
              {
                  xmlWriter.writeAttribute(attName,attValue);
              }
              else
              {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }


  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "caseUuid"));
                                 
                                        if (localCaseUuid != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCaseUuid));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("caseUuid cannot be null!!");
                                        }
                                     if (localRuleTracker){
                            elementList.add(new javax.xml.namespace.QName("",
                                                                      "rule"));
                            
                            
                                    if (localRule==null){
                                         throw new org.apache.axis2.databinding.ADBException("rule cannot be null!!");
                                    }
                                    elementList.add(localRule);
                                } if (localComponentTracker){
                            elementList.add(new javax.xml.namespace.QName("",
                                                                      "component"));
                            
                            
                                    if (localComponent==null){
                                         throw new org.apache.axis2.databinding.ADBException("component cannot be null!!");
                                    }
                                    elementList.add(localComponent);
                                }
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "isActive"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsActive));
                             if (localDispositionStatusTracker){
                            elementList.add(new javax.xml.namespace.QName("",
                                                                      "dispositionStatus"));
                            
                            
                                    if (localDispositionStatus==null){
                                         throw new org.apache.axis2.databinding.ADBException("dispositionStatus cannot be null!!");
                                    }
                                    elementList.add(localDispositionStatus);
                                } if (localDispositionReasonUuidTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "dispositionReasonUuid"));
                                 
                                        if (localDispositionReasonUuid != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDispositionReasonUuid));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("dispositionReasonUuid cannot be null!!");
                                        }
                                    } if (localDispositionStatusDateTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "dispositionStatusDate"));
                                 
                                        if (localDispositionStatusDate != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDispositionStatusDate));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("dispositionStatusDate cannot be null!!");
                                        }
                                    } if (localDispositionStatusEditorTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "dispositionStatusEditor"));
                                 
                                        if (localDispositionStatusEditor != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDispositionStatusEditor));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("dispositionStatusEditor cannot be null!!");
                                        }
                                    } if (localDispositionAnnotationTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "dispositionAnnotation"));
                                 
                                        if (localDispositionAnnotation != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDispositionAnnotation));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("dispositionAnnotation cannot be null!!");
                                        }
                                    } if (localDetailTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "detail"));
                                 
                                        if (localDetail != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDetail));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("detail cannot be null!!");
                                        }
                                    } if (localAssignedToTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "assignedTo"));
                                 
                                        if (localAssignedTo != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignedTo));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("assignedTo cannot be null!!");
                                        }
                                    } if (localEffectiveTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "effectiveTime"));
                                 
                                        if (localEffectiveTime != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEffectiveTime));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("effectiveTime cannot be null!!");
                                        }
                                    }
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "pathUuid"));
                                 
                                        if (localPathUuid != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPathUuid));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("pathUuid cannot be null!!");
                                        }
                                    
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "databaseUuid"));
                                 
                                        if (localDatabaseUuid != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDatabaseUuid));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("databaseUuid cannot be null!!");
                                        }
                                    

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{

        
        

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static Case parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            Case object =
                new Case();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    
                            if (!"Case".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (Case)org.ihtsdo.qadb.ws.data.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                 
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","caseUuid").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCaseUuid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","rule").equals(reader.getName())){
                                
                                                object.setRule(org.ihtsdo.qadb.ws.data.Rule.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","component").equals(reader.getName())){
                                
                                                object.setComponent(org.ihtsdo.qadb.ws.data.Component.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","isActive").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsActive(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","dispositionStatus").equals(reader.getName())){
                                
                                                object.setDispositionStatus(org.ihtsdo.qadb.ws.data.DispositionStatus.Factory.parse(reader));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","dispositionReasonUuid").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDispositionReasonUuid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","dispositionStatusDate").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDispositionStatusDate(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDate(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","dispositionStatusEditor").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDispositionStatusEditor(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","dispositionAnnotation").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDispositionAnnotation(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","detail").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDetail(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","assignedTo").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignedTo(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","effectiveTime").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setEffectiveTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToDate(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","pathUuid").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPathUuid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","databaseUuid").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDatabaseUuid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            
                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                            



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          