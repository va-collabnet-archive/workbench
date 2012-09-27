/*
 * 
 */
package org.ihtsdo.mojo.schema.test;


import org.ihtsdo.mojo.schema.config.TransformersConfigApi;

import junit.framework.TestCase;


// TODO: Auto-generated Javadoc
/**
 * The Class TestConfigApi.
 */
public class TestConfigApi extends TestCase {
	
	/**
	 * Test api.
	 */
	public void testApi() {
		TransformersConfigApi api = new TransformersConfigApi("transformConfig.xml");
		
		assertEquals(2, api.getAllTransformersNames().size());
		assertEquals(0, api.getIntId("boolean-to-enumerated"));
		System.out.println(0 + "," + api.getIntId("boolean-to-enumerated")); 
		assertEquals(1, api.getIntId("enumerated-to-boolean"));
		System.out.println(1 + "," + api.getIntId("enumerated-to-boolean")); 
		assertEquals("org.ihtsdo.mojo.schema.transformer.BooleanToEnumeratedTransformer", 
				api.getValueAt(api.getIntId("boolean-to-enumerated"), "class"));
		System.out.println("org.ihtsdo.mojo.schema.transformer.BooleanToEnumeratedTransformer" + "," +
				api.getValueAt(api.getIntId("boolean-to-enumerated"), "class")); 
		assertEquals("org.ihtsdo.mojo.schema.transformer.EnumeratedToBooleanTransformer", 
				api.getValueAt(api.getIntId("enumerated-to-boolean"), "class"));
		System.out.println("org.ihtsdo.mojo.schema.transformer.EnumeratedToBooleanTransformer" + "," +
				api.getValueAt(api.getIntId("enumerated-to-boolean"), "class")); 
		
		for (String loopValue : api.getCollectionAt(api.getIntId("enumerated-to-boolean"), "parameters.valuesForTrue.uuid")) {
			System.out.println("Param for true: " + loopValue);
		}
		
		for (String loopValue : api.getCollectionAt(api.getIntId("enumerated-to-boolean"), "parameters.valuesForFalse.uuid")) {
			System.out.println("Param for false: " + loopValue);
		}
		
	}

}
