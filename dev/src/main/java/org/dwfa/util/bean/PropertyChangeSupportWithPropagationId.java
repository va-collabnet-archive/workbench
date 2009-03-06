package org.dwfa.util.bean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;

public class PropertyChangeSupportWithPropagationId extends
		PropertyChangeSupport {

	private long propagationId = 0;
	public PropertyChangeSupportWithPropagationId(Object sourceBean) {
		super(sourceBean);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void firePropertyChange(PropertyChangeEvent evt) {
		evt.setPropagationId(propagationId++);
		super.firePropertyChange(evt);
	}
	
}
