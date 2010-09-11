package org.ihtsdo.arena.conceptview;


import java.awt.datatransfer.DataFlavor;

import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DragPanelDataFlavorsTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void test() throws Exception {
		@SuppressWarnings("unused")
		DataFlavor relGroupFlavor = new DataFlavor(
				"application/x-java-jvm-local-objectref;class=" + 
					RelGroupChronicleBI.class.getName()) ;
		@SuppressWarnings("unused")
		DataFlavor descVersionFlavor = new DataFlavor(
				"application/x-java-jvm-local-objectref;class=" + 
					DescriptionChronicleBI.class.getName());
		@SuppressWarnings("unused")
		DataFlavor relVersionFlavor = new DataFlavor(
				"application/x-java-jvm-local-objectref;class=" + 
					RelationshipChronicleBI.class.getName());
	}
	

	@After
	public void tearDown() throws Exception {
	}

}
