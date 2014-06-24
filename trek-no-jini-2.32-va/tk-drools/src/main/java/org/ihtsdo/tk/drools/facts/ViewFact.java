package org.ihtsdo.tk.drools.facts;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class ViewFact <T extends View> extends Fact<T>{
        protected View view;
	
	protected ViewFact(View view) {
		super();
                this.view = view;
	}
	
	public View getView() {
		return view;
	}

	@Override
	public String toString() {
		return "View :" + view;
	}

}
