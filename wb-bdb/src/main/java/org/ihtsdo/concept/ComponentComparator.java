package org.ihtsdo.concept;

import java.util.Comparator;
import org.ihtsdo.tk.api.ComponentChronicleBI;

public class ComponentComparator implements Comparator<ComponentChronicleBI> {

    @Override
    public int compare(ComponentChronicleBI o1, ComponentChronicleBI o2) {
        return o1.getNid() - o2.getNid();
    }
}
