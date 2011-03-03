package org.ihtsdo.arena.conceptview;

import java.awt.datatransfer.DataFlavor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

public class DragPanelDataFlavors {

    public static final DataFlavor relGroupFlavor;
    public static final DataFlavor descVersionFlavor;
    public static final DataFlavor relVersionFlavor;
    public static final DataFlavor conAttrVersionFlavor;
    public static final DataFlavor conceptFlavor;
    public static final DataFlavor[] dragPanelFlavors;
    public static final Set<DataFlavor> dragPanelFlavorSet;

    static {
        try {
            conAttrVersionFlavor = new DataFlavor(
                    "application/x-java-jvm-local-objectref;class="
                    + ConAttrVersionBI.class.getName());
            relGroupFlavor = new DataFlavor(
                    "application/x-java-jvm-local-objectref;class="
                    + RelGroupVersionBI.class.getName());
            descVersionFlavor = new DataFlavor(
                    "application/x-java-jvm-local-objectref;class="
                    + DescriptionVersionBI.class.getName());
            relVersionFlavor = new DataFlavor(
                    "application/x-java-jvm-local-objectref;class="
                    + RelationshipVersionBI.class.getName());
            conceptFlavor = new DataFlavor(
                    "application/x-java-jvm-local-objectref;class="
                    + I_GetConceptData.class.getName());

            dragPanelFlavors = new DataFlavor[]{relGroupFlavor, descVersionFlavor, relVersionFlavor, conceptFlavor, conAttrVersionFlavor};

            dragPanelFlavorSet = new HashSet<DataFlavor>();

            dragPanelFlavorSet.addAll(Arrays.asList(dragPanelFlavors));

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
