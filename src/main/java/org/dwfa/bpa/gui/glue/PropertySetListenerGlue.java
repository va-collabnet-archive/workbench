/*
 * Created on Jun 27, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui.glue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class PropertySetListenerGlue {

    private String removeMethodName, addMethodName, replaceMethodName, getSetMethodName;
    private Class<?> methodArgClass;
    private Object target;
    /**
     * @param property
     * @param target
     */
    public PropertySetListenerGlue(String removeMethodName, String addMethodName, 
        String replaceMethodName, String getSetMethodName, Class<?> methodArgClass, Object target) {
        this.removeMethodName = removeMethodName;
        this.addMethodName = addMethodName;
        this.replaceMethodName = replaceMethodName;
        this.getSetMethodName = getSetMethodName;
        this.methodArgClass = methodArgClass;
        this.target = target;
    }

    public void removeObj(Object obj) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (this.removeMethodName == null) {
            return;
        }
        Method targetMethod = target.getClass().getMethod(this.removeMethodName, new Class[] {methodArgClass});
        targetMethod.invoke(target, new Object[] {obj});
    }
    public void addObj(Object obj) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (this.addMethodName == null) {
            return;
        }
        Method targetMethod = target.getClass().getMethod(this.addMethodName, new Class[] {methodArgClass});
        targetMethod.invoke(target, new Object[] {obj});
    }
    public void replaceObj(Object oldObj, Object newObj) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (this.replaceMethodName == null) {
            return;
        }
        Method targetMethod = target.getClass().getMethod(this.replaceMethodName, new Class[] {methodArgClass, methodArgClass});
        targetMethod.invoke(target, new Object[] {oldObj, newObj});
    }
    public Set<?> getSet() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (this.getSetMethodName == null) {
            return new HashSet<Object>();
        }
        Method targetMethod = target.getClass().getMethod(this.getSetMethodName, new Class[] {});
        return (Set<?>) targetMethod.invoke(target, new Object[] {});
    }
}
