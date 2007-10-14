package org.dwfa.maven.transform;

import junit.framework.TestCase;

public class DateGenerationTest extends TestCase {

   public void testGenerate() {
      try {
         
         assertTrue(UuidSnomedMap.getCurrentEffectiveDate().endsWith(" 00:00:00"));       
      
      } catch (Exception e) {
         fail("exception: " + e.getLocalizedMessage());
      }
      
   }


}
