
/**
 * Rule.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.2  Built on : Sep 06, 2010 (09:42:47 CEST)
 */
            
                package org.ihtsdo.qadb.ws.data;
            

            /**
            *  Rule bean class
            */
        
        public  class Rule
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = Rule
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
                        * field for RuleUuid
                        */

                        
                                    protected java.lang.String localRuleUuid ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRuleUuidTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRuleUuid(){
                               return localRuleUuid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RuleUuid
                               */
                               public void setRuleUuid(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localRuleUuidTracker = true;
                                       } else {
                                          localRuleUuidTracker = false;
                                              
                                       }
                                   
                                            this.localRuleUuid=param;
                                    

                               }
                            

                        /**
                        * field for Name
                        */

                        
                                    protected java.lang.String localName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNameTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getName(){
                               return localName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Name
                               */
                               public void setName(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localNameTracker = true;
                                       } else {
                                          localNameTracker = false;
                                              
                                       }
                                   
                                            this.localName=param;
                                    

                               }
                            

                        /**
                        * field for Description
                        */

                        
                                    protected java.lang.String localDescription ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDescriptionTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDescription(){
                               return localDescription;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Description
                               */
                               public void setDescription(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDescriptionTracker = true;
                                       } else {
                                          localDescriptionTracker = false;
                                              
                                       }
                                   
                                            this.localDescription=param;
                                    

                               }
                            

                        /**
                        * field for Status
                        */

                        
                                    protected int localStatus ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStatusTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getStatus(){
                               return localStatus;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Status
                               */
                               public void setStatus(int param){
                            
                                       // setting primitive attribute tracker to true
                                       
                                               if (param==java.lang.Integer.MIN_VALUE) {
                                           localStatusTracker = false;
                                              
                                       } else {
                                          localStatusTracker = true;
                                       }
                                   
                                            this.localStatus=param;
                                    

                               }
                            

                        /**
                        * field for Severity
                        */

                        
                                    protected java.lang.String localSeverity ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSeverityTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSeverity(){
                               return localSeverity;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Severity
                               */
                               public void setSeverity(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localSeverityTracker = true;
                                       } else {
                                          localSeverityTracker = false;
                                              
                                       }
                                   
                                            this.localSeverity=param;
                                    

                               }
                            

                        /**
                        * field for PackageName
                        */

                        
                                    protected java.lang.String localPackageName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPackageNameTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPackageName(){
                               return localPackageName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PackageName
                               */
                               public void setPackageName(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPackageNameTracker = true;
                                       } else {
                                          localPackageNameTracker = false;
                                              
                                       }
                                   
                                            this.localPackageName=param;
                                    

                               }
                            

                        /**
                        * field for PackageUrl
                        */

                        
                                    protected java.lang.String localPackageUrl ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPackageUrlTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPackageUrl(){
                               return localPackageUrl;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PackageUrl
                               */
                               public void setPackageUrl(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPackageUrlTracker = true;
                                       } else {
                                          localPackageUrlTracker = false;
                                              
                                       }
                                   
                                            this.localPackageUrl=param;
                                    

                               }
                            

                        /**
                        * field for DitaUuid
                        */

                        
                                    protected java.lang.String localDitaUuid ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDitaUuidTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDitaUuid(){
                               return localDitaUuid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DitaUuid
                               */
                               public void setDitaUuid(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDitaUuidTracker = true;
                                       } else {
                                          localDitaUuidTracker = false;
                                              
                                       }
                                   
                                            this.localDitaUuid=param;
                                    

                               }
                            

                        /**
                        * field for RuleCode
                        */

                        
                                    protected java.lang.String localRuleCode ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRuleCodeTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRuleCode(){
                               return localRuleCode;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RuleCode
                               */
                               public void setRuleCode(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localRuleCodeTracker = true;
                                       } else {
                                          localRuleCodeTracker = false;
                                              
                                       }
                                   
                                            this.localRuleCode=param;
                                    

                               }
                            

                        /**
                        * field for Category
                        */

                        
                                    protected java.lang.String localCategory ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCategoryTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getCategory(){
                               return localCategory;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Category
                               */
                               public void setCategory(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localCategoryTracker = true;
                                       } else {
                                          localCategoryTracker = false;
                                              
                                       }
                                   
                                            this.localCategory=param;
                                    

                               }
                            

                        /**
                        * field for ExpectedResult
                        */

                        
                                    protected java.lang.String localExpectedResult ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localExpectedResultTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getExpectedResult(){
                               return localExpectedResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ExpectedResult
                               */
                               public void setExpectedResult(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localExpectedResultTracker = true;
                                       } else {
                                          localExpectedResultTracker = false;
                                              
                                       }
                                   
                                            this.localExpectedResult=param;
                                    

                               }
                            

                        /**
                        * field for SuggestedResolution
                        */

                        
                                    protected java.lang.String localSuggestedResolution ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSuggestedResolutionTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSuggestedResolution(){
                               return localSuggestedResolution;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SuggestedResolution
                               */
                               public void setSuggestedResolution(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localSuggestedResolutionTracker = true;
                                       } else {
                                          localSuggestedResolutionTracker = false;
                                              
                                       }
                                   
                                            this.localSuggestedResolution=param;
                                    

                               }
                            

                        /**
                        * field for Example
                        */

                        
                                    protected java.lang.String localExample ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localExampleTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getExample(){
                               return localExample;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Example
                               */
                               public void setExample(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localExampleTracker = true;
                                       } else {
                                          localExampleTracker = false;
                                              
                                       }
                                   
                                            this.localExample=param;
                                    

                               }
                            

                        /**
                        * field for StandingIssues
                        */

                        
                                    protected java.lang.String localStandingIssues ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localStandingIssuesTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getStandingIssues(){
                               return localStandingIssues;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param StandingIssues
                               */
                               public void setStandingIssues(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localStandingIssuesTracker = true;
                                       } else {
                                          localStandingIssuesTracker = false;
                                              
                                       }
                                   
                                            this.localStandingIssues=param;
                                    

                               }
                            

                        /**
                        * field for IsWhitelistAllowed
                        */

                        
                                    protected boolean localIsWhitelistAllowed ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsWhitelistAllowedTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsWhitelistAllowed(){
                               return localIsWhitelistAllowed;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsWhitelistAllowed
                               */
                               public void setIsWhitelistAllowed(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       
                                               if (false) {
                                           localIsWhitelistAllowedTracker = false;
                                              
                                       } else {
                                          localIsWhitelistAllowedTracker = true;
                                       }
                                   
                                            this.localIsWhitelistAllowed=param;
                                    

                               }
                            

                        /**
                        * field for IsWhitelistResetAllowed
                        */

                        
                                    protected boolean localIsWhitelistResetAllowed ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsWhitelistResetAllowedTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsWhitelistResetAllowed(){
                               return localIsWhitelistResetAllowed;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsWhitelistResetAllowed
                               */
                               public void setIsWhitelistResetAllowed(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       
                                               if (false) {
                                           localIsWhitelistResetAllowedTracker = false;
                                              
                                       } else {
                                          localIsWhitelistResetAllowedTracker = true;
                                       }
                                   
                                            this.localIsWhitelistResetAllowed=param;
                                    

                               }
                            

                        /**
                        * field for IsWhitelistResetWhenClosed
                        */

                        
                                    protected boolean localIsWhitelistResetWhenClosed ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIsWhitelistResetWhenClosedTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getIsWhitelistResetWhenClosed(){
                               return localIsWhitelistResetWhenClosed;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IsWhitelistResetWhenClosed
                               */
                               public void setIsWhitelistResetWhenClosed(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       
                                               if (false) {
                                           localIsWhitelistResetWhenClosedTracker = false;
                                              
                                       } else {
                                          localIsWhitelistResetWhenClosedTracker = true;
                                       }
                                   
                                            this.localIsWhitelistResetWhenClosed=param;
                                    

                               }
                            

                        /**
                        * field for DitaDocumentationLinkUuid
                        */

                        
                                    protected java.lang.String localDitaDocumentationLinkUuid ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDitaDocumentationLinkUuidTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDitaDocumentationLinkUuid(){
                               return localDitaDocumentationLinkUuid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DitaDocumentationLinkUuid
                               */
                               public void setDitaDocumentationLinkUuid(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDitaDocumentationLinkUuidTracker = true;
                                       } else {
                                          localDitaDocumentationLinkUuidTracker = false;
                                              
                                       }
                                   
                                            this.localDitaDocumentationLinkUuid=param;
                                    

                               }
                            

                        /**
                        * field for DitaGeneratedTopicUuid
                        */

                        
                                    protected java.lang.String localDitaGeneratedTopicUuid ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDitaGeneratedTopicUuidTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDitaGeneratedTopicUuid(){
                               return localDitaGeneratedTopicUuid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DitaGeneratedTopicUuid
                               */
                               public void setDitaGeneratedTopicUuid(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDitaGeneratedTopicUuidTracker = true;
                                       } else {
                                          localDitaGeneratedTopicUuidTracker = false;
                                              
                                       }
                                   
                                            this.localDitaGeneratedTopicUuid=param;
                                    

                               }
                            

                        /**
                        * field for ModifiedBy
                        */

                        
                                    protected java.lang.String localModifiedBy ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localModifiedByTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getModifiedBy(){
                               return localModifiedBy;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ModifiedBy
                               */
                               public void setModifiedBy(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localModifiedByTracker = true;
                                       } else {
                                          localModifiedByTracker = false;
                                              
                                       }
                                   
                                            this.localModifiedBy=param;
                                    

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
                       Rule.this.serialize(parentQName,factory,xmlWriter);
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
                           namespacePrefix+":Rule",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "Rule",
                           xmlWriter);
                   }

               
                   }
                if (localRuleUuidTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"ruleUuid", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"ruleUuid");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("ruleUuid");
                                    }
                                

                                          if (localRuleUuid==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("ruleUuid cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRuleUuid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNameTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"name", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"name");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("name");
                                    }
                                

                                          if (localName==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("name cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDescriptionTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"description", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"description");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("description");
                                    }
                                

                                          if (localDescription==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("description cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDescription);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localStatusTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"status", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"status");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("status");
                                    }
                                
                                               if (localStatus==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("status cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStatus));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSeverityTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"severity", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"severity");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("severity");
                                    }
                                

                                          if (localSeverity==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("severity cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSeverity);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPackageNameTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"packageName", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"packageName");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("packageName");
                                    }
                                

                                          if (localPackageName==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("packageName cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPackageName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPackageUrlTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"packageUrl", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"packageUrl");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("packageUrl");
                                    }
                                

                                          if (localPackageUrl==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("packageUrl cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPackageUrl);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDitaUuidTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"ditaUuid", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"ditaUuid");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("ditaUuid");
                                    }
                                

                                          if (localDitaUuid==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("ditaUuid cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDitaUuid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRuleCodeTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"ruleCode", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"ruleCode");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("ruleCode");
                                    }
                                

                                          if (localRuleCode==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("ruleCode cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRuleCode);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCategoryTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"category", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"category");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("category");
                                    }
                                

                                          if (localCategory==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("category cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localCategory);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localExpectedResultTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"expectedResult", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"expectedResult");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("expectedResult");
                                    }
                                

                                          if (localExpectedResult==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("expectedResult cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localExpectedResult);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSuggestedResolutionTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"suggestedResolution", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"suggestedResolution");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("suggestedResolution");
                                    }
                                

                                          if (localSuggestedResolution==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("suggestedResolution cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSuggestedResolution);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localExampleTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"example", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"example");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("example");
                                    }
                                

                                          if (localExample==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("example cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localExample);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localStandingIssuesTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"standingIssues", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"standingIssues");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("standingIssues");
                                    }
                                

                                          if (localStandingIssues==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("standingIssues cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localStandingIssues);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsWhitelistAllowedTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"isWhitelistAllowed", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"isWhitelistAllowed");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("isWhitelistAllowed");
                                    }
                                
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isWhitelistAllowed cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsWhitelistAllowed));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsWhitelistResetAllowedTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"isWhitelistResetAllowed", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"isWhitelistResetAllowed");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("isWhitelistResetAllowed");
                                    }
                                
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isWhitelistResetAllowed cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsWhitelistResetAllowed));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIsWhitelistResetWhenClosedTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"isWhitelistResetWhenClosed", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"isWhitelistResetWhenClosed");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("isWhitelistResetWhenClosed");
                                    }
                                
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("isWhitelistResetWhenClosed cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsWhitelistResetWhenClosed));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDitaDocumentationLinkUuidTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"ditaDocumentationLinkUuid", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"ditaDocumentationLinkUuid");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("ditaDocumentationLinkUuid");
                                    }
                                

                                          if (localDitaDocumentationLinkUuid==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("ditaDocumentationLinkUuid cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDitaDocumentationLinkUuid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDitaGeneratedTopicUuidTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"ditaGeneratedTopicUuid", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"ditaGeneratedTopicUuid");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("ditaGeneratedTopicUuid");
                                    }
                                

                                          if (localDitaGeneratedTopicUuid==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("ditaGeneratedTopicUuid cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDitaGeneratedTopicUuid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localModifiedByTracker){
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"modifiedBy", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"modifiedBy");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("modifiedBy");
                                    }
                                

                                          if (localModifiedBy==null){
                                              // write the nil attribute
                                              
                                                     throw new org.apache.axis2.databinding.ADBException("modifiedBy cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localModifiedBy);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
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

                 if (localRuleUuidTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "ruleUuid"));
                                 
                                        if (localRuleUuid != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRuleUuid));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("ruleUuid cannot be null!!");
                                        }
                                    } if (localNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "name"));
                                 
                                        if (localName != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localName));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("name cannot be null!!");
                                        }
                                    } if (localDescriptionTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "description"));
                                 
                                        if (localDescription != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDescription));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("description cannot be null!!");
                                        }
                                    } if (localStatusTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "status"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStatus));
                            } if (localSeverityTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "severity"));
                                 
                                        if (localSeverity != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSeverity));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("severity cannot be null!!");
                                        }
                                    } if (localPackageNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "packageName"));
                                 
                                        if (localPackageName != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPackageName));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("packageName cannot be null!!");
                                        }
                                    } if (localPackageUrlTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "packageUrl"));
                                 
                                        if (localPackageUrl != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPackageUrl));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("packageUrl cannot be null!!");
                                        }
                                    } if (localDitaUuidTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "ditaUuid"));
                                 
                                        if (localDitaUuid != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDitaUuid));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("ditaUuid cannot be null!!");
                                        }
                                    } if (localRuleCodeTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "ruleCode"));
                                 
                                        if (localRuleCode != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRuleCode));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("ruleCode cannot be null!!");
                                        }
                                    } if (localCategoryTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "category"));
                                 
                                        if (localCategory != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCategory));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("category cannot be null!!");
                                        }
                                    } if (localExpectedResultTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "expectedResult"));
                                 
                                        if (localExpectedResult != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localExpectedResult));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("expectedResult cannot be null!!");
                                        }
                                    } if (localSuggestedResolutionTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "suggestedResolution"));
                                 
                                        if (localSuggestedResolution != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSuggestedResolution));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("suggestedResolution cannot be null!!");
                                        }
                                    } if (localExampleTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "example"));
                                 
                                        if (localExample != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localExample));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("example cannot be null!!");
                                        }
                                    } if (localStandingIssuesTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "standingIssues"));
                                 
                                        if (localStandingIssues != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStandingIssues));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("standingIssues cannot be null!!");
                                        }
                                    } if (localIsWhitelistAllowedTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "isWhitelistAllowed"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsWhitelistAllowed));
                            } if (localIsWhitelistResetAllowedTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "isWhitelistResetAllowed"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsWhitelistResetAllowed));
                            } if (localIsWhitelistResetWhenClosedTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "isWhitelistResetWhenClosed"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIsWhitelistResetWhenClosed));
                            } if (localDitaDocumentationLinkUuidTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "ditaDocumentationLinkUuid"));
                                 
                                        if (localDitaDocumentationLinkUuid != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDitaDocumentationLinkUuid));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("ditaDocumentationLinkUuid cannot be null!!");
                                        }
                                    } if (localDitaGeneratedTopicUuidTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "ditaGeneratedTopicUuid"));
                                 
                                        if (localDitaGeneratedTopicUuid != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDitaGeneratedTopicUuid));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("ditaGeneratedTopicUuid cannot be null!!");
                                        }
                                    } if (localModifiedByTracker){
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "modifiedBy"));
                                 
                                        if (localModifiedBy != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localModifiedBy));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("modifiedBy cannot be null!!");
                                        }
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
        public static Rule parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            Rule object =
                new Rule();

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
                    
                            if (!"Rule".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (Rule)org.ihtsdo.qadb.ws.data.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                 
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","ruleUuid").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRuleUuid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","name").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","description").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDescription(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","status").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStatus(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setStatus(java.lang.Integer.MIN_VALUE);
                                           
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","severity").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSeverity(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","packageName").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPackageName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","packageUrl").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPackageUrl(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","ditaUuid").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDitaUuid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","ruleCode").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRuleCode(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","category").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCategory(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","expectedResult").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setExpectedResult(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","suggestedResolution").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSuggestedResolution(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","example").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setExample(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","standingIssues").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setStandingIssues(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","isWhitelistAllowed").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsWhitelistAllowed(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","isWhitelistResetAllowed").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsWhitelistResetAllowed(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","isWhitelistResetWhenClosed").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIsWhitelistResetWhenClosed(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","ditaDocumentationLinkUuid").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDitaDocumentationLinkUuid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","ditaGeneratedTopicUuid").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDitaGeneratedTopicUuid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","modifiedBy").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setModifiedBy(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
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
           
          