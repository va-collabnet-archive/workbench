package org.ihtsdo.arena.conceptview;

import java.awt.datatransfer.DataFlavor;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

public class DragPanelDataFlavors {
	
	public static DataFlavor relGroupFlavor;
		
	public static DataFlavor descVersionFlavor;
	
	public static DataFlavor relVersionFlavor;
	
	public static DataFlavor conceptFlavor;
	
	public static DataFlavor[] dragPanelFlavors;

	public static Set<DataFlavor> dragPanelFlavorSet;

	static {
		try {
			relGroupFlavor = new DataFlavor(
					"application/x-java-jvm-local-objectref;class=" + 
					RelGroupVersionBI.class.getName()) ;
			descVersionFlavor = new DataFlavor(
					"application/x-java-jvm-local-objectref;class=" + 
					DescriptionVersionBI.class.getName());
			relVersionFlavor = new DataFlavor(
					"application/x-java-jvm-local-objectref;class=" + 
						RelationshipVersionBI.class.getName());
			conceptFlavor = new DataFlavor(
					"application/x-java-jvm-local-objectref;class=" + 
					I_GetConceptData.class.getName());
			
			dragPanelFlavors = new DataFlavor[] { relGroupFlavor, descVersionFlavor, relVersionFlavor, conceptFlavor };
			
			dragPanelFlavorSet = new HashSet<DataFlavor>();
			for (DataFlavor f: dragPanelFlavors) {
				dragPanelFlavorSet.add(f);
			}
			
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
			
	}

}
