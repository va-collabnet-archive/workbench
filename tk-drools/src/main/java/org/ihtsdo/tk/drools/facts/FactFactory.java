package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelSpec;

public class FactFactory {

	public static Fact<?> get(Context context, Object component) {
		if (ConAttrVersionBI.class.isAssignableFrom(component.getClass())) {
			return new ConAttrFact(context, (ConAttrVersionBI) component);
		}
		if (ConceptVersionBI.class.isAssignableFrom(component.getClass())) {
			return new ConceptFact(context, (ConceptVersionBI) component);
		}
		if (DescriptionVersionBI.class.isAssignableFrom(component.getClass())) {
			return new DescFact(context, (DescriptionVersionBI) component);
		}
		if (RelationshipVersionBI.class.isAssignableFrom(component.getClass())) {
			return new RelFact(context, (RelationshipVersionBI) component);
		}
		if (RelGroupVersionBI.class.isAssignableFrom(component.getClass())) {
			return new RelGroupFact(context, (RelGroupVersionBI) component);
		}
		if (RelGroupVersionBI.class.isAssignableFrom(component.getClass())) {
			return new RelGroupFact(context, (RelGroupVersionBI) component);
		}
		if (RelSpec.class.isAssignableFrom(component.getClass())) {
			return new RelSpecFact(context, (RelSpec) component);
		}
		if (DescriptionSpec.class.isAssignableFrom(component.getClass())) {
			return new DescSpecFact(context, (DescriptionSpec) component);
		}
		
		
		throw new UnsupportedOperationException("Can't handle component: " + 
				component);
	}
}
