package org.ihtsdo.tk.binding.snomed;

import org.ihtsdo.tk.api.constraint.DescriptionConstraint;
import org.ihtsdo.tk.api.constraint.RelationshipConstraintTarget;
import org.ihtsdo.tk.api.constraint.RelationshipConstraintSource;

public class SnomedConstraints {

    public static RelationshipConstraintSource FINDING_SITE_OUT =
            SnomedRelationshipSpec.FINDING_SITE.getSourceRelationshipConstraint();
    public static RelationshipConstraintTarget FINDING_SITE_IN =
            SnomedRelationshipSpec.FINDING_SITE.getTargetRelationshipConstraint();
    public static DescriptionConstraint FS_SEMANTIC_TAG =
            new DescriptionConstraint(Taxonomies.SNOMED,
            WbDescType.FULLY_SPECIFIED, "\\(.*\\)$");
}
