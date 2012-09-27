package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class DescFact extends ComponentFact<DescriptionVersionBI> {

    public DescFact(Context context, DescriptionVersionBI component, ViewCoordinate vc) {
        super(context, component, vc);
    }

    public DescriptionVersionBI getDesc() {
        return component;
    }
}
