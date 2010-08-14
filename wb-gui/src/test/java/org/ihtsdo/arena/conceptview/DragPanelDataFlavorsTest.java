package org.ihtsdo.arena.conceptview;


import java.awt.datatransfer.DataFlavor;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_RelTuple;
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
		DataFlavor relGroupFlavor = new DataFlavor("application/x-java-jvm-local-objectref;class=" + 
				RelGroupForDragPanel.class.getName()) ;
		@SuppressWarnings("unused")
		DataFlavor descVersionFlavor = new DataFlavor("application/x-java-jvm-local-objectref;class=" + 
				I_DescriptionTuple.class.getName());
		@SuppressWarnings("unused")
		DataFlavor relVersionFlavor = new DataFlavor("application/x-java-jvm-local-objectref;class=" + 
				I_RelTuple.class.getName());
	}
	

	@After
	public void tearDown() throws Exception {
	}

}
