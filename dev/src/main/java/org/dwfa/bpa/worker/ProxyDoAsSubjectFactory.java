/*
 * Created on Dec 7, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.worker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;

import org.dwfa.bpa.process.I_Work;



public class ProxyDoAsSubjectFactory {
	I_Work worker;


    /**
     * @param subject
     */
    public ProxyDoAsSubjectFactory(I_Work worker) {
        super();
        this.worker = worker;
    }

    public Object makeProxy(Object obj, Class interfaceClass) {
        Handler handler = new Handler(obj);
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(),
                new Class[] { interfaceClass }, handler);
    }

    private class Handler implements InvocationHandler {
        Object rootObj;

        /**
         * @param obj
         */
        public Handler(Object obj) {
            super();
            rootObj = obj;
        }

        public Object invoke(Object proxy, final Method method,
                final Object[] args) throws Throwable {
                if (worker.getLoginContext() == null) {
                    return invokeNoSubject(method, args);
                }
                return invokeAsSubject(method, args);
        }
        private Object invokeNoSubject(Method method,
                Object[] args) throws Throwable {
            return method.invoke(rootObj, args);
        }
        private Object invokeAsSubject(final Method method,
                final Object[] args) throws Throwable {
            return Subject.doAs(worker.getLoginContext().getSubject(), new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    return method.invoke(rootObj, args);
                }
            });
        }

    }

	/**
	 * @return Returns the worker.
	 */
	protected I_Work getWorker() {
		return worker;
	}

	/**
	 * @param worker The worker to set.
	 */
	protected void setWorker(I_Work worker) {
		this.worker = worker;
	}


}
