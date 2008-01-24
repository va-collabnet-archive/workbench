/*
 * Created on Feb 8, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui.glue;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;

/**
 * @author kec
 *
 */
public class ItemListenerGlue implements ItemListener {
    private String methodName;
    private Class<?>[] methodArgClasses;
    private Object target;
    /**
     * @param property
     * @param target
     */
    public ItemListenerGlue(String methodName, Object target) {
        this.methodName = methodName;
        this.methodArgClasses = new Class[] {boolean.class};
        this.target = target;
    }

	/**
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent evt) {
        try {
            Method targetMethod = target.getClass().getMethod(this.methodName, this.methodArgClasses);
             targetMethod.invoke(target, new Object[] {new Boolean(evt.getStateChange() == ItemEvent.SELECTED)});
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
