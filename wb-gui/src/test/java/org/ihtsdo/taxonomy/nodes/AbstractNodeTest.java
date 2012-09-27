
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy.nodes;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.taxonomy.model.TaxonomyModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author kec
 */
public class AbstractNodeTest {
   public AbstractNodeTest() {}

   //~--- methods -------------------------------------------------------------

   @After
   public void tearDown() {}

   @AfterClass
   public static void tearDownClass() throws Exception {}

   /**
    * Test of getCnid method, of class AbstractNode.
    */
   @Test
   public void testGetCnid() {
      getCnid(1, -1);
      getCnid(-1, 1);
      getCnid(Integer.MAX_VALUE, Integer.MIN_VALUE);
      getCnid(Integer.MIN_VALUE, Integer.MAX_VALUE);
      getCnid(0, Integer.MAX_VALUE);
      getCnid(Integer.MIN_VALUE, 0);
      getCnid(-1, Integer.MAX_VALUE);
      getCnid(Integer.MIN_VALUE, -1);
      getCnid(1, Integer.MAX_VALUE);
      getCnid(Integer.MIN_VALUE, 1);
   }

   /**
    * Test of getParentNid method, of class AbstractNode.
    */
   @Test
   public void testGetParentNid() {
      getParentNid(1, -1);
      getParentNid(-1, 1);
      getParentNid(Integer.MAX_VALUE, Integer.MIN_VALUE);
      getParentNid(Integer.MIN_VALUE, Integer.MAX_VALUE);
      getParentNid(0, Integer.MAX_VALUE);
      getParentNid(Integer.MIN_VALUE, 0);
      getParentNid(-1, Integer.MAX_VALUE);
      getParentNid(Integer.MIN_VALUE, -1);
      getParentNid(1, Integer.MAX_VALUE);
      getParentNid(Integer.MIN_VALUE, 1);
   }

   //~--- get methods ---------------------------------------------------------

   private void getCnid(int cnid, int parentNid) {
      long nodeId = TaxonomyModel.getNodeId(cnid, parentNid);
      int  result = TaxonomyModel.getCnid(nodeId);

      assertEquals(cnid, result);
   }

   private void getParentNid(int cnid, int parentNid) {
      long nodeId = TaxonomyModel.getNodeId(cnid, parentNid);
      int  result = TaxonomyModel.getParentNid(nodeId);

      assertEquals(parentNid, result);
   }

   //~--- set methods ---------------------------------------------------------

   @Before
   public void setUp() {}

   @BeforeClass
   public static void setUpClass() throws Exception {}
}
