package org.dwfa.maven.transform;

import junit.framework.TestCase;

public class SctIdGeneratorTest extends TestCase {

   
   //Concept table
   //369445005 0  Chronic proctocolitis (disorder) XUU7a D5-45285 1

   //Description table  
   //513502016 8  133947006   Occupations 0  3  en

   //Rel table
   //100000028 280844000   116680003   71737002 0  0  0
   //1000036

   
   public void testGenerate() {
      try {
         SctIdGenerator.generate(0, SctIdGenerator.PROJECT.AMT, SctIdGenerator.NAMESPACE.NEHTA, SctIdGenerator.TYPE.CONCEPT);
         fail("Generator should have thrown an error");
      } catch (Exception e) {
            // expected exception at 0 index;
      }
      try {
         assertEquals("1011000036106", SctIdGenerator.generate(1, SctIdGenerator.PROJECT.AMT, SctIdGenerator.NAMESPACE.NEHTA, SctIdGenerator.TYPE.CONCEPT));
         
      
      } catch (Exception e) {
         fail("exception: " + e.getLocalizedMessage());
      }
      
   }

   public void testVerhoeffCheck() {
      // From http://en.wikipedia.org/wiki/Verhoeff_algorithm#Example
      assertEquals(true, SctIdGenerator.verhoeffCheck("1428570"));
      assertEquals(false, SctIdGenerator.verhoeffCheck("1428571"));
      assertEquals(false, SctIdGenerator.verhoeffCheck("1428572"));
      assertEquals(false, SctIdGenerator.verhoeffCheck("1428573"));
      assertEquals(false, SctIdGenerator.verhoeffCheck("1428574"));
      assertEquals(false, SctIdGenerator.verhoeffCheck("1428575"));
      assertEquals(false, SctIdGenerator.verhoeffCheck("1428576"));
      assertEquals(false, SctIdGenerator.verhoeffCheck("1428577"));
      assertEquals(false, SctIdGenerator.verhoeffCheck("1428578"));
      assertEquals(false, SctIdGenerator.verhoeffCheck("1428579"));
   }

   public void testVerhoeffCompute() {
      // From http://en.wikipedia.org/wiki/Verhoeff_algorithm#Example
      assertEquals(0, SctIdGenerator.verhoeffCompute("142857"));
   }

}
