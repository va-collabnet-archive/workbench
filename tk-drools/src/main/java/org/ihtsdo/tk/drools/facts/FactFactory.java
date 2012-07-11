package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;    
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelationshipSpec;

public class FactFactory {

    public static Fact<?> get(Context context, Object component, ViewCoordinate vc) {
        if (ConceptAttributeVersionBI.class.isAssignableFrom(component.getClass())) {
            return new ConAttrFact(context, (ConceptAttributeVersionBI) component, vc);
        }
        if (ConceptVersionBI.class.isAssignableFrom(component.getClass())) {
            return new ConceptFact(context, (ConceptVersionBI) component, vc);
        }
        if (DescriptionVersionBI.class.isAssignableFrom(component.getClass())) {
            return new DescFact(context, (DescriptionVersionBI) component, vc);
        }
        if (RelationshipVersionBI.class.isAssignableFrom(component.getClass())) {
            return new RelFact(context, (RelationshipVersionBI) component, vc);
        }
        if (RelationshipGroupVersionBI.class.isAssignableFrom(component.getClass())) {
            return new RelGroupFact(context, (RelationshipGroupVersionBI) component, vc);
        }
        if (RelationshipGroupVersionBI.class.isAssignableFrom(component.getClass())) {
            return new RelGroupFact(context, (RelationshipGroupVersionBI) component, vc);
        }
        if (RelationshipSpec.class.isAssignableFrom(component.getClass())) {
            return new RelSpecFact(context, (RelationshipSpec) component);
        }
        if (DescriptionSpec.class.isAssignableFrom(component.getClass())) {
            return new DescSpecFact(context, (DescriptionSpec) component);
        }
        if (RefexVersionBI.class.isAssignableFrom(component.getClass())) {
            return new RefexFact(context, (RefexVersionBI) component, vc);
        }
        throw new UnsupportedOperationException("Can't handle component: "
                + component);
    }
    
    public static Fact<?> get(View view) {
        return new ViewFact(view);
    }
}
