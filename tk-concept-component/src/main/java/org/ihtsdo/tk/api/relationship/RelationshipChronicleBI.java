package org.ihtsdo.tk.api.relationship;

import org.ihtsdo.tk.api.ComponentChronicleBI;

public interface RelationshipChronicleBI extends ComponentChronicleBI<RelationshipVersionBI> {

    int getSourceNid();

    int getTargetNid();
}
