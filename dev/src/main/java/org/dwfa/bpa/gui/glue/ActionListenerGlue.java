/*
 * Created on Feb 8, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui.glue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

/**
 * @author kec
 *
 */
public class ActionListenerGlue implements ActionListener {

    private String methodName;
    private Class[] methodArgClasses;
    private Object[] methodArgs;
    private Object target;
    /**
     * @param property
     * @param target
     */
    public ActionListenerGlue(String methodName, boolean stateToSet, Object target) {
        this.methodName = methodName;
        this.methodArgClasses = new Class[] {boolean.class};
        this.methodArgs = new Object[] {new Boolean(stateToSet)};
        this.target = target;
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        try {
            Method targetMethod = target.getClass().getMethod(this.methodName, this.methodArgClasses);
            targetMethod.invoke(target, this.methodArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

 }
