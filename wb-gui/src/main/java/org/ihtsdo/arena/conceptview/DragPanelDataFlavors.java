package org.ihtsdo.arena.conceptview;

import java.awt.datatransfer.DataFlavor;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_RelTuple;

public class DragPanelDataFlavors {
	
	public static DataFlavor relGroupFlavor;
		
	public static DataFlavor descVersionFlavor;
	
	public static DataFlavor relVersionFlavor;
	
	public static DataFlavor[] dragPanelFlavors;

	public static Set<DataFlavor> dragPanelFlavorSet;

	static {
		try {
			relGroupFlavor = new DataFlavor(
					"application/x-java-jvm-local-objectref;class=" + 
						RelGroupForDragPanel.class.getName()) ;
			descVersionFlavor = new DataFlavor(
					"application/x-java-jvm-local-objectref;class=" + 
						I_DescriptionTuple.class.getName());
			relVersionFlavor = new DataFlavor(
					"application/x-java-jvm-local-objectref;class=" + 
						I_RelTuple.class.getName());
			dragPanelFlavors = new DataFlavor[] { relGroupFlavor, descVersionFlavor, relVersionFlavor };
			
			dragPanelFlavorSet = new HashSet<DataFlavor>();
			for (DataFlavor f: dragPanelFlavors) {
				dragPanelFlavorSet.add(f);
			}
			
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
			
	}

}
