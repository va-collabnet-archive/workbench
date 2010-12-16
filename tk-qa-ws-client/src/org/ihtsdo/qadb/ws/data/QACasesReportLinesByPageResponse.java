
/**
 * QACasesReportLinesByPageResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.2  Built on : Sep 06, 2010 (09:42:47 CEST)
 */
            
                package org.ihtsdo.qadb.ws.data;
            

            /**
            *  QACasesReportLinesByPageResponse bean class
            */
        
        public  class QACasesReportLinesByPageResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://www.ihtsdo.org/qadb/qadb-service/",
                "QACasesReportLinesByPageResponse",
                "ns1");

            

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://www.ihtsdo.org/qadb/qadb-service/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        

                        /**
                        * field for Lines
                        * This was an Array!
                        */

                        
                                    protected org.ihtsdo.qadb.ws.data.WsQACasesReportLine[] localLines ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localLinesTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return org.ihtsdo.qadb.ws.data.WsQACasesReportLine[]
                           */
                           public  org.ihtsdo.qadb.ws.data.WsQACasesReportLine[] getLines(){
                               return localLines;
                           }

                           
                        


                               
                              /**
                               * validate the array for Lines
                               */
                              protected void validateLines(org.ihtsdo.qadb.ws.data.WsQACasesReportLine[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Lines
                              */
                              public void setLines(org.ihtsdo.qadb.ws.data.WsQACasesReportLine[] param){
                              
                                   validateLines(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localLinesTracker = true;
                                          } else {
                                             localLinesTracker = false;
                                                 
                                          }
                                      
                                      this.localLines=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.ihtsdo.qadb.ws.data.WsQACasesReportLine
                             */
                             public void addLines(org.ihtsdo.qadb.ws.data.WsQACasesReportLine param){
                                   if (localLines == null){
                                   localLines = new org.ihtsdo.qadb.ws.data.WsQACasesReportLine[]{};
                                   }

                            
                                 //update the setting tracker
                                localLinesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localLines);
                               list.add(param);
                               this.localLines =
                             (org.ihtsdo.qadb.ws.data.WsQACasesReportLine[])list.toArray(
                            new org.ihtsdo.qadb.ws.data.WsQACasesReportLine[list.size()]);

                             }
                             

                        /**
                        * field for SortBy
                        * This was an Array!
                        */

                        
                                    protected org.ihtsdo.qadb.ws.data.IntBoolKeyValue[] localSortBy ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSortByTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return org.ihtsdo.qadb.ws.data.IntBoolKeyValue[]
                           */
                           public  org.ihtsdo.qadb.ws.data.IntBoolKeyValue[] getSortBy(){
                               return localSortBy;
                           }

                           
                        


                               
                              /**
                               * validate the array for SortBy
                               */
                              protected void validateSortBy(org.ihtsdo.qadb.ws.data.IntBoolKeyValue[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param SortBy
                              */
                              public void setSortBy(org.ihtsdo.qadb.ws.data.IntBoolKeyValue[] param){
                              
                                   validateSortBy(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localSortByTracker = true;
                                          } else {
                                             localSortByTracker = false;
                                                 
                                          }
                                      
                                      this.localSortBy=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.ihtsdo.qadb.ws.data.IntBoolKeyValue
                             */
                             public void addSortBy(org.ihtsdo.qadb.ws.data.IntBoolKeyValue param){
                                   if (localSortBy == null){
                                   localSortBy = new org.ihtsdo.qadb.ws.data.IntBoolKeyValue[]{};
                                   }

                            
                                 //update the setting tracker
                                localSortByTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localSortBy);
                               list.add(param);
                               this.localSortBy =
                             (org.ihtsdo.qadb.ws.data.IntBoolKeyValue[])list.toArray(
                            new org.ihtsdo.qadb.ws.data.IntBoolKeyValue[list.size()]);

                             }
                             

                        /**
                        * field for Filter
                        * This was an Array!
                        */

                        
                                    protected org.ihtsdo.qadb.ws.data.IntStrKeyValue[] localFilter ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFilterTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return org.ihtsdo.qadb.ws.data.IntStrKeyValue[]
                           */
                           public  org.ihtsdo.qadb.ws.data.IntStrKeyValue[] getFilter(){
                               return localFilter;
                           }

                           
                        


                               
                              /**
                               * validate the array for Filter
                               */
                              protected void validateFilter(org.ihtsdo.qadb.ws.data.IntStrKeyValue[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Filter
                              */
                              public void setFilter(org.ihtsdo.qadb.ws.data.IntStrKeyValue[] param){
                              
                                   validateFilter(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localFilterTracker = true;
                                          } else {
                                             localFilterTracker = false;
                                                 
                                          }
                                      
                                      this.localFilter=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.ihtsdo.qadb.ws.data.IntStrKeyValue
                             */
                             public void addFilter(org.ihtsdo.qadb.ws.data.IntStrKeyValue param){
                                   if (localFilter == null){
                                   localFilter = new org.ihtsdo.qadb.ws.data.IntStrKeyValue[]{};
                                   }

                            
                                 //update the setting tracker
                                localFilterTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localFilter);
                               list.add(param);
                               this.localFilter =
                             (org.ihtsdo.qadb.ws.data.IntStrKeyValue[])list.toArray(
                            new org.ihtsdo.qadb.ws.data.IntStrKeyValue[list.size()]);

                             }
                             

                        /**
                        * field for InitialLine
                        */

                        
                                    protected int localInitialLine ;
                                

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getInitialLine(){
                               return localInitialLine;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param InitialLine
                               */
                               public void setInitialLine(int param){
                            
                                            this.localInitialLine=param;
                                    

                               }
                            

                        /**
                        * field for FinalLine
                        */

                        
                                    protected int localFinalLine ;
                                

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getFinalLine(){
                               return localFinalLine;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param FinalLine
                               */
                               public void setFinalLine(int param){
                            
                                            this.localFinalLine=param;
                                    

                               }
                            

                        /**
                        * field for TotalLines
                        */

                        
                                    protected int localTotalLines ;
                                

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getTotalLines(){
                               return localTotalLines;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param TotalLines
                               */
                               public void setTotalLines(int param){
                            
                                            this.localTotalLines=param;
                                    

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
                       new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       QACasesReportLinesByPageResponse.this.serialize(MY_QNAME,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
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
                           namespacePrefix+":QACasesReportLinesByPageResponse",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "QACasesReportLinesByPageResponse",
                           xmlWriter);
                   }

               
                   }
                if (localLinesTracker){
                                       if (localLines!=null){
                                            for (int i = 0;i < localLines.length;i++){
                                                if (localLines[i] != null){
                                                 localLines[i].serialize(new javax.xml.namespace.QName("","lines"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                        // we don't have to do any thing since minOccures is zero
                                                    
                                                }

                                            }
                                     } else {
                                        
                                               throw new org.apache.axis2.databinding.ADBException("lines cannot be null!!");
                                        
                                    }
                                 } if (localSortByTracker){
                                       if (localSortBy!=null){
                                            for (int i = 0;i < localSortBy.length;i++){
                                                if (localSortBy[i] != null){
                                                 localSortBy[i].serialize(new javax.xml.namespace.QName("","sortBy"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                        // we don't have to do any thing since minOccures is zero
                                                    
                                                }

                                            }
                                     } else {
                                        
                                               throw new org.apache.axis2.databinding.ADBException("sortBy cannot be null!!");
                                        
                                    }
                                 } if (localFilterTracker){
                                       if (localFilter!=null){
                                            for (int i = 0;i < localFilter.length;i++){
                                                if (localFilter[i] != null){
                                                 localFilter[i].serialize(new javax.xml.namespace.QName("","filter"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                        // we don't have to do any thing since minOccures is zero
                                                    
                                                }

                                            }
                                     } else {
                                        
                                               throw new org.apache.axis2.databinding.ADBException("filter cannot be null!!");
                                        
                                    }
                                 }
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"initialLine", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"initialLine");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("initialLine");
                                    }
                                
                                               if (localInitialLine==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("initialLine cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localInitialLine));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"finalLine", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"finalLine");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("finalLine");
                                    }
                                
                                               if (localFinalLine==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("finalLine cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFinalLine));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             
                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"totalLines", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"totalLines");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("totalLines");
                                    }
                                
                                               if (localTotalLines==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("totalLines cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTotalLines));
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

                 if (localLinesTracker){
                             if (localLines!=null) {
                                 for (int i = 0;i < localLines.length;i++){

                                    if (localLines[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("",
                                                                          "lines"));
                                         elementList.add(localLines[i]);
                                    } else {
                                        
                                                // nothing to do
                                            
                                    }

                                 }
                             } else {
                                 
                                        throw new org.apache.axis2.databinding.ADBException("lines cannot be null!!");
                                    
                             }

                        } if (localSortByTracker){
                             if (localSortBy!=null) {
                                 for (int i = 0;i < localSortBy.length;i++){

                                    if (localSortBy[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("",
                                                                          "sortBy"));
                                         elementList.add(localSortBy[i]);
                                    } else {
                                        
                                                // nothing to do
                                            
                                    }

                                 }
                             } else {
                                 
                                        throw new org.apache.axis2.databinding.ADBException("sortBy cannot be null!!");
                                    
                             }

                        } if (localFilterTracker){
                             if (localFilter!=null) {
                                 for (int i = 0;i < localFilter.length;i++){

                                    if (localFilter[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("",
                                                                          "filter"));
                                         elementList.add(localFilter[i]);
                                    } else {
                                        
                                                // nothing to do
                                            
                                    }

                                 }
                             } else {
                                 
                                        throw new org.apache.axis2.databinding.ADBException("filter cannot be null!!");
                                    
                             }

                        }
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "initialLine"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localInitialLine));
                            
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "finalLine"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFinalLine));
                            
                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "totalLines"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTotalLines));
                            

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
        public static QACasesReportLinesByPageResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            QACasesReportLinesByPageResponse object =
                new QACasesReportLinesByPageResponse();

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
                    
                            if (!"QACasesReportLinesByPageResponse".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (QACasesReportLinesByPageResponse)org.ihtsdo.qadb.ws.data.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                 
                    
                    reader.next();
                
                        java.util.ArrayList list1 = new java.util.ArrayList();
                    
                        java.util.ArrayList list2 = new java.util.ArrayList();
                    
                        java.util.ArrayList list3 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","lines").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list1.add(org.ihtsdo.qadb.ws.data.WsQACasesReportLine.Factory.parse(reader));
                                                                
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone1 = false;
                                                        while(!loopDone1){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone1 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("","lines").equals(reader.getName())){
                                                                    list1.add(org.ihtsdo.qadb.ws.data.WsQACasesReportLine.Factory.parse(reader));
                                                                        
                                                                }else{
                                                                    loopDone1 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setLines((org.ihtsdo.qadb.ws.data.WsQACasesReportLine[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.ihtsdo.qadb.ws.data.WsQACasesReportLine.class,
                                                                list1));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","sortBy").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list2.add(org.ihtsdo.qadb.ws.data.IntBoolKeyValue.Factory.parse(reader));
                                                                
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone2 = false;
                                                        while(!loopDone2){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone2 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("","sortBy").equals(reader.getName())){
                                                                    list2.add(org.ihtsdo.qadb.ws.data.IntBoolKeyValue.Factory.parse(reader));
                                                                        
                                                                }else{
                                                                    loopDone2 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setSortBy((org.ihtsdo.qadb.ws.data.IntBoolKeyValue[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.ihtsdo.qadb.ws.data.IntBoolKeyValue.class,
                                                                list2));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","filter").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    list3.add(org.ihtsdo.qadb.ws.data.IntStrKeyValue.Factory.parse(reader));
                                                                
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone3 = false;
                                                        while(!loopDone3){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone3 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("","filter").equals(reader.getName())){
                                                                    list3.add(org.ihtsdo.qadb.ws.data.IntStrKeyValue.Factory.parse(reader));
                                                                        
                                                                }else{
                                                                    loopDone3 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setFilter((org.ihtsdo.qadb.ws.data.IntStrKeyValue[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.ihtsdo.qadb.ws.data.IntStrKeyValue.class,
                                                                list3));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","initialLine").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setInitialLine(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","finalLine").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setFinalLine(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","totalLines").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setTotalLines(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
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
           
          