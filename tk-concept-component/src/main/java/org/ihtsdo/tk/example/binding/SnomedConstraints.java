package org.ihtsdo.tk.example.binding;

import org.ihtsdo.tk.api.constraint.DescriptionConstraint;
import org.ihtsdo.tk.api.constraint.RelConstraintIncoming;
import org.ihtsdo.tk.api.constraint.RelConstraintOutgoing;

public class SnomedConstraints {

    public static RelConstraintOutgoing FINDING_SITE_OUT =
            SnomedRelSpec.FINDING_SITE.getOriginatingRelConstraint();
    public static RelConstraintIncoming FINDING_SITE_IN =
            SnomedRelSpec.FINDING_SITE.getDestinationRelConstraint();
    public static DescriptionConstraint FS_SEMANTIC_TAG =
            new DescriptionConstraint(Taxonomies.SNOMED,
            WbDescType.FULLY_SPECIFIED, "\\(.*\\)$");
}
