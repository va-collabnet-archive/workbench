package org.dwfa.ace.api.ebr;


public interface I_ThinExtByRefPartConcept extends I_ThinExtByRefPart {

   public int getC1id();
   public void setC1id(int c1id);

   /**
    * @deprecated use getC1id
    * @return
    */
   public int getConceptId();
   
   /**
    * @deprecated use setC1id
    * @param conceptId
    */
   public void setConceptId(int conceptId);

}