package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;    
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelSpec;

public class FactFactory {

    public static Fact<?> get(Context context, Object component, ViewCoordinate vc) {
        if (ConAttrVersionBI.class.isAssignableFrom(component.getClass())) {
            return new ConAttrFact(context, (ConAttrVersionBI) component, vc);
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
        if (RelGroupVersionBI.class.isAssignableFrom(component.getClass())) {
            return new RelGroupFact(context, (RelGroupVersionBI) component, vc);
        }
        if (RelGroupVersionBI.class.isAssignableFrom(component.getClass())) {
            return new RelGroupFact(context, (RelGroupVersionBI) component, vc);
        }
        if (RelSpec.class.isAssignableFrom(component.getClass())) {
            return new RelSpecFact(context, (RelSpec) component);
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
}
