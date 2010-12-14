package org.ihtsdo.concept;

import java.util.Comparator;
import org.ihtsdo.tk.api.ComponentChroncileBI;

public class ComponentComparator implements Comparator<ComponentChroncileBI> {

    @Override
    public int compare(ComponentChroncileBI o1, ComponentChroncileBI o2) {
        return o1.getNid() - o2.getNid();
    }
}
